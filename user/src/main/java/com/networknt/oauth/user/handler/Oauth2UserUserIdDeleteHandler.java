package com.networknt.oauth.user.handler;

import com.hazelcast.core.IMap;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.User;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Oauth2UserUserIdDeleteHandler extends UserAuditHandler implements LightHttpHandler {
    static final String USER_NOT_FOUND = "ERR12013";
    static Logger logger = LoggerFactory.getLogger(Oauth2UserUserIdDeleteHandler.class);
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String userId = exchange.getQueryParameters().get("userId").getFirst();
        IMap<String, User> users = CacheStartupHookProvider.hz.getMap("users");
        if(users.get(userId) == null) {
            setExchangeStatus(exchange, USER_NOT_FOUND, userId);
        } else {
            users.delete(userId);
        }
        processAudit(exchange);
    }
}
