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

package com.networknt.oauth.code.spnego;

import java.util.Properties;

/**
 * An interface for specifying how a program/library interacts with an
 * implementing object when performing user authorization (authZ). This
 * interface allows for a much more limited and simpler design than the
 * <a href="https://en.wikipedia.org/wiki/Role-based_access_control">Role
 * Based Access Control</a> (RBAC) model and the
 * <a href="https://en.wikipedia.org/wiki/Attribute-Based_Access_Control">Attribute
 * Based Access Control</a> (ABAC) model.
 *
 * <p>
 * This interface does not specify how an implementing object obtains
 * user information or from where the user information is stored. An
 * implementing object is free to aggregate information from multiple
 * sources (ie. LDAP Server, RDBMS, etc.). Data retrieval such as
 * roles, groups, attributes, resources, user information etc. is
 * defined by the implementing object. Since it is assumed that the
 * given store is a shared resource, the implementing object must take
 * care of synchronizing access to any shared resource. This interface
 * does not define, specify or suggest synchronization semantics.
 * </p>
 *
 * <p>
 * This interface does not specify a new or different Access Control model
 * and instead follows the RBAC and ABAC style for access control.
 * However, one meaningful difference is that instead of determining
 * resource permissions/access based on roles, the lookup is
 * based on attributes of the user. Usage semantics will be in the
 * same style as RBAC but policies will be in the style of ABAC.
 * </p>
 *
 * <p>
 * The SPNEGO Library provides at least one concrete reference implementation
 * of the <code>UserAccessControl</code> interface. The {@link LdapAccessControl}
 * class implements this interface. The <code>LdapAccessControl</code>
 * class connects to an LDAP server for the purpose of obtaining user attribute/
 * role/group/etc. information. For additional examples about attribute/role semantics,
 * please see the javadoc for the {@link UserAccessControl} interface.
 * </p>
 *
 * <p>
 * In the following examples, the source of attribute information is defined to be
 * in an LDAP/Active Directory Group, User Department, Email Distribution List, etc.
 * A policy is configured to search one of these attribute sets or optionally to
 * search all of these attribute sets. The attribute sets can be mixed to allow
 * for a more expressive policy statement. e.g. 1) A user has access if they are
 * in <i>this</i> AD Group and belong in one of <i>these</i> departments, or
 * 2) a user has access if they are in <i>this</i> email distribution list
 * and in one of <i>these</i> AD Groups or is in one <i>these</i> departments,
 * or 3) a user can see <i>the edit button</i> if they are in <i>this</i>
 * AD Group and in one of <i>these</i> other AD Groups.
 * </p>
 *
 * <p>
 * <b>Example Usage 1:</b>
 * A new ticketing application where every user of the application has the same
 * access rights and the only requirement is that they have logged-in to their
 * workstation/computer. Under this scenario, every user must first authenticate (authN)
 * but since every authN user has the same access rights to the ticketing application,
 * authZ may have little to no value. Hence, there would be no need to implement
 * or enable authZ for this application.
 * </p>
 *
 * <p>
 * <b>Example Usage 2:</b>
 * A new feature will be introduced to the ticketing application. The new feature
 * should only be accessible by users who are in the LDAP Group/Active Directory Group
 * BizDev Mngrs., Client Services, or Acct. Mngrs. Since authN is assumed to have already
 * taken place, the developer of the new feature can protect access to the new feature
 * by invoking one of the methods in the <code>UserAccessControl</code> interface.
 *
 * <pre>
 * UserAccessControl accessControl = ...;
 * boolean allowPrivAccess = false;
 *
 * String[] attributes = new String[] {"BizDev Mngrs.", "Client Services", "Acct. Mngrs."};
 *
 * if (accessControl.anyRole("dfelix", attributes)) {
 *     allowPrivAccess = true;
 * }
 * </pre>
 *
 * In the above example, if the user dfelix has at least one of those attributes (in this
 * example, each attribute is an AD group),
 * then the code inside the if block will execute. The <code>anyRole</code> method
 * will return true if it finds at least one matching attribute.
 *
 * <p>
 * <b>Example Usage 3:</b>
 * A second new feature will be introduced but this time only certain departments within the
 * Information Technology (IT) division should have access to this latest feature.
 * The IT division has many departments (e.g. Helpdesk/Desktop Support, Networking Team,
 * Database Admins,  Business Analysts, Software Developers, etc.). However, only Business
 * Analysts and Software Developers within IT should have access to this latest feature.
 *
 * <pre>
 * UserAccessControl accessControl = ...;
 * boolean editAndAdminBtns = false;
 *
 * String attributeX = "IT Group";
 * String[] attributeYs = new String[] {"Biz. Analyst", "Developer"};
 *
 * if (accessControl.hasRole("dfelix", attributeX, attributeYs)) {
 *     editAndAdminBtns = true;
 * }
 * </pre>
 *
 * In the above example, if dfelix has the attribute IT Group and has either
 * Biz. Analyst or Developer, then the code inside the if block will execute.
 * The <code>hasRole</code> method will return true if it can match the first
 * attribute AND at least one of the remaining attributes.
 *
 * <p>
 * The <code>UserAccessControl</code> interface treats the traditional notion of a
 * role as a higher level abstraction that includes any and all attributes that can
 * identify a user or a set of users. For example, a job title named "Team Lead", a
 * department named "Human Resources", an Active Directory group named
 * "Offsite Users - NY", an email distribution list named "Performance Team", etc.,
 * are abstractly defined as attributes.
 * </p>
 *
 * <p>
 * Each attribute concretely belongs to an attribute set. The definition of an attribute set
 * is somewhat arbitrary but it is not abstract. i.e. job titles, departments, email distribution
 * lists, active directory groups, user location, etc. are attribute sets. An attribute set is
 * commonly and sometimes indirectly specified in a user store (database/ldap/active directory/etc.)
 * as user information properties, system roles/groups, company distribution lists/groups, etc.
 * </p>
 *
 * <p>
 * In the simplest examples, an attribute from one attribute set and another attribute from
 * another attribute set may be combined to form a policy statement. In the example above
 * (Example Usage 3), edit/admin buttons will be enabled only if the user dfelix has the
 * attribute IT Group (from the division attribute set) AND a second attribute of either
 * Biz. Analyst or Developer (from the departments attribute set).
 * </p>
 *
 * <p>
 * <b>Example Usage 4:</b>
 * The ticketing system is nearing maturity and it has been determined that only
 * Biz. Analysts from the IT Group (i.e. policy statement A) or
 * any Biz. Analyst out of the Los Angeles office (i.e. policy statement B)
 * should have access to the edit/admin buttons.
 * Biz. Analysts from the other locations should not have the edit/admin buttons enabled.
 *
 * <pre>
 * UserAccessControl accessControl = ...;
 * boolean editAndAdminBtns = false;
 *
 * if (accessControl.hasRole("dfelix", "IT Group", "Biz. Analyst")
 *         || accessControl.hasRole("dfelix", "Los Angeles", "Biz. Analyst")) {
 *     editAndAdminBtns = true;
 * }
 * </pre>
 *
 * <p>
 * In the above example, two policy statements were defined - policy A and policy B.
 * Policy statement A uses attributes from the attribute set division and an attribute
 * from the attribute set department.
 * Policy statement B uses attributes from the attribute set location and an attribute
 * from the attribute set department.
 * Given the arbitrary nature of attributes, attribute sets and how they are combined
 * to form a policy statement, an implementing class of the <code>UserAccessControl</code>
 * interface MUST allow the above usage semantics. For an inspiration and/or example of how
 * this can be achieved, please see the {@link LdapAccessControl} source code.
 * </p>
 *
 * <p>
 * Note that policy statement A and policy statement B can be re-defined into
 * one policy statement, policy statement C.
 *
 * <pre>
 * UserAccessControl accessControl = ...;
 * boolean editAndAdminBtns = false;
 *
 * String attributeX = "Biz. Analyst";
 * String[] attributeYs = new String[] {"Los Angeles", "IT Group"};
 *
 * if (accessControl.hasRole("dfelix", attributeX, attributeYs)) {
 *     editAndAdminBtns = true;
 * }
 * </pre>
 *
 * An implementation of this interface MUST support the usage semantics for policy A,
 * policy B, and policy C. Classes that implement the <code>UserAccessControl</code>
 * interface MUST allow symmetry in the usage semantics. By way of example, if a
 * developer calls <code>accessControl.hasRole("dfelix", "Biz. Analyst", "IT Group")</code>
 * in one part/section of the program/code and then calls
 * <code>accessControl.hasRole("dfelix", "IT Group", "Biz. Analyst")</code>
 * in another part/section of the program, BOTH calls MUST return true
 * (either both returns true or both returns false). For an inspiration and/or example
 * of how this can be achieved, please see the {@link LdapAccessControl} source code.
 *
 * <p>
 * It is encouraged that the attribute identifier, the value passed-in to the method(s) of
 * this interface, be unique across the entire set of attributes and attribute sets. For
 * example, if an email distribution list (an attribute set) is named "Performance Team"
 * (an attribute) and there exists a department (an attribute set) also named "Performance Team"
 * (an attribute), one of the attribute sets must be excluded from the policy lookup definition
 * or one of the attributes must be renamed (e.g. "Perf. Team") or one of the attributes sets
 * can not be a search filter so that the uniqueness property is maintained.
 * Another alternative is to modify the policy statement(s) such that the uniqueness
 * property is still maintained. Finally, the uniqueness property is enabled by default
 * but can be disabled via a configuration parameter.
 * </p>
 *
 * <p>
 * <b>Uniqueness Example (with LdapAccessControl):</b>
 * <pre>
 * &lt;!-- AD Group (excluding group names that match with department) --&gt;
 * &lt;init-param&gt;
 *     &lt;param-name&gt;spnego.authz.ldap.filter.1&lt;/param-name&gt;
 *      &lt;param-value&gt;
 *      &lt;![CDATA[((sAMAccountName=%1$s)
 *      (memberOf:1.2.840.113556.1.4.1941:=CN=%2$s,OU=Groups,OU=Los Angeles,DC=athena,DC=local)(!((sAMAccountType=805306368)(department=%2$s))))]]&gt;
 *      &lt;/param-value&gt;
 * &lt;/init-param&gt;
 * &lt;!-- Department --&gt;
 * &lt;init-param&gt;
 *     &lt;param-name&gt;spnego.authz.ldap.filter.2&lt;/param-name&gt;
 *     &lt;param-value&gt;
 *     &lt;![CDATA[((sAMAccountType=805306368)(sAMAccountName=%1$s)((sAMAccountType=805306368)(department=%2$s)))]]&gt;
 *     &lt;/param-value&gt;
 * &lt;/init-param&gt;
 * <i>filter must all be on one line. wrapped here for compactness.</i>
 * </pre>
 *
 * In the above example, the filter one (1) will find the AD group provided that a department
 * is not named the same. The consequence to this alternative approach is that AD Groups
 * with the same name as a department name will no longer be available as an option.
 *
 * <p>
 * Classes that implement the <code>UserAccessControl</code> interface MUST throw an
 * exception if it finds that the uniqueness property has been broken (provided that
 * the uniqueness property has not been disabled). For example,
 * if some policy statement D, specifies that a user MUST be in the IT Group AND that the
 * user must be in the Performance Team email distribution list; however the attribute
 * identifier Performance Team is defined in two attribute sets
 * (division and email distribution list), implementations of this interface
 * MUST throw an exception upon executing policy statement D.
 *
 * <pre>
 * UserAccessControl accessControl = ...;
 * boolean editAndAdminBtns = false;
 *
 * try {
 *     if (accessControl.hasRole("dfelix", "IT Group", "Performance Team")) {
 *         // will never get in here since an exception will be thrown
 *         editAndAdminBtns = true;
 *     }
 * } catch (MyRuntimeExceptions mre) {
 *     System.err.println("Uniqueness property broken. The attribute Performance Team "
 *         + "was found in at least two attribute sets. The division attribute set "
 *         + "AND in the email distribution list attribute set. "
 *         + "uniqueness property=enabled");
 *     System.exit(-1);
 * }
 * </pre>
 *
 * <p>
 * In the above example, if a policy was defined to search the  division attribute set
 * and the email distribution list attribute set, AND it finds the attribute
 * "Performance Team" in both attribute sets, an exception MUST be thrown.
 * </p>
 *
 * <p>
 * For an inspiration and/or example of how this can be achieved, please see the
 * {@link LdapAccessControl} source code.
 * </p>
 *
 * <p>
 * <b>User-defined Resource Label Example:</b>
 * </p>
 *
 * <p>
 * An alternative to specifying department names, groups, email distribution lists, etc.
 * is to use a resource label. Resource labels are optional and hence must undergo additional
 * configuration before use. The <code>anyAccess</code> and the <code>hasAccess</code>
 * methods are used as an alternative to the <code>anyRole</code> method and the
 * <code>hasRole</code> methods.
 *
 * <pre>
 * UserAccessControl accessControl = ...;
 * boolean editAndAdminBtns = false;
 *
 * if (accessControl.hasAccess("dfelix", "admin-buttons")) {
 *     editAndAdminBtns = true;
 * }
 * </pre>
 *
 * <p>
 * In the above example, the attribute(s) that support the policy is abstracted by the
 * user-defined resource label named admin-buttons. Concretely, the resource label
 * admin-buttons could have been assigned the attributes IT Group, Biz. Analyst, and Developer.
 * </p>
 *
 * <p>
 * To see how a web application/service can leverage user access controls, as well as see
 * additional usage examples, please take a look at the javadoc for the
 * {@link UserAccessControl} interface.
 * </p>
 *
 * <p>
 * Also, take a look at the <a href="http://spnego.sourceforge.net/reference_docs.html"
 * target="_blank">reference docs</a> for a complete list of configuration parameters.
 * </p>
 *
 * <p>
 * To see a working example and instructions, take a look at the
 * <a href="http://spnego.sourceforge.net/user_access_control.html"
 * target="_blank">authZ for standalone apps</a> example.
 * </p>
 *
 * <p>
 * Enabling authZ for servlet containers (Tomcat, JBoss, etc.) can be found in the
 * <a href="http://spnego.sourceforge.net/enable_authZ_ldap.html"
 * target="_blank">enable authZ with LDAP</a> guide.
 * </p>
 *
 *
 * @author Darwin V. Felix
 *
 */
