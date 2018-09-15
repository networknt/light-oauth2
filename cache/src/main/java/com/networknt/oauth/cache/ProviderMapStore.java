
package com.networknt.oauth.cache;

import com.hazelcast.core.MapStore;
import com.networknt.oauth.cache.model.Provider;
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
public class ProviderMapStore implements MapStore<String, Provider> {
    private static final Logger logger = LoggerFactory.getLogger(ProviderMapStore.class);
    private static final DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    private static final String insert = "INSERT INTO oauth_provider (provider_id, server_url, uri, provider_name) VALUES (?, ?, ?, ?)";
    private static final String delete = "DELETE FROM oauth_provider WHERE provider_id = ?";
    private static final String select = "SELECT * FROM oauth_provider WHERE provider_id = ?";
    private static final String update = "UPDATE oauth_provider SET server_url=?, uri=?, provider_name=? WHERE provider_id=?";
    private static final String loadall = "SELECT provider_id FROM oauth_provider";

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
    public synchronized void store(String key, Provider provider) {
        if(logger.isDebugEnabled()) logger.debug("Store:"  + key);
        if(load(key) == null) {
            try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(insert)) {
                stmt.setString(1, provider.getProviderId());
                stmt.setString(2, provider.getServerUrl());
                stmt.setString(3, provider.getUri());
                stmt.setString(4, provider.getProviderName());

                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Exception:", e);
                throw new RuntimeException(e);
            }
        } else {
            try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(update)) {
                stmt.setString(1, provider.getServerUrl());
                stmt.setString(2, provider.getUri());
                stmt.setString(3, provider.getProviderName());
                stmt.setString(4, provider.getProviderId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Exception:", e);
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public synchronized void storeAll(Map<String, Provider> map) {
        for (Map.Entry<String, Provider> entry : map.entrySet())
            store(entry.getKey(), entry.getValue());
    }
    @Override
    public synchronized void deleteAll(Collection<String> keys) {
        keys.forEach(this::delete);
    }
    @Override
    public synchronized Provider load(String key) {
        if(logger.isDebugEnabled()) logger.debug("Load:"  + key);
        Provider provider = null;
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(select)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    provider = new Provider();
                    provider.setProviderId(key);
                    provider.setServerUrl(rs.getString("server_url"));
                    provider.setUri(rs.getString("uri"));
                    provider.setProviderName(rs.getString("provider_name"));
                }
            }
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }
        return provider;
    }
    @Override
    public synchronized Map<String, Provider> loadAll(Collection<String> keys) {
        Map<String, Provider> result = new HashMap<>();
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
                    keys.add(rs.getString("provider_id"));
                }
            }
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }
        return keys;
    }

}

