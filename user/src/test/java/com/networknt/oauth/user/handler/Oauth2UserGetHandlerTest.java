package com.networknt.oauth.user.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.networknt.client.Client;
import com.networknt.config.Config;
import com.networknt.exception.ApiException;
import com.networknt.exception.ClientException;
import com.networknt.oauth.cache.model.User;
import com.networknt.status.Status;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by stevehu on 2017-01-04.
 */
public class Oauth2UserGetHandlerTest {
    @ClassRule
    public static TestServer server = TestServer.getInstance();

    static final Logger logger = LoggerFactory.getLogger(Oauth2UserGetHandlerTest.class);

    @Test
    public void testPageMissing() throws ClientException, ApiException {
        CloseableHttpClient client = Client.getInstance().getSyncClient();
        HttpGet httpGet = new HttpGet("http://localhost:6885/oauth2/user");

        try {
            CloseableHttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            String body = IOUtils.toString(response.getEntity().getContent(), "utf8");
            Assert.assertEquals(400, statusCode);
            if(statusCode == 400) {
                Status status = Config.getInstance().getMapper().readValue(body, Status.class);
                Assert.assertNotNull(status);
                Assert.assertEquals("ERR11000", status.getCode());
                Assert.assertEquals("VALIDATOR_REQUEST_PARAMETER_QUERY_MISSING", status.getMessage()); // page is missing
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDefaultPageSizePage2() throws ClientException, ApiException {
        CloseableHttpClient client = Client.getInstance().getSyncClient();
        HttpGet httpGet = new HttpGet("http://localhost:6885/oauth2/user?page=2");

        try {
            CloseableHttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            String body = IOUtils.toString(response.getEntity().getContent(), "utf8");
            Assert.assertEquals(200, statusCode);
            if(statusCode == 200) {
                // make sure that there are three users in the result.
                List<User> users = Config.getInstance().getMapper().readValue(body, new TypeReference<List<User>>(){});
                logger.debug("users size = " + users.size());
                Assert.assertTrue(users.size() >= 3 && users.size() <=5);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDefaultPageSizePage1() throws ClientException, ApiException {
        CloseableHttpClient client = Client.getInstance().getSyncClient();
        HttpGet httpGet = new HttpGet("http://localhost:6885/oauth2/user?page=1");

        try {
            CloseableHttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            String body = IOUtils.toString(response.getEntity().getContent(), "utf8");
            Assert.assertEquals(200, statusCode);
            if(statusCode == 200) {
                // make sure that there are three users in the result.
                List<User> users = Config.getInstance().getMapper().readValue(body, new TypeReference<List<User>>(){});
                Assert.assertEquals(10, users.size());
                // make sure that the first is admin
                User user = users.get(0);
                Assert.assertEquals("admin", user.getUserId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUserIdFilter() throws ClientException, ApiException {
        CloseableHttpClient client = Client.getInstance().getSyncClient();
        HttpGet httpGet = new HttpGet("http://localhost:6885/oauth2/user?page=2&userId=test");

        try {
            CloseableHttpResponse response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            String body = IOUtils.toString(response.getEntity().getContent(), "utf8");
            Assert.assertEquals(200, statusCode);
            if(statusCode == 200) {
                // make sure that there are three users in the result.
                List<User> users = Config.getInstance().getMapper().readValue(body, new TypeReference<List<User>>(){});
                Assert.assertEquals(2, users.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
