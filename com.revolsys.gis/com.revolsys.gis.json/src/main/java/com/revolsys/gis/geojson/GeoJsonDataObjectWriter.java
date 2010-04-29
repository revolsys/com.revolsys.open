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
    int i) {
    final double x = coordinates.getX(i);
    out.print('[');
    out.value(x);

    final double y = coordinates.getY(i);
    out.print(',');
    out.value(y);

    double z = coordinates.getZ(i);
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
    }
    out.endObject();
  }

  private void line(
    final LineString line) {
    type("LineString");
    out.endAttribute();
    out.label("coordinates");
    coordinates(line);
  }

  private void point(
    final Point point) {
    type("Point");
    out.endAttribute();
    final CoordinatesList coordinates = CoordinatesListUtil.get(point);
    out.label("coordinates");
    coordinate(coordinates, 0);
  }

  private void polygon(
    final Polygon polygon) {
    type("Polygon");

    out.endAttribute();
    out.label("coordinates");
    final LineString exteriorRing = polygon.getExteriorRing();
    coordinates(exteriorRing);
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      LineString interiorRing = polygon.getInteriorRingN(i);
      coordinates(interiorRing);
    }
  }

  private void type(
    final String type) {
    out.label("type");
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
    type("Feature");

    final DataObjectMetaData metaData = object.getMetaData();
    final int geometryIndex = metaData.getGeometryAttributeIndex();
    if (geometryIndex != -1) {
      final Geometry geometry = object.getValue(geometryIndex);
      out.endAttribute();
      out.label("geometry");
      geometry(geometry);
    }
    final int numAttributes = metaData.getAttributeCount();
    if (numAttributes > 1 || numAttributes == 1 && geometryIndex == -1) {
      out.endAttribute();
      out.label("properties");
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
    type("FeatureCollection");
    Integer srid = getProperty(IoConstants.SRID_PROPERTY);
    if (srid != null && srid != 0) {
      out.endAttribute();
      srid(srid);
    }
    out.endAttribute();
    out.label("features");
    out.startList();
  }

  private void srid(
    int srid) {
    String urn = "urn:ogc:def:crs:EPSG::" + srid;
    out.label("crs");
    out.startObject();
    type("name");
    out.endAttribute();
    out.label("properties");
    out.startObject();
    out.label("name");
    out.value(urn);
    out.endObject();
    out.endObject();
  }
}
