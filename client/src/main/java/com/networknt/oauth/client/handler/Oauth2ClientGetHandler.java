package com.networknt.oauth.client.handler;

import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.impl.predicates.LikePredicate;
import com.networknt.config.Config;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Client;
import com.networknt.oauth.cache.model.ClientComparator;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

public class Oauth2ClientGetHandler implements HttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2ClientGetHandler.class);

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        IMap<String, Client> clients = CacheStartupHookProvider.hz.getMap("clients");
        Deque<String> clientNameDeque = exchange.getQueryParameters().get("clientName");
        String clientName = clientNameDeque == null? "%" : clientNameDeque.getFirst() + "%";
        int page = Integer.valueOf(exchange.getQueryParameters().get("page").getFirst()) - 1;
        Deque<String> pageSizeDeque = exchange.getQueryParameters().get("pageSize");
        int pageSize = pageSizeDeque == null? 10 : Integer.valueOf(pageSizeDeque.getFirst());

        LikePredicate likePredicate = new LikePredicate("clientName", clientName);

        PagingPredicate pagingPredicate = new PagingPredicate(likePredicate, new ClientComparator(), pageSize);
        pagingPredicate.setPage(page);
        Collection<Client> values = clients.values(pagingPredicate);

        List results = new ArrayList();
        for (Client value : values) {
            Client c = Client.copyClient(value);
            c.setClientSecret(null);
            results.add(c);
        }
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(results));
    }
}
