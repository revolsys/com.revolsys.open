package com.revolsys.io.shp.geometry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.io.EndianInput;
import com.revolsys.io.shp.ShapefileConstants;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class MultiPolygonConverter implements ShapefileGeometryConverter {
  private GeometryFactory geometryFactory;

  public MultiPolygonConverter() {
    this(null);
  }

  public MultiPolygonConverter(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      this.geometryFactory = geometryFactory;
    } else {
      this.geometryFactory = GeometryFactory.getFactory();
    }
  }

  private int addPart(final int partType, final int index,
    final List<Integer> partIndexes, final List<Integer> partTypes,
    final List<CoordinatesList> partPoints, final LineString ring) {
    partIndexes.add(index);
    partTypes.add(partType);
    final CoordinatesList points = CoordinatesListUtil.get(ring);
    partPoints.add(points);

    return points.size();
  }

  public int getShapeType() {
    return ShapefileConstants.MULTI_PATCH_SHAPE;
  }

  public Geometry read(final EndianInput in, final long recordLength)
    throws IOException {
    final List<Polygon> polygons = new ArrayList<Polygon>();
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    int dimension;
    if (recordLength > 44 + 8 * numParts + 16 * numPoints) {
      dimension = 3;
    } else {
      dimension = 2;
    }
    final int[] partIndex = ShapefileGeometryUtil.readPartIndex(in, numParts,
      numPoints);
    final int[] partTypes = ShapefileGeometryUtil.readIntArray(in, numParts);

    final List<CoordinatesList> parts = ShapefileGeometryUtil.createCoordinatesLists(
      partIndex, dimension);
    ShapefileGeometryUtil.readPoints(in, partIndex, parts);
    if (dimension > 2) {
      ShapefileGeometryUtil.readCoordinates(in, partIndex, parts, 2);
    }
    List<CoordinatesList> rings = new ArrayList<CoordinatesList>();
    for (int i = 0; i < numParts; i++) {
      final int partType = partTypes[i];
      final CoordinatesList points = parts.get(i);
      switch (partType) {
        case 2:
          if (!rings.isEmpty()) {
            final Polygon polygon = geometryFactory.createPolygon(rings);
            polygons.add(polygon);
            rings = new ArrayList<CoordinatesList>();
          }
          rings.add(points);
        break;
        case 3:
          if (rings.isEmpty()) {
            throw new IllegalStateException(
              "Interior ring without a exterior ring");
          } else {
            rings.add(points);

          }
        break;

        default:
          throw new IllegalStateException("Unsupported part type " + partType);
      }
    }
    if (!rings.isEmpty()) {
      final Polygon polygon = geometryFactory.createPolygon(rings);
      polygons.add(polygon);
    }
    return geometryFactory.createMultiPolygon(polygons);
  }

  public void write(final EndianOutput out, final Geometry geometry)
    throws IOException {
    if (geometry instanceof MultiPolygon) {
      final MultiPolygon multiPolygon = (MultiPolygon)geometry;

      int numPoints = 0;
      final List<Integer> partIndexes = new ArrayList<Integer>();
      final List<Integer> partTypes = new ArrayList<Integer>();
      final List<CoordinatesList> partPoints = new ArrayList<CoordinatesList>();
      boolean hasZ = false;
      for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
        final Polygon polygon = (Polygon)multiPolygon.getGeometryN(i);
        final LineString exteriorRing = polygon.getExteriorRing();
        if (exteriorRing.getDimension() > 2) {
          hasZ = true;
        }
        numPoints += addPart(ShapefileConstants.OUTER_RING, numPoints,
          partIndexes, partTypes, partPoints, exteriorRing);
        for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
          final LineString innerRing = polygon.getInteriorRingN(j);
          numPoints += addPart(ShapefileConstants.INNER_RING, numPoints,
            partIndexes, partTypes, partPoints, innerRing);
        }
      }

      final int numParts = partIndexes.size();
      int recordLength = 44 + 8 * numParts + 16 * numPoints;
      if (hasZ) {
        recordLength += 16 + 8 * numPoints;
      }

      out.writeInt(recordLength / 2);
      out.writeLEInt(getShapeType());
      ShapefileGeometryUtil.writeEnvelope(out,
        multiPolygon.getEnvelopeInternal());
      out.writeLEInt(numParts);
      out.writeLEInt(numPoints);
      for (final Integer partIndex : partIndexes) {
        out.writeLEInt(partIndex);
      }
      for (final Integer partType : partTypes) {
        out.writeLEInt(partType);
      }

      for (final CoordinatesList points : partPoints) {
        ShapefileGeometryUtil.writeXYCoordinates(out, points);
      }
      if (hasZ) {
        ShapefileGeometryUtil.writeZCoordinates(out, partPoints);
      }
    } else {
      throw new IllegalArgumentException("Expecting " + MultiPolygon.class
        + " geometry got " + geometry.getClass());
    }
  }
}
