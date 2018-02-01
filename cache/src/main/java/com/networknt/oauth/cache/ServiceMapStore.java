package com.networknt.oauth.cache;

import com.hazelcast.core.MapStore;
import com.networknt.oauth.cache.model.Service;
import com.networknt.service.SingletonServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by stevehu on 2016-12-27.
 */
public class ServiceMapStore implements MapStore<String, Service> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceMapStore.class);
    private static final DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    private static final String insert = "INSERT INTO service (service_id, service_type, service_name, service_desc, scope, owner_id) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String delete = "DELETE FROM service WHERE service_id = ?";
    private static final String select = "SELECT * FROM service WHERE service_id = ?";
    private static final String update = "UPDATE service SET service_type = ?, service_name=?, service_desc=?, scope=?, owner_id=? WHERE service_id=?";
    private static final String loadall = "SELECT service_id FROM service";

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
    public synchronized void store(String key, Service service) {
        if(logger.isDebugEnabled()) logger.debug("Store:"  + key);
        if(load(key) == null) {
            try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(insert)) {
                stmt.setString(1, service.getServiceId());
                stmt.setString(2, service.getServiceType().toString());
                stmt.setString(3, service.getServiceName());
                stmt.setString(4, service.getServiceDesc());
                stmt.setString(5, service.getScope());
                stmt.setString(6, service.getOwnerId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Exception:", e);
                throw new RuntimeException(e);
            }
        } else {
            try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(update)) {
                stmt.setString(1, service.getServiceType().toString());
                stmt.setString(2, service.getServiceName());
                stmt.setString(3, service.getServiceDesc());
                stmt.setString(4, service.getScope());
                stmt.setString(5, service.getOwnerId());
                stmt.setString(6, service.getServiceId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Exception:", e);
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public synchronized void storeAll(Map<String, Service> map) {
        for (Map.Entry<String, Service> entry : map.entrySet())
            store(entry.getKey(), entry.getValue());
    }
    @Override
    public synchronized void deleteAll(Collection<String> keys) {
        keys.forEach(this::delete);
    }
    @Override
    public synchronized Service load(String key) {
        if(logger.isDebugEnabled()) logger.debug("Load:"  + key);
        Service service = null;
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(select)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    service = new Service();
                    service.setServiceId(key);
                    service.setServiceType(Service.ServiceTypeEnum.fromValue(rs.getString("service_type")));
                    service.setServiceName(rs.getString("service_name"));
                    service.setServiceDesc(rs.getString("service_desc"));
                    service.setScope(rs.getString("scope"));
                    service.setOwnerId(rs.getString("owner_id"));
                }
            }
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }
        return service;
    }
    @Override
    public synchronized Map<String, Service> loadAll(Collection<String> keys) {
        Map<String, Service> result = new HashMap<>();
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
