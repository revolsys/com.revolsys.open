package com.revolsys.record.io.format.gml.type;

import javax.xml.namespace.QName;

import com.revolsys.datatype.DataType;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.MultiLineString;
import com.revolsys.geometry.model.MultiPoint;
import com.revolsys.geometry.model.MultiPolygon;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.record.io.format.gml.Gml;
import com.revolsys.record.io.format.gml.GmlRecordWriter;
import com.revolsys.record.io.format.xml.XmlWriter;

public class GmlGeometryFieldType extends AbstractGmlFieldType {
  public GmlGeometryFieldType(final DataType dataType) {
    super(dataType, "xs:" + dataType.getName());
  }

  private void coordinates(final XmlWriter out, final LineString points) {
    out.startTag(Gml.COORDINATES);
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
    out.endTag(Gml.COORDINATES);
  }

  private void coordinates(final XmlWriter out, final Point point) {
    out.startTag(Gml.COORDINATES);
    final int axisCount = point.getAxisCount();
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      if (axisIndex > 0) {
        out.text(",");
      }
      final double value = point.getCoordinate(axisIndex);
      number(out, value);
    }
    out.endTag(Gml.COORDINATES);
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
    geometryCollection(out, Gml.MULTI_GEOMETRY, Gml.GEOMETRY_MEMBER, geometryCollection, writeSrsName);
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
    out.startTag(Gml.LINEAR_RING);
    final LineString points = line;
    coordinates(out, points);
    out.endTag(Gml.LINEAR_RING);
  }

  private void lineString(final XmlWriter out, final LineString line, final boolean writeSrsName) {
    out.startTag(Gml.LINE_STRING);
    srsName(out, line, writeSrsName);
    if (!line.isEmpty()) {
      final LineString points = line;
      coordinates(out, points);
    }
    out.endTag(Gml.LINE_STRING);
  }

  private void multiLineString(final XmlWriter out, final MultiLineString multiLine,
    final boolean writeSrsName) {
    geometryCollection(out, Gml.MULTI_LINE_STRING, Gml.LINE_STRING_MEMBER, multiLine, writeSrsName);
  }

  private void multiPoint(final XmlWriter out, final MultiPoint multiPoint,
    final boolean writeSrsName) {
    geometryCollection(out, Gml.MULTI_POINT, Gml.POINT_MEMBER, multiPoint, writeSrsName);
  }

  private void multiPolygon(final XmlWriter out, final MultiPolygon multiPolygon,
    final boolean writeSrsName) {
    geometryCollection(out, Gml.MULTI_POLYGON, Gml.POLYGON_MEMBER, multiPolygon, writeSrsName);
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
    out.startTag(Gml.POINT);
    srsName(out, point, writeSrsName);
    if (!point.isEmpty()) {
      coordinates(out, point);
    }
    out.endTag(Gml.POINT);
  }

  private void polygon(final XmlWriter out, final Polygon polygon, final boolean writeSrsName) {
    out.startTag(Gml.POLYGON);
    srsName(out, polygon, writeSrsName);
    if (!polygon.isEmpty()) {
      final LineString exteriorRing = polygon.getShell();
      out.startTag(Gml.OUTER_BOUNDARY_IS);
      linearRing(out, exteriorRing, false);
      out.endTag(Gml.OUTER_BOUNDARY_IS);

      for (int i = 0; i < polygon.getHoleCount(); i++) {
        final LineString interiorRing = polygon.getHole(i);
        out.startTag(Gml.INNER_BOUNDARY_IS);
        linearRing(out, interiorRing, false);
        out.endTag(Gml.INNER_BOUNDARY_IS);
      }
    }
    out.endTag(Gml.POLYGON);
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
