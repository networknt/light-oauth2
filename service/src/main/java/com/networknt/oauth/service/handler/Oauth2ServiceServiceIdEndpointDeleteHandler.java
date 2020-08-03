/*
 * Copyright (c) 2016 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.networknt.oauth.service.handler;

import com.hazelcast.core.IMap;
import com.networknt.handler.LightHttpHandler;
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
public class Oauth2ServiceServiceIdEndpointDeleteHandler extends ServiceAuditHandler implements LightHttpHandler {
    private static Logger logger = LoggerFactory.getLogger(Oauth2ServiceServiceIdEndpointDeleteHandler.class);
    private static final String SERVICE_ENDPOINT_NOT_FOUND = "ERR12042";

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String serviceId = exchange.getQueryParameters().get("serviceId").getFirst();
        if(logger.isDebugEnabled()) logger.debug("Delete service endpoint for serviceId " + serviceId);
        IMap<String, List<ServiceEndpoint>> serviceEndpoints = CacheStartupHookProvider.hz.getMap("serviceEndpoints");
        if(serviceEndpoints.get(serviceId) == null || serviceEndpoints.get(serviceId).size() == 0) {
            setExchangeStatus(exchange, SERVICE_ENDPOINT_NOT_FOUND, serviceId);
        } else {
            serviceEndpoints.delete(serviceId);
        }
        processAudit(exchange);
    }
}
