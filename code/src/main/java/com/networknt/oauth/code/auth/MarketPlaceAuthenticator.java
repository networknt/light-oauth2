package com.networknt.oauth.code.auth;

import com.networknt.oauth.code.security.LightPasswordCredential;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;

/**
 * This is the market place authentication class. It should be defined in the authenticate_class
 * column in client table when register the market place application so that this class will be
 * invoked when a user is login from market place.
 *
 * The current implementation for market place will use LDAP to do authentication and market place
 * roles configuration file for authorization. In the future, the authentication should be moved to
 * the SPNEGO/Kerberos and authorization will be from database.
 *
 * @author Steve Hu
 */
public class MarketPlaceAuthenticator extends AuthenticatorBase<MarketPlaceAuth> {
    private static final String USER_TYPE_EMPLOYEE = "employee";
    private static final String USER_TYPE_PUBLIC = "public";

    @Override
    public Account authenticate(String id, Credential credential) {
        LightPasswordCredential passwordCredential = (LightPasswordCredential) credential;
        char[] password = passwordCredential.getPassword();
        String clientAuthClass = passwordCredential.getClientAuthClass();
        String userType = passwordCredential.getUserType();
        if(USER_TYPE_EMPLOYEE.equals(userType)) {
            // LDAP authentication and database authorization

        } else {
            // Database authentication and authorization

        }
        return null;
    }


}
