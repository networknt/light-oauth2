# light-oauth2
An OAuth2 service provider based on [light-java](https://github.com/networknt/light-java) and embedded json data for clients and users. This server is
designed for development only and it cannot be used on production.


# Key generation


```
keytool -genkey -keyalg RSA -alias selfsigned -keystore primary.jks -storepass password -validity 3600 -keysize 2048

keytool -export -alias selfsigned -keystore primary.jks -rfc -file primary.crt

```

# Long live JWT token for testing

The light-oauth2 contains two testing key pairs for testing only. Both private keys and public key certificates
can be found in resources/config/oauth folder. The same public key certificates are included in light-java so that
the server can verify any token issued by this oauth server.

Important note:
For your official deployment, please create key pair of your own or buy certificate from one of
the CAs.

The following is a token generated for petstore api with scope write:pets and read:pets

```
Bearer eyJraWQiOiIxMDAiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTc5MDAzNTcwOSwianRpIjoiSTJnSmdBSHN6NzJEV2JWdUFMdUU2QSIsImlhdCI6MTQ3NDY3NTcwOSwibmJmIjoxNDc0Njc1NTg5LCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6InN0ZXZlIiwidXNlcl90eXBlIjoiRU1QTE9ZRUUiLCJjbGllbnRfaWQiOiJmN2Q0MjM0OC1jNjQ3LTRlZmItYTUyZC00YzU3ODc0MjFlNzIiLCJzY29wZSI6WyJ3cml0ZTpwZXRzIiwicmVhZDpwZXRzIl19.mue6eh70kGS3Nt2BCYz7ViqwO7lh_4JSFwcHYdJMY6VfgKTHhsIGKq2uEDt3zwT56JFAePwAxENMGUTGvgceVneQzyfQsJeVGbqw55E9IfM_uSM-YcHwTfR7eSLExN4pbqzVDI353sSOvXxA98ZtJlUZKgXNE1Ngun3XFORCRIB_eH8B0FY_nT_D1Dq2WJrR-re-fbR6_va95vwoUdCofLRa4IpDfXXx19ZlAtfiVO44nw6CS8O87eGfAm7rCMZIzkWlCOFWjNHnCeRsh7CVdEH34LF-B48beiG5lM7h4N12-EME8_VDefgMjZ8eqs1ICvJMxdIut58oYbdnkwTjkA
```



# Start a standalone server

Given you have JDK8 and Maven 3 installed.

```
git clone https://github.com/networknt/light-oauth2.git
cd light-oauth2
mvn install exec:exec

```

In order to start the server with externalized configuration.

```
java -jar -D target/oauth2
```

# Start a docker container

with default configuration

```
docker run -d -p 8888:8888 networknt/oauth2-server
```

with externalized configuration in /home/steve/tmp/config/oauth2 folder
```
docker run -d -v /home/steve/tmp/config/oauth2:/config -p 8888:8888 networknt/oauth2-server
```

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

[Wiki - OAuth2 Introduction](https://github.com/networknt/light-oauth2/wiki/OAuth2-Introduction)


How to start the oauth2 server as standalone Java application

https://youtu.be/MZfRH-AAzWU

How to start the oauth2 server in docker container

https://youtu.be/w0a8f0hJVmU

How to customize the oauth2 server

https://youtu.be/eq1BxjDFg6o

