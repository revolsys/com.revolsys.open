package com.revolsys.record.io.format.esri.map.rest;

import org.springframework.util.StringUtils;

import com.revolsys.io.PathName;

public class Service extends ArcGisResponse implements CatalogElement {
  private String serviceType;

  public Service() {
  }

  protected Service(final Catalog catalog, final String serviceName, final String serviceType) {
    setService(catalog, serviceName, serviceType);
  }

  public Service(final String serviceType) {
    this.serviceType = serviceType;
  }

  @Override
  public String getIconName() {
    return "file";
  }

  public String getServiceDescription() {
    final String serviceDescription = getValue("serviceDescription");
    return StringUtils.trimWhitespace(serviceDescription);
  }

  public String getServiceType() {
    return this.serviceType;
  }

  public void setService(final Catalog catalog, final String servicePath,
    final String serviceType) {
    this.serviceType = serviceType;
    setName(PathName.newPathName(servicePath).getName());
    final String path = servicePath + "/" + serviceType;
    init(catalog, path);
  }
}
