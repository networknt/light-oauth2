package com.networknt.oauth.service.handler;

public class OauthServiceConfig {
    boolean enableAudit;
    String consoleURL;

    public boolean isEnableAudit() {
        return enableAudit;
    }

    public void setEnableAudit(boolean enableAudit) {
        this.enableAudit = enableAudit;
    }

	public String getConsoleURL() {
		return consoleURL;
	}

	public void setConsoleURL(String consoleURL) {
		this.consoleURL = consoleURL;
	}
}
