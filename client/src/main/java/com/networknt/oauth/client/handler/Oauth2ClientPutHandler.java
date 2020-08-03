package com.networknt.oauth.client.handler;

import com.hazelcast.core.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.config.JsonMapper;
import com.networknt.handler.LightHttpHandler;
import com.networknt.httpstring.AttachmentConstants;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Client;
import com.networknt.oauth.cache.model.User;
import com.networknt.security.JwtVerifier;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.util.Map;

public class Oauth2ClientPutHandler  extends ClientAuditHandler implements LightHttpHandler {
    private static Logger logger = LoggerFactory.getLogger(Oauth2ClientPutHandler.class);
    private static final String CLIENT_NOT_FOUND = "ERR12014";
    private static final String DEREF_NOT_EXTERNAL = "ERR12043";
    private static final String INCORRECT_TOKEN_TYPE = "ERR11601";
    private static final String PERMISSION_DENIED = "ERR11620";

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
        Map<String, Object> body = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        Client client = Config.getInstance().getMapper().convertValue(body, Client.class);
        if(client.getDerefClientId() != null && Client.ClientTypeEnum.EXTERNAL != client.getClientType()) {
            // only external client may have deref client id
            setExchangeStatus(exchange, DEREF_NOT_EXTERNAL);
            return;
        }

        String clientId = client.getClientId();

        if(enableSecurity) {
            String ownerId = client.getOwnerId();
            Map<String, Object> auditInfo = exchange.getAttachment(AttachmentConstants.AUDIT_INFO);
            String userId = (String)auditInfo.get("user_id");
            String roles = (String)auditInfo.get("roles");
            if(userId == null) {
                setExchangeStatus(exchange, INCORRECT_TOKEN_TYPE, "Authorization Code Token");
                return;
            }
            if(!userId.equals(ownerId)) {
                // only the same user or admin can update.
                if(roles == null || !roles.contains("admin")) {
                    setExchangeStatus(exchange, PERMISSION_DENIED, roles);
                    return;
                }
            }
        }

        IMap<String, Client> clients = CacheStartupHookProvider.hz.getMap("clients");
        Client originalClient = clients.get(clientId);
        if(originalClient == null) {
            setExchangeStatus(exchange, CLIENT_NOT_FOUND, clientId);
        } else {
            // set client secret as it is not returned by query.
            Client c = Client.copyClient(client);
            client.setClientSecret(originalClient.getClientSecret());
            clients.set(clientId, client);
            exchange.getResponseSender().send(JsonMapper.toJson(c));
        }
        processAudit(exchange);
    }
}
