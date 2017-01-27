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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;

public class Service implements IdentifiedDataSerializable {
  private String serviceId = null;

  /**
   * service type
   */
  public enum ServiceTypeEnum {
    MS("ms"),
    
    API("api");

    private final String value;

    ServiceTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ServiceTypeEnum fromValue(String text) {
      for (ServiceTypeEnum b : ServiceTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  private ServiceTypeEnum serviceType = null;

  private String serviceName = null;

  private String serviceDesc = null;

  private String ownerId = null;

  private String scope = null;

  private Date createDt = null;

  private Date updateDt = null;

  public Service serviceId(String serviceId) {
    this.serviceId = serviceId;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "a unique service id")
  @JsonProperty("serviceId")
  public String getServiceId() {
    return serviceId;
  }
  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public Service serviceType(ServiceTypeEnum serviceType) {
    this.serviceType = serviceType;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "service type")
  @JsonProperty("serviceType")
  public ServiceTypeEnum getServiceType() {
    return serviceType;
  }
  public void setServiceType(ServiceTypeEnum serviceType) {
    this.serviceType = serviceType;
  }

  public Service serviceName(String serviceName) {
    this.serviceName = serviceName;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "service name")
  @JsonProperty("serviceName")
  public String getServiceName() {
    return serviceName;
  }
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public Service serviceDesc(String serviceDesc) {
    this.serviceDesc = serviceDesc;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "service description")
  @JsonProperty("serviceDesc")
  public String getServiceDesc() {
    return serviceDesc;
  }
  public void setServiceDesc(String serviceDesc) {
    this.serviceDesc = serviceDesc;
  }

  public Service ownerId(String ownerId) {
    this.ownerId = ownerId;
    return this;
  }

  
  @ApiModelProperty(example = "null", value = "service owner userId")
  @JsonProperty("ownerId")
  public String getOwnerId() {
    return ownerId;
  }
  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public Service scope(String scope) {
    this.scope = scope;
    return this;
  }

  
  @ApiModelProperty(example = "null", required = true, value = "service scopes separated by space")
  @JsonProperty("scope")
  public String getScope() {
    return scope;
  }
  public void setScope(String scope) {
    this.scope = scope;
  }

  public Service createDt(Date createDt) {
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

  public Service updateDt(Date updateDt) {
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
    Service service = (Service) o;
    return Objects.equals(serviceId, service.serviceId) &&
        Objects.equals(serviceType, service.serviceType) &&
        Objects.equals(serviceName, service.serviceName) &&
        Objects.equals(serviceDesc, service.serviceDesc) &&
        Objects.equals(ownerId, service.ownerId) &&
        Objects.equals(scope, service.scope) &&
        Objects.equals(createDt, service.createDt) &&
        Objects.equals(updateDt, service.updateDt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serviceId, serviceType, serviceName, serviceDesc, ownerId, scope, createDt, updateDt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Service {\n");
    
    sb.append("    serviceId: ").append(toIndentedString(serviceId)).append("\n");
    sb.append("    serviceType: ").append(toIndentedString(serviceType)).append("\n");
    sb.append("    serviceName: ").append(toIndentedString(serviceName)).append("\n");
    sb.append("    serviceDesc: ").append(toIndentedString(serviceDesc)).append("\n");
    sb.append("    ownerId: ").append(toIndentedString(ownerId)).append("\n");
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
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

  public Service() {

  }

  @Override
  public void readData(ObjectDataInput in) throws IOException {
    this.serviceId = in.readUTF();
    this.serviceType = Service.ServiceTypeEnum.fromValue(in.readUTF());
    this.serviceName = in.readUTF();
    this.serviceDesc = in.readUTF();
    this.ownerId = in.readUTF();
    this.scope = in.readUTF();
    this.createDt = in.readObject();
    this.updateDt = in.readObject();
  }

  @Override
  public void writeData(ObjectDataOutput out) throws IOException {
    out.writeUTF(this.serviceId);
    out.writeUTF(this.serviceType.toString());
    out.writeUTF(this.serviceName);
    out.writeUTF(this.serviceDesc);
    out.writeUTF(this.ownerId);
    out.writeUTF(this.scope);
    out.writeObject(this.createDt);
    out.writeObject(this.updateDt);
  }

  @JsonIgnore
  @Override
  public int getFactoryId() {
    return ServiceDataSerializableFactory.ID;
  }

  @JsonIgnore
  @Override
  public int getId() {
    return ServiceDataSerializableFactory.SERVICE_TYPE;
  }


}

