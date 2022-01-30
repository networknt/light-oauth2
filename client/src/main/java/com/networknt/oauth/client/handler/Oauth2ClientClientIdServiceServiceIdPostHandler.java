
package com.networknt.oauth.client.handler;

import com.hazelcast.map.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Client;
import com.networknt.oauth.cache.model.Service;
import com.networknt.service.SingletonServiceFactory;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Add one or more endpoints of a service to a client
 *
 * @author Steve Hu
 */
public class Oauth2ClientClientIdServiceServiceIdPostHandler  extends ClientAuditHandler implements LightHttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(Oauth2ClientClientIdServiceServiceIdPostHandler.class);
    private static final DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    private static final String insert = "INSERT INTO client_service (client_id, service_id, endpoint) VALUES (?, ?, ?)";
    private static final String delete = "DELETE FROM client_service WHERE client_id = ? AND service_id = ?";
    private static final String scope = "SELECT DISTINCT scope FROM client_service s, service_endpoint e WHERE s.service_id = e.service_id AND s.endpoint = e.endpoint AND client_id = ?";
    private static final String update = "UPDATE client SET scope = ? WHERE client_id = ?";
    private static final String CLIENT_NOT_FOUND = "ERR12014";
    private static final String SERVICE_NOT_FOUND = "ERR12015";

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        // ensure that both clientId and serviceId exist.
        String clientId = exchange.getQueryParameters().get("clientId").getFirst();
        IMap<String, Client> clients = CacheStartupHookProvider.hz.getMap("clients");
        Client client = clients.get(clientId);

        if(client == null) {
            setExchangeStatus(exchange, CLIENT_NOT_FOUND, clientId);
            processAudit(exchange);
            return;
        }

        String serviceId = exchange.getQueryParameters().get("serviceId").getFirst();
        IMap<String, Service> services = CacheStartupHookProvider.hz.getMap("services");
        if(services.get(serviceId) == null) {
            setExchangeStatus(exchange, SERVICE_NOT_FOUND, serviceId);
            processAudit(exchange);
            return;
        }
        List<String> endpoints = (List)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        if(logger.isDebugEnabled()) logger.debug("Create endpoints {} for clientId {}, serviceId {}", endpoints, clientId, serviceId);
        Map<String, Object> result = new HashMap<>();
        if(endpoints != null && endpoints.size() > 0) {
            try (Connection connection = ds.getConnection()) {
                connection.setAutoCommit(false);
                // remove existing endpoints and add new ones.
                try (PreparedStatement stmt = connection.prepareStatement(delete)) {
                    stmt.setString(1, clientId);
                    stmt.setString(2, serviceId);
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    logger.error("Exception:", e);
                    connection.rollback();
                    throw new RuntimeException(e);
                }
                try (PreparedStatement stmt = connection.prepareStatement(insert)) {
                    for (String endpoint : endpoints) {
                        stmt.setString(1, clientId);
                        stmt.setString(2, serviceId);
                        stmt.setString(3, endpoint);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
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
                connection.commit();

                String s = Arrays.stream(joiner.toString().split(" "))
                        .distinct()
                        .filter(st -> !st.isEmpty())
                        .collect(Collectors.joining(" "));
                // update client scope in cache and db
                result.put("old_scope", client.getScope());
                client.setScope(s);
                clients.set(clientId, client);
                result.put("new_scope", s);


            } catch (SQLException e) {
                logger.error("SQLException:", e);
                throw new RuntimeException(e);
            }
        }
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(result));
        processAudit(exchange);
    }
}
