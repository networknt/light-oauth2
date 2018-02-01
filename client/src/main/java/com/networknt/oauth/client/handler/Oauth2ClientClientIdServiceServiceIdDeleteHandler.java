
package com.networknt.oauth.client.handler;

import com.hazelcast.core.IMap;
import com.networknt.config.Config;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Client;
import com.networknt.oauth.cache.model.Service;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * delete endpoints for a service that is linked to a client.
 *
 * @author Steve Hu
 */
public class Oauth2ClientClientIdServiceServiceIdDeleteHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(Oauth2ClientClientIdServiceServiceIdGetHandler.class);
    private static final DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    private static final String delete = "DELETE FROM client_service WHERE client_id = ? AND service_id = ?";
    private static final String scope = "SELECT DISTINCT scope FROM client_service s, service_endpoint e WHERE s.service_id = e.service_id AND s.endpoint = e.endpoint AND client_id = ?";
    private static final String CLIENT_NOT_FOUND = "ERR12014";
    private static final String SERVICE_NOT_FOUND = "ERR12015";

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        // ensure that both clientId and serviceId exist.
        String clientId = exchange.getQueryParameters().get("clientId").getFirst();
        IMap<String, Client> clients = CacheStartupHookProvider.hz.getMap("clients");
        Client client = clients.get(clientId);
        if(client == null) {
            Status status = new Status(CLIENT_NOT_FOUND, clientId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        }

        String serviceId = exchange.getQueryParameters().get("serviceId").getFirst();
        IMap<String, Service> services = CacheStartupHookProvider.hz.getMap("services");
        if(services.get(serviceId) == null) {
            Status status = new Status(SERVICE_NOT_FOUND, serviceId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        }

        Map<String, Object> result = new HashMap<>();
        try (Connection connection = ds.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement stmt = connection.prepareStatement(delete)) {
                stmt.setString(1, clientId);
                stmt.setString(2, serviceId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Exception:", e);
                connection.rollback();
                throw new RuntimeException(e);
            }

            StringJoiner joiner = new StringJoiner(" ");
            try (PreparedStatement stmt = connection.prepareStatement(scope)) {
                stmt.setString(1, clientId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        joiner.add(rs.getString("scope"));
                    }
                }
            }

            String s = Arrays.stream(joiner.toString().split(" "))
                    .distinct()
                    .filter(st -> !st.isEmpty())
                    .collect(Collectors.joining(" "));

            result.put("old_scope", client.getScope());
            client.setScope(s);
            result.put("new_scope", s);

            connection.commit();
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(result));
    }
}
