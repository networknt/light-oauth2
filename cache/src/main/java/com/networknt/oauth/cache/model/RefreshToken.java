package com.networknt.oauth.cache.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import io.swagger.annotations.ApiModelProperty;

import java.io.IOException;
import java.util.Objects;

public class RefreshToken implements IdentifiedDataSerializable {
  private String refreshToken = null;

  private String userId = null;

  private String clientId = null;

  private String scope = null;

  private String csrf = null;

  public RefreshToken refreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "refresh token")
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

  
  @ApiModelProperty(example = "null", required = true, value = "user id")
  @JsonProperty("userId")
  public String getUserId() {
    return userId;
  }
  public void setUserId(String userId) {
    this.userId = userId;
  }

  public RefreshToken clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "client id")
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

  
  @ApiModelProperty(example = "null", value = "service scopes separated by space")
  @JsonProperty("scope")
  public String getScope() {
    return scope;
  }
  public void setScope(String scope) {
    this.scope = scope;
  }

  public RefreshToken csrf(String csrf) {
    this.csrf = csrf;
    return this;
  }


  @ApiModelProperty(example = "null", value = "csrf")
  @JsonProperty("csrf")
  public String getCsrf() {
    return csrf;
  }
  public void setCsrf(String csrf) {
    this.csrf = csrf;
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
        Objects.equals(clientId, token.clientId) &&
        Objects.equals(csrf, token.csrf) &&
        Objects.equals(scope, token.scope);

  }

  @Override
  public int hashCode() {
    return Objects.hash(refreshToken, userId, clientId, scope, csrf);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RefreshToken {\n");
    
    sb.append("    refreshToken: ").append(toIndentedString(refreshToken)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
    sb.append("    csrf: ").append(toIndentedString(csrf)).append("\n");
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
    this.clientId = in.readUTF();
    this.scope = in.readUTF();
    this.csrf = in.readUTF();
  }

  @Override
  public void writeData(ObjectDataOutput out) throws IOException {
    out.writeUTF(this.refreshToken);
    out.writeUTF(this.userId);
    out.writeUTF(this.clientId);
    out.writeUTF(this.scope);
    out.writeUTF(this.csrf);
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

