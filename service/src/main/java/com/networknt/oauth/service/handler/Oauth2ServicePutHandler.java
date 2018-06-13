package com.networknt.oauth.service.handler;

import com.hazelcast.core.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Service;
import com.networknt.oauth.cache.model.User;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.util.Map;

public class Oauth2ServicePutHandler extends ServiceAuditHandler implements LightHttpHandler {
    static Logger logger = LoggerFactory.getLogger(Oauth2ServicePostHandler.class);
    static final String SERVICE_NOT_FOUND = "ERR12015";
    static final String USER_NOT_FOUND = "ERR12013";

    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> body = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        Service service = Config.getInstance().getMapper().convertValue(body, Service.class);

        String serviceId = service.getServiceId();

        IMap<String, Service> services = CacheStartupHookProvider.hz.getMap("services");
        if(services.get(serviceId) == null) {
            setExchangeStatus(exchange, SERVICE_NOT_FOUND, serviceId);
        } else {
            // make sure the owner_id exists in users map.
            String ownerId = service.getOwnerId();
            if(ownerId != null) {
                IMap<String, User> users = CacheStartupHookProvider.hz.getMap("users");
                if(!users.containsKey(ownerId)) {
                    setExchangeStatus(exchange, USER_NOT_FOUND, ownerId);
                    processAudit(exchange);
                    return;
                }
            }
            services.set(serviceId, service);
        }
        processAudit(exchange);
    }
}
