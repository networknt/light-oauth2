package com.networknt.oauth.handler;

import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Created by steve on 21/09/16.
 */
public class MapIdentityManager implements IdentityManager {

    private final Map<String, Object> users;

    MapIdentityManager(final Map<String, Object> users) {
        this.users = users;
    }

    public Account verify(Account account) {
        // An existing account so for testing assume still valid.
        return account;
    }

    public Account verify(String id, Credential credential) {
        Account account = getAccount(id);
        if (account != null && verifyCredential(account, credential)) {
            return account;
        }

        return null;
    }

    public Account verify(Credential credential) {
        // TODO Auto-generated method stub
        return null;
    }

    private boolean verifyCredential(Account account, Credential credential) {
        if (credential instanceof PasswordCredential) {
            char[] password = ((PasswordCredential) credential).getPassword();
            Map<String, Object> map = (Map<String, Object>)users.get(account.getPrincipal().getName());
            char[] expectedPassword = ((String)map.get("password")).toCharArray();
            return Arrays.equals(password, expectedPassword);
        }
        return false;
    }

    private Account getAccount(final String id) {
        if (users.containsKey(id)) {
            return new Account() {

                private final Principal principal = new Principal() {

                    public String getName() {
                        return id;
                    }
                };

                public Principal getPrincipal() {
                    return principal;
                }

                public Set<String> getRoles() {
                    return Collections.emptySet();
                }

            };
        }
        return null;
    }
}
