package com.revolsys.io.gml.type;

import javax.xml.namespace.QName;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.io.gml.GmlConstants;
import com.revolsys.io.gml.GmlDataObjectWriter;
import com.revolsys.io.xml.io.XmlWriter;
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

  private void geometry(
    final XmlWriter out,
    final Object value,
    final boolean writeSrsName) {
    if (value instanceof Point) {
      final Point point = (Point)value;
      point(out, point, writeSrsName);
    } else if (value instanceof LineString) {
      final LineString line = (LineString)value;
      lineString(out, line, writeSrsName);
    } else if (value instanceof Polygon) {
      final Polygon polygon = (Polygon)value;
      polygon(out, polygon, writeSrsName);
    } else if (value instanceof MultiPoint) {
      final MultiPoint multiPoint = (MultiPoint)value;
      multiPoint(out, multiPoint, writeSrsName);
    } else if (value instanceof MultiLineString) {
      final MultiLineString multiLine = (MultiLineString)value;
      multiLineString(out, multiLine, writeSrsName);
    } else if (value instanceof MultiPolygon) {
      final MultiPolygon multiPolygon = (MultiPolygon)value;
      multiPolygon(out, multiPolygon, writeSrsName);
    } else if (value instanceof GeometryCollection) {
      final GeometryCollection geometryCollection = (GeometryCollection)value;
      geometryCollection(out, geometryCollection, writeSrsName);
    }
  }

  private void geometryCollection(
    final XmlWriter out,
    final GeometryCollection geometryCollection,
    boolean writeSrsName) {
    geometryCollection(out, MULTI_GEOMETRY, GEOMETRY_MEMBER,
      geometryCollection, writeSrsName);
  }

  private void geometryCollection(
    final XmlWriter out,
    final QName tag,
    final QName memberTag,
    final GeometryCollection geometryCollection,
    final boolean writeSrsName) {
    out.startTag(tag);
    srsName(out, geometryCollection, writeSrsName);
    for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
      final Geometry geometry = geometryCollection.getGeometryN(i);
      out.startTag(memberTag);
      geometry(out, geometry, false);
      out.endTag(memberTag);
    }
    out.endTag(tag);
  }

  private void linearRing(
    final XmlWriter out,
    final LineString line,
    final boolean writeSrsName) {
    out.startTag(LINEAR_RING);
    final CoordinatesList points = CoordinatesListUtil.get(line);
    coordinates(out, points);
    out.endTag(LINEAR_RING);
  }

  private void lineString(
    final XmlWriter out,
    final LineString line,
    final boolean writeSrsName) {
    out.startTag(LINE_STRING);
    srsName(out, line, writeSrsName);
    final CoordinatesList points = CoordinatesListUtil.get(line);
    coordinates(out, points);
    out.endTag(LINE_STRING);
  }

  private void multiLineString(
    final XmlWriter out,
    final MultiLineString multiLine,
    boolean writeSrsName) {
    geometryCollection(out, MULTI_LINE_STRING, LINE_STRING_MEMBER, multiLine,
      writeSrsName);
  }

  private void multiPoint(
    final XmlWriter out,
    final MultiPoint multiPoint,
    boolean writeSrsName) {
    geometryCollection(out, MULTI_POINT, POINT_MEMBER, multiPoint, writeSrsName);
  }

  private void multiPolygon(
    final XmlWriter out,
    final MultiPolygon multiPolygon,
    boolean writeSrsName) {
    geometryCollection(out, MULTI_POLYGON, POLYGON_MEMBER, multiPolygon,
      writeSrsName);
  }

  private void point(
    final XmlWriter out,
    final Point point,
    final boolean writeSrsName) {
    out.startTag(POINT);
    srsName(out, point, writeSrsName);
    final CoordinatesList points = CoordinatesListUtil.get(point);
    coordinates(out, points);
    out.endTag(POINT);
  }

  private void polygon(
    final XmlWriter out,
    final Polygon polygon,
    final boolean writeSrsName) {
    out.startTag(POLYGON);
    srsName(out, polygon, writeSrsName);

    final LineString exteriorRing = polygon.getExteriorRing();
    out.startTag(OUTER_BOUNDARY_IS);
    linearRing(out, exteriorRing, false);
    out.endTag(OUTER_BOUNDARY_IS);

    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      final LineString interiorRing = polygon.getInteriorRingN(i);
      out.startTag(INNER_BOUNDARY_IS);
      linearRing(out, interiorRing, false);
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

  private void coordinates(
    final XmlWriter out,
    final CoordinatesList points) {
    out.startTag(GmlConstants.COORDINATES);
    final byte numAxis = points.getNumAxis();
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
      out.text(",");
      out.text(y);
      if (numAxis > 2) {
        final double z = points.getZ(i);
        if (Double.isNaN(z)) {
          out.text(0);
        } else {
          out.text(z);
        }
      }
    }
    out.endTag(GmlConstants.COORDINATES);
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
    XmlWriter out,
    final Geometry geometry,
    boolean writeSrsName) {
    if (writeSrsName) {
      GeometryFactory factory = GeometryFactory.getFactory(geometry);
      GmlDataObjectWriter.srsName(out, factory);
    }
  }

  @Override
  protected void writeValueText(
    final XmlWriter out,
    final Object value) {
    geometry(out, value, true);
  }
}
