package com.revolsys.swing.map.overlay;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.jts.GeometryEditUtil;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.segment.Segment;
import com.revolsys.jts.geom.vertex.Vertex;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.util.CollectionUtil;

public class CloseLocation implements Comparable<CloseLocation> {

  private final AbstractDataObjectLayer layer;

  private final LayerDataObject object;

  private Vertex vertex;

  private Segment segment;

  private final Point point;

  public CloseLocation(final AbstractDataObjectLayer layer,
    final LayerDataObject object, final Segment segment, final Point point) {
    this.layer = layer;
    this.object = object;
    this.segment = segment;
    this.point = point;
  }

  public CloseLocation(final AbstractDataObjectLayer layer,
    final LayerDataObject object, final Vertex vertex) {
    this.layer = layer;
    this.object = object;
    this.vertex = vertex;
    this.point = vertex;
  }

  @Override
  public int compareTo(final CloseLocation location) {
    return 0;
  }

  public <G extends Geometry> G getGeometry() {
    if (vertex == null) {
      return segment.getGeometry();
    } else {
      return vertex.getGeometry();
    }
  }

  public GeometryFactory getGeometryFactory() {
    return layer.getGeometryFactory();
  }

  public Object getId() {
    Object id = null;
    if (this.object != null) {
      id = this.object.getIdValue();
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
    int[] index;
    if (vertex != null) {
      index = vertex.getVertexId();
    } else {
      index = this.segment.getSegmentId();
    }
    return CollectionUtil.toString(CollectionUtil.arrayToList(index));
  }

  public AbstractDataObjectLayer getLayer() {
    return layer;
  }

  public DataObjectMetaData getMetaData() {
    return layer.getMetaData();
  }

  public LayerDataObject getObject() {
    return this.object;
  }

  public Point getPoint() {
    return this.point;
  }

  public Segment getSegment() {
    return this.segment;
  }

  public int[] getSegmentId() {
    return this.segment.getSegmentId();
  }

  public String getType() {
    final Geometry geometry = getGeometry();
    if (geometry instanceof Point) {
      return "Point";
    } else if (segment != null) {
      return "Edge";
    } else {
      if (GeometryEditUtil.isFromPoint(geometry, getVertexIndex())
        || GeometryEditUtil.isToPoint(geometry, getVertexIndex())) {
        return "End-Vertex";
      } else {
        return "Vertex";
      }
    }
  }

  public String getTypePath() {
    final DataObjectMetaData metaData = getMetaData();
    return metaData.getPath();
  }

  public Vertex getVertex() {
    return vertex;
  }

  public int[] getVertexIndex() {
    if (vertex == null) {
      return null;
    } else {
      return this.vertex.getVertexId();
    }
  }

  @Override
  public String toString() {
    final StringBuffer string = new StringBuffer();
    string.append(getTypePath());
    string.append(", ");
    final DataObjectMetaData metaData = getMetaData();
    string.append(metaData.getIdAttributeName());
    string.append("=");
    final Object id = getId();
    string.append(id);
    string.append(", ");
    string.append(getType());
    int[] index = getVertexIndex();
    if (index != null) {
      string.append(", index=");
    } else {
      string.append(", index=");
      index = getSegmentId();
    }
    final String indexString = CollectionUtil.toString(CollectionUtil.arrayToList(index));
    string.append(indexString);
    return string.toString();
  }

}
