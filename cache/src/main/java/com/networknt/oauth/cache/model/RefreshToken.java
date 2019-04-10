package com.networknt.oauth.cache.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

import java.io.IOException;
import java.util.Objects;

/**
 * This is the cache of the previous token when create a new access token from a refresh token.
 *
 * csrf is always regenerated
 * custom claims are retrieved from client cache
 * userType and user roles are retrieved from user cache
 * @author Steve Hu
 */
public class RefreshToken implements IdentifiedDataSerializable {
  private String refreshToken = null;

  private String userId = null;

  private String userType = null;

  private String roles = null;

  private String clientId = null;

  private String scope = null;

  public RefreshToken refreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
    return this;
  }

  
  @JsonProperty("refreshToken")
  public String getRefreshToken() {
    return refreshToken;
  }
  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public RefreshToken userId(String userId) {
    this.userId = userId;
    return this;
  }

  
  @JsonProperty("userId")
  public String getUserId() {
    return userId;
  }
  public void setUserId(String userId) {
    this.userId = userId;
  }


  public RefreshToken userType(String userType) {
    this.userType = userType;
    return this;
  }


  @JsonProperty("userType")
  public String getUserType() {
    return userType;
  }
  public void setUserType(String userType) {
    this.userType = userType;
  }



  public RefreshToken roles(String roles) {
    this.roles = roles;
    return this;
  }


  @JsonProperty("roles")
  public String getRoles() {
    return roles;
  }
  public void setRoles(String roles) {
    this.roles = roles;
  }


  public RefreshToken clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  
  @JsonProperty("clientId")
  public String getClientId() {
    return clientId;
  }
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public RefreshToken scope(String scope) {
    this.scope = scope;
    return this;
  }

  
  @JsonProperty("scope")
  public String getScope() {
    return scope;
  }
  public void setScope(String scope) {
    this.scope = scope;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RefreshToken token = (RefreshToken) o;
    return Objects.equals(refreshToken, token.refreshToken) &&
        Objects.equals(userId, token.userId) &&
        Objects.equals(userType, token.userType) &&
        Objects.equals(roles, token.roles) &&
        Objects.equals(clientId, token.clientId) &&
        Objects.equals(scope, token.scope);

  }

  @Override
  public int hashCode() {
    return Objects.hash(refreshToken, userId, userType, roles, clientId, scope);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RefreshToken {\n");
    
    sb.append("    refreshToken: ").append(toIndentedString(refreshToken)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    userType: ").append(toIndentedString(userType)).append("\n");
    sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

  public RefreshToken () {

  }

  @Override
  public void readData(ObjectDataInput in) throws IOException {
    this.refreshToken = in.readUTF();
    this.userId = in.readUTF();
    this.userType = in.readUTF();
    this.roles = in.readUTF();
    this.clientId = in.readUTF();
    this.scope = in.readUTF();
  }

  @Override
  public void writeData(ObjectDataOutput out) throws IOException {
    out.writeUTF(this.refreshToken);
    out.writeUTF(this.userId);
    out.writeUTF(this.userType);
    out.writeUTF(this.roles);
    out.writeUTF(this.clientId);
    out.writeUTF(this.scope);
  }

  @JsonIgnore
  @Override
  public int getFactoryId() {
    return RefreshTokenDataSerializableFactory.ID;
  }

  @JsonIgnore
  @Override
  public int getId() {
    return RefreshTokenDataSerializableFactory.REFRESH_TOKEN_TYPE;
  }


}

