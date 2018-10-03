package com.networknt.oauth.token.handler;

import com.networknt.client.Http2Client;
import com.networknt.config.Config;
import com.networknt.exception.ApiException;
import com.networknt.exception.ClientException;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.status.Status;
import io.undertow.UndertowOptions;
import io.undertow.client.ClientConnection;
import io.undertow.client.ClientRequest;
import io.undertow.client.ClientResponse;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.IoUtils;
import org.xnio.OptionMap;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static com.networknt.client.oauth.OauthHelper.encodeCredentials;

public class Oauth2DerefGetHandlerTest {
    @ClassRule
    public static TestServer server = TestServer.getInstance();

    private static final Logger logger = LoggerFactory.getLogger(Oauth2DerefGetHandlerTest.class);

    /**
     * This is the normal flow. The right client_id and client_secret are passed in and the client_id in reference is
     * matching the authenticated client_id. Please note that we create the references cache in the beginning.
     *
     * @throws ClientException
     * @throws ApiException
     */
    @Test
    public void testDerefGet() throws ClientException, ApiException {
        // first create references cache
        Map<String, String> referenceMap = new HashMap<>();
        referenceMap.put("jwt", "eyJraWQiOiIxMDAiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTc5MDAzNTcwOSwianRpIjoiSTJnSmdBSHN6NzJEV2JWdUFMdUU2QSIsImlhdCI6MTQ3NDY3NTcwOSwibmJmIjoxNDc0Njc1NTg5LCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6InN0ZXZlIiwidXNlcl90eXBlIjoiRU1QTE9ZRUUiLCJjbGllbnRfaWQiOiJmN2Q0MjM0OC1jNjQ3LTRlZmItYTUyZC00YzU3ODc0MjFlNzIiLCJzY29wZSI6WyJ3cml0ZTpwZXRzIiwicmVhZDpwZXRzIl19.mue6eh70kGS3Nt2BCYz7ViqwO7lh_4JSFwcHYdJMY6VfgKTHhsIGKq2uEDt3zwT56JFAePwAxENMGUTGvgceVneQzyfQsJeVGbqw55E9IfM_uSM-YcHwTfR7eSLExN4pbqzVDI353sSOvXxA98ZtJlUZKgXNE1Ngun3XFORCRIB_eH8B0FY_nT_D1Dq2WJrR-re-fbR6_va95vwoUdCofLRa4IpDfXXx19ZlAtfiVO44nw6CS8O87eGfAm7rCMZIzkWlCOFWjNHnCeRsh7CVdEH34LF-B48beiG5lM7h4N12-EME8_VDefgMjZ8eqs1ICvJMxdIut58oYbdnkwTjkA");
        referenceMap.put("clientId", "841c7d50-7690-11e8-adc0-fa7ae01bbebc");
        CacheStartupHookProvider.hz.getMap("references").put("sbXYjyIQTTikjLSr0m_2ww", referenceMap);

        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI("https://localhost:6882"), Http2Client.WORKER, Http2Client.SSL, Http2Client.BUFFER_POOL, OptionMap.create(UndertowOptions.ENABLE_HTTP2, true)).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        try {
            ClientRequest request = new ClientRequest().setMethod(Methods.GET).setPath("/oauth2/deref/sbXYjyIQTTikjLSr0m_2ww");
            request.getRequestHeaders().put(Headers.AUTHORIZATION, "Basic " + encodeCredentials("841c7d50-7690-11e8-adc0-fa7ae01bbebc", "f6h1FTI8Q3-7UScPZDzfXA"));
            connection.sendRequest(request, client.createClientCallback(reference, latch));
            latch.await();
        } catch (Exception e) {
            logger.error("Exception: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
        Assert.assertEquals(200, statusCode);
        logger.debug("body = " + body);
        Assert.assertFalse(body.startsWith("{"));
    }

    /**
     * A fake client_id is passed in and this client cannot be found in the AS. Although everything else
     * are setup correctly, we are expecting client not found error.
     *
     * @throws ClientException
     * @throws IOException
     */
    @Test
    public void testClientNotFound() throws ClientException , IOException {
        // first create references cache
        Map<String, String> referenceMap = new HashMap<>();
        referenceMap.put("jwt", "eyJraWQiOiIxMDAiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTc5MDAzNTcwOSwianRpIjoiSTJnSmdBSHN6NzJEV2JWdUFMdUU2QSIsImlhdCI6MTQ3NDY3NTcwOSwibmJmIjoxNDc0Njc1NTg5LCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6InN0ZXZlIiwidXNlcl90eXBlIjoiRU1QTE9ZRUUiLCJjbGllbnRfaWQiOiJmN2Q0MjM0OC1jNjQ3LTRlZmItYTUyZC00YzU3ODc0MjFlNzIiLCJzY29wZSI6WyJ3cml0ZTpwZXRzIiwicmVhZDpwZXRzIl19.mue6eh70kGS3Nt2BCYz7ViqwO7lh_4JSFwcHYdJMY6VfgKTHhsIGKq2uEDt3zwT56JFAePwAxENMGUTGvgceVneQzyfQsJeVGbqw55E9IfM_uSM-YcHwTfR7eSLExN4pbqzVDI353sSOvXxA98ZtJlUZKgXNE1Ngun3XFORCRIB_eH8B0FY_nT_D1Dq2WJrR-re-fbR6_va95vwoUdCofLRa4IpDfXXx19ZlAtfiVO44nw6CS8O87eGfAm7rCMZIzkWlCOFWjNHnCeRsh7CVdEH34LF-B48beiG5lM7h4N12-EME8_VDefgMjZ8eqs1ICvJMxdIut58oYbdnkwTjkA");
        referenceMap.put("clientId", "841c7d50-7690-11e8-adc0-fa7ae01bbebc");
        CacheStartupHookProvider.hz.getMap("references").put("sbXYjyIQTTikjLSr0m_2ww", referenceMap);

        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI("https://localhost:6882"), Http2Client.WORKER, Http2Client.SSL, Http2Client.BUFFER_POOL, OptionMap.create(UndertowOptions.ENABLE_HTTP2, true)).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        try {
            ClientRequest request = new ClientRequest().setMethod(Methods.GET).setPath("/oauth2/deref/sbXYjyIQTTikjLSr0m_2ww");
            request.getRequestHeaders().put(Headers.AUTHORIZATION, "Basic " + encodeCredentials("fake", "f6h1FTI8Q3-7UScPZDzfXA"));
            connection.sendRequest(request, client.createClientCallback(reference, latch));
            latch.await();
        } catch (Exception e) {
            logger.error("Exception: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
        logger.debug("statusCode = " + statusCode);
        logger.debug("body = " + body);
        Assert.assertEquals(404, statusCode);
        Status status = Config.getInstance().getMapper().readValue(body, Status.class);
        Assert.assertNotNull(status);
        Assert.assertEquals("ERR12014", status.getCode());
        Assert.assertEquals("CLIENT_NOT_FOUND", status.getMessage());
    }

    /**
     * Similar with the normal flow, this time a wrong password is passed into the authorization header. An error
     * is expected with UNAUTHORIZED_CLIENT status code.
     *
     * @throws ClientException
     * @throws IOException
     */
    @Test
    public void testFailedClientAuthentication() throws ClientException, IOException {
        // first create references cache
        Map<String, String> referenceMap = new HashMap<>();
        referenceMap.put("jwt", "eyJraWQiOiIxMDAiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTc5MDAzNTcwOSwianRpIjoiSTJnSmdBSHN6NzJEV2JWdUFMdUU2QSIsImlhdCI6MTQ3NDY3NTcwOSwibmJmIjoxNDc0Njc1NTg5LCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6InN0ZXZlIiwidXNlcl90eXBlIjoiRU1QTE9ZRUUiLCJjbGllbnRfaWQiOiJmN2Q0MjM0OC1jNjQ3LTRlZmItYTUyZC00YzU3ODc0MjFlNzIiLCJzY29wZSI6WyJ3cml0ZTpwZXRzIiwicmVhZDpwZXRzIl19.mue6eh70kGS3Nt2BCYz7ViqwO7lh_4JSFwcHYdJMY6VfgKTHhsIGKq2uEDt3zwT56JFAePwAxENMGUTGvgceVneQzyfQsJeVGbqw55E9IfM_uSM-YcHwTfR7eSLExN4pbqzVDI353sSOvXxA98ZtJlUZKgXNE1Ngun3XFORCRIB_eH8B0FY_nT_D1Dq2WJrR-re-fbR6_va95vwoUdCofLRa4IpDfXXx19ZlAtfiVO44nw6CS8O87eGfAm7rCMZIzkWlCOFWjNHnCeRsh7CVdEH34LF-B48beiG5lM7h4N12-EME8_VDefgMjZ8eqs1ICvJMxdIut58oYbdnkwTjkA");
        referenceMap.put("clientId", "841c7d50-7690-11e8-adc0-fa7ae01bbebc");
        CacheStartupHookProvider.hz.getMap("references").put("sbXYjyIQTTikjLSr0m_2ww", referenceMap);

        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI("https://localhost:6882"), Http2Client.WORKER, Http2Client.SSL, Http2Client.BUFFER_POOL, OptionMap.create(UndertowOptions.ENABLE_HTTP2, true)).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        try {
            ClientRequest request = new ClientRequest().setMethod(Methods.GET).setPath("/oauth2/deref/sbXYjyIQTTikjLSr0m_2ww");
            request.getRequestHeaders().put(Headers.AUTHORIZATION, "Basic " + encodeCredentials("841c7d50-7690-11e8-adc0-fa7ae01bbebc", "WrongPassword"));
            connection.sendRequest(request, client.createClientCallback(reference, latch));
            latch.await();
        } catch (Exception e) {
            logger.error("Exception: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
        logger.debug("statusCode = " + statusCode);
        logger.debug("body = " + body);
        Assert.assertEquals(401, statusCode);
        Status status = Config.getInstance().getMapper().readValue(body, Status.class);
        Assert.assertNotNull(status);
        Assert.assertEquals("ERR12007", status.getCode());
        Assert.assertEquals("UNAUTHORIZED_CLIENT", status.getMessage());

    }

    /**
     * Missing authorization header when calling this service.
     *
     * @throws ClientException
     * @throws IOException
     */
    @Test
    public void testMissingAuthorizationHeader() throws ClientException, IOException {
        // first create references cache
        Map<String, String> referenceMap = new HashMap<>();
        referenceMap.put("jwt", "eyJraWQiOiIxMDAiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTc5MDAzNTcwOSwianRpIjoiSTJnSmdBSHN6NzJEV2JWdUFMdUU2QSIsImlhdCI6MTQ3NDY3NTcwOSwibmJmIjoxNDc0Njc1NTg5LCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6InN0ZXZlIiwidXNlcl90eXBlIjoiRU1QTE9ZRUUiLCJjbGllbnRfaWQiOiJmN2Q0MjM0OC1jNjQ3LTRlZmItYTUyZC00YzU3ODc0MjFlNzIiLCJzY29wZSI6WyJ3cml0ZTpwZXRzIiwicmVhZDpwZXRzIl19.mue6eh70kGS3Nt2BCYz7ViqwO7lh_4JSFwcHYdJMY6VfgKTHhsIGKq2uEDt3zwT56JFAePwAxENMGUTGvgceVneQzyfQsJeVGbqw55E9IfM_uSM-YcHwTfR7eSLExN4pbqzVDI353sSOvXxA98ZtJlUZKgXNE1Ngun3XFORCRIB_eH8B0FY_nT_D1Dq2WJrR-re-fbR6_va95vwoUdCofLRa4IpDfXXx19ZlAtfiVO44nw6CS8O87eGfAm7rCMZIzkWlCOFWjNHnCeRsh7CVdEH34LF-B48beiG5lM7h4N12-EME8_VDefgMjZ8eqs1ICvJMxdIut58oYbdnkwTjkA");
        referenceMap.put("clientId", "841c7d50-7690-11e8-adc0-fa7ae01bbebc");
        CacheStartupHookProvider.hz.getMap("references").put("sbXYjyIQTTikjLSr0m_2ww", referenceMap);

        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI("https://localhost:6882"), Http2Client.WORKER, Http2Client.SSL, Http2Client.BUFFER_POOL, OptionMap.create(UndertowOptions.ENABLE_HTTP2, true)).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        try {
            ClientRequest request = new ClientRequest().setMethod(Methods.GET).setPath("/oauth2/deref/sbXYjyIQTTikjLSr0m_2ww");
            // there is no Authorization header here.
            connection.sendRequest(request, client.createClientCallback(reference, latch));
            latch.await();
        } catch (Exception e) {
            logger.error("Exception: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
        logger.debug("statusCode = " + statusCode);
        logger.debug("body = " + body);
        Assert.assertEquals(401, statusCode);
        Status status = Config.getInstance().getMapper().readValue(body, Status.class);
        Assert.assertNotNull(status);
        Assert.assertEquals("ERR12002", status.getCode());
        Assert.assertEquals("MISSING_AUTHORIZATION_HEADER", status.getMessage());

    }

    /**
     * De-reference token cannot be found
     *
     * @throws ClientException
     * @throws IOException
     */
    @Test
    public void testDerefTokenNotFound() throws ClientException, IOException {
        // first create references cache
        Map<String, String> referenceMap = new HashMap<>();
        referenceMap.put("jwt", "eyJraWQiOiIxMDAiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTc5MDAzNTcwOSwianRpIjoiSTJnSmdBSHN6NzJEV2JWdUFMdUU2QSIsImlhdCI6MTQ3NDY3NTcwOSwibmJmIjoxNDc0Njc1NTg5LCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6InN0ZXZlIiwidXNlcl90eXBlIjoiRU1QTE9ZRUUiLCJjbGllbnRfaWQiOiJmN2Q0MjM0OC1jNjQ3LTRlZmItYTUyZC00YzU3ODc0MjFlNzIiLCJzY29wZSI6WyJ3cml0ZTpwZXRzIiwicmVhZDpwZXRzIl19.mue6eh70kGS3Nt2BCYz7ViqwO7lh_4JSFwcHYdJMY6VfgKTHhsIGKq2uEDt3zwT56JFAePwAxENMGUTGvgceVneQzyfQsJeVGbqw55E9IfM_uSM-YcHwTfR7eSLExN4pbqzVDI353sSOvXxA98ZtJlUZKgXNE1Ngun3XFORCRIB_eH8B0FY_nT_D1Dq2WJrR-re-fbR6_va95vwoUdCofLRa4IpDfXXx19ZlAtfiVO44nw6CS8O87eGfAm7rCMZIzkWlCOFWjNHnCeRsh7CVdEH34LF-B48beiG5lM7h4N12-EME8_VDefgMjZ8eqs1ICvJMxdIut58oYbdnkwTjkA");
        referenceMap.put("clientId", "841c7d50-7690-11e8-adc0-fa7ae01bbebc");
        CacheStartupHookProvider.hz.getMap("references").put("sbXYjyIQTTikjLSr0m_2ww", referenceMap);

        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI("https://localhost:6882"), Http2Client.WORKER, Http2Client.SSL, Http2Client.BUFFER_POOL, OptionMap.create(UndertowOptions.ENABLE_HTTP2, true)).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        try {
            ClientRequest request = new ClientRequest().setMethod(Methods.GET).setPath("/oauth2/deref/fake");
            request.getRequestHeaders().put(Headers.AUTHORIZATION, "Basic " + encodeCredentials("841c7d50-7690-11e8-adc0-fa7ae01bbebc", "f6h1FTI8Q3-7UScPZDzfXA"));
            connection.sendRequest(request, client.createClientCallback(reference, latch));
            latch.await();
        } catch (Exception e) {
            logger.error("Exception: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
        logger.debug("statusCode = " + statusCode);
        logger.debug("body = " + body);
        Assert.assertEquals(400, statusCode);
        Status status = Config.getInstance().getMapper().readValue(body, Status.class);
        Assert.assertNotNull(status);
        Assert.assertEquals("ERR12045", status.getCode());
        Assert.assertEquals("DEREF_TOKEN_NOT_FOUND", status.getMessage());

    }

    /**
     * The client saved in the reference cache does not match the authenticated client_id from the request.
     * It is optional but if the client registers a deref_client_id, then it should be validated.
     *
     * @throws ClientException
     * @throws ApiException
     */
    @Test
    public void testDerefClientNotMatch() throws ClientException, IOException {
        // first create references cache
        Map<String, String> referenceMap = new HashMap<>();
        referenceMap.put("jwt", "eyJraWQiOiIxMDAiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTc5MDAzNTcwOSwianRpIjoiSTJnSmdBSHN6NzJEV2JWdUFMdUU2QSIsImlhdCI6MTQ3NDY3NTcwOSwibmJmIjoxNDc0Njc1NTg5LCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6InN0ZXZlIiwidXNlcl90eXBlIjoiRU1QTE9ZRUUiLCJjbGllbnRfaWQiOiJmN2Q0MjM0OC1jNjQ3LTRlZmItYTUyZC00YzU3ODc0MjFlNzIiLCJzY29wZSI6WyJ3cml0ZTpwZXRzIiwicmVhZDpwZXRzIl19.mue6eh70kGS3Nt2BCYz7ViqwO7lh_4JSFwcHYdJMY6VfgKTHhsIGKq2uEDt3zwT56JFAePwAxENMGUTGvgceVneQzyfQsJeVGbqw55E9IfM_uSM-YcHwTfR7eSLExN4pbqzVDI353sSOvXxA98ZtJlUZKgXNE1Ngun3XFORCRIB_eH8B0FY_nT_D1Dq2WJrR-re-fbR6_va95vwoUdCofLRa4IpDfXXx19ZlAtfiVO44nw6CS8O87eGfAm7rCMZIzkWlCOFWjNHnCeRsh7CVdEH34LF-B48beiG5lM7h4N12-EME8_VDefgMjZ8eqs1ICvJMxdIut58oYbdnkwTjkA");
        referenceMap.put("clientId", "841c7d50-7690-11e8-adc0-fa7ae01bbebc");
        CacheStartupHookProvider.hz.getMap("references").put("sbXYjyIQTTikjLSr0m_2ww", referenceMap);

        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI("https://localhost:6882"), Http2Client.WORKER, Http2Client.SSL, Http2Client.BUFFER_POOL, OptionMap.create(UndertowOptions.ENABLE_HTTP2, true)).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        try {
            ClientRequest request = new ClientRequest().setMethod(Methods.GET).setPath("/oauth2/deref/sbXYjyIQTTikjLSr0m_2ww");
            // this client_id is not the same as in the referenceMap
            request.getRequestHeaders().put(Headers.AUTHORIZATION, "Basic " + encodeCredentials("6e9d1db3-2feb-4c1f-a5ad-9e93ae8ca59d", "f6h1FTI8Q3-7UScPZDzfXA"));
            connection.sendRequest(request, client.createClientCallback(reference, latch));
            latch.await();
        } catch (Exception e) {
            logger.error("Exception: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
        logger.debug("statusCode = " + statusCode);
        logger.debug("body = " + body);
        Assert.assertEquals(400, statusCode);
        Status status = Config.getInstance().getMapper().readValue(body, Status.class);
        Assert.assertNotNull(status);
        Assert.assertEquals("ERR12044", status.getCode());
        Assert.assertEquals("DEREF_CLIENT_NOT_MATCH", status.getMessage());
    }

    /**
     * JWT token cannot be found in the referenceMap. This is not possible if there is no bugs.
     *
     * @throws ClientException
     * @throws ApiException
     */
    @Test
    public void testJwtNotFound() throws ClientException, IOException {
        // first create references cache
        Map<String, String> referenceMap = new HashMap<>();
        //referenceMap.put("jwt", "eyJraWQiOiIxMDAiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTc5MDAzNTcwOSwianRpIjoiSTJnSmdBSHN6NzJEV2JWdUFMdUU2QSIsImlhdCI6MTQ3NDY3NTcwOSwibmJmIjoxNDc0Njc1NTg5LCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6InN0ZXZlIiwidXNlcl90eXBlIjoiRU1QTE9ZRUUiLCJjbGllbnRfaWQiOiJmN2Q0MjM0OC1jNjQ3LTRlZmItYTUyZC00YzU3ODc0MjFlNzIiLCJzY29wZSI6WyJ3cml0ZTpwZXRzIiwicmVhZDpwZXRzIl19.mue6eh70kGS3Nt2BCYz7ViqwO7lh_4JSFwcHYdJMY6VfgKTHhsIGKq2uEDt3zwT56JFAePwAxENMGUTGvgceVneQzyfQsJeVGbqw55E9IfM_uSM-YcHwTfR7eSLExN4pbqzVDI353sSOvXxA98ZtJlUZKgXNE1Ngun3XFORCRIB_eH8B0FY_nT_D1Dq2WJrR-re-fbR6_va95vwoUdCofLRa4IpDfXXx19ZlAtfiVO44nw6CS8O87eGfAm7rCMZIzkWlCOFWjNHnCeRsh7CVdEH34LF-B48beiG5lM7h4N12-EME8_VDefgMjZ8eqs1ICvJMxdIut58oYbdnkwTjkA");
        referenceMap.put("clientId", "841c7d50-7690-11e8-adc0-fa7ae01bbebc");
        CacheStartupHookProvider.hz.getMap("references").put("sbXYjyIQTTikjLSr0m_2ww", referenceMap);

        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI("https://localhost:6882"), Http2Client.WORKER, Http2Client.SSL, Http2Client.BUFFER_POOL, OptionMap.create(UndertowOptions.ENABLE_HTTP2, true)).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        try {
            ClientRequest request = new ClientRequest().setMethod(Methods.GET).setPath("/oauth2/deref/sbXYjyIQTTikjLSr0m_2ww");
            // this client_id is not the same as in the referenceMap
            request.getRequestHeaders().put(Headers.AUTHORIZATION, "Basic " + encodeCredentials("841c7d50-7690-11e8-adc0-fa7ae01bbebc", "f6h1FTI8Q3-7UScPZDzfXA"));
            connection.sendRequest(request, client.createClientCallback(reference, latch));
            latch.await();
        } catch (Exception e) {
            logger.error("Exception: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
        logger.debug("statusCode = " + statusCode);
        logger.debug("body = " + body);
        Assert.assertEquals(400, statusCode);
        Status status = Config.getInstance().getMapper().readValue(body, Status.class);
        Assert.assertNotNull(status);
        Assert.assertEquals("ERR12046", status.getCode());
        Assert.assertEquals("JWT_TOKEN_NOT_FOUND", status.getMessage());
    }

}
