package com.revolsys.gis.format.shape.io.geometry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.format.shape.io.ShapeConstants;
import com.revolsys.gis.io.EndianInput;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

public class Polygon2DMConverter implements ShapefileGeometryConverter {
  private GeometryFactory geometryFactory;

  public Polygon2DMConverter() {
    this(null);
  }

  public Polygon2DMConverter(
    final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      this.geometryFactory = geometryFactory;
    } else {
      this.geometryFactory = new GeometryFactory(new PrecisionModel());
    }
  }

  public int getShapeType() {
    return ShapeConstants.POLYGON_Z_SHAPE;
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

    byte dimension = 3;
    if (recordLength > 44 + 4 * numParts + 24 * numPoints) {
      dimension = 4;
    }

    final CoordinatesList[] parts = ShapefileGeometryUtil.createCoordinatesLists(
      partIndex, dimension);

    ShapefileGeometryUtil.readPoints(in, partIndex, parts);
    if (dimension == 4) {
      ShapefileGeometryUtil.readCoordinates(in, partIndex, parts, 3);
    }
    final CoordinatesList exteriorCoords = parts[0];
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

      final List<CoordinatesList> rings = new ArrayList<CoordinatesList>();
      CoordinatesList exteroirCoords = CoordinatesListUtil.get(polygon.getExteriorRing());
      if (JtsGeometryUtil.isCCW(exteroirCoords)) {
        exteroirCoords = exteroirCoords.reverse();
      }
      rings.add(exteroirCoords);
      numPoints += exteroirCoords.size();
      for (int i = 0; i < numHoles; i++) {
        final LineString interior = polygon.getInteriorRingN(i);
        CoordinatesList interiorCoords = CoordinatesListUtil.get(interior);
        if (!JtsGeometryUtil.isCCW(interiorCoords)) {
          interiorCoords = interiorCoords.reverse();
        }
        rings.add(interiorCoords);
        numPoints += interiorCoords.size();
      }

      final int numParts = 1 + numHoles;
      final int recordLength = 60 + 4 * numParts + 24 * numPoints;

      out.writeInt(recordLength / 2);
      out.writeLEInt(getShapeType());
      ShapefileGeometryUtil.writeEnvelope(out, polygon.getEnvelopeInternal());
      out.writeLEInt(numParts);
      out.writeLEInt(numPoints);

      int partIndex = 0;
      for (final CoordinatesList ring : rings) {
        out.writeLEInt(partIndex);
        partIndex += ring.size();
      }

      for (final CoordinatesList ring : rings) {
        ShapefileGeometryUtil.write2DCoordinates(out, ring);
      }
      double minZ = Double.MAX_VALUE;
      double maxZ = Double.MIN_VALUE;
      for (final CoordinatesList ring : rings) {
        for (int i = 0; i < ring.size(); i++) {
          double z = ring.getOrdinate(i, 2);
          if (Double.isNaN(z)) {
            z = 0;
          }
          minZ = Math.min(z, minZ);
          maxZ = Math.max(z, maxZ);
        }
      }

      out.writeLEDouble(minZ);
      out.writeLEDouble(maxZ);
      for (final CoordinatesList ring : rings) {
        for (int i = 0; i < ring.size(); i++) {
          double z = ring.getOrdinate(i, 2);
          if (Double.isNaN(z)) {
            z = 0;
          }
          out.writeLEDouble(z);
        }
      }

    } else {
      throw new IllegalArgumentException("Expecting " + Polygon.class
        + " geometry got " + geometry.getClass());
    }
  }
}
