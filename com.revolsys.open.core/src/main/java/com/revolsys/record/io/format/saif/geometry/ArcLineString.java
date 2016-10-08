package com.revolsys.record.io.format.saif.geometry;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.LineStringDoubleBuilder;
import com.revolsys.geometry.model.impl.LineStringDoubleGf;
import com.revolsys.record.io.format.saif.SaifConstants;

public class ArcLineString extends LineStringDoubleGf {
  private String qualifier;

  public ArcLineString(final GeometryFactory geometryFactory, final LineStringDoubleBuilder line) {
    super(geometryFactory, line);
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
