package com.networknt.oauth.token.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.ClientType;
import com.hazelcast.core.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.exception.ApiException;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.auth.Authenticator;
import com.networknt.oauth.auth.DefaultAuth;
import com.networknt.oauth.cache.AuditInfoHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.OAuth2Constants;
import com.networknt.oauth.cache.model.*;
import com.networknt.oauth.security.LightPasswordCredential;
import com.networknt.oauth.token.helper.HttpAuth;
import com.networknt.security.JwtConfig;
import com.networknt.security.JwtIssuer;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.status.Status;
import com.networknt.utility.CodeVerifierUtil;
import com.networknt.utility.HashUtil;
import com.networknt.utility.Util;
import io.undertow.security.idm.Account;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Headers;
import org.jose4j.jwt.JwtClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.regex.Matcher;

/**
 * This handler will issue the token based on the information about the client and user. The content
 * of the token will be depending on the grant type. Also, the format of the token is depending on
 * the client type. For public client type, the token is by reference token, other client type will
 * issue the JWT token directly. There is an endpoint in this service to dereference token once the
 * request comes into the internal network.
 *
 * We also introduce a new client type called trusted and limited other grant types except authorization
 * code and client credentials to be used.
 *
 * @author Steve Hu
 *
 */
public class Oauth2TokenPostHandler extends TokenAuditHandler implements LightHttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(Oauth2TokenPostHandler.class);

    private static final String UNABLE_TO_PARSE_FORM_DATA = "ERR12000";
    private static final String UNSUPPORTED_GRANT_TYPE = "ERR12001";
    private static final String MISSING_AUTHORIZATION_HEADER = "ERR12002";
    private static final String INVALID_AUTHORIZATION_HEADER = "ERR12003";
    private static final String INVALID_BASIC_CREDENTIALS = "ERR12004";
    private static final String JSON_PROCESSING_EXCEPTION = "ERR12005";
    private static final String CLIENT_NOT_FOUND = "ERR12014";
    private static final String UNAUTHORIZED_CLIENT = "ERR12007";
    private static final String INVALID_AUTHORIZATION_CODE = "ERR12008";
    private static final String GENERIC_EXCEPTION = "ERR10014";
    private static final String RUNTIME_EXCEPTION = "ERR10010";
    private static final String USERNAME_REQUIRED = "ERR12022";
    private static final String PASSWORD_REQUIRED = "ERR12023";
    private static final String INCORRECT_PASSWORD = "ERR12016";
    private static final String NOT_TRUSTED_CLIENT = "ERR12024";
    private static final String MISSING_REDIRECT_URI = "ERR12025";
    private static final String MISMATCH_REDIRECT_URI = "ERR12026";
    private static final String MISMATCH_SCOPE = "ERR12027";
    private static final String MISMATCH_CLIENT_ID = "ERR12028";
    private static final String REFRESH_TOKEN_NOT_FOUND = "ERR12029";
    private static final String USER_ID_REQUIRED_FOR_CLIENT_AUTHENTICATED_USER_GRANT_TYPE = "ERR12031";
    private static final String USER_TYPE_REQUIRED_FOR_CLIENT_AUTHENTICATED_USER_GRANT_TYPE = "ERR12032";
    private static final String INVALID_CODE_VERIFIER = "ERR12037";
    private static final String CODE_VERIFIER_TOO_SHORT = "ERR12038";
    private static final String CODE_VERIFIER_TOO_LONG = "ERR12039";
    private static final String CODE_VERIFIER_MISSING = "ERR12040";
    private static final String CODE_VERIFIER_FAILED = "ERR12041";
    private static final String INVALID_CODE_CHALLENGE_METHOD = "ERR12033";
    private static final String CLIENT_AUTHENTICATE_CLASS_NOT_FOUND = "ERR10043";

    static JwtConfig config = (JwtConfig)Config.getInstance().getJsonObjectConfig("jwt", JwtConfig.class);
    private final static String CONFIG = "oauth_token";
    private final static OauthTokenConfig oauth_config = (OauthTokenConfig) Config.getInstance().getJsonObjectConfig(CONFIG, OauthTokenConfig.class);
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        ObjectMapper mapper = Config.getInstance().getMapper();
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        Map<String, Object> formMap = new HashMap<>();

        final FormParserFactory parserFactory = FormParserFactory.builder().build();
        final FormDataParser parser = parserFactory.createParser(exchange);
        try {
            FormData data = parser.parseBlocking();
            for (String fd : data) {
                for (FormData.FormValue val : data.get(fd)) {
                    //logger.debug("fd = " + fd + " value = " + val.getValue());
                    formMap.put(fd, val.getValue());
                }
            }
        } catch (Exception e) {
            logger.error("Exception:", e);
            setExchangeStatus(exchange, UNABLE_TO_PARSE_FORM_DATA, e.getMessage());
            processAudit(exchange);
            return;
        }
        try {
            String grantType = (String)formMap.remove("grant_type");
            if("client_credentials".equals(grantType)) {
                exchange.getResponseSender().send(mapper.writeValueAsString(handleClientCredentials(exchange, formMap)));
            } else if("authorization_code".equals(grantType)) {
                exchange.getResponseSender().send(mapper.writeValueAsString(handleAuthorizationCode(exchange, formMap)));
            } else if("password".equals(grantType)) {
                exchange.getResponseSender().send(mapper.writeValueAsString(handlePassword(exchange, formMap)));
            } else if("refresh_token".equals(grantType)) {
                exchange.getResponseSender().send(mapper.writeValueAsString(handleRefreshToken(exchange, formMap)));
            } else if("client_authenticated_user".equals(grantType)) {
                exchange.getResponseSender().send(mapper.writeValueAsString(handleClientAuthenticatedUser(exchange, formMap)));
            } else {
                setExchangeStatus(exchange, UNSUPPORTED_GRANT_TYPE, grantType);
            }
        } catch (JsonProcessingException e) {
            logger.error("JsonProcessingException:", e);
            setExchangeStatus(exchange, JSON_PROCESSING_EXCEPTION, e.getMessage());
        } catch (ApiException e) {
            logger.error("ApiException", e);
            exchange.setStatusCode(e.getStatus().getStatusCode());
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(e.getStatus().toString());
        }
        processAudit(exchange);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> handleClientCredentials(HttpServerExchange exchange, Map<String, Object> formMap) throws ApiException {
        String scope = (String)formMap.get("scope");
        if(logger.isDebugEnabled()) logger.debug("scope = " + scope);
        Client client = authenticateClient(exchange, formMap);
        if(client != null) {
            if(scope == null) {
                scope = client.getScope();
            } else {
                // make sure scope is in scope defined in client.
                if(!matchScope(scope, client.getScope())) {
                    throw new ApiException(new Status(MISMATCH_SCOPE, scope, client.getScope()));
                }
            }
            String jwt;
            Map<String, Object> customMap = null;
            try {
                // assume that the custom_claim is in format of json map string.
                String customClaim = client.getCustomClaim();
                if(customClaim != null && customClaim.length() > 0) {
                    customMap = Config.getInstance().getMapper().readValue(customClaim, new TypeReference<Map<String, Object>>(){});
                }
                jwt = JwtIssuer.getJwt(mockCcClaims(client.getClientId(), scope, customMap));
            } catch (Exception e) {
                logger.error("Exception:", e);
                throw new ApiException(new Status(GENERIC_EXCEPTION, e.getMessage()));
            }
            // if the client type is external, save the jwt to reference map and send the reference
            if(Client.ClientTypeEnum.EXTERNAL == client.getClientType()) {
                jwt = jwtReference(jwt, client.getDerefClientId());
            }
            Map<String, Object> resMap = new HashMap<>();
            resMap.put("access_token", jwt);
            resMap.put("token_type", "bearer");
            resMap.put("expires_in", config.getExpiredInMinutes()*60);
            return resMap;
        }
        return new HashMap<>(); // return an empty hash map. this is actually not reachable at all.
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> handleAuthorizationCode(HttpServerExchange exchange, Map<String, Object> formMap) throws ApiException {
        String code = (String)formMap.get("code");
        String redirectUri = (String)formMap.get("redirect_uri");
        String csrf = (String)formMap.get("csrf");
        if(logger.isDebugEnabled()) logger.debug("code = " + code + " redirectUri = " + redirectUri);
        Client client = authenticateClient(exchange, formMap);
        if(client != null) {
            // authorization code can only be used once for security reason.
            Map<String, String> codeMap = (Map<String, String>)CacheStartupHookProvider.hz.getMap("codes").remove(code);
            String userId = codeMap.get("userId");
            // get userType and roles from code map as there are some authenticators doesn't support user_profile table.
            String userType = codeMap.get("userType");
            String roles = codeMap.get("roles");
            String uri = codeMap.get("redirectUri");
            String scope = codeMap.get("scope");
            if(logger.isDebugEnabled()) logger.debug("variable from codeMap cache userId = " + userId + " redirectUri = " + redirectUri + " scope = " + scope + " userType = " + userType + " roles = " + roles);

            // PKCE
            String codeChallenge = codeMap.get(OAuth2Constants.CODE_CHALLENGE);
            String codeChallengeMethod = codeMap.get(OAuth2Constants.CODE_CHALLENGE_METHOD);
            String codeVerifier = (String)formMap.get(OAuth2Constants.CODE_VERIFIER);

            if(userId != null) {
                // if uri is not null, redirectUri must not be null and must be identical.
                if(uri != null) {
                    if(redirectUri == null) {
                        throw new ApiException(new Status(MISSING_REDIRECT_URI, uri));
                    } else {
                        if(!uri.equals(redirectUri)) {
                            throw new ApiException(new Status(MISMATCH_REDIRECT_URI, redirectUri, uri));
                        }
                    }
                }
                // no passed in scope, take the client scope, otherwise, validate the passed in scope
                if(scope == null) {
                    scope = client.getScope();
                } else {
                    // make sure scope is in scope defined in client.
                    if(!matchScope(scope, client.getScope())) {
                        throw new ApiException(new Status(MISMATCH_SCOPE, scope, client.getScope()));
                    }
                }
                // PKCE code verifier check against code challenge
                if (codeChallenge != null && codeChallengeMethod != null) {
                    // based on whether code_challenge has been stored at corresponding authorization code request previously
                    // decide whether this client(RP) supports PKCE
                    if(codeVerifier == null || codeVerifier.trim().length() == 0) {
                        throw new ApiException(new Status(CODE_VERIFIER_MISSING));
                    }
                    if(codeVerifier.length() < CodeVerifierUtil.MIN_CODE_VERIFIER_LENGTH) {
                        throw new ApiException(new Status(CODE_VERIFIER_TOO_SHORT, codeVerifier));
                    }
                    if(codeVerifier.length() > CodeVerifierUtil.MAX_CODE_VERIFIER_LENGTH) {
                        throw new ApiException(new Status(CODE_VERIFIER_TOO_LONG, codeVerifier));
                    }

                    Matcher m = CodeVerifierUtil.VALID_CODE_CHALLENGE_PATTERN.matcher(codeVerifier);
                    if(!m.matches()) {
                        throw new ApiException(new Status(INVALID_CODE_VERIFIER, codeVerifier));
                    }

                    // https://tools.ietf.org/html/rfc7636#section-4.2
                    // plain or S256
                    if (codeChallengeMethod.equals(CodeVerifierUtil.CODE_CHALLENGE_METHOD_S256)) {
                        String s = CodeVerifierUtil.deriveCodeVerifierChallenge(codeVerifier);
                        if(!codeChallenge.equals(s)) {
                            throw new ApiException(new Status(CODE_VERIFIER_FAILED));
                        }
                    } else if(codeChallengeMethod.equals(CodeVerifierUtil.CODE_CHALLENGE_METHOD_PLAIN)){
                        if(!codeChallenge.equals(codeVerifier)) {
                            throw new ApiException(new Status(CODE_VERIFIER_FAILED));
                        }
                    } else {
                        throw new ApiException(new Status(INVALID_CODE_CHALLENGE_METHOD, codeChallengeMethod));
                    }
                }

                String jwt;
                Map<String, Object> customMap = null;
                try {
                    // assume that the custom_claim is in format of json map string.
                    String customClaim = client.getCustomClaim();
                    if(customClaim != null && customClaim.length() > 0) {
                        customMap = Config.getInstance().getMapper().readValue(customClaim, new TypeReference<Map<String, Object>>(){});
                    }
                    jwt = JwtIssuer.getJwt(mockAcClaims(client.getClientId(), scope, userId, userType, roles, csrf, customMap));
                } catch (Exception e) {
                    throw new ApiException(new Status(GENERIC_EXCEPTION, e.getMessage()));
                }
                // generate a refresh token and associate it with userId and clientId
                String refreshToken = UUID.randomUUID().toString();
                RefreshToken token = new RefreshToken();
                token.setRefreshToken(refreshToken);
                token.setUserId(userId);
                token.setUserType(userType);
                token.setRoles(roles);
                token.setClientId(client.getClientId());
                token.setScope(scope);
                IMap<String, RefreshToken> tokens = CacheStartupHookProvider.hz.getMap("tokens");
                tokens.set(refreshToken, token);
                // if the client type is external, save the jwt to reference map and send the reference
                if(Client.ClientTypeEnum.EXTERNAL == client.getClientType()) {
                    jwt = jwtReference(jwt, client.getDerefClientId());
                }
                Map<String, Object> resMap = new HashMap<>();
                resMap.put("access_token", jwt);
                resMap.put("token_type", "bearer");
                resMap.put("expires_in", config.getExpiredInMinutes()*60);
                resMap.put("refresh_token", refreshToken);
                return resMap;
            } else {
                throw new ApiException(new Status(INVALID_AUTHORIZATION_CODE, code));
            }
        }
        return new HashMap<>(); // return an empty hash map. this is actually not reachable at all.
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> handlePassword(HttpServerExchange exchange, Map<String, Object> formMap) throws ApiException {
        String userId = (String)formMap.get("username");
        String scope = (String)formMap.get("scope");
        String userType = (String)formMap.get("user_type");
        String roles = (String)formMap.get("roles");
        if(logger.isDebugEnabled()) logger.debug("userId = " + userId + " scope = " + scope);
        char[] password = null;
        if(formMap.get("password") != null) {
            password = ((String)formMap.get("password")).toCharArray();
        }

        Client client = authenticateClient(exchange, formMap);
        if(client != null) {
            // authenticate user with credentials
            if(userId != null) {
                if(password != null) {
                    // make sure that the client is trusted.
                    if(client.getClientType() == Client.ClientTypeEnum.TRUSTED) {
                        if (scope == null) {
                            scope = client.getScope(); // use the default scope defined in client if scope is not passed in
                        } else {
                            // make sure scope is in scope defined in client.
                            if (!matchScope(scope, client.getScope())) {
                                throw new ApiException(new Status(MISMATCH_SCOPE, scope, client.getScope()));
                            }
                        }

                        // authenticate user with different authenticators.
                        String clientAuthClass = client.getAuthenticateClass();
                        Class clazz = DefaultAuth.class;
                        if(clientAuthClass != null && clientAuthClass.trim().length() > 0) {
                            try {
                                clazz = Class.forName(clientAuthClass);
                            } catch (ClassNotFoundException e) {
                                logger.error("Authenticate Class " + clientAuthClass + " not found.", e);
                                throw new ApiException(new Status(CLIENT_AUTHENTICATE_CLASS_NOT_FOUND, clientAuthClass));
                            }
                        }
                        Authenticator authenticator = SingletonServiceFactory.getBean(Authenticator.class, clazz);

                        Account account = authenticator.authenticate(userId, new LightPasswordCredential(password, clientAuthClass, userType));
                        if(account == null) {
                            throw new ApiException(new Status(INCORRECT_PASSWORD));
                        } else {
                            try {
                                Map<String, Object> customMap = null;
                                // assume that the custom_claim is in format of json map string.
                                String customClaim = client.getCustomClaim();
                                if(customClaim != null && customClaim.length() > 0) {
                                    customMap = Config.getInstance().getMapper().readValue(customClaim, new TypeReference<Map<String, Object>>(){});
                                }
                                String jwt = JwtIssuer.getJwt(mockAcClaims(client.getClientId(), scope, userId, userType, roles, null, customMap));
                                // generate a refresh token and associate it with userId and clientId
                                String refreshToken = UUID.randomUUID().toString();
                                RefreshToken token = new RefreshToken();
                                token.setRefreshToken(refreshToken);
                                token.setUserId(userId);
                                token.setClientId(client.getClientId());
                                token.setScope(scope);
                                IMap<String, RefreshToken> tokens = CacheStartupHookProvider.hz.getMap("tokens");
                                tokens.set(refreshToken, token);

                                Map<String, Object> resMap = new HashMap<>();
                                resMap.put("access_token", jwt);
                                resMap.put("token_type", "bearer");
                                resMap.put("expires_in", config.getExpiredInMinutes()*60);
                                resMap.put("refresh_token", refreshToken);
                                return resMap;
                            } catch (Exception e) {
                                logger.error("Exception:", e);
                                throw new ApiException(new Status(GENERIC_EXCEPTION, e.getMessage()));
                            }
                        }
                    } else {
                        throw new ApiException(new Status(NOT_TRUSTED_CLIENT));
                    }
                } else {
                    throw new ApiException(new Status(PASSWORD_REQUIRED));
                }
            } else {
                throw new ApiException(new Status(USERNAME_REQUIRED));
            }
        }
        return new HashMap<>(); // return an empty hash map. this is actually not reachable at all.
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> handleRefreshToken(HttpServerExchange exchange, Map<String, Object> formMap) throws ApiException {
        String refreshToken = (String)formMap.get("refresh_token");
        String scope = (String) formMap.get("scope");
        // Get csrf token from the input. every time a new token is generated, a new csrf token will be used.
        String csrf = (String)formMap.get("csrf");
        if(logger.isDebugEnabled()) logger.debug("refreshToken = " + refreshToken + " scope = " + scope);
        Client client = authenticateClient(exchange, formMap);
        if(client != null) {
            // make sure that the refresh token can be found and client_id matches.
            IMap<String, RefreshToken> tokens = CacheStartupHookProvider.hz.getMap("tokens");
            RefreshToken token = tokens.remove(refreshToken);
            if(token != null) {
                String userId = token.getUserId();
                String userType = token.getUserType();
                String roles = token.getRoles();
                String clientId = token.getClientId();
                String oldScope = token.getScope();

                if(client.getClientId().equals(clientId)) {
                    if(scope == null) {
                        scope = oldScope; // use the previous scope when access token is generated
                    } else {
                        // make sure scope is the same as oldScope or contained in oldScope.
                        if(!matchScope(scope, oldScope)) {
                            throw new ApiException(new Status(MISMATCH_SCOPE, scope, oldScope));
                        }
                    }
                    String jwt;
                    Map<String, Object> customMap = null;
                    // assume that the custom_claim is in format of json map string.
                    String customClaim = client.getCustomClaim();
                    try {
                        if(customClaim != null && customClaim.length() > 0) {
                            customMap = Config.getInstance().getMapper().readValue(customClaim, new TypeReference<Map<String, Object>>(){});
                        }
                        jwt = JwtIssuer.getJwt(mockAcClaims(client.getClientId(), scope, userId, userType, roles, csrf, customMap));
                    } catch (Exception e) {
                        throw new ApiException(new Status(GENERIC_EXCEPTION, e.getMessage()));
                    }
                    // generate a new refresh token and associate it with userId and clientId
                    String newRefreshToken = UUID.randomUUID().toString();
                    RefreshToken newToken = new RefreshToken();
                    newToken.setRefreshToken(newRefreshToken);
                    newToken.setUserId(userId);
                    newToken.setUserType(userType);
                    newToken.setRoles(roles);
                    newToken.setClientId(client.getClientId());
                    newToken.setScope(scope);
                    tokens.put(refreshToken, newToken);
                    // if the client type is external, save the jwt to reference map and send the reference
                    if(Client.ClientTypeEnum.EXTERNAL == client.getClientType()) {
                        jwt = jwtReference(jwt, client.getDerefClientId());
                    }
                    Map<String, Object> resMap = new HashMap<>();
                    resMap.put("access_token", jwt);
                    resMap.put("token_type", "bearer");
                    resMap.put("expires_in", config.getExpiredInMinutes()*60);
                    resMap.put("refresh_token", newRefreshToken);
                    return resMap;

                } else {
                    // mismatched client id
                    throw new ApiException(new Status(MISMATCH_CLIENT_ID, client.getClientId(), clientId));
                }
            } else {
                // refresh token cannot be found.
                throw new ApiException(new Status(REFRESH_TOKEN_NOT_FOUND, refreshToken));
            }
        }
        return new HashMap<>(); // return an empty hash map. this is actually not reachable at all.
    }

    /**
     * This grant type is custom grant type that assume client has already authenticated the user and only send the user info
     * to the authorization server to get the access token. The token is similar with authorization code token. All extra info
     * from the formMap will be put into the token as custom claim.
     *
     * Also, only
     *
     * @param exchange
     * @param formMap
     * @return
     * @throws ApiException
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> handleClientAuthenticatedUser(HttpServerExchange exchange, Map<String, Object> formMap) throws ApiException {
        if(logger.isDebugEnabled()) logger.debug("client authenticated user grant formMap = " + formMap);
        Client client = authenticateClient(exchange, formMap);
        if(client != null) {
            // make sure that client is trusted
            if(Client.ClientTypeEnum.TRUSTED == client.getClientType()) {
                String scope = (String)formMap.remove("scope");
                if(scope == null) {
                    scope = client.getScope(); // use the default scope defined in client if scope is not passed in
                } else {
                    // make sure scope is in scope defined in client.
                    if(!matchScope(scope, client.getScope())) {
                        throw new ApiException(new Status(MISMATCH_SCOPE, scope, client.getScope()));
                    }
                }
                // make sure that userId and userType are passed in the formMap.
                String userId = (String)formMap.remove("userId");
                if(userId == null) {
                    throw new ApiException(new Status(USER_ID_REQUIRED_FOR_CLIENT_AUTHENTICATED_USER_GRANT_TYPE));
                }

                String userType = (String)formMap.remove("userType");
                if(userType == null) {
                    throw new ApiException(new Status(USER_TYPE_REQUIRED_FOR_CLIENT_AUTHENTICATED_USER_GRANT_TYPE));

                }
                String roles = (String)formMap.remove("roles");
                String jwt;
                try {
                    jwt = JwtIssuer.getJwt(mockAcClaims(client.getClientId(), scope, userId, userType, roles, null, formMap));
                } catch (Exception e) {
                    throw new ApiException(new Status(GENERIC_EXCEPTION, e.getMessage()));
                }

                // generate a refresh token and associate it with userId and clientId
                String refreshToken = UUID.randomUUID().toString();
                RefreshToken token = new RefreshToken();
                token.setRefreshToken(refreshToken);
                token.setUserId(userId);
                token.setUserType(userType);
                token.setRoles(roles);
                token.setClientId(client.getClientId());
                token.setScope(scope);
                IMap<String, RefreshToken> tokens = CacheStartupHookProvider.hz.getMap("tokens");
                tokens.set(refreshToken, token);

                Map<String, Object> resMap = new HashMap<>();
                resMap.put("access_token", jwt);
                resMap.put("token_type", "bearer");
                resMap.put("expires_in", config.getExpiredInMinutes()*60);
                resMap.put("refresh_token", refreshToken);
                return resMap;
            } else {
                // not trusted client, this is not allowed.
                throw new ApiException(new Status(NOT_TRUSTED_CLIENT));
            }
        }
        return new HashMap<>(); // return an empty hash map. this is actually not reachable at all.
    }

    private Client authenticateClient(HttpServerExchange exchange, Map<String, Object> formMap) throws ApiException {
        HttpAuth httpAuth = new HttpAuth(exchange);

        String clientId;
        String clientSecret;
        if(!httpAuth.isHeaderAvailable()) {
            clientId = (String)formMap.remove("client_id");
            clientSecret = (String)formMap.remove("client_secret");
        } else {
            clientId = httpAuth.getClientId();
            clientSecret = httpAuth.getClientSecret();
        }

        if(clientId == null || clientId.trim().isEmpty() || clientSecret == null || clientSecret.trim().isEmpty()) {
            if(!httpAuth.isHeaderAvailable()) {
                throw new ApiException(new Status(MISSING_AUTHORIZATION_HEADER));
            } else if(httpAuth.isInvalidCredentials()) {
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

    private JwtClaims mockCcClaims(String clientId, String scopeString, Map<String, Object> formMap) {
        JwtClaims claims = JwtIssuer.getDefaultJwtClaims();
        claims.setClaim("client_id", clientId);
        List<String> scope = Arrays.asList(scopeString.split("\\s+"));
        claims.setStringListClaim("scope", scope); // multi-valued claims work too and will end up as a JSON array
        if(formMap != null) {
            for(Map.Entry<String, Object> entry : formMap.entrySet()) {
                claims.setClaim(entry.getKey(), entry.getValue());
            }
        }
        return claims;
    }

    private JwtClaims mockAcClaims(String clientId, String scopeString, String userId, String userType, String roleString, String csrf, Map<String, Object> formMap) {
        JwtClaims claims = JwtIssuer.getDefaultJwtClaims();
        claims.setClaim("user_id", userId);
        claims.setClaim("user_type", userType);
        claims.setClaim("client_id", clientId);
        if(csrf != null) claims.setClaim("csrf", csrf);
        if(scopeString != null && scopeString.trim().length() > 0) {
            List<String> scope = Arrays.asList(scopeString.split("\\s+"));
            claims.setStringListClaim("scope", scope); // multi-valued claims work too and will end up as a JSON array
        }
        if(roleString != null && roleString.trim().length() > 0) {
            List<String> roles = Arrays.asList(roleString.split("\\s+"));
            claims.setStringListClaim("roles", roles); // multi-valued claims work too and will end up as a JSON array
        }

        if(formMap != null) {
            for(Map.Entry<String, Object> entry : formMap.entrySet()) {
                claims.setClaim(entry.getKey(), entry.getValue());
            }
        }
        return claims;
    }

    private static boolean matchScope(String s1, String s2) {
        boolean matched = true;
        if(s1 == null || s2 == null) {
            matched = false;
        } else {
            if(!s1.equals(s2)) {
                String[] split = s1.split("\\s+");
                for (String aSplit : split) {
                    if (!s2.contains(aSplit)) {
                        matched = false;
                        break;
                    }
                }
            }
        }
        return matched;
    }

    private String jwtReference(String jwt, String clientId) {
        String uuid = Util.getUUID();
        Map<String, String> referenceMap = new HashMap<>();
        referenceMap.put("jwt", jwt);
        if(clientId != null) {
            referenceMap.put("clientId", clientId);
        }
        CacheStartupHookProvider.hz.getMap("references").set(uuid, referenceMap);
        return uuid;
    }
}
