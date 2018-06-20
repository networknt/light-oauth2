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

  private String endpoint = null;

  private String port = null;

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



  @ApiModelProperty(example = "null", required = true, value = "endpoint")
  @JsonProperty("endpoint")
  public String getEndpoint() {
    return endpoint;
  }
  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }


  @ApiModelProperty(example = "null", required = true, value = "port")
  @JsonProperty("port")
  public String getPort() {
    return port;
  }
  public void setPort(String port) {
    this.port = port;
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
        Objects.equals(endpoint, provider.endpoint) &&
        Objects.equals(port, provider.port);

  }

  @Override
  public int hashCode() {
    return Objects.hash(providerId, endpoint, port);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Provider {\n");

    sb.append("    providerid: ").append(toIndentedString(providerId)).append("\n");
    sb.append("    endpoint: ").append(toIndentedString(endpoint)).append("\n");
    sb.append("    port: ").append(toIndentedString(port)).append("\n");
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
    this.endpoint = in.readUTF();
    this.port = in.readUTF();
    this.providerName = in.readUTF();
  }

  @Override
  public void writeData(ObjectDataOutput out) throws IOException {
    out.writeUTF(this.providerId);
    out.writeUTF(this.endpoint);
    out.writeUTF(this.port);
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

