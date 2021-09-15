package com.networknt.oauth.token.handler;

import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.oauth.cache.AuditInfoHandler;
import com.networknt.oauth.cache.model.AuditInfo;
import com.networknt.oauth.cache.model.Oauth2Service;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenAuditHandler extends AuditInfoHandler {
    static final Logger logger = LoggerFactory.getLogger(TokenAuditHandler.class);
    private final static String CONFIG = "oauth-token";
    private final static OauthTokenConfig oauthTokenConfig = (OauthTokenConfig) Config.getInstance().getJsonObjectConfig(CONFIG, OauthTokenConfig.class);


    protected void processAudit(HttpServerExchange exchange) throws Exception{
        if (oauthTokenConfig.isEnableAudit() ) {
            AuditInfo auditInfo = new AuditInfo();
            auditInfo.setServiceId(Oauth2Service.TOKEN);
            auditInfo.setEndpoint(exchange.getHostName() + exchange.getRelativePath());
            auditInfo.setRequestHeader(exchange.getRequestHeaders().toString());
            auditInfo.setRequestBody(Config.getInstance().getMapper().writeValueAsString(exchange.getAttachment(BodyHandler.REQUEST_BODY)));
            auditInfo.setResponseCode(exchange.getStatusCode());
            auditInfo.setResponseHeader(exchange.getResponseHeaders().toString());
            auditInfo.setResponseBody(Config.getInstance().getMapper().writeValueAsString(exchange.getResponseCookies()));
            saveAudit(auditInfo);
        }
    }
}
