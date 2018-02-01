package com.networknt.oauth.cache;

import com.hazelcast.core.MapStore;
import com.networknt.oauth.cache.model.Service;
import com.networknt.oauth.cache.model.ServiceEndpoint;
import com.networknt.service.SingletonServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ServiceEndpointMapStore implements MapStore<String, List<ServiceEndpoint>> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceEndpointMapStore.class);
    private static final DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    private static final String insert = "INSERT INTO service_endpoint (service_id, endpoint, operation, scope) VALUES (?, ?, ?, ?)";
    private static final String delete = "DELETE FROM service_endpoint WHERE service_id = ?";
    private static final String select = "SELECT * FROM service_endpoint WHERE service_id = ?";
    private static final String loadall = "SELECT service_id FROM service_endpoint";

    @Override
    public synchronized void store(String key, List<ServiceEndpoint> serviceEndpoints) {
        if(logger.isDebugEnabled()) logger.debug("Store:"  + key);
        try (Connection connection = ds.getConnection()) {
            connection.setAutoCommit(false);
            if(load(key) != null) {
                // delete all endpoints for this serviceId first
                try (PreparedStatement stmt = connection.prepareStatement(delete)) {
                    stmt.setString(1, key);
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    logger.error("Exception:", e);
                    connection.rollback();
                    throw new RuntimeException(e);
                }
            }
            try (PreparedStatement stmt = connection.prepareStatement(insert)) {
                for (ServiceEndpoint serviceEndpoint : serviceEndpoints) {
                    stmt.setString(1, key);
                    stmt.setString(2, serviceEndpoint.getEndpoint());
                    stmt.setString(3, serviceEndpoint.getOperation());
                    stmt.setString(4, serviceEndpoint.getScope());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            } catch (SQLException e) {
                logger.error("Exception:", e);
                connection.rollback();
                throw new RuntimeException(e);
            }
            connection.commit();
        } catch (SQLException e) {
            logger.error("SQLException:", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void storeAll(Map<String, List<ServiceEndpoint>> map) {
        for (Map.Entry<String, List<ServiceEndpoint>> entry : map.entrySet())
            store(entry.getKey(), entry.getValue());
    }

    @Override
    public synchronized void delete(String key) {
        if(logger.isDebugEnabled()) logger.debug("Delete:" + key);
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(delete)) {
            stmt.setString(1, key);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void deleteAll(Collection<String> keys) {
        keys.forEach(this::delete);
    }

    @Override
    public synchronized List<ServiceEndpoint> load(String key) {
        if(logger.isDebugEnabled()) logger.debug("Load:"  + key);
        List<ServiceEndpoint> serviceEndpoints = new ArrayList<>();
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(select)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ServiceEndpoint serviceEndpoint = new ServiceEndpoint();
                    serviceEndpoint.setEndpoint(rs.getString("endpoint"));
                    serviceEndpoint.setOperation(rs.getString("operation"));
                    serviceEndpoint.setScope(rs.getString("scope"));
                    serviceEndpoints.add(serviceEndpoint);
                }
            }
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }
        return serviceEndpoints;
    }

    @Override
    public synchronized Map<String, List<ServiceEndpoint>> loadAll(Collection<String> keys) {
        Map<String, List<ServiceEndpoint>> result = new HashMap<>();
        for (String key : keys) result.put(key, load(key));
        return result;
    }

    @Override
    public Iterable<String> loadAllKeys() {
        if(logger.isDebugEnabled()) logger.debug("loadAllKeys is called");
        List<String> keys = new ArrayList<>();
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(loadall)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    keys.add(rs.getString("service_id"));
                }
            }
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }
        return keys;
    }
}
