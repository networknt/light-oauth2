---
date: 2017-01-01T15:14:13-05:00
title: Enterprise Edition Tutorial
---

Note: the following steps use Oracle database as an example. MySQL and Postgres should be the same
by choosing docker-compose-mysql.yml or docker-compose-postgres.yml when starting docker-compose.

# Start Services

In production mode, all services will have docker images downloaded from hub.docker.com or private
docker hub within your organization. And Kubernetes or other docker orchestration tools will be
used to manage containers. 

To help use to understand how each service work and enable user to modify services, the first section
of this tutorial will focus on development mode which will build these services and dockerize them. 
And start them as a docker compose. 

The following will check out the repo, build and start services with Oracle XE database.   

```
git clone git@github.com:networknt/light-oauth2.git
cd light-oauth2
git checkout enterprise
mvn clean install -DskipTests
docker-compose -f docker-compose-oracle.yml up
```

It will take about 30 seconds to have all services and database up and running. If Oracle XE image
doesn't exist on your host, it will take longer to download it.

If you have modified source code, please follow the steps to restart services. 
```
docker-compose -f docker-compose-oracle.yml down
mvn clean install
./cleanup.sh
docker-compose -f docker-compose-oracle.yml up
```


# Test Services

By default, the security is partially disabled on these services out of the box so that users
can easily test these services to learn how to use them. In later sections, there are steps to enable
all security with config files.

### Code

This is part of authorization flow that takes user's credentials and redirect back authorization
code to the webserver. 

There are two endpoints: /oauth2/code@get and /oauth2/code@post

The GET endpoint uses Basic Authorization and POST endpoint uses Form Authorization.

In most of the cases, you should use GET endpoint as it provides popup window on
the browser to ask username and password. And there is no need to create a login page
and error page.

POST endpoint is usually used when you want to have customized login page and error page to make 
sure users have the same experience as they browser other part of your web server. Browser will have 
a login form to collect user credentials and posts it to the OAuth2 server endpoint. Once the user
is authenticated, a authorization code is redirected back to the browser with a redirect URI passed
in from the request or the default redirect URI for the client will be used from client registration.
As you might guess, this endpoint requires customization most of the time on login page and error page.
Default login page and error page are provided as a starting points to make your customized pages.


There is only one admin user after the system is installed and the default password
is "123456". The password needs to be reset immediately with User Service for
production.  

To get authorization code put the following url into your browser.

```
http://localhost:6881/oauth2/code?response_type=code&client_id=f7d42348-c647-4efb-a52d-4c5787421e72&redirect_uri=http://localhost:8080/authorization
```

If this is the first time you hit this url on the browser, you will have a popup window for user
credentials. Now let's use admin/123456 to login given you haven't reset the password
yet for admin user.

Once authentication is completed, an authorization code will be redirect to your
browser. Something like the following.

```
http://localhost:8080/authorization?code=pVk10fdsTiiJ1HdUlV4y1g
```

If you want to call the get endpoint from your command line or script, you can put
the user credentials into the header in above command. Just make sure you have 
a server listening to the redirect uri you have specified. 

Here is a sample curl command.

```
curl -H "Authorization: Basic admin:123456" http://localhost:6881/oauth2/code?response_type=code&client_id=f7d42348-c647-4efb-a52d-4c5787421e72&redirect_uri=http://localhost:8080/authorization
``` 
If you want to try the above command line, you have to make sure that redirect_uri is alive. Otherwise,
you will have an error that doesn't make any sense.

### Token

This service has only one endpoint to get access token. It supports authorization
code grant type and client credentials grant type. 

Authorization Grant with authorization code redirected in the previous step. Please
replace code with the newly retrieved one as it is only valid for 10 minutes.
 
```
curl -H "Authorization: Basic f7d42348-c647-4efb-a52d-4c5787421e72:f6h1FTI8Q3-7UScPZDzfXA" -H "Content-Type: application/x-www-form-urlencoded" -X POST -d "grant_type=authorization_code&code=c0iAfPAeTk2BpiPWj-CYPQ" http://localhost:6882/oauth2/token
```

The above command will have the following output.

