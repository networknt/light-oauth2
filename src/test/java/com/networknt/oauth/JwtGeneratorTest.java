package com.networknt.oauth;

import com.networknt.security.JwtHelper;
import org.jose4j.jwt.JwtClaims;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by steve on 01/09/16.
 */
public class JwtGeneratorTest {

    @Test
    public void testJwtGen() throws Exception {
        JwtClaims claims = JwtHelper.getDefaultJwtClaims();
        claims.setClaim("user_id", "steve");
        claims.setClaim("user_type", "EMPLOYEE");
        claims.setClaim("client_id", "ddcaf0ba-1131-2232-3313-d6f2753f25dc");
        List<String> scope = Arrays.asList("api.r", "api.w");
        claims.setStringListClaim("scope", scope); // multi-valued claims work too and will end up as a JSON array

        String jwt = JwtHelper.getJwt(claims);
        Assert.assertNotNull(jwt);
        System.out.println(jwt);
    }




}