public interface UserAccessControl {

    /**
     * Used for clean-up when usage of the object is no longer needed and no other
     * method calls will be made on this instance.
     *
     * <p>
     * Calling this method is an indication that no other method calls will be called
     * on this instance. If method calls must resume on this instance, the init method
     * MUST be called before this instance can be placed back into service.
     * </p>
     *
     * <p>
     * If this method has been called and a reference to the instance is
     * maintained, the init method must be called again to re-initialize the
     * object's instance variables.
     * </p>
     */
    void destroy();

    /**
     * Checks to see if the given user has at least one of the passed-in attributes.
     *
     * <pre>
     * String[] attributes = new String[] {"Developer", "Los Angeles", "Manager"};
     *
     * if (accessControl.anyRole("dfelix", attributes)) {
     *     // will be in here if dfelix has at least one matching attribute
     * }
     * </pre>
     *
     * @param username e.g. dfelix
     * @param attribute e.g. Team Lead, IT, Developer
     * @return true if the user has at least one of the passed-in roles/features
     */
    boolean anyRole(final String username, final String... attribute);

    /**
     * Checks to see if the given user has the passed-in attribute.
     *
     * <pre>
     * String attribute = "Los Angeles";
     *
     * if (accessControl.hasRole("dfelix", attribute)) {
     *     // will be in here if dfelix has that one attribute
     * }
     * </pre>
     *
     * @param username e.g. dfelix
     * @param attribute e.g. IT
     * @return true if the user has the passed-in role/attribute
     */
    boolean hasRole(final String username, final String attribute);

