# undertow-server-oauth2
An OAuth2 service provider based on undertow-server and embedded json data for clients and users. This server is
designed for development only and it cannot be used on production.


# Key generation


```
keytool -genkey -keyalg RSA -alias selfsigned -keystore primary.jks -storepass password -validity 3600 -keysize 2048

keytool -export -alias selfsigned -keystore primary.jks -rfc -file primary.crt

```

# Long live JWT token for testing

The undertow-server-oauth2 contains two testing key pairs for testing only. Both private keys and public key certificates
can be found in resources/config/oauth folder. The same public key certificates are included in undertow-server so that
the server can verify any token issued by this oauth server.

Important note:
For your official deployment, please create key pair of your own or buy certificate from one of
the CAs.

The following is a token generated for petstore api with scope write:pets and read:pets

```
Bearer eyJraWQiOiIxMDAiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTc5MDAwMDU1MSwianRpIjoicHVEMEN2RWNFblZyQVEtNEtBNENKQSIsImlhdCI6MTQ3NDY0MDU1MSwibmJmIjoxNDc0NjQwNDMxLCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6InN0ZXZlIiwidXNlcl90eXBlIjoiRU1QTE9ZRUUiLCJjbGllbnRfaWQiOiJmN2Q0MjM0OC1jNjQ3LTRlZmItYTUyZC00YzU3ODc0MjFlNzIiLCJzY29wZSI6WyJ3cml0ZTpwZXRzIiwicmVhZDpwZXRzIl19.U9HPvFOxeyWD_zTxMWY8JI263Am81KNpgRDMdHX42epNk_pDpPv2_rYjDHMGWrUfVLQKKLXueKxJuMyCmbBHfCkJqkmb6__44Y2t8CMHcUPvtKpuoF-YbG-LzMDSC1weYwMsC9kq84raFPHN0LevdjwKCoPBNIeYO8Oc1M5klglWBMIONnpxQyE5lM6HF-S3B45Pg0jE6acQ9ha1IrrHvTN3IZHU7YgS4SkEaxdxkziRQJh6Ml_r8j5kkkjeij8G6cCjn4XSQ0L6J3iGXmeClnAEYkDmoZBpYb_RCcNRxEmNaqz-M6LHILqDZDunPKTb98rPhqHseJPppLDsAWaAZg
```



# Start a standalone server

Given you have JDK8 and Maven 3 installed.

```
git clone https://github.com/networknt/undertow-server-oauth2.git
cd undertow-server-oauth2
mvn install exec:exec

```

# Start a docker container



# Token endpoint
/oauth2/token can be used to get JWT access token. Here is one of the responses.


```
{"access_token":"eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTQ3MjgzNTE0NiwianRpIjoidko5NnZVWFVoTmd3a29OWkhHWnZHdyIsImlhdCI6MTQ3MjgzNDU0NiwibmJmIjoxNDcyODM0NDI2LCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6InN0ZXZlIiwidXNlcl90eXBlIjoiRU1QTE9ZRUUiLCJjbGllbnRfaWQiOiJhYWFhYWFhYS0xMjM0LTEyMzQtMTIzNC1iYmJiYmJiYiIsInNjb3BlIjpbImFwaS5yIiwiYXBpLnciXX0.ZAIUYASDUO_4g9hmWFNYy4Zg1oDg-m3nvIGJAU7zUaWs8wt_a8FSCfwsfzhEe1EBjajnvTzGkSYOi2gwkyDVLoXN0tAfgrbFCFrR-LtNV9KWy82-HF1sYzIgx6M0-7PigVHqIacjdKmPgsA4GmNiG5AoMjoCYllJaISOmdSu6z6SD2APhHBlJcZFuMDjCaX-TNfesW7cHzLrcppGIwwGSCMlt8KEvmQBOKizpWcsj2MhvQmvjhFr7v6yU1h6o1So3w1NCFDK421Qwx4Pcbew912dJ9dOOOdQ4IbmI3757VF88QeJbI8SgjzlMX3t6KPLtyBkGs9geAU40Ui7pjzROQ"}
```


# Code endpoint
/oauth2/code can be used to get authorization code. The code is redirect to the uri specified by the
client. Here is an example of redirected uri.

```
http://localhost:8080/oauth?code=Gp6GHT02SJ6G_-wyvaMNPw
```

# User login

When using authorization flow, the client application will redirect to authorization code endpoint on
OAuth2 server, the server will authenticate the user by poping up a login page. Please use the
following builtin credentials:

username: stevehu
password: 123456


# Admin interface

Not implemented yet. If you want to add new client or new user, please update clients.json and users.json
in config folder. Also, the config folder can be externalized for you standalone instance or docker
container instance.

# Further info

[Wiki - OAuth2 Introduction](https://github.com/networknt/undertow-server-oauth2/wiki/OAuth2-Introduction)
