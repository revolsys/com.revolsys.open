package com.revolsys.io.kml;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.util.UrlUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class KmlXmlWriter extends XmlWriter implements Kml22Constants {

  public KmlXmlWriter(final OutputStream out) {
    super(out);
  }

  public KmlXmlWriter(final OutputStream out, final boolean useNamespaces) {
    super(out, useNamespaces);
  }

  public KmlXmlWriter(final OutputStream out, final String charsetName)
    throws UnsupportedEncodingException {
    super(out, charsetName);
  }

  public KmlXmlWriter(final OutputStream out, final String charsetName,
    final boolean useNamespaces) throws UnsupportedEncodingException {
    super(out, charsetName, useNamespaces);
  }

  public KmlXmlWriter(final Writer out) {
    super(out);
  }

  public KmlXmlWriter(final Writer out, final boolean useNamespaces) {
    super(out, useNamespaces);
  }

  public void writeData(final String name, final Object value) {
    if (value != null) {
      startTag(DATA);
      attribute(NAME, name);
      element(VALUE, value);
      endTag();
    }
  }

  public void writeGeometry(final Geometry geometry) {
    KmlWriterUtil.writeGeometry(this, geometry);
  }

  public void writeLatLonBox(final Envelope envelope) {
    startTag(LAT_LON_BOX);
    element(NORTH, envelope.getMaxY());
    element(SOUTH, envelope.getMinY());
    element(WEST, envelope.getMinX());
    element(EAST, envelope.getMaxX());
    endTag();

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
    KmlWriterUtil.writeLineString(this, lineString);

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
    KmlWriterUtil.writeLineString(this, exteriorRing);

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
    startTag(POLYGON);
    startTag(OUTER_BOUNDARY_IS);
    startTag(LINEAR_RING);
    final LineString exteriorRing = polygon.getExteriorRing();
    KmlWriterUtil.writeLineString(this, exteriorRing);
    endTag();
    endTag();
    endTag();

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
