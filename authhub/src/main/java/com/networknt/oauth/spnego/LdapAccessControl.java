/**
 * Copyright (C) 2009 "Darwin V. Felix" <darwinfelix@users.sourceforge.net>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.networknt.oauth.spnego;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

/**
 * The <code>LdapAccessControl</code> class is a reference implementation 
 * of the {@link UserAccessControl} interface. This class only performs 
 * user authorization (authZ) and not user authentication (authN). This 
 * class implements an authZ model in a similar style as the 
 * <a href="https://en.wikipedia.org/wiki/Role-based_access_control">Role 
 * Based Access Control</a> (RBAC) model and the  
 * <a href="https://en.wikipedia.org/wiki/Attribute-Based_Access_Control">Attribute 
 * Based Access Control</a> (ABAC) model. However, this pedagogical implementation 
 * is much simpler and limited than the two formal models.
 *
 * <p>
 * Usage examples and semantics can be found in the javadoc of the interface 
 * that this class implements ({@link UserAccessControl}).
 * </p>
 *
 * <p>
 * The <code>LdapAccessControl</code> implementation makes calls to an 
 * LDAP server (e.g. Microsoft's Active Directory (AD) server) when performing 
 * a lookup to determine if a user has one or more attributes defined. The 
 * LDAP queries are based on LDAP search/fiter criteria(s) defined at the time  
 * an instance of this class is placed into service. An instance of this class 
 * is considered to be in service after invoking the <code>init</code> method. 
 * </p>
 *
 * <p>
 * In the SPNEGO Library, the <code>SpnegoHttpFilter</code> class is the mechanism 
 * that performs user authentication (authN) whilst the <code>LdapAccessControl</code> 
 * class is the default mechanism that performs user authorization (authZ). This 
 * default can be replaced by any class that implement the <code>UserAccessControl</code> 
 * interface. To change the default, specify the new class in the SPNEGO Library's 
 * filter definition section of the web.xml file.
 * </p>
 *
 * <p>
 * Authorization (authZ) is an optional feature of the SPNEGO Library. The SPNEGO 
 * Library provides an interface, {@link UserAccessControl}, to applications
 * that need authZ capability. Applications can check a user's authZ by calling 
 * methods defined in the <code>SpnegoAccessControl</code> interface.
 * </p>
 *
 * <p>
 * The <code>LdapAccessControl</code> class is configured within the same web.xml 
 * filter section as the <code>SpnegoHttpFilter</code> class. The configuration is 
 * specified by adding additional filter parameters to the <code>SpnegoHttpFilter</code> 
 * filter definition.
 * </p>
 *
 * <p>
 * <b>Example web.xml Configuration:</b>
 * <pre>
 * &lt;filter&gt;
 *     &lt;filter-name&gt;SpnegoHttpFilter&lt;/filter-name&gt;
 *     &lt;filter-class&gt;net.sourceforge.spnego.SpnegoHttpFilter&lt;/filter-class&gt;
 *
 *     &lt;!-- spnego http filter params (authN) --&gt;
 *     ... existing authN params here just as before ...
 *
 *     &lt;!-- spnego http filter params (authZ) --&gt;     
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;spnego.authz.class&lt;/param-name&gt;
 *         &lt;param-value&gt;net.sourceforge.spnego.LdapAccessControl&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;spnego.authz.ldap.url&lt;/param-name&gt;
 *         &lt;param-value&gt;ldap://athena.local:389&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *
 *     &lt;!-- an example user-defined resource label --&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;spnego.authz.resource.name.1&lt;/param-name&gt;
 *         &lt;param-value&gt;admin-buttons&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;spnego.authz.resource.access.1&lt;/param-name&gt;
 *         &lt;param-value&gt;Biz. Analyst,Los Angeles,IT Group&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;spnego.authz.resource.type.1&lt;/param-name&gt;
 *         &lt;param-value&gt;has&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *
 *     &lt;!-- CDATA required since specifying filter(s) in web.xml (vs. a policy file) --&gt;
 *     &lt;!-- also notice the %1$s and the %2$s tokens (always required) --&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;spnego.authz.ldap.filter.1&lt;/param-name&gt;
 *          &lt;param-value&gt;&lt;![CDATA[(&amp;(sAMAccountName=%1$s)(memberOf:1.2.840.113556.1.4.1941:=CN=%2$s,OU=Groups,OU=Los Angeles,DC=athena,DC=local))]]&gt;&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;spnego.authz.ldap.filter.2&lt;/param-name&gt;
 *         &lt;param-value&gt;&lt;![CDATA[(&amp;(sAMAccountType=805306368)(sAMAccountName=%1$s)(&amp;(sAMAccountType=805306368)(department=%2$s)))]]&gt;&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 * &lt;/filter&gt;
 * </pre>
 *
 * <p>
 * As an alternative option, the <code>spnego.authz.ldap.filter.[i]</code> parameters and 
 * the <code>spnego.authz.resource.[name|access|type].[i]</code> parameters may be specified 
 * in a policy file.
 * </p>
 *
 * <p>
 * <b>Example Policy File Configuration:</b>
 * <pre>
 * &lt;filter&gt;
 *     &lt;filter-name&gt;SpnegoHttpFilter&lt;/filter-name&gt;
 *     &lt;filter-class&gt;net.sourceforge.spnego.SpnegoHttpFilter&lt;/filter-class&gt;
 *
 *     &lt;!-- spnego http filter params (authN) --&gt;
 *     ... existing authN params here just as before ...
 *
 *     &lt;!-- spnego http filter params (authZ) --&gt;     
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;spnego.authz.class&lt;/param-name&gt;
 *         &lt;param-value&gt;net.sourceforge.spnego.LdapAccessControl&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;spnego.authz.ldap.url&lt;/param-name&gt;
 *         &lt;param-value&gt;ldap://athena.local:389&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;spnego.authz.policy.file&lt;/param-name&gt;
 *         &lt;param-value&gt;C:/Apache Software Foundation/Tomcat 7.0/conf/spnego.policy&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 * &lt;/filter&gt;
 * </pre>
 *
 * Policy file contents:
 * <pre>
 * # an example user-defined resource label
 * spnego.authz.resource.name.1=admin-buttons
 * spnego.authz.resource.access.1=Biz. Analyst,Los Angeles,IT Group
 * spnego.authz.resource.type.1=has
 *
 * # do NOT use CDATA like in the web.xml file
 * # the %1$s and the %2$s tokens are always required
 * spnego.authz.ldap.filter.1=(&amp;(sAMAccountName=%1$s)(memberOf:1.2.840.113556.1.4.1941:=CN=%2$s,OU=Groups,OU=Los Angeles,DC=athena,DC=local))
 * spnego.authz.ldap.filter.2=(&amp;(sAMAccountType=805306368)(sAMAccountName=%1$s)(&amp;(sAMAccountType=805306368)(department=%2$s)))
 * </pre>
 *
 * <p>
 * For more information on how a web application/service can leverage access controls  
 * or to view some usage examples, please read the javadoc of the {@link UserAccessControl} interface.
 * </p>
 *
 * <p>
 * Also, take a look at the <a href="http://spnego.sourceforge.net/reference_docs.html" 
 * target="_blank">reference docs</a> for a complete list of configuration parameters.
 * </p>
 *
 * <p>
 * Finally, to see a working example and instructions, take a look at the 
 * <a href="http://spnego.sourceforge.net/user_access_control.html" 
 * target="_blank">authZ for standalone apps</a> example and the 
 * <a href="http://spnego.sourceforge.net/enable_authZ_ldap.html" 
 * target="_blank">enable authZ with LDAP</a> guide. 
 * </p>
 *
 *
 * @author Darwin V. Felix
 *
 */
