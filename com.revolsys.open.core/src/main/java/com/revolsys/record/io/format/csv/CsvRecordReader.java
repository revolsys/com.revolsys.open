package com.revolsys.record.io.format.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.Maps;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.cs.esri.EsriCoordinateSystems;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.PathName;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.property.FieldProperties;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Property;

public class CsvRecordReader extends AbstractIterator<Record>implements RecordReader {

  private final char fieldSeparator;

  private String geometryColumnName;

  private GeometryFactory geometryFactory = GeometryFactory.floating3();

  private DataType geometryType = DataTypes.GEOMETRY;

  private boolean hasPointFields;

  private BufferedReader in;

  private String pointXFieldName;

  private String pointYFieldName;

  private RecordDefinition recordDefinition;

  private RecordFactory recordFactory;

  private Resource resource;

  public CsvRecordReader(final Resource resource) {
    this(resource, ArrayRecord.FACTORY, Csv.FIELD_SEPARATOR);
  }

  public CsvRecordReader(final Resource resource, final char fieldSeparator) {
    this(resource, ArrayRecord.FACTORY, fieldSeparator);
  }

  public CsvRecordReader(final Resource resource, final RecordFactory recordFactory) {
    this(resource, recordFactory, Csv.FIELD_SEPARATOR);
  }

