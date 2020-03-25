package com.networknt.oauth.auth;

import com.networknt.oauth.security.LightPasswordCredential;
import com.networknt.service.SingletonServiceFactory;
import io.undertow.security.idm.Account;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

/**
 * These are all live test case and they are depending on the light-portal hybrid-query running locally
 * and registered on a local Consul server running in Docker container. In most of the cases, these
 * test cases will be disabled. They are created for developers to debug the implementation.
 *
 * @author Steve Hu
 */
public class LightPortalAuthenticatorTest {
    //@Test
    public void testSplitRoles() {
        String s = "user admin lightapi.net";
        LightPortalAuthenticator auth = new LightPortalAuthenticator();
        Set<String> set = auth.splitRoles(s);
        Assert.assertEquals(set.size(), 3);
        Assert.assertTrue(set.contains("user"));
    }

    /**
     * Manually inject the authenticator and test with a constructed LightPasswordCredential.
     */
    //@Test
    public void testAuthenticate() {
        Class clazz = DefaultAuth.class;
        try {
            clazz = Class.forName("com.networknt.oauth.auth.LightPortalAuth");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Authenticator authenticator = SingletonServiceFactory.getBean(Authenticator.class, clazz);
        Assert.assertTrue(authenticator != null);
        Account account = authenticator.authenticate("stevehu@gmail.com", new LightPasswordCredential("123456".toCharArray(), null, null));
        Assert.assertTrue(account != null);
    }
}
