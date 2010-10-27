package com.revolsys.gis.ecsv.io;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.ecsv.io.type.EcsvGeometryFieldType;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.IoConstants;
import com.vividsolutions.jts.geom.Geometry;

public class EcsvDataObjectWriter extends AbstractWriter<DataObject> {
  private static final NumberFormat NUMBER_FORMAT = new DecimalFormat(
    "#.#########################");

  private final List<QName> attributeHeaderTypes = Arrays.asList(new QName[] {
    EcsvConstants.ATTRIBUTE_NAME, EcsvConstants.ATTRIBUTE_TYPE,
    EcsvConstants.ATTRIBUTE_LENGTH, EcsvConstants.ATTRIBUTE_SCALE,
    EcsvConstants.ATTRIBUTE_REQUIRED
  });

  private final SimpleDateFormat dateFormat = new SimpleDateFormat(
    "yyyy-MM-dd'T'HH:mm:ss");

  private final EcsvGeometryFieldType geometryFieldType = new EcsvGeometryFieldType();

  private final DataObjectMetaData metaData;

  private boolean open;

  private final PrintWriter out;

  public EcsvDataObjectWriter(
    final DataObjectMetaData metaData,
    final java.io.Writer out) {
    this.metaData = metaData;
    this.out = new PrintWriter(new BufferedWriter(out));
    setProperty(EcsvConstants.TYPE_NAME, metaData.getName());
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    if (geometryAttribute != null) {
      final Integer srid = geometryAttribute.getProperty(AttributeProperties.SRID);
      if (srid != null) {
        setProperty(IoConstants.GEOMETRY_FACTORY,
          GeometryFactory.getFactory(srid));
      }
    }

  }

  @Override
  public void close() {
    out.close();
  }

  @Override
  public void flush() {
    out.flush();
  }

  private void newLine() {
    out.print('\n');
  }

  @Override
  public String toString() {
    return metaData.getName().toString();
  }

  public void write(
    final DataObject object) {
    if (!open) {
      open = true;
      writeFileProperties();
      writeAttributeHeaders();
    }
    final int attributeCount = metaData.getAttributeCount();
    for (int i = 0; i < attributeCount; i++) {
      final Object value = object.getValue(i);
      writeField(value);
      if (i < attributeCount - 1) {
        out.print(',');
      }
    }
    newLine();
  }

  private void writeAttributeHeaders() {
    final int numAttributes = metaData.getAttributeCount();
    for (int i = 0; i < numAttributes; i++) {
      final String name = metaData.getAttributeName(i);
      writeField(name);
      if (i < numAttributes - 1) {
        out.print(',');
      }
    }
    newLine();
    for (int i = 0; i < numAttributes; i++) {
      final DataType type = metaData.getAttributeType(i);
      writeField(type.getName());
      if (i < numAttributes - 1) {
        out.print(',');
      }
    }
    newLine();
    for (int i = 0; i < numAttributes; i++) {
      final Integer length = metaData.getAttributeLength(i);
      writeField(length);
      if (i < numAttributes - 1) {
        out.print(',');
      }
    }
    newLine();
    for (int i = 0; i < numAttributes; i++) {
      final Integer scale = metaData.getAttributeScale(i);
      writeField(scale);
      if (i < numAttributes - 1) {
        out.print(',');
      }
    }
    newLine();
    for (int i = 0; i < numAttributes; i++) {
      final Boolean required = metaData.isAttributeRequired(i);
      writeField(required);
      if (i < numAttributes - 1) {
        out.print(',');
      }
    }
    newLine();
    newLine();
  }

  private void writeField(
    final Object value) {
    writeField(value, "\"");
  }

  @SuppressWarnings("unchecked")
  private void writeField(
    final Object value,
    final String wrapChars) {
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
        geometryFieldType.write(out, geometry);
        out.write('"');
      } else if (value instanceof String) {
        final String string = (String)value;
        writeField(string, wrapChars);
      } else if (value instanceof Date) {
        final Date date = (Date)value;
        final String string = dateFormat.format(date);
        writeField(string, wrapChars);
      } else if (value instanceof Number) {
        writeField(NUMBER_FORMAT.format(value), wrapChars);
      } else {
        writeField(value.toString(), wrapChars);
      }
    }
  }

  private void writeField(
    final String value) {
    final String wrapChars = "\"";
    writeField(value, wrapChars);
  }

  private void writeField(
    final String value,
    final String wrapChars) {
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
    for (final Entry<String, Object> property : getProperties().entrySet()) {
      final String name = property.getKey();
      final Object value = property.getValue();
      final String defaultPrefix = "com.revolsys.io.";
      if (name.startsWith(defaultPrefix)) {
        writeFileProperty(name.substring(defaultPrefix.length()), value);
      } else if (!name.startsWith("java:")) {
        writeFileProperty(name, value);
      }
    }
    writeFileProperty(EcsvConstants.ATTRIBUTE_HEADER_TYPES,
      attributeHeaderTypes);
    newLine();
  }

  private void writeFileProperty(
    final String name,
    final Object value) {
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
