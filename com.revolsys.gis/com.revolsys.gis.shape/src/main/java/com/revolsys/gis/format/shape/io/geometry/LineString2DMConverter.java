package com.revolsys.gis.format.shape.io.geometry;

import java.io.IOException;

import com.revolsys.gis.format.shape.io.ShapefileConstants;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.io.EndianInput;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;

public class LineString2DMConverter implements ShapefileGeometryConverter {
  private GeometryFactory geometryFactory;

  public LineString2DMConverter() {
    this(null);
  }

  public LineString2DMConverter(
    final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      this.geometryFactory = geometryFactory;
    } else {
      this.geometryFactory = new GeometryFactory(new PrecisionModel());
    }
  }

  public int getShapeType() {
    return ShapefileConstants.POLYLINE_SHAPE;
  }

  /*
   * (non-Javadoc)
   * @see
   * com.revolsys.gis.format.shape.io.geometry.ShapefileGeometryConverter#read
   * (int, com.revolsys.gis.format.core.io.LittleEndianRandomAccessFile)
   */
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

  /*
   * (non-Javadoc)
   * @see
   * com.revolsys.gis.format.shape.io.geometry.ShapefileGeometryConverter#write
   * (com.revolsys.gis.format.core.io.LittleEndianRandomAccessFile,
   * com.vividsolutions.jts.geom.Geometry)
   */
  public void write(
    final EndianOutput out,
    final Geometry geometry)
    throws IOException {
    if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      final Envelope envelope = line.getEnvelopeInternal();
      final CoordinateSequence coordinates = line.getCoordinateSequence();
      final int recordLength = (4 * MathUtil.BYTES_IN_INT + (4 + 2 * coordinates.size())
        * MathUtil.BYTES_IN_DOUBLE) / 2;
      out.writeInt(recordLength);
      out.writeLEInt(getShapeType());
      ShapefileGeometryUtil.writeEnvelope(out, envelope);
      out.writeLEInt(1);
      out.writeLEInt(coordinates.size());
      out.writeLEInt(0);

      for (int i = 0; i < coordinates.size(); i++) {
        out.writeLEDouble(coordinates.getOrdinate(i, 0));
        out.writeLEDouble(coordinates.getOrdinate(i, 1));
      }
    } else {
      throw new IllegalArgumentException("Expecting " + LineString.class
        + " geometry got " + geometry.getClass());
    }
  }
}