```
{"access_token":"eyJraWQiOiIxMDAiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTQ4MzIwOTQ4NiwianRpIjoib0dtdXEzSl85d0tlOUVIT2RWM21PUSIsImlhdCI6MTQ4MzIwODg4NiwibmJmIjoxNDgzMjA4NzY2LCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6ImFkbWluIiwidXNlcl90eXBlIjoiYWRtaW4iLCJjbGllbnRfaWQiOiJmN2Q0MjM0OC1jNjQ3LTRlZmItYTUyZC00YzU3ODc0MjFlNzIiLCJzY29wZSI6WyJwZXRzdG9yZS5yIiwicGV0c3RvcmUudyJdfQ.gQ5HI2drObxorsQvz86RYT5tgk7QCnEBm9zNod7SbC--v8s4OfFIM4FQbxGqlMzbU3_dDXiyMSGzOFD_ShZ5se9W2FLxLjbMmBJwQG89peymcdY2mTgQoKJMYxL602a7cloyuoDZ_l-OQSj6RMdgRw4FKmMdOqMKWauoh58faZqvHgGxk43hlKW4bBy4vqg2IhNsUm_vIf-SVAUAMqp0Birt94FfjM3QSCQfwHXfK1nCWjFvfRIoN6w7XrPDQtnZq_8Mhdv8dNwowDLoYayKoUpr7i84gFA11-J1gocJOALj1kYody6kU5CfMwGOSX90PUEmdVy_3WnyEAp3blC-Iw","token_type":"bearer","expires_in":600} 
```

Client Credentials grant doesn't need authorization code but only client_id and
client_secret. Here is the curl command line to get access token.

```
curl -H "Authorization: Basic f7d42348-c647-4efb-a52d-4c5787421e72:f6h1FTI8Q3-7UScPZDzfXA" -H "Content-Type: application/x-www-form-urlencoded" -X POST -d "grant_type=client_credentials" http://localhost:6882/oauth2/token
```

The above command will have the following output.

```
{"access_token":"eyJraWQiOiIxMDAiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTQ4MzIwOTc1NywianRpIjoiOVhWdGV2dXZ2cjMwQ0lnZVFuUTFUUSIsImlhdCI6MTQ4MzIwOTE1NywibmJmIjoxNDgzMjA5MDM3LCJ2ZXJzaW9uIjoiMS4wIiwiY2xpZW50X2lkIjoiZjdkNDIzNDgtYzY0Ny00ZWZiLWE1MmQtNGM1Nzg3NDIxZTcyIiwic2NvcGUiOlsicGV0c3RvcmUuciIsInBldHN0b3JlLnciXX0.C8oHgjKpaKWAYJvSqZ4_VT2sw8XXpABFq-aXgNUN2mCEKZJN7AkA6qio0fK4ZCTn5lT9bLou6SOEDV-uXvcU1_XlvKTTnbMO2g-s_7-O-xXxSCAXiLZ-5C7ieGt7enQrxrESUEsgr0Kow4a34GjxAod5j0vcKzhZ6vrcQcuCecPKaeovV0nkBZH2cGPhaLvK346RA9VjxITcR1DgzPWIO3AYJGaIrF8-mCA6Ad8LNi8mB0T5pHIST5fpVTsDYF3KjQJKYiwEhVMbfErBrsmiUUHJ7fYNi5ntLvT-61rupqrQeudl54gg4onct6rT9A2HmuV0iucECkwm9urJ2QxO-A","token_type":"bearer","expires_in":600}
```

If you are interested, you can compare the claims of above tokens at https://jwt.io/

### Service

OAuth2 is used to protect services and each service must register itself with scope in
order to have fine-grained access control. This microservice provides endpoint to add,
update, remove and query services. 

To add a new service.

```
curl -H "Content-Type: application/json" -X POST -d '{"serviceId":"AACT0003","serviceType":"ms","serviceName":"Retail Account","serviceDesc":"Microservices for Retail Account","scope":"act.r act.w","ownerId":"admin"}' http://localhost:6883/oauth2/service
```

To query all services.

```
curl http://localhost:6883/oauth2/service

```
And here is the result.

```
[{"serviceType":"ms","serviceDesc":"A microservice that serves account information","scope":"a.r b.r","serviceId":"AACT0001","serviceName":"Account Service","ownerId":"admin","updateDt":null,"createDt":"2016-12-31"},{"serviceType":"ms","serviceDesc":"Microservices for Retail Account","scope":"act.r act.w","serviceId":"AACT0003","serviceName":"Retail Account","ownerId":"admin","updateDt":null,"createDt":"2016-12-31"}]
```

To query a service with service id.

```
curl http://localhost:6883/oauth2/service/AACT0003

```
And here is the result.
```
{"serviceType":"ms","serviceDesc":"Microservices for Retail Account","scope":"act.r act.w","serviceId":"AACT0003","serviceName":"Retail Account","ownerId":"admin"}
```

To update above service type to "api".

