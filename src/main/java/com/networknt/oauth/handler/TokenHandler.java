package com.networknt.oauth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.security.JwtHelper;
import com.networknt.config.Config;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.jose4j.jwt.JwtClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;

public class TokenHandler implements HttpHandler {
    static final Logger logger = LoggerFactory.getLogger(TokenHandler.class);
    static final String CLIENTS = "clients";
    private final ObjectMapper objectMapper;
    private final Map<String, Object> data = Config.getInstance().getJsonMapConfig(CLIENTS);

    public TokenHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(
                Headers.CONTENT_TYPE, "application/json");
        // get Authorization header.
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if(auth == null || auth.trim().length() == 0) {
            exchange.getResponseSender().send(ByteBuffer.wrap(
                    objectMapper.writeValueAsBytes(
                            Collections.singletonMap("error", "Missing authorization header"))));
        } else {
            logger.debug("Authorization header = " + auth);
            String basic = auth.substring(0, 5);
            if("BASIC".equalsIgnoreCase(basic)) {
                String credentials = auth.substring(6);
                logger.debug("credentials = " + credentials);
                String clientId;
                String clientSecret;
                int pos = credentials.indexOf(':');
                if(pos == -1) {
                    credentials = decodeCredentials(credentials);
                }
                pos = credentials.indexOf(':');
                if(pos != -1) {
                    clientId = credentials.substring(0, pos);
                    clientSecret = credentials.substring(pos + 1);
                    logger.debug("clientId = " + clientId + " clientSecret = " + clientSecret);
                    Map<String, Object> cs = (Map<String, Object>)data.get(clientId);
                    if(cs == null) {
                        exchange.getResponseSender().send(ByteBuffer.wrap(
                                objectMapper.writeValueAsBytes(
                                        Collections.singletonMap("error", "Invalid client-id"))));
                    } else {
                        String secret = (String)cs.get("client_secret");
                        if(clientSecret.equals(secret)) {

                            String scope = (String)cs.get("scope");
                            Map<String, Object> resMap = new HashMap<String, Object>();
                            resMap.put("access_token", JwtHelper.getJwt(mockClaims(clientId, scope)));
                            resMap.put("token_type", "bearer");
                            resMap.put("expires_in", 600);
                            exchange.getResponseSender().send(ByteBuffer.wrap(
                                    objectMapper.writeValueAsBytes(
                                            resMap)));
                        } else {
                            exchange.getResponseSender().send(ByteBuffer.wrap(
                                    objectMapper.writeValueAsBytes(
                                            Collections.singletonMap("error", "unauthorized_client"))));
                        }

                    }
                } else {
                    exchange.getResponseSender().send(ByteBuffer.wrap(
                            objectMapper.writeValueAsBytes(
                                    Collections.singletonMap("error", "Invalid basic credentials"))));
                }
            } else {
                exchange.getResponseSender().send(ByteBuffer.wrap(
                        objectMapper.writeValueAsBytes(
                                Collections.singletonMap("error", "Invalid authorization header"))));
            }

        }
    }

    public JwtClaims mockClaims(String clientId, String scopeString) {
        JwtClaims claims = JwtHelper.getDefaultJwtClaims();
        claims.setClaim("client_id", clientId);
        List<String> scope = Arrays.asList(scopeString.split("\\s+"));
        claims.setStringListClaim("scope", scope); // multi-valued claims work too and will end up as a JSON array
        return claims;
    }

    public static String decodeCredentials(String cred) {
        return new String(org.apache.commons.codec.binary.Base64.decodeBase64(cred));
    }


}
