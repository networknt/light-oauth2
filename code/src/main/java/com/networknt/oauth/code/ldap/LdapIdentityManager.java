package com.networknt.oauth.code.ldap;

import com.networknt.config.Config;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.security.Principal;
import java.util.*;

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
public class LdapIdentityManager implements IdentityManager {
    private final static String contextFactory = "com.sun.jndi.ldap.LdapCtxFactory";
    private final static String AUTH_METHOD = "simple";
    private final static String LDAP_CREDENTIAL = "ladpCredential";
    private final static String CONFIG = "ldap";
    private final static String SECRET = "secret";
    private final static LdapConfig config = (LdapConfig)Config.getInstance().getJsonObjectConfig(CONFIG, LdapConfig.class);
    private final static Map<String, Object> secret = Config.getInstance().getJsonMapConfig(SECRET);
    // the secret map should be decrypted already at this time.
    private final static String ldapCredential = (String)secret.get(LDAP_CREDENTIAL);
    final Logger logger = LoggerFactory.getLogger(LdapIdentityManager.class);

    public void LdapIdentityManager() {
        logger.info("LdapIdentityManager is constructed");
    }

    /**
     * This method is not used in LDAP authentication
     *
     * @param account Account
     * @return
     */
    @Override
    public Account verify(Account account) {
        // An existing account so for testing assume still valid.
        return account;
    }

    /**
     * This is the real method that does the authentication and authorization.
     *
     * @param id user identifier
     * @param credential user password
     * @return
     */
    @Override
    public Account verify(String id, Credential credential) {
        if (credential instanceof PasswordCredential) {
            // first look up the user with oauth username and credential
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
            env.put(Context.PROVIDER_URL, config.getUri());
            env.put(Context.SECURITY_PRINCIPAL, config.ldapPrincipal);
            env.put(Context.SECURITY_CREDENTIALS, ldapCredential);
            DirContext ctx = null;
            String user = null;
            try {
                ctx = new InitialDirContext(env);
                // lookup user with id
                SearchControls ctrls = new SearchControls();
                ctrls.setReturningAttributes(new String[] { "givenName", "sn","memberOf" });
                ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);

                NamingEnumeration<javax.naming.directory.SearchResult> answers = ctx.search("o=undertow.io", "(uid=" + id + ")", ctrls);
                javax.naming.directory.SearchResult result = answers.nextElement();
                user = result.getNameInNamespace();
                if(logger.isDebugEnabled()) logger.debug("user lookup up result = " + user);
            } catch (NamingException e) {
                logger.error("Fail to log into LDAP with " + config.getLdapPrincipal(), e);
                return null;
            } finally {
                if(ctx != null) {
                    try {
                        ctx.close();
                    } catch (Exception e) {}
                }
            }

            char[] password = ((PasswordCredential) credential).getPassword();
            String dn = String.format(config.getDn(), id);
            env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
            env.put(Context.PROVIDER_URL, config.getUri());
            env.put(Context.SECURITY_AUTHENTICATION, AUTH_METHOD);
            env.put(Context.SECURITY_PRINCIPAL, dn);
            env.put(Context.SECURITY_CREDENTIALS, Arrays.toString(password));
            try {
                ctx = new InitialDirContext(env);
                // now get user group info to build account object.
                SearchControls ctrls = new SearchControls();
                ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                NamingEnumeration<SearchResult> results = ctx.search(toDC(config.getDomain()),"(& (userPrincipalName="+id+")(objectClass=user))", ctrls);
                if(!results.hasMore())
                    throw new AuthenticationException("Principal name not found");

                SearchResult result = results.next();
                System.out.println("distinguisedName: " + result.getNameInNamespace() ); // CN=Firstname Lastname,OU=Mycity,DC=mydomain,DC=com

                Attribute memberOf = result.getAttributes().get("memberOf");
                Set<String> roles = new HashSet<>();
                if(memberOf!=null) {
                    for(int idx=0; idx<memberOf.size(); idx++) {
                        if(logger.isDebugEnabled()) logger.debug("memberOf: " + memberOf.get(idx).toString() ); // CN=Mygroup,CN=Users,DC=mydomain,DC=com
                        //Attribute att = context.getAttributes(memberOf.get(idx).toString(), new String[]{"CN"}).get("CN");
                        //System.out.println( att.get().toString() ); //  CN part of groupname
                        roles.add(memberOf.get(idx).toString());
                    }
                }
                return new Account() {
                    private final Principal principal = () -> id;
                    @Override
                    public Principal getPrincipal() {
                        return principal;
                    }
                    @Override
                    public Set<String> getRoles() {
                        return roles;
                    }
                };
            } catch (NamingException e) {
                // binding is failed.
                logger.error("failed to authenticate dn = ", dn);
                return null;
            } finally {
                if(ctx != null) {
                    try {
                        ctx.close();
                    } catch (Exception e) {}
                }
            }
        }
        logger.error("verfifyCredential is failed due to wrong Credential type = " + credential.toString());
        return null;
    }

    @Override
    public Account verify(Credential credential) {
        return null;
    }

    /**
     * Create "DC=sub,DC=mydomain,DC=com" string
     * @param domainName    sub.mydomain.com
     * @return String as concat DC
     */
    private static String toDC(String domainName) {
        StringBuilder buf = new StringBuilder();
        for (String token : domainName.split("\\.")) {
            if(token.length()==0) continue;
            if(buf.length()>0)  buf.append(",");
            buf.append("DC=").append(token);
        }
        return buf.toString();
    }
}
