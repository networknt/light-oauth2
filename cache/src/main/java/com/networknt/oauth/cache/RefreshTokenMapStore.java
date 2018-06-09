package com.networknt.oauth.cache;

import com.hazelcast.core.MapStore;
import com.networknt.oauth.cache.model.RefreshToken;
import com.networknt.oauth.cache.model.User;
import com.networknt.service.SingletonServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class RefreshTokenMapStore implements MapStore<String, RefreshToken> {
    static final Logger logger = LoggerFactory.getLogger(RefreshTokenMapStore.class);
    static final DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    private static final String insert = "INSERT INTO refresh_token (user_id, client_id, scope, refresh_token) VALUES (?, ?, ?, ?)";
    private static final String delete = "DELETE FROM refresh_token WHERE refresh_token = ?";
    private static final String select = "SELECT * FROM refresh_token WHERE refresh_token = ?";
    private static final String update = "UPDATE refresh_token SET  scope=? WHERE refresh_token = ?";
    private static final String loadall = "SELECT refresh_token FROM refresh_token";


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
    public synchronized void store(String key, RefreshToken token) {
        if(logger.isDebugEnabled()) logger.debug("Store:"  + key);
        if(load(key) == null) {
            try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(insert)) {
                stmt.setString(1, token.getUserId());
                stmt.setString(2, token.getClientId());
                stmt.setString(3, token.getScope());
                stmt.setString(4, token.getRefreshToken());
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Exception:", e);
                throw new RuntimeException(e);
            }
        } else {
            /* there shouldn't have update by token

            */
        }
    }
    @Override
    public synchronized void storeAll(Map<String, RefreshToken> map) {
        for (Map.Entry<String, RefreshToken> entry : map.entrySet())
            store(entry.getKey(), entry.getValue());
    }
    @Override
    public synchronized void deleteAll(Collection<String> keys) {
        keys.forEach(this::delete);
    }
    @Override
    public synchronized RefreshToken load(String key) {
        if(logger.isDebugEnabled()) logger.debug("Load:"  + key);
        RefreshToken token = null;
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(select)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    token = new RefreshToken();
                    token.setRefreshToken(key);
                    token.setUserId(rs.getString("user_id"));
                    token.setClientId(rs.getString("client_id"));
                    token.setScope(rs.getString("scope"));
                }
            }
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }
        return token;
    }
    @Override
    public synchronized Map<String, RefreshToken> loadAll(Collection<String> keys) {
        Map<String, RefreshToken> result = new HashMap<>();
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
                    keys.add(rs.getString("refresh_token"));
                }
            }
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }
        return keys;
    }
}
