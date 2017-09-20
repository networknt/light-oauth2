---
date: 2017-09-19T21:05:10-04:00
title: User Management
---

# User

This is a service for user registration and management. The OAuth server supports
integration with other user management system like active directory or LDAP.
However, for most enterprise customers, their customer information normally will
be in database. This service provides a database table for user management and
several endpoints to manage users. 

In OAuth 2.0 specification, user is normally called resource owner. 

## User Type

Currently there are three user types to support. 

* customer
* partner
* employee

## User Id

User id must be unique within the system.

## Password

Password is provided when registering and it is hashed and salted in persistence
layer. 

## User Micro Service

This service has several endpoints and listening to port 6885.

Here is the specification.

```
swagger: '2.0'

info:
  version: "1.0.0"
  title: OAuth2 User Service
  description: OAuth2 User Service microservices endpoints. 
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
  /oauth2/user:
    get:
      description: Return all users
      operationId: getAllUsers
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
              $ref: "#/definitions/User"
      security:
      - user_auth:
        - "oauth.user.r"
    post:
      description: Return a user object
      operationId: createUser
      parameters:
      - in: "body"
        name: "body"
        description: "User object that needs to be added"
        required: true
        schema:
          $ref: "#/definitions/User"      
      responses:
        200:
          description: Successful response
      security:
      - user_auth:
        - "oauth.user.w"
    put:
      description: Return the updated user
      operationId: updateUser
      parameters:
      - in: "body"
        name: "body"
        description: "User object that needs to be added"
        required: true
        schema:
          $ref: "#/definitions/User"      
      responses:
        200:
          description: Successful response
      security:
      - user_auth:
        - "oauth.user.w"
          
  /oauth2/user/{userId}:
    delete:
      description: Delete a user by Id
      operationId: deleteUser
      parameters:
      - name: "userId"
        in: "path"
        description: "User Id"
        required: true
        type: "string"
      responses:
        400:
          description: "Invalid userId supplied"
        404:
          description: "User not found"
      security:
        - user_auth:
          - oauth.user.w
    get:
      description: Get a user by Id
      operationId: getUser
      parameters:
      - name: "userId"
        in: "path"
        description: "User Id"
        required: true
        type: "string"
      responses:
        200: 
          description: Successful response
          schema:
            $ref: "#/definitions/User"          
        400:
          description: "Invalid userId supplied"
        404:
          description: "User not found"
      security:
        - user_auth:
          - oauth.user.r
          - oauth.user.w
  /oauth2/password/{userId}:
    post:
      description: Reset Password
      operationId: resetPassword
      parameters:
      - in: "body"
        name: "body"
        description: "Password object that needs to be added"
        required: true
        schema:
          $ref: "#/definitions/Password"      
      - name: "userId"
        in: "path"
        description: "User Id"
        required: true
        type: "string"
      responses:
        404:
          description: "User not found"
      security:
      - user_auth:
        - "oauth.user.w"
          
securityDefinitions:
  user_auth:
    type: "oauth2"
    authorizationUrl: "http://localhost:8888/oauth2/code"
    flow: "implicit"
    scopes:
      oauth.user.w: "write user"
      oauth.user.r: "read user"
definitions:
  User:
    type: "object"
    required:
    - "userId"
    - "userType"
    - "firstName"
    - "lastName"
    - "email"
    properties:
      userId:
        type: "string"
        description: "a unique id"
      userType:
        type: "string"
        description: "user type"
        enum:
        - "admin"
        - "employee"
        - "customer"
        - "partner"
      firstName:
        type: "string"
        description: "first name"
      lastName:
        type: "string"
        description: "last name"
      email:
        type: "string"
        description: "email address"
      password:
        type: "string"
        format: "password"
        description: "password"
      passwordConfirm:
        type: "string"
        format: "password"
        description: "password confirm"
      createDt:
        type: "string"
        format: "date-time"
        description: "create date time"
      updateDt:
        type: "string"
        format: "date-time"
        description: "update date time"
  Password:
    type: "object"
    required:
    - "password"
    - "newPassword"
    - "newPasswordConfirm"
    properties:
      password:
        type: "string"
        format: "password"
        description: "existing password"
      newPassword:
        type: "string"
        format: "password"
        description: "new password"
      newPasswordConfirm:
        type: "string"
        format: "password"
        description: "new password confirm"

```

