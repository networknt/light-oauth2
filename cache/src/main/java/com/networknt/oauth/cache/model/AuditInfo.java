package com.networknt.oauth.cache.model;



public class AuditInfo  {

  private Long logId = null;

  private Oauth2Service serviceId = null;

  private String endpoint = null;

  private String requestHeader = null;

  private String requestBody = null;

  private Integer responseCode = null;

  private String responseHeader = null;

  private String responseBody = null;

  public Long getLogId() {
    return logId;
  }


  public Oauth2Service getServiceId() {
    return serviceId;
  }

  public void setServiceId(Oauth2Service serviceId) {
    this.serviceId = serviceId;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getRequestHeader() {
    return requestHeader;
  }

  public void setRequestHeader(String requestHeader) {
    this.requestHeader = requestHeader;
  }

  public String getRequestBody() {
    return requestBody;
  }

  public void setRequestBody(String requestBody) {
    this.requestBody = requestBody;
  }

  public Integer getResponseCode() {
    return responseCode;
  }

  public void setResponseCode(Integer responseCode) {
    this.responseCode = responseCode;
  }

  public String getResponseHeader() {
    return responseHeader;
  }

  public void setResponseHeader(String responseHeader) {
    this.responseHeader = responseHeader;
  }

  public String getResponseBody() {
    return responseBody;
  }

  public void setResponseBody(String responseBody) {
    this.responseBody = responseBody;
  }
}