    /**
     * Checks to see if the given user has the first attribute
     * AND has at least one of the passed-in attributes.
     *
     * <pre>
     * String attributeX = "Los Angeles";
     * String[] attributeYs = new String[] {"Developer", "Manager"};
     *
     * if (accessControl.hasRole("dfelix", attributeX, attributeYs)) {
     *     // will be in here if dfelix has attributeX
     *     // AND has at least one of the attributeYs.
     * }
     * </pre>
     *
     * @param username e.g. dfelix
     * @param attributeX e.g. Information Technology
     * @param attributeYs e.g. Team Lead, IT-Architecture-DL
     * @return true if user has featureX AND at least one the featureYs
     */
    boolean hasRole(final String username
            , final String attributeX, final String... attributeYs);

    /**
     * Checks to see if the given user has at least one of the passed-in
     * user-defined resource labels.
     *
     * <pre>
     * if (accessControl.anyAccess("dfelix", "admin-links", "buttons-for-ops")) {
     *     // will be in here if dfelix has at least one matching resource
     * }
     * </pre>
     *
     * @param username e.g. dfelix
     * @param resources e.g. admin-links, ops-buttons
     * @return true if the user has at least one of the passed-in user-defined resource labels
     */
    boolean anyAccess(final String username, final String... resources);

