
package com.networknt.oauth.service;

import com.networknt.config.Config;
import com.networknt.handler.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Methods;
import com.networknt.info.ServerInfoGetHandler;
import com.networknt.health.HealthGetHandler;
import com.networknt.oauth.service.handler.*;

public class PathHandlerProvider implements HandlerProvider {
    @Override
    public HttpHandler getHandler() {
        return Handlers.routing()
        
            .add(Methods.POST, "/oauth2/service", new Oauth2ServicePostHandler())
        
            .add(Methods.PUT, "/oauth2/service", new Oauth2ServicePutHandler())
        
            .add(Methods.GET, "/oauth2/service", new Oauth2ServiceGetHandler())
        
            .add(Methods.POST, "/oauth2/service/{serviceId}/endpoint", new Oauth2ServiceServiceIdEndpointPostHandler())
        
            .add(Methods.DELETE, "/oauth2/service/{serviceId}/endpoint", new Oauth2ServiceServiceIdEndpointDeleteHandler())
        
            .add(Methods.GET, "/oauth2/service/{serviceId}/endpoint", new Oauth2ServiceServiceIdEndpointGetHandler())
        
            .add(Methods.DELETE, "/oauth2/service/{serviceId}", new Oauth2ServiceServiceIdDeleteHandler())
        
            .add(Methods.GET, "/oauth2/service/{serviceId}", new Oauth2ServiceServiceIdGetHandler())
        
            .add(Methods.GET, "/health", new HealthGetHandler())
        
            .add(Methods.GET, "/server/info", new ServerInfoGetHandler())
        
        ;
    }
}
