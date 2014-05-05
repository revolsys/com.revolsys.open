package com.revolsys.gis.jts;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineString;

public class LineSegmentImpl extends com.revolsys.jts.geom.LineSegmentImpl {
  private static final long serialVersionUID = 3905321662159212931L;

  private GeometryFactory geometryFactory;

  public LineSegmentImpl() {
  }

  public LineSegmentImpl(final Coordinates coordinates1,
    final Coordinates coordinates2) {
    this(null, coordinates1, coordinates2);
  }

  public LineSegmentImpl(final GeometryFactory geometryFactory,
    final Coordinates point1, final Coordinates point2) {
    super(point1, point2);
    setGeometryFactory(geometryFactory);
  }

  public LineSegmentImpl(final GeometryFactory geometryFactory,
    final int axisCount, final double... coordinates) {
    super(axisCount, coordinates);
    setGeometryFactory(geometryFactory);
  }

  public LineSegmentImpl(final GeometryFactory geometryFactory,
    final LineSegment line) {
    super(line);
    setGeometryFactory(geometryFactory);
  }

  public LineSegmentImpl(final GeometryFactory geometryFactory,
    final LineString line) {
    super(line);
    setGeometryFactory(geometryFactory);
  }

  public LineSegmentImpl(final int axisCount, final double... coordinates) {
    this(null, axisCount, coordinates);
  }

  public LineSegmentImpl(final LineSegment line) {
    this(null, line);
    setGeometryFactory(null);
  }

  public LineSegmentImpl(final LineString line) {
    this(null, line);
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  private void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      this.geometryFactory = GeometryFactory.getFactory();
    } else {
      this.geometryFactory = geometryFactory;
    }
  }

}
