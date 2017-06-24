---
date: 2017-06-24T08:04:57-04:00
title: Custom Grant Type in Enterprise Edition
---

There are four standard grant types in OAuth2 spec and it outlines how to add custom
grant types. With more and more uses asking for some special integration with our OAuth2
services, we are adding the following custom grant types. These are non-standard and will
only be used with a special client_type called trusted in most of the cases. 


# client_authenticated_user

This particular grant type is to address the following use cases. 

* A customer has a legacy web server that consumes web services in XML and slowly to
convert to JSON based restful APIs. The web server has single sign-on already and all
users have been authenticated by the web server. It needs to get JWT token in order to
access newly created RESTful APIs. Instead of SAML and JWT dancing, the customer wants
to get JWT token from OAuth2 provider directly with the trust relationship between the 
web server and OAuth2 token service. Once user has been authenticated, a user info JSON
object will be submitted to OAuth2 token endpoint along with client_id and client_secret
to get access token and optional refresh token.

The client_authenticated_user grant type is suitable in cases where the resource owner 
has a trust relationship with the client, such as the device operating system or a highly 
privileged application. The authorization server should take special care when enabling 
this grant type and only allow it when other flows are not viable. In this implementation, 
only client registered as trusted can use this grant type. 

This grant type is suitable for clients capable of authenticating users by itself. The
way client authenticate user is out of the scope of this grant type. The flow is very
similar with password grant type.

### Request

The client makes a request to the token endpoint by adding the following 
parameters using the "application/x-www-form-urlencoded" format with a character 
encoding of UTF-8 in the HTTP request entity-body:

* grant_type
         
REQUIRED. Value MUST be set to "client_authenticated_user".

* userId

REQUIRED. The resource owner username.

* userType

REQUIRED. The resource owner password.

* scope
         
OPTIONAL. The scope of the access request.

* client_id:client_secret
         
REQUIRED, as BASIC authorization header with encoded client_id:client_secret 
to authenticate with the authorization server.

* other custom claims

OPTIONAL. These will be added to the JWT token as custom claims. 

### Response

If the access token request is valid and authorized, the authorization server 
issues an access token and a refresh token. If the request failed client
authentication or is invalid, the authorization server returns an error response.


### Error

The following two errors might occur if userId or userType is missing from the request

```
ERR12031:
  statusCode: 400
  code: ERR12031
  message: USER_ID_REQUIRED_FOR_CLIENT_AUTHENTICATED_USER_GRANT_TYPE
  description: UserId is required for client_authenticated_user grant type.

ERR12032:
  statusCode: 400
  code: ERR12032
  message: USER_TYPE_REQUIRED_FOR_CLIENT_AUTHENTICATED_USER_GRANT_TYPE
  description: UserType is required for client_authenticated_user grant type.

ERR12024:
  statusCode: 400
  code: ERR12024
  message: NOT_TRUSTED_CLIENT
  description: Grant type is allowed only for trusted client type.

```

There are other errors but above ones are specific for this grant type. 

