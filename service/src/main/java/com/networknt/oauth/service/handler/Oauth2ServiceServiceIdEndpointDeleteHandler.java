
package com.networknt.oauth.service.handler;

import com.hazelcast.core.IMap;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.ServiceEndpoint;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Delete all endpoints for a service by serviceId
 *
 * @author Steve Hu
 */
public class Oauth2ServiceServiceIdEndpointDeleteHandler implements HttpHandler {
    private static Logger logger = LoggerFactory.getLogger(Oauth2ServiceServiceIdEndpointDeleteHandler.class);
    private static final String SERVICE_ENDPOINT_NOT_FOUND = "ERR12042";

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String serviceId = exchange.getQueryParameters().get("serviceId").getFirst();
        if(logger.isDebugEnabled()) logger.debug("Delete service endpoint for serviceId " + serviceId);
        IMap<String, List<ServiceEndpoint>> serviceEndpoints = CacheStartupHookProvider.hz.getMap("serviceEndpoints");
        if(serviceEndpoints.get(serviceId) == null || serviceEndpoints.get(serviceId).size() == 0) {
            Status status = new Status(SERVICE_ENDPOINT_NOT_FOUND, serviceId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
        } else {
            serviceEndpoints.delete(serviceId);
        }

    }
}
