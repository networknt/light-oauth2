
package com.networknt.oauth.provider;

import com.networknt.health.HealthGetHandler;
import com.networknt.info.ServerInfoGetHandler;
import com.networknt.oauth.provider.handler.Oauth2ProviderGetHandler;
import com.networknt.oauth.provider.handler.Oauth2ProviderPostHandler;
import com.networknt.oauth.provider.handler.Oauth2ProviderProviderIdDeleteHandler;
import com.networknt.oauth.provider.handler.Oauth2ProviderPutHandler;
import com.networknt.handler.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.util.Methods;

public class PathHandlerProvider implements HandlerProvider {
    @Override
    public HttpHandler getHandler() {
        return Handlers.routing()


            .add(Methods.POST, "/oauth2/provider", new Oauth2ProviderPostHandler())
            .add(Methods.GET, "/oauth2/provider", new Oauth2ProviderGetHandler())
           .add(Methods.DELETE, "/oauth2/provider/{providerId}", new Oauth2ProviderProviderIdDeleteHandler())
                .add(Methods.GET, "/health", new HealthGetHandler())
            .add(Methods.GET, "/server/info", new ServerInfoGetHandler())
                .add(Methods.PUT, "/oauth2/provider", new Oauth2ProviderPutHandler())

                ;
    }
}
