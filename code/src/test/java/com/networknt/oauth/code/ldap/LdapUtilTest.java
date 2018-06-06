package com.networknt.oauth.code.ldap;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.networknt.oauth.code.handler.TestServer;

public class LdapUtilTest {
	@ClassRule
    public static TestServer server = TestServer.getInstance();
    static final Logger logger = LoggerFactory.getLogger(LdapUtilTest.class);
    
	@Test
    public void testAuthentication() throws Exception {
        String user = "jduke";
        String password = "theduke";
        
        Assert.assertEquals(true, LdapUtil.authenticate(user, password));
    }

	@Test
    public void testAuthorization() throws Exception {
        String user = "jduke";
        String expectedGroups = "cn=just-users,ou=users,dc=undertow,dc=io,cn=best-users,ou=users,dc=undertow,dc=io";
        
        Assert.assertEquals(expectedGroups, String.join(",", LdapUtil.authorize(user)));
    }
	
	@Test
    public void testAuth() throws Exception {
		String user = "jduke";
        String password = "theduke";
        
        // function returns null always
        Assert.assertEquals(null, LdapUtil.auth(user, password));
    }
	
}
