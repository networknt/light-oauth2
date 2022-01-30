package com.networknt.oauth.code.handler;

import com.hazelcast.map.IMap;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Client;
import com.networknt.utility.Util;
import io.undertow.security.api.SecurityContext;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Oauth2CodePostHandler extends CodeAuditHandler implements LightHttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2CodeGetHandler.class);
    static final String CLIENT_NOT_FOUND = "ERR12014";

    static final String DEFAULT_AUTHENTICATE_CLASS = "com.networknt.oauth.code.auth.FormAuthentication";
    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

        // get the form from the exchange
        final FormData data = exchange.getAttachment(FormDataParser.FORM_DATA);

        final FormData.FormValue jClientId = data.getFirst("client_id");
        final FormData.FormValue jRedirectUri = data.getFirst("redirect_uri");
        final FormData.FormValue jState = data.getFirst("state");
        final FormData.FormValue jRemember = data.getFirst("remember");
        final String clientId = jClientId.getValue();
        final String remember = jRemember == null ? null : jRemember.getValue();  // should be 'Y' or 'N' if not null.
        String redirectUri = jRedirectUri == null ? null : jRedirectUri.getValue();
        final String state = jState == null ? null : jState.getValue();
        if(logger.isDebugEnabled()) {
            logger.debug("client_id = " + clientId + " state = " + state + " redirectUri = " + redirectUri + " remember = " + remember);
        }
        // check if the client_id is valid
        IMap<String, Client> clients = CacheStartupHookProvider.hz.getMap("clients");
        Client client = clients.get(clientId);
        if(client == null) {
            if(logger.isDebugEnabled()) logger.debug("client is not found for clientId = " + clientId);
            setExchangeStatus(exchange, CLIENT_NOT_FOUND, clientId);
            processAudit(exchange);
        } else {
            final SecurityContext context = exchange.getSecurityContext();
            String userId = context.getAuthenticatedAccount().getPrincipal().getName();
            if(logger.isDebugEnabled()) logger.debug("userId = " + userId);
            if("error".equals(userId)) {
                exchange.setStatusCode(StatusCodes.BAD_REQUEST);
                exchange.getResponseSender().send(context.getAuthenticatedAccount().getRoles().iterator().next());
                processAudit(exchange);
            } else {
                Set<String> roles = context.getAuthenticatedAccount().getRoles();
                Map<String, String> codeMap = new HashMap<>();
                codeMap.put("userId", userId);
                if(roles != null && !roles.isEmpty()) {
                    codeMap.put("roles", String.join(" ", roles));
                }
                // generate auth code
                String code = Util.getUUID();
                if(redirectUri == null) {
                    redirectUri = client.getRedirectUri();
                } else {
                    codeMap.put("redirectUri", redirectUri);
                }
                if(remember != null) codeMap.put("remember", remember); // pass the remember checkbox value to the token service
                CacheStartupHookProvider.hz.getMap("codes").set(code, codeMap);

                redirectUri = redirectUri + "?code=" + code;
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
    }
}
