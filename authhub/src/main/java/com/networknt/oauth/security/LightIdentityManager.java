package com.networknt.oauth.security;

import com.networknt.oauth.auth.Authenticator;
import com.networknt.oauth.auth.DefaultAuth;
import com.networknt.service.SingletonServiceFactory;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use the LDAP for authentication and authorization. For get request, a popup window will show up if
 * the user is not logged in yet. Otherwise, the authorization code will be redirected automatically.
 *
 * If post endpoint is used, a customized form should be provided by a single page app or integrated
 * into existing application.
 *
 * The credentials will be passed to the endpoint with basic authentication protocol.
 *
 * Given the nature of this service only be called once per day with SSO for the browser applications,
 * it doesn't make any sense to have cache enabled. All authentication will go to LDAP server directly.
 *
 * The current implementation is based on an assumption that only one partition is used on the LDAP/AD
 * if there are multiple partitions, we need to resolve the username to a directory entry attribute by
 * a searchFilter.
 *
 * @author Steve Hu
 */
public class LightIdentityManager implements IdentityManager {
    final Logger logger = LoggerFactory.getLogger(LightIdentityManager.class);

    @Override
    public Account verify(Account account) {
        // An existing account so for testing assume still valid.
        return account;
    }
    @Override
    public Account verify(String id, Credential credential) {
        if (credential instanceof LightPasswordCredential) {
            LightPasswordCredential passwordCredential = (LightPasswordCredential) credential;
            String clientAuthClass = passwordCredential.getClientAuthClass();
            // get authenticator object.
            Class clazz = DefaultAuth.class;
            if(clientAuthClass != null && clientAuthClass.trim().length() > 0) {
                try {
                    clazz = Class.forName(clientAuthClass);
                } catch (ClassNotFoundException e) {
                    logger.error("Authenticate Class " + clientAuthClass + " not found.", e);
                    return null;
                }
            }
            Authenticator authenticator = SingletonServiceFactory.getBean(Authenticator.class, clazz);
            return authenticator.authenticate(id, credential);
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
                // get authenticator object.
                Class clazz = DefaultAuth.class;
                if(clientAuthClass != null && clientAuthClass.trim().length() > 0) {
                    try {
                        clazz = Class.forName(clientAuthClass);
                    } catch (ClassNotFoundException e) {
                        logger.error("Authenticate Class " + clientAuthClass + " not found.", e);
                        return null;
                    }
                }
                Authenticator authenticator = SingletonServiceFactory.getBean(Authenticator.class, clazz);
                return authenticator.authenticate(name, credential);
            } catch (GSSException e) {
                logger.error("GSSException:", e);
                return null;
            }
        }
        return null;
    }
}
