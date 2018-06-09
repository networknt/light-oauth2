package com.networknt.oauth.token.handler;

import com.hazelcast.core.IMap;
import com.networknt.config.Config;
import com.networknt.oauth.cache.AuditInfoHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.AuditInfo;
import com.networknt.oauth.cache.model.Oauth2Service;
import com.networknt.oauth.cache.model.RefreshToken;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Oauth2RefreshTokenRefreshTokenDeleteHandler extends RefreshTokenAuditHandler implements HttpHandler {
    private static final String REFRESH_TOKEN_NOT_FOUND = "ERR12029";

    private static Logger logger = LoggerFactory.getLogger(Oauth2RefreshTokenRefreshTokenDeleteHandler.class);
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String refreshToken = exchange.getQueryParameters().get("refreshToken").getFirst();
        if(logger.isDebugEnabled()) logger.debug("refreshToken = " + refreshToken);
        IMap<String, RefreshToken> tokens = CacheStartupHookProvider.hz.getMap("tokens");
        if(tokens.get(refreshToken) == null) {
            Status status = new Status(REFRESH_TOKEN_NOT_FOUND, refreshToken);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
        } else {
            tokens.delete(refreshToken);
        }
        processAudit(exchange);

    }

}
