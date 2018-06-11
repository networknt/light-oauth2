package com.networknt.oauth.authorize.handler;

import com.hazelcast.core.IMap;
import com.networknt.config.Config;
import com.networknt.oauth.cache.AuditInfoHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.OAuth2Constants;
import com.networknt.oauth.cache.model.AuditInfo;
import com.networknt.oauth.cache.model.Client;
import com.networknt.oauth.cache.model.Oauth2Service;
import com.networknt.status.Status;
import com.networknt.utility.CodeVerifierUtil;
import com.networknt.utility.Util;
import io.undertow.security.api.SecurityContext;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * This handler is for get request, the credentials might in header or just query parameters.
 * Need to check client info in order to find out which class to handle the authentication
 * clients are cached so that it has better performance. If client_id cannot be found in cache,
 * go to db to get it. It must be something added recently and not in cache yet.
 *
 */
public class Oauth2AuthorizeGetHandler extends AuditInfoHandler implements HttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2AuthorizeGetHandler.class);
    static final String CLIENT_NOT_FOUND = "ERR12014";
    static final String INVALID_CODE_CHALLENGE_METHOD = "ERR12033";
    static final String CODE_CHALLENGE_TOO_SHORT = "ERR12034";
    static final String CODE_CHALLENGE_TOO_LONG = "ERR12035";
    static final String INVALID_CODE_CHALLENGE_FORMAT = "ERR12036";
    private final static String CONFIG = "oauth_authorize";
    private final static OauthAuthConfig config = (OauthAuthConfig) Config.getInstance().getJsonObjectConfig(CONFIG, OauthAuthConfig.class);

    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        // parse all the parameters here as this is a redirected get request.
        Map<String, String> params = new HashMap<>();
        Map<String, Deque<String>> pnames = exchange.getQueryParameters();
        for (Map.Entry<String, Deque<String>> entry : pnames.entrySet()) {
            String pname = entry.getKey();
            Iterator<String> pvalues = entry.getValue().iterator();
            if(pvalues.hasNext()) {
                params.put(pname, pvalues.next());
            }
        }
        if(logger.isDebugEnabled()) logger.debug("params", params);
        String clientId = params.get("client_id");
        // check if the client_id is valid
        IMap<String, Client> clients = CacheStartupHookProvider.hz.getMap("clients");
        Client client = clients.get(clientId);
        if(client == null) {
            Status status = new Status(CLIENT_NOT_FOUND, clientId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
        } else {
            String code = Util.getUUID();
            final SecurityContext context = exchange.getSecurityContext();
            String userId = context.getAuthenticatedAccount().getPrincipal().getName();
            Map<String, String> codeMap = new HashMap<>();
            codeMap.put("userId", userId);
            String scope = params.get("scope");
            if(scope != null) {
                codeMap.put("scope", scope);
            }
            String redirectUri = params.get("redirect_uri");
            if(redirectUri == null) {
                redirectUri = client.getRedirectUri();
            } else {
                codeMap.put("redirectUri", redirectUri);
            }
            // https://tools.ietf.org/html/rfc7636#section-4 PKCE
            String codeChallenge = params.get(OAuth2Constants.CODE_CHALLENGE);
            String codeChallengeMethod = params.get(OAuth2Constants.CODE_CHALLENGE_METHOD);
            if (codeChallenge == null) {
                // PKCE is not used by this client.
                // Do we need to force native client to use PKCE?
            } else {
                if(codeChallengeMethod != null) {
                    // https://tools.ietf.org/html/rfc7636#section-4.2
                    // plain or S256
                    if (!codeChallengeMethod.equals(CodeVerifierUtil.CODE_CHALLENGE_METHOD_S256) &&
                            !codeChallengeMethod.equals(CodeVerifierUtil.CODE_CHALLENGE_METHOD_PLAIN)) {
                        Status status = new Status(INVALID_CODE_CHALLENGE_METHOD, codeChallengeMethod);
                        exchange.setStatusCode(status.getStatusCode());
                        exchange.getResponseSender().send(status.toString());
                        processAudit(exchange);
                        return;
                    }
                } else {
                    // https://tools.ietf.org/html/rfc7636#section-4.3
                    // default code_challenge_method is plain
                    codeChallengeMethod = CodeVerifierUtil.CODE_CHALLENGE_METHOD_PLAIN;
                }
                // validate codeChallenge.
                if(codeChallenge.length() < CodeVerifierUtil.MIN_CODE_VERIFIER_LENGTH) {
                    Status status = new Status(CODE_CHALLENGE_TOO_SHORT, codeChallenge);
                    exchange.setStatusCode(status.getStatusCode());
                    exchange.getResponseSender().send(status.toString());
                    processAudit(exchange);
                    return;
                }
                if(codeChallenge.length() > CodeVerifierUtil.MAX_CODE_VERIFIER_LENGTH) {
                    Status status = new Status(CODE_CHALLENGE_TOO_LONG, codeChallenge);
                    exchange.setStatusCode(status.getStatusCode());
                    exchange.getResponseSender().send(status.toString());
                    processAudit(exchange);
                    return;
                }
                // check the format
                Matcher m = CodeVerifierUtil.VALID_CODE_CHALLENGE_PATTERN.matcher(codeChallenge);
                if(!m.matches()) {
                    Status status = new Status(INVALID_CODE_CHALLENGE_FORMAT, codeChallenge);
                    exchange.setStatusCode(status.getStatusCode());
                    exchange.getResponseSender().send(status.toString());
                    processAudit(exchange);
                    return;
                }
                // put the code challenge and method into the codes map.
                codeMap.put(OAuth2Constants.CODE_CHALLENGE, codeChallenge);
                codeMap.put(OAuth2Constants.CODE_CHALLENGE_METHOD, codeChallengeMethod);
            }

            CacheStartupHookProvider.hz.getMap("codes").set(code, codeMap);
            redirectUri = redirectUri + "?code=" + code;
            String state = params.get("state");
            if(state != null) {
                redirectUri = redirectUri + "&state=" + state;
            }
            if(logger.isDebugEnabled()) logger.debug("redirectUri = " + redirectUri);
            // now redirect here.
            exchange.setStatusCode(StatusCodes.FOUND);
            exchange.getResponseHeaders().put(Headers.LOCATION, redirectUri);
            exchange.endExchange();
            processAudit(exchange);
        }
    }

    private void processAudit(HttpServerExchange exchange) throws Exception {
        if (config.isEnableAudit() ) {
            AuditInfo auditInfo = new AuditInfo();
            auditInfo.setServiceId(Oauth2Service.REFRESHTOKEN);
            auditInfo.setEndpoint(exchange.getHostName() + exchange.getRelativePath());
            auditInfo.setRequestHeader(Config.getInstance().getMapper().writeValueAsString(exchange.getRequestHeaders()));
            auditInfo.setRequestBody(Config.getInstance().getMapper().writeValueAsString(exchange.getRequestCookies()));
            auditInfo.setResponseHeader(Config.getInstance().getMapper().writeValueAsString(exchange.getResponseHeaders()));
            auditInfo.setResponseBody(Config.getInstance().getMapper().writeValueAsString(exchange.getResponseCookies()));
            saveAudit(auditInfo);
        }
    }
}
