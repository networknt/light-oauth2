package com.networknt.oauth.code.handler;

import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;
public class LdapAuthTest {
    @ClassRule
    public static TestServer server = TestServer.getInstance();
    static final Logger logger = LoggerFactory.getLogger(LdapAuthTest.class);

    private final static String ldapURI = "ldaps://localhost:10636/ou=users,dc=undertow,dc=io";
    private final static String contextFactory = "com.sun.jndi.ldap.LdapCtxFactory";

    private static DirContext ldapContext () throws Exception {
        Hashtable<String,String> env = new Hashtable <String,String>();
        return ldapContext(env);
    }

    private static DirContext ldapContext (Hashtable <String,String>env) throws Exception {
        env.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
        env.put(Context.PROVIDER_URL, ldapURI);
        if(ldapURI.toUpperCase().startsWith("LDAPS://")) {
            env.put(Context.SECURITY_PROTOCOL, "ssl");
            env.put("java.naming.ldap.factory.socket", "com.networknt.oauth.code.handler.LdapSSLSocketFactory");
        }
        env.put(Context.SECURITY_PRINCIPAL, "uid=oauth,ou=users,dc=undertow,dc=io");
        env.put(Context.SECURITY_CREDENTIALS, "theoauth");

        DirContext ctx = new InitialDirContext(env);
        return ctx;
    }

    private static String getUid (String user) throws Exception {
        DirContext ctx = ldapContext();

        String filter = "(uid=" + user + ")";
        SearchControls ctrl = new SearchControls();
        ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration answer = ctx.search("", filter, ctrl);

        String dn;
        if (answer.hasMore()) {
            SearchResult result = (SearchResult) answer.next();
            dn = result.getNameInNamespace();
        }
        else {
            dn = null;
        }
        answer.close();
        return dn;
    }

    private static boolean testBind (String dn, String password) throws Exception {
        Hashtable<String,String> env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
        env.put(Context.PROVIDER_URL, ldapURI);
        if(ldapURI.toUpperCase().startsWith("LDAPS://")) {
            env.put(Context.SECURITY_PROTOCOL, "ssl");
            env.put("java.naming.ldap.factory.socket", "com.networknt.oauth.code.handler.LdapSSLSocketFactory");
        }
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, dn);
        env.put(Context.SECURITY_CREDENTIALS, password);

        try {
            DirContext ctx = new InitialDirContext(env);

        }
        catch (javax.naming.AuthenticationException e) {
            return false;
        }
        return true;
    }

    @Test
    public void testAuthentication() throws Exception {
        String user = "jduke";
        String password = "theduke";
        String dn = getUid( user );

        if (dn != null) {
            /* Found user - test password */
            if ( testBind( dn, password ) ) {
                System.out.println( "user '" + user + "' authentication succeeded" );
            }
            else {
                System.out.println( "user '" + user + "' authentication failed" );
                System.exit(1);
            }
        }
        else {
            System.out.println( "user '" + user + "' not found" );
            System.exit(1);
        }
    }
}
