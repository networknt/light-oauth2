---
date: 2017-01-01T09:37:32-05:00
title: Enterprise Edition
---

There are six standard services that cover standard OAuth2 grant flows and extended features like 
service on-boarding, client on-boarding, user management and public key certificate distribution.

This document only describe the features and processes of each service. Please refer to [tutorial](/tutorial/enterprise/)
on how to access these services. 


# Code

This is a service that accepts user credentials and redirects back authorization code with redirect
URL defined in the client registration or overwritten it by passing in a redirect URI in the request.

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
        description: "encoded username:password mandatory if Basic Authentication is used"
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
      - name: "redirect_uri"
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
      - name: "state"
        in: "query"
        description: "to prevent cross-site request forgery"
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
      - name: "redirect_uri"
        in: "formData"
        description: "Redirect Uri"
        required: false
        type: "string"
      - name: "state"
        in: "formData"
        description: "to prevent cross-site request forgery"
        required: false
        type: "string"
      responses:
        302:
          description: "Successful Operation"
```

## /oauth2/code@get

The get endpoint is the most used as it is very simple and supported by all browsers
without any customization. When request is received by the service, the following
validations or processes are done before issuing a authorization code redirect.

* Make sure that user credentials are passed in. Otherwise, it will redirect the
browser to display popup window to collect userId and password. The username and
password will be passed in as Basic Authorization header. The passed in userId
and password will be matched with cached password(which is hashed and salted). If
userId and password combination is not valid, then a brand new popup window will
be shown. Depending on browsers, an error message will be shown up after several
times of retries.

* If password is incorrect, then the following error will return.

```
  "ERR12016": {
    "statusCode": 401,
    "code": "ERR12016",
    "message": "INCORRECT_PASSWORD",
    "description": "Incorrect password."
  }
```

* Make sure that response_type and client_id are passed in as parameters. If not, 
the following error will be returned.

```
  "ERR11000": {
    "statusCode": 400,
    "code": "ERR11000",
    "message": "VALIDATOR_REQUEST_PARAMETER_QUERY_MISSING",
    "description": "Query parameter '%s' is required on path '%s' but not found in request."
  }
```

* If response_type doesn't equal "code" then the following error will return

```
  "ERR11002": {
    "statusCode": 400,
    "code": "ERR11002",
    "message": "VALIDATOR_REQUEST_PARAMETER_ENUM_INVALID",
    "description": "Value '%s' for parameter '%s' is not allowed. Allowed values are <%s>."
  }
```

* Make sure client_id passed in is valid again in memory client cache. If not, 
then the following error will be returned.

```
  "ERR12014": {
    "statusCode": 404,
    "code": "ERR12014",
    "message": "CLIENT_NOT_FOUND",
    "description": "Client %s is not found."
  }
```

* As you can see from the specification, there is an optional parameter called 
redirect_uri. If this parameter is passed in, it will be used to redirect the
authorization code. Otherwise, the default redirect_uri from client_id will be
used. This is retrieved from in memory client cache. The url is populated when
client is registered during on-boarding process.



## /oauth2/code@post

To be completed later

# Token

This is a post endpoint to get JSON web tokens. Currently, it support two different
access tokens: authorization code token(which has user_id and user_type) and client
credentials token which doesn't have user info in the claim.

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
      responses:
        200:
          description: "Successful Operation"
```

## /oauth2/token

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

# Service

Every micro service or API needs to register itself to OAuth2 server in order to control who
can access it. During the registration/on-boarding, a list of scopes defined in the OpenAPI
specification should be populated as well. This list of scopes will be used for client to
register scopes in order to access this particular service or API.

This service has several endpoints and listening to port 6883.

Here is the specification.

