package com.networknt.oauth.client;

import com.networknt.oauth.client.handler.*;
import com.networknt.server.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import com.networknt.info.ServerInfoGetHandler;
import com.networknt.health.HealthGetHandler;
import io.undertow.util.Methods;

public class PathHandlerProvider implements HandlerProvider {
    @Override
    public HttpHandler getHandler() {
        HttpHandler handler = Handlers.routing()
            .add(Methods.GET, "/health", new HealthGetHandler())
            .add(Methods.GET, "/server/info", new ServerInfoGetHandler())
            .add(Methods.DELETE, "/oauth2/client/{clientId}", new Oauth2ClientClientIdDeleteHandler())
            .add(Methods.GET, "/oauth2/client/{clientId}", new Oauth2ClientClientIdGetHandler())
            .add(Methods.GET, "/oauth2/client", new Oauth2ClientGetHandler())
            .add(Methods.POST, "/oauth2/client", new Oauth2ClientPostHandler())
            .add(Methods.PUT, "/oauth2/client", new Oauth2ClientPutHandler())
        ;
        return handler;
    }
}

