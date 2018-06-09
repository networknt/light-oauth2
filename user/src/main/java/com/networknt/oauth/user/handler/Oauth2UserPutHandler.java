package com.networknt.oauth.user.handler;

import com.hazelcast.core.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.User;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.util.Map;

public class Oauth2UserPutHandler extends UserAuditHandler implements HttpHandler {
    static final String USER_NOT_FOUND = "ERR12013";
    static Logger logger = LoggerFactory.getLogger(Oauth2UserPostHandler.class);
    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> body = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        User user = Config.getInstance().getMapper().convertValue(body, User.class);
        String userId = user.getUserId();
        IMap<String, User> users = CacheStartupHookProvider.hz.getMap("users");
        User u = users.get(userId);
        if(u == null) {
            Status status = new Status(USER_NOT_FOUND, userId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
        } else {
            // as password is not in the return value, chances are password is not in the user object
            user.setPassword(u.getPassword());
            user.setUpdateDt(new Date(System.currentTimeMillis()));
            users.set(userId, user);
        }
        processAudit(exchange);
    }
}
