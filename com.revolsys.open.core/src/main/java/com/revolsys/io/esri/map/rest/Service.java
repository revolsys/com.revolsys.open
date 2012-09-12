package com.revolsys.io.esri.map.rest;

import org.springframework.util.StringUtils;

public class Service extends ArcGisResponse {

  private String serviceName;

  private String serviceType;

  public Service() {
  }

  protected Service(final Catalog catalog, final String serviceName,
    final String serviceType) {
    super(catalog, serviceName + "/" + serviceType);
    this.serviceName = serviceName;
    this.serviceType = serviceType;
  }

  public Service(final String serviceType) {
    this.serviceType = serviceType;
  }

  public String getServiceDescription() {
    final String serviceDescription = getValue("serviceDescription");
    return StringUtils.trimWhitespace(serviceDescription);
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getServiceType() {
    return serviceType;
  }

  public void setServiceName(final String serviceName) {
    this.serviceName = serviceName;
    setName(serviceName + "/MapServer");
  }

}
