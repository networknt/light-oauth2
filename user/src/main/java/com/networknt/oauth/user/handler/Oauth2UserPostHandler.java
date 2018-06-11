package com.networknt.oauth.user.handler;

import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.SqlPredicate;
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.User;
import com.networknt.status.Status;
import com.networknt.utility.HashUtil;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.util.Map;
import java.util.Set;

public class Oauth2UserPostHandler extends UserAuditHandler implements HttpHandler {
    private static final String PASSWORD_OR_PASSWORDCONFIRM_EMPTY = "ERR12011";
    private static final String PASSWORD_PASSWORDCONFIRM_NOT_MATCH = "ERR12012";
    private static final String USER_ID_EXISTS = "ERR12020";
    private static final String EMAIL_EXISTS = "ERR12021";

    static Logger logger = LoggerFactory.getLogger(Oauth2UserPostHandler.class);
    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> body = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        User user = Config.getInstance().getMapper().convertValue(body, User.class);

        String email = user.getEmail();
        IMap<String, User> users = CacheStartupHookProvider.hz.getMap("users");
        // make sure that email is not duplicated in users.
        Predicate predicate = new SqlPredicate(String.format("email = %s", email));
        Set<User> set = (Set<User>) users.values(predicate);
        if(set != null && set.size() > 0) {
            Status status = new Status(EMAIL_EXISTS, email);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            processAudit(exchange);
            return;
        }

        String password = user.getPassword();
        String passwordConfirm = user.getPasswordConfirm();
        if(password != null && password.length() > 0 && passwordConfirm != null && passwordConfirm.length() > 0) {
            // check if there are the same
            if(password.equals(passwordConfirm)) {
                // hash the password with salt.
                String hashedPass = HashUtil.generateStorngPasswordHash(password);
                user.setPassword(hashedPass);
                user.setPasswordConfirm(null);
                String userId = user.getUserId();
                if(users.get(userId) == null) {
                    users.set(userId, user);
                } else {
                    Status status = new Status(USER_ID_EXISTS, userId);
                    exchange.setStatusCode(status.getStatusCode());
                    exchange.getResponseSender().send(status.toString());
                }
            } else {
                // password and passwordConfirm not match.
                Status status = new Status(PASSWORD_PASSWORDCONFIRM_NOT_MATCH, password, passwordConfirm);
                exchange.setStatusCode(status.getStatusCode());
                exchange.getResponseSender().send(status.toString());
            }
        } else {
            // error password or passwordConform is empty
            Status status = new Status(PASSWORD_OR_PASSWORDCONFIRM_EMPTY, password, passwordConfirm);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
        }
        processAudit(exchange);
    }
}
