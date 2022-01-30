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
import com.networknt.oauth.cache.model.Service;
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
