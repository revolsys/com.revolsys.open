package com.revolsys.gis.format.shape.io.geometry;

import java.io.IOException;

import com.revolsys.gis.format.shape.io.ShapeConstants;
import com.revolsys.gis.io.EndianInput;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.PrecisionModel;

public class LineString2DConverter implements ShapefileGeometryConverter {
  private GeometryFactory geometryFactory;

  public LineString2DConverter() {
    this(null);
  }

  public LineString2DConverter(
    final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      this.geometryFactory = geometryFactory;
    } else {
      this.geometryFactory = new GeometryFactory(new PrecisionModel());
    }
  }

  public int getShapeType() {
    return ShapeConstants.POLYLINE_SHAPE;
  }

  public Geometry read(
    final EndianInput in,
    final long recordLength)
    throws IOException {
    // skip bounding box;
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int[] partIndex = new int[numParts + 1];
    partIndex[numParts] = numPoints;
    final Coordinate[] coordinates = new Coordinate[numPoints];
    for (int i = 0; i < partIndex.length - 1; i++) {
      partIndex[i] = in.readLEInt();
    }
    for (int i = 0; i < coordinates.length; i++) {
      final double x = in.readLEDouble();
      final double y = in.readLEDouble();
      coordinates[i] = new Coordinate(x, y);
    }
    if (numParts == 1) {
      return geometryFactory.createLineString(coordinates);
    } else {
      final LineString[] lines = new LineString[numParts];
      for (int i = 0; i < partIndex.length - 1; i++) {
        final int startIndex = partIndex[i];
        final int endIndex = partIndex[i + 1];
        final int numCoords = endIndex - startIndex;

        final Coordinate[] partCoords = new Coordinate[numCoords];
        System.arraycopy(coordinates, startIndex, partCoords, 0, numCoords);
        lines[i] = geometryFactory.createLineString(partCoords);
      }
      return geometryFactory.createMultiLineString(lines);
    }
  }

  public void write(
    final EndianOutput out,
    final Geometry geometry)
    throws IOException {
    if (geometry instanceof LineString) {
      writePolyLineHeader(out, geometry);
      final LineString line = (LineString)geometry;
      out.writeLEInt(0);
      ShapefileGeometryUtil.write2DCoordinates(out, line);
    } else if (geometry instanceof MultiLineString) {
      final MultiLineString multiLine = (MultiLineString)geometry;
      writePolyLineHeader(out, multiLine);
      ShapefileGeometryUtil.writePartIndexes(out, multiLine);
      ShapefileGeometryUtil.write2DCoordinates(out, multiLine);
    } else {
      throw new IllegalArgumentException("Expecting " + LineString.class
        + " geometry got " + geometry.getClass());
    }
  }

  private void writePolyLineHeader(
    final EndianOutput out,
    final Geometry geometry)
    throws IOException {
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
