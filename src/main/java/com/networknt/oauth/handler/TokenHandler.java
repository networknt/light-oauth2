package com.networknt.oauth.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.exception.ApiException;
import com.networknt.security.JwtHelper;
import com.networknt.config.Config;
import com.networknt.status.Status;
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

    static final String UNABLE_TO_PARSE_FORM_DATA = "ERR12000";
    static final String UNSUPPORTED_GRANT_TYPE = "ERR12001";
    static final String MISSING_AUTHORIZATION_HEADER = "ERR12002";
    static final String INVALID_AUTHORIZATION_HEADER = "ERR12003";
    static final String INVALID_BASIC_CREDENTIALS = "ERR12004";
    static final String JSON_PROCESSING_EXCEPTION = "ERR12005";
    static final String CLIENT_ID_NOTFOUND = "ERR12006";
    static final String UNAUTHORIZED_CLIENT = "ERR12007";
    static final String INVALID_AUTHORIZATION_CODE = "ERR12008";
    static final String GENERIC_EXCEPTION = "ERR10014";

    static final String CLIENTS = "clients";
    static final String USERS = "users";
    private final ObjectMapper objectMapper;
    public static final Map<String, Object> clients = Config.getInstance().getJsonMapConfig(CLIENTS);
    public static final Map<String, Object> users = Config.getInstance().getJsonMapConfig(USERS);

    public TokenHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void handleRequest(HttpServerExchange exchange) throws ApiException {
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
            Status status = new Status(UNABLE_TO_PARSE_FORM_DATA, e.getMessage());
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        }
        try {
            if("client_credentials".equals(formMap.get("grant_type"))) {
                exchange.getResponseSender().send(objectMapper.writeValueAsString(handleClientCredentials(exchange)));
            } else if("authorization_code".equals(formMap.get("grant_type"))) {
                exchange.getResponseSender().send(objectMapper.writeValueAsString(handleAuthorizationCode(exchange, (String)formMap.get("code"))));
            } else {
                Status status = new Status(UNSUPPORTED_GRANT_TYPE, formMap.get("grant_type"));
                exchange.setStatusCode(status.getStatusCode());
                exchange.getResponseSender().send(status.toString());
                return;
            }
        } catch (JsonProcessingException e) {
            Status status = new Status(JSON_PROCESSING_EXCEPTION, e.getMessage());
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        } catch (ApiException e) {
            exchange.setStatusCode(e.getStatus().getStatusCode());
            exchange.getResponseSender().send(e.getStatus().toString());
        }
    }

    private Map<String, Object> handleClientCredentials(HttpServerExchange exchange) throws ApiException {
        // get Authorization header.
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if(auth == null || auth.trim().length() == 0) {
            throw new ApiException(new Status(MISSING_AUTHORIZATION_HEADER));
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
                        throw new ApiException(new Status(CLIENT_ID_NOTFOUND, clientId));
                    } else {
                        String secret = (String)cs.get("client_secret");
                        if(secret.equals(clientSecret)) {
                            String scope = (String)cs.get("scope");
                            String jwt = null;
                            try {
                                jwt = JwtHelper.getJwt(mockCcClaims(clientId, scope));
                            } catch (Exception e) {
                                throw new ApiException(new Status(GENERIC_EXCEPTION, e.getMessage()));
                            }
                            Map<String, Object> resMap = new HashMap<String, Object>();
                            resMap.put("access_token", jwt);
                            resMap.put("token_type", "bearer");
                            resMap.put("expires_in", 600);
                            return resMap;
                        } else {
                            throw new ApiException(new Status(UNAUTHORIZED_CLIENT));
                        }

                    }
                } else {
                    throw new ApiException(new Status(INVALID_BASIC_CREDENTIALS, credentials));
                }
            } else {
                throw new ApiException(new Status(INVALID_AUTHORIZATION_HEADER, auth));
            }

        }
    }

    private Map<String, Object> handleAuthorizationCode(HttpServerExchange exchange, String code) throws ApiException {
        // get Authorization header.
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if(auth == null || auth.trim().length() == 0) {
            throw new ApiException(new Status(MISSING_AUTHORIZATION_HEADER));
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
                        throw new ApiException(new Status(CLIENT_ID_NOTFOUND, clientId));
                    } else {
                        String secret = (String)cs.get("client_secret");
                        if(secret.equals(clientSecret)) {
                            String userId = (String)CodeHandler.codes.remove(code);
                            if(userId != null) {
                                Map<String, Object> user = (Map<String, Object>)users.get(userId);
                                String scope = (String)cs.get("scope");
                                String jwt = null;
                                try {
                                    jwt = JwtHelper.getJwt(mockAcClaims(clientId, scope, userId, (String)user.get("userType")));
                                } catch (Exception e) {
                                    throw new ApiException(new Status(GENERIC_EXCEPTION, e.getMessage()));
                                }
                                Map<String, Object> resMap = new HashMap<String, Object>();
                                resMap.put("access_token", jwt);
                                resMap.put("token_type", "bearer");
                                resMap.put("expires_in", 600);
                                return resMap;
                            } else {
                                throw new ApiException(new Status(INVALID_AUTHORIZATION_CODE, code));
                            }
                        } else {
                            throw new ApiException(new Status(UNAUTHORIZED_CLIENT));
                        }
                    }
                } else {
                    throw new ApiException(new Status(INVALID_BASIC_CREDENTIALS, credentials));
                }
            } else {
                throw new ApiException(new Status(INVALID_AUTHORIZATION_HEADER, auth));
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
