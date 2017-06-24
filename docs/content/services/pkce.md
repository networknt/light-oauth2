---
date: 2017-06-24T08:05:13-04:00
title: Proof key for code exchange (PKCE) 
---

Public OAuth clients that use the code grant and run on smartphones are susceptible to 
a code interception attack. Fortunately, this attack can be successfully prevented by 
establishing a secure binding between the authorisation request and the subsequent token 
request.

The OAuth work group devised an official mini extension of the protocol for that, called 
Proof Key for Code Exchange (PKCE) and published in September 2015 as RFC 7636. It is a 
countermeasure against the authorization code interception attack.

## How does PKCE work?

* The client creates a large random string called the code verifier.
* The client then computes its SHA-256 hash, called the code_challenge.
* The client passes the code_challenge and code_challenge_method (a keyword for the SHA-256 
hash) along with the regular authorisation request parameters to the OAuth2 server. The 
server stores them until a token request is received by the client.
* When the client receives the authorisation code, it makes a token request with the 
code_verifier included. The OAuth2 server recomputes the code challenge, and if it matches 
the original one, releases the requested tokens.

PKCE essentially works by preventing a malicious app or code had intercepted the code (as it 
was passed from the system browser / the OS to the app) from exchanging it for a token.

The latest release of the light-oauth2 server adds complete support for PKCE. In order to make 
use of it a public client just needs to set the appropriate PKCE request parameters. The server 
will take care of the rest.

We also provide utility class CodeVerifierUtil in light-4j utility module to assist Java client
to create code verifier and code challenge. 



## PKCE Authorization Request

An authorization request that uses PKCE goes out with 

code_challenge parameter and optionally with code_challenge_method parameter. 

The value of code_challenge parameter is computed by applying a code challenge method (= computation 
logic) to a code verifier.

A code verifier itself is a random string using characters of [A-Z] / [a-z] / [0-9] / "-" / "." / "_" / "~", 
with a minimum length of 43 characters and a maximum length of 128 characters.

The defined code challenge methods are plain and S256. Respective computation logics to convert a code 
verifier into a code challenge are as follows.

* plain	code_challenge = code_verifier

* S256	code_challenge = BASE64URL-ENCODE(SHA256(ASCII(code_verifier)))

The plain method does not change the input, so the value of code_verifier and the resultant value 
of code_challenge are equal.

The S256 method computes the SHA-256 hash of the input and then encodes the hash value using Base64-URL. 

When the used code challenge method is S256, a client application must tell it by including 
code_challenge_method=S256 parameter in an authorization request. When code_challenge_method parameter 
is omitted, an authorization server assumes plain as the default value.

## PKCE Authorization Response

After generating an authorization code, an authorization server saves it into its in-memory data grid 
with the code challenge and the code challenge method contained in the authorization request.

The authorization server will use the saved code challenge and the code challenge method later to 
verify a token request from the client application.

A response from the authorization endpoint has nothing special for PKCE. It's a normal response as usual.


## PKCE Token Request

After receiving an authorization code from an authorization server, a client application makes a token 
request. In addition to the authorization code, the token request must include the code verifier 
used to compute the code challenge.

The name of the request parameter to specify a code verifier is code_verifier.

## PKCE Token Response

A token endpoint of an authorization server that supports PKCE checks whether a token request contains 
a valid code verifier.

Of course, this check is performed only when grant_type is authorization_code and the authorization 
code contained in the token request is associated with a code challenge.

If a token request does not contain a valid code verifier although the conditions above meet, the 
request is regarded as from a malicious application and the authorization server returns an error 
response.

Verification is performed by comparing two code challenges.

One is what was contained in the authorization request and is stored in the in-memory data grid. The 
other is what an authorization server computes using the code verifier contained in the token request 
and the code challenge method stored in the in-memory data grid.

If the two code challenges are equal, the token request can be regarded as from the legitimate client 
application that has made the original authorization request. Otherwise, the token request must be 
regarded from a malicious application.

If a token request is verified, an authorization server issues an access token as usual.


## Errors

The following errors are PKCE specific

```
ERR12033:
  statusCode: 400
  code: ERR12033
  message: INVALID_CODE_CHALLENGE_METHOD
  description: Invalid PKCE code challenge method %s.

ERR12034:
  statusCode: 400
  code: ERR12034
  message: CODE_CHALLENGE_TOO_SHORT
  description: PKCE codeChallenge length under lower limit, codeChallenge = %s
ERR12035:
  statusCode: 400
  code: ERR12035
  message: CODE_CHALLENGE_TOO_LONG
  description: PKCE codeChallenge length over upper limit, codeChallenge = %s
ERR12036:
  statusCode: 400
  code: ERR12036
  message: INVALID_CODE_CHALLENGE_FORMAT
  description: PKCE codeChallenge format is invalid, codeChallenge = %s
ERR12037:
  statusCode: 400
  code: ERR12037
  message: INVALID_CODE_VERIFIER_FORMAT
  description: PKCE codeVerifier format is invalid, codeVerifier = %s
ERR12038:
  statusCode: 400
  code: ERR12038
  message: CODE_VERIFIER_TOO_SHORT
  description: PKCE codeVerifier length under lower limit , codeVerifier = %s
ERR12039:
  statusCode: 400
  code: ERR12039
  message: CODE_VERIFIER_TOO_LONG
  description: PKCE codeVerifier length over upper limit , codeVerifier = %s
ERR12040:
  statusCode: 400
  code: ERR12040
  message: CODE_VERIFIER_MISSING
  description: PKCE codeVerifier is not specified
ERR12041:
  statusCode: 400
  code: ERR12041
  message: CODE_VERIFIER_FAILED
  description: PKCE verification failed

```

## References

[tools.ietf.org/html/rfc6749](https://tools.ietf.org/html/rfc6749)

[tools.ietf.org/html/rfc7636](https://tools.ietf.org/html/rfc7636)

[enhancing-oauth-security-for-mobile-applications-with-pkse](http://openid.net/2015/05/26/enhancing-oauth-security-for-mobile-applications-with-pkse/)




