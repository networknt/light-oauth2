package com.networknt.oauth.authorize.handler;

import com.hazelcast.map.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.AuditInfoHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.AuditInfo;
import com.networknt.oauth.cache.model.Client;
import com.networknt.oauth.cache.model.Oauth2Service;
import com.networknt.utility.Util;
import io.undertow.security.api.SecurityContext;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Oauth2AuthorizePostHandler extends AuditInfoHandler implements LightHttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2AuthorizeGetHandler.class);
    static final String CLIENT_NOT_FOUND = "ERR12014";
    private final static String CONFIG = "oauth_authorize";
    private final static OauthAuthConfig oauth_config = (OauthAuthConfig) Config.getInstance().getJsonObjectConfig(CONFIG, OauthAuthConfig.class);

    static final String DEFAULT_AUTHENTICATE_CLASS = "com.networknt.oauth.code.auth.FormAuthentication";
    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

        // parse all the parameters here as this is a redirected get request.
        Map<String, String> params = new HashMap<>();
        Map<String, Deque<String>> pnames = exchange.getQueryParameters();
        for (Map.Entry<String, Deque<String>> entry : pnames.entrySet()) {
            String pname = entry.getKey();
            Iterator<String> pvalues = entry.getValue().iterator();
            if(pvalues.hasNext()) {
                params.put(pname, pvalues.next());
            }
        }
        if(logger.isDebugEnabled()) logger.debug("params", params);
        String clientId = params.get("client_id");
        // check if the client_id is valid
        IMap<String, Client> clients = CacheStartupHookProvider.hz.getMap("clients");
        Client client = clients.get(clientId);
        if(client == null) {
            setExchangeStatus(exchange, CLIENT_NOT_FOUND, clientId);
            processAudit(exchange);
        } else {
                /*
                String clazz = (String)client.get("authenticateClass");
                if(clazz == null) clazz = DEFAULT_AUTHENTICATE_CLASS;
                Authentication auth = (Authentication)Class.forName(clazz).getConstructor().newInstance();
                String userId = auth.authenticate(exchange);
                if(userId == null) {
                    Status status = new Status(MISSING_AUTHORIZATION_HEADER, clientId);
                    exchange.setStatusCode(status.getStatusCode());
                    exchange.getResponseSender().send(status.toString());
                    return;
                }
                */
            final SecurityContext context = exchange.getSecurityContext();
            String userId = context.getAuthenticatedAccount().getPrincipal().getName();
            Map<String, String> codeMap = new HashMap<>();
            codeMap.put("userId", userId);
            // generate auth code
            String code = Util.getUUID();
            String redirectUri = params.get("redirect_uri");
            if(redirectUri == null) {
                redirectUri = client.getRedirectUri();
            } else {
                codeMap.put("redirectUri", redirectUri);
            }
            CacheStartupHookProvider.hz.getMap("codes").set(code, codeMap);

            redirectUri = redirectUri + "?code=" + code;
            String state = params.get("state");
            if(state != null) {
                redirectUri = redirectUri + "&state=" + state;
            }
            if(logger.isDebugEnabled()) logger.debug("redirectUri = " + redirectUri);
            // now redirect here.
            exchange.setStatusCode(StatusCodes.FOUND);
            exchange.getResponseHeaders().put(Headers.LOCATION, redirectUri);
            exchange.endExchange();
            processAudit(exchange);
        }
    }


    public void processAudit(HttpServerExchange exchange) throws Exception {
        if (oauth_config.isEnableAudit() ) {
            AuditInfo auditInfo = new AuditInfo();
            auditInfo.setServiceId(Oauth2Service.AUTHORIZE);
            auditInfo.setEndpoint(exchange.getHostName() + exchange.getRelativePath());
            auditInfo.setRequestHeader(exchange.getRequestHeaders().toString());
            auditInfo.setRequestBody(Config.getInstance().getMapper().writeValueAsString(exchange.getAttachment(BodyHandler.REQUEST_BODY)));
            auditInfo.setResponseCode(exchange.getStatusCode());
            auditInfo.setResponseHeader(exchange.getResponseHeaders().toString());
            auditInfo.setResponseBody(Config.getInstance().getMapper().writeValueAsString(exchange.getResponseCookies()));
            saveAudit(auditInfo);
        }
    }
}
