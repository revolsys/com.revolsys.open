package com.revolsys.io.shp.geometry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.EndianInput;
import com.revolsys.io.shp.ShapefileConstants;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

public class LineString2DConverter implements ShapefileGeometryConverter {
  private GeometryFactory geometryFactory;

  public LineString2DConverter() {
    this(null);
  }

  public LineString2DConverter(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      this.geometryFactory = geometryFactory;
    } else {
      this.geometryFactory = new GeometryFactory();
    }
  }

  public int getShapeType() {
    return ShapefileConstants.POLYLINE_SHAPE;
  }

  public Geometry read(final EndianInput in, final long recordLength)
    throws IOException {
    // skip bounding box;
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    if (numParts == 1) {
      in.readLEInt();
      final CoordinatesList points = new DoubleCoordinatesList(numPoints, 2);
      for (int i = 0; i < numPoints; i++) {
        final double x = in.readLEDouble();
        points.setX(i, x);
        final double y = in.readLEDouble();
        points.setY(i, y);
      }

      return geometryFactory.createLineString(points);
    } else {
      final int[] partIndex = new int[numParts + 1];
      partIndex[numParts] = numPoints;
      for (int i = 0; i < partIndex.length - 1; i++) {
        partIndex[i] = in.readLEInt();

      }
      final List<CoordinatesList> pointsList = new ArrayList<CoordinatesList>();
      for (int i = 0; i < partIndex.length - 1; i++) {
        final int startIndex = partIndex[i];
        final int endIndex = partIndex[i + 1];
        final int numCoords = endIndex - startIndex;
        final CoordinatesList points = new DoubleCoordinatesList(2, numCoords);

        for (final int j = 0; j < numCoords; i++) {
          final double x = in.readLEDouble();
          points.setX(j, x);
          final double y = in.readLEDouble();
          points.setY(j, y);
        }
      }
      return geometryFactory.createMultiLineString(pointsList);
    }
  }

  public void write(final EndianOutput out, final Geometry geometry)
    throws IOException {
    if (geometry instanceof LineString) {
      writePolyLineHeader(out, geometry);
      final LineString line = (LineString)geometry;
      out.writeLEInt(0);
      ShapefileGeometryUtil.writeXYCoordinates(out, line);
    } else if (geometry instanceof MultiLineString) {
      final MultiLineString multiLine = (MultiLineString)geometry;
      writePolyLineHeader(out, multiLine);
      ShapefileGeometryUtil.writePolylinePartIndexes(out, multiLine);
      ShapefileGeometryUtil.writeXYCoordinates(out, multiLine);
    } else {
      throw new IllegalArgumentException("Expecting " + LineString.class
        + " geometry got " + geometry.getClass());
    }
  }

  private void writePolyLineHeader(final EndianOutput out,
    final Geometry geometry) throws IOException {
    final int numCoordinates = geometry.getNumPoints();
    final int numGeometries = geometry.getNumGeometries();
    final Envelope envelope = geometry.getEnvelopeInternal();
    final int recordLength = ((3 + numGeometries) * MathUtil.BYTES_IN_INT + (4 + 2 * numCoordinates)
      * MathUtil.BYTES_IN_DOUBLE) / 2;
    out.writeInt(recordLength);
    out.writeLEInt(getShapeType());
    ShapefileGeometryUtil.writeEnvelope(out, envelope);
    out.writeLEInt(numGeometries);
    out.writeLEInt(numCoordinates);
  }

}
