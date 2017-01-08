The fastest, lightest and cloud/container ready OAuth 2.0 Authorization Server based on microservices 
architecture built on top of Light Java framework. 

[Developer Chat](https://gitter.im/networknt/light-oauth2) |
[Documentation](https://networknt.github.io/light-oauth2) |
[Contribution Guide](CONTRIBUTING.md) |

[![Build Status](https://travis-ci.org/networknt/light-oauth2.svg?branch=master)](https://travis-ci.org/networknt/light-oauth2)

## Why this OAuth 2.0 Authorization Server

### Fast and small memory footprint to lower production cost.

The Development Edition can support 60000 user login and get authorization code redirect
and can generate 700 access tokens per second on my laptop. 

The Enterprise Edition has 6 microservices and each service can be scaled individually.

### Seamlessly integration with Light-Java framework

* Built on top of Light-Java
* Light-Java Client and Security modules manages all the communication with OAuth2
* Support service onboarding from Light-Portal
* Support client onboarding from Light-Portal
* Support user management from Light-Portal
* Open sourced OpenAPI specifications for all microserivces

### Development Edition for API or Service development and integration

It has no dependency and everything is in memory with extenalized configuration for
users and clients.

### Public key certificate distribution

There is a key service with endpoint to retrieve public key certificate from microservices
during runtime. 

### OAuth2 server, portal and light Java to form ecosystem

[light-java](https://github.com/networknt/light-java) to build API

[light-oauth2](https://github.com/networknt/light-oauth2) to control API access

[light-portal](https://github.com/networknt/light-portal) to manage clients and APIs



