package com.networknt.oauth.token.handler;

import com.hazelcast.map.IMap;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Client;
import com.networknt.status.Status;
import com.networknt.exception.ApiException;
import com.networknt.utility.HashUtil;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.FlexBase64;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
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

public class Oauth2DerefGetHandler extends TokenAuditHandler implements LightHttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2DerefGetHandler.class);

    static final String DEREF_TOKEN_NOT_FOUND = "ERR12045";
    static final String MISSING_AUTHORIZATION_HEADER = "ERR12002";
    static final String CLIENT_NOT_FOUND = "ERR12014";
    static final String DEREF_CLIENT_NOT_MATCH = "ERR12044";
    static final String UNAUTHORIZED_CLIENT = "ERR12007";
    static final String JWT_TOKEN_NOT_FOUND = "ERR12046";
    static final String RUNTIME_EXCEPTION = "ERR10010";

    private static final String BASIC_PREFIX = BASIC + " ";
    private static final String LOWERCASE_BASIC_PREFIX = BASIC_PREFIX.toLowerCase(Locale.ENGLISH);
    private static final int PREFIX_LENGTH = BASIC_PREFIX.length();
    private static final String COLON = ":";

    public void handleRequest(HttpServerExchange exchange) throws Exception {
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
        String clientId = authenticate(authHeader);
        if(clientId != null) {
            if(logger.isDebugEnabled()) logger.debug("clientId = " + clientId);
            String token = exchange.getQueryParameters().get("token").getFirst();
            if(logger.isDebugEnabled()) logger.debug("token = " + token);

            Map<String, String> referenceMap = (Map<String, String>) CacheStartupHookProvider.hz.getMap("references").get(token);
            if(referenceMap == null) {
                setExchangeStatus(exchange, DEREF_TOKEN_NOT_FOUND, token);
                processAudit(exchange);
                return;
            }
            String refClientId = referenceMap.get("clientId");
            if(refClientId != null && !refClientId.equals(clientId)) {
                // it is not the right dereference client.
                setExchangeStatus(exchange, DEREF_CLIENT_NOT_MATCH, clientId);
                processAudit(exchange);
                return;
            }

            String jwt = referenceMap.get("jwt");
            if(jwt == null) {
                setExchangeStatus(exchange, JWT_TOKEN_NOT_FOUND, token);
                processAudit(exchange);
                return;
            }
            exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "application/text");
            exchange.getResponseSender().send(jwt);
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

}
