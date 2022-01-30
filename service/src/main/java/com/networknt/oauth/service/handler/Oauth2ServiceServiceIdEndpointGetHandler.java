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

import com.hazelcast.map.IMap;
import com.networknt.config.Config;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.ServiceEndpoint;
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
public class Oauth2ServiceServiceIdEndpointGetHandler extends ServiceAuditHandler implements LightHttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2ServiceServiceIdEndpointGetHandler.class);
    static final String SERVICE_ENDPOINT_NOT_FOUND = "ERR12042";

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        IMap<String, List<ServiceEndpoint>> serviceEndpoints = CacheStartupHookProvider.hz.getMap("serviceEndpoints");

        String serviceId = exchange.getQueryParameters().get("serviceId").getFirst();
        List<ServiceEndpoint> values = serviceEndpoints.get(serviceId);

        if(values == null || values.size() == 0) {
            setExchangeStatus(exchange, SERVICE_ENDPOINT_NOT_FOUND, serviceId);
            processAudit(exchange);
            return;
        }
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(values));
        processAudit(exchange);
    }
}
