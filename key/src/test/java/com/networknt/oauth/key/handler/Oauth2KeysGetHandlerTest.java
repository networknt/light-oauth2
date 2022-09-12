
package com.networknt.oauth.key.handler;

import com.networknt.client.Http2Client;
import com.networknt.exception.ClientException;
import com.networknt.openapi.OpenApiHandler;
import com.networknt.openapi.ResponseValidator;
import com.networknt.openapi.SchemaValidator;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.status.Status;
import io.undertow.UndertowOptions;
import io.undertow.client.ClientConnection;
import io.undertow.client.ClientRequest;
import io.undertow.client.ClientResponse;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.OptionMap;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertNotNull;


public class Oauth2KeysGetHandlerTest {

    @ClassRule
    public static TestServer server = TestServer.getInstance();

    static final Logger logger = LoggerFactory.getLogger(Oauth2KeysGetHandlerTest.class);
    static final String url = "https://localhost:6886";
    static final String JSON_MEDIA_TYPE = "application/json";

    @Test
    public void testOauth2KeysGetHandlerTest() throws ClientException {

        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.borrowConnection(new URI(url), Http2Client.WORKER, Http2Client.SSL, Http2Client.BUFFER_POOL, OptionMap.create(UndertowOptions.ENABLE_HTTP2, true)).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        String requestUri = "/oauth2/keys";
        String httpMethod = "get";
        try {
            ClientRequest request = new ClientRequest().setPath(requestUri).setMethod(Methods.GET);
            
            //customized header parameters 
            request.getRequestHeaders().put(new HttpString("host"), "localhost");
            connection.sendRequest(request, client.createClientCallback(reference, latch));
            
            latch.await();
        } catch (Exception e) {
            logger.error("Exception: ", e);
            throw new ClientException(e);
        } finally {
            client.returnConnection(connection);
        }
        String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
        Optional<HeaderValues> contentTypeName = Optional.ofNullable(reference.get().getResponseHeaders().get(Headers.CONTENT_TYPE));
        SchemaValidator schemaValidator = new SchemaValidator(OpenApiHandler.helper.openApi3);
        ResponseValidator responseValidator = new ResponseValidator(schemaValidator);
        int statusCode = reference.get().getResponseCode();
        Status status;
        if(contentTypeName.isPresent()) {
            status = responseValidator.validateResponseContent(body, requestUri, httpMethod, String.valueOf(statusCode), contentTypeName.get().getFirst());
        } else {
            status = responseValidator.validateResponseContent(body, requestUri, httpMethod, String.valueOf(statusCode), JSON_MEDIA_TYPE);
        }
        assertNotNull(status);
    }


}