public class LdapAccessControl implements UserAccessControl {

    private static final Logger LOGGER =
            Logger.getLogger(LdapAccessControl.class.getName());

    private static final String POLICY_FILE = "spnego.authz.policy.file";

    private static final String SERVER_REALM = "spnego.server.realm";

    private static final String LDAP_FACTORY = "spnego.authz.ldap.factory";

    private static final String LDAP_AUTHN = "spnego.authz.ldap.authn";

    private static final String LDAP_POOL = "spnego.authz.ldap.pool";

    private static final String LDAP_DEECE = "spnego.authz.ldap.deecee";

    private static final String LDAP_URL = "spnego.authz.ldap.url";

    private static final String LDAP_USERNAME = "spnego.authz.ldap.username";

    private static final String KRB5_USERNAME = "spnego.preauth.username";

    private static final String LDAP_PASSWORD = "spnego.authz.ldap.password";

    private static final String KRB5_PASSWORD = "spnego.preauth.password";

    private static final String TTL = "spnego.authz.ttl";

    private static final String UNIQUE = "spnego.authz.unique";

    private static final String PREFIX_FILTER = "spnego.authz.ldap.filter.";

    private static final String PREFIX_NAME = "spnego.authz.resource.name.";

    private static final String PREFIX_TYPE = "spnego.authz.resource.type.";

