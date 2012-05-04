package com.revolsys.io.esri.gdb.xml.type;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.InPlaceIterator;
import com.revolsys.io.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.io.xml.XsiConstants;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class XmlGeometryFieldType extends AbstractEsriGeodatabaseXmlFieldType {
  public XmlGeometryFieldType(final FieldType esriFieldType,
    final DataType dataType) {
    super(dataType, "xs:" + dataType.getName(), esriFieldType);
  }

  @Override
  public int getFixedLength() {
    return 0;
  }

  @Override
  protected String getType(final Object value) {
    if (value instanceof Point) {
      return POINT_N_TYPE;
    } else if (value instanceof LineString || value instanceof MultiLineString) {
      return POLYLINE_N_TYPE;
    } else if (value instanceof Polygon) {
      return POLYGON_N_TYPE;
    }
    return null;
  }

  private void writeLineString(final XmlWriter out, final LineString line) {
    final CoordinatesList points = CoordinatesListUtil.get(line);
    final boolean hasZ = points.getNumAxis() > 2;
    out.element(HAS_ID, false);
    out.element(HAS_Z, hasZ);
    out.element(HAS_M, false);

    out.startTag(PATH_ARRAY);
    out.attribute(XsiConstants.TYPE, PATH_ARRAY_TYPE);

    writePath(out, points, hasZ);

    out.endTag(PATH_ARRAY);
  }

  private void writeMultiLineString(
    final XmlWriter out,
    final MultiLineString multiLine) {
    final boolean hasZ;
    if (multiLine.isEmpty()) {
      hasZ = false;
    } else {
      final CoordinatesList points = CoordinatesListUtil.get((LineString)multiLine.getGeometryN(0));
      hasZ = points.getNumAxis() > 2;
    }
    out.element(HAS_ID, false);
    out.element(HAS_Z, hasZ);
    out.element(HAS_M, false);

    out.startTag(PATH_ARRAY);
    out.attribute(XsiConstants.TYPE, PATH_ARRAY_TYPE);
    for (int i = 0; i < multiLine.getNumGeometries(); i++) {
      final LineString line = (LineString)multiLine.getGeometryN(i);
      final CoordinatesList points = CoordinatesListUtil.get(line);
      writePath(out, points, hasZ);
    }
    out.endTag(PATH_ARRAY);
  }

  public void writePath(
    final XmlWriter out,
    final CoordinatesList points,
    final boolean hasZ) {
    out.startTag(PATH);
    out.attribute(XsiConstants.TYPE, PATH_TYPE);

    writePointArray(out, points, hasZ);

    out.endTag(PATH);
  }

  public void writePoint(
    final XmlWriter out,
    final Coordinates coordinates,
    final boolean hasZ) {
    out.element(X, coordinates.getX());
    out.element(Y, coordinates.getY());
    if (hasZ) {
      out.element(Z, coordinates.getZ());
    }
  }

  private void writePoint(final XmlWriter out, final Point point) {
    final Coordinates coordinates = CoordinatesUtil.get(point);
    final boolean hasZ = coordinates.getNumAxis() > 2;
    writePoint(out, coordinates, hasZ);
  }

  public void writePointArray(
    final XmlWriter out,
    final CoordinatesList points,
    final boolean hasZ) {
    out.startTag(POINT_ARRAY);
    out.attribute(XsiConstants.TYPE, POINT_ARRAY_TYPE);

    for (final Coordinates point : new InPlaceIterator(points)) {
      out.startTag(POINT);
      out.attribute(XsiConstants.TYPE, POINT_N_TYPE);
      writePoint(out, point, hasZ);
      out.endTag(POINT);
    }

    out.endTag(POINT_ARRAY);
  }

  private void writePolygon(final XmlWriter out, final Polygon polygon) {
    final boolean hasZ;
    final LineString exteriorRing = polygon.getExteriorRing();
    final CoordinatesList points = CoordinatesListUtil.get(exteriorRing);
    hasZ = points.getNumAxis() > 2;
    out.element(HAS_ID, false);
    out.element(HAS_Z, hasZ);
    out.element(HAS_M, false);

    out.startTag(RING_ARRAY);
    out.attribute(XsiConstants.TYPE, RING_ARRAY_TYPE);

    writeRing(out, exteriorRing, hasZ);

    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      final LineString interiorRing = polygon.getInteriorRingN(i);
      writeRing(out, interiorRing, hasZ);
    }

    out.endTag(RING_ARRAY);
  }

  private void writeRing(
    final XmlWriter out,
    final LineString line,
    final boolean hasZ) {
    out.startTag(RING);
    out.attribute(XsiConstants.TYPE, RING_TYPE);
    final CoordinatesList points = CoordinatesListUtil.get(line);

    writePointArray(out, points, hasZ);

    out.endTag(RING);
  }

  @Override
  protected void writeValueText(final XmlWriter out, final Object value) {
    if (value instanceof Point) {
      final Point point = (Point)value;
      writePoint(out, point);
    } else if (value instanceof LineString) {
      final LineString line = (LineString)value;
      writeLineString(out, line);
    } else if (value instanceof Polygon) {
      final Polygon polygon = (Polygon)value;
      writePolygon(out, polygon);
    } else if (value instanceof MultiLineString) {
      final MultiLineString multiLine = (MultiLineString)value;
      writeMultiLineString(out, multiLine);
    }
  }
}
