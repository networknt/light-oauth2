package com.networknt.oauth.code;

import com.hazelcast.core.IMap;
import com.networknt.health.HealthGetHandler;
import com.networknt.info.ServerInfoGetHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.User;
import com.networknt.oauth.code.handler.MapIdentityManager;
import com.networknt.oauth.code.handler.Oauth2CodeGetHandler;
import com.networknt.oauth.code.handler.Oauth2CodePostHandler;
import com.networknt.server.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.handlers.AuthenticationCallHandler;
import io.undertow.security.handlers.AuthenticationConstraintHandler;
import io.undertow.security.handlers.AuthenticationMechanismsHandler;
import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.impl.BasicAuthenticationMechanism;
import io.undertow.security.impl.CachedAuthenticatedSessionMechanism;
import io.undertow.security.impl.FormAuthenticationMechanism;
import io.undertow.server.HttpHandler;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.util.Methods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PathHandlerProvider implements HandlerProvider {
    @Override
    public HttpHandler getHandler() {
        IMap<String, User> users = CacheStartupHookProvider.hz.getMap("users");
        final IdentityManager identityManager = new MapIdentityManager(users);

        HttpHandler handler = Handlers.routing()
            .add(Methods.GET, "/health", new HealthGetHandler())
            .add(Methods.GET, "/server/info", new ServerInfoGetHandler())
            .add(Methods.GET, "/oauth2/code", addBasicSecurity(new Oauth2CodeGetHandler(), identityManager))
            .add(Methods.POST, "/oauth2/code", addFormSecurity(new Oauth2CodePostHandler(), identityManager))
        ;
        return handler;
    }

    private static HttpHandler addBasicSecurity(final HttpHandler toWrap, final IdentityManager identityManager) {
        HttpHandler handler = toWrap;
        handler = new AuthenticationCallHandler(handler);
        handler = new AuthenticationConstraintHandler(handler);
        final List<AuthenticationMechanism> mechanisms = Collections.singletonList(new BasicAuthenticationMechanism("oauth2"));
        handler = new AuthenticationMechanismsHandler(handler, mechanisms);
        handler = new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, identityManager, handler);
        return handler;
    }

    private static HttpHandler addFormSecurity(final HttpHandler toWrap, final IdentityManager identityManager) {
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
}
