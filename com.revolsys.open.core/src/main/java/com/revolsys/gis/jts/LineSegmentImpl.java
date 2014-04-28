package com.revolsys.gis.jts;

import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineString;

public class LineSegmentImpl extends com.revolsys.jts.geom.LineSegmentImpl {
  private static final long serialVersionUID = 3905321662159212931L;

  private static final GeometryFactory FACTORY = GeometryFactory.getFactory();

  private final double[] coordinates;

  private GeometryFactory geometryFactory;

  public LineSegmentImpl() {
    this.coordinates = null;
  }

  public LineSegmentImpl(final Coordinates coordinates1,
    final Coordinates coordinates2) {
    this(FACTORY, coordinates1, coordinates2);
  }

  public LineSegmentImpl(final GeometryFactory geometryFactory,
    final Coordinates coordinates1, final Coordinates coordinates2) {
    this.geometryFactory = geometryFactory;
    final int axisCount = Math.max(coordinates1.getAxisCount(),
      coordinates2.getAxisCount());
    coordinates = new double[axisCount * 2];
    for (int i = 0; i < axisCount; i++) {
      setValue(0, i, coordinates1.getValue(i));
      setValue(1, i, coordinates2.getValue(i));
    }
  }

  public LineSegmentImpl(final GeometryFactory geometryFactory,
    final int axisCount, final double... coordinates) {
    this.geometryFactory = geometryFactory;
    if (coordinates == null || coordinates.length == 0 || axisCount < 1) {
      this.coordinates = null;
    } else if (coordinates.length % axisCount == 0) {
      this.coordinates = new double[axisCount * 2];
      int i = 0;
      final int axisCount2 = coordinates.length / 2;
      for (int vertexIndex = 0; vertexIndex < 2; vertexIndex++) {
        for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
          double value;
          if (axisIndex < axisCount2) {
            value = coordinates[vertexIndex * axisCount2 + axisIndex];
          } else {
            value = Double.NaN;
          }
          coordinates[i++] = value;
        }
      }
    } else {
      throw new IllegalArgumentException("Expecting a multiple of " + axisCount
        + " not " + coordinates.length);
    }
  }

  public LineSegmentImpl(final GeometryFactory geometryFactory,
    final LineSegment line) {
    this(geometryFactory, line.get(0), line.get(1));
  }

  public LineSegmentImpl(final int axisCount, final double... coordinates) {
    this(FACTORY, 2, coordinates);
  }

  public LineSegmentImpl(final LineSegment line) {
    this(line.getGeometryFactory(), line.get(0), line.get(1));
  }

  public LineSegmentImpl(final LineString line) {
    this(GeometryFactory.getFactory(line), CoordinatesListUtil.get(line, 0),
      CoordinatesListUtil.get(line, line.getVertexCount() - 1));
  }

  public LineSegment extend(final double startDistance, final double endDistance) {
    final double angle = angle();
    final Coordinates c1 = CoordinatesUtil.offset(get(0), angle, -startDistance);
    final Coordinates c2 = CoordinatesUtil.offset(get(1), angle, endDistance);
    return new LineSegmentImpl(c1, c2);

  }

  @Override
  public int getAxisCount() {
    return (byte)(coordinates.length / 2);
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  @Override
  public double getValue(final int index, final int axisIndex) {
    final int axisCount = getAxisCount();
    if (axisIndex >= 0 && axisIndex < axisCount) {
      if (index >= 0 && index < 2) {
        final int valueIndex = index * axisCount + axisIndex;
        final double value = coordinates[valueIndex];
        return value;
      }
    }
    return Double.NaN;
  }

  @Override
  public boolean isEmpty() {
    return get(0) == null || get(1) == null;
  }

  @Override
  public void setCoordinates(final Coordinates s0, final Coordinates s1) {
    setPoint(0, s0);
    setPoint(1, s1);
  }

  @Override
  public void setValue(final int index, final int axisIndex, final double value) {
    final int axisCount = getAxisCount();
    if (axisIndex >= 0 && axisIndex < axisCount) {
      if (index >= 0 && index < 2) {
        final int valueIndex = index * axisCount + axisIndex;
        coordinates[valueIndex] = value;
      }
    }
  }

}
