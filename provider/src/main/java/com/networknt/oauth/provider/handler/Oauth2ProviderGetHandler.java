package com.networknt.oauth.provider.handler;

import com.hazelcast.core.IMap;

import com.networknt.config.Config;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;

import com.networknt.oauth.cache.model.Provider;
import com.networknt.oauth.provider.ProviderAuditHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Oauth2ProviderGetHandler extends ProviderAuditHandler implements LightHttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2ProviderGetHandler.class);

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        IMap<String, Provider> providers = CacheStartupHookProvider.hz.getMap("providers");


        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(providers));
        processAudit(exchange);
    }
}
