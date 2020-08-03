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
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.handler.LightHttpHandler;
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
public class Oauth2ServiceServiceIdEndpointPostHandler extends ServiceAuditHandler implements LightHttpHandler {
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
            setExchangeStatus(exchange, SERVICE_NOT_FOUND, serviceId);
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
