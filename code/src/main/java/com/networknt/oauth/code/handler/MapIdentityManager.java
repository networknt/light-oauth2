package com.networknt.oauth.code.handler;

import com.hazelcast.core.IMap;
import com.networknt.oauth.cache.model.User;
import com.networknt.oauth.code.security.LightGSSContextCredential;
import com.networknt.oauth.code.security.LightPasswordCredential;
import com.networknt.utility.HashUtil;
import io.undertow.security.idm.*;
import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by steve on 21/09/16.
 */
public class MapIdentityManager implements IdentityManager {
    final Logger logger = LoggerFactory.getLogger(MapIdentityManager.class);
    private final IMap<String, User> users;
    public MapIdentityManager(final IMap<String, User> users) {
        this.users = users;
    }

    @Override
    public Account verify(Account account) {
        // An existing account so for testing assume still valid.
        return account;
    }
    @Override
    public Account verify(String id, Credential credential) {
        Account account = getAccount(id);
        if (account != null && verifyCredential(account, credential)) {
            return account;
        }

        return null;
    }
    @Override
    public Account verify(Credential credential) {
        if (credential instanceof LightGSSContextCredential) {
            try {
                final LightGSSContextCredential gssCredential = (LightGSSContextCredential) credential;
                final String name = gssCredential.getGssContext().getSrcName().toString();
                final String clientAuthClass = gssCredential.getClientAuthClass();
                final String userType = gssCredential.getUserType();

                return new Account() {

                    private final Principal principal = new Principal() {

                        @Override
                        public String getName() {
                            return name;
                        }
                    };

                    @Override
                    public Principal getPrincipal() {
                        return principal;
                    }

                    // TODO add authorization roles here from LDAP
                    @Override
                    public Set<String> getRoles() {
                        // TODO based on clientAuthClass and userType to make decision to populate roles
                        return Collections.emptySet();
                    }
                };

            } catch (GSSException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private boolean verifyCredential(Account account, Credential credential) {
        boolean match = false;
        if (credential instanceof LightPasswordCredential) {
            LightPasswordCredential passwordCredential = (LightPasswordCredential)credential;

            char[] password = passwordCredential.getPassword();
            String clientAuthClass = passwordCredential.getClientAuthClass();
            String userType = passwordCredential.getUserType();

            User user = users.get(account.getPrincipal().getName());
            String expectedPassword = user.getPassword();
            try {
                match = HashUtil.validatePassword(password, expectedPassword);
                Arrays.fill(password, ' ');
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                logger.error("Exception:", e);
            }
        }
        if(logger.isDebugEnabled()) logger.debug("verfifyCredential = " + match);
        return match;
    }

    private Account getAccount(final String id) {
        if (users.containsKey(id)) {
            return new Account() {

                private final Principal principal = () -> id;
                @Override
                public Principal getPrincipal() {
                    return principal;
                }
                @Override
                public Set<String> getRoles() {
                    return Collections.emptySet();
                }

            };
        }
        return null;
    }

    //
}
