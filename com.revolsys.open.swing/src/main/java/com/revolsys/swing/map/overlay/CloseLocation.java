package com.revolsys.swing.map.overlay;

import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.identifier.SingleIdentifier;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.jts.GeometryEditUtil;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.segment.Segment;
import com.revolsys.jts.geom.vertex.Vertex;
import com.revolsys.swing.map.layer.record.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.util.CollectionUtil;

public class CloseLocation implements Comparable<CloseLocation> {

  private final AbstractDataObjectLayer layer;

  private final LayerRecord object;

  private Vertex vertex;

  private Segment segment;

  private final Point point;

  public CloseLocation(final AbstractDataObjectLayer layer,
    final LayerRecord object, final Segment segment, final Point point) {
    this.layer = layer;
    this.object = object;
    this.segment = segment;
    this.point = point;
  }

  public CloseLocation(final AbstractDataObjectLayer layer,
    final LayerRecord object, final Vertex vertex) {
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
    if (this.vertex == null) {
      return this.segment.getGeometry();
    } else {
      return this.vertex.getGeometry();
    }
  }

  public GeometryFactory getGeometryFactory() {
    return this.layer.getGeometryFactory();
  }

  public Object getId() {
    Identifier id = null;
    if (this.object != null) {
      id = this.object.getIdentifier();
    }
    if (id == null) {
      id = SingleIdentifier.create("NEW");
    }
    return id;
  }

  public String getIdAttributeName() {
    return getMetaData().getIdAttributeName();
  }

  public String getIndexString() {
    int[] index;
    if (this.vertex != null) {
      index = this.vertex.getVertexId();
    } else {
      index = this.segment.getSegmentId();
    }
    return CollectionUtil.toString(CollectionUtil.arrayToList(index));
  }

  public AbstractDataObjectLayer getLayer() {
    return this.layer;
  }

  public RecordDefinition getMetaData() {
    return this.layer.getMetaData();
  }

  public LayerRecord getObject() {
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
    } else if (this.segment != null) {
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
    final RecordDefinition metaData = getMetaData();
    return metaData.getPath();
  }

  public Vertex getVertex() {
    return this.vertex;
  }

  public int[] getVertexIndex() {
    if (this.vertex == null) {
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
    final RecordDefinition metaData = getMetaData();
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
