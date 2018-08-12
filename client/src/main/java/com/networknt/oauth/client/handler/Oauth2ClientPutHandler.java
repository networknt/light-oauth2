package com.networknt.oauth.client.handler;

import com.hazelcast.core.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Client;
import com.networknt.oauth.cache.model.User;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.util.Map;

public class Oauth2ClientPutHandler  extends ClientAuditHandler implements LightHttpHandler {
    static Logger logger = LoggerFactory.getLogger(Oauth2ClientPutHandler.class);
    static final String CLIENT_NOT_FOUND = "ERR12014";
    static final String USER_NOT_FOUND = "ERR12013";
    static final String DEREF_NOT_EXTERNAL = "ERR12043";

    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> body = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        Client client = Config.getInstance().getMapper().convertValue(body, Client.class);
        if(client.getDerefClientId() != null && Client.ClientTypeEnum.EXTERNAL != client.getClientType()) {
            // only external client may have deref client id
            setExchangeStatus(exchange, DEREF_NOT_EXTERNAL);
            return;
        }

        String clientId = client.getClientId();

        IMap<String, Client> clients = CacheStartupHookProvider.hz.getMap("clients");
        Client originalClient = clients.get(clientId);
        if(originalClient == null) {
            setExchangeStatus(exchange, CLIENT_NOT_FOUND, clientId);
        } else {
            // make sure the owner_id exists in users map.
            String ownerId = client.getOwnerId();
            if(ownerId != null) {
                IMap<String, User> users = CacheStartupHookProvider.hz.getMap("users");
                if(!users.containsKey(ownerId)) {
                    Status status = new Status(USER_NOT_FOUND, ownerId);
                    exchange.setStatusCode(status.getStatusCode());
                    exchange.getResponseSender().send(status.toString());
                    processAudit(exchange);
                    return;
                }
            }
            // set client secret as it is not returned by query.
            client.setClientSecret(originalClient.getClientSecret());
            clients.set(clientId, client);
        }
        processAudit(exchange);
    }
}
