---
date: 2016-12-31T21:20:22-05:00
title: Getting Started
---

## OAuth2 Introduction

If you are new to OAuth2, please read this [introduction](introduction/) document to get familiar 
with the concept. 

## Select the Right Edition

### Enterprise Edition


A microservices and database based OAuth2 server that have 7 services and numeric endpoints to 
support user login, access token, user registration, service registration, client 
registration and public key certificate distribution. It can support millions users 
and thousands of clients and services with scopes. It should be easily handle 
thousands of concurrent users per instance and each microservice can be scaled individually.

Enterprise edition is designed for production use and Specifications can be found at 
https://github.com/networknt/swagger

Three databases are supported: Oracle, Mysql and Postgres


### Provider Edition

This is a microservices based OAuth2 server that built on top of light-eventuate
with event sourcing and CQRS. It is designed for OAuth2 service providers who want
ultimate scalability, maximum throughput and minimum latency. 

Codebase can be accessed from kafka branch and it is in planning phase.

Provider edition is used by service providers and it is yet to be implemented.


## Build the server from source code

Note: as Oracle client is not available in public maven repo, you have to install it manually by
following this [link](https://dimitrisli.wordpress.com/2012/08/09/maven-install-ojdbc6/) to install
it before building the source code.

```
git clone git@github.com:networknt/light-oauth2.git
cd light-oauth2
mvn clean install
```

##
