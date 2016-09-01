package com.networknt.oauth.handler;

import com.networknt.config.Config;
import com.networknt.server.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.PathTemplateHandler;


public class OAuthHandlerProvider implements HandlerProvider {

    public HttpHandler getHandler() {
        return Handlers.path().addPrefixPath("/oauth/token",
                new TokenHandler(Config.getInstance().getMapper()))
                .addPrefixPath("/oauth/code",
                        new CodeHandler(Config.getInstance().getMapper()));
    }
}
