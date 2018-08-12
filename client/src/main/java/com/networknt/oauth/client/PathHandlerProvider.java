
package com.networknt.oauth.client;

import com.networknt.health.HealthGetHandler;
import com.networknt.info.ServerInfoGetHandler;
import com.networknt.oauth.client.handler.*;
import com.networknt.handler.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.util.Methods;

public class PathHandlerProvider implements HandlerProvider {
    @Override
    public HttpHandler getHandler() {
        return Handlers.routing()
        
            .add(Methods.DELETE, "/oauth2/client/{clientId}/service", new Oauth2ClientClientIdServiceDeleteHandler())
        
            .add(Methods.GET, "/oauth2/client/{clientId}/service", new Oauth2ClientClientIdServiceGetHandler())
        
            .add(Methods.GET, "/health", new HealthGetHandler())
        
            .add(Methods.POST, "/oauth2/client", new Oauth2ClientPostHandler())
        
            .add(Methods.PUT, "/oauth2/client", new Oauth2ClientPutHandler())
        
            .add(Methods.GET, "/oauth2/client", new Oauth2ClientGetHandler())
        
            .add(Methods.DELETE, "/oauth2/client/{clientId}", new Oauth2ClientClientIdDeleteHandler())
        
            .add(Methods.GET, "/oauth2/client/{clientId}", new Oauth2ClientClientIdGetHandler())
        
            .add(Methods.POST, "/oauth2/client/{clientId}/service/{serviceId}", new Oauth2ClientClientIdServiceServiceIdPostHandler())
        
            .add(Methods.DELETE, "/oauth2/client/{clientId}/service/{serviceId}", new Oauth2ClientClientIdServiceServiceIdDeleteHandler())
        
            .add(Methods.GET, "/oauth2/client/{clientId}/service/{serviceId}", new Oauth2ClientClientIdServiceServiceIdGetHandler())
        
            .add(Methods.GET, "/server/info", new ServerInfoGetHandler())
        
        ;
    }
}
