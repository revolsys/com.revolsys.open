package com.revolsys.swing.map.overlay;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public class CloseLocation implements Comparable<CloseLocation> {

  private final AbstractRecordLayer layer;

  private final Point viewportPoint;

  private final Point sourcePoint;

  private final LayerRecord record;

  private Segment segment;

  private Vertex vertex;

  public CloseLocation(final AbstractRecordLayer layer, final LayerRecord record,
    final Segment segment, final Point viewportPoint, final Point sourcePoint) {
    this.layer = layer;
    this.record = record;
    this.segment = segment;
    this.viewportPoint = viewportPoint;
    this.sourcePoint = sourcePoint;
  }

  public CloseLocation(final AbstractRecordLayer layer, final LayerRecord record,
    final Vertex vertex) {
    this.layer = layer;
    this.record = record;
    this.vertex = vertex;
    this.viewportPoint = vertex;
    this.sourcePoint = vertex;
  }

  @Override
  public int compareTo(final CloseLocation location) {
    return 0;
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof CloseLocation) {
      final CloseLocation location = (CloseLocation)other;
      final LayerRecord record1 = getRecord();
      final LayerRecord record2 = location.getRecord();
      if (record1 == null) {
        if (record2 != null) {
          return false;
        }
      } else if (record2 == null) {
        return false;
      } else if (!record2.isSame(record1)) {
        return false;
      }
      if (!DataType.equal(getVertex(), location.getVertex())) {
        return false;
      } else if (!DataType.equal(getSegment(), location.getSegment())) {
        return false;
      } else if (location.getViewportPoint().equals(getViewportPoint())) {
        return true;
      } else {
        return false;
      }
    }
    return false;
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
    if (this.record != null) {
      id = this.record.getIdentifier();
    }
    if (id == null) {
      id = Identifier.newIdentifier("NEW");
    }
    return id;
  }

  public String getIdFieldName() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getIdFieldName();
    }
  }

  public String getIndexString() {
    int[] index;
    if (this.vertex != null) {
      index = this.vertex.getVertexId();
    } else {
      index = this.segment.getSegmentId();
    }
    return Strings.toString(Lists.arrayToList(index));
  }

  public AbstractRecordLayer getLayer() {
    return this.layer;
  }

  public String getLayerPath() {
    if (this.layer == null) {
      return null;
    } else {
      return this.layer.getPath();
    }
  }

  public LayerRecord getRecord() {
    return this.record;
  }

  public RecordDefinition getRecordDefinition() {
    if (this.layer == null) {
      return null;
    } else {
      return this.layer.getRecordDefinition();
    }
  }

  public Segment getSegment() {
    return this.segment;
  }

  public int[] getSegmentId() {
    return this.segment.getSegmentId();
  }

  public int[] getSegmentIdNext() {
    final int[] segmentId = getSegmentId();
    final int[] newId = segmentId.clone();
    newId[newId.length - 1] = newId[newId.length - 1] + 1;
    return newId;
  }

  public Point getSourcePoint() {
    return this.sourcePoint;
  }

  public String getType() {
    final Geometry geometry = getGeometry();
    if (geometry instanceof Point) {
      return "Point";
    } else if (this.segment != null) {
      return "Edge";
    } else {
      final Vertex vertex = geometry.getVertex(getVertexId());
      if (vertex.isFrom() || vertex.isTo()) {
        return "End-Vertex";
      } else {
        return "Vertex";
      }
    }
  }

  public Vertex getVertex() {
    return this.vertex;
  }

  public int[] getVertexId() {
    if (this.vertex == null) {
      return null;
    } else {
      return this.vertex.getVertexId();
    }
  }

  public Point getViewportPoint() {
    return this.viewportPoint;
  }

  @Override
  public int hashCode() {
    return this.viewportPoint.hashCode();
  }

  public boolean isFromVertex() {
    if (this.vertex == null) {
      return false;
    } else {
      return this.vertex.isFrom();
    }
  }

  public boolean isSegment() {
    return this.segment != null;
  }

  public boolean isVertex() {
    return this.vertex != null;
  }

  @Override
  public String toString() {
    final StringBuilder string = new StringBuilder();
    final String layerPath = getLayerPath();
    if (Property.hasValue(layerPath)) {
      string.append(layerPath);
    }
    if (getRecordDefinition() != null) {
      string.append(", ");
      final RecordDefinition recordDefinition = getRecordDefinition();
      string.append(recordDefinition.getIdFieldName());
      string.append("=");
      final Object id = getId();
      string.append(id);
      string.append(", ");
    }
    string.append(getType());
    int[] index = getVertexId();
    if (index != null) {
      string.append(", index=");
    } else {
      string.append(", index=");
      index = getSegmentId();
    }
    final String indexString = Strings.toString(Lists.arrayToList(index));
    string.append(indexString);
    return string.toString();
  }

}
