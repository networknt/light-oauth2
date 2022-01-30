package com.networknt.oauth.client.handler;

import com.hazelcast.map.IMap;
import com.networknt.config.Config;
import com.networknt.config.JsonMapper;
import com.networknt.handler.LightHttpHandler;
import com.networknt.httpstring.AttachmentConstants;
import com.networknt.oauth.cache.AuditInfoHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.AuditInfo;
import com.networknt.oauth.cache.model.Client;
import com.networknt.oauth.cache.model.Oauth2Service;
import com.networknt.security.JwtVerifier;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Oauth2ClientClientIdDeleteHandler  extends ClientAuditHandler implements LightHttpHandler {
    private static Logger logger = LoggerFactory.getLogger(Oauth2ClientClientIdDeleteHandler.class);
    private static final String CLIENT_NOT_FOUND = "ERR12014";
    private static final String OPENAPI_SECURITY_CONFIG = "openapi-security";
    private static final String ENABLE_VERIFY_JWT = "enableVerifyJwt";
    private static final String INCORRECT_TOKEN_TYPE = "ERR11601";
    private static final String PERMISSION_DENIED = "ERR11620";

    private static boolean enableSecurity = false;
    static {
        Map<String, Object> config = Config.getInstance().getJsonMapConfig(OPENAPI_SECURITY_CONFIG);
        // fallback to generic security.yml
        if(config == null) config = Config.getInstance().getJsonMapConfig(JwtVerifier.SECURITY_CONFIG);
        Object object = config.get(ENABLE_VERIFY_JWT);
        enableSecurity = object != null && Boolean.valueOf(object.toString());
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String clientId = exchange.getQueryParameters().get("clientId").getFirst();

        IMap<String, Client> clients = CacheStartupHookProvider.hz.getMap("clients");
        Client client = clients.get(clientId);
        if(client == null) {
            setExchangeStatus(exchange, CLIENT_NOT_FOUND, clientId);
        } else {
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
            Client c = Client.copyClient(client);
            c.setClientSecret(null); // remove the client secret
            clients.delete(clientId);
            exchange.getResponseSender().send(JsonMapper.toJson(c));
        }
        processAudit(exchange);
    }
}