    private static final String PREFIX_ACCESS = "spnego.authz.resource.access.";

    private static final String HAS = "has";

    private static final String ANY = "any";

    /** case-sensitive. e.g. values mail,department,name,memberOf, etc. */
    private static final String USER_INFO = "spnego.authz.user.info";

    /** e.g. (&(sAMAccountType=805306368)(sAMAccountName=%1$s)) */
    private static final String USER_INFO_FILTER = "spnego.authz.ldap.user.filter";

    /** default is 20 minutes. */
    private static final long DEFAULT_TTL = 20 * 60 * 1000;

    /** maximum number of ldap filters is 200 */
    private static final int MAX_NUM_FILTERS = 200;

    /** read lock for reading instance variables and write lock for ldap search. */
    private final transient ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final transient Lock readLock = readWriteLock.readLock();
    private final transient Lock writeLock = readWriteLock.writeLock();

    /** cache LDAP results to minimize trips to ldap server. */
    private final transient Map<String, Long> matchedList = new HashMap<String, Long>();
    private final transient Map<String, Long> unMatchedList = new HashMap<String, Long>();
    private final transient Map<String, UserInfo> userInfoList = new HashMap<String, UserInfo>();

    private transient Hashtable<String, String> environment;
    private transient SearchControls srchCntrls;

    /** DC= base portionS of the ldap search filter. */
    private transient String deecee = "";

    /** ldap search filter(s). */
    private transient Set<String> policy = new HashSet<String>();

    /** determines how long to keep in cache. */
    private transient long expiration = DEFAULT_TTL;

    /** determines if an exception should be thrown if it finds a duplicate. */
    private transient boolean uniqueOnly = true;

    /** access resources by using a user-defined label. */
    private transient Map<String, Map<String, String[]>> resources =
            new HashMap<String, Map<String, String[]>>();

    private transient List<String> userInfoLabels = new ArrayList<String>();

    private transient String userInfoFilter;

    /**
     * Default constructor.
     */
    public LdapAccessControl() {
        // default constructor
    }

    @Override
    public void destroy() {
        LOGGER.info("destroy()...");
        this.writeLock.lock();
        try {
            this.matchedList.clear();
            this.unMatchedList.clear();
            this.environment.clear();
            this.environment = null;
            this.srchCntrls = null;
            this.deecee = "";
            this.policy.clear();
            this.expiration = DEFAULT_TTL;
            this.resources.clear();
            this.userInfoLabels.clear();
            this.userInfoFilter = null;
        } finally {
            this.writeLock.unlock();
        }
    }

