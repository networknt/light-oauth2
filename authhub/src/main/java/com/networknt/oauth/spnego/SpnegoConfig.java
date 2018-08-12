package com.networknt.oauth.spnego;

public class SpnegoConfig {
    String debug;
    String useKeyTab;
    String keyTab;
    String principal;

    public String getDebug() {
        return debug;
    }

    public void setDebug(String debug) {
        this.debug = debug;
    }

    public String getUseKeyTab() {
        return useKeyTab;
    }

    public void setUseKeyTab(String useKeyTab) {
        this.useKeyTab = useKeyTab;
    }

    public String getKeyTab() {
        return keyTab;
    }

    public void setKeyTab(String keyTab) {
        this.keyTab = keyTab;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }
}
