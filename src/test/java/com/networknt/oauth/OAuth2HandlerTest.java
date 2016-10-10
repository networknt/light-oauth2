package com.networknt.oauth;

import com.networknt.config.Config;
import com.networknt.exception.ClientException;
import com.networknt.exception.ExceptionHandler;
import com.networknt.security.JwtHelper;
import com.networknt.server.Server;
import com.networknt.status.Status;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve on 09/10/16.
 */
public class OAuth2HandlerTest {
    static final Logger logger = LoggerFactory.getLogger(OAuth2HandlerTest.class);

    static Server server = null;

    @BeforeClass
    public static void setUp() {
        if (server == null) {
            logger.info("starting server");
            server.start();
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (server != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {

            }
            server.stop();
            logger.info("The server is stopped.");
        }
    }

    public static String encodeCredentials(String clientId, String clientSecret) {
        String cred = "";
        if(clientSecret != null) {
            cred = clientId + ":" + clientSecret;
        } else {
            cred = clientId;
        }
        String encodedValue = null;
        byte[] encodedBytes = Base64.encodeBase64(cred.getBytes());
        encodedValue = new String(encodedBytes);
        return encodedValue;
    }


    @Test(expected = ConnectException.class)
    public void testAuthorizationCode() throws Exception {
        String url = "http://localhost:8888/oauth2/code?response_type=code&client_id=6e9d1db3-2feb-4c1f-a5ad-9e93ae8ca59d&redirect_uri=http://localhost:8080/authorization";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        // add authentication header
        httpGet.setHeader("Authorization", "Basic " + encodeCredentials("stevehu", "123456"));
        CloseableHttpResponse response = client.execute(httpGet);
        // at this moment, an exception will help as it is redirected to localhost:8080 and it is not up.
    }

    @Test
    public void testCodeWithoutResponseType() throws Exception {
        String url = "http://localhost:8888/oauth2/code?client_id=6e9d1db3-2feb-4c1f-a5ad-9e93ae8ca59d&redirect_uri=http://localhost:8888/authorization";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Basic " + encodeCredentials("stevehu", "123456"));
        try {
            CloseableHttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            Assert.assertEquals(401, statusCode);
            if(statusCode == 401) {
                Status status = Config.getInstance().getMapper().readValue(response.getEntity().getContent(), Status.class);
                Assert.assertNotNull(status);
                Assert.assertEquals("ERR12009", status.getCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCodeWithoutClientId() throws Exception {
        String url = "http://localhost:8888/oauth2/code?response_type=code&redirect_uri=http://localhost:8888/authorization";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Basic " + encodeCredentials("stevehu", "123456"));
        try {
            CloseableHttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            Assert.assertEquals(401, statusCode);
            if(statusCode == 401) {
                Status status = Config.getInstance().getMapper().readValue(response.getEntity().getContent(), Status.class);
                Assert.assertNotNull(status);
                Assert.assertEquals("ERR12009", status.getCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCodeInvalidResonseType() throws Exception {
        String url = "http://localhost:8888/oauth2/code?response_type=wrong&client_id=6e9d1db3-2feb-4c1f-a5ad-9e93ae8ca59d&redirect_uri=http://localhost:8080/authorization";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Basic " + encodeCredentials("stevehu", "123456"));
        try {
            CloseableHttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            Assert.assertEquals(401, statusCode);
            if(statusCode == 401) {
                Status status = Config.getInstance().getMapper().readValue(response.getEntity().getContent(), Status.class);
                Assert.assertNotNull(status);
                Assert.assertEquals("ERR12010", status.getCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCodeClientIdNotFound() throws Exception {
        String url = "http://localhost:8888/oauth2/code?response_type=code&client_id=fake&redirect_uri=http://localhost:8080/authorization";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Basic " + encodeCredentials("stevehu", "123456"));
        try {
            CloseableHttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, statusCode);
            if(statusCode == 404) {
                Status status = Config.getInstance().getMapper().readValue(response.getEntity().getContent(), Status.class);
                Assert.assertNotNull(status);
                Assert.assertEquals("ERR12006", status.getCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testClientCredentialsToken() throws Exception {
        String url = "http://localhost:8888/oauth2/token";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Authorization", "Basic " + encodeCredentials("f7d42348-c647-4efb-a52d-4c5787421e72", "f6h1FTI8Q3-7UScPZDzfXA"));

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(httpPost);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        String body = EntityUtils.toString(response.getEntity());
        logger.debug("response body = " + body);
        Assert.assertTrue(body.indexOf("access_token") > 0);
    }

    @Test
    public void testTokenInvalidForm() throws Exception {
        String url = "http://localhost:8888/oauth2/token";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Authorization", "Basic " + encodeCredentials("f7d42348-c647-4efb-a52d-4c5787421e72", "f6h1FTI8Q3-7UScPZDzfXA"));
        httpPost.setEntity(new StringEntity("test"));
        HttpResponse response = client.execute(httpPost);
        Assert.assertEquals(400, response.getStatusLine().getStatusCode());
        Status status = Config.getInstance().getMapper().readValue(response.getEntity().getContent(), Status.class);
        Assert.assertNotNull(status);
        Assert.assertEquals("ERR12000", status.getCode());
    }

    @Test
    public void testTokenInvalidGrantType() throws Exception {
        String url = "http://localhost:8888/oauth2/token";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Authorization", "Basic " + encodeCredentials("f7d42348-c647-4efb-a52d-4c5787421e72", "f6h1FTI8Q3-7UScPZDzfXA"));
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grant_type", "fake"));
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(httpPost);
        Assert.assertEquals(400, response.getStatusLine().getStatusCode());
        Status status = Config.getInstance().getMapper().readValue(response.getEntity().getContent(), Status.class);
        Assert.assertNotNull(status);
        Assert.assertEquals("ERR12001", status.getCode());
    }

    @Test
    public void testTokenMissingAuthHeader() throws Exception {
        String url = "http://localhost:8888/oauth2/token";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        //httpPost.setHeader("Authorization", "Basic " + encodeCredentials("f7d42348-c647-4efb-a52d-4c5787421e72", "f6h1FTI8Q3-7UScPZDzfXA"));
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(httpPost);
        Assert.assertEquals(401, response.getStatusLine().getStatusCode());
        Status status = Config.getInstance().getMapper().readValue(response.getEntity().getContent(), Status.class);
        Assert.assertNotNull(status);
        Assert.assertEquals("ERR12002", status.getCode());
    }

    @Test
    public void testTokenClientIdNotFound() throws Exception {
        String url = "http://localhost:8888/oauth2/token";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Authorization", "Basic " + encodeCredentials("fake", "f6h1FTI8Q3-7UScPZDzfXA"));
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(httpPost);
        Assert.assertEquals(404, response.getStatusLine().getStatusCode());
        Status status = Config.getInstance().getMapper().readValue(response.getEntity().getContent(), Status.class);
        Assert.assertNotNull(status);
        Assert.assertEquals("ERR12006", status.getCode());
    }

    @Test
    public void testTokenUnAuthedClientId() throws Exception {
        String url = "http://localhost:8888/oauth2/token";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Authorization", "Basic " + encodeCredentials("f7d42348-c647-4efb-a52d-4c5787421e72", "fake"));
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(httpPost);
        Assert.assertEquals(401, response.getStatusLine().getStatusCode());
        Status status = Config.getInstance().getMapper().readValue(response.getEntity().getContent(), Status.class);
        Assert.assertNotNull(status);
        Assert.assertEquals("ERR12007", status.getCode());
    }

    @Test
    public void testTokenInvalidCredentials() throws Exception {
        String url = "http://localhost:8888/oauth2/token";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Authorization", "Basic abc");
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(httpPost);
        Assert.assertEquals(401, response.getStatusLine().getStatusCode());
        Status status = Config.getInstance().getMapper().readValue(response.getEntity().getContent(), Status.class);
        Assert.assertNotNull(status);
        Assert.assertEquals("ERR12004", status.getCode());
    }

    @Test
    public void testTokenInvalidAuthHeader() throws Exception {
        String url = "http://localhost:8888/oauth2/token";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Authorization", "Bearer abc");
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(httpPost);
        Assert.assertEquals(401, response.getStatusLine().getStatusCode());
        Status status = Config.getInstance().getMapper().readValue(response.getEntity().getContent(), Status.class);
        Assert.assertNotNull(status);
        Assert.assertEquals("ERR12003", status.getCode());
    }

}
