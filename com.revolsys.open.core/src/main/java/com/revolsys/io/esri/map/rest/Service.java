package com.revolsys.io.esri.map.rest;

import org.springframework.util.StringUtils;

public class Service extends ArcGisResponse {

  private String serviceName;

  private String serviceType;

  protected Service(Catalog catalog, String serviceName, String serviceType) {
    super(catalog, serviceName + "/" + serviceType);
    this.serviceName = serviceName;
    this.serviceType = serviceType;
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getServiceType() {
    return serviceType;
  }

  public String getServiceDescription() {
    String serviceDescription = getValue("serviceDescription");
    return StringUtils.trimWhitespace(serviceDescription);
  }

}
