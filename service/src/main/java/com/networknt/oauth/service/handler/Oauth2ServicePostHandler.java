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
import com.networknt.config.JsonMapper;
import com.networknt.handler.LightHttpHandler;
import com.networknt.httpstring.AttachmentConstants;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Service;
import com.networknt.oauth.cache.model.User;
import com.networknt.security.JwtVerifier;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.util.Map;

public class Oauth2ServicePostHandler extends ServiceAuditHandler implements LightHttpHandler {
    static Logger logger = LoggerFactory.getLogger(Oauth2ServicePostHandler.class);
    static final String SERVICE_ID_EXISTS = "ERR12018";
    private static final String INCORRECT_TOKEN_TYPE = "ERR11601";

    private static final String OPENAPI_SECURITY_CONFIG = "openapi-security";
    private static final String ENABLE_VERIFY_JWT = "enableVerifyJwt";
    private static boolean enableSecurity = false;
    static {
        Map<String, Object> config = Config.getInstance().getJsonMapConfig(OPENAPI_SECURITY_CONFIG);
        // fallback to generic security.yml
        if(config == null) config = Config.getInstance().getJsonMapConfig(JwtVerifier.SECURITY_CONFIG);
        Object object = config.get(ENABLE_VERIFY_JWT);
        enableSecurity = object != null && Boolean.valueOf(object.toString());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> body = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        Service service = Config.getInstance().getMapper().convertValue(body, Service.class);

        if(enableSecurity) {
            // get userId from JWT token.
            Map<String, Object> auditInfo = exchange.getAttachment(AttachmentConstants.AUDIT_INFO);
            // the auditInfo won't be null as it passes the Jwt verification
            String userId = (String)auditInfo.get("user_id");
            if(userId == null) {
                // wrong token. client credentials token won't work here. Must be authorization code token.
                setExchangeStatus(exchange, INCORRECT_TOKEN_TYPE, "Authorization Code Token");
                return;
            }
            service.setOwnerId(userId);
        }
        String serviceId = service.getServiceId();
        IMap<String, Service> services = CacheStartupHookProvider.hz.getMap("services");
        if(services.get(serviceId) == null) {
            services.set(serviceId, service);
            exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(service));
        } else {
            setExchangeStatus(exchange, SERVICE_ID_EXISTS, serviceId);
        }
        processAudit(exchange);
    }
}
