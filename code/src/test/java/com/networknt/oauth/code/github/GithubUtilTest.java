package com.networknt.oauth.code.github;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GithubUtilTest {
    static final Logger logger = LoggerFactory.getLogger(GithubUtilTest.class);
	
	@Test
    public void testAuthorization() throws Exception {
        String user = "ahmed1";
        
        Set<String> tmp = GithubUtil.authorize(user);
        //System.out.println("OUTPUT: " + tmp.toString());
        //Assert.assertEquals(17, String.join(",", GithubUtil.authorize(user)));
    }
}
