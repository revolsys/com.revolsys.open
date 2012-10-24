package com.revolsys.io.shp.geometry;

import java.io.IOException;
import java.util.List;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.io.EndianInput;
import com.revolsys.io.shp.ShapefileConstants;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Geometry;

public class Polygon3DConverter implements ShapefileGeometryConverter {
  private GeometryFactory geometryFactory;

  public Polygon3DConverter() {
    this(null);
  }

  public Polygon3DConverter(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      this.geometryFactory = geometryFactory;
    } else {
      this.geometryFactory = GeometryFactory.getFactory();
    }
  }

  @Override
  public int getShapeType() {
    return ShapefileConstants.POLYGON_ZM_SHAPE;
  }

  @Override
  public Geometry read(final EndianInput in, final long recordLength)
    throws IOException {
    in.skipBytes(4 * MathUtil.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int numPoints = in.readLEInt();
    final int[] partIndex = ShapefileGeometryUtil.SHP_INSTANCE.readPartIndex(
      in, numParts, numPoints);

    byte dimension = 3;
    if (recordLength > 44 + 4 * numParts + 24 * numPoints) {
      dimension = 4;
    }

    final List<CoordinatesList> parts = ShapefileGeometryUtil.SHP_INSTANCE.createCoordinatesLists(
      partIndex, dimension);

    ShapefileGeometryUtil.SHP_INSTANCE.readPoints(in, partIndex, parts);
    ShapefileGeometryUtil.SHP_INSTANCE.readCoordinates(in, partIndex, parts, 2);
    if (dimension == 4) {
      ShapefileGeometryUtil.SHP_INSTANCE.readCoordinates(in, partIndex, parts,
        3);
    }
    return geometryFactory.createPolygon(parts);
  }

  @Override
  public void write(final EndianOutput out, final Geometry geometry)
    throws IOException {
    ShapefileGeometryUtil.SHP_INSTANCE.writePolygonZ(out, geometry);

  }
}
