---
date: 2017-09-19T21:01:03-04:00
title: Token Endpoint
---

# Token

This is a post endpoint to get JSON web tokens. Currently, it support three different
grant types: authorization_code, client_credentials and password.

## Authorization Code Grant Type

### Request

The client makes a request to the token endpoint by sending the following 
parameters using the "application/x-www-form-urlencoded" format.

* grant_type
         
REQUIRED. Value MUST be set to "authorization_code".

* code
         
REQUIRED.  The authorization code received from the authorization server.

* redirect_uri
         
REQUIRED, if the "redirect_uri" parameter was included in the authorization 
request, and their values MUST be identical.

* client_id:client_secret
         
REQUIRED, as BASIC authorization header with encoded client_id:client_secret 
to authenticate with the authorization server.


* code_verifier

OPTIONAL, PKCE code verifier that used by Mobile Native App

### Response

If the access token request is valid and authorized, the authorization server 
issues an access token and a refresh token.  If the request client
authentication failed or is invalid, the authorization server returns an error 
response.


## Client Credentials Grant Type

The client can request an access token using only its client credentials 
(or other supported means of authentication) when the client is requesting 
access to the protected resources under its control, or those of another 
resource owner that have been previously arranged with the authorization 
server.
   
### Request

The client makes a request to the token endpoint by adding the following 
parameters using the "application/x-www-form-urlencoded" format with a 
character encoding of UTF-8 in the HTTP request entity-body:

* grant_type
         
REQUIRED. Value MUST be set to "client_credentials".

* scope
         
OPTIONAL. The scope of the access request.

* client_id:client_secret
         
REQUIRED, as BASIC authorization header with encoded client_id:client_secret 
to authenticate with the authorization server.
   
   
### Response

If the access token request is valid and authorized, the authorization server 
issues an access token. A refresh token SHOULD NOT be included. If the request
failed client authentication or is invalid, the authorization server returns 
an error response.

## Resource Owner Password Credentials Grant Type

The resource owner password credentials grant type is suitable in cases where 
the resource owner has a trust relationship with the client, such as the device 
operating system or a highly privileged application. The authorization server 
should take special care when enabling this grant type and only allow it when 
other flows are not viable. In this implementation, only client registered as
trusted can use password grant type. 

This grant type is suitable for clients capable of obtaining the resource owner's 
credentials (username and password, typically using an interactive form).  It is 
also used to migrate existing clients using direct authentication schemes such as 
HTTP Basic or Digest authentication to OAuth by converting the stored credentials 
to an access token.

### Request

The client makes a request to the token endpoint by adding the following 
parameters using the "application/x-www-form-urlencoded" format with a character 
encoding of UTF-8 in the HTTP request entity-body:

* grant_type
         
REQUIRED. Value MUST be set to "password".

* username

REQUIRED. The resource owner username.

* password

REQUIRED. The resource owner password.

* scope
         
OPTIONAL. The scope of the access request.

* client_id:client_secret
         
REQUIRED, as BASIC authorization header with encoded client_id:client_secret 
to authenticate with the authorization server.

### Response

If the access token request is valid and authorized, the authorization server 
issues an access token and a refresh token. If the request failed client
authentication or is invalid, the authorization server returns an error response.

## Refresh Token Grant Type

If the authorization server issued a refresh token to the client, the client 
makes a refresh request to the token endpoint by adding the following parameters 
using the "application/x-www-form-urlencoded" format with a character encoding of 
UTF-8 in the HTTP request entity-body.

* grant_type

REQUIRED.  Value MUST be set to "refresh_token".

* refresh_token

REQUIRED.  The refresh token issued to the client.

* scope

OPTIONAL.  The scope of the access request. The requested scope MUST NOT include 
any scope not originally granted by the resource owner, and if omitted is treated 
as equal to the scope originally granted by the resource owner.

Because refresh tokens are typically long-lasting credentials used to request 
additional access tokens, the refresh token is bound to the client to which it was 
issued. If the client type is confidential or the client was issued client credentials 
(or assigned other authentication requirements), the client MUST authenticate with the
authorization server.

The authorization server MUST:

* require client authentication for confidential clients or for any client that was 
issued client credentials (or with other authentication requirements),

* authenticate the client if client authentication is included and ensure that the 
refresh token was issued to the authenticated client, and

* validate the refresh token.

If valid and authorized, the authorization server issues an access token. If the 
request failed verification or is invalid, the authorization server returns an error
response.

