package com.revolsys.jts.geom.segment;

import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDoubleGF;

public class LineSegmentDoubleGF extends LineSegmentDouble {
  private static final long serialVersionUID = 3905321662159212931L;

  private GeometryFactory geometryFactory;

  public LineSegmentDoubleGF() {
  }

  public LineSegmentDoubleGF(final GeometryFactory geometryFactory,
    final int axisCount, final double... coordinates) {
    super(geometryFactory, axisCount, coordinates);
    setGeometryFactory(geometryFactory);
  }

  public LineSegmentDoubleGF(final GeometryFactory geometryFactory,
    final LineString line) {
    super(geometryFactory, line);
    setGeometryFactory(geometryFactory);
  }

  public LineSegmentDoubleGF(final GeometryFactory geometryFactory,
    final Point point1, final Point point2) {
    super(geometryFactory, point1, point2);
    setGeometryFactory(geometryFactory);
  }

  public LineSegmentDoubleGF(final int axisCount, final double... coordinates) {
    this(null, axisCount, coordinates);
  }

  public LineSegmentDoubleGF(final LineSegment line) {
    this(null, line);
    setGeometryFactory(null);
  }

  public LineSegmentDoubleGF(final LineString line) {
    this(null, line);
  }

  public LineSegmentDoubleGF(final Point coordinates1, final Point coordinates2) {
    this(null, coordinates1, coordinates2);
  }

  @Override
  protected LineSegment createLineSegment(
    final GeometryFactory geometryFactory, final int axisCount,
    final double... coordinates) {
    return new LineSegmentDoubleGF(geometryFactory, axisCount, coordinates);
  }

  @Override
  protected Point createPoint(final GeometryFactory geometryFactory,
    final double... coordinates) {
    return new PointDoubleGF(geometryFactory, coordinates);
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  private void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      this.geometryFactory = GeometryFactory.floating3();
    } else {
      this.geometryFactory = geometryFactory;
    }
  }

}
