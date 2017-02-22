package com.networknt.oauth.client.handler;

import com.hazelcast.core.IMap;
import com.networknt.config.Config;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Client;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Oauth2ClientClientIdGetHandler implements HttpHandler {
    static final String CLIENT_NOT_FOUND = "ERR12014";

    static Logger logger = LoggerFactory.getLogger(Oauth2ClientClientIdGetHandler.class);
    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String clientId = exchange.getQueryParameters().get("clientId").getFirst();

        IMap<String, Client> clients = CacheStartupHookProvider.hz.getMap("clients");
        Client client = clients.get(clientId);

        if(client == null) {
            Status status = new Status(CLIENT_NOT_FOUND, clientId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        }
        Client c = Client.copyClient(client);
        c.setClientSecret(null);
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(c));
    }
}
