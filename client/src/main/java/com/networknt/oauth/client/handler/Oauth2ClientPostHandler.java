package com.networknt.oauth.client.handler;

import com.hazelcast.core.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Client;
import com.networknt.oauth.cache.model.User;
import com.networknt.status.Status;
import com.networknt.utility.HashUtil;
import com.networknt.utility.Util;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.util.Map;
import java.util.UUID;

public class Oauth2ClientPostHandler implements HttpHandler {

    static Logger logger = LoggerFactory.getLogger(Oauth2ClientPostHandler.class);
    static final String CLIENT_ID_EXISTS = "ERR12019";
    static final String USER_NOT_FOUND = "ERR12013";

    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> body = (Map<String, Object>)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        Client client = Config.getInstance().getMapper().convertValue(body, Client.class);

        // generate client_id and client_secret here.
        String clientId = UUID.randomUUID().toString();
        client.setClientId(clientId);
        String clientSecret = Util.getUUID();
        client.setClientSecret(HashUtil.generateStorngPasswordHash(clientSecret));

        IMap<String, Client> clients = CacheStartupHookProvider.hz.getMap("clients");
        if(clients.get(clientId) == null) {
            // make sure the owner_id exists in users map.
            String ownerId = client.getOwnerId();
            if(ownerId != null) {
                IMap<String, User> users = CacheStartupHookProvider.hz.getMap("users");
                if(!users.containsKey(ownerId)) {
                    Status status = new Status(USER_NOT_FOUND, ownerId);
                    exchange.setStatusCode(status.getStatusCode());
                    exchange.getResponseSender().send(status.toString());
                    return;
                }
            }
            clients.set(clientId, client);
            // send the client back with client_id and client_secret
            Client c = Client.copyClient(client);
            c.setClientSecret(clientSecret);
            exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(c));
        } else {
            Status status = new Status(CLIENT_ID_EXISTS, clientId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
        }
    }
}
