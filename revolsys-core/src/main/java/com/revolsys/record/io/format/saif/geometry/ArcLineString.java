package com.revolsys.record.io.format.saif.geometry;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.LineStringDoubleGeometryFactory;
import com.revolsys.record.io.format.saif.SaifConstants;

public class ArcLineString extends LineStringDoubleGeometryFactory {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private String qualifier;

  public ArcLineString(final GeometryFactory geometryFactory, final int axisCount,
    final int vertexCount, final double... coordinates) {
    super(geometryFactory, axisCount, vertexCount, coordinates);
  }

  public String getOsnGeometryType() {
    return SaifConstants.ARC;
  }

  public String getQualifier() {
    return this.qualifier;
  }

  public void setQualifier(final String qualifier) {
    this.qualifier = qualifier;
  }
}
