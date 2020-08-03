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
package com.networknt.oauth.client.handler;

import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.oauth.cache.AuditInfoHandler;
import com.networknt.oauth.cache.model.AuditInfo;
import com.networknt.oauth.cache.model.Oauth2Service;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClientAuditHandler extends AuditInfoHandler {
    static final Logger logger = LoggerFactory.getLogger(ClientAuditHandler.class);
    private final static String CONFIG = "oauth_client";
    private final static OauthClientConfig oauth_config = (OauthClientConfig) Config.getInstance().getJsonObjectConfig(CONFIG, OauthClientConfig.class);


    protected void processAudit(HttpServerExchange exchange) throws Exception{
        if (oauth_config.isEnableAudit() ) {
            AuditInfo auditInfo = new AuditInfo();
            auditInfo.setServiceId(Oauth2Service.CLIENT);
            auditInfo.setEndpoint(exchange.getHostName() + exchange.getRelativePath());
            auditInfo.setRequestHeader(exchange.getRequestHeaders().toString());
            auditInfo.setRequestBody(Config.getInstance().getMapper().writeValueAsString(exchange.getAttachment(BodyHandler.REQUEST_BODY)));
            auditInfo.setResponseCode(exchange.getStatusCode());
            auditInfo.setResponseHeader(exchange.getResponseHeaders().toString());
            auditInfo.setResponseBody(Config.getInstance().getMapper().writeValueAsString(exchange.getResponseCookies()));
            saveAudit(auditInfo);
        }
    }


}
