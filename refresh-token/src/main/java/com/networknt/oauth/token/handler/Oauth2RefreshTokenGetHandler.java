package com.networknt.oauth.token.handler;

import com.hazelcast.map.IMap;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.impl.predicates.LikePredicate;
import com.networknt.config.Config;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.RefreshToken;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Deque;

public class Oauth2RefreshTokenGetHandler extends RefreshTokenAuditHandler implements LightHttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2RefreshTokenGetHandler.class);

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        IMap<String, RefreshToken> tokens = CacheStartupHookProvider.hz.getMap("tokens");
        Deque<String> userIdDeque = exchange.getQueryParameters().get("userId");
        String userId = userIdDeque == null? "%" : userIdDeque.getFirst() + "%";
        int page = Integer.valueOf(exchange.getQueryParameters().get("page").getFirst()) - 1;
        Deque<String> pageSizeDeque = exchange.getQueryParameters().get("pageSize");
        int pageSize = pageSizeDeque == null? 10 : Integer.valueOf(pageSizeDeque.getFirst());
        if(logger.isDebugEnabled()) logger.debug("userId = " + userId + " page = " + page + " pageSize = " + pageSize);
        LikePredicate likePredicate = new LikePredicate("userId", userId);

        PagingPredicate pagingPredicate = Predicates.pagingPredicate(likePredicate, pageSize);
        pagingPredicate.setPage(page);
        Collection<RefreshToken> values = tokens.values(pagingPredicate);

        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(values));
        processAudit(exchange);
    }
}
