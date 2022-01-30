package com.networknt.oauth.token.handler;

import com.hazelcast.map.IMap;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.RefreshToken;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Oauth2RefreshTokenRefreshTokenDeleteHandler extends RefreshTokenAuditHandler implements LightHttpHandler {
    private static final String REFRESH_TOKEN_NOT_FOUND = "ERR12029";

    private static Logger logger = LoggerFactory.getLogger(Oauth2RefreshTokenRefreshTokenDeleteHandler.class);
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String refreshToken = exchange.getQueryParameters().get("refreshToken").getFirst();
        if(logger.isDebugEnabled()) logger.debug("refreshToken = " + refreshToken);
        IMap<String, RefreshToken> tokens = CacheStartupHookProvider.hz.getMap("tokens");
        if(tokens.get(refreshToken) == null) {
            setExchangeStatus(exchange, REFRESH_TOKEN_NOT_FOUND, refreshToken);
        } else {
            tokens.delete(refreshToken);
        }
        processAudit(exchange);
    }
}
