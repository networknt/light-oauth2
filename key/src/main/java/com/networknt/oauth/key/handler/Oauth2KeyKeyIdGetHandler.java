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

package com.networknt.oauth.key.handler;

import com.hazelcast.core.IMap;
import com.networknt.body.BodyHandler;

import com.networknt.client.oauth.KeyRequest;
import com.networknt.client.oauth.OauthHelper;
import com.networknt.config.Config;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.AuditInfoHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.*;
import com.networknt.status.Status;
import com.networknt.exception.ApiException;
import com.networknt.exception.ClientException;
import com.networknt.utility.HashUtil;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Locale;
import java.util.Map;

import static io.undertow.util.Headers.AUTHORIZATION;
import static io.undertow.util.Headers.BASIC;

/**
 * Get certificate that is used to verify the particular kid passed as a parameter.
 *
 * @author Steve Hu
 */
public class Oauth2KeyKeyIdGetHandler  extends AuditInfoHandler implements LightHttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2KeyKeyIdGetHandler.class);

    static final String CONFIG_SECURITY = "security";
    static final String CONFIG_JWT = "jwt";
    static final String CONFIG_CERTIFICATE = "certificate";

    static final String KEY_NOT_FOUND = "ERR12017";
    static final String MISSING_AUTHORIZATION_HEADER = "ERR12002";
    static final String CLIENT_NOT_FOUND = "ERR12014";
    static final String RUNTIME_EXCEPTION = "ERR10010";
    static final String UNAUTHORIZED_CLIENT = "ERR12007";
    static final String INVALID_KEY_ID = "ERR12030";

    private static final String PROVIDER_ID = "providerId";
    private static final String BASIC_PREFIX = BASIC + " ";
    private static final String LOWERCASE_BASIC_PREFIX = BASIC_PREFIX.toLowerCase(Locale.ENGLISH);
    private static final int PREFIX_LENGTH = BASIC_PREFIX.length();
    private static final String COLON = ":";
    private final static String CONFIG = "oauth_key";
    private final static OauthKeyConfig oauth_config = (OauthKeyConfig) Config.getInstance().getJsonObjectConfig(CONFIG, OauthKeyConfig.class);

    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

        String keyId = exchange.getQueryParameters().get("keyId").getFirst();
        if(logger.isDebugEnabled()) logger.debug("keyId = " + keyId);
        // find the location of the certificate
        Map<String, Object> config = Config.getInstance().getJsonMapConfig(CONFIG_SECURITY);
        Map<String, Object> jwtConfig = (Map<String, Object>)config.get(CONFIG_JWT);
        Map<String, Object> certificateConfig = (Map<String, Object>)jwtConfig.get(CONFIG_CERTIFICATE);
        String provider_id = config.get(PROVIDER_ID)==null ? "": config.get(PROVIDER_ID).toString();

        if (getProviderId(keyId)==null || "00".equals(getProviderId(keyId)) || provider_id.equals(getProviderId(keyId))) {
            if (getProviderId(keyId)==null  || provider_id.equals(getProviderId(keyId))) {
                // Standalone light-oauth server
                // check if client_id and client_secret in header are valid pair.
                HeaderValues values = exchange.getRequestHeaders().get(AUTHORIZATION);
                String authHeader;
                if(values != null) {
                    authHeader = values.getFirst();
                } else {
                    setExchangeStatus(exchange, MISSING_AUTHORIZATION_HEADER);
                    processAudit(exchange);
                    return;
                }
                if(authHeader == null) {
                    setExchangeStatus(exchange, MISSING_AUTHORIZATION_HEADER);
                    processAudit(exchange);
                    return;
                }
                if (authenticate(authHeader) == null) {
                    setExchangeStatus(exchange, UNAUTHORIZED_CLIENT);
                    processAudit(exchange);
                    return;
                }
            }
            String filename = (String)certificateConfig.get(getKeyId(keyId));
            if(filename != null) {
                String content = Config.getInstance().getStringFromFile(filename);
                if(logger.isDebugEnabled()) logger.debug("certificate = " + content);
                if(content != null) {
                    exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "application/text");
                    exchange.getResponseSender().send(content);
                } else {
                    setExchangeStatus(exchange, KEY_NOT_FOUND, keyId);
                }
            } else {
                setExchangeStatus(exchange, INVALID_KEY_ID, keyId);
            }
        } else {
            //Federation light-oauth server
            String content = getCertificateFromProvider(getProviderId(keyId), getKeyId(keyId));
            if(logger.isDebugEnabled()) logger.debug("certificate from provider = " + content);
            if(content != null) {
                exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "application/text");
                exchange.getResponseSender().send(content);
            } else {
                setExchangeStatus(exchange, KEY_NOT_FOUND, keyId);
            }
        }

        processAudit(exchange);
    }

    @SuppressWarnings("unchecked")
    private String authenticate(String authHeader) throws ApiException {
        String result = null;
        if (authHeader.toLowerCase(Locale.ENGLISH).startsWith(LOWERCASE_BASIC_PREFIX)) {
            String base64Challenge = authHeader.substring(PREFIX_LENGTH);
            String plainChallenge;
            try {
                ByteBuffer decode = FlexBase64.decode(base64Challenge);
                // assume charset is UTF_8
                Charset charset = StandardCharsets.UTF_8;
                plainChallenge = new String(decode.array(), decode.arrayOffset(), decode.limit(), charset);
                logger.debug("Found basic auth header %s (decoded using charset %s) in %s", plainChallenge, charset, authHeader);
                int colonPos;
                if ((colonPos = plainChallenge.indexOf(COLON)) > -1) {
                    String clientId = plainChallenge.substring(0, colonPos);
                    String clientSecret = plainChallenge.substring(colonPos + 1);
                    // match with db/cached user credentials.
                    IMap<String, Client> clients = CacheStartupHookProvider.hz.getMap("clients");
                    Client client = clients.get(clientId);
                    if(client == null) {
                        throw new ApiException(new Status(CLIENT_NOT_FOUND, clientId));
                    }
                    if(!HashUtil.validatePassword(clientSecret.toCharArray(), client.getClientSecret())) {
                        throw new ApiException(new Status(UNAUTHORIZED_CLIENT));
                    }
                    result = clientId;
                }
            } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                logger.error("Exception:", e);
                throw new ApiException(new Status(RUNTIME_EXCEPTION));
            }
        }
        return result;
    }


    private String getCertificateFromProvider(String providerId, String keyId) throws ClientException {
        IMap<String, Provider> providers = CacheStartupHookProvider.hz.getMap("providers");
        Provider provider = providers.get(providerId);
        String key = null;

        if(provider != null) {
            KeyRequest keyRequest = new KeyRequest(keyId);
            keyRequest.setServerUrl(provider.getServerUrl());
            keyRequest.setUri(provider.getUri() + "/00" + keyId);
            keyRequest.setEnableHttp2(true);
            key = OauthHelper.getKey(keyRequest);
        }
        return key;
    }

    private String getProviderId(String keyId)  {
        if ( keyId.length()< 4) {
            return null;
        } else {
            return keyId.substring(0,2);
        }
    }

    private String getKeyId(String keyId)  {
        if (keyId.length()< 4) {
            return keyId;
        } else {
            return keyId.substring(2);
        }
    }



    private void processAudit(HttpServerExchange exchange) throws Exception {
        if (oauth_config.isEnableAudit() ) {
            AuditInfo auditInfo = new AuditInfo();
            auditInfo.setServiceId(Oauth2Service.KEY);
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

