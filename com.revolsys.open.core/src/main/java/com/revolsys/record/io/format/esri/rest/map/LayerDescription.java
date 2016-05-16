package com.revolsys.record.io.format.esri.rest.map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.io.format.esri.rest.ArcGisResponse;
import com.revolsys.record.io.format.esri.rest.CatalogElement;

public class LayerDescription extends ArcGisResponse implements CatalogElement {
  private int id = -1;

  private int maxRecordCount = 1000;

  private long maxScale = 0;

  private long minScale = Long.MAX_VALUE;

  private boolean defaultVisibility = true;

  private int parentLayerId = -1;

  private ArcGisRestAbstractLayerService service;

  public LayerDescription() {
  }

  public LayerDescription(final ArcGisRestAbstractLayerService service) {
    super(service);
    this.service = service;
  }

  public LayerDescription(final ArcGisRestAbstractLayerService service, final MapEx properties) {
    this(service);
    initialize(properties);
  }

  public boolean getDefaultVisibility() {
    return this.defaultVisibility;
  }

  @Override
  public String getIconName() {
    return "file";
  }

  public int getId() {
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

  public int getParentLayerId() {
    return this.parentLayerId;
  }

  public ArcGisRestAbstractLayerService getService() {
    return this.service;
  }

  @Override
  protected void initialize(final MapEx properties) {
    super.initialize(properties);
    setInitialized(true);
  }

  public void setDefaultVisibility(final boolean defaultVisibility) {
    this.defaultVisibility = defaultVisibility;
  }

  public void setId(final int id) {
    this.id = id;
    setResourceUrl(getService().getResourceUrl(Integer.toString(this.id)));
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

  public void setParentLayerId(final int parentLayerId) {
    this.parentLayerId = parentLayerId;
  }

  @Override
  public String toString() {
    return getName();
  }
}
