package com.networknt.oauth.code.github;

public class GithubConfig {
	String protocol;
	String host;
	String pathPrefix;
	String owner;
	String repo;
	String path;
	
	public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    
    public String getPathPrefix() { return pathPrefix; }
    public void setPathPrefix(String pathPrefix) { this.pathPrefix = pathPrefix; }
    
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    
    public String getRepo() { return repo; }
    public void setRepo(String repo) { this.repo = repo; }
    
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    
}
