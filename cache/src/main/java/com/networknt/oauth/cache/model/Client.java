package com.networknt.oauth.cache.model;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import io.swagger.annotations.ApiModelProperty;
import java.sql.Date;

public class Client implements IdentifiedDataSerializable {
  private String clientId = null;

  private String clientSecret = null;

  /**
   * client type
   */
  public enum ClientTypeEnum {
    CONFIDENTIAL("confidential"),
    
    PUBLIC("public"),
    
    TRUSTED("trusted");

    private final String value;

    ClientTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ClientTypeEnum fromValue(String text) {
      for (ClientTypeEnum b : ClientTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  private ClientTypeEnum clientType = null;
  /**
   * client profile
   */
  public enum ClientProfileEnum {
    WEBSERVER("webserver"),
    
    BROWSER("browser"),
    
    MOBILE("mobile"),
    
    SERVICE("service"),
    
    BATCH("batch");

    private final String value;

    ClientProfileEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ClientProfileEnum fromValue(String text) {
      for (ClientProfileEnum b : ClientProfileEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  private ClientProfileEnum clientProfile = null;

  private String clientName = null;

  private String clientDesc = null;

  private String ownerId = null;

  private String scope = null;

  private String redirectUri = null;

  private Date createDt = null;

  private Date updateDt = null;

  public Client clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "a unique client id")
  @JsonProperty("clientId")
  public String getClientId() {
    return clientId;
  }
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public Client clientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "client secret")
  @JsonProperty("clientSecret")
  public String getClientSecret() {
    return clientSecret;
  }
  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public Client clientType(ClientTypeEnum clientType) {
    this.clientType = clientType;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "client type")
  @JsonProperty("clientType")
  public ClientTypeEnum getClientType() {
    return clientType;
  }
  public void setClientType(ClientTypeEnum clientType) {
    this.clientType = clientType;
  }

  public Client clientProfile(ClientProfileEnum clientProfile) {
    this.clientProfile = clientProfile;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "client profile")
  @JsonProperty("clientProfile")
  public ClientProfileEnum getClientProfile() {
    return clientProfile;
  }
  public void setClientProfile(ClientProfileEnum clientProfile) {
    this.clientProfile = clientProfile;
  }

  public Client clientName(String clientName) {
    this.clientName = clientName;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "client name")
  @JsonProperty("clientName")
  public String getClientName() {
    return clientName;
  }
  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  public Client clientDesc(String clientDesc) {
    this.clientDesc = clientDesc;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "client description")
  @JsonProperty("clientDesc")
  public String getClientDesc() {
    return clientDesc;
  }
  public void setClientDesc(String clientDesc) {
    this.clientDesc = clientDesc;
  }

  public Client ownerId(String ownerId) {
    this.ownerId = ownerId;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "client owner id")
  @JsonProperty("ownerId")
  public String getOwnerId() {
    return ownerId;
  }
  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public Client scope(String scope) {
    this.scope = scope;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "client scope separated by space")
  @JsonProperty("scope")
  public String getScope() {
    return scope;
  }
  public void setScope(String scope) {
    this.scope = scope;
  }

  public Client redirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
    return this;
  }


  @ApiModelProperty(example = "null", value = "redirect uri")
  @JsonProperty("redirectUri")
  public String getRedirectUri() {
    return redirectUri;
  }
  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  public Client createDt(Date createDt) {
    this.createDt = createDt;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "create date time")
  @JsonProperty("createDt")
  public Date getCreateDt() {
    return createDt;
  }
  public void setCreateDt(Date createDt) {
    this.createDt = createDt;
  }

  public Client updateDt(Date updateDt) {
    this.updateDt = updateDt;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "update date time")
  @JsonProperty("updateDt")
  public Date getUpdateDt() {
    return updateDt;
  }
  public void setUpdateDt(Date updateDt) {
    this.updateDt = updateDt;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Client client = (Client) o;
    return Objects.equals(clientId, client.clientId) &&
        Objects.equals(clientSecret, client.clientSecret) &&
        Objects.equals(clientType, client.clientType) &&
        Objects.equals(clientProfile, client.clientProfile) &&
        Objects.equals(clientName, client.clientName) &&
        Objects.equals(clientDesc, client.clientDesc) &&
        Objects.equals(ownerId, client.ownerId) &&
        Objects.equals(scope, client.scope) &&
        Objects.equals(redirectUri, client.redirectUri) &&
        Objects.equals(createDt, client.createDt) &&
        Objects.equals(updateDt, client.updateDt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clientId, clientSecret, clientType, clientProfile, clientName, clientDesc, ownerId, scope, redirectUri, createDt, updateDt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Client {\n");
    
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    clientSecret: ").append(toIndentedString(clientSecret)).append("\n");
    sb.append("    clientType: ").append(toIndentedString(clientType)).append("\n");
    sb.append("    clientProfile: ").append(toIndentedString(clientProfile)).append("\n");
    sb.append("    clientName: ").append(toIndentedString(clientName)).append("\n");
    sb.append("    clientDesc: ").append(toIndentedString(clientDesc)).append("\n");
    sb.append("    ownerId: ").append(toIndentedString(ownerId)).append("\n");
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
    sb.append("    redirectUri: ").append(toIndentedString(redirectUri)).append("\n");
    sb.append("    createDt: ").append(toIndentedString(createDt)).append("\n");
    sb.append("    updateDt: ").append(toIndentedString(updateDt)).append("\n");
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

  public Client() {

  }

  @Override
  public void readData(ObjectDataInput in) throws IOException {
    this.clientId = in.readUTF();
    this.clientSecret = in.readUTF();
    this.clientType = Client.ClientTypeEnum.fromValue(in.readUTF());
    this.clientProfile = Client.ClientProfileEnum.fromValue(in.readUTF());
    this.clientName = in.readUTF();
    this.clientDesc = in.readUTF();
    this.ownerId = in.readUTF();
    this.scope = in.readUTF();
    this.redirectUri = in.readUTF();
    this.createDt = in.readObject();
    this.updateDt = in.readObject();
  }

  @Override
  public void writeData(ObjectDataOutput out) throws IOException {
    out.writeUTF(this.clientId);
    out.writeUTF(this.clientSecret);
    out.writeUTF(this.clientType.toString());
    out.writeUTF(this.clientProfile.toString());
    out.writeUTF(this.clientName);
    out.writeUTF(this.clientDesc);
    out.writeUTF(this.ownerId);
    out.writeUTF(this.scope);
    out.writeUTF(this.redirectUri);
    out.writeObject(this.createDt);
    out.writeObject(this.updateDt);
  }

  @JsonIgnore
  @Override
  public int getFactoryId() {
    return ClientDataSerializableFactory.ID;
  }

  @JsonIgnore
  @Override
  public int getId() {
    return ClientDataSerializableFactory.CLIENT_TYPE;
  }


}

