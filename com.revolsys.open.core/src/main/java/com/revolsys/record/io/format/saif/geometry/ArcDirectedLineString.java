package com.revolsys.record.io.format.saif.geometry;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.LineStringDoubleBuilder;
import com.revolsys.record.io.format.saif.SaifConstants;

public class ArcDirectedLineString extends ArcLineString {
  private String flowDirection;

  public ArcDirectedLineString(final GeometryFactory geometryFactory,
    final LineStringDoubleBuilder line) {
    super(geometryFactory, line);
  }

  @Override
  public String getOsnGeometryType() {
    return SaifConstants.ARC_DIRECTED;
  }

  public String getFlowDirection() {
    return this.flowDirection;
  }

  public void setFlowDirection(final String flowDirection) {
    this.flowDirection = flowDirection;
  }
}
