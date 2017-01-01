---
date: 2016-12-31T21:20:22-05:00
title: Getting Started
---

## OAuth2 Introduction

If you are new to OAuth2, please read this [introduction](introduction/) document to get familiar with the concept. 

## Select the Right Edition

There are three editions of OAuth2 server and they should be used in different situations.

### Developerment Edition

This is the simplest OAuth2 server designed for development as it doesn't have any dependency. 
Client and User info is retrieved from configuration files which can be externalized
and modified. 

This server has two endpoints:

/oauth2/code      

/oauth2/token

The code endpoint is to authenticate user and provide authorization code redirect to
the user-agent. It can handle 60K requests per second user login to get auth code on
a core i7 laptop.

The token endpoint is to issue access token and other tokens. It can generate 700
private key signed JWT tokens per second on a core i7 laptop.

As client and user info are in JSON config files, it can only manage up to hundreds
of clients and users for development purpose.

Codebase can be accessed from dev branch and it is functional.


### Enterprise Edition


A microservices and database based OAuth2 server that have 6 services and numeric endpoints to 
support user login, access token, user registration, service registration, client 
registration and public key certificate distribution. It can support millions users 
and thousands of clients and services with scopes. It should be easily handle 
thousands of concurrent users per instance and each microservice can be scaled individually.

Enterprise edition is designed for production use and Specifications can be found at https://github.com/networknt/swagger

Three databases are supported:

Oracle - Codebase can be acccessed from oracle branch.

MySQL - Codebase can be accessed from mysql branch

Postgres - Codebase can be accessed from postgres branch


### Provider Edition

This is a microservices based OAuth2 server that built on top of light-eventuate
with event sourcing and CQRS. It is designed for OAuth2 service providers who want
ultimate scalability, maximum throughput and minimum latency. 

Codebase can be accessed from kafka branch and it is in planning phase.

Provider edition is used by service providers and it is yet to be implemented.

