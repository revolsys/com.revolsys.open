package com.revolsys.format.gml.type;

import javax.xml.namespace.QName;

import com.revolsys.data.types.DataType;
import com.revolsys.format.gml.GmlConstants;
import com.revolsys.format.gml.GmlRecordWriter;
import com.revolsys.format.xml.XmlWriter;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;

public class GmlGeometryFieldType extends AbstractGmlFieldType {
  public GmlGeometryFieldType(final DataType dataType) {
    super(dataType, "xs:" + dataType.getName());
  }

  private void coordinates(final XmlWriter out, final LineString points) {
    out.startTag(GmlConstants.COORDINATES);
    final int axisCount = points.getAxisCount();
    boolean first = true;
    for (int i = 0; i < points.getVertexCount(); i++) {
      if (first) {
        first = false;
      } else {
        out.text(" ");
      }
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        if (axisIndex > 0) {
          out.text(",");
        }
        final double value = points.getCoordinate(i, axisIndex);
        number(out, value);
      }
    }
    out.endTag(GmlConstants.COORDINATES);
  }

  private void coordinates(final XmlWriter out, final Point point) {
    out.startTag(GmlConstants.COORDINATES);
    final int axisCount = point.getAxisCount();
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      if (axisIndex > 0) {
        out.text(",");
      }
      final double value = point.getCoordinate(axisIndex);
      number(out, value);
    }
    out.endTag(GmlConstants.COORDINATES);
  }

  private void geometry(final XmlWriter out, final Object value, final boolean writeSrsName) {
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

  private void geometryCollection(final XmlWriter out, final GeometryCollection geometryCollection,
    final boolean writeSrsName) {
    geometryCollection(out, MULTI_GEOMETRY, GEOMETRY_MEMBER, geometryCollection, writeSrsName);
  }

  private void geometryCollection(final XmlWriter out, final QName tag, final QName memberTag,
    final GeometryCollection geometryCollection, final boolean writeSrsName) {
    out.startTag(tag);
    srsName(out, geometryCollection, writeSrsName);
    for (int i = 0; i < geometryCollection.getGeometryCount(); i++) {
      final Geometry geometry = geometryCollection.getGeometry(i);
      out.startTag(memberTag);
      geometry(out, geometry, false);
      out.endTag(memberTag);
    }
    out.endTag(tag);
  }

  private void linearRing(final XmlWriter out, final LineString line, final boolean writeSrsName) {
    out.startTag(LINEAR_RING);
    final LineString points = line;
    coordinates(out, points);
    out.endTag(LINEAR_RING);
  }

  private void lineString(final XmlWriter out, final LineString line, final boolean writeSrsName) {
    out.startTag(LINE_STRING);
    srsName(out, line, writeSrsName);
    if (!line.isEmpty()) {
      final LineString points = line;
      coordinates(out, points);
    }
    out.endTag(LINE_STRING);
  }

  private void multiLineString(final XmlWriter out, final MultiLineString multiLine,
    final boolean writeSrsName) {
    geometryCollection(out, MULTI_LINE_STRING, LINE_STRING_MEMBER, multiLine, writeSrsName);
  }

  private void multiPoint(final XmlWriter out, final MultiPoint multiPoint,
    final boolean writeSrsName) {
    geometryCollection(out, MULTI_POINT, POINT_MEMBER, multiPoint, writeSrsName);
  }

  private void multiPolygon(final XmlWriter out, final MultiPolygon multiPolygon,
    final boolean writeSrsName) {
    geometryCollection(out, MULTI_POLYGON, POLYGON_MEMBER, multiPolygon, writeSrsName);
  }

  public void number(final XmlWriter out, final double value) {
    if (Double.isInfinite(value)) {
      if (value < 0) {
        out.text("-INF");
      } else {
        out.text("INF");
      }
    } else if (Double.isNaN(value)) {
      out.text("NaN");
    } else {
      out.text(value);
    }
  }

  private void point(final XmlWriter out, final Point point, final boolean writeSrsName) {
    out.startTag(POINT);
    srsName(out, point, writeSrsName);
    if (!point.isEmpty()) {
      coordinates(out, point);
    }
    out.endTag(POINT);
  }

  private void polygon(final XmlWriter out, final Polygon polygon, final boolean writeSrsName) {
    out.startTag(POLYGON);
    srsName(out, polygon, writeSrsName);
    if (!polygon.isEmpty()) {
      final LineString exteriorRing = polygon.getShell();
      out.startTag(OUTER_BOUNDARY_IS);
      linearRing(out, exteriorRing, false);
      out.endTag(OUTER_BOUNDARY_IS);

      for (int i = 0; i < polygon.getHoleCount(); i++) {
        final LineString interiorRing = polygon.getHole(i);
        out.startTag(INNER_BOUNDARY_IS);
        linearRing(out, interiorRing, false);
        out.endTag(INNER_BOUNDARY_IS);
      }
    }
    out.endTag(POLYGON);
  }

  private void srsName(final XmlWriter out, final Geometry geometry, final boolean writeSrsName) {
    if (writeSrsName) {
      final GeometryFactory factory = geometry.getGeometryFactory();
      GmlRecordWriter.srsName(out, factory);
    }
  }

  @Override
  protected void writeValueText(final XmlWriter out, final Object value) {
    geometry(out, value, true);
  }
}
