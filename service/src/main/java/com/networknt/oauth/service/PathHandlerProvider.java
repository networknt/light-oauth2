package com.networknt.oauth.service;

import com.networknt.oauth.service.handler.*;
import com.networknt.server.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.util.Methods;

public class PathHandlerProvider implements HandlerProvider {
    @Override
    public HttpHandler getHandler() {
        HttpHandler handler = Handlers.routing()
            .add(Methods.GET, "/oauth2/service", new Oauth2ServiceGetHandler())
            .add(Methods.POST, "/oauth2/service", new Oauth2ServicePostHandler())
            .add(Methods.PUT, "/oauth2/service", new Oauth2ServicePutHandler())
            .add(Methods.DELETE, "/oauth2/service/{serviceId}", new Oauth2ServiceServiceIdDeleteHandler())
            .add(Methods.GET, "/oauth2/service/{serviceId}", new Oauth2ServiceServiceIdGetHandler())
        ;
        return handler;
    }
}

