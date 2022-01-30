package com.networknt.oauth.cache;

import com.networknt.oauth.cache.model.*;
import com.networknt.service.SingletonServiceFactory;
import org.h2.tools.RunScript;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by stevehu on 2016-12-27.
 */
public class AuditHandlerTest {

    @BeforeClass
    public static void runOnceBeforeClass() {
        System.out.println("@BeforeClass - runOnceBeforeClass");
        DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
        try (Connection connection = ds.getConnection()) {
            String schemaResourceName = "/create_h2.sql";
            InputStream in = AuditHandlerTest.class.getResourceAsStream(schemaResourceName);

            if (in == null) {
                throw new RuntimeException("Failed to load resource: " + schemaResourceName);
            }
            InputStreamReader reader = new InputStreamReader(in, UTF_8);
            RunScript.execute(connection, reader);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // Run once, e.g close connection, cleanup
    @AfterClass
    public static void runOnceAfterClass() {
        System.out.println("@AfterClass - runOnceAfterClass");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSave() {
        AuditInfoHandler auditInfoHandler = new AuditInfoHandler();

        AuditInfo auditInfo = new AuditInfo();
        auditInfo.setServiceId(Oauth2Service.SERVICE);
        auditInfo.setEndpoint("https://google.cloud.com/service");
        auditInfo.setRequestHeader("<head></head>");
        auditInfo.setRequestBody("<body></body>");
        auditInfo.setResponseCode(111);
        auditInfo.setResponseHeader("<head></head>");
        auditInfo.setResponseBody("<body></body>");

        auditInfoHandler.saveAudit(auditInfo);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testClean() throws  Exception{
        AuditInfoHandler auditInfoHandler = new AuditInfoHandler();

        String myDate = "2017/10/29";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date date = sdf.parse(myDate);
        auditInfoHandler.clean(date);

    }

}
