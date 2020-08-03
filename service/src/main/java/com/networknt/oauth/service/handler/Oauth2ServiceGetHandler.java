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
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.impl.predicates.LikePredicate;
import com.networknt.config.Config;
import com.networknt.config.JsonMapper;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Service;
import com.networknt.oauth.cache.model.ServiceComparator;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Get all the services to display with pagination support. It returns a list of services and the
 * total count.
 *
 * @author Steve Hu
 */
public class Oauth2ServiceGetHandler extends ServiceAuditHandler implements LightHttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2ServiceGetHandler.class);
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        IMap<String, Service> services = CacheStartupHookProvider.hz.getMap("services");
        Deque<String> hostDeque = exchange.getQueryParameters().get("host");
        String host = hostDeque == null ? "%" : hostDeque.getFirst() + "%";

        int page = Integer.valueOf(exchange.getQueryParameters().get("page").getFirst());
        Deque<String> pageSizeDeque = exchange.getQueryParameters().get("pageSize");
        int pageSize = pageSizeDeque == null? 10 : Integer.valueOf(pageSizeDeque.getFirst());

        LikePredicate likePredicate = new LikePredicate("host", host);

        PagingPredicate pagingPredicate = new PagingPredicate(likePredicate, new ServiceComparator(), pageSize);
        pagingPredicate.setPage(page);
        Collection<Service> values = services.values(pagingPredicate);
        Map<String, Object> map = new HashMap<>();
        map.put("services", values);
        map.put("total", services.size());

        exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(JsonMapper.toJson(map));
        processAudit(exchange);
    }
}
