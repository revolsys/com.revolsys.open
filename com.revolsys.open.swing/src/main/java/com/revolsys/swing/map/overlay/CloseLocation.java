package com.revolsys.swing.map.overlay;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.model.geometry.util.IndexedLineSegment;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.util.CollectionUtil;
import com.vividsolutions.jts.geom.Geometry;

public class CloseLocation {

  private final LayerDataObject object;

  private final int[] vertexIndex;

  private final IndexedLineSegment segment;

  private final DataObjectLayer layer;

  private final Geometry geometry;

  public CloseLocation(final DataObjectLayer layer,
    final LayerDataObject object, final Geometry geometry,
    final int[] vertexIndex, final IndexedLineSegment segment) {
    this.object = object;
    this.layer = layer;
    this.geometry = geometry;
    this.vertexIndex = vertexIndex;
    this.segment = segment;
  }

  public void appendIdAndIndex(final StringBuffer string) {
    final DataObjectMetaData metaData = getMetaData();
    string.append(metaData.getIdAttributeName());
    string.append("=");
    final Object id = getId();
    string.append(id);
    string.append(", ");
    int[] index = vertexIndex;
    if (index != null) {
      string.append("vertexIndex=");
    } else {
      string.append("segmentIndex=");
      index = segment.getIndex();
    }
    final String indexString = CollectionUtil.toString(CollectionUtil.arrayToList(index));
    string.append(indexString);
  }

  @SuppressWarnings("unchecked")
  public <G extends Geometry> G getGeometry() {
    return (G)geometry;
  }

  public GeometryFactory getGeometryFactory() {
    return layer.getGeometryFactory();
  }

  public Object getId() {
    Object id = null;
    if (object != null) {
      id = object.getIdValue();
    }
    if (id == null) {
      id = "NEW";
    }
    return id;
  }

  public String getIdAttributeName() {
    return getMetaData().getIdAttributeName();
  }

  public String getIndexString() {
    int[] index = vertexIndex;
    if (index != null) {
    } else {
      index = segment.getIndex();
    }
    return CollectionUtil.toString(CollectionUtil.arrayToList(index));
  }

  public DataObjectLayer getLayer() {
    return layer;
  }

  public DataObjectMetaData getMetaData() {
    return layer.getMetaData();
  }

  public LayerDataObject getObject() {
    return object;
  }

  public IndexedLineSegment getSegment() {
    return segment;
  }

  public String getTypePath() {
    final DataObjectMetaData metaData = getMetaData();
    return metaData.getPath();
  }

  public int[] getVertexIndex() {
    return vertexIndex;
  }

  @Override
  public String toString() {
    final StringBuffer string = new StringBuffer();
    string.append(getTypePath());
    string.append(", ");
    appendIdAndIndex(string);
    return string.toString();
  }

}
