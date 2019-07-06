package com.networknt.oauth.code.handler;

import com.hazelcast.core.IMap;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Client;
import com.networknt.status.Status;
import com.networknt.utility.Util;
import io.undertow.UndertowLogger;
import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.SecurityContext;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
        final String clientId = jClientId.getValue();
        String redirectUri = jRedirectUri == null ? null : jRedirectUri.getValue();
        final String state = jState == null ? null : jState.getValue();

        // check if the client_id is valid
        IMap<String, Client> clients = CacheStartupHookProvider.hz.getMap("clients");
        Client client = clients.get(clientId);
        if(client == null) {
            setExchangeStatus(exchange, CLIENT_NOT_FOUND, clientId);
            processAudit(exchange);
        } else {
            final SecurityContext context = exchange.getSecurityContext();
            String userId = context.getAuthenticatedAccount().getPrincipal().getName();
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