## /oauth2/password/{userId}@post

This endpoint is used to reset user password. The user has to provide existing password,
the new password and password confirmation in order to reset it.

There are several validations need to be performed before the password can be reset.

* If userId cannot be found in the user cache, then the following error will be returned.

```
  "ERR12013": {
    "statusCode": 404,
    "code": "ERR12013",
    "message": "USER_NOT_FOUND",
    "description": "User %s is not found."
  }
```

* If password is not matched the cached password, then the following error will be returned.

```
  "ERR12016": {
    "statusCode": 401,
    "code": "ERR12016",
    "message": "INCORRECT_PASSWORD",
    "description": "Incorrect password."
  }
```

* If new password and password confirmation are not matched, then the following error
will be returned.

```
  "ERR12012": {
    "statusCode": 400,
    "code": "ERR12012",
    "message": "PASSWORD_PASSWORDCONFIRM_NOT_MATCH",
    "description": "Password %s and PasswordConfirm %s are not matched."
  }
```

## /oauth2/user@get

This endpoint gets all the users from user service with filter and sorted on 
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


## /oauth2/user@post

This endpoint is used to create a new user. The following validations will be performed
before a new user will be added.

* If the userId exists in the system, then the following error will be returned.

```
  "ERR12020": {
    "statusCode": 400,
    "code": "ERR12020",
    "message": "USER_ID_EXISTS",
    "description": "User id %s exists."
  }
```

* If the email exists in the system, then the following error will be returned.

```
  "ERR12021": {
    "statusCode": 400,
    "code": "ERR12021",
    "message": "EMAIL_EXISTS",
    "description": "Email %s exists."
  }
```

* If new password and password confirmation are not matched, the following error 
will be returned.

```
  "ERR12012": {
    "statusCode": 400,
    "code": "ERR12012",
    "message": "PASSWORD_PASSWORDCONFIRM_NOT_MATCH",
    "description": "Password %s and PasswordConfirm %s are not matched."
  }
```

* If password or password confirmation is empty, the following error will be returned.

```
  "ERR12011": {
    "statusCode": 400,
    "code": "ERR12011",
    "message": "PASSWORD_OR_PASSWORDCONFIRM_EMPTY",
    "description": "Password %s or PasswordConfirm %s is empty."
  }
```


## /oauth2/user@put

This is the endpoint to update existing user. Most of the attributes of users can be
updated through this endpoint except password. You must use password reset endpoint
to update password. 

The following validations will be performed before the user will be updated.

* If userId cannot be found in cache, then the following error will be returned.

```
  "ERR12013": {
    "statusCode": 404,
    "code": "ERR12013",
    "message": "USER_NOT_FOUND",
    "description": "User %s is not found."
  }
```


## /oauth2/user/{userId}@delete

This endpoint is used to delete an existing user. It removes the user physically so
be careful when calling this endpoint. On the user interface, please make sure the
operator confirms the action before submitting the request to the service.

Before the user is deleted, the following validation will be performed. 

* If userId doesn't exist in memory, then the following error will be returned.

```
  "ERR12013": {
    "statusCode": 404,
    "code": "ERR12013",
    "message": "USER_NOT_FOUND",
    "description": "User %s is not found."
  }
```

## /oauth2/user/{userId}get

This is the endpoint to get a particular user with userId. The server will perform
the following validations before the user object is returned to the consumer.

* If userId doesn't exist in cache, then the following error will be returned.

```
  "ERR12013": {
    "statusCode": 404,
    "code": "ERR12013",
    "message": "USER_NOT_FOUND",
    "description": "User %s is not found."
  }
```

