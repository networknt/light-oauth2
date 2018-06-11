package com.networknt.oauth.github;

public class GithubMetadata {
	static class Groups {
		String primary;
		String[] secondary;
		
		public String getPrimary() { return primary; }
	    public void setPrimary(String primary) { this.primary = primary; }
	    
	    public String[] getSecondary() { return secondary; }
	    public void setSecondary(String[] secondary) { this.secondary = secondary; }
	}
	String[] subscribed_api;
	String email_address;
	boolean invited;
	String name;
	String github_username;
	Groups groups;
	String[] orgs;
	String _id;
	
	public String[] getSubscribed_api() { return subscribed_api; }
    public void setSubscribed_api(String[] subscribed_api) { this.subscribed_api = subscribed_api; }
    
	public String getEmail_address() { return email_address; }
    public void setEmail_address(String email_address) { this.email_address = email_address; }
    
    public boolean getInvited() { return invited; }
    public void setInvited(boolean invited) { this.invited = invited; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getGithub_username() { return github_username; }
    public void setGithub_username(String github_username) { this.github_username = github_username; }
    
    public Groups getGroups() { return groups; }
    public void setGroups(Groups groups) { this.groups = groups; }
    
    public String[] getOrgs() { return orgs; }
    public void setOrgs(String[] orgs) { this.orgs = orgs; }
    
    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
}
