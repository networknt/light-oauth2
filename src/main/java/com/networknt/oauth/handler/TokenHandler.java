package com.networknt.oauth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.security.JwtHelper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.jose4j.jwt.JwtClaims;

import java.nio.ByteBuffer;
import java.util.*;

public class TokenHandler implements HttpHandler {
    private final ObjectMapper objectMapper;

    public TokenHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(
                Headers.CONTENT_TYPE, "application/json");

        Map<String, Object> resMap = new HashMap<String, Object>();
        resMap.put("access_token", JwtHelper.getJwt(mockClaims()));
        resMap.put("token_type", "bearer");
        resMap.put("expires_in", 600);
        exchange.getResponseSender().send(ByteBuffer.wrap(
                objectMapper.writeValueAsBytes(
                        resMap)));
    }

    public JwtClaims mockClaims() {
        JwtClaims claims = JwtHelper.getDefaultJwtClaims();
        claims.setClaim("user_id", "steve");
        claims.setClaim("user_type", "EMPLOYEE");
        claims.setClaim("client_id", "aaaaaaaa-1234-1234-1234-bbbbbbbb");
        List<String> scope = Arrays.asList("api.r", "api.w");
        claims.setStringListClaim("scope", scope); // multi-valued claims work too and will end up as a JSON array
        return claims;
    }
}
