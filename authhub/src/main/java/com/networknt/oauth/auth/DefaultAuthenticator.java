package com.networknt.oauth.auth;

import com.hazelcast.map.IMap;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.User;
import com.networknt.oauth.ldap.LdapUtil;
import com.networknt.oauth.security.LightGSSContextCredential;
import com.networknt.oauth.security.LightPasswordCredential;
import com.networknt.utility.HashUtil;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DefaultAuthenticator extends AuthenticatorBase<DefaultAuth> {
    private static final Logger logger = LoggerFactory.getLogger(DefaultAuthenticator.class);


    @Override
    public Account authenticate(String id, Credential credential) {
        IMap<String, User> users = CacheStartupHookProvider.hz.getMap("users");
        Account account = getAccount(id);
        if (credential instanceof LightPasswordCredential) {
            LightPasswordCredential passwordCredential = (LightPasswordCredential)credential;
            char[] password = passwordCredential.getPassword();
            String clientAuthClass = passwordCredential.getClientAuthClass();
            String userType = passwordCredential.getUserType();

            User user = users.get(account.getPrincipal().getName());
            String expectedPassword = user.getPassword();
            boolean match = false;
            try {
                match = HashUtil.validatePassword(password, expectedPassword);
                Arrays.fill(password, ' ');
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                logger.error("Exception:", e);
                return null;
            }
            if(!match) return null;
        } else if(credential instanceof LightGSSContextCredential) {
            return new Account() {
                private Set<String> roles = LdapUtil.authorize(id);
                private final Principal principal = () -> id;
                @Override
                public Principal getPrincipal() {
                    return principal;
                }
                @Override
                public Set<String> getRoles() { return roles; }
            };
        }
        return account;
    }

    private Account getAccount(final String id) {
        IMap<String, User> users = CacheStartupHookProvider.hz.getMap("users");
        if (users.containsKey(id)) {
            return new Account() {
                private Set<String> roles = parseRoles(users.get(id).getRoles());
                private final Principal principal = () -> id;
                @Override
                public Principal getPrincipal() {
                    return principal;
                }
                @Override
                public Set<String> getRoles() { return roles; }
            };
        }
        return null;
    }

    public Set<String> parseRoles(String roles) {
        Set<String> set = Collections.EMPTY_SET;
        if(roles != null) {
            // remove the leading and trailing spaces.
            roles = roles.trim();
            if(roles.contains(" ")) {
                // multiple roles in a format separated by " ".
                set = new HashSet<>(Arrays.asList(roles.split("\\s+")));
            } else {
                // only one role is available
                set = new HashSet<>();
                set.add(roles);
            }
        }
        return set;
    }
}
