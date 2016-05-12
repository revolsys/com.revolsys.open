package com.revolsys.record.io.format.esri.rest;

import org.springframework.util.StringUtils;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.io.PathName;

public class ArcGisRestService extends ArcGisResponse
  implements CatalogElement, GeometryFactoryProxy {
  private String serviceType;

  private String serviceDescription;

  private String supportedQueryFormats;

  private String capabilities;

  private String copyrightText;

  private String description;

  private MapEx documentInfo;

  private String units;

  private BoundingBox fullExtent = BoundingBox.EMPTY;

  private BoundingBox initialExtent = BoundingBox.EMPTY;

  private GeometryFactory spatialReference = GeometryFactory.DEFAULT;

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

  public String getCapabilities() {
    return this.capabilities;
  }

  public String getCopyrightText() {
    return this.copyrightText;
  }

  public String getDescription() {
    return this.description;
  }

  public MapEx getDocumentInfo() {
    return this.documentInfo;
  }

  public BoundingBox getFullExtent() {
    return this.fullExtent;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.spatialReference;
  }

  @Override
  public String getIconName() {
    return "file";
  }

  public BoundingBox getInitialExtent() {
    return this.initialExtent;
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

  public GeometryFactory getSpatialReference() {
    return this.spatialReference;
  }

  public String getSupportedQueryFormats() {
    return this.supportedQueryFormats;
  }

  public String getUnits() {
    return this.units;
  }

  @Override
  protected void initialize(final MapEx properties) {
    super.initialize(properties);
    this.spatialReference = newGeometryFactory(properties, "spatialReference");
    this.initialExtent = newBoundingBox(properties, "initialExtent");
    this.fullExtent = newBoundingBox(properties, "fullExtent");
  }

  public void setCapabilities(final String capabilities) {
    this.capabilities = capabilities;
  }

  public void setCopyrightText(final String copyrightText) {
    this.copyrightText = copyrightText;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public void setServiceDescription(final String serviceDescription) {
    this.serviceDescription = StringUtils.trimWhitespace(serviceDescription);
  }

  public void setSupportedQueryFormats(final String supportedQueryFormats) {
    this.supportedQueryFormats = supportedQueryFormats;
  }

  public void setUnits(final String units) {
    this.units = units;
  }
}
