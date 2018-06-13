package com.networknt.oauth.service.handler;

import com.hazelcast.core.IMap;
import com.networknt.config.Config;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Service;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Oauth2ServiceServiceIdGetHandler extends ServiceAuditHandler implements LightHttpHandler {
    static Logger logger = LoggerFactory.getLogger(Oauth2ServiceServiceIdGetHandler.class);
    static final String SERVICE_NOT_FOUND = "ERR12015";
    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String serviceId = exchange.getQueryParameters().get("serviceId").getFirst();

        IMap<String, Service> services = CacheStartupHookProvider.hz.getMap("services");
        Service service = services.get(serviceId);

        if(service == null) {
            setExchangeStatus(exchange, SERVICE_NOT_FOUND, serviceId);
            processAudit(exchange);
            return;
        }
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(service));
        processAudit(exchange);
    }
}
