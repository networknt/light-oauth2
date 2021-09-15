package com.networknt.oauth.token.handler;

import com.networknt.client.Http2Client;
import com.networknt.exception.ClientException;
import io.undertow.UndertowOptions;
import io.undertow.client.ClientConnection;
import io.undertow.client.ClientRequest;
import io.undertow.client.ClientResponse;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.IoUtils;
import org.xnio.OptionMap;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Oauth2BootstrapTokenTest {
    @ClassRule
    public static TestServer server = TestServer.getInstance();

    private static final Logger logger = LoggerFactory.getLogger(Oauth2SigningPostHandlerTest.class);

    private static String encodeCredentials(String clientId, String clientSecret) {
        String cred;
        if(clientSecret != null) {
            cred = clientId + ":" + clientSecret;
        } else {
            cred = clientId;
        }
        String encodedValue;
        byte[] encodedBytes = Base64.encodeBase64(cred.getBytes(UTF_8));
        encodedValue = new String(encodedBytes, UTF_8);
        return encodedValue;
    }

    /**
     * Get a bootstrap token with credentials (client_id and client_secret encoded) in the Authorization header in the request.
     */
    @Test
    public void testBootstrapTokenHeaderCredentials() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "bootstrap_token");
        params.put("sid", "com.networknt.petstore-1.0.0");
        String s = Http2Client.getFormDataString(params);
        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI("https://localhost:6882"), Http2Client.WORKER, client.getDefaultXnioSsl(), Http2Client.BUFFER_POOL, OptionMap.create(UndertowOptions.ENABLE_HTTP2, true)).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }

        try {
            connection.getIoThread().execute(new Runnable() {
                @Override
                public void run() {
                    final ClientRequest request = new ClientRequest().setMethod(Methods.POST).setPath("/oauth2/token");
                    request.getRequestHeaders().put(Headers.HOST, "localhost");
                    request.getRequestHeaders().put(Headers.CONTENT_TYPE, "application/x-www-form-urlencoded");
                    request.getRequestHeaders().put(Headers.AUTHORIZATION, "Basic " + encodeCredentials("f7d42348-c647-4efb-a52d-4c5787421e00", "f6h1FTI8Q3-7UScPZDzfXA"));
                    request.getRequestHeaders().put(Headers.TRANSFER_ENCODING, "chunked");
                    connection.sendRequest(request, client.createClientCallback(reference, latch, s));
                }
            });
            latch.await(10, TimeUnit.SECONDS);
            int statusCode = reference.get().getResponseCode();
            String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
            Assert.assertEquals(200, statusCode);
            logger.debug("response body = " + body);
            Assert.assertTrue(body.indexOf("access_token") > 0);
        } catch (Exception e) {
            logger.error("IOException: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
    }

    /**
     * Get a bootstrap token with credentials (client_id and client_secret) in the body of the request.
     */
    @Test
    public void testBootstrapTokenBodyCredentials() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "bootstrap_token");
        params.put("sid", "com.networknt.petstore-1.0.0");
        params.put("client_id", "f7d42348-c647-4efb-a52d-4c5787421e00");
        params.put("client_secret", "f6h1FTI8Q3-7UScPZDzfXA");
        String s = Http2Client.getFormDataString(params);
        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI("https://localhost:6882"), Http2Client.WORKER, client.getDefaultXnioSsl(), Http2Client.BUFFER_POOL, OptionMap.create(UndertowOptions.ENABLE_HTTP2, true)).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }

        try {
            connection.getIoThread().execute(new Runnable() {
                @Override
                public void run() {
                    final ClientRequest request = new ClientRequest().setMethod(Methods.POST).setPath("/oauth2/token");
                    request.getRequestHeaders().put(Headers.HOST, "localhost");
                    request.getRequestHeaders().put(Headers.CONTENT_TYPE, "application/x-www-form-urlencoded");
                    request.getRequestHeaders().put(Headers.TRANSFER_ENCODING, "chunked");
                    connection.sendRequest(request, client.createClientCallback(reference, latch, s));
                }
            });
            latch.await(10, TimeUnit.SECONDS);
            int statusCode = reference.get().getResponseCode();
            String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
            Assert.assertEquals(200, statusCode);
            logger.debug("response body = " + body);
            Assert.assertTrue(body.indexOf("access_token") > 0);
        } catch (Exception e) {
            logger.error("IOException: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
    }

    /**
     * Get a bootstrap token with wrong scope in the body of the request. Expecting error.
     */
    @Test
    public void testBootstrapTokenWithWrongScope() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "bootstrap_token");
        params.put("sid", "com.networknt.petstore-1.0.0");
        params.put("client_id", "f7d42348-c647-4efb-a52d-4c5787421e00");
        params.put("client_secret", "f6h1FTI8Q3-7UScPZDzfXA");
        params.put("scope", "portal.w");
        String s = Http2Client.getFormDataString(params);
        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI("https://localhost:6882"), Http2Client.WORKER, client.getDefaultXnioSsl(), Http2Client.BUFFER_POOL, OptionMap.create(UndertowOptions.ENABLE_HTTP2, true)).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }

        try {
            connection.getIoThread().execute(new Runnable() {
                @Override
                public void run() {
                    final ClientRequest request = new ClientRequest().setMethod(Methods.POST).setPath("/oauth2/token");
                    request.getRequestHeaders().put(Headers.HOST, "localhost");
                    request.getRequestHeaders().put(Headers.CONTENT_TYPE, "application/x-www-form-urlencoded");
                    request.getRequestHeaders().put(Headers.TRANSFER_ENCODING, "chunked");
                    connection.sendRequest(request, client.createClientCallback(reference, latch, s));
                }
            });
            latch.await(10, TimeUnit.SECONDS);
            int statusCode = reference.get().getResponseCode();
            String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
            Assert.assertEquals(400, statusCode);
            logger.debug("response body = " + body);
            Assert.assertTrue(body.indexOf("ERR12027") > 0);
        } catch (Exception e) {
            logger.error("IOException: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
    }

}
