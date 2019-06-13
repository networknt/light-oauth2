package com.networknt.oauth.token.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.client.oauth.SignRequest;
import com.networknt.config.Config;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Client;
import com.networknt.oauth.token.helper.HttpAuth;
import com.networknt.security.JwtIssuer;
import com.networknt.status.Status;
import com.networknt.exception.ApiException;
import com.networknt.utility.HashUtil;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.jose4j.jwt.JwtClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

public class Oauth2SigningPostHandler extends TokenAuditHandler implements LightHttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(Oauth2SigningPostHandler.class);
    private static final String MISSING_AUTHORIZATION_HEADER = "ERR12002";
    private static final String INVALID_AUTHORIZATION_HEADER = "ERR12003";
    private static final String INVALID_BASIC_CREDENTIALS = "ERR12004";
    private static final String CLIENT_NOT_FOUND = "ERR12014";
    private static final String UNAUTHORIZED_CLIENT = "ERR12007";
    private static final String RUNTIME_EXCEPTION = "ERR10010";
    private static final String GENERIC_EXCEPTION = "ERR10014";

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        ObjectMapper mapper = Config.getInstance().getMapper();
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        // check authorization header for basic authentication
        Client client = authenticateClient(exchange);
        if(client != null) {
            String jwt;
            Map<String, Object> body = (Map<String, Object>)exchange.getAttachment(BodyHandler.REQUEST_BODY);
            SignRequest sr = Config.getInstance().getMapper().convertValue(body, SignRequest.class);
            int expires = sr.getExpires();
            try {
                // assume that the custom_claim is in format of json map string.
                Map<String, Object>  customClaim = sr.getPayload();
                jwt = JwtIssuer.getJwt(mockCcClaims(client.getClientId(), expires, customClaim));
            } catch (Exception e) {
                logger.error("Exception:", e);
                throw new ApiException(new Status(GENERIC_EXCEPTION, e.getMessage()));
            }
            Map<String, Object> resMap = new HashMap<>();
            resMap.put("access_token", jwt);
            resMap.put("token_type", "bearer");
            resMap.put("expires_in", expires);
            exchange.getResponseSender().send(mapper.writeValueAsString(resMap));
        }
        processAudit(exchange);
    }

    private Client authenticateClient(HttpServerExchange exchange) throws ApiException {
        HttpAuth httpAuth = new HttpAuth(exchange);

        String clientId;
        String clientSecret;
        if(!httpAuth.isHeaderAvailable()) {
            throw new ApiException(new Status(MISSING_AUTHORIZATION_HEADER));
        } else {
            clientId = httpAuth.getClientId();
            clientSecret = httpAuth.getClientSecret();
        }

        if(clientId == null || clientId.trim().isEmpty() || clientSecret == null || clientSecret.trim().isEmpty()) {
            if(httpAuth.isInvalidCredentials()) {
                throw new ApiException(new Status(INVALID_BASIC_CREDENTIALS, httpAuth.getCredentials()));
            } else {
                throw new ApiException(new Status(INVALID_AUTHORIZATION_HEADER, httpAuth.getAuth()));
            }
        }

        return validateClientSecret(clientId, clientSecret);
    }

    private Client validateClientSecret(String clientId, String clientSecret) throws ApiException {
        IMap<String, Client> clients = CacheStartupHookProvider.hz.getMap("clients");
        Client client = clients.get(clientId);
        if(client == null) {
            throw new ApiException(new Status(CLIENT_NOT_FOUND, clientId));
        } else {
            try {
                if(HashUtil.validatePassword(clientSecret.toCharArray(), client.getClientSecret())) {
                    return client;
                } else {
                    throw new ApiException(new Status(UNAUTHORIZED_CLIENT));
                }
            } catch ( NoSuchAlgorithmException | InvalidKeySpecException e) {
                logger.error("Exception:", e);
                throw new ApiException(new Status(RUNTIME_EXCEPTION));
            }
        }
    }

    private JwtClaims mockCcClaims(String clientId, Integer expiresIn, Map<String, Object> formMap) {
        JwtClaims claims = JwtIssuer.getJwtClaimsWithExpiresIn(expiresIn);
        claims.setClaim("client_id", clientId);
        if(formMap != null) {
            for(Map.Entry<String, Object> entry : formMap.entrySet()) {
                claims.setClaim(entry.getKey(), entry.getValue());
            }
        }
        return claims;
    }

}
