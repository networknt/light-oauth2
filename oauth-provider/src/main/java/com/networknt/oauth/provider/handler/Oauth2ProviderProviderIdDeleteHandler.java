package com.networknt.oauth.provider.handler;

import com.hazelcast.core.IMap;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Client;
import com.networknt.oauth.provider.ProviderAuditHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Oauth2ProviderProviderIdDeleteHandler extends ProviderAuditHandler implements LightHttpHandler {
    static final String CLIENT_NOT_FOUND = "ERR12014";

    static Logger logger = LoggerFactory.getLogger(Oauth2ProviderProviderIdDeleteHandler.class);


    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String clientId = exchange.getQueryParameters().get("clientId").getFirst();

        IMap<String, Client> clients = CacheStartupHookProvider.hz.getMap("clients");
        if(clients.get(clientId) == null) {
            setExchangeStatus(exchange, CLIENT_NOT_FOUND, clientId);
        } else {
            clients.delete(clientId);
        }
        processAudit(exchange);
    }

}
