package com.revolsys.gis.kml.io;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;

import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.util.UrlUtil;
import com.revolsys.xml.io.XmlWriter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class KmlXmlWriter extends XmlWriter implements Kml22Constants {

  public KmlXmlWriter(
    final OutputStream out) {
    super(out);
  }

  public KmlXmlWriter(
    final OutputStream out,
    final boolean useNamespaces) {
    super(out, useNamespaces);
  }

  public KmlXmlWriter(
    final OutputStream out,
    final String charsetName)
    throws UnsupportedEncodingException {
    super(out, charsetName);
  }

  public KmlXmlWriter(
    final OutputStream out,
    final String charsetName,
    final boolean useNamespaces)
    throws UnsupportedEncodingException {
    super(out, charsetName, useNamespaces);
  }

  public KmlXmlWriter(
    final Writer out) {
    super(out);
  }

  public KmlXmlWriter(
    final Writer out,
    final boolean useNamespaces) {
    super(out, useNamespaces);
  }

  private void write(
    final CoordinateSequence coordinateSequence) {
    startTag(Kml22Constants.COORDINATES);
    boolean hasZ = coordinateSequence.getDimension() > 2;
    for (int i = 0; i < coordinateSequence.size(); i++) {
      write(String.valueOf(coordinateSequence.getX(i)));
      write(',');
      write(String.valueOf(coordinateSequence.getY(i)));
      if (hasZ) {
        final double z = coordinateSequence.getOrdinate(i, 2);
        if (!Double.isNaN(z)) {
          write(',');
          write(String.valueOf(z));
        }
      }
      write(' ');
    }
    endTag();
  }

  public void writeData(
    final String name,
    final Object value) {
    if (value != null) {
      startTag(DATA);
      attribute(NAME, name);
      element(VALUE, value);
      endTag();
    }
  }

  public void writeGeometry(
    final Geometry geometry) {
    if (geometry != null) {
      final int numGeometries = geometry.getNumGeometries();
      if (numGeometries > 1) {
        startTag(MULTI_GEOMETRY);
        for (int i = 0; i < numGeometries; i++) {
          writeGeometry(geometry.getGeometryN(i));
        }
        endTag();
      } else {
        Geometry geoGraphicsGeom = GeometryProjectionUtil.perform(geometry,
          COORDINATE_SYSTEM);
        if (geoGraphicsGeom instanceof Point) {
          final Point point = (Point)geoGraphicsGeom;
          writePoint(point);
        } else if (geoGraphicsGeom instanceof LinearRing) {
          final LinearRing line = (LinearRing)geoGraphicsGeom;
          writeLinearRing(line);
        } else if (geoGraphicsGeom instanceof LineString) {
          final LineString line = (LineString)geoGraphicsGeom;
          writeLineString(line);
        } else if (geoGraphicsGeom instanceof Polygon) {
          final Polygon polygon = (Polygon)geoGraphicsGeom;
          writePolygon(polygon);
        }
      }
    }
  }

  public void writeLatLonBox(
    final Envelope envelope) {
    startTag(LAT_LON_BOX);
    element(NORTH, envelope.getMaxY());
    element(SOUTH, envelope.getMinY());
    element(WEST, envelope.getMinX());
    element(EAST, envelope.getMaxX());
    endTag();

  }

  private void writeLinearRing(
    final LineString ring) {
    startTag(Kml22Constants.LINEAR_RING);
    final CoordinateSequence coordinateSequence = ring.getCoordinateSequence();
    write(coordinateSequence);
    endTag();

  }

  private void writeLineString(
    final LineString line) {
    startTag(Kml22Constants.LINE_STRING);
    final CoordinateSequence coordinateSequence = line.getCoordinateSequence();
    write(coordinateSequence);
    endTag();
  }

  public void writeNetworkLink(
    final Envelope envelope,
    final String name,
    final Integer minLod,
    final Integer maxLod,
    final String href) {

    startTag(NETWORK_LINK);
    if (name != null) {
      element(NAME, name);
    }
    writeRegion(envelope, minLod, maxLod);
    startTag(LINK);
    element(HREF, href);
    element(VIEW_REFRESH_MODE, "onRegion");

    endTag();
    endTag();

  }

  public void writePlacemarkLineString(
    final Envelope envelope,
    final String name,
    final String styleUrl) {

    startTag(PLACEMARK);
    if (name != null) {
      element(NAME, name);
    }
    if (styleUrl != null) {
      element(STYLE_URL, styleUrl);
    }
    startTag(LINE_STRING);
    startTag(COORDINATES);
    final double maxY = envelope.getMaxY();
    final double minY = envelope.getMinY();
    final double maxX = envelope.getMaxX();
    final double minX = envelope.getMinX();
    write(Double.toString(minX));
    write(',');
    write(Double.toString(minY));
    write(' ');
    write(Double.toString(maxX));
    write(',');
    write(Double.toString(minY));
    write(' ');
    write(Double.toString(maxX));
    write(',');
    write(Double.toString(maxY));
    write(' ');
    write(Double.toString(minX));
    write(',');
    write(Double.toString(maxY));
    write(' ');
    write(Double.toString(minX));
    write(',');
    write(Double.toString(minY));

    endTag();
    endTag();
    endTag();

  }

  public void writePlacemarkLineString(
    final LineString lineString,
    final String name,
    final String styleUrl) {

    startTag(PLACEMARK);
    if (name != null) {
      element(NAME, name);
    }
    if (styleUrl != null) {
      element(STYLE_URL, styleUrl);
    }
    writeLineString(lineString);

    endTag();

  }

  public void writePlacemarkLineString(
    final Polygon polygon,
    final String name,
    final String styleUrl) {

    startTag(PLACEMARK);
    if (name != null) {
      element(NAME, name);
    }
    if (styleUrl != null) {
      element(STYLE_URL, styleUrl);
    }
    final LineString exteriorRing = polygon.getExteriorRing();
    writeLineString(exteriorRing);

    endTag();

  }

  public void writePlacemarkPoint(
    final Envelope envelope,
    final String name,
    final String styleUrl) {

    startTag(PLACEMARK);
    if (name != null) {
      element(NAME, name);
    }
    if (styleUrl != null) {
      element(STYLE_URL, styleUrl);
    }
    startTag(POINT);
    startTag(COORDINATES);
    final Coordinate centre = envelope.centre();
    write(Double.toString(centre.x));
    write(',');
    write(Double.toString(centre.y));

    endTag();
    endTag();
    endTag();
  }

  public void writePlacemarkPolygon(
    final Polygon polygon,
    final String name,
    final String styleUrl) {

    startTag(PLACEMARK);
    if (name != null) {
      element(NAME, name);
    }
    if (styleUrl != null) {
      element(STYLE_URL, styleUrl);
    }
    startTag(POLYGON);
    startTag(OUTER_BOUNDARY_IS);
    startTag(LINEAR_RING);
    final LineString exteriorRing = polygon.getExteriorRing();
    writeLineString(exteriorRing);
    endTag();
    endTag();
    endTag();

    endTag();

  }

  private void writePoint(
    final Point point) {
    startTag(POINT);
    write(point.getCoordinateSequence());
    endTag();
  }

  private void writePolygon(
    final Polygon polygon) {
    startTag(Kml22Constants.POLYGON);
    startTag(Kml22Constants.OUTER_BOUNDARY_IS);
    writeLinearRing(polygon.getExteriorRing());
    endTag();
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      startTag(Kml22Constants.INNER_BOUNDARY_IS);
      final LineString ring = polygon.getInteriorRingN(i);
      writeLinearRing(ring);
      endTag();
    }
    endTag();
  }

  public void writeRegion(
    final Envelope envelope,
    final Integer minLod,
    final Integer maxLod) {
    startTag(REGION);

    startTag(LAT_LON_ALT_BOX);
    element(NORTH, envelope.getMaxY());
    element(SOUTH, envelope.getMinY());
    element(EAST, envelope.getMaxX());
    element(WEST, envelope.getMinX());
    endTag();
    if (minLod != null || maxLod != null) {
      startTag(LOD);
      if (minLod != null) {
        element(MIN_LOD_PIXELS, minLod);
        element(MAX_LOD_PIXELS, maxLod);
      }
      endTag();
    }

    endTag();
  }

  public void writeWmsGroundOverlay(
    final Envelope envelope,
    final String baseUrl,
    final String name) {

    startTag(GROUND_OVERLAY);
    if (name != null) {
      element(NAME, name);
    }
    writeLatLonBox(envelope);
    startTag(ICON);
    final Map<String, String> parameters = Collections.singletonMap("BBOX",
      envelope.getMinX() + "," + envelope.getMinY() + "," + envelope.getMaxX()
        + "," + envelope.getMaxY());
    element(HREF, UrlUtil.getUrl(baseUrl, parameters));

    endTag();
    endTag();

  }

}
