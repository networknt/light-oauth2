package com.networknt.oauth.code.handler;

import com.networknt.handler.LightHttpHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class CodePostHandlerWrapper extends BaseWrapper implements LightHttpHandler {
    HttpHandler handler;

    public CodePostHandlerWrapper() {
        handler = addFormSecurity(new Oauth2CodePostHandler(), basicIdentityManager);
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        handler.handleRequest(httpServerExchange);
    }

}
