package com.revolsys.io.kml;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.io.StringBufferWriter;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.util.UrlUtil;

public class KmlXmlWriter extends XmlWriter implements Kml22Constants {
  public static void append(final StringBuffer buffer, final Geometry geometry) {
    final KmlXmlWriter writer = new KmlXmlWriter(
      new StringBufferWriter(buffer), false);

    writer.writeGeometry(geometry);
    writer.close();
  }

  public static long getLookAtRange(final BoundingBox boundingBox) {
    if (boundingBox.isEmpty()) {
      return 1000;
    } else {
      final double minX = boundingBox.getMinX();
      final double maxX = boundingBox.getMaxX();
      final double centreX = boundingBox.getCentreX();

      final double minY = boundingBox.getMinY();
      final double maxY = boundingBox.getMaxY();
      final double centreY = boundingBox.getCentreY();

      double maxMetres = 0;

      for (final double y : new double[] {
        minY, centreY, maxY
      }) {
        final double widthMetres = GeographicCoordinateSystem.distanceMetres(
          minX, y, maxX, y);
        if (widthMetres > maxMetres) {
          maxMetres = widthMetres;
        }
      }
      for (final double x : new double[] {
        minX, centreX, maxX
      }) {
        final double heightMetres = GeographicCoordinateSystem.distanceMetres(
          x, minY, x, maxY);
        if (heightMetres > maxMetres) {
          maxMetres = heightMetres;
        }
      }
      if (maxMetres == 0) {
        return 1000;
      } else {
        final double lookAtScale = 1.2;
        final double lookAtRange = maxMetres / 2 / Math.tan(Math.toRadians(25))
          * lookAtScale;
        return (long)Math.ceil(lookAtRange);
      }
    }
  }

  public KmlXmlWriter(final OutputStream out) {
    super(out);
  }

  public KmlXmlWriter(final OutputStream out, final boolean useNamespaces) {
    super(out, useNamespaces);
  }

  public KmlXmlWriter(final Writer out) {
    super(out);
  }

  public KmlXmlWriter(final Writer out, final boolean useNamespaces) {
    super(out, useNamespaces);
  }

  public void write(final CoordinatesList coordinateSequence) {
    startTag(Kml22Constants.COORDINATES);
    final boolean hasZ = coordinateSequence.getDimension() > 2;
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

  public void writeData(final String name, final Object value) {
    if (value != null) {
      startTag(DATA);
      attribute(NAME, name);
      element(VALUE, value);
      endTag();
    }
  }

  public void writeExtendedData(final Map<String, ? extends Object> data) {
    boolean hasValues = false;
    for (final Entry<String, ? extends Object> entry : data.entrySet()) {
      final String attributeName = entry.getKey();
      final Object value = entry.getValue();
      if (!(value instanceof Geometry)) {
        if (value != null) {
          final String stringValue = value.toString();
          if (StringUtils.hasText(stringValue)) {
            if (!hasValues) {
              hasValues = true;
              startTag(EXTENDED_DATA);
            }
            startTag(DATA);
            attribute(NAME, attributeName);
            element(VALUE, value);
            endTag(DATA);
          }
        }
      }
    }
    if (hasValues) {
      endTag(EXTENDED_DATA);
    }
  }

  public void writeGeometry(final Geometry geometry) {
    if (geometry != null) {
      final int numGeometries = geometry.getNumGeometries();
      if (numGeometries > 1) {
        startTag(Kml22Constants.MULTI_GEOMETRY);
        for (int i = 0; i < numGeometries; i++) {
          writeGeometry(geometry.getGeometry(i));
        }
        endTag();
      } else {
        final Geometry geoGraphicsGeom = GeometryProjectionUtil.perform(
          geometry, Kml22Constants.COORDINATE_SYSTEM_ID);
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
        } else if (geoGraphicsGeom instanceof GeometryCollection) {
          final GeometryCollection collection = (GeometryCollection)geoGraphicsGeom;
          writeMultiGeometry(collection);
        }
      }
    }
  }

  public void writeLatLonBox(final Envelope envelope) {
    startTag(LAT_LON_BOX);
    element(NORTH, envelope.getMaxY());
    element(SOUTH, envelope.getMinY());
    element(WEST, envelope.getMinX());
    element(EAST, envelope.getMaxX());
    endTag();

  }

  public void writeLinearRing(final LineString ring) {
    startTag(Kml22Constants.LINEAR_RING);
    final CoordinatesList coordinateSequence = ring.getCoordinatesList();
    write(coordinateSequence);
    endTag();

  }

  public void writeLineString(final LineString line) {
    startTag(Kml22Constants.LINE_STRING);
    final CoordinatesList coordinateSequence = line.getCoordinatesList();
    write(coordinateSequence);
    endTag();
  }

  public void writeMultiGeometry(final GeometryCollection collection) {
    startTag(Kml22Constants.MULTI_GEOMETRY);
    for (int i = 0; i < collection.getNumGeometries(); i++) {
      final Geometry geometry = collection.getGeometry(i);
      writeGeometry(geometry);
    }
    endTag(Kml22Constants.MULTI_GEOMETRY);

  }

  public void writeNetworkLink(final Envelope envelope, final String name,
    final Integer minLod, final Integer maxLod, final String href) {

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

  public void writePlacemark(final Geometry geometry, final String name,
    final String styleUrl) {
    startTag(PLACEMARK);
    if (name != null) {
      element(NAME, name);
    }
    if (styleUrl != null) {
      element(STYLE_URL, styleUrl);
    }
    writeGeometry(geometry);

    endTag();
  }

  public void writePlacemarkLineString(final Envelope envelope,
    final String name, final String styleUrl) {

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

  public void writePlacemarkLineString(final LineString lineString,
    final String name, final String styleUrl) {

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

  public void writePlacemarkLineString(final Polygon polygon,
    final String name, final String styleUrl) {

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

  public void writePlacemarkPoint(final Envelope envelope, final String name,
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

  public void writePlacemarkPolygon(final Polygon polygon, final String name,
    final String styleUrl) {

    startTag(PLACEMARK);
    if (name != null) {
      element(NAME, name);
    }
    if (styleUrl != null) {
      element(STYLE_URL, styleUrl);
    }
    writePolygon(polygon);

    endTag();

  }

  public void writePoint(final Point point) {
    startTag(Kml22Constants.POINT);
    write(point.getCoordinateSequence());
    endTag();
  }

  public void writePolygon(final Polygon polygon) {
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

  public void writeRegion(final Envelope envelope, final Integer minLod,
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

  public void writeWmsGroundOverlay(final Envelope envelope,
    final String baseUrl, final String name) {

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
