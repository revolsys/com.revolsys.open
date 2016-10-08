package com.revolsys.record.io.format.saif.geometry;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.LineStringDoubleBuilder;
import com.revolsys.record.io.format.saif.SaifConstants;

public class OrientedArcLineString extends ArcLineString {
  private String traversalDirection;

  public OrientedArcLineString(final GeometryFactory geometryFactory,
    final LineStringDoubleBuilder line) {
    super(geometryFactory, line);
  }

  @Override
  public String getOsnGeometryType() {
    return SaifConstants.ORIENTED_ARC;
  }

  public String getTraversalDirection() {
    return this.traversalDirection;
  }

  public void setTraversalDirection(final String traversalDirection) {
    this.traversalDirection = traversalDirection;
  }
}
