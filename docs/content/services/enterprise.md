---
date: 2017-01-01T09:37:32-05:00
title: Enterprise Edition
---

There are six standard services that cover standard OAuth2 grant flows and extended features like 
service on-boarding, client on-boarding, user management and public key certificate distribution.

## Code

This is a service that accepts user credentials and redirects back authorization code with redirect
URL defined in the client registration or overwritten it by passing in a redirect URL in the request.

There are two endpoints and the service default listening port is 6881. 

Here is the specification:

```
swagger: '2.0'

info:
  version: "1.0.0"
  title: OAuth2 Service Authorization Code
  description: OAuth2 Service that logs in user and provide authorization code. 
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
  /oauth2/code:
    get:
      description: Return 302 redirect with authorization code
      operationId: getAuthCode
      parameters:
      - name: "Authorization"
        in: "header"
        required: false
        type: "string"
      - name: "response_type"
        in: "query"
        description: "The response type for authorization code"
        required: true
        type: "string"
        enum:
        - "code"
      - name: "client_id"
        in: "query"
        description: "The client id for authorization code"
        required: true
        type: "string"
      - name: "redirect_url"
        in: "query"
        description: "The redirect uri for authorization code"
        required: false
        type: "string"
      - name: "username"
        in: "query"
        description: "The user name for authorization code"
        required: false
        type: "string"
      - name: "password"
        in: "query"
        description: "The password for authorization code in clear text"
        required: false
        type: "string"
      responses:
        302:
          description: "Successful Operation"

    post:
      description: Return 302 redirect with authorization code
      operationId: postAuthCode
      consumes:
      - "application/x-www-form-urlencoded"
      produces:
      - "application/json"
      parameters:
      - name: "j_username"
        in: "formData"
        description: "User name"
        required: true
        type: "string"
      - name: "j_password"
        in: "formData"
        description: "Password"
        required: true
        type: "string"
      - name: "response_type"
        in: "formData"
        description: "Response type"
        required: true
        type: "string"
        enum: 
        - "code"
      - name: "client_id"
        in: "formData"
        description: "Client Id"
        required: true
        type: "string"
      - name: "redirect_url"
        in: "formData"
        description: "Redirect Url"
        required: false
        type: "string"
      responses:
        302:
          description: "Successful Operation"

```

### /oauth2/code@get

The get is the most used endpoint as it is very simple and supported by all browsers. 

### /oauth2/code@post

To be completed later

## Token

## Client

## Service

## User

## Key

