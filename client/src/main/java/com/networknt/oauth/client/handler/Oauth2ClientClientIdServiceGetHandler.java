
package com.networknt.oauth.client.handler;

import com.hazelcast.map.IMap;
import com.networknt.config.Config;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Client;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * get all the linked services and endpoints for a client
 *
 * @author Steve Hu
 */
public class Oauth2ClientClientIdServiceGetHandler  extends ClientAuditHandler implements LightHttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(Oauth2ClientClientIdServiceGetHandler.class);
    private static final DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    private static final String select = "SELECT * FROM client_service WHERE client_id = ? ORDER BY service_id";
    private static final String CLIENT_NOT_FOUND = "ERR12014";

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        // ensure that clientId exists.
        String clientId = exchange.getQueryParameters().get("clientId").getFirst();
        IMap<String, Client> clients = CacheStartupHookProvider.hz.getMap("clients");
        Client client = clients.get(clientId);
        if(client == null) {
            setExchangeStatus(exchange, CLIENT_NOT_FOUND, clientId);
            processAudit(exchange);
            return;
        }

        Map<String, List<String>> serviceEndpoints = new HashMap<>();

        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(select)) {
            stmt.setString(1, clientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String serviceId = rs.getString("service_id");
                    String endpoint = rs.getString("endpoint");
                    List<String> endpoints = serviceEndpoints.get(serviceId);
                    if(endpoints == null) {
                        endpoints = new ArrayList<>();
                        serviceEndpoints.put(serviceId, endpoints);
                    }
                    endpoints.add(endpoint);
                }
            }
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }

        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(serviceEndpoints));
        processAudit(exchange);
    }
}
