package com.networknt.oauth.code.auth;

import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;

public class DefaultAuthenticator extends AuthenticatorBase<DefaultAuth> {

    @Override
    public Account authenticate(String id, Credential credential) {

        return null;
    }

}
