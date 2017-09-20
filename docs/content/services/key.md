---
date: 2017-09-19T21:06:21-04:00
title: Key Distribution
---

# Key

In microservices architecture, the traditional way of copying public key certificates
to hosts of services is not working. With container orchestration tool like Kubernetes
old containers can be shutdown and new container can be started at anytime. So the push
certificates to services has to be changed to pull certificates from OAuth2 server
instead. This service is designed to pull public key certificate based on keyId that is
in the JWT token header. It is tightly integrated with light-4j framework security
component.

For more information on how light-4j security module integrates with this service, please
refer to [key distribution](https://networknt.github.io/light-4j/architecture/key-distribution/) 

This service is listening to port number 6886.

Here is the specification

```
swagger: '2.0'

info:
  version: "1.0.0"
  title: OAuth2 Key Service
  description: OAuth2 Key Service microservices endpoints. 
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
  /oauth2/key/{keyId}:
    get:
      description: Get a key by Id
      operationId: getKeyById
      parameters:
      - name: "keyId"
        in: "path"
        description: "Key Id"
        required: true
        type: "string"
      responses:
        200: 
          description: Successful response
          schema:
            $ref: "#/definitions/Key"          
        400:
          description: "Invalid keyId supplied"
        404:
          description: "Key not found"
      security:
        - key_auth:
          - oauth.key.r
          - oauth.key.w

securityDefinitions:
  key_auth:
    type: "oauth2"
    authorizationUrl: "http://localhost:8888/oauth2/code"
    flow: "implicit"
    scopes:
      oauth.key.w: "write key"
      oauth.key.r: "read key"
definitions:
  Key:
    type: "object"
    required:
    - "keyId"
    - "certificate"
    properties:
      keyId:
        type: "string"
        description: "a unique id"
      certificate:
        type: "string"
        description: "certificate"

```

## /oauth2/key/{keyId}@get

This endpoint is used to get public key certificate for JWT signature verification based
on keyId in the JWT header. Light-Java framework should have packaged with several keys
already when deployed to production, however, keys are changing frequently as old ones
are expired. You don't want to redeploy your services just due to key changes on the
OAuth server. This endpoint is available for all services which have an entry in client
table so that clientId and clientSecret can be used to verify the identity of the service.

The following validations are performed before the key is issued by the service.

* If authorization header doesn't exist in the request, the following error will be
returned.

```
  "ERR12002": {
    "statusCode": 401,
    "code": "ERR12002",
    "message": "MISSING_AUTHORIZATION_HEADER",
    "description": "Missing authorization header. client credentials must be passed in as Authorization header."
  }
```

* If the client secret is not correct when matching with hashed and salted client secret
in cache, then the following error will be returned.

```
  "ERR12007": {
    "statusCode": 401,
    "code": "ERR12007",
    "message": "UNAUTHORIZED_CLIENT",
    "description": "Unauthorized client with wrong client secret."
  }
```

* If the client id in the authorization header doesn't exist in client cache in memory,
then the following error will be returned.

```
  "ERR12014": {
    "statusCode": 404,
    "code": "ERR12014",
    "message": "CLIENT_NOT_FOUND",
    "description": "Client %s is not found."
  }
```

* If the keyId cannot be found on the server, then the following error will be returned.

```
  "ERR10010": {
    "statusCode": 500,
    "code": "ERR10010",
    "message": "RUNTIME_EXCEPTION",
    "description": "Unexpected runtime exception"
  }
```
