# OAuth 2 web console

This project was bootstrapped with [Create React App](https://github.com/facebook/create-react-app).

## Setup guide
1. Clone the source code using the command below

```
    git clone https://github.com/networknt/light-oauth2.git
```

2. Install dependencies

```
    cd light-oauth2/oauth2-console
    npm install
```

3. Run the console in development mode

```
    npm start
```

## ssl settings to avoid NET:ERR_CERT_AUTHORITY_INVALID

1. create keystore and truststore from local root

    + create local root certificate 
    ```
        openssl req -x509 -out localhost.crt -keyout localhost.key \
            -newkey rsa:2048 -nodes -sha256 -days 3650 \
            -subj '/CN=localhost' -extensions EXT -config <( \
            printf "[dn]\nCN=localhost\n[req]\ndistinguished_name = dn\n[EXT]\nsubjectAltName=DNS:localhost\nkeyUsage=digitalSignature\nextendedKeyUsage=serverAuth")
    ```

    + create keystore
    ```
        openssl pkcs12 -export -in localhost.crt -inkey localhost.key -out localhost.p12 -name "localhost_key" 
        keytool -importkeystore -destkeystore server.keystore -deststorepass password -srckeystore localhost.p12 -srcstoretype PKCS12 -srcstorepass password
    ```

    + create truststore
    ```
        keytool -import -file localhost.crt -keystore server.truststore -storepass password
    ```

    + copy the generated keystore and truststore files to the server config folder (override existing) and then restart the server

2. install root certificate to your os

For Chrome, this can be done via `Settings>Advanced>Manage certificates`. From the popup dialog, you can import the `localhost.crt` generated above into `Login>Certificates`. You can also download the certificate (.crt) file from the browser.
After the installation, please set the certificate as `Always Trust` and then restart Chrome.
