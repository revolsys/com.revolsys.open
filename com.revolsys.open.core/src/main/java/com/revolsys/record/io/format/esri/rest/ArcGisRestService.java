package com.revolsys.record.io.format.esri.rest;

import org.springframework.util.StringUtils;

import com.revolsys.io.PathName;

public class ArcGisRestService extends ArcGisResponse implements CatalogElement {
  private String serviceType;

  private String serviceDescription;

  public ArcGisRestService() {
  }

  public ArcGisRestService(final ArcGisRestCatalog arcGisRestCatalog, final String serviceName,
    final String serviceType) {
    this.serviceType = serviceType;
    setName(PathName.newPathName(serviceName).getName());
    final String path = serviceName + "/" + serviceType;
    init(arcGisRestCatalog, path);
  }

  public ArcGisRestService(final String serviceType) {
    this.serviceType = serviceType;
  }

  @Override
  public String getIconName() {
    return "file";
  }

  @Override
  public CatalogElement getParent() {
    return getCatalog();
  }

  public String getServiceDescription() {
    return this.serviceDescription;
  }

  public String getServiceType() {
    return this.serviceType;
  }

  public void setServiceDescription(final String serviceDescription) {
    this.serviceDescription = StringUtils.trimWhitespace(serviceDescription);
  }
}
