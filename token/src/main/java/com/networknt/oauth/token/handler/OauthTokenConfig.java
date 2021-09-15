package com.networknt.oauth.token.handler;

public class OauthTokenConfig {
    public static final String CONFIG_NAME = "oauth-token";
    boolean enableAudit;
    String bootstrapToken;
    String bootstrapClientId;
    String bootstrapClientSecret;
    String bootstrapScope;

    public boolean isEnableAudit() {
        return enableAudit;
    }

    public void setEnableAudit(boolean enableAudit) {
        this.enableAudit = enableAudit;
    }

    public String getBootstrapToken() {
        return bootstrapToken;
    }

    public void setBootstrapToken(String bootstrapToken) {
        this.bootstrapToken = bootstrapToken;
    }

    public String getBootstrapClientId() {
        return bootstrapClientId;
    }

    public void setBootstrapClientId(String bootstrapClientId) {
        this.bootstrapClientId = bootstrapClientId;
    }

    public String getBootstrapClientSecret() {
        return bootstrapClientSecret;
    }

    public void setBootstrapClientSecret(String bootstrapClientSecret) {
        this.bootstrapClientSecret = bootstrapClientSecret;
    }

    public String getBootstrapScope() {
        return bootstrapScope;
    }

    public void setBootstrapScope(String bootstrapScope) {
        this.bootstrapScope = bootstrapScope;
    }
}
