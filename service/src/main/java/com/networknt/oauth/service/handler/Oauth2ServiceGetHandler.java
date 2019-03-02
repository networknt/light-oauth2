package com.networknt.oauth.service.handler;

import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.impl.predicates.LikePredicate;
import com.networknt.config.Config;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Service;
import com.networknt.oauth.cache.model.ServiceComparator;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Deque;

public class Oauth2ServiceGetHandler extends ServiceAuditHandler implements LightHttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2ServiceGetHandler.class);
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        IMap<String, Service> services = CacheStartupHookProvider.hz.getMap("services");

        Deque<String> serviceIdDeque = exchange.getQueryParameters().get("serviceId");
        String serviceId = serviceIdDeque == null? "%" : serviceIdDeque.getFirst() + "%";
        int page = Integer.valueOf(exchange.getQueryParameters().get("page").getFirst()) - 1;
        Deque<String> pageSizeDeque = exchange.getQueryParameters().get("pageSize");
        int pageSize = pageSizeDeque == null? 10 : Integer.valueOf(pageSizeDeque.getFirst());

        LikePredicate likePredicate = new LikePredicate("serviceId", serviceId);

        PagingPredicate pagingPredicate = new PagingPredicate(likePredicate, new ServiceComparator(), pageSize);
        pagingPredicate.setPage(page);
        Collection<Service> values = services.values(pagingPredicate);

        exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(values));
        processAudit(exchange);
    }
}
