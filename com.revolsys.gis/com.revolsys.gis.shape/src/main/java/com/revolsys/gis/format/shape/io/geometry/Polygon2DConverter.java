package com.revolsys.gis.format.shape.io.geometry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.format.shape.io.ShapeConstants;
import com.revolsys.gis.io.EndianInput;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.jts.CoordinateSequenceUtil;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

public class Polygon2DConverter implements ShapefileGeometryConverter {
  private GeometryFactory geometryFactory;

  public Polygon2DConverter() {
    this(null);
  }

  public Polygon2DConverter(
    final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      this.geometryFactory = geometryFactory;
    } else {
      this.geometryFactory = new GeometryFactory(new PrecisionModel());
    }
  }

  public int getShapeType() {
    return ShapeConstants.POLYGON_SHAPE;
  }

  public Geometry read(
    final EndianInput in,
    final long recordLength)
    throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int[] partIndex = ShapefileGeometryUtil.readPartIndex(in, numParts,
      numPoints);

    final CoordinateSequence[] parts = ShapefileGeometryUtil.createCoordinateSequences(
      partIndex, 2);

    ShapefileGeometryUtil.readPoints(in, partIndex, parts);
    final CoordinateSequence exteriorCoords = parts[0];
    final LinearRing shell = geometryFactory.createLinearRing(exteriorCoords);
    final LinearRing[] holes = new LinearRing[parts.length - 1];
    for (int j = 0; j < holes.length; j++) {
      holes[j] = geometryFactory.createLinearRing(parts[j]);
    }
    return geometryFactory.createPolygon(shell, holes);
  }

  public void write(
    final EndianOutput out,
    final Geometry geometry)
    throws IOException {
    if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;

      int numPoints = 0;

      final int numHoles = polygon.getNumInteriorRing();

      final List<CoordinateSequence> rings = new ArrayList<CoordinateSequence>();
      CoordinateSequence exteroirCoords = polygon.getExteriorRing()
        .getCoordinateSequence();
      if (JtsGeometryUtil.isCCW(exteroirCoords)) {
        exteroirCoords = CoordinateSequenceUtil.reverse(exteroirCoords);
      }
      rings.add(exteroirCoords);
      numPoints += exteroirCoords.size();
      for (int i = 0; i < numHoles; i++) {
        final LineString interior = polygon.getInteriorRingN(i);
        CoordinateSequence interiorCoords = interior.getCoordinateSequence();
        if (!JtsGeometryUtil.isCCW(interiorCoords)) {
          interiorCoords = CoordinateSequenceUtil.reverse(interiorCoords);
        }
        rings.add(interiorCoords);
        numPoints += interiorCoords.size();
      }

      final int numParts = 1 + numHoles;
      final int recordLength = 44 + 4 * numParts + 16 * numPoints;

      out.writeInt(recordLength / 2);
      out.writeLEInt(getShapeType());
      ShapefileGeometryUtil.writeEnvelope(out, polygon.getEnvelopeInternal());
      out.writeLEInt(numParts);
      out.writeLEInt(numPoints);

      int partIndex = 0;
      for (final CoordinateSequence ring : rings) {
        out.writeLEInt(partIndex);
        partIndex += ring.size();
      }

      for (final CoordinateSequence ring : rings) {
        ShapefileGeometryUtil.write2DCoordinates(out, ring);
      }
    } else {
      throw new IllegalArgumentException("Expecting " + Polygon.class
        + " geometry got " + geometry.getClass());
    }
  }
}
