package com.revolsys.swing.map.overlay;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.model.geometry.util.IndexedLineSegment;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.util.CollectionUtil;
import com.vividsolutions.jts.geom.Geometry;

public class CloseLocation {

  private final DataObject object;

  private final int[] vertexIndex;

  private final IndexedLineSegment segment;

  private final DataObjectLayer layer;

  private final Geometry geometry;

  public CloseLocation(final DataObjectLayer layer, final DataObject object,
    final Geometry geometry, final int[] vertexIndex,
    final IndexedLineSegment segment) {
    this.object = object;
    this.layer = layer;
    this.geometry = geometry;
    this.vertexIndex = vertexIndex;
    this.segment = segment;
  }

  @SuppressWarnings("unchecked")
  public <G extends Geometry> G getGeometry() {
    return (G)geometry;
  }

  public GeometryFactory getGeometryFactory() {
    return layer.getGeometryFactory();
  }

  public DataObjectLayer getLayer() {
    return layer;
  }

  public DataObjectMetaData getMetaData() {
    return layer.getMetaData();
  }

  public DataObject getObject() {
    return object;
  }

  public IndexedLineSegment getSegment() {
    return segment;
  }

  public int[] getVertexIndex() {
    return vertexIndex;
  }

  @Override
  public String toString() {
    final StringBuffer string = new StringBuffer();
    final DataObjectMetaData metaData = getMetaData();
    string.append(metaData.getPath());
    string.append(", ");
    string.append(metaData.getIdAttributeName());
    string.append("=");
    Object id = null;
    if (object != null) {
      id = object.getIdValue();
    }
    if (id == null) {
      id = "NEW";
    }
    string.append(id);
    string.append(", ");
    int[] index = vertexIndex;
    if (index != null) {
      string.append("vertexIndex=");

    } else {
      string.append("segmentIndex=");
      index = segment.getIndex();
    }
    string.append(CollectionUtil.toString(CollectionUtil.arrayToList(index)));
    return string.toString();
  }
}
