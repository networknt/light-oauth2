---
date: 2017-09-19T21:07:30-04:00
title: Refresh Token
---

# Refresh Token

Refresh Token is issued in Authorization Code Grant and Resource Owner Password Credentials 
Grant along with access token. Also, for maximum security, a refresh token is issued every
time the old refresh token is used to renew an access token. 


This service is listening to port number 6886.

Here is the specification


```
swagger: '2.0'

info:
  version: "1.0.0"
  title: OAuth2 Refresh Token Management
  description: OAuth2 refresh token management microservices endpoints. 
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
  - application/json
produces:
  - application/json

paths:
  /oauth2/refresh_token:
    get:
      description: Return all refresh tokens
      operationId: getAllRefreshToken
      parameters:
      - name: "page"
        in: "query"
        description: "Page number"
        required: true
        type: "integer"
        format: "int32"
      - name: "pageSize"
        in: "query"
        description: "Pag size"
        required: false
        type: "integer"
        format: "int32"
      - name: "userId"
        in: "query"
        description: "Partial userId for filter"
        required: false
        type: "string"
      responses:
        200:
          description: "successful operation"
          schema:
            type: "array"
            items:
              $ref: "#/definitions/RefreshToken"
      security:
      - refresh_token_auth:
        - "oauth.refresh_token.r"
          
  /oauth2/refresh_token/{refreshToken}:
    delete:
      description: Delete a refresh token
      operationId: deleteRefreshToken
      parameters:
      - name: "refreshToken"
        in: "path"
        description: "Refresh Token"
        required: true
        type: "string"
      responses:
        400:
          description: "Invalid refresh token supplied"
        404:
          description: "Refresh token not found"
      security:
        - refresh_token_auth:
          - oauth.refresh_token.w
    get:
      description: Get a refresh token
      operationId: getRefreshToken
      parameters:
      - name: "refreshToken"
        in: "path"
        description: "Refresh token"
        required: true
        type: "string"
      responses:
        200: 
          description: Successful response
          schema:
            $ref: "#/definitions/RefreshToken"          
        400:
          description: "Invalid refresh token supplied"
        404:
          description: "Refresh token not found"
      security:
        - refresh_token_auth:
          - oauth.refresh_token.r
          - oauth.refresh_token.w

securityDefinitions:
  refresh_token_auth:
    type: "oauth2"
    authorizationUrl: "http://localhost:8888/oauth2/code"
    flow: "implicit"
    scopes:
      oauth.refresh_token.w: "write oauth refresh token"
      oauth.refresh_token.r: "read oauth refresh token"
definitions:
  RefreshToken:
    type: "object"
    required:
    - "refreshToken"
    - "userId"
    - "clientId"
    properties:
      refreshToken:
        type: "string"
        description: "refresh token"
      userId:
        type: "string"
        description: "user id"
      clientId:
        type: "string"
        description: "client id"
      scope:
        type: "string"
        description: "service scopes separated by space"

```

## Implementation

### /oauth2/refresh_token@get

This endpoint gets all the issued refresh tokens with filter and sorted on 
userId. A page query parameter is mandatory. pageSize and userId filter
are optional.

* page 

Page number which must be specified. It starts with 1 and an empty list will
be returned if the page is greater than the last page.

* pageSize

Default pageSize is 10 and you can overwrite it with another number. Please don't
use a big number due to performance reason. 

* userId

This is the only filter available and it supports filter by start with a few characters.
For example, "userId=abc" means any userId starts with "abc". The result is also
sorted by userId in the pagination. 


The following validation will be performed in the service.

* If page is missing from the query parameter, an error will be returned.

```
  "ERR11000": {
    "statusCode": 400,
    "code": "ERR11000",
    "message": "VALIDATOR_REQUEST_PARAMETER_QUERY_MISSING",
    "description": "Query parameter '%s' is required on path '%s' but not found in request."
  }
```


### /oauth2/refresh_token/{refreshToken}@delete

This endpoint is used to revoke a refresh token. It removes the refresh token from
in memory data grid when calling this endpoint. On the user interface, please make 
sure the operator confirms the action before submitting the request to the service.

Before the refresh token is deleted, the following validation will be performed. 

* If refresh token doesn't exist in memory, then the following error will be 
returned.

```
  "ERR12029": {
    "statusCode": 404,
    "code": "ERR12029",
    "message": "REFRESH_TOKEN_NOT_FOUND",
    "description": "Refresh token %s is not found."
  }
```


### /oauth2/refresh_token/{refreshToken}

This is the endpoint to get a particular refresh token information. The server 
will perform the following validations before the refresh token object is returned 
to the consumer.

* If refresh token doesn't exist in cache, then the following error will be returned.

```
  "ERR12029": {
    "statusCode": 404,
    "code": "ERR12029",
    "message": "REFRESH_TOKEN_NOT_FOUND",
    "description": "Refresh token %s is not found."
  }
```

