package com.networknt.oauth.cache.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import io.swagger.annotations.ApiModelProperty;

import java.io.IOException;
import java.util.Objects;

/**
 * This is the cache object Oauth service provider
 *
 */
public class Provider implements IdentifiedDataSerializable {
  private String providerId = null;

  private String serverUrl = null;

  private String uri = null;

  private String providerName = null;


  public Provider Provider(String providerId) {
    this.providerId = providerId;
    return this;
  }


  @ApiModelProperty(example = "null", required = true, value = "provider id")
  @JsonProperty("providerId")
  public String getProviderId() {
    return providerId;
  }
  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }



  @ApiModelProperty(example = "null", required = true, value = "serverUrl")
  @JsonProperty("serverUrl")
  public String getServerUrl() {
    return serverUrl;
  }
  public void setServerUrl(String serverUrl) {
    this.serverUrl = serverUrl;
  }


  @ApiModelProperty(example = "null", required = true, value = "uri")
  @JsonProperty("uri")
  public String getUri() {
    return uri;
  }
  public void setUri(String uri) {
    this.uri = uri;
  }


  @ApiModelProperty(example = "null", value = "provider name")
  @JsonProperty("providerName")
  public String getProviderName() {
    return providerName;
  }
  public void setProviderName(String providerName) {
    this.providerName = providerName;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Provider provider = (Provider) o;
    return Objects.equals(providerId, provider.providerId) &&
            Objects.equals(serverUrl, provider.serverUrl) &&
            Objects.equals(uri, provider.uri);

  }

  @Override
  public int hashCode() {
    return Objects.hash(providerId, serverUrl, uri);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Provider {\n");

    sb.append("    providerid: ").append(toIndentedString(providerId)).append("\n");
    sb.append("    serverUrl: ").append(toIndentedString(serverUrl)).append("\n");
    sb.append("    uri: ").append(toIndentedString(uri)).append("\n");
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

  public Provider() {

  }

  @Override
  public void readData(ObjectDataInput in) throws IOException {
    this.providerId = in.readUTF();
    this.serverUrl = in.readUTF();
    this.uri = in.readUTF();
    this.providerName = in.readUTF();
  }

  @Override
  public void writeData(ObjectDataOutput out) throws IOException {
    out.writeUTF(this.providerId);
    out.writeUTF(this.serverUrl);
    out.writeUTF(this.uri);
    out.writeUTF(this.providerName);
  }

  @JsonIgnore
  @Override
  public int getFactoryId() {
    return ProviderDataSerializableFactory.ID;
  }

  @JsonIgnore
  @Override
  public int getId() {
    return ProviderDataSerializableFactory.PROVIDER_TYPE;
  }


}

