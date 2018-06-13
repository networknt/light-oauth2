package com.networknt.oauth.service.handler;

import com.hazelcast.core.IMap;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Service;
import com.networknt.oauth.cache.model.ServiceEndpoint;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delete a service by a serviceId, if there are endpoints associated with the service
 * delete endpoints before deleting the service.
 *
 * @author Steve Hu
 */
public class Oauth2ServiceServiceIdDeleteHandler extends ServiceAuditHandler implements LightHttpHandler {
    static final String SERVICE_NOT_FOUND = "ERR12015";
    static Logger logger = LoggerFactory.getLogger(Oauth2ServiceServiceIdGetHandler.class);
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String serviceId = exchange.getQueryParameters().get("serviceId").getFirst();
        IMap<String, ServiceEndpoint> serviceEndpoints = CacheStartupHookProvider.hz.getMap("serviceEndpoints");
        IMap<String, Service> services = CacheStartupHookProvider.hz.getMap("services");
        if(services.get(serviceId) == null) {
            setExchangeStatus(exchange, SERVICE_NOT_FOUND, serviceId);
        } else {
            serviceEndpoints.delete(serviceId);
            services.delete(serviceId);
        }
        processAudit(exchange);
    }
}
