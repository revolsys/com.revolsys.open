package com.revolsys.record.io.format.esri.rest.map;

import com.revolsys.io.PathName;
import com.revolsys.record.io.format.esri.rest.ArcGisResponse;
import com.revolsys.record.io.format.esri.rest.ArcGisRestCatalog;
import com.revolsys.record.io.format.esri.rest.CatalogElement;

public class LayerDescription extends ArcGisResponse implements CatalogElement {
  private LayerGroupDescription parent;

  private ArcGisRestMapServer mapServer;

  private Integer id;

  private PathName pathName;

  public LayerDescription() {
  }

  public LayerDescription(final ArcGisRestMapServer mapServer, final Integer id,
    final String name) {
    this.mapServer = mapServer;
    this.id = id;
    setName(name);
    final ArcGisRestCatalog arcGisRestCatalog = mapServer.getCatalog();
    final String mapServerPath = mapServer.getPath();
    init(arcGisRestCatalog, mapServerPath + "/" + id);
    this.pathName = PathName.newPathName(mapServerPath).getParent().newChild(name);
  }

  public Boolean getDefaultVisibility() {
    return getValue("defaultVisibility");
  }

  @Override
  public String getIconName() {
    return "table";
  }

  public Integer getId() {
    return this.id;
  }

  public ArcGisRestMapServer getMapServer() {
    return this.mapServer;
  }

  public Double getMaxScale() {
    return getDoubleValue("maxScale");
  }

  public Double getMinScale() {
    return getDoubleValue("minScale");
  }

  @Override
  public LayerGroupDescription getParent() {
    return this.parent;
  }

  public PathName getPathName() {
    return this.pathName;
  }

  public void setParent(final LayerGroupDescription parent) {
    this.parent = parent;
    final String name = getName();
    this.pathName = parent.getPathName().newChild(name);
  }

  @Override
  public String toString() {
    return getName();
  }
}