The authorization server issues a new refresh token and the client MUST discard the 
old refresh token and replace it with the new refresh token. The authorization server 
revoke the old refresh token after issuing a new refresh token to the client. The new 
refresh token scope MUST be identical to that of the refresh token included by the 
client in the request.
   
   
## Implementation

There is only one post endpoint for this service and the default port is 6882.

Here is the specification.

```
swagger: '2.0'

info:
  version: "1.0.0"
  title: OAuth2 Service Token Service
  description: OAuth2 Service that issues access tokens. 
  contact:
    email: stevehu@gmail.com
  license:
    name: "Apache 2.0"
    url: "http://www.apache.org/licenses/LICENSE-2.0.html"
host: oauth2.networknt.com
schemes:
  - http
  - https

consumes:
  - application/x-www-form-urlencoded   
produces:
  - application/json

paths:
  /oauth2/token:
    post:
      description: JSON object that contains access token
      operationId: postToken
      parameters:
      - name: authorization
        description: "encoded client_id and client_secret pair"
        in: header
        type: string
        required: true      
      - name: grant_type
        type: string
        enum: 
        - authorization_code
        - client_credentials
        - password
        - refresh_token
        required: true
        in: formData
      - name: code
        description: "used in authorization_code to specify the code" 
        type: string
        in: formData
      - name: username
        description: "mandatory in password grant type"
        type: string
        in: formData
      - name: password
        description: "mandatory in password grant type"
        type: string
        in: formData
      - name: scope
        description: "used by all flows to specify scope in the access token"
        type: string
        in: formData
      - name: redirect_uri
        description: "used in authorization code if code endpoint with rediret_uri"
        type: string
        in: formData
      - name: refresh_token
        description: "refresh token used to get another access token"
        type: string
        in: formData
      responses:
        200:
          description: "Successful Operation"
```

### /oauth2/token

When a post request is received, the following validations and processes will be performed.

* The handler expects a x-www-form-urlencoded form and if it doesn't exist, the following
error will be returned.

```
  "ERR12000": {
    "statusCode": 400,
    "code": "ERR12000",
    "message": "UNABLE_TO_PARSE_FORM_DATA",
    "description": "Unable to parse x-www-form-urlencoded form data."
  }
```

* The parameter grant type should only allowed client_credentials and authorization_code. If
other grant_type is passed in, the following error will be returned.

```
  "ERR12001": {
    "statusCode": 400,
    "code": "ERR12001",
    "message": "UNSUPPORTED_GRANT_TYPE",
    "description": "Unsupported grant type %s. Only authorization_code and client_credentials are supported."
  }
```

* When this endpoint is called, the client_id and client_secret must be base64 encoded and put
into Authorization header. If there is no Authorization header available in the request, the
following error will be returned.

```
  "ERR11017": {
    "statusCode": 400,
    "code": "ERR11017",
    "message": "VALIDATOR_REQUEST_PARAMETER_HEADER_MISSING",
    "description": "Header parameter '%s' is required on path '%s' but not found in request."
  }
```

* When validating client_id and client_secret, the first thing is to check the client_id in
the client cache in memory. If client_id doesn't exist, the following error will be returned.

```
  "ERR12014": {
    "statusCode": 404,
    "code": "ERR12014",
    "message": "CLIENT_NOT_FOUND",
    "description": "Client %s is not found."
  }
```

* The encoded client_id:client_secret combination will be decoded and then client secret will
be checked with hashed and salted in memory client secret to make sure it is correct. If the
client secret is not correct, the following error will be returned.

```
  "ERR12007": {
    "statusCode": 401,
    "code": "ERR12007",
    "message": "UNAUTHORIZED_CLIENT",
    "description": "Unauthorized client with wrong client secret."
  }
```

* If the basic client_id:client_secret cannot be decoded and parsed correctly, the following
error will be returned.

```
  "ERR12004": {
    "statusCode": 401,
    "code": "ERR12004",
    "message": "INVALID_BASIC_CREDENTIALS",
    "description": "Invalid Basic credentials %s."
  }
```

* If there is no Basic authorization header passed in, i.e. a bearer token is passed in, the
following error will be returned.

```
  "ERR12003": {
    "statusCode": 401,
    "code": "ERR12003",
    "message": "INVALID_AUTHORIZATION_HEADER",
    "description": "Invalid authorization header %s. Basic authentication with credentials is required."
  }
```

If all validation is passed, a JWT token will be generated and returned in a JSON object.
