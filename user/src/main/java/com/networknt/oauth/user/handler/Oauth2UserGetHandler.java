package com.networknt.oauth.user.handler;

import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.impl.predicates.LikePredicate;
import com.networknt.config.Config;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.User;
import com.networknt.oauth.cache.model.UserComparator;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Deque;

/**
 * Created by stevehu on 2017-01-03.
 * The current implementation creates a new PagingPredicate for each request which is not very
 * efficient. We should create a map of PagingPredicate object with user_id as key from JWT. In
 * that case, the same user navigate from page to page will use the same object without creating
 * a new one each request.
 * TODO implement above once people report performance issue.
 */
public class Oauth2UserGetHandler implements HttpHandler {
    static Logger logger = LoggerFactory.getLogger(Oauth2UserGetHandler.class);

    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        IMap<String, User> users = CacheStartupHookProvider.hz.getMap("users");
        Deque<String> userIdDeque = exchange.getQueryParameters().get("userId");
        String userId = userIdDeque == null? "%" : userIdDeque.getFirst() + "%";
        int page = Integer.valueOf(exchange.getQueryParameters().get("page").getFirst()) - 1;
        Deque<String> pageSizeDeque = exchange.getQueryParameters().get("pageSize");
        int pageSize = pageSizeDeque == null? 10 : Integer.valueOf(pageSizeDeque.getFirst());

        LikePredicate likePredicate = new LikePredicate("userId", userId);

        PagingPredicate pagingPredicate = new PagingPredicate(likePredicate, new UserComparator(), pageSize);
        pagingPredicate.setPage(page);
        Collection<User> values = users.values(pagingPredicate);

        for (User value : values) {
            value.setPassword(null);
        }
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(values));
    }
}
