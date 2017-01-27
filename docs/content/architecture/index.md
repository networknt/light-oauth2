---
date: 2016-12-31T21:16:50-05:00
title: Architecture
---

## Microservices

It is built on top of Light-Java framework as 7 microservices and each serivce has several
endpoints to support user login, access token retrieval, user registration, service 
registration, client registration and public key certificate distribution. It can support 
millions users and thousands of clients and services with scopes. It should be easily handle 
thousands of concurrent users per instance and each service can be scaled individually if 
necessary.

## In-Memory Data Grid

Hazelcast is used as Data Grid across multiple services and majority of operations
won't hit database server for best performance. This also makes database as plugin
so that persistence layer can be anything from SQL to NoSQL.


## Built-in Security

Except code, token and key services, other services are protected by OAuth2 itself and 
additional security as well. These sevices can be deployed at different locations within 
your network for maximum security and flexibility. 

## Multiple Database Support

Currently, Oralce, MySQL and Postgres are supported, but other databases(sql or nosql) 
can be easily supported by implementing a MapStore of Hazelcast and create a initial 
db script. 

## Easy to customize and integrate

Each service can be easily customized and won't impact other services. Also, it is very 
easy to extend in order to integrate with other existing services within your organization.

## Cloud and Docker friendly

Designed as native cloud services on top of light weight Java framework to lower the cost of
VM provisioning.

