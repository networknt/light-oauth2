package com.networknt.oauth.token.helper;

import io.undertow.server.HttpServerExchange;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpAuth {
    private boolean headerAvailable;
    private boolean basicAuth = false;
    private boolean invalidCredentials;
    private String clientId;
    private String clientSecret;
    private String credentials;
    private String auth;

    public HttpAuth(HttpServerExchange exchange) {
        process(exchange);
    }

    private void process(HttpServerExchange exchange) {
        auth = exchange.getRequestHeaders().getFirst("Authorization");
        if(auth == null || auth.trim().length() == 0) {
            headerAvailable = false;
        } else {
            headerAvailable = true;

            String basic = auth.substring(0, 5);
            if("BASIC".equalsIgnoreCase(basic)) {
                basicAuth = true;
                credentials = auth.substring(6);
                int pos = credentials.indexOf(':');
                if (pos == -1) {
                    credentials = decodeCredentials(credentials);
                }
                pos = credentials.indexOf(':');
                if (pos != -1) {
                    clientId = credentials.substring(0, pos);
                    clientSecret = credentials.substring(pos + 1);
                    invalidCredentials = false;
                } else {
                    invalidCredentials = true;
                }
            }
        }
    }

    private String decodeCredentials(String cred) {
        return new String(org.apache.commons.codec.binary.Base64.decodeBase64(cred), UTF_8);
    }

    public boolean isHeaderAvailable() {
        return headerAvailable;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public boolean isInvalidCredentials() {
        return invalidCredentials;
    }

    public boolean isValid() {
        return isHeaderAvailable() && !isInvalidCredentials();
    }

    public boolean isBasicAuth() {
        return basicAuth;
    }

    public String getCredentials() {
        return credentials;
    }

    public String getAuth() {
        return auth;
    }
}
