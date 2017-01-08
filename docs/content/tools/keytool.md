---
date: 2017-01-08T15:50:36-05:00
title: keytool
---

In production environment, it is recommended to buy certificates to sign the JWT
token; however, most people will use self-signed certificate on non-production
environment. Java Keytool is a very convenient tool to generate key pair and public
key certificate.

Here is an example to generate key pair and public key certificate in two steps.


```
keytool -genkey -keyalg RSA -alias selfsigned -keystore primary.jks -storepass password -validity 3600 -keysize 2048

keytool -export -alias selfsigned -keystore primary.jks -rfc -file primary.crt

```