```
swagger: '2.0'

info:
  version: "1.0.0"
  title: OAuth2 Service Registration
  description: OAuth2 Service Registration microservices endpoints. 
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
  /oauth2/service:
    post:
      description: Return a service object
      operationId: createService
      parameters:
      - in: "body"
        name: "body"
        description: "Service object that needs to be added"
        required: true
        schema:
          $ref: "#/definitions/Service"      
      responses:
        200:
          description: Successful response
          schema:
            $ref: "#/definitions/Service"          
      security:
      - service_auth:
        - "oauth.service.w"
    put:
      description: Return the updated service
      operationId: updateService
      parameters:
      - in: "body"
        name: "body"
        description: "Service object that needs to be added"
        required: true
        schema:
          $ref: "#/definitions/Service"      
      responses:
        200:
          description: Successful response
          schema:
            $ref: "#/definitions/Service"          
      security:
      - service_auth:
        - "oauth.service.w"
    get:
      description: Return all services
      operationId: getAllService
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
      - name: "serviceId"
        in: "query"
        description: "Partial serviceId for filter"
        required: false
        type: "string"
      responses:
        200:
          description: "successful operation"
          schema:
            type: "array"
            items:
              $ref: "#/definitions/Service"
      security:
      - service_auth:
        - "oauth.service.r"
          
  /oauth2/service/{serviceId}:
    delete:
      description: Delete a service by Id
      operationId: deleteService
      parameters:
      - name: "serviceId"
        in: "path"
        description: "Service Id"
        required: true
        type: "string"
      responses:
        400:
          description: "Invalid serviceId supplied"
        404:
          description: "Service not found"
      security:
        - service_auth:
          - oauth.service.w
    get:
      description: Get a service by Id
      operationId: getService
      parameters:
      - name: "serviceId"
        in: "path"
        description: "Service Id"
        required: true
        type: "string"
      responses:
        200: 
          description: Successful response
          schema:
            $ref: "#/definitions/Service"          
        400:
          description: "Invalid serviceId supplied"
        404:
          description: "Service not found"
      security:
        - service_auth:
          - oauth.service.r
          - oauth.service.w

securityDefinitions:
  service_auth:
    type: "oauth2"
    authorizationUrl: "http://localhost:8888/oauth2/code"
    flow: "implicit"
    scopes:
      oauth.service.w: "write oauth service"
      oauth.service.r: "read oauth service"
definitions:
  Service:
    type: "object"
    required:
    - "serviceId"
    - "serviceName"
    - "serviceType"
    - "scope"
    properties:
      serviceId:
        type: "string"
        description: "a unique service id"
      serviceType:
        type: "string"
        description: "service type"
        enum:
        - "ms"
        - "api"
      serviceName:
        type: "string"
        description: "service name"
      serviceDesc:
        type: "string"
        description: "service description"
      ownerId:
        type: "string"
        description: "service owner userId"
      scope:
        type: "string"
        description: "service scopes separated by space"
      createDt:
        type: "string"
        format: "date-time"
        description: "create date time"
      updateDt:
        type: "string"
        format: "date-time"
        description: "update date time"
        

```

## /oauth2/service@get

This endpoint gets all the services from service with filter and sorted on 
serviceId. A page query parameter is mandatory and pageSize and serviceId filter
are optional.

* page 

Page number which must be specified. It starts with 1 and an empty list will
be returned if the page is greater than the last page.

* pageSize

Default pageSize is 10 and you can overwrite it with another number. Please don't
use a big number due to performance reason. 

* serviceId

This is the only filter available and it supports filter by start with a few characters.
For example, "serviceId=abc" means any serviceId starts with "abc". The result is also
sorted by serviceId in the pagination. 


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

## /oauth2/service@post

This endpoint is used to create a new service. The following validation will be performed
before a new service is created.

* If serviceId exists in the cache, then the following error will be returned.

```
  "ERR12018": {
    "statusCode": 400,
    "code": "ERR12018",
    "message": "SERVICE_ID_EXISTS",
    "description": "Service id %s exists."
  }
```

* If ownerId doesn't exist in user cache, then the following error will be returned.

```
  "ERR12013": {
    "statusCode": 404,
    "code": "ERR12013",
    "message": "USER_NOT_FOUND",
    "description": "User %s is not found."
  }
```


## /oauth2/service@put

This is the endpoint to update existing service. Before service is updated, the
following validation will be performed.

* If serviceId cannot be found in the service cache, then the following error will be
returned.

```
  "ERR12015": {
    "statusCode": 404,
    "code": "ERR12015",
    "message": "SERVICE_NOT_FOUND",
    "description": "Service %s is not found."
  }
```

* If ownerId doesn't exist in user cache, then the following error will be returned.

```
  "ERR12013": {
    "statusCode": 404,
    "code": "ERR12013",
    "message": "USER_NOT_FOUND",
    "description": "User %s is not found."
  }
```

## /oauth2/service/{serviceId}@delete

This endpoint is used to delete existing service. Before the service is deleted,
the following validations will be performed.

