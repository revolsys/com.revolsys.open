package com.revolsys.io.ecsv;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.util.Map.Entry;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.io.ecsv.type.EcsvFieldType;
import com.revolsys.io.ecsv.type.EcsvFieldTypeRegistry;
import com.revolsys.io.ecsv.type.StringFieldType;

public class EcsvDataObjectWriter extends AbstractWriter<DataObject> implements
  EcsvConstants {

  private final DataObjectMetaData metaData;

  private boolean open;

  private final PrintWriter out;

  public EcsvDataObjectWriter(final DataObjectMetaData metaData,
    final java.io.Writer out) {
    this.metaData = metaData;
    this.out = new PrintWriter(new BufferedWriter(out));

  }

  @Override
  public void close() {
    out.print(MULTI_LINE_LIST_END);
    out.close();
  }

  @Override
  public void flush() {
    out.flush();
  }

  private void newLine() {
    out.print(RECORD_SEPARATOR);
  }

  public void open() {
    if (!open) {
      open = true;
      writeHeader();
    }
  }

  @Override
  public String toString() {
    return metaData.getPath().toString();
  }

  public void write(final DataObject object) {
    open();
    final int attributeCount = metaData.getAttributeCount();
    for (int i = 0; i < attributeCount; i++) {
      final Object value = object.getValue(i);
      final DataType dataType = metaData.getAttributeType(i);
      writeField(dataType, value);
      if (i < attributeCount - 1) {
        out.print(FIELD_SEPARATOR);
      }
    }
    newLine();
  }

  private void writeAttributeHeaders(final DataObjectMetaData metaData) {
    writeMapStart(ATTRIBUTE_PROPERTIES);
    writeMultiLineListStart(EcsvProperties.ATTRIBUTE_NAME, DataTypes.STRING,
      COLLECTION_START);
    final int numAttributes = metaData.getAttributeCount();
    for (int i = 0; i < numAttributes; i++) {
      final String name = metaData.getAttributeName(i);
      writeField(DataTypes.STRING, name);
      if (i < numAttributes - 1) {
        out.print(FIELD_SEPARATOR);
      }
    }
    newLine();
    writeMultiLineEnd(COLLECTION_END);

    writeMultiLineListStart(EcsvProperties.ATTRIBUTE_TYPE, DataTypes.QNAME,
      COLLECTION_START);
    for (int i = 0; i < numAttributes; i++) {
      final DataType type = metaData.getAttributeType(i);
      writeField(DataTypes.QNAME, type.getName());
      if (i < numAttributes - 1) {
        out.print(FIELD_SEPARATOR);
      }
    }
    newLine();
    writeMultiLineEnd(COLLECTION_END);

    writeMultiLineListStart(EcsvProperties.ATTRIBUTE_LENGTH, DataTypes.INT,
      COLLECTION_START);
    for (int i = 0; i < numAttributes; i++) {
      final Integer length = metaData.getAttributeLength(i);
      writeField(DataTypes.INTEGER, length);
      if (i < numAttributes - 1) {
        out.print(FIELD_SEPARATOR);
      }
    }
    newLine();
    writeMultiLineEnd(COLLECTION_END);

    writeMultiLineListStart(EcsvProperties.ATTRIBUTE_SCALE, DataTypes.INT,
      COLLECTION_START);
    for (int i = 0; i < numAttributes; i++) {
      final Integer scale = metaData.getAttributeScale(i);
      writeField(DataTypes.INTEGER, scale);
      if (i < numAttributes - 1) {
        out.print(FIELD_SEPARATOR);
      }
    }
    newLine();
    writeMultiLineEnd(COLLECTION_END);

    writeMultiLineListStart(EcsvProperties.ATTRIBUTE_REQUIRED,
      DataTypes.BOOLEAN, COLLECTION_START);
    for (int i = 0; i < numAttributes; i++) {
      final Boolean required = metaData.isAttributeRequired(i);
      writeField(DataTypes.BOOLEAN, required);
      if (i < numAttributes - 1) {
        out.print(FIELD_SEPARATOR);
      }
    }
    newLine();
    writeMultiLineEnd(COLLECTION_END);

    writeMapEnd();
  }

  private void writeDataStart(final DataObjectMetaData metaData) {
    writeMultiLineListStart(RECORDS, metaData.getPath(), MULTI_LINE_LIST_START);
  }

  private void writeField(final DataType dataType, final Object value) {
    if (value != null) {
      final EcsvFieldType fieldType = EcsvFieldTypeRegistry.INSTANCE.getFieldType(dataType);
      if (fieldType == null) {
        StringFieldType.writeQuotedString(out, value);
      } else {
        fieldType.writeValue(out, value);
      }
    }
  }

  private void writeFileProperties() {
    for (final Entry<String, Object> property : getProperties().entrySet()) {
      final String name = property.getKey();
      final Object value = property.getValue();
      final String defaultPrefix = "com.revolsys.io.";
      if (name.startsWith(defaultPrefix)) {
        writeProperty(name.substring(defaultPrefix.length()), value);
      } else if (!name.startsWith("java:")) {
        writeProperty(name, value);
      } else if (name.equals(IoConstants.GEOMETRY_FACTORY)) {
        writeProperty(name, value);
      }
    }
  }

  private void writeHeader() {
    writeProperty(ECSV_VERSION, VERSION_1_0_0_DRAFT1);

    writeFileProperties();
    writeTypeDefinition(metaData);
    writeDataStart(metaData);
  }

  private void writeMapEnd() {
    out.write(MAP_END);
    newLine();
  }

  private void writeMapStart(final String propertyName) {
    writeMultiLineStart(propertyName, MAP_TYPE, MAP_START);
  }

  private void writeMultiLineEnd(final String collectionStart) {
    out.write(collectionStart);
    newLine();
  }

  private void writeMultiLineListStart(
    final String propertyName,
    final DataType dataType,
    final String collectionStart) {
    final String name = dataType.getName();
    writeMultiLineListStart(propertyName, name, collectionStart);
  }

  private void writeMultiLineListStart(
    final String propertyName,
    final String path,
    final String collectionStart) {
    final String type = LIST_TYPE + TYPE_PARAMETER_START + path
      + TYPE_PARAMETER_END;
    writeMultiLineStart(propertyName, type, collectionStart);
  }

  private void writeMultiLineStart(
    final String propertyName,
    final String typePath,
    final String collectionStart) {
    StringFieldType.writeQuotedString(out, propertyName);
    out.print(FIELD_SEPARATOR);
    StringFieldType.writeQuotedString(out, typePath);
    out.print(FIELD_SEPARATOR);
    out.print(collectionStart);
    newLine();
  }

  private void writeProperty(final String name, final Object value) {
    if (value != null) {
      final DataType dataType = DataTypes.getType(value.getClass());
      EcsvFieldType fieldType = EcsvFieldTypeRegistry.INSTANCE.getFieldType(dataType);
      if (fieldType == null) {
        fieldType = new StringFieldType();
      }
      final String type = fieldType.getTypeName();
      StringFieldType.writeQuotedString(out, name);
      out.write(FIELD_SEPARATOR);
      writeField(DataTypes.QNAME, type);
      out.write(FIELD_SEPARATOR);
      fieldType.writeValue(out, value);
      newLine();
    }
  }

  private void writeProperty(
    final String name,
    final String type,
    final Object value) {
    if (value != null) {
      StringFieldType.writeQuotedString(out, name);
      out.write(FIELD_SEPARATOR);
      final DataType dataType = DataTypes.getType(value.getClass());
      EcsvFieldType fieldType = EcsvFieldTypeRegistry.INSTANCE.getFieldType(dataType);
      if (fieldType == null) {
        fieldType = new StringFieldType();
      }
      writeField(DataTypes.QNAME, type);
      out.write(FIELD_SEPARATOR);
      fieldType.writeValue(out, value);
      newLine();
    }
  }

  private void writeTypeDefinition(final DataObjectMetaData metaData) {

    writeMapStart(TYPE_DEFINITION);

    writeProperty(EcsvProperties.NAME, metaData.getPath());
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    if (geometryAttribute != null) {
      final GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
      writeProperty(EcsvProperties.GEOMETRY_FACTORY_PROPERTY, geometryFactory);
    }

    writeAttributeHeaders(metaData);

    writeMapEnd();
  }
}
