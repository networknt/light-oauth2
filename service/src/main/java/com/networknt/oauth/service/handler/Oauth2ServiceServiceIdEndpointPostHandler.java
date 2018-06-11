
package com.networknt.oauth.service.handler;

import com.hazelcast.core.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Service;
import com.networknt.oauth.cache.model.ServiceEndpoint;
import com.networknt.oauth.cache.model.User;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * create or update a list of endpoints for the serviceId
 *
 * @author Steve Hu
 */
public class Oauth2ServiceServiceIdEndpointPostHandler extends ServiceAuditHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(Oauth2ServiceServiceIdEndpointPostHandler.class);
    static final String SERVICE_NOT_FOUND = "ERR12015";

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        List<Map<String, Object>> body = (List)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        String serviceId = exchange.getQueryParameters().get("serviceId").getFirst();
        if(logger.isDebugEnabled()) logger.debug("post serviceEndpoints for serviceId " + serviceId);

        // ensure that the serviceId exists
        IMap<String, Service> services = CacheStartupHookProvider.hz.getMap("services");
        if(services.get(serviceId) == null) {
            Status status = new Status(SERVICE_NOT_FOUND, serviceId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            processAudit(exchange);
            return;
        }

        IMap<String, List<ServiceEndpoint>> serviceEndpoints = CacheStartupHookProvider.hz.getMap("serviceEndpoints");
        List<ServiceEndpoint> list = new ArrayList<>();
        for(Map<String, Object> m: body) {
            list.add(Config.getInstance().getMapper().convertValue(m, ServiceEndpoint.class));
        }
        serviceEndpoints.set(serviceId, list);
        processAudit(exchange);
    }
}