* If serviceId cannot be found in the service cache, then the following error will be
returned.

```
  "ERR12015": {
    "statusCode": 404,
    "code": "ERR12015",
    "message": "SERVICE_NOT_FOUND",
    "description": "Service %s is not found."
  }
```

## /oauth2/service/{serviceId}@get

This endpoint is used to get a particular service by serviceId. Before the service is
returned, the following validation will be performed.

* If serviceId cannot be found in the service cache, then the following error will be
returned.

```
  "ERR12015": {
    "statusCode": 404,
    "code": "ERR12015",
    "message": "SERVICE_NOT_FOUND",
    "description": "Service %s is not found."
  }
```


# Client

Before initiating the protocol, the client registers with the authorization server. The 
means through which the client registers with the authorization server are not defined
in OAuth 2.0 specification. 

As an extension, we have implemented client registration/on-boarding as a micro service
that exposes several endpoints. 

Before digging into the details of implementation, let's clarify some concepts about
client. 

## Client Type

OAuth defines two client types, based on their ability to authenticate securely with 
the authorization server (i.e., ability to maintain the confidentiality of their 
client credentials):

* confidential

Clients capable of maintaining the confidentiality of their credentials (e.g., client 
implemented on a secure server with restricted access to the client credentials), or 
capable of secure client authentication using other means.

* public

Clients incapable of maintaining the confidentiality of their credentials (e.g., 
clients executing on the device used by the resource owner, such as an installed 
native application or a web browser-based application), and incapable of secure 
client authentication via any other means.

Above are standard client types defined in the specification and we have added
another one to control which client can issue resource owner password credentials
grant request. 

* trusted

These clients are marked as trusted and they are the only clients that can issue
resource owner password credentials grant type. For API management team, please
make sure that trusted client is also confidential and the client and resource
must be deployed and managed by the same organization as this flow is not as
secure as authorization code and client credentials flows.


The client type designation is based on the authorization server's definition of 
secure authentication and its acceptable exposure levels of client credentials.  
The authorization server does not make assumptions about the client type.

A client may be implemented as a distributed set of components or services, each
with a different client type and security context (e.g., a distributed client with 
both a confidential server-based component and a public browser-based component).  
In this case, the client should register each component or service as a separate 
client.

In a microservices architecture, a service might call other services to fulfill
its request, in this case, it should register itself as a service and a client.
That means the owner needs to follow both service on-boarding and client on-boarding
processes.

## Client Profile

This specification has been designed around the following client profiles:

* web application (web server)

A web application is a confidential client running on a web server. Resource owners 
access the client via an HTML user interface rendered in a user-agent on the device 
used by the resource owner. The client credentials as well as any access token issued 
to the client are stored on the web server and are not exposed to or accessible by 
the resource owner.

* user-agent-based application (browser)
      
A user-agent-based application is a public client in which the client code is 
downloaded from a web server and executes within a user-agent (e.g., web browser) on 
the device used by the resource owner. Protocol data and credentials are easily 
accessible (and often visible) to the resource owner. Since such applications
reside within the user-agent, they can make seamless use of the user-agent 
capabilities when requesting authorization.

* native application (mobile)

A native application is a public client installed and executed on the device used by 
the resource owner. Protocol data and credentials are accessible to the resource owner.  
It is assumed that any client authentication credentials included in the application can 
be extracted. On the other hand, dynamically issued credentials such as access tokens or 
refresh tokens can receive an acceptable level of protection. At a minimum, these 
credentials are protected from hostile servers with which the application may interact.  
On some platforms, these credentials might be protected from other applications residing 
on the same device.

The specification only mentioned above client profiles and the following two profiles
are added in our OAuth 2.0 implementation.

* batch application (batch)

Batch jobs are very similar with web application but they are managed by enterprise
scheduler and executed in a projected environment. It is considered as confidential
client.

* service (service)

Services are usually protected as resources but in a microservices architecture, a
service can also be a client to call other services or resources. These services
normally running within light-weight containers in a secured environment. And they
are considered as confidential clients.

## Client Identifier

The authorization server issues the registered client a client identifier - a unique 
string representing the registration information provided by the client. The client 
identifier is not a secret; it is exposed to the resource owner and MUST NOT be used
alone for client authentication. The client identifier is unique to the authorization 
server. In our implementation, it is a UUID generated on the server. Here is an example:

