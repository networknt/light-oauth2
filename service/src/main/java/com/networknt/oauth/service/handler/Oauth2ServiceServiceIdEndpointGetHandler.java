
package com.networknt.oauth.service.handler;

import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.impl.predicates.LikePredicate;
import com.networknt.config.Config;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Service;
import com.networknt.oauth.cache.model.ServiceComparator;
import com.networknt.oauth.cache.model.ServiceEndpoint;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Retrieve all service endpoints for a particular serviceId
 *
 * @author Steve Hu
 */
public class Oauth2ServiceServiceIdEndpointGetHandler implements HttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2ServiceServiceIdEndpointGetHandler.class);
    static final String SERVICE_ENDPOINT_NOT_FOUND = "ERR12042";

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        IMap<String, List<ServiceEndpoint>> serviceEndpoints = CacheStartupHookProvider.hz.getMap("serviceEndpoints");

        String serviceId = exchange.getQueryParameters().get("serviceId").getFirst();
        List<ServiceEndpoint> values = serviceEndpoints.get(serviceId);

        if(values == null || values.size() == 0) {
            Status status = new Status(SERVICE_ENDPOINT_NOT_FOUND, serviceId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        }
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(values));
    }
}
