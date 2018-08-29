package com.networknt.oauth.code;

import com.networknt.config.Config;
import com.networknt.health.HealthGetHandler;
import com.networknt.info.ServerInfoGetHandler;
import com.networknt.oauth.code.handler.Oauth2CodeGetHandler;
import com.networknt.oauth.code.handler.Oauth2CodePostHandler;
import com.networknt.oauth.security.LightBasicAuthenticationMechanism;
import com.networknt.oauth.security.LightGSSAPIAuthenticationMechanism;
import com.networknt.oauth.security.LightIdentityManager;
import com.networknt.handler.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.api.GSSAPIServerSubjectFactory;
import io.undertow.security.handlers.AuthenticationCallHandler;
import io.undertow.security.handlers.AuthenticationConstraintHandler;
import io.undertow.security.handlers.AuthenticationMechanismsHandler;
import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.impl.CachedAuthenticatedSessionMechanism;
import io.undertow.security.impl.FormAuthenticationMechanism;
import io.undertow.server.HttpHandler;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.util.Methods;

import javax.security.auth.Subject;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.networknt.oauth.spnego.KerberosKDCUtil.login;

public class PathHandlerProvider implements HandlerProvider {
    private static final String SPNEGO_SERVICE_PASSWORD = "spnegoServicePassword";
    private static final String SECRET_CONFIG = "secret";
    private static final Map<String, Object> secret = Config.getInstance().getJsonMapConfig(SECRET_CONFIG);
    private static final String spnegoServicePassword = (String)secret.get(SPNEGO_SERVICE_PASSWORD);

    @Override
    public HttpHandler getHandler() {
        final IdentityManager basicIdentityManager = new LightIdentityManager();

        HttpHandler handler = Handlers.routing()
            .add(Methods.GET, "/health", new HealthGetHandler())
            .add(Methods.GET, "/server/info", new ServerInfoGetHandler())
            .add(Methods.GET, "/oauth2/code", addGetSecurity(new Oauth2CodeGetHandler(), basicIdentityManager))
            .add(Methods.POST, "/oauth2/code", addFormSecurity(new Oauth2CodePostHandler(), basicIdentityManager))
        ;
        return handler;
    }

    private HttpHandler addFormSecurity(final HttpHandler toWrap, final IdentityManager identityManager) {
        HttpHandler handler = toWrap;
        handler = new AuthenticationCallHandler(handler);
        handler = new AuthenticationConstraintHandler(handler);
        final List<AuthenticationMechanism> mechanisms = new ArrayList<>();
        mechanisms.add(new CachedAuthenticatedSessionMechanism());
        mechanisms.add(new FormAuthenticationMechanism("oauth2", "/login", "/error", "/oauth2/code"));
        handler = new AuthenticationMechanismsHandler(handler, mechanisms);
        handler = new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, identityManager, handler);
        handler = new SessionAttachmentHandler(handler, new InMemorySessionManager("oauth2"), new SessionCookieConfig());
        return handler;
    }

    private HttpHandler addGetSecurity(final HttpHandler toWrap, final IdentityManager identityManager) {
        HttpHandler handler = toWrap;
        handler = new AuthenticationCallHandler(handler);
        handler = new AuthenticationConstraintHandler(handler);
        List<AuthenticationMechanism> mechanisms = new ArrayList<>();
        // bypass the SPNEGO if service password is not even configured.
        if(spnegoServicePassword != null) {
            mechanisms.add(new LightGSSAPIAuthenticationMechanism(new SubjectFactory()));
        }
        mechanisms.add(new LightBasicAuthenticationMechanism("OAuth"));
        handler = new AuthenticationMechanismsHandler(handler, mechanisms);
        handler = new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, identityManager, handler);
        return handler;
    }

    private class SubjectFactory implements GSSAPIServerSubjectFactory {
        @Override
        public Subject getSubjectForHost(String hostName) throws GeneralSecurityException {
            return login("HTTP/" + hostName, spnegoServicePassword.toCharArray());
        }
    }
}
