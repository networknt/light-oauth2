package com.networknt.oauth.auth;

import com.networknt.client.ClientConfig;
import com.networknt.client.ClientRequestCarrier;
import com.networknt.client.Http2Client;
import com.networknt.cluster.Cluster;
import com.networknt.config.JsonMapper;
import com.networknt.httpstring.AttachmentConstants;
import com.networknt.oauth.security.LightPasswordCredential;
import com.networknt.server.Server;
import com.networknt.service.SingletonServiceFactory;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import io.undertow.UndertowOptions;
import io.undertow.client.ClientConnection;
import io.undertow.client.ClientRequest;
import io.undertow.client.ClientResponse;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.OptionMap;

import java.net.URI;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is the light-portal authentication class. It should be defined in the authenticate_class
 * column in client table when register the light-portal application so that this class will be
 * invoked when a user is login from light-portal site lightapi.net.
 * <p>
 * The current implementation for light-portal will use portal user-query to do authentication and
 * roles in the user profile for authorization.
 *
 * Unlike other corporate applications, there is only one type of user for the light-portal.
 *
 * The assumption is that the light-oauth2 and light-portal are deployed to the same cloud and register
 * to the same Consul cluster. If that is not the case, then you cannot use service lookup but use direct
 * url like lightapi.net/portal/query to access the portal query service.
 *
 * @author Steve Hu
 */
public class LightPortalAuthenticator extends AuthenticatorBase<LightPortalAuth> {
    private static final Logger logger = LoggerFactory.getLogger(LightPortalAuthenticator.class);
    private static final String cmd = "{\"host\":\"lightapi.net\",\"service\":\"user\",\"action\":\"loginUser\",\"version\":\"0.1.0\",\"data\":{\"email\":\"%s\",\"password\":\"%s\"}}";
    private static final String queryServiceId = "com.networknt.portal.hybrid.query-1.0.0";
    private static String tag = Server.getServerConfig().getEnvironment();
    // Get the singleton Http2Client instance
    static Http2Client client = Http2Client.getInstance();
    static ClientConnection connection;
    static Cluster cluster;

    public LightPortalAuthenticator() {
        // Get the singleton Cluster instance
        cluster = SingletonServiceFactory.getBean(Cluster.class);
        String host = cluster.serviceToUrl("https", queryServiceId, tag, null);
        if(host != null) {
            // we only need to try to establish a connection if we can find a host. Due to service startup sequence, we might not have hybrid-query service available in the beginning.
            try {
                connection = client.connect(new URI(host), Http2Client.WORKER, Http2Client.SSL, Http2Client.BUFFER_POOL, OptionMap.create(UndertowOptions.ENABLE_HTTP2, true)).get();
            } catch (Exception e) {
                // ignore the error here. Just log it as the target server might not be up and running in the case of direct registry. In that case, we get Connection refused error.
                logger.error("Exception:", e);
            }
        }
    }

    @Override
    public Account authenticate(String id, Credential credential) {
        LightPasswordCredential passwordCredential = (LightPasswordCredential) credential;
        char[] password = passwordCredential.getPassword();
        // user-query service authentication and authorization
        try {
            if(connection == null || !connection.isOpen()) {
                // The connection is close or not created.
                String host = cluster.serviceToUrl("https", queryServiceId, tag, null);
                connection = client.connect(new URI(host), Http2Client.WORKER, Http2Client.SSL, Http2Client.BUFFER_POOL, OptionMap.create(UndertowOptions.ENABLE_HTTP2, true)).get();
            }
            final CountDownLatch latch = new CountDownLatch(1);
            final AtomicReference<ClientResponse> reference = new AtomicReference<>();
            final String s = String.format(cmd, id, new String(password));
            String message = "/portal/query?cmd=" + URLEncoder.encode(s, "UTF-8");
            final ClientRequest request = new ClientRequest().setMethod(Methods.GET).setPath(message);
            request.getRequestHeaders().put(Headers.HOST, "localhost");
            boolean injectOpenTracing = ClientConfig.get().isInjectOpenTracing();
            if(injectOpenTracing) {
                Tracer tracer = passwordCredential.getExchange().getAttachment(AttachmentConstants.EXCHANGE_TRACER);
                if(tracer != null && tracer.activeSpan() != null) {
                    Tags.SPAN_KIND.set(tracer.activeSpan(), Tags.SPAN_KIND_CLIENT);
                    Tags.HTTP_METHOD.set(tracer.activeSpan(), request.getMethod().toString());
                    // remove the url as it contains password which is sensitive.
                    // Tags.HTTP_URL.set(tracer.activeSpan(), request.getPath());
                    tracer.inject(tracer.activeSpan().context(), Format.Builtin.HTTP_HEADERS, new ClientRequestCarrier(request));
                }
            }
            connection.sendRequest(request, client.createClientCallback(reference, latch));
            latch.await();
            int statusCode = reference.get().getResponseCode();
            String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
            if(statusCode == 200) {
                Map<String, Object> map = JsonMapper.string2Map(body);
                // {"roles":"user","id":"stevehu@gmail.com"}
                Account account = new Account() {
                    private Set<String> roles = splitRoles((String)map.get("roles"));
                    private final Principal principal = () -> id;
                    @Override
                    public Principal getPrincipal() {
                        return principal;
                    }

                    @Override
                    public Set<String> getRoles() {
                        return roles;
                    }
                };
                return account;
            } else {
                // create a dummy account to return the error the the StatelessAuthHandler in light-spa-4j
                Map<String, Object> map = JsonMapper.string2Map(body);
                Account account = new Account() {
                    private final Principal principal = () -> "error";
                    @Override
                    public Principal getPrincipal() {
                        return principal;
                    }

                    @Override
                    public Set<String> getRoles() {
                        Set<String> roles = new HashSet<>();
                        roles.add((String)map.get("description"));
                        return roles;
                    }
                };
                return account;
            }
        } catch (Exception e) {
            logger.error("Exception:", e);
            Account account = new Account() {
                private final Principal principal = () -> "error";
                @Override
                public Principal getPrincipal() {
                    return principal;
                }

                @Override
                public Set<String> getRoles() {
                    Set<String> roles = new HashSet<>();
                    roles.add(e.getMessage());
                    return roles;
                }
            };
            return account;
        }
    }

    public Set<String> splitRoles(String roles) {
        Set<String> set = new HashSet<>();
        if(roles != null) {
            String[] splited = roles.split("\\s+");
            set = new HashSet<>(Arrays.asList(splited));
        }
        return set;
    }
}