```
f7d42348-c647-4efb-a52d-4c5787421e72
```
   
## Client Secret

Clients in possession of a client secret MAY use the HTTP Basic authentication scheme 
as defined in [RFC2617] to authenticate with the authorization server. The client 
identifier is encoded using the "application/x-www-form-urlencoded" encoding algorithm 
, and the encoded value is used as the username; the client secret is encoded using 
the same algorithm and used as the password. The authorization server supports the HTTP 
Basic authentication scheme for authenticating clients that were issued a client secret.

For example (with extra line breaks for display purposes only):

```
     Authorization: Basic czZCaGRSa3F0Mzo3RmpmcDBaQnIxS3REUmJuZlZkbUl3
```

   
## Other Authentication Methods
   
Currently we don't support other authentication method but we are open to support others
if there are request from our users.

## Unregistered Clients

Due to security reasons, all client must be registered before authenticated on the server.
Unregistered clients are not supported on this implementation.

## Client Micro Service

This service has several endpoints and listening to port 6884.

Here is the specification.

```
swagger: '2.0'

info:
  version: "1.0.0"
  title: OAuth2 Client Registration
  description: OAuth2 Client Registration microservices endpoints. 
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
  /oauth2/client:
    post:
      description: Return a client object
      operationId: createClient
      parameters:
      - in: "body"
        name: "body"
        description: "Client object that needs to be added"
        required: true
        schema:
          $ref: "#/definitions/Client"      
      responses:
        200:
          description: Successful response
          schema:
            $ref: "#/definitions/Client"          
      security:
      - client_auth:
        - "oauth.client.w"
    put:
      description: Return the updated client
      operationId: updateClient
      parameters:
      - in: "body"
        name: "body"
        description: "Client object that needs to be added"
        required: true
        schema:
          $ref: "#/definitions/Client"      
      responses:
        200:
          description: Successful response
          schema:
            $ref: "#/definitions/Client"          
      security:
      - client_auth:
        - "oauth.client.w"
    get:
      description: Return all clients
      operationId: getAllClient
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
      - name: "clientName"
        in: "query"
        description: "Partial clientName for filter"
        required: false
        type: "string"
      responses:
        200:
          description: "successful operation"
          schema:
            type: "array"
            items:
              $ref: "#/definitions/Client"
      security:
      - client_auth:
        - "oauth.client.r"
          
  /oauth2/client/{clientId}:
    delete:
      description: Delete a client by Id
      operationId: deleteClient
      parameters:
      - name: "clientId"
        in: "path"
        description: "Client Id"
        required: true
        type: "string"
      responses:
        400:
          description: "Invalid clientId supplied"
        404:
          description: "Client not found"
      security:
        - client_auth:
          - oauth.client.w
    get:
      description: Get a client by Id
      operationId: getClient
      parameters:
      - name: "clientId"
        in: "path"
        description: "Client Id"
        required: true
        type: "string"
      responses:
        200: 
          description: Successful response
          schema:
            $ref: "#/definitions/Client"          
        400:
          description: "Invalid clientId supplied"
        404:
          description: "Client not found"
      security:
        - client_auth:
          - oauth.client.r
          - oauth.client.w

securityDefinitions:
  client_auth:
    type: "oauth2"
    authorizationUrl: "http://localhost:8888/oauth2/code"
    flow: "implicit"
    scopes:
      oauth.client.w: "write oauth client"
      oauth.client.r: "read oauth client"
definitions:
  Client:
    type: "object"
    required:
    - clientType
    - clientProfile
    - clientName
    - clientDesc
    - ownerId
    - scope
    properties:
      clientId:
        type: "string"
        description: "a unique client id"
      clientSecret:
        type: "string"
        description: "client secret"
      clientType:
        type: "string"
        description: "client type"
        enum:
        - confidential
        - public
        - trusted
      clientProfile:
        type: "string"
        description: "client profile"
        enum:
        - webserver
        - browser
        - mobile
        - service
        - batch
      clientName:
        type: "string"
        description: "client name"
      clientDesc:
        type: "string"
        description: "client description"
      ownerId:
        type: "string"
        description: "client owner id"
      scope:
        type: "string"
        description: "client scope separated by space"
      redirectUri:
        type: "string"
        description: "redirect uri"
      createDt:
        type: "string"
        format: "date-time"
        description: "create date time"
      updateDt:
        type: "string"
        format: "date-time"
        description: "update date time"

```

