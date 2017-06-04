package com.networknt.oauth.token;

import com.networknt.health.HealthGetHandler;
import com.networknt.info.ServerInfoGetHandler;
import com.networknt.oauth.token.handler.Oauth2TokenPostHandler;
import com.networknt.server.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.util.Methods;

public class PathHandlerProvider implements HandlerProvider {
    @Override
    public HttpHandler getHandler() {
        HttpHandler handler = Handlers.routing()
            .add(Methods.GET, "/health", new HealthGetHandler())
            .add(Methods.GET, "/server/info", new ServerInfoGetHandler())
            .add(Methods.POST, "/oauth2/token", new Oauth2TokenPostHandler())
        ;
        return handler;
    }
}

