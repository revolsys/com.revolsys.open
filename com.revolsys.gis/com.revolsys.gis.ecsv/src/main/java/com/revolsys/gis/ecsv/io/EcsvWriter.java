package com.revolsys.gis.ecsv.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.AbstractWriter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

public class EcsvWriter extends AbstractWriter<DataObject> {
  private final List<QName> attributeHeaderTypes = Arrays.asList(new QName[] {
    EcsvConstants.ATTRIBUTE_NAME, EcsvConstants.ATTRIBUTE_TYPE,
    EcsvConstants.ATTRIBUTE_LENGTH, EcsvConstants.ATTRIBUTE_SCALE,
    EcsvConstants.ATTRIBUTE_REQUIRED
  });

  private final SimpleDateFormat dateFormat = new SimpleDateFormat(
    "yyyy-MM-dd'T'HH:mm:ss");

  private final WKTWriter geometryWriter = new WKTWriter(3);

  private final DataObjectMetaData metaData;

  private final BufferedWriter out;

  private boolean open;

  public EcsvWriter(
    final DataObjectMetaData metaData,
    final BufferedWriter out) {
    this.metaData = metaData;
    this.out = out;
    setProperty(EcsvConstants.TYPE_NAME, metaData.getName());
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    if (geometryAttribute != null) {
      final Integer srid = geometryAttribute.getProperty(AttributeProperties.SRID);
      setProperty(EcsvConstants.SRID, srid);
    }

  }

  public EcsvWriter(
    final DataObjectMetaData type,
    final OutputStream out) {
    this(type, new OutputStreamWriter(out, EcsvConstants.CHARACTER_SET));
  }

  public EcsvWriter(
    final DataObjectMetaData type,
    final java.io.Writer out) {
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

  public void write(
    final DataObject object) {
    if (!open) {
      open = true;
      writeFileProperties();
      writeAttributeHeaders();
    }
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
      // TODO Auto-generated catch block
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
      for (int i = 0; i < numAttributes; i++) {
        final DataType type = metaData.getAttributeType(i);
        writeField(type.getName());
        if (i < numAttributes - 1) {
          out.write(',');
        }
      }
      newLine();
      for (int i = 0; i < numAttributes; i++) {
        final Integer length = metaData.getAttributeLength(i);
        writeField(length);
        if (i < numAttributes - 1) {
          out.write(',');
        }
      }
      newLine();
      for (int i = 0; i < numAttributes; i++) {
        final Integer scale = metaData.getAttributeScale(i);
        writeField(scale);
        if (i < numAttributes - 1) {
          out.write(',');
        }
      }
      newLine();
      for (int i = 0; i < numAttributes; i++) {
        final Boolean required = metaData.isAttributeRequired(i);
        writeField(required);
        if (i < numAttributes - 1) {
          out.write(',');
        }
      }
      newLine();
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

  private void writeFileProperties() {
    try {
      for (final Entry<QName, Object> property : getProperties().entrySet()) {
        final QName name = property.getKey();
        final Object value = property.getValue();
        writeFileProperty(name, value);
      }
      writeFileProperty(EcsvConstants.ATTRIBUTE_HEADER_TYPES,
        attributeHeaderTypes);
      newLine();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private void writeFileProperty(
    final QName name,
    final Object value)
    throws IOException {
    if (value != null) {
      final DataType dataType = DataTypes.getType(value.getClass());
      final QName type = dataType.getName();
      writeField(name);
      out.write(',');
      writeField(type);
      out.write(',');
      writeField(value);
      newLine();
    }
  }

}
