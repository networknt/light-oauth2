package com.networknt.oauth.code.auth;

import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;

public interface Authenticator<T> {
    /**
     * Authenticate user with the credential and userType. Return null if failed to authenticate.
     * Otherwise, return an account object that represent the user profile.
     */
    Account authenticate(String id, Credential credential);
}
