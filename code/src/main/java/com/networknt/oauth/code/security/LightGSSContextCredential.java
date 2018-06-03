/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.networknt.oauth.code.security;

import io.undertow.security.idm.Credential;
import org.ietf.jgss.GSSContext;

/**
 * A {@link Credential} to wrap an established GSSContext.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class LightGSSContextCredential implements Credential {

    private final GSSContext gssContext;
    private final String clientAuthClass;
    private final String userType;

    public LightGSSContextCredential(final GSSContext gssContext, final String clientAuthClass, final String userType) {
        this.gssContext = gssContext;
        this.clientAuthClass = clientAuthClass;
        this.userType = userType;
    }

    public GSSContext getGssContext() {
        return gssContext;
    }

    public String getClientAuthClass() { return clientAuthClass; }

    public String getUserType() { return userType; }
}
