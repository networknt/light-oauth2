---
date: 2017-01-08T16:15:49-05:00
title: How to generate long lived token
---

In normal case, your clients or testing tools should integrate with OAuth
authorization server to get tokens during runtime; however, for manual
testing, it is very inconvenient to get access token every 10 minutes.
To make tester's jobs easier, you can generate a long lived token for dev
testing from a tool in Light-Java framework.

The light-oauth2 contains two testing key pairs for testing only. Both private 
keys and public key certificates can be found in resources/config/oauth folder. 
The same public key certificates are included in light-java so that the server 
can verify any token issued by this oauth server.

Important note:
For your official deployment, please create key pair of your own or buy 
certificate from one of the CAs.


The tool is actually a test case that you can add an entry to generate
token based on your need. 

It is locate at light-java/src/test/java/com/networknt/security/JwtHelperTest.java

Here are two examples that generate authorization code grant type tokens. You can
add your own test case with different parameters.


```
    @Test
    public void longLivedATMP1000Jwt() throws Exception {
        JwtClaims claims = getTestClaims("eric", "EMPLOYEE", "f7d42348-c647-4efb-a52d-4c5787421e72", Arrays.asList("ATMP1000.w", "ATMP1000.r"));
        claims.setExpirationTimeMinutesInTheFuture(5256000);
        String jwt = JwtHelper.getJwt(claims);
        System.out.println("***LongLived ATMP1000 JWT***: " + jwt);
    }


    @Test
    public void longLivedPetStoreJwt() throws Exception {
        JwtClaims claims = getTestClaims("steve", "EMPLOYEE", "f7d42348-c647-4efb-a52d-4c5787421e72", Arrays.asList("write:pets", "read:pets"));
        claims.setExpirationTimeMinutesInTheFuture(5256000);
        String jwt = JwtHelper.getJwt(claims);
        System.out.println("***LongLived PetStore JWT***: " + jwt);
    }

```

The following is a token generated for petstore api with scope write:pets 
and read:pets

```
Bearer eyJraWQiOiIxMDAiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTc5NDgwMDYzOSwianRpIjoiWFhlQmpJYXUwUk5ZSTl3dVF0MWxtUSIsImlhdCI6MTQ3OTQ0MDYzOSwibmJmIjoxNDc5NDQwNTE5LCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6InN0ZXZlIiwidXNlcl90eXBlIjoiRU1QTE9ZRUUiLCJjbGllbnRfaWQiOiJmN2Q0MjM0OC1jNjQ3LTRlZmItYTUyZC00YzU3ODc0MjFlNzIiLCJzY29wZSI6WyJ3cml0ZTpwZXRzIiwicmVhZDpwZXRzIl19.f5XdkmhOoHT2lgTobqVGPp2aWUv_ItA0tqyLHC_CeMbmwzPvREqb5-oJ9T_m3VwRcJlPTh8xTdSjrLITXClaQFE4Y0bT8C-u6bb38uT-NQ5mjUjLrFQYHCF6GqwL7YkwQt_rshEqtrDFe1T4HoEL_9FHbOxf3MSJ39UKq0Ef_9mHXkn4Y-SHfdapeuUWc_4dDPdxzEdzbqmf1WSOOgTuM5O5F2fK4p_ix8LQl0H3AnMZIhIDyygQEnYPxEG-u35gwh503wfxio6buIf0b2Kku2PXPE36lethZwIVaPTncEcY5OPxfBxXuy-Wq-YQizd7NnpJTteHYbdQXupjK7NDvQ
```

