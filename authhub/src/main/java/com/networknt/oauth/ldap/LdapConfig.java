package com.networknt.oauth.ldap;

public class LdapConfig {
    String uri;
    String domain;
    String ldapPrincipal;
    String searchFilter;
    String searchBase;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDomain() { return domain; }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getLdapPrincipal() {
        return ldapPrincipal;
    }

    public void setLdapPrincipal(String ldapPrincipal) {
        this.ldapPrincipal = ldapPrincipal;
    }

    public String getSearchFilter() { return searchFilter; }

    public void setSearchFilter(String searchFilter) { this.searchFilter = searchFilter; }

    public String getSearchBase() { return searchBase; }

    public void setSearchBase(String searchBase) { this.searchBase = searchBase; }
}
