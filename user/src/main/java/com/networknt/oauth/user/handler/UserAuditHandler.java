package com.networknt.oauth.user.handler;

import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.oauth.cache.AuditInfoHandler;
import com.networknt.oauth.cache.model.AuditInfo;
import com.networknt.oauth.cache.model.Oauth2Service;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UserAuditHandler extends AuditInfoHandler {
    static final Logger logger = LoggerFactory.getLogger(OauthUserConfig.class);
    private final static String CONFIG = "oauth_user";
    private final static OauthUserConfig oauth_config = (OauthUserConfig) Config.getInstance().getJsonObjectConfig(CONFIG, OauthUserConfig.class);


    protected void processAudit(HttpServerExchange exchange) throws Exception{
        if (oauth_config.isEnableAudit() ) {
            AuditInfo auditInfo = new AuditInfo();
            auditInfo.setServiceId(Oauth2Service.USER);
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
