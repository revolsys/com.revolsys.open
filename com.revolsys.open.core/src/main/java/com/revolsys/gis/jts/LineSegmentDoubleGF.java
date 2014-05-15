package com.revolsys.gis.jts;

import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineSegmentDouble;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;

public class LineSegmentDoubleGF extends LineSegmentDouble {
  private static final long serialVersionUID = 3905321662159212931L;

  private GeometryFactory geometryFactory;

  public LineSegmentDoubleGF() {
  }

  public LineSegmentDoubleGF(final GeometryFactory geometryFactory,
    final int axisCount, final double... coordinates) {
    super(axisCount, coordinates);
    setGeometryFactory(geometryFactory);
  }

  public LineSegmentDoubleGF(final GeometryFactory geometryFactory,
    final LineSegment line) {
    super(line);
    setGeometryFactory(geometryFactory);
  }

  public LineSegmentDoubleGF(final GeometryFactory geometryFactory,
    final LineString line) {
    super(line);
    setGeometryFactory(geometryFactory);
  }

  public LineSegmentDoubleGF(final GeometryFactory geometryFactory,
    final Point point1, final Point point2) {
    super(point1, point2);
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
