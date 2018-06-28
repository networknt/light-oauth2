package com.networknt.oauth.provider.handler;

import com.hazelcast.core.IMap;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Client;
import com.networknt.oauth.cache.model.Provider;
import com.networknt.oauth.provider.ProviderAuditHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Oauth2ProviderProviderIdDeleteHandler extends ProviderAuditHandler implements LightHttpHandler {
    static final String CLIENT_NOT_FOUND = "ERR12014";

    static Logger logger = LoggerFactory.getLogger(Oauth2ProviderProviderIdDeleteHandler.class);


    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String providerId = exchange.getQueryParameters().get("providerId").getFirst();

        IMap<String, Provider> providers = CacheStartupHookProvider.hz.getMap("providers");
        if(providers.get(providerId) == null) {
            setExchangeStatus(exchange, CLIENT_NOT_FOUND, providerId);
        } else {
            providers.delete(providerId);
        }
        processAudit(exchange);
    }

}