### /oauth2/client/{clientId}@delete

This endpoint is used to delete existing client. The following validation will be
performed in the service.

* If clientId cannot be found in the in-memory grid, then the following error will
be returned.

```
  "ERR12014": {
    "statusCode": 404,
    "code": "ERR12014",
    "message": "CLIENT_NOT_FOUND",
    "description": "Client %s is not found."
  }
```

### /oauth2/client/{clientId}@get

This endpoint is used to get a particular client with clientId. The following
validation will be performed in the service.

* If clientId cannot be found in the in-memory grid, then the following error will
be returned.

```
  "ERR12014": {
    "statusCode": 404,
    "code": "ERR12014",
    "message": "CLIENT_NOT_FOUND",
    "description": "Client %s is not found."
  }
```


### /oauth2/client@get

This endpoint gets all the clients from client service with filter and sorted on 
clientName. A page query parameter is mandatory and pageSize and clientName filter
are optional.

* page 

Page number which must be specified. It starts with 1 and an empty list will
be returned if the page is greater than the last page.

* pageSize

Default pageSize is 10 and you can overwrite it with another number. Please don't
use a big number due to performance reason. 

* clientName

This is the only filter available and it supports filter by start with a few characters.
For example, "clientName=abc" means any clientName starts with "abc". The result is also
sorted by clientName in the pagination. 


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


### /oauth2/client@post

This endpoint is used to create a new client. This usually will be called from light-portal
and the following validations will be performed before a new client is added.

* Verify that clientId exist in the cache. The clientId is generated as a UUID so this
cannot be triggered. It is implemented this way just want to be in sync with other
services.

```
  "ERR12019": {
    "statusCode": 400,
    "code": "ERR12019",
    "message": "CLIENT_ID_EXISTS",
    "description": "Client id %s exists."
  }
```

* Verify that ownerId is in user cache in memory. If it doesn't exist, the following
error will be returned.

```
  "ERR12013": {
    "statusCode": 404,
    "code": "ERR12013",
    "message": "USER_NOT_FOUND",
    "description": "User %s is not found."
  }
```

* Make sure the clientType is from a list of valid values. If not, an error message
will be returned.

```
  "ERR11004": {
    "statusCode": 400,
    "code": "ERR11004",
    "message": "VALIDATOR_SCHEMA",
    "description": "Schema Validation Error - %s"
  }
```

* Make sure that clientProfile is from a list of valid values. If not, an error
message will be returned.

```
  "ERR11004": {
    "statusCode": 400,
    "code": "ERR11004",
    "message": "VALIDATOR_SCHEMA",
    "description": "Schema Validation Error - %s"
  }
```


### /oauth2/client@put

This endpoint is used to update an existing client. This usually will be called from 
light-portal and the following validations will be performed before a client is updated.

* Verify that clientId exist in the cache. If clientId doesn't existing the cache, an
error message will be returned.

```
  "ERR12014": {
    "statusCode": 404,
    "code": "ERR12014",
    "message": "CLIENT_NOT_FOUND",
    "description": "Client %s is not found."
  }
```

* Verify that ownerId is in user cache in memory. If it doesn't exist, the following
error will be returned.

```
  "ERR12013": {
    "statusCode": 404,
    "code": "ERR12013",
    "message": "USER_NOT_FOUND",
    "description": "User %s is not found."
  }
```

* Make sure the clientType is from a list of valid values. If not, an error message
will be returned.

```
  "ERR11004": {
    "statusCode": 400,
    "code": "ERR11004",
    "message": "VALIDATOR_SCHEMA",
    "description": "Schema Validation Error - %s"
  }
```

* Make sure that clientProfile is from a list of valid values. If not, an error
message will be returned.

```
  "ERR11004": {
    "statusCode": 400,
    "code": "ERR11004",
    "message": "VALIDATOR_SCHEMA",
    "description": "Schema Validation Error - %s"
  }
```


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


# Key

In microservices architecture, the traditional way of copying public key certificates
to hosts of services is not working. With container orchestration tool like Kubernetes
old containers can be shutdown and new container can be started at anytime. So the push
certificates to services has to be changed to pull certificates from OAuth2 server
instead. This service is designed to pull public key certificate based on keyId that is
in the JWT token header. It is tighly integrated with Light-Java framework security
component. 

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
