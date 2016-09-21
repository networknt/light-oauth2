package com.networknt.oauth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.security.JwtHelper;
import com.networknt.config.Config;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;
import org.jose4j.jwt.JwtClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;

public class TokenHandler implements HttpHandler {
    static final Logger logger = LoggerFactory.getLogger(TokenHandler.class);
    static final String CLIENTS = "clients";
    static final String USERS = "users";
    private final ObjectMapper objectMapper;
    private final Map<String, Object> clients = Config.getInstance().getJsonMapConfig(CLIENTS);
    private final Map<String, Object> users = Config.getInstance().getJsonMapConfig(USERS);

    public TokenHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        Map<String, Object> formMap = new HashMap<String, Object>();

        final FormParserFactory parserFactory = FormParserFactory.builder().build();
        final FormDataParser parser = parserFactory.createParser(exchange);
        try {
            FormData data = parser.parseBlocking();
            Iterator<String> it = data.iterator();
            while(it.hasNext()) {
                String fd = it.next();
                for (FormData.FormValue val : data.get(fd)) {
                    logger.debug("fd = " + fd + " value = " + val.getValue());
                    formMap.put(fd, val.getValue());
                }
            }
        } catch (Exception e) {
            exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            exchange.getResponseSender().send(ByteBuffer.wrap(
                    objectMapper.writeValueAsBytes(
                            Collections.singletonMap("error", "Unable to parse form data"))));
        }

        if("client_credentials".equals(formMap.get("grant_type"))) {
            exchange.getResponseSender().send(objectMapper.writeValueAsString(handleClientCredentials(exchange)));
        } else if("authorization_code".equals(formMap.get("grant_type"))) {
            exchange.getResponseSender().send(objectMapper.writeValueAsString(handleAuthorizationCode(exchange, (String)formMap.get("code"))));
        } else {
            exchange.setStatusCode(StatusCodes.BAD_REQUEST);
            exchange.getResponseSender().send(ByteBuffer.wrap(
                    objectMapper.writeValueAsBytes(
                            Collections.singletonMap("error", "Unsupported grant type"))));
        }
    }

    private Map<String, Object> handleClientCredentials(HttpServerExchange exchange) throws Exception {
        // get Authorization header.
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if(auth == null || auth.trim().length() == 0) {
            exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
            Map<String, Object> result = new HashMap<String, Object> ();
            result.put("error", "Missing authorization header");
            return result;
        } else {
            String basic = auth.substring(0, 5);
            if("BASIC".equalsIgnoreCase(basic)) {
                String credentials = auth.substring(6);
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
                    Map<String, Object> cs = (Map<String, Object>)clients.get(clientId);
                    if(cs == null) {
                        exchange.setStatusCode(StatusCodes.NOT_FOUND);
                        Map<String, Object> result = new HashMap<String, Object> ();
                        result.put("error", "Invalid client-id");
                        return result;
                    } else {
                        String secret = (String)cs.get("client_secret");
                        if(secret.equals(clientSecret)) {
                            String scope = (String)cs.get("scope");
                            Map<String, Object> resMap = new HashMap<String, Object>();
                            resMap.put("access_token", JwtHelper.getJwt(mockCcClaims(clientId, scope)));
                            resMap.put("token_type", "bearer");
                            resMap.put("expires_in", 600);
                            return resMap;
                        } else {
                            exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
                            Map<String, Object> result = new HashMap<String, Object> ();
                            result.put("error", "Unauthorized_client");
                            return result;
                        }

                    }
                } else {
                    exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
                    Map<String, Object> result = new HashMap<String, Object> ();
                    result.put("error", "Invalid basic credentials");
                    return result;
                }
            } else {
                exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
                Map<String, Object> result = new HashMap<String, Object> ();
                result.put("error", "Invalid authorization header");
                return result;
            }

        }
    }

    private Map<String, Object> handleAuthorizationCode(HttpServerExchange exchange, String code) throws Exception {
        // get Authorization header.
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if(auth == null || auth.trim().length() == 0) {
            exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
            Map<String, Object> result = new HashMap<String, Object> ();
            result.put("error", "Missing authorization header");
            return result;
        } else {
            String basic = auth.substring(0, 5);
            if("BASIC".equalsIgnoreCase(basic)) {
                String credentials = auth.substring(6);
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
                    Map<String, Object> cs = (Map<String, Object>)clients.get(clientId);
                    if(cs == null) {
                        exchange.setStatusCode(StatusCodes.NOT_FOUND);
                        Map<String, Object> result = new HashMap<String, Object> ();
                        result.put("error", "Invalid client-id");
                        return result;
                    } else {
                        String secret = (String)cs.get("client_secret");
                        if(secret.equals(clientSecret)) {
                            String userId = (String)CodeHandler.codes.remove(code);
                            if(userId != null) {
                                Map<String, Object> user = (Map<String, Object>)users.get(userId);
                                String scope = (String)cs.get("scope");
                                Map<String, Object> resMap = new HashMap<String, Object>();
                                resMap.put("access_token", JwtHelper.getJwt(mockAcClaims(clientId, scope, userId, (String)user.get("userType"))));
                                resMap.put("token_type", "bearer");
                                resMap.put("expires_in", 600);
                                return resMap;
                            } else {
                                exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
                                Map<String, Object> result = new HashMap<String, Object> ();
                                result.put("error", "Invalid authorization code");
                                return result;
                            }
                        } else {
                            exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
                            Map<String, Object> result = new HashMap<String, Object> ();
                            result.put("error", "Unauthorized_client");
                            return result;
                        }
                    }
                } else {
                    exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
                    Map<String, Object> result = new HashMap<String, Object> ();
                    result.put("error", "Invalid basic credentials");
                    return result;
                }
            } else {
                exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
                Map<String, Object> result = new HashMap<String, Object> ();
                result.put("error", "Invalid authorization header");
                return result;
            }
        }
    }


    public JwtClaims mockCcClaims(String clientId, String scopeString) {
        JwtClaims claims = JwtHelper.getDefaultJwtClaims();
        claims.setClaim("client_id", clientId);
        List<String> scope = Arrays.asList(scopeString.split("\\s+"));
        claims.setStringListClaim("scope", scope); // multi-valued claims work too and will end up as a JSON array
        return claims;
    }

    public JwtClaims mockAcClaims(String clientId, String scopeString, String userId, String userType) {
        JwtClaims claims = JwtHelper.getDefaultJwtClaims();
        claims.setClaim("user_id", userId);
        claims.setClaim("user_type", userType);
        claims.setClaim("client_id", clientId);
        List<String> scope = Arrays.asList(scopeString.split("\\s+"));
        claims.setStringListClaim("scope", scope); // multi-valued claims work too and will end up as a JSON array
        return claims;
    }

    public static String decodeCredentials(String cred) {
        return new String(org.apache.commons.codec.binary.Base64.decodeBase64(cred));
    }
}
