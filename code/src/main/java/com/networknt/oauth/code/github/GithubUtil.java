package com.networknt.oauth.code.github;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.IoUtils;
import org.xnio.OptionMap;

import com.networknt.client.Http2Client;
import com.networknt.config.Config;
import com.networknt.exception.ClientException;

import io.undertow.client.ClientConnection;
import io.undertow.client.ClientRequest;
import io.undertow.client.ClientResponse;
import io.undertow.util.Headers;
import io.undertow.util.Methods;

public class GithubUtil {
	private final static Logger logger = LoggerFactory.getLogger(GithubUtil.class);
	private final static String CONFIG_GITHUB = "github";
	private final static String CONFIG_SECRET = "secret";
	private final static String GITHUB_TOKEN = "githubToken";

	private final static GithubConfig config = (GithubConfig) Config.getInstance().getJsonObjectConfig(CONFIG_GITHUB,
			GithubConfig.class);
	private final static Map<String, Object> secret = Config.getInstance().getJsonMapConfig(CONFIG_SECRET);
	private final static String githubToken = (String) secret.get(GITHUB_TOKEN);

	/**
	 *
	 * @param username
	 *            String
	 * @return A set of group attributes for the username on github DB. You can
	 *         only call this method if the username has been authenticated
	 * @throws ClientException 
	 */
	public static Set<String> authorize(String username) throws Exception {
		Set<String> groups = new HashSet<String>();

		String apiURL = config.protocol + "://" + config.host + config.pathPrefix;
		String contentsURL = "/repos/" + config.owner + "/" + config.repo + "/contents/" + config.path;

		final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        
        final ClientConnection connection = client.connect(new URI(apiURL), Http2Client.WORKER, Http2Client.SSL, Http2Client.POOL, OptionMap.EMPTY).get();
        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        try {
        	final ClientRequest request = new ClientRequest().setMethod(Methods.GET).setPath(contentsURL);
			request.getRequestHeaders().put(Headers.AUTHORIZATION, "token " + githubToken);
			
			connection.sendRequest(request, client.createClientCallback(reference, latch)); 
			latch.await(); 
			/*
			connection.sendRequest(request, new ClientCallback<ClientExchange>() {
				@Override
				public void completed(ClientExchange result) {
					new StringReadChannelListener(Http2Client.POOL) {

						@Override
						protected void stringDone(String string) {
							logger.info("getToken response = " + string);
                            latch.countDown();
						}

						@Override
						protected void error(IOException e) {
							logger.error("IOException:", e);
                            latch.countDown();
						}
					}.setup(result.getResponseChannel());
				}

				@Override
				public void failed(IOException e) {
					logger.error("IOException:", e);
                    latch.countDown();
				}
			});*/
        } catch (Exception e) {
            logger.error("Exception: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
        
		/*
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(apiURL + contentsURL);

		// add request header
		request.setHeader("Authorization", "token " + githubToken);
		// Execute request
		HttpResponse response = null;
		try {
			response = client.execute(request);

			int returnCode = response.getStatusLine().getStatusCode();
			if (returnCode == 200) {
				String responseString = new BasicResponseHandler().handleResponse(response);
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(responseString);
				String content = json.get("content").toString();
				content = new String(Base64.decodeBase64(content.getBytes()));
				// Get groups
				JSONArray metadata = new JSONArray(content);
				for (int i = 0; i < metadata.length(); i++) {
					org.json.JSONObject itemArr = (org.json.JSONObject) metadata.get(i);
					if (itemArr.get("github_username").toString().equals(username)) {
						if (logger.isDebugEnabled())
							logger.debug(username + " is part of group(s): " + itemArr.get("groups").toString());
						groups.add(itemArr.get("groups").toString());
					}
				}
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("Failed to authorize user " + username, e);
			return null;
		}*/
		return groups;
	}

}
