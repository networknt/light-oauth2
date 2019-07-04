package com.networknt.oauth.code.handler;

import com.networknt.handler.LightHttpHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class CodeGetHandlerWrapper extends BaseWrapper implements LightHttpHandler {

    HttpHandler handler;

    public CodeGetHandlerWrapper() {
        handler = addGetSecurity(new Oauth2CodeGetHandler(), basicIdentityManager);
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        handler.handleRequest(httpServerExchange);
    }


}