```
curl -H "Content-Type: application/json" -X PUT -d '{"serviceType":"api","serviceDesc":"Microservices for Retail Account","scope":"act.r act.w","serviceId":"AACT0003","serviceName":"Retail Account","ownerId":"admin"}' http://localhost:6883/oauth2/service
```

To delete above service with service id.

```
curl -X DELETE http://localhost:6883/oauth2/service/AACT0003

```

### Client

Every client that accesses service(s) must register itself in order to get
access token during runtime. An entity might be a client and service at the
same time and in this case, it must register twice as client and service.

To add a new client.

```
curl -H "Content-Type: application/json" -X POST -d '{"clientType":"mobile","clientName":"AccountViewer","clientDesc":"Retail Online Banking Account Viewer","scope":"act.r act.w","redirectUri": "http://localhost:8080/authorization","ownerId":"admin"}' http://localhost:6884/oauth2/client
```

And here is the result with client_id and client_secret.

```
{"clientDesc":"Retail Online Banking Account Viewer","clientType":"mobile","redirectUri":"http://localhost:8080/authorization","clientId":"e24e7110-c39f-49f1-85eb-8434cb577482","clientName":"AccountViewer","scope":"act.r act.w","clientSecret":"YDJLse8SQRapHyoMsdPUig","ownerId":"admin","createDt":"2016-12-31"}
```

To query all clients.

```
curl http://localhost:6884/oauth2/client

```
And here is the result.

```
[{"clientDesc":"PetStore Web Server that calls PetStore API","clientId":"f7d42348-c647-4efb-a52d-4c5787421e72","clientType":"server","redirectUri":"http://localhost:8080/authorization","clientName":"PetStore Web Server","scope":"petstore.r petstore.w","ownerId":"admin","updateDt":null,"createDt":"2016-12-31","authenticateClass":null},{"clientDesc":"Retail Online Banking Account Viewer","clientId":"9ef89c7b-f17b-4a64-a24b-ce539ed80641","clientType":"mobile","redirectUri":"http://localhost:8080/authorization","clientName":"AccountViewer","scope":"act.r act.w","ownerId":"admin","updateDt":null,"createDt":"2016-12-31","authenticateClass":null}]
```

To query a client by id.

```
curl http://localhost:6884/oauth2/client/f7d42348-c647-4efb-a52d-4c5787421e72
```

And here is the result.

```
{"clientDesc":"PetStore Web Server that calls PetStore API","clientId":"f7d42348-c647-4efb-a52d-4c5787421e72","clientType":"server","redirectUri":"http://localhost:8080/authorization","clientName":"PetStore Web Server","scope":"petstore.r petstore.w","clientSecret":"f6h1FTI8Q3-7UScPZDzfXA","ownerId":"admin","updateDt":null,"createDt":"2016-12-31","authenticateClass":null}
```

To update a client with a shorter clientDesc.

```
curl -H "Content-Type: application/json" -X PUT -d '{"clientDesc":"PetStore Web Server","clientId":"f7d42348-c647-4efb-a52d-4c5787421e72","clientType":"server","redirectUri":"http://localhost:8080/authorization","clientName":"PetStore Web Server","scope":"petstore.r petstore.w","clientSecret":"f6h1FTI8Q3-7UScPZDzfXA","ownerId":"admin","updateDt":null,"createDt":"2016-12-31","authenticateClass":null}' http://localhost:6884/oauth2/client
```

To delete a client with client id.

```
curl -X DELETE http://localhost:6884/oauth2/client/9ef89c7b-f17b-4a64-a24b-ce539ed80641

```


### User

The OAuth2 services can be integrated with existing Active Directory, LDAP or customer
database for authentication. If there is no existing authentication service, you can
register users into database.

To add a new user.

```
curl -H "Content-Type: application/json" -X POST -d '{"userId":"stevehu","userType":"employee","firstName":"Steve","lastName":"Hu","email":"stevehu@gmail.com","password":"123456","passwordConfirm":"123456"}' http://localhost:6885/oauth2/user
```

To query a user.

```
curl http://localhost:6885/oauth2/user/stevehu

```

And here is the result.

```
{"firstName":"Steve","lastName":"Hu","userType":"employee","userId":"stevehu","email":"stevehu@gmail.com"}
```

To update the user type to partner.

```
curl -H "Content-Type: application/json" -X PUT -d '{"firstName":"Steve","lastName":"Hu","userType":"partner","userId":"stevehu","email":"stevehu@gmail.com"}' http://localhost:6885/oauth2/user
```

To reset the password.

```
curl -H "Content-Type: application/json" -X POST -d '{"password":"123456","newPassword":"stevehu","newPasswordConfirm":"stevehu"}' http://localhost:6885/oauth2/password/stevehu
```

