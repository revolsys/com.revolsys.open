package com.revolsys.gis.geojson;

import java.io.BufferedWriter;
import java.io.Writer;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.json.JsonWriter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeoJsonDataObjectWriter extends AbstractWriter<DataObject> {

  boolean initialized = false;

  /** The writer */
  private JsonWriter out;

  public GeoJsonDataObjectWriter(
    final Writer out) {
    this.out = new JsonWriter(new BufferedWriter(out));
    this.out.setIndent(true);
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  public void close() {
    if (out != null) {
      try {
        writeFooter();
      } finally {
        out.close();
        out = null;
      }
    }
  }

  private void coordinate(
    final CoordinatesList coordinates,
    final int i) {
    final double x = coordinates.getX(i);
    out.print('[');
    out.value(x);

    final double y = coordinates.getY(i);
    out.print(',');
    out.value(y);

    final double z = coordinates.getZ(i);
    if (!Double.isNaN(z)) {
      out.print(',');
      out.value(z);
    }
    out.print(']');
  }

  private void coordinates(
    final CoordinatesList coordinates) {
    out.startList();
    out.indent();
    coordinate(coordinates, 0);
    for (int i = 1; i < coordinates.size(); i++) {
      out.endAttribute();
      out.indent();
      coordinate(coordinates, i);
    }
    out.endList();
  }

  private void coordinates(
    final LineString line) {
    final CoordinatesList coordinates = CoordinatesListUtil.get(line);
    coordinates(coordinates);
  }

  public void coordinates(
    final Point point) {
    final CoordinatesList coordinates = CoordinatesListUtil.get(point);
    coordinate(coordinates, 0);
  }

  public void coordinates(
    final Polygon polygon) {
    out.startList();
    out.indent();

    final LineString exteriorRing = polygon.getExteriorRing();
    coordinates(exteriorRing);
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      final LineString interiorRing = polygon.getInteriorRingN(i);
      out.endAttribute();
      coordinates(interiorRing);
    }

    out.endList();
  }

  @Override
  public void flush() {
    out.flush();
  }

  private void geometry(
    final Geometry geometry) {
    out.startObject();
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      point(point);
    } else if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      line(line);
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      polygon(polygon);
    } else if (geometry instanceof MultiPoint) {
      final MultiPoint multiPoint = (MultiPoint)geometry;
      multiPoint(multiPoint);
    } else if (geometry instanceof MultiLineString) {
      final MultiLineString multiLine = (MultiLineString)geometry;
      multiLineString(multiLine);
    } else if (geometry instanceof MultiPolygon) {
      final MultiPolygon multiPolygon = (MultiPolygon)geometry;
      multiPolygon(multiPolygon);
    }
    out.endObject();
  }

  private void line(
    final LineString line) {
    type(GeoJsonConstants.LINE_STRING);
    out.endAttribute();
    out.label(GeoJsonConstants.COORDINATES);
    coordinates(line);
  }

  private void multiLineString(
    final MultiLineString multiLineString) {
    type(GeoJsonConstants.MULTI_LINE_STRING);

    out.endAttribute();
    out.label(GeoJsonConstants.COORDINATES);
    out.startList();
    out.indent();
    final int numGeometries = multiLineString.getNumGeometries();
    if (numGeometries > 0) {
      coordinates((LineString)multiLineString.getGeometryN(0));
      for (int i = 1; i < numGeometries; i++) {
        final LineString lineString = (LineString)multiLineString.getGeometryN(i);
        out.endAttribute();
        coordinates(lineString);
      }
    }
    out.endList();
  }

  private void multiPoint(
    final MultiPoint multiPoint) {
    type(GeoJsonConstants.MULTI_POINT);

    out.endAttribute();
    out.label(GeoJsonConstants.COORDINATES);
    out.startList();
    out.indent();
    final int numGeometries = multiPoint.getNumGeometries();
    if (numGeometries > 0) {
      coordinates((Point)multiPoint.getGeometryN(0));
      for (int i = 1; i < numGeometries; i++) {
        final Point point = (Point)multiPoint.getGeometryN(i);
        out.endAttribute();
        coordinates(point);
      }
    }
    out.endList();
  }

  private void multiPolygon(
    final MultiPolygon multiPolygon) {
    type(GeoJsonConstants.MULTI_POLYGON);

    out.endAttribute();
    out.label(GeoJsonConstants.COORDINATES);
    out.startList();
    out.indent();
    final int numGeometries = multiPolygon.getNumGeometries();
    if (numGeometries > 0) {
      coordinates((Polygon)multiPolygon.getGeometryN(0));
      for (int i = 1; i < numGeometries; i++) {
        final Polygon polygon = (Polygon)multiPolygon.getGeometryN(i);
        out.endAttribute();
        coordinates(polygon);
      }
    }
    out.endList();
  }

  private void point(
    final Point point) {
    type(GeoJsonConstants.POINT);
    out.endAttribute();
    out.label(GeoJsonConstants.COORDINATES);
    coordinates(point);
  }

  private void polygon(
    final Polygon polygon) {
    type(GeoJsonConstants.POLYGON);

    out.endAttribute();
    out.label(GeoJsonConstants.COORDINATES);
    coordinates(polygon);
  }

  private void srid(
    final int srid) {
    final String urn = "urn:ogc:def:crs:EPSG::" + srid;
    out.label(GeoJsonConstants.CRS);
    out.startObject();
    type(GeoJsonConstants.NAME);
    out.endAttribute();
    out.label(GeoJsonConstants.PROPERTIES);
    out.startObject();
    out.label(GeoJsonConstants.NAME);
    out.value(urn);
    out.endObject();
    out.endObject();
  }

  private void type(
    final String type) {
    out.label(GeoJsonConstants.TYPE);
    out.value(type);
  }

  public void write(
    final DataObject object) {
    if (initialized) {
      out.endAttribute();
    } else {
      writeHeader();
      initialized = true;
    }
    out.startObject();
    type(GeoJsonConstants.FEATURE);

    final DataObjectMetaData metaData = object.getMetaData();
    final int geometryIndex = metaData.getGeometryAttributeIndex();
    if (geometryIndex != -1) {
      final Geometry geometry = object.getValue(geometryIndex);
      out.endAttribute();
      out.label(GeoJsonConstants.GEOMETRY);
      geometry(geometry);
    }
    final int numAttributes = metaData.getAttributeCount();
    if (numAttributes > 1 || numAttributes == 1 && geometryIndex == -1) {
      out.endAttribute();
      out.label(GeoJsonConstants.PROPERTIES);
      out.startObject();
      int lastIndex = numAttributes - 1;
      if (lastIndex == geometryIndex) {
        lastIndex--;
      }
      for (int i = 0; i < numAttributes; i++) {
        if (i != geometryIndex) {
          final String name = metaData.getAttributeName(i);
          final Object value = object.getValue(i);
          out.label(name);
          out.value(value);
        }
        if (i < lastIndex) {
          out.endAttribute();
        }
      }
      out.endObject();
    }
    out.endObject();
  }

  private void writeFooter() {
    out.endList();
    out.endObject();
    final String callback = getProperty(IoConstants.JSONP_PROPERTY);
    if (callback != null) {
      out.print(");");
    }
  }

  private void writeHeader() {
    final String callback = getProperty(IoConstants.JSONP_PROPERTY);
    if (callback != null) {
      out.print(callback);
      out.print('(');
    }
    out.startObject();
    type(GeoJsonConstants.FEATURE_COLLECTION);
    final Integer srid = getProperty(IoConstants.SRID_PROPERTY);
    if (srid != null && srid != 0) {
      out.endAttribute();
      srid(srid);
    }
    out.endAttribute();
    out.label(GeoJsonConstants.FEATURES);
    out.startList();
  }
}
