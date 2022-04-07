package com.networknt.oauth.key.handler;

import com.networknt.config.Config;
import com.networknt.handler.LightHttpHandler;
import com.networknt.http.RequestEntity;
import com.networknt.http.ResponseEntity;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.keys.RsaKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Get jwks from the primary.crt and secondary.crt file as this is the standard way to
 * support jwk resolver in the security.yml and its framework specific security config.
 *
 * @author Steve Hu
 */
public class Oauth2KeysGetHandler implements LightHttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(Oauth2KeysGetHandler.class);
    public static final String CONFIG_SECURITY = "openapi-security";
    public static final String CONFIG_JWT = "jwt";
    public static final String CONFIG_CERTIFICATE = "certificate";

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> config = Config.getInstance().getJsonMapConfig(CONFIG_SECURITY);
        Map<String, Object> jwtConfig = (Map<String, Object>)config.get(CONFIG_JWT);
        Map<String, Object> certificateConfig = (Map<String, Object>)jwtConfig.get(CONFIG_CERTIFICATE);
        // add all the JWK objects to a list
        List<JsonWebKey> jwkList = new ArrayList<>();

        for(Map.Entry<String, Object> entry : certificateConfig.entrySet()) {
            String kid = entry.getKey();
            String filename = (String)entry.getValue();
            if(filename != null) {
                try (InputStream inStream = Config.getInstance().getInputStreamFromFile(filename)) {
                    if(inStream != null) {
                        CertificateFactory cf = CertificateFactory.getInstance("X.509");
                        X509Certificate certificate = (X509Certificate) cf.generateCertificate(inStream);
                        PublicKey pk = certificate.getPublicKey();
                        // Create a JWK object from the public key
                        PublicJsonWebKey jwk = PublicJsonWebKey.Factory.newPublicJwk(pk);
                        jwk.setKeyId(kid);
                        jwkList.add(jwk);
                    }
                }
            }
        }

        // create a JsonWebKeySet object with the list of JWK objects
        JsonWebKeySet jwks = new JsonWebKeySet(jwkList);
        // and output the JSON of the JWKS
        String jwksJson = jwks.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY);
        if(logger.isDebugEnabled()) logger.debug(jwksJson);
        exchange.setStatusCode(200);
        exchange.getResponseSender().send(jwksJson);
    }
}
