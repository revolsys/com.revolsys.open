package com.revolsys.gis.kml.io;

import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.io.AbstractMapWriter;
import com.revolsys.io.FileUtil;
import com.vividsolutions.jts.geom.Geometry;

public class KmlMapWriter extends AbstractMapWriter {

  /** The writer */
  private KmlXmlWriter out;

  public KmlMapWriter(
    final Writer out) {
    this.out = new KmlXmlWriter(out);

    writeHeader();
  }

  /**
   * Closes the underlying reader.
   */
  public void close() {
    if (out != null) {
      try {
        out.endTag();
        out.endTag();
        out.endDocument();
      } finally {
        FileUtil.closeSilent(out);
        out = null;
      }
    }
  }

  public void flush() {
    out.flush();
  }

  public void write(
    final Map<String, ? extends Object> values) {
    out.startTag(Kml22Constants.PLACEMARK);
    Geometry multiGeometry = null;
    out.startTag(Kml22Constants.EXTENDED_DATA);
    for (final Entry<String, ? extends Object> field : values.entrySet()) {
      final String key = field.getKey();
      final Object value = field.getValue();
      if (value instanceof Geometry) {
        final Geometry geometry = (Geometry)value;
        if (multiGeometry == null) {
          multiGeometry = geometry;
        } else {
          multiGeometry = multiGeometry.union(geometry);
        }
      } else {
        out.writeData(key, value);
      }
    }

    out.endTag();
    out.writeGeometry(multiGeometry);
    out.endTag();
  }

  private void writeHeader() {
    out.startDocument();
    out.startTag(Kml22Constants.KML);
    out.startTag(Kml22Constants.DOCUMENT);
  }
}
