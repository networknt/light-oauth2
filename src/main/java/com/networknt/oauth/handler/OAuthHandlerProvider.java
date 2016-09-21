package com.networknt.oauth.handler;

import com.networknt.config.Config;
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
import io.undertow.server.HttpHandler;

import java.util.Collections;
import java.util.List;
import java.util.Map;


public class OAuthHandlerProvider implements HandlerProvider {

    public HttpHandler getHandler() {
        Map users = Config.getInstance().getJsonMapConfig("users");
        final IdentityManager identityManager = new MapIdentityManager(users);

        return Handlers.path().addPrefixPath("/oauth2/token",
                new TokenHandler(Config.getInstance().getMapper()))
                .addPrefixPath("/oauth2/code",
                        addSecurity(new CodeHandler(Config.getInstance().getMapper()), identityManager));
    }

    private static HttpHandler addSecurity(final HttpHandler toWrap, final IdentityManager identityManager) {
        HttpHandler handler = toWrap;
        handler = new AuthenticationCallHandler(handler);
        handler = new AuthenticationConstraintHandler(handler);
        final List<AuthenticationMechanism> mechanisms = Collections.<AuthenticationMechanism>singletonList(new BasicAuthenticationMechanism("My Realm"));
        handler = new AuthenticationMechanismsHandler(handler, mechanisms);
        handler = new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, identityManager, handler);
        return handler;
    }
}
