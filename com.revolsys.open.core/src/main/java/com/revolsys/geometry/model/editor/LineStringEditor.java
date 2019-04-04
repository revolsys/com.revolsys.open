package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.LineStringDoubleBuilder;

public class LineStringEditor extends LineStringDoubleBuilder {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public LineStringEditor(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  public LineStringEditor(final GeometryFactory geometryFactory, final int vertexCapacity) {
    super(geometryFactory, vertexCapacity);
  }

  public LineStringEditor(final GeometryFactory geometryFactory, final int axisCount,
    final double... coordinates) {
    super(geometryFactory, axisCount, coordinates);
  }

  public LineStringEditor(final int axisCount, final int vertexCount, final double... coordinates) {
    super(axisCount, vertexCount, coordinates);
  }

}
