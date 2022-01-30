package com.networknt.oauth.client.handler;

import com.hazelcast.map.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.handler.LightHttpHandler;
import com.networknt.httpstring.AttachmentConstants;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Client;
import com.networknt.security.JwtVerifier;
import com.networknt.utility.HashUtil;
import com.networknt.utility.Util;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

public class Oauth2ClientPostHandler  extends ClientAuditHandler implements LightHttpHandler {
    private static Logger logger = LoggerFactory.getLogger(Oauth2ClientPostHandler.class);
    private static final String CLIENT_ID_EXISTS = "ERR12019";
    private static final String DEREF_NOT_EXTERNAL = "ERR12043";
    private static final String INCORRECT_TOKEN_TYPE = "ERR11601";

    private static final String OPENAPI_SECURITY_CONFIG = "openapi-security";
    private static final String ENABLE_VERIFY_JWT = "enableVerifyJwt";
    private static boolean enableSecurity = false;
    static {
        Map<String, Object> config = Config.getInstance().getJsonMapConfig(OPENAPI_SECURITY_CONFIG);
        // fallback to generic security.yml
        if(config == null) config = Config.getInstance().getJsonMapConfig(JwtVerifier.SECURITY_CONFIG);
        Object object = config.get(ENABLE_VERIFY_JWT);
        enableSecurity = object != null && Boolean.valueOf(object.toString());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> body = (Map<String, Object>)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        Client client = Config.getInstance().getMapper().convertValue(body, Client.class);
        if(client.getDerefClientId() != null && Client.ClientTypeEnum.EXTERNAL != client.getClientType()) {
            // only external client may have deref client id
            setExchangeStatus(exchange, DEREF_NOT_EXTERNAL);
            return;
        }
        if(enableSecurity) {
            // get userId from JWT token.
            Map<String, Object> auditInfo = exchange.getAttachment(AttachmentConstants.AUDIT_INFO);
            // the auditInfo won't be null as it passes the Jwt verification
            String userId = (String)auditInfo.get("user_id");
            if(userId == null) {
                // wrong token. client credentials token won't work here. Must be authorization code token.
                setExchangeStatus(exchange, INCORRECT_TOKEN_TYPE, "Authorization Code Token");
                return;
            }
            client.setOwnerId(userId);
        }
        // generate client_id and client_secret here.
        String clientId = UUID.randomUUID().toString();
        client.setClientId(clientId);
        String clientSecret = Util.getUUID();
        client.setClientSecret(HashUtil.generateStrongPasswordHash(clientSecret));

        IMap<String, Client> clients = CacheStartupHookProvider.hz.getMap("clients");
        if(clients.get(clientId) == null) {
            clients.set(clientId, client);
            // send the client back with client_id and client_secret
            Client c = Client.copyClient(client);
            c.setClientSecret(clientSecret);
            exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(c));
        } else {
            setExchangeStatus(exchange, CLIENT_ID_EXISTS, clientId);
        }
        processAudit(exchange);
    }
}
