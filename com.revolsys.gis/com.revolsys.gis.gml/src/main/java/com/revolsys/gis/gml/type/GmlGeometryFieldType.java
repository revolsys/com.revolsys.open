package com.revolsys.gis.gml.type;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.gml.GmlConstants;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.xml.io.XmlWriter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GmlGeometryFieldType extends AbstractGmlFieldType {
  public GmlGeometryFieldType(
    final DataType dataType) {
    super(dataType, "xs:" + dataType.getName().getLocalPart());
  }

  public void geometry(
    final XmlWriter out,
    final Object value) {
    if (value instanceof Point) {
      final Point point = (Point)value;
      point(out, point);
    } else if (value instanceof LineString) {
      final LineString line = (LineString)value;
      lineString(out, line);
    } else if (value instanceof Polygon) {
      final Polygon polygon = (Polygon)value;
      polygon(out, polygon);
    } else if (value instanceof MultiPoint) {
      final MultiPoint multiPoint = (MultiPoint)value;
      multiPoint(out, multiPoint);
    } else if (value instanceof MultiLineString) {
      final MultiLineString multiLine = (MultiLineString)value;
      multiLineString(out, multiLine);
    } else if (value instanceof MultiPolygon) {
      final MultiPolygon multiPolygon = (MultiPolygon)value;
      multiPolygon(out, multiPolygon);
    } else if (value instanceof GeometryCollection) {
      final GeometryCollection geometryCollection = (GeometryCollection)value;
      geometryCollection(out, geometryCollection);
    }
  }

  private void geometryCollection(
    final XmlWriter out,
    final GeometryCollection geometryCollection) {
    geometryCollection(out, MULTI_GEOMETRY, GEOMETRY_MEMBERS,
      geometryCollection);
  }

  private void geometryCollection(
    final XmlWriter out,
    final QName tag,
    final QName membersTag,
    final GeometryCollection geometryCollection) {
    out.startTag(tag);
    out.startTag(membersTag);
    for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
      final Geometry geometry = geometryCollection.getGeometryN(i);
      geometry(out, geometry);
    }
    out.endTag(membersTag);
    out.endTag(tag);
  }

  private void linearRing(
    final XmlWriter out,
    final LineString line) {
    out.startTag(LINEAR_RING);
    srsName(line);
    final CoordinatesList points = CoordinatesListUtil.get(line);
    posList(out, points);
    out.endTag(LINEAR_RING);
  }

  private void lineString(
    final XmlWriter out,
    final LineString line) {
    out.startTag(LINE_STRING);
    srsName(line);
    final CoordinatesList points = CoordinatesListUtil.get(line);
    posList(out, points);
    out.endTag(LINE_STRING);
  }

  private void multiLineString(
    final XmlWriter out,
    final MultiLineString multiLine) {
    geometryCollection(out, MULTI_CURVE, CURVE_MEMBERS, multiLine);
  }

  private void multiPoint(
    final XmlWriter out,
    final MultiPoint multiPoint) {
    geometryCollection(out, MULTI_POINT, POINT_MEMBERS, multiPoint);
  }

  private void multiPolygon(
    final XmlWriter out,
    final MultiPolygon multiPolygon) {
    geometryCollection(out, MULTI_SURFACE, SURFACE_MEMBERS, multiPolygon);
  }

  private void point(
    final XmlWriter out,
    final Point point) {
    out.startTag(POINT);
    srsName(point);
    final Coordinates coordinates = CoordinatesUtil.get(point);
    pos(out, coordinates);
    out.endTag(POINT);
  }

  private void polygon(
    final XmlWriter out,
    final Polygon polygon) {
    out.startTag(POLYGON);
    srsName(polygon);

    final LineString exteriorRing = polygon.getExteriorRing();
    out.startTag(OUTER_BOUNDARY_IS);
    linearRing(out, exteriorRing);
    out.endTag(OUTER_BOUNDARY_IS);

    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      final LineString interiorRing = polygon.getInteriorRingN(i);
      out.startTag(INNER_BOUNDARY_IS);
      linearRing(out, interiorRing);
      out.endTag(INNER_BOUNDARY_IS);
    }

    out.endTag(POLYGON);
  }

  private void pos(
    final XmlWriter out,
    final Coordinates coordinates) {
    out.startTag(GmlConstants.POS);
    final byte numAxis = coordinates.getNumAxis();
    out.attribute(GmlConstants.DIMENSION, numAxis);
    final double x = coordinates.getX();
    out.text(x);
    final double y = coordinates.getY();
    out.text(" ");
    out.text(y);
    if (numAxis > 2) {
      final double z = coordinates.getZ();
      out.text(" ");
      if (Double.isNaN(z)) {
        out.text(0);
      } else {
        out.text(z);
      }
    }
    out.endTag(GmlConstants.POS);
  }

  private void posList(
    final XmlWriter out,
    final CoordinatesList points) {
    out.startTag(GmlConstants.POS_LIST);
    final byte numAxis = points.getNumAxis();
    out.attribute(GmlConstants.DIMENSION, numAxis);
    boolean first = true;
    for (int i = 0; i < points.size(); i++) {
      if (first) {
        first = false;
      } else {
        out.text(" ");
      }
      final double x = points.getX(i);
      out.text(x);
      final double y = points.getY(i);
      out.text(" ");
      out.text(y);
      if (numAxis > 2) {
        final double z = points.getZ(i);
        out.text(" ");
        if (Double.isNaN(z)) {
          out.text(0);
        } else {
          out.text(z);
        }
      }
    }
    out.endTag(GmlConstants.POS_LIST);
  }

  private void srsName(
    final Geometry geometry) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void writeValueText(
    final XmlWriter out,
    final Object value) {
    geometry(out, value);
  }
}
