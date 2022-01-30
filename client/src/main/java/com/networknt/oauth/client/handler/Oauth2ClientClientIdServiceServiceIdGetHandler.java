
package com.networknt.oauth.client.handler;

import com.hazelcast.map.IMap;
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
import java.util.ArrayList;
import java.util.List;

/**
 * get a list of endpoints of a service linked to a client
 *
 * @author Steve Hu
 */
public class Oauth2ClientClientIdServiceServiceIdGetHandler  extends ClientAuditHandler implements LightHttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(Oauth2ClientClientIdServiceServiceIdGetHandler.class);
    private static final DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    private static final String select = "SELECT * FROM client_service WHERE client_id = ? AND service_id = ?";
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

        List<String> endpoints = new ArrayList<>();
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(select)) {
            stmt.setString(1, clientId);
            stmt.setString(2, serviceId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    endpoints.add(rs.getString("endpoint"));
                }
            }
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }

        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(endpoints));
        processAudit(exchange);
    }
}
