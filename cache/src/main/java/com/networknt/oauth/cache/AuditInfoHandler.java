package com.networknt.oauth.cache;

import com.networknt.oauth.cache.model.AuditInfo;
import com.networknt.service.SingletonServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;


public class AuditInfoHandler {
    static final Logger logger = LoggerFactory.getLogger(AuditInfoHandler.class);
    static final DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    private static final String insert = "INSERT INTO audit_log (log_id, service_id, endpoint, request_header, request_body, response_code, response_header, response_body) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String clean = "DELETE FROM audit_log WHERE log_id < ?";

    protected void saveAudit(AuditInfo auditInfo) {
        if(logger.isDebugEnabled()) logger.debug("Store audit info:"  + auditInfo.getServiceId().name() + "; " + auditInfo.getEndpoint());
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(insert)) {
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, auditInfo.getServiceId().name());
            stmt.setString(3, auditInfo.getEndpoint());
            stmt.setString(4, auditInfo.getRequestHeader());
            stmt.setString(5, auditInfo.getRequestBody());
            stmt.setInt(6, auditInfo.getResponseCode()==null? 0: auditInfo.getResponseCode());
            stmt.setString(7, auditInfo.getResponseHeader());
            stmt.setString(8, auditInfo.getResponseBody());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }

    }

    public void clean(Date date) {
        if(logger.isDebugEnabled()) logger.debug("Clean the records older than:" + date);
        long logId = date.getTime();
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(clean)) {
            stmt.setLong(1, logId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }
    }
}
