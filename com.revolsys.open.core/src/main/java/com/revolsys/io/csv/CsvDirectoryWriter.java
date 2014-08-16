package com.revolsys.io.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.jts.geom.Geometry;

public class CsvDirectoryWriter extends AbstractRecordWriter {
  private File directory;

  private final Map<RecordDefinition, CsvRecordWriter> writers = new HashMap<RecordDefinition, CsvRecordWriter>();

  private final Map<String, RecordDefinition> recordDefinitionMap = new HashMap<>();

  private boolean ewkt = true;

  public CsvDirectoryWriter() {
  }

  public CsvDirectoryWriter(final File baseDirectory) {
    setDirectory(baseDirectory);
  }

  @Override
  public void close() {
    for (final CsvRecordWriter writer : this.writers.values()) {
      FileUtil.closeSilent(writer);
    }
    this.writers.clear();
    this.recordDefinitionMap.clear();
  }

  @Override
  public void flush() {
    for (final CsvRecordWriter writer : this.writers.values()) {
      writer.flush();
    }
  }

  public File getDirectory() {
    return this.directory;
  }

  public RecordDefinition getRecordDefinition(final String path) {
    return this.recordDefinitionMap.get(path);
  }

  private CsvRecordWriter getWriter(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    CsvRecordWriter writer = this.writers.get(recordDefinition);
    if (writer == null) {
      try {
        final String path = recordDefinition.getPath();
        final File file = new File(this.directory, path.toString() + ".csv");
        file.getParentFile().mkdirs();
        writer = new CsvRecordWriter(recordDefinition, new FileWriter(file),
          this.ewkt);
        final Geometry geometry = record.getGeometryValue();
        if (geometry != null) {
          writer.setProperty(IoConstants.GEOMETRY_FACTORY,
            geometry.getGeometryFactory());
        }
        this.writers.put(recordDefinition, writer);
        this.recordDefinitionMap.put(path, recordDefinition);
        final File vrtFile = new File(this.directory, path.toString() + ".vrt");
        try (
          XmlWriter vrtWriter = new XmlWriter(new FileWriter(vrtFile))) {
          vrtWriter.setIndent(true);
          vrtWriter.startDocument("UTF-8", "1.0");
          vrtWriter.startTag("OGRVRTDataSource");
          vrtWriter.startTag("OGRVRTLayer");
          final String typeName = recordDefinition.getTypeName();
          vrtWriter.attribute("name", typeName);
          vrtWriter.startTag("SrcDataSource");
          vrtWriter.attribute("relativeToVRT", "1");
          vrtWriter.text(typeName + ".csv");
          vrtWriter.endTag("SrcDataSource");

          vrtWriter.element(new QName("SrcLayer"), typeName);

          for (final Attribute attribute : recordDefinition.getAttributes()) {
            final String fieldName = attribute.getName();
            final DataType fieldType = attribute.getType();
            final Class<?> typeClass = attribute.getTypeClass();
            if (Geometry.class.isAssignableFrom(typeClass)) {

            } else {
              vrtWriter.startTag("Field");
              vrtWriter.attribute("name", fieldName);
              String type = "String";
              if (Arrays.asList(DataTypes.BYTE, DataTypes.SHORT, DataTypes.INT,
                DataTypes.LONG, DataTypes.INTEGER).contains(fieldType)) {
                type = "Integer";
              } else if (Arrays.asList(DataTypes.FLOAT, DataTypes.DOUBLE,
                DataTypes.DECIMAL).contains(fieldType)) {
                type = "Real";
              } else if (DataTypes.DATE.equals(type)) {
                type = "Date";
              } else if (DataTypes.DATE_TIME.equals(type)) {
                type = "DateTime";
              } else {
                type = "String";
              }
              vrtWriter.attribute("type", type);
              final int length = attribute.getLength();
              if (length > 0) {
                vrtWriter.attribute("width", length);
              }
              final int scale = attribute.getScale();
              if (scale > 0) {
                vrtWriter.attribute("scale", scale);
              }
              vrtWriter.attribute("src", fieldName);
              vrtWriter.endTag("Field");
            }
          }
          vrtWriter.endTag("OGRVRTLayer");
          vrtWriter.endTag("OGRVRTDataSource");
          vrtWriter.endDocument();
        }

      } catch (final IOException e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
    }
    return writer;
  }

  public boolean isEwkt() {
    return this.ewkt;
  }

  public void setDirectory(final File baseDirectory) {
    this.directory = baseDirectory;
    baseDirectory.mkdirs();
  }

  public void setEwkt(final boolean ewkt) {
    this.ewkt = ewkt;
  }

  @Override
  public String toString() {
    return this.directory.getAbsolutePath();
  }

  @Override
  public void write(final Record object) {
    final CsvRecordWriter writer = getWriter(object);
    writer.write(object);
  }

}
