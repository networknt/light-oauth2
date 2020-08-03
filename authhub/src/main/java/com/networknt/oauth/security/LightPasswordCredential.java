package com.networknt.oauth.security;

import com.sun.net.httpserver.HttpServer;
import io.undertow.security.idm.Credential;
import io.undertow.server.HttpServerExchange;

/**
 * For the OAuth 2.0 authorization code grant type, we need to pass the client specific authentication
 * class to the IdentityManager so that we can decide which class is used to do the authentication and
 * authorization based on the client. This gives us the flexibility to call different implementations
 * of authenticator based on which client is calling the authorization code service in OAuth 2.0 provider.
 *
 * For example, if the call comes from online banking application, then authenticate with retail
 * customers database. If the call comes from online brokerage application, then authenticate with
 * brokerage customers database.
 *
 * The userType can further differentiate which authenticator to use. For example, if the user type is
 * back office, then the authentication is done through LDAP/AD. If the user type is front office, then
 * the authentication is done with IMS Connect module to the Mainframe.
 *
 * @author Steve Hu
 */
public class LightPasswordCredential implements Credential {
    private final char[] password;
    private final String clientAuthClass;
    private final String userType;
    private HttpServerExchange exchange;

    public LightPasswordCredential(char[] password, String clientAuthClass, String userType, HttpServerExchange exchange) {
        this.password = password;
        this.clientAuthClass = clientAuthClass;
        this.userType = userType;
        this.exchange = exchange;
    }

    public char[] getPassword() { return this.password; }

    public String getClientAuthClass() { return this.clientAuthClass; }

    public String getUserType() { return this.userType; }

    public HttpServerExchange getExchange() {
        return exchange;
    }
}
