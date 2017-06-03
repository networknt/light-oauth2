package com.networknt.oauth.token;

import com.networknt.health.HealthGetHandler;
import com.networknt.info.ServerInfoGetHandler;
import com.networknt.oauth.token.handler.Oauth2RefreshTokenGetHandler;
import com.networknt.oauth.token.handler.Oauth2RefreshTokenRefreshTokenDeleteHandler;
import com.networknt.oauth.token.handler.Oauth2RefreshTokenRefreshTokenGetHandler;
import com.networknt.server.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.util.Methods;

public class PathHandlerProvider implements HandlerProvider {
    @Override
    public HttpHandler getHandler() {
        return Handlers.routing()
            .add(Methods.GET, "/v2/health", new HealthGetHandler())
            .add(Methods.GET, "/v2/server/info", new ServerInfoGetHandler())
            .add(Methods.GET, "/oauth2/refresh_token", new Oauth2RefreshTokenGetHandler())
            .add(Methods.DELETE, "/oauth2/refresh_token/{refreshToken}", new Oauth2RefreshTokenRefreshTokenDeleteHandler())
            .add(Methods.GET, "/oauth2/refresh_token/{refreshToken}", new Oauth2RefreshTokenRefreshTokenGetHandler())
        ;
    }
}

