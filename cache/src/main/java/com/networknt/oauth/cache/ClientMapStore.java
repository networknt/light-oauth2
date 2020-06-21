package com.networknt.oauth.cache;

import com.hazelcast.core.MapStore;
import com.networknt.oauth.cache.model.Client;
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
public class ClientMapStore implements MapStore<String, Client> {
    private static final Logger logger = LoggerFactory.getLogger(ClientMapStore.class);
    private static final DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    private static final String insert = "INSERT INTO client (client_id, client_secret, client_type, client_profile, client_name, client_desc, scope, custom_claim, redirect_uri, authenticate_class, deref_client_id, owner_id, host) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String delete = "DELETE FROM client WHERE client_id = ?";
    private static final String select = "SELECT * FROM client WHERE client_id = ?";
    private static final String update = "UPDATE client SET client_type=?, client_profile=?, client_name=?, client_desc=?, scope=?, custom_claim=?, redirect_uri=?, authenticate_class=?, deref_client_id=?, owner_id=?, host=? WHERE client_id=?";
    private static final String loadall = "SELECT client_id FROM client";

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
    public synchronized void store(String key, Client client) {
        if(logger.isDebugEnabled()) logger.debug("Store:"  + key);
        if(load(key) == null) {
            try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(insert)) {
                stmt.setString(1, client.getClientId());
                stmt.setString(2, client.getClientSecret());
                stmt.setString(3, client.getClientType().toString());
                stmt.setString(4, client.getClientProfile().toString());
                stmt.setString(5, client.getClientName());
                stmt.setString(6, client.getClientDesc());
                stmt.setString(7, client.getScope());
                stmt.setString(8, client.getCustomClaim());
                stmt.setString(9, client.getRedirectUri());
                stmt.setString(10, client.getAuthenticateClass());
                stmt.setString(11, client.getDerefClientId());
                stmt.setString(12, client.getOwnerId());
                stmt.setString(13, client.getHost());
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Exception:", e);
                throw new RuntimeException(e);
            }
        } else {
            try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(update)) {
                stmt.setString(1, client.getClientType().toString());
                stmt.setString(2, client.getClientProfile().toString());
                stmt.setString(3, client.getClientName());
                stmt.setString(4, client.getClientDesc());
                stmt.setString(5, client.getScope());
                stmt.setString(6, client.getCustomClaim());
                stmt.setString(7, client.getRedirectUri());
                stmt.setString(8, client.getAuthenticateClass());
                stmt.setString(9, client.getDerefClientId());
                stmt.setString(10, client.getOwnerId());
                stmt.setString(11, client.getHost());
                stmt.setString(12, client.getClientId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Exception:", e);
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public synchronized void storeAll(Map<String, Client> map) {
        for (Map.Entry<String, Client> entry : map.entrySet())
            store(entry.getKey(), entry.getValue());
    }
    @Override
    public synchronized void deleteAll(Collection<String> keys) {
        keys.forEach(this::delete);
    }
    @Override
    public synchronized Client load(String key) {
        if(logger.isDebugEnabled()) logger.debug("Load:"  + key);
        Client client = null;
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(select)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    client = new Client();
                    client.setClientId(key);
                    client.setClientSecret(rs.getString("client_secret"));
                    client.setClientType(Client.ClientTypeEnum.fromValue(rs.getString("client_type")));
                    client.setClientProfile(Client.ClientProfileEnum.fromValue(rs.getString("client_profile")));
                    client.setClientName(rs.getString("client_name"));
                    client.setClientDesc(rs.getString("client_desc"));
                    client.setScope(rs.getString("scope"));
                    client.setCustomClaim(rs.getString("custom_claim"));
                    client.setRedirectUri(rs.getString("redirect_uri"));
                    client.setAuthenticateClass(rs.getString("authenticate_class"));
                    client.setDerefClientId(rs.getString("deref_client_id"));
                    client.setOwnerId(rs.getString("owner_id"));
                    client.setHost(rs.getString("host"));
                }
            }
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }
        return client;
    }
    @Override
    public synchronized Map<String, Client> loadAll(Collection<String> keys) {
        Map<String, Client> result = new HashMap<>();
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
                    keys.add(rs.getString("client_id"));
                }
            }
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }
        return keys;
    }

}
