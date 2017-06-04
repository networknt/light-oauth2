package com.networknt.oauth.user;

import com.networknt.health.HealthGetHandler;
import com.networknt.info.ServerInfoGetHandler;
import com.networknt.oauth.user.handler.*;
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
            .add(Methods.POST, "/oauth2/password/{userId}", new Oauth2PasswordUserIdPostHandler())
            .add(Methods.GET, "/oauth2/user", new Oauth2UserGetHandler())
            .add(Methods.POST, "/oauth2/user", new Oauth2UserPostHandler())
            .add(Methods.PUT, "/oauth2/user", new Oauth2UserPutHandler())
            .add(Methods.DELETE, "/oauth2/user/{userId}", new Oauth2UserUserIdDeleteHandler())
            .add(Methods.GET, "/oauth2/user/{userId}", new Oauth2UserUserIdGetHandler())
        ;
        return handler;
    }
}

