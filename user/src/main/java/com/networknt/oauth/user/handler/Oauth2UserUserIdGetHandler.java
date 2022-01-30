package com.networknt.oauth.user.handler;

import com.hazelcast.map.IMap;
import com.networknt.config.Config;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.User;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Oauth2UserUserIdGetHandler extends UserAuditHandler implements LightHttpHandler {
    static final String USER_NOT_FOUND = "ERR12013";
    static Logger logger = LoggerFactory.getLogger(Oauth2UserUserIdGetHandler.class);
    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String userId = exchange.getQueryParameters().get("userId").getFirst();

        IMap<String, User> users = CacheStartupHookProvider.hz.getMap("users");
        User user = users.get(userId);

        if(user == null) {
            setExchangeStatus(exchange, USER_NOT_FOUND, userId);
            processAudit(exchange);
            return;
        }
        // remove password here
        user.setPassword(null);
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(user));
        processAudit(exchange);
    }
}
