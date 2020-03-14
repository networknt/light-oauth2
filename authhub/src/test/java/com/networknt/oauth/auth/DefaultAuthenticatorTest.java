package com.networknt.oauth.auth;

import org.junit.Assert;
import org.junit.Test;

public class DefaultAuthenticatorTest {

    // The method is a private method and this test is only working when the method is modified
    // as public method. That is why this method is commented out.
    //@Test
    public void testParseRoles() {
        String r0 = null;
        String r1 = "admin";
        String r2 = "admin user";
        String r3 = "admin user     manager";
        String r4 = " admin       user ";
        DefaultAuthenticator authenticator = new DefaultAuthenticator();

        Assert.assertTrue(authenticator.parseRoles(r0).isEmpty());
        Assert.assertEquals(1, authenticator.parseRoles(r1).size());
        Assert.assertEquals(2, authenticator.parseRoles(r2).size());
        Assert.assertEquals(3, authenticator.parseRoles(r3).size());
        Assert.assertEquals(2, authenticator.parseRoles(r4).size());
    }

}
