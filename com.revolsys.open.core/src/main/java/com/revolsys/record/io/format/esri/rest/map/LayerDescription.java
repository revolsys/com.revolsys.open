package com.revolsys.record.io.format.esri.rest.map;

import com.revolsys.io.PathName;
import com.revolsys.record.io.format.esri.rest.ArcGisResponse;
import com.revolsys.record.io.format.esri.rest.ArcGisRestCatalog;
import com.revolsys.record.io.format.esri.rest.CatalogElement;

public class LayerDescription extends ArcGisResponse implements CatalogElement {
  private LayerGroupDescription parent;

  private ArcGisRestAbstractLayerService service;

  private Integer id;

  private PathName pathName;

  private int maxRecordCount = 10000;

  private long maxScale = 0;

  private long minScale = Long.MAX_VALUE;

  private boolean defaultVisibility = true;

  public LayerDescription() {
  }

  public LayerDescription(final ArcGisRestAbstractLayerService service, final Integer id,
    final String name) {
    this.service = service;
    this.id = id;
    setName(name);
    final ArcGisRestCatalog arcGisRestCatalog = service.getCatalog();
    final String mapServerPath = service.getPath();
    init(arcGisRestCatalog, mapServerPath + "/" + id);
    this.pathName = PathName.newPathName(mapServerPath).getParent().newChild(name);
  }

  public boolean getDefaultVisibility() {
    return this.defaultVisibility;
  }

  @Override
  public String getIconName() {
    return "table";
  }

  public Integer getId() {
    return this.id;
  }

  public int getMaxRecordCount() {
    return this.maxRecordCount;
  }

  public long getMaxScale() {
    return this.maxScale;
  }

  public long getMinScale() {
    return this.minScale;
  }

  @Override
  public LayerGroupDescription getParent() {
    return this.parent;
  }

  public PathName getPathName() {
    return this.pathName;
  }

  public ArcGisRestAbstractLayerService getService() {
    return this.service;
  }

  public void setDefaultVisibility(final boolean defaultVisibility) {
    this.defaultVisibility = defaultVisibility;
  }

  public void setMaxRecordCount(final int maxRecordCount) {
    this.maxRecordCount = maxRecordCount;
  }

  public void setMaxScale(final long maxScale) {
    if (maxScale < 0) {
      this.maxScale = 0;
    } else {
      this.maxScale = maxScale;
    }
  }

  public void setMinScale(final long minScale) {
    if (minScale > 0) {
      this.minScale = minScale;
    } else {
      this.minScale = Long.MAX_VALUE;
    }
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
