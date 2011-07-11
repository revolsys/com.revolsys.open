package com.revolsys.gis.format.shape.io.geometry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.format.shape.io.ShapefileConstants;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.io.EndianInput;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.revolsys.gis.cs.GeometryFactory;
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
      this.geometryFactory = new GeometryFactory();
    }
  }

  public int getShapeType() {
    return ShapefileConstants.POLYGON_SHAPE;
  }

  public Geometry read(
    final EndianInput in,
    final long recordLength)
    throws IOException {
    return ShapefileGeometryUtil.readPolygon(geometryFactory, in);
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
      final int recordLength = 44 + 4 * numParts + 16 * numPoints;

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
    } else {
      throw new IllegalArgumentException("Expecting " + Polygon.class
        + " geometry got " + geometry.getClass());
    }
  }
}