    /**
     * Checks to see if the passed-in user has access to the
     * user-defined resource label.
     *
     * <pre>
     * UserAccessControl accessControl = ...;
     * boolean editAndAdminBtns = false;
     *
     * if (accessControl.hasAccess("dfelix", "admin-buttons")) {
     *     editAndAdminBtns = true;
     * }
     * </pre>
     *
     * @param username e.g. dfelix
     * @param resource e.g. admin-buttons
     * @return true if user has access to the user-defined resource labels
     */
    boolean hasAccess(final String username, final String resource);

    /**
     * Checks to see if the given user has the first resource label
     * AND has at least one of the passed-in resource labels.
     *
     * <pre>
     * String resourceX = "phone-list";
     * String[] resourceYs = new String[] {"staff-directory", "procedure-manual"};
     *
     * if (accessControl.hasAccess("dfelix", resourceX, resourceYs)) {
     *     // will be in here if dfelix has resourceX
     *     // AND has at least one of the resourceYs.
     * }
     * </pre>
     *
     * @param username e.g. dfelix
     * @param resourceX e.g. phone-list
     * @param resourceYs e.g. staff-directory, procedure-manual, emergency-contact-list
     * @return true if user has resourceX AND at least one the resourceYs
     */
    boolean hasAccess(final String username, final String resourceX, final String... resourceYs);

    /**
     * Returns the user's info object for the given user.
     * @param username String
     * @return the user's info object for the given user
     */
    UserInfo getUserInfo(final String username);

    /**
     * Method is used for initialization prior to use/calling any other method.
     *
     * <p>
     * Calling this method is an indication that this instance is in service/active
     * and any method can be called at anytime for the purpose of servicing a request.
     * </p>
     *
     * <p>
     * If this method has been called and a reference to the instance is
     * maintained, this method should not be called again unless the destroy
     * method is called first.
     * </p>
     * @param props Properties
     */
    void init(final Properties props);
}