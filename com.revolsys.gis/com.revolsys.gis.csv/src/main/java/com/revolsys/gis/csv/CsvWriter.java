package com.revolsys.gis.csv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.AbstractWriter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

public class CsvWriter extends AbstractWriter<DataObject> {
  private final SimpleDateFormat dateFormat = new SimpleDateFormat(
    "yyyy-MM-dd'T'HH:mm:ss");

  private final WKTWriter geometryWriter = new WKTWriter(3);

  private final DataObjectMetaData metaData;

  private final BufferedWriter out;

  public CsvWriter(
    final DataObjectMetaData type,
    final BufferedWriter out) {
    this.metaData = type;
    this.out = out;
  }

  public CsvWriter(
    final DataObjectMetaData type,
    final OutputStream out) {
    this(type, new OutputStreamWriter(out, CsvConstants.CHARACTER_SET));
  }

  public CsvWriter(
    final DataObjectMetaData type,
    final Writer out) {
    this(type, new BufferedWriter(out));
  }

  public void flush() {
    try {
      out.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void close() {
    try {
      out.close();
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public String toString() {
    return metaData.getName().toString();
  }

  private void newLine()
    throws IOException {
    out.write('\n');
  }

  public void open() {
    writeAttributeHeaders();
  }

  public void write(
    final DataObject object) {
    try {
      final int attributeCount = metaData.getAttributeCount();
      for (int i = 0; i < attributeCount; i++) {
        final Object value = object.getValue(i);
        writeField(value);
        if (i < attributeCount - 1) {
          out.write(',');
        }
      }
      newLine();
    } catch (final IOException e) {
      e.printStackTrace();
    }

  }

  private void writeAttributeHeaders() {
    try {
      final int numAttributes = metaData.getAttributeCount();
      for (int i = 0; i < numAttributes; i++) {
        final String name = metaData.getAttributeName(i);
        writeField(name);
        if (i < numAttributes - 1) {
          out.write(',');
        }
      }
      newLine();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private void writeField(
    final Object value)
    throws IOException {
    writeField(value, "\"");
  }

  @SuppressWarnings("unchecked")
  private void writeField(
    final Object value,
    final String wrapChars)
    throws IOException {
    if (value != null) {
      if (value instanceof Collection) {
        final Collection<Object> collection = (Collection<Object>)value;
        out.write(wrapChars);
        final Iterator<Object> iterator = collection.iterator();
        while (iterator.hasNext()) {
          final Object object = iterator.next();
          writeField(object, wrapChars + wrapChars);
          if (iterator.hasNext()) {
            out.write(',');
          }
        }
        out.write(wrapChars);
      } else if (value instanceof Geometry) {
        final Geometry geometry = (Geometry)value;
        out.write('"');
        geometryWriter.write(geometry, out);
        out.write('"');
      } else if (value instanceof String) {
        final String string = (String)value;
        writeField(string, wrapChars);
      } else if (value instanceof Date) {
        final Date date = (Date)value;
        final String string = dateFormat.format(date);
        writeField(string, wrapChars);
      } else {
        writeField(value.toString(), wrapChars);
      }
    }
  }

  private void writeField(
    final String value)
    throws IOException {
    final String wrapChars = "\"";
    writeField(value, wrapChars);
  }

  private void writeField(
    final String value,
    final String wrapChars)
    throws IOException {
    if (value != null) {
      if (value.length() == 0) {
        out.write(wrapChars);
        out.write(wrapChars);
      } else if (value.indexOf(',') != -1 || value.indexOf('\n') != -1) {
        out.write(wrapChars);
        for (final char c : value.toCharArray()) {
          if (c == '"') {
            out.write(wrapChars);
            out.write(wrapChars);
          } else {
            out.write(c);
          }
        }
        out.write(wrapChars);
      } else {
        out.write(value);
      }
    }
  }
}
