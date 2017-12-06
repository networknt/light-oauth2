A fast, light weight and cloud native OAuth 2.0 Server based on microservices architecture 
built on top of light-4j and light-rest-4j frameworks. 

[Developer Chat](https://gitter.im/networknt/light-oauth2) |
[Documentation](https://doc.networknt.com/service/oauth/service/) |
[Contribution Guide](CONTRIBUTING.md) |

[![Build Status](https://travis-ci.org/networknt/light-oauth2.svg?branch=master)](https://travis-ci.org/networknt/light-oauth2)

## Why this OAuth 2.0 Authorization Server

### Fast and small memory footprint to lower production cost.

The authorization code service can support 60000 user login and get authorization code redirected in a second on a Mac Book Pro.
The token service can generate 700 JWT access tokens signed by PKI per second on a Mac Book Pro. 

It has 7 microservices connected with in-memory data grid and each service can be
scaled individually.


### More secure than other implementations

OAuth 2.0 is just a specification and a lot of details are in the individual
implementation. Our implementation has a lot of extensions and enhancements 
for additional security and prevent users making mistakes. For example, we
have added an additional client type called "trusted" and only this type of
client can issue resource owner password credentials grant type and any custom
grant types. Also, when expose your OAuth 2.0 services the Internet, you only
need to expose authorization code service for user login and token service for
client to get access token. Other services can only be accessed internal. 

### Seamlessly integration with Light-Java framework

* Built on top of light-4j and light-rest-4j
* light-4j client and security modules manages all the communication with OAuth2
* Support service on-boarding from Light-Portal
* Support client on-boarding from Light-Portal
* Support user management from Light-Portal
* Open sourced OpenAPI specifications for all microserivces

### Easy to integrate with your APIs or services

The OAuth2 services can be started in a docker compose for your local development
and can be managed by Kubernetes on official environment.

### Support multiple databases and can be extended and customized easily

Out of the box, it supports Mysql, Postgres and Oracle XE and H2 for unit tests. Other
databases can be easily added with configuration change in service.yml config file.


### Public key certificate distribution

With distributed security verification, JWT signature public key certificates must
but distributed to all resource servers. The traditional push approach is not
working with microservices architecture and pull approach is adopted. There is a 
key service with endpoint to retrieve public key certificate from microservices 
during runtime based on the key_id from JWT header. For more details on key service,
please refer to [key distribution][] 

### OAuth2 server, portal and light Java to form ecosystem

[light-java](https://github.com/networknt/light-java) to build API

[light-oauth2](https://github.com/networknt/light-oauth2) to control API access

[light-portal](https://github.com/networknt/light-portal) to manage clients and APIs


Along with the documentation on each [light-oauth2 service][], there are
[light-oauth2 tutorial][] to help user to get started.  

[key distribution]: https://doc.networknt.com/service/oauth/service/key/
[light-oauth2 service]: https://doc.networknt.com/service/oauth/service/
[light-oauth2 tutorial]: https://doc.networknt.com/tutorial/oauth/