    /*
     * (non-Javadoc)
     * @see net.sourceforge.spnego.UserAccessControl#anyRole(java.lang.String, java.lang.String[])
     */
    @Override
    public boolean anyRole(final String username, final String... attributes) {
        for (String role : attributes) {
            if (hasRole(username, role)) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see net.sourceforge.spnego.UserAccessControl#hasRole(java.lang.String, java.lang.String)
     */
    @Override
    public boolean hasRole(final String username, final String attribute) {
        final String key = username + "_attr_" + attribute;
        final long now = System.currentTimeMillis();

        try {
            if (!matchedExpired(key, now)) {
                return true;
            }

            if (!unMatchedExpired(key, now)) {
                return false;
            }

            // query AD to update both MapS and expiration time
            LOGGER.fine("username: " + username + "; role: " + attribute);

            this.writeLock.lock();
            try {
                // remove from cache if exists
                this.matchedList.remove(key);
                this.unMatchedList.remove(key);

                int count = 0;
                final LdapContext context = new InitialLdapContext(environment, null);
                for (String filter : this.policy) {
                    // perform AD lookup add to cache 
                    final NamingEnumeration<SearchResult> results =
                            context.search(this.deecee
                                    , String.format(filter, username, attribute)
                                    , this.srchCntrls);

                    final boolean found = results.hasMoreElements();
                    results.close();

                    // add to cache
                    if (found) {
                        count++;
                        //LOGGER.info("add attribute to matchedList: " + attribute);
                        this.matchedList.put(key, System.currentTimeMillis());
                        if (!this.uniqueOnly) {
                            break;
                        }
                    }

                    // check if we have a duplicate attribute
                    if (count > 1 && this.uniqueOnly) {
                        this.matchedList.remove(key);
                        throw new IllegalArgumentException("Uniqueness property violated. "
                                + "Found duplicate role/attribute:" + attribute
                                + ". This MAY be caused by an improper policy definition"
                                + "; filter=" + filter
                                + "; policy=" + this.policy);
                    }
                }
                context.close();

                if (0 == count) {
                    //LOGGER.info("add attribute to unMatchedList: " + attribute);
                    this.unMatchedList.put(key, System.currentTimeMillis());
                } else {
                    cacheUserInfo(username);
                }

            } finally {
                this.writeLock.unlock();
            }
        } catch (NamingException lex) {
            LOGGER.severe(lex.getMessage());
            throw new RuntimeException(lex);
        }

        return hasRole(username, attribute);
    }

    /*
     * (non-Javadoc)
     * @see net.sourceforge.spnego.UserAccessControl#hasRole(java.lang.String, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean hasRole(final String username, final String attributeX, final String... attributeYs) {

        // assert
        if (null == attributeYs || 0 == attributeYs.length) {
            final String errorMsg = "Must provide at least two parameters";
            LOGGER.severe(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        boolean found = false;
        final boolean featX = hasRole(username, attributeX);

        for (String featY : attributeYs) {
            found = featX && hasRole(username, featY);
            if (found) {
                break;
            }
        }

        return found;
    }

    /*
     * (non-Javadoc)
     * @see net.sourceforge.spnego.UserAccessControl#anyAccess(java.lang.String, java.lang.String[])
     */
    @Override
    public boolean anyAccess(final String username, final String... resources) {
        for (String resource : resources) {
            if (hasAccess(username, resource)) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see net.sourceforge.spnego.UserAccessControl#hasAccess(java.lang.String, java.lang.String)
     */
    @Override
    public boolean hasAccess(final String username, final String resource) {
        final String key = username + "_res_" + resource;
        final long now = System.currentTimeMillis();

        if (!matchedExpired(key, now)) {
            return true;
        }

        if (!unMatchedExpired(key, now)) {
            return false;
        }

        // query AD to update both MapS and expiration time
        LOGGER.fine("username: " + username + "; resource: " + resource);

        boolean matched = false;
        boolean containsHas = false;
        boolean containsAny = false;
        String[] attributes = new String[] {};

        this.readLock.lock();
        try {
            // assert
            if (!this.resources.containsKey(resource)) {
                throw new IllegalArgumentException(
                        "Policy not found for user-defined Resource labeled: " + resource);
            }

            containsHas = this.resources.get(resource).containsKey(HAS);
            containsAny = this.resources.get(resource).containsKey(ANY);
            if (containsHas) {
                attributes = this.resources.get(resource).get(HAS);
            } else if (containsAny) {
                attributes = this.resources.get(resource).get(ANY);
            }
        } finally {
            this.readLock.unlock();
        }

        if (containsHas) {
            if (attributes.length > 1) {
                matched = this.hasRole(username, attributes[0]
                        , Arrays.copyOfRange(attributes, 1, attributes.length));
            } else if (attributes.length == 1) {
                matched = this.hasRole(username, attributes[0]);
            } else {
                throw new IllegalStateException("No attribute(s) defined for resource: " + resource);
            }
        } else if (containsAny) {
            matched = this.anyRole(username, attributes);
        } else {
            throw new UnsupportedOperationException("Allowed resource.type(s): [any|has]");
        }

        this.writeLock.lock();
        try {
            if (matched) {
                //LOGGER.info("add resource to matchedList: " + resource);
                this.matchedList.put(key, now);
            } else {
                //LOGGER.info("add resource to unMatchedList: " + resource);
                this.unMatchedList.put(key, now);
            }
        } finally {
            this.writeLock.unlock();
        }

        return matched;
    }

    /*
     * (non-Javadoc)
     * @see net.sourceforge.spnego.UserAccessControl#hasAccess(java.lang.String, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean hasAccess(final String username, final String resourceX, final String... resourceYs) {

        // assert
        if (null == resourceYs || 0 == resourceYs.length) {
            final String errorMsg = "Must provide at least two parameters";
            LOGGER.severe(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        boolean found = false;
        final boolean resX = hasAccess(username, resourceX);

        for (String resY : resourceYs) {
            found = resX && hasAccess(username, resY);
            if (found) {
                break;
            }
        }

        return found;
    }

    /**
     * Returns a user info object if specified in  web.xml or the spnego.policy file.
     *
     * <p>Case-sensitive</p>
     *
     * <p>
     * <b>web.xml Example:</b>
     * <pre>
     *     ...
     *     &lt;init-param&gt;
     *         &lt;param-name&gt;spnego.authz.user.info&lt;/param-name&gt;
     *         &lt;param-value&gt;mail,department,memberOf,displayName&lt;/param-value&gt;
     *     &lt;/init-param&gt;
     *     &lt;init-param&gt;
     *         &lt;param-name&gt;spnego.authz.ldap.user.filter&lt;/param-name&gt;
     *         &lt;param-value&gt;&lt;![CDATA[(&amp;(sAMAccountType=805306368)(sAMAccountName=%1$s))]]&gt;&lt;/param-value&gt;
     *     &lt;/init-param&gt;
     *     ...
     * </pre>
     *
     * <b>spnego.policy File Example:</b>
     * <pre>
     * ...
     * # case-sensitive
     * spnego.authz.user.info=mail,department,memberOf,displayName
     * spnego.authz.ldap.user.filter=(&amp;(sAMAccountType=805306368)(sAMAccountName=%1$s))
     * ...
     * </pre>
     *
     * @param username e.g. dfelix
     * @return UserInfo object with the specified ldap attributes
     */
    @Override
    public UserInfo getUserInfo(final String username) {
        final long now = System.currentTimeMillis();
        final boolean expired = matchedExpired(username, now);

        this.readLock.lock();
        try {
            if (!expired) {
                return this.userInfoList.get(username);
            }
        } finally {
            this.readLock.unlock();
        }

        this.writeLock.lock();
        try {
            return cacheUserInfo(username);
        } catch (NamingException nex) {
            final String errorMessage = "Could not get user info for: " + username;
            LOGGER.warning(errorMessage);
            throw new IllegalStateException(errorMessage, nex);
        } finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public void init(final Properties props) {
        LOGGER.info("init()...");

        this.readLock.lock();
        try {
            if (this.environment != null) {
                // must call the destroy method before re-initializing
                throw new IllegalStateException("LdapAccessControl already initialized");
            }
        } finally {
            this.readLock.unlock();
        }

        final String policyFile = props.getProperty(POLICY_FILE, "");
        final Properties policies = new Properties();
        if (!policyFile.isEmpty()) {
            try {
                LOGGER.info("policy file: " + policyFile);
                final FileInputStream fis =new FileInputStream(policyFile);
                try {
                    policies.load(fis);
                } finally {
                    fis.close();
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(
                        "Policy File NOT Found: " + policyFile, e);
            }
        }

        // use defaults if not specified
        final Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY
                , policies.getProperty(LDAP_FACTORY
                        , props.getProperty(LDAP_FACTORY
                                , "com.sun.jndi.ldap.LdapCtxFactory")));
        env.put(Context.SECURITY_AUTHENTICATION
                , policies.getProperty(LDAP_AUTHN
                        , props.getProperty(LDAP_AUTHN
                                , "Simple")));
        env.put("com.sun.jndi.ldap.connect.pool"
                , policies.getProperty(LDAP_POOL
                        , props.getProperty(LDAP_POOL
                                , "true")));

        // if deecee was not provided, calculate using server's realm
        String dc = policies.getProperty(LDAP_DEECE
                , props.getProperty(LDAP_DEECE, ""));
        if (dc.isEmpty()) {
            final String tmp = props.getProperty(SERVER_REALM
                    , policies.getProperty(SERVER_REALM, ""));
            if (tmp.trim().isEmpty()) {
                throw new IllegalArgumentException("MUST provide the serve's deecee. "
                        + " specify a value for the " + LDAP_DEECE + " property.");
            }
            dc = "DC=" + tmp.replaceAll("\\.", ",DC=");
        }
        LOGGER.info(dc);

        // assert that an ldap url was provided
        if (policies.getProperty(LDAP_URL, props.getProperty(LDAP_URL, "")).isEmpty()) {
            final String errorMessage = "Must provide a value for the spnego.authz.ldap.url parameter";
            LOGGER.severe(errorMessage);
            throw new IllegalStateException(errorMessage);
        } else {
            env.put(Context.PROVIDER_URL
                    , policies.getProperty(LDAP_URL, props.getProperty(LDAP_URL)));
            LOGGER.info("ldap provider url: " + env.get(Context.PROVIDER_URL));
        }

        // if username/password not provided, default to krb5 username/password
        // if nothing is specified because a keytab file was specified... error.
        if (policies.getProperty(LDAP_USERNAME, props.getProperty(LDAP_USERNAME
                , props.getProperty(KRB5_USERNAME, policies.getProperty(KRB5_USERNAME, "")))).isEmpty()) {
            final String errorMessage = "Must provide a username to use for connecting to the LDAP server";
            LOGGER.severe(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        if (policies.getProperty(LDAP_PASSWORD, props.getProperty(LDAP_PASSWORD
                , props.getProperty(KRB5_PASSWORD, policies.getProperty(KRB5_PASSWORD, "")))).isEmpty()) {
            final String errorMessage = "Must provide a password to use for connecting to the LDAP server";
            LOGGER.severe(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        env.put(Context.SECURITY_PRINCIPAL
                , policies.getProperty(LDAP_USERNAME, props.getProperty(LDAP_USERNAME
                        , props.getProperty(KRB5_USERNAME, policies.getProperty(KRB5_USERNAME)))));
        env.put(Context.SECURITY_CREDENTIALS
                , policies.getProperty(LDAP_PASSWORD, props.getProperty(LDAP_PASSWORD
                        , props.getProperty(KRB5_PASSWORD, policies.getProperty(KRB5_PASSWORD)))));
        LOGGER.info("ldap security principal: " + env.get(Context.SECURITY_PRINCIPAL));

        // specifiy how many minutes the cache is good for
        // used to control when to re-query/perform another ldap search
        long ttl = DEFAULT_TTL;
        ttl = Long.parseLong(policies.getProperty(TTL, props.getProperty(TTL, "-1")));
        LOGGER.info("spnego.authz.ttl: " + ttl);

        // determine if we're allowed to violate the uniqueness property
        this.uniqueOnly = Boolean.parseBoolean(policies.getProperty(UNIQUE
                , props.getProperty(UNIQUE, "true")));

        LOGGER.info("uniqueness property enabled: " + uniqueOnly);

        this.writeLock.lock();
        try {
            this.deecee = dc;

            // create policy statements
            loadPolicies(policies, props);

            // optional labels for resources
            loadResourceNames(policies, props);

            if (ttl < 1) {
                this.expiration = DEFAULT_TTL;
            } else {
                this.expiration = ttl * 60 * 1000;
            }
            LOGGER.info("cache expiration in millis: " + this.expiration);

            this.srchCntrls = new SearchControls();
            this.srchCntrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            this.environment = env;

            final String[] labels = policies.getProperty(USER_INFO
                    , props.getProperty(USER_INFO, "")).split(",");

            LOGGER.info("UserInfo label count: " + labels.length);
            for (String label : labels) {
                LOGGER.info(label);
                this.userInfoLabels.add(label.trim());
            }

            this.userInfoFilter = policies.getProperty(USER_INFO_FILTER
                    , props.getProperty(USER_INFO_FILTER, ""));

            LOGGER.info("UserInfo filter: " + this.userInfoFilter);

        } finally {
            this.writeLock.unlock();
        }
    }

    private boolean matchedExpired(final String key, final long now) {
        final boolean matched = this.matchedList.containsKey(key);
        boolean matchExpired = true;

        this.readLock.lock();
        try {
            // if has role, check if not expired
            if (matched) {
                matchExpired = now - this.matchedList.get(key) > expiration;
            }
            return !(matched && !matchExpired);
        } finally {
            this.readLock.unlock();
        }
    }

    private boolean unMatchedExpired(final String key, final long now) {
        final boolean unMatched = this.unMatchedList.containsKey(key);
        boolean unMatchedExpired = true;

        this.readLock.lock();
        try {
            // check if we know it's missing and we've checked recently
            if (unMatched) {
                unMatchedExpired = now - this.unMatchedList.get(key) > expiration;
            }
            return !(unMatched && !unMatchedExpired);
        } finally {
            this.readLock.unlock();
        }
    }

    // pre-condition is that caller has write lock
    private void loadPolicies(final Properties props, final Properties policies) {
        for (int i=0; i<=MAX_NUM_FILTERS; i++) {
            final int idx = i+1;
            final String filter = policies.getProperty(PREFIX_FILTER + idx
                    , props.getProperty(PREFIX_FILTER + idx, "")).trim();
            if (MAX_NUM_FILTERS == i) {
                final String errorMessage = "Over the max number of filters allowed: " + i;
                LOGGER.severe(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            } else if (filter.isEmpty()) {
                break;
            }
            this.policy.add(filter);
        }

        // need minimum of one policy to execute
        if (0 == this.policy.size()) {
            final String errorMessage = "Must specify at least one spnego.authz.ldap.filter.1";
            LOGGER.severe(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }

    // pre-condition is that caller has write lock
    private void loadResourceNames(final Properties props, final Properties policies) {
        this.resources = new HashMap<String, Map<String, String[]>>();
        for (int i=0; i<=MAX_NUM_FILTERS; i++) {
            final int idx = i+1;
            final Map<String, String[]> access = new HashMap<String, String[]>();

            final String resname = policies.getProperty(PREFIX_NAME + idx
                    , props.getProperty(PREFIX_NAME + idx, "")).trim();

            final String restype = policies.getProperty(PREFIX_TYPE + idx
                    , props.getProperty(PREFIX_TYPE + idx, "").toLowerCase().trim());

            final String[] resaccess = policies.getProperty(PREFIX_ACCESS + idx
                    , props.getProperty(PREFIX_ACCESS + idx, "")).trim().split(",");

            for (int j=0; j<resaccess.length; j++) {
                resaccess[j] = resaccess[j].trim();
            }

            access.put(restype, resaccess);

            if (MAX_NUM_FILTERS == i) {
                final String errorMessage = "Over the max number of resources allowed: " + i;
                LOGGER.severe(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            } else if (resname.isEmpty()) {
                break;
            }

            this.resources.put(resname, access);
        }
    }

    // pre-condition is that caller has write lock
    private UserInfo cacheUserInfo(final String username) throws NamingException {

        if (null == this.userInfoFilter
                || this.userInfoFilter.isEmpty()
                || this.userInfoLabels.size() == 0) {
            LOGGER.info(USER_INFO_FILTER + " was empty OR no value(s) specified for the "
                    + USER_INFO +" property");
            return null;
        }

        // perform AD lookup add to cache 
        final LdapContext context = new InitialLdapContext(this.environment, null);
        final NamingEnumeration<SearchResult> results =
                context.search(this.deecee
                        , String.format(this.userInfoFilter, username)
                        , this.srchCntrls);

        boolean found = false;
        final Map<String, List<String>> labelInfo = new HashMap<String, List<String>>();
        while (results.hasMoreElements()) {
            found = true;
            final SearchResult result = (SearchResult) results.nextElement();
            final Attributes attributes = result.getAttributes();
            for (@SuppressWarnings("rawtypes")
                 NamingEnumeration iter = attributes.getAll(); iter.hasMore();) {
                final Attribute attribute = (Attribute) iter.next();
                final String label = attribute.getID();
                final List<String> info = new ArrayList<String>();
                if (this.userInfoLabels.contains(label)) {
                    labelInfo.put(label, info);
                    for (@SuppressWarnings("rawtypes")
                         NamingEnumeration enmr = attribute.getAll(); enmr.hasMore();) {
                        info.add(enmr.next().toString());
                    }
                }
            }
        }
        results.close();
        context.close();

        // add to cache
        final UserInfo userInfoObject;
        if (found) {
            //LOGGER.info("add to cache userInfoList");
            userInfoObject = new UserInfo() {
                private final Map<String, List<String>> info = labelInfo;
                private final String labels = userInfoLabels.toString();

                @Override
                public List<String> getInfo(final String label) {
                    if (!hasInfo(label)) {
                        throw new NullPointerException(
                                "UserInfo label not found or not in user store: " + label
                                        + " - labels specified in property file: " + labels);
                    }
                    return new ArrayList<String>(info.get(label));
                }

                @Override
                public List<String> getLabels() {
                    return new ArrayList<String>(info.keySet());
                }

                @Override
                public boolean hasInfo(final String label) {
                    return info.containsKey(label);
                }
            };
            this.userInfoList.put(username, userInfoObject);
        } else {
            throw new IllegalArgumentException("UserInfo not found. "
                    + ". This MAY be caused by an incorrect spnego.authz.ldap.user.filter definition"
                    + "; filter=" + this.userInfoFilter
                    + "; policy=" + this.policy);
        }

        return userInfoObject;
    }
}