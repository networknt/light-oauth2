package com.networknt.oauth.code.handler;

import com.networknt.config.Config;
import com.networknt.oauth.cache.AuditInfoHandler;
import com.networknt.oauth.cache.model.AuditInfo;
import com.networknt.oauth.cache.model.Oauth2Service;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CodeAuditHandler extends AuditInfoHandler {
    static final Logger logger = LoggerFactory.getLogger(CodeAuditHandler.class);
    private final static String CONFIG = "oauth_code";
    private final static OauthCodeConfig config = (OauthCodeConfig) Config.getInstance().getJsonObjectConfig(CONFIG, OauthCodeConfig.class);


    protected void processAudit(HttpServerExchange exchange) throws Exception{
        if (config.isEnableAudit() ) {
            AuditInfo auditInfo = new AuditInfo();
            auditInfo.setServiceId(Oauth2Service.REFRESHTOKEN);
            auditInfo.setEndpoint(exchange.getHostName() + exchange.getRelativePath());
            auditInfo.setRequestHeader(Config.getInstance().getMapper().writeValueAsString(exchange.getRequestHeaders()));
            auditInfo.setRequestBody(Config.getInstance().getMapper().writeValueAsString(exchange.getRequestCookies()));
            auditInfo.setResponseHeader(Config.getInstance().getMapper().writeValueAsString(exchange.getResponseHeaders()));
            auditInfo.setResponseBody(Config.getInstance().getMapper().writeValueAsString(exchange.getResponseCookies()));
            saveAudit(auditInfo);
        }
    }


}
