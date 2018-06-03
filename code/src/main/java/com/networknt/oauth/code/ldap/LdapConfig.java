package com.networknt.oauth.code.ldap;

public class LdapConfig {
    String uri;
    String domain;
    String dn;
    String ldapPrincipal;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public String getLdapPrincipal() {
        return ldapPrincipal;
    }

    public void setLdapPrincipal(String ldapPrincipal) {
        this.ldapPrincipal = ldapPrincipal;
    }
}