To remove a user.

```
curl -X DELETE http://localhost:6885/oauth2/user/stevehu

```


### Key

Light-Java and Light-OAuth2 support distributed security verification and this
requires the JWT public key certificate to be distributed to all services. By
default, all services built on top of Light-Java will include a set of 
certificates. But how to distributed new certificates to thousands of running
services if certificates are renewed? There is no way we can copy certificates
to all the running containers as they are dynamic and new containers can be
started anytime by container orchestration tool. 

The traditional push approach is not working and a new way of pull certificates
from OAuth2 key service is implemented in Light-Java and Light-OAuth2.

This feature is tightly integrated with Light-Java and it should work seamlessly.

The first step to get certificate is to encode client_id:client_secret pair for
basic authentication. 

Here is the client_id:client_secret

```
f7d42348-c647-4efb-a52d-4c5787421e72:f6h1FTI8Q3-7UScPZDzfXA
```

Go to https://www.base64encode.org/ to encode it to

```
ZjdkNDIzNDgtYzY0Ny00ZWZiLWE1MmQtNGM1Nzg3NDIxZTcyOmY2aDFGVEk4UTMtN1VTY1BaRHpmWEE=
```

To get certificate by a key id.

```
curl -H "Authorization: Basic ZjdkNDIzNDgtYzY0Ny00ZWZiLWE1MmQtNGM1Nzg3NDIxZTcyOmY2aDFGVEk4UTMtN1VTY1BaRHpmWEE=" http://localhost:6886/oauth2/key/101
```

And here is the result.

```
-----BEGIN CERTIFICATE-----
MIIDkzCCAnugAwIBAgIEUBGbJDANBgkqhkiG9w0BAQsFADB6MQswCQYDVQQGEwJDQTEQMA4GA1UE
CBMHT250YXJpbzEQMA4GA1UEBxMHVG9yb250bzEmMCQGA1UEChMdTmV0d29yayBOZXcgVGVjaG5v
bG9naWVzIEluYy4xDDAKBgNVBAsTA0FQSTERMA8GA1UEAxMIU3RldmUgSHUwHhcNMTYwOTIyMjI1
OTIxWhcNMjYwODAxMjI1OTIxWjB6MQswCQYDVQQGEwJDQTEQMA4GA1UECBMHT250YXJpbzEQMA4G
A1UEBxMHVG9yb250bzEmMCQGA1UEChMdTmV0d29yayBOZXcgVGVjaG5vbG9naWVzIEluYy4xDDAK
BgNVBAsTA0FQSTERMA8GA1UEAxMIU3RldmUgSHUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEK
AoIBAQCqYfarFwug2DwpG/mmcW77OluaHVNsKEVJ/BptLp5suJAH/Z70SS5pwM4x2QwMOVO2ke8U
rsAws8allxcuKXrbpVt4evpO1Ly2sFwqB1bjN3+VMp6wcT+tSjzYdVGFpQAYHpeA+OLuoHtQyfpB
0KCveTEe3KAG33zXDNfGKTGmupZ3ZfmBLINoey/X13rY71ITt67AY78VHUKb+D53MBahCcjJ9YpJ
UHG+Sd3d4oeXiQcqJCBCVpD97awWARf8WYRIgU1xfCe06wQ3CzH3+GyfozLeu76Ni5PwE1tm7Dhg
EDSSZo5khmzVzo4G0T2sOeshePc5weZBNRHdHlJA0L0fAgMBAAGjITAfMB0GA1UdDgQWBBT9rnek
spnrFus5wTszjdzYgKll9TANBgkqhkiG9w0BAQsFAAOCAQEAT8udTfUGBgeWbN6ZAXRI64VsSJj5
1sNUN1GPDADLxZF6jArKU7LjBNXn9bG5VjJqlx8hQ1SNvi/t7FqBRCUt/3MxDmGZrVZqLY1kZ2e7
x+5RykbspA8neEUtU8sOr/NP3O5jBjU77EVec9hNNT5zwKLevZNL/Q5mfHoc4GrIAolQvi/5fEqC
8OMdOIWS6sERgjaeI4tXxQtHDcMo5PeLW0/7t5sgEsadZ+pkdeEMVTmLfgf97bpNNI7KF5uEbYnQ
NpwCT+NNC5ACmJmKidrfW23kml1C7vr7YzTevw9QuH/hN8l/Rh0fr+iPEVpgN6Zv00ymoKGmjuuW
owVmdKg/0w==
-----END CERTIFICATE-----
```

