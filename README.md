# undertow-server-oauth2
An OAuth2 service provider based on undertow-server and embedded orientdb


# Key generation


```
keytool -genkey -keyalg RSA -alias selfsigned -keystore primary.jks -storepass password -validity 3600 -keysize 2048

keytool -export -alias selfsigned -keystore primary.jks -rfc -file primary.crt

```

# Long live JWT token for testing

The undertow-server-oauth2 contains a testing key pair for testing only. Both private key and public key certificate
can be found in resources/config/oauth folder. The same public key certificate is included in undertow-server so that
the server can verify any token issued by this oauth server.

Important note:
For your official deployment, please create key pair of your own or buy certificate from one of
the CAs.


```
Bearer eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTc4ODEzMjczNSwianRpIjoiNWtyM2ZWOHJaelBZNEJrSnNYZzFpQSIsImlhdCI6MTQ3Mjc3MjczNSwibmJmIjoxNDcyNzcyNjE1LCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6InN0ZXZlIiwidXNlcl90eXBlIjoiRU1QTE9ZRUUiLCJjbGllbnRfaWQiOiJkZGNhZjBiYS0xMTMxLTIyMzItMzMxMy1kNmYyNzUzZjI1ZGMiLCJzY29wZSI6WyJhcGkuciIsImFwaS53Il19.gteJiy1uao8HLeWRljpZxHWUgQfofwmnFP-zv3EPUyXjyCOy3xclnfeTnTE39j8PgBwdFASPcDLLk1YfZJbsU6pLlmYXLtdpHDBsVmIRuch6LFPCVQ3JdqSQVci59OhSK0bBThGWqCD3UzDI_OnX4IVCAahcT9Bu94m5u_H_JNmwDf1XaP3Lt4I34buYMuRD9stchsnZi-tuIRkL13FARm1XA9aPZUMUXFdedBWDXo1zMREQ_qCJXOpaZDJM9Im0rIkq9wTEVU00pbRp_Vcdya3dfkFteBMHiwFVt6VNQaco5BXURDAIzXidwQxNEbX1ek03wra8AIani65ZK7fy_w
```



# Start the server

mvn exec:exec


# Token endpoint
/oauth/token can be used to get JWT access token. Here is one of the responses.

curl localhost:8888/oauth/token


```
{"access_token":"eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTQ3MjgzNTE0NiwianRpIjoidko5NnZVWFVoTmd3a29OWkhHWnZHdyIsImlhdCI6MTQ3MjgzNDU0NiwibmJmIjoxNDcyODM0NDI2LCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6InN0ZXZlIiwidXNlcl90eXBlIjoiRU1QTE9ZRUUiLCJjbGllbnRfaWQiOiJhYWFhYWFhYS0xMjM0LTEyMzQtMTIzNC1iYmJiYmJiYiIsInNjb3BlIjpbImFwaS5yIiwiYXBpLnciXX0.ZAIUYASDUO_4g9hmWFNYy4Zg1oDg-m3nvIGJAU7zUaWs8wt_a8FSCfwsfzhEe1EBjajnvTzGkSYOi2gwkyDVLoXN0tAfgrbFCFrR-LtNV9KWy82-HF1sYzIgx6M0-7PigVHqIacjdKmPgsA4GmNiG5AoMjoCYllJaISOmdSu6z6SD2APhHBlJcZFuMDjCaX-TNfesW7cHzLrcppGIwwGSCMlt8KEvmQBOKizpWcsj2MhvQmvjhFr7v6yU1h6o1So3w1NCFDK421Qwx4Pcbew912dJ9dOOOdQ4IbmI3757VF88QeJbI8SgjzlMX3t6KPLtyBkGs9geAU40Ui7pjzROQ"}
```


# Code endpoint
Not implemented yet.


# User login


# Admin interface