  public CsvRecordReader(final Resource resource, final RecordFactory recordFactory,
    final char fieldSeparator) {
    this.resource = resource;
    this.recordFactory = recordFactory;
    this.fieldSeparator = fieldSeparator;
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  protected void doClose() {
    FileUtil.closeSilent(this.in);
    this.recordFactory = null;
    this.geometryFactory = null;
    this.in = null;
    this.resource = null;
  }

  @Override
  protected void doInit() {
    try {
      this.pointXFieldName = getProperty("pointXFieldName");
      this.pointYFieldName = getProperty("pointYFieldName");
      this.geometryColumnName = getProperty("geometryColumnName", "GEOMETRY");

      this.geometryFactory = GeometryFactory.get(getProperty("geometryFactory"));
      if (this.geometryFactory == null) {
        final Integer geometrySrid = Property.getInteger(this, "geometrySrid");
        if (geometrySrid == null) {
          this.geometryFactory = EsriCoordinateSystems.getGeometryFactory(this.resource);
        } else {
          this.geometryFactory = GeometryFactory.floating3(geometrySrid);
        }
      }
      if (this.geometryFactory == null) {
        this.geometryFactory = GeometryFactory.floating3();
      }
      final DataType geometryType = DataTypes.getType((String)getProperty("geometryType"));
      if (Geometry.class.isAssignableFrom(geometryType.getJavaClass())) {
        this.geometryType = geometryType;
      }

      this.in = new BufferedReader(FileUtil.newUtf8Reader(this.resource.getInputStream()));
      final String[] line = readNextRecord();
      newRecordDefinition(line);
    } catch (final IOException e) {
      Exceptions.log(getClass(), "Unable to open " + this.resource, e);
    } catch (final NoSuchElementException e) {
    }
  }

  @Override
  protected Record getNext() {
    try {
      final String[] record = readNextRecord();
      if (record != null && record.length > 0) {
        return parseRecord(record);
      } else {
        throw new NoSuchElementException();
      }
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Reads the next line from the file.
   *
   * @return the next line from the file without trailing newline
   * @throws IOException if bad things happen during the read
   */
  private String getNextLine() throws IOException {
    final BufferedReader in = this.in;
    if (in == null) {
      throw new NoSuchElementException();
    } else {
      final String nextLine = this.in.readLine();
      if (nextLine == null) {
        throw new NoSuchElementException();
      }
      return nextLine;
    }
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    open();
    return this.recordDefinition;
  }

  private void newRecordDefinition(final String[] fieldNames) throws IOException {
    this.hasPointFields = Property.hasValue(this.pointXFieldName)
      && Property.hasValue(this.pointYFieldName);
    if (this.hasPointFields) {
      this.geometryType = DataTypes.POINT;
    } else {
      this.pointXFieldName = null;
      this.pointYFieldName = null;
    }
    final List<FieldDefinition> fields = new ArrayList<>();
    FieldDefinition geometryField = null;
    for (final String fieldName : fieldNames) {
      if (fieldName != null) {
        DataType type;
        int length = 0;
        boolean isGeometryField = false;
        if (fieldName.equalsIgnoreCase(this.geometryColumnName)) {
          type = this.geometryType;
          isGeometryField = true;
        } else if ("GEOMETRY".equalsIgnoreCase(fieldName)) {
          type = DataTypes.GEOMETRY;
          isGeometryField = true;
        } else if ("GEOMETRYCOLLECTION".equalsIgnoreCase(fieldName)
          || "GEOMETRY_COLLECTION".equalsIgnoreCase(fieldName)) {
          type = DataTypes.GEOMETRY_COLLECTION;
          isGeometryField = true;
        } else if ("POINT".equalsIgnoreCase(fieldName)) {
          type = DataTypes.POINT;
          isGeometryField = true;
        } else if ("MULTI_POINT".equalsIgnoreCase(fieldName)
          || "MULTIPOINT".equalsIgnoreCase(fieldName)) {
          type = DataTypes.MULTI_POINT;
          isGeometryField = true;
        } else if ("LINE_STRING".equalsIgnoreCase(fieldName)
          || "LINESTRING".equalsIgnoreCase(fieldName) || "LINE".equalsIgnoreCase(fieldName)) {
          type = DataTypes.LINE_STRING;
          isGeometryField = true;
        } else if ("MULTI_LINESTRING".equalsIgnoreCase(fieldName)
          || "MULTILINESTRING".equalsIgnoreCase(fieldName)
          || "MULTILINE".equalsIgnoreCase(fieldName) || "MULTI_LINE".equalsIgnoreCase(fieldName)) {
          type = DataTypes.MULTI_LINE_STRING;
          isGeometryField = true;
        } else if ("POLYGON".equalsIgnoreCase(fieldName)) {
          type = DataTypes.POLYGON;
          isGeometryField = true;
        } else if ("MULTI_POLYGON".equalsIgnoreCase(fieldName)
          || "MULTIPOLYGON".equalsIgnoreCase(fieldName)) {
          type = DataTypes.MULTI_POLYGON;
          isGeometryField = true;
        } else {
          type = DataTypes.STRING;
          length = 4000;
        }
        final FieldDefinition field = new FieldDefinition(fieldName, type, length, false);
        if (isGeometryField) {
          geometryField = field;
        }
        fields.add(field);
      }
    }
    if (this.hasPointFields) {
      if (geometryField == null) {
        geometryField = new FieldDefinition(this.geometryColumnName, this.geometryType, true);
        fields.add(geometryField);
      }
    }
    if (geometryField != null) {
      geometryField.setProperty(FieldProperties.GEOMETRY_FACTORY, this.geometryFactory);
    }
    final RecordStoreSchema schema = getProperty("schema");
    String typePath = getProperty("typePath");
    if (!Property.hasValue(typePath)) {
      final Resource resource1 = this.resource;
      typePath = "/" + FileUtil.getBaseName(resource1.getFilename());
      String schemaPath = getProperty("schemaPath");
      if (Property.hasValue(schemaPath)) {
        if (!schemaPath.startsWith("/")) {
          schemaPath = "/" + schemaPath;
        }
        typePath = schemaPath + typePath;
      }
    }
    final PathName pathName = PathName.newPathName(typePath);
    this.recordDefinition = new RecordDefinitionImpl(schema, pathName, getProperties(), fields);
  }

  /**
   * Parses an incoming String and returns an array of elements.
   *
   * @param nextLine the string to parse
   * @return the comma-tokenized list of elements, or null if nextLine is null
   * @throws IOException if bad things happen during the read
   */
  private String[] parseLine(final String nextLine, final boolean readLine) throws IOException {
    String line = nextLine;
    if (line.length() == 0) {
      return new String[0];
    } else {

      final List<String> fields = new ArrayList<String>();
      StringBuilder sb = new StringBuilder();
      boolean inQuotes = false;
      boolean hadQuotes = false;
      do {
        if (inQuotes && readLine) {
          sb.append("\n");
          line = getNextLine();
          if (line == null) {
            break;
          }
        }
        for (int i = 0; i < line.length(); i++) {
          final char c = line.charAt(i);
          if (c == '"') {
            hadQuotes = true;
            if (inQuotes && line.length() > i + 1 && line.charAt(i + 1) == '"') {
              sb.append(line.charAt(i + 1));
              i++;
            } else {
              inQuotes = !inQuotes;
              if (i > 2 && line.charAt(i - 1) != this.fieldSeparator && line.length() > i + 1
                && line.charAt(i + 1) != this.fieldSeparator) {
                sb.append(c);
              }
            }
          } else if (c == this.fieldSeparator && !inQuotes) {
            hadQuotes = false;
            if (hadQuotes || sb.length() > 0) {
              fields.add(sb.toString());
            } else {
              fields.add(null);
            }
            sb = new StringBuilder();
          } else {
            sb.append(c);
          }
        }
      } while (inQuotes);
      if (sb.length() > 0 || fields.size() > 0) {
        if (hadQuotes || sb.length() > 0) {
          fields.add(sb.toString());
        } else {
          fields.add(null);
        }
      }
      return fields.toArray(new String[0]);
    }
  }

  /**
   * Parse a record containing an array of String values into a Record with
   * the strings converted to the objects based on the attribute data type.
   *
   * @param record The record.
   * @return The Record.
   */
  private Record parseRecord(final String[] record) {
    final Record object = this.recordFactory.newRecord(this.recordDefinition);
    for (int i = 0; i < this.recordDefinition.getFieldCount(); i++) {
      String value = null;
      if (i < record.length) {
        value = record[i];
        if (value != null) {
          final DataType dataType = this.recordDefinition.getFieldType(i);
          final Object value1 = value;
          final Object convertedValue = dataType.toObject(value1);
          object.setValue(i, convertedValue);
        }
      }
    }
    if (this.hasPointFields) {
      final Double x = Maps.getDouble(object, this.pointXFieldName);
      final Double y = Maps.getDouble(object, this.pointYFieldName);
      if (x != null && y != null) {
        final Geometry geometry = this.geometryFactory.point(x, y);
        object.setGeometryValue(geometry);
      }
    }
    return object;
  }

  /**
   * Reads the next line from the buffer and converts to a string array.
   *
   * @return a string array with each comma-separated element as a separate
   *         entry.
   * @throws IOException if bad things happen during the read
   */
  private String[] readNextRecord() throws IOException {
    final String nextLine = getNextLine();
    return parseLine(nextLine, true);
  }
}
