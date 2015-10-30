package com.revolsys.record.io.format.csv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

import com.revolsys.converter.string.StringConverter;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.datatype.DataType;
import com.revolsys.geometry.cs.esri.EsriCoordinateSystems;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Paths;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.wkt.EWktWriter;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.WrappedException;

public class CsvRecordWriter extends AbstractRecordWriter {
  private final boolean ewkt;

  private final char fieldSeparator;

  /** The writer */
  private final Writer out;

  private final RecordDefinition recordDefinition;

  private final boolean useQuotes;

  public CsvRecordWriter(final RecordDefinition recordDefinition, final Path path,
    final char fieldSeparator, final boolean useQuotes, final boolean ewkt) {
    this(recordDefinition, Paths.newWriter(path), fieldSeparator, useQuotes, ewkt);
    final GeometryFactory geometryFactory = recordDefinition.getGeometryFactory();
    EsriCoordinateSystems.writePrjFile(path, geometryFactory);
  }

  public CsvRecordWriter(final RecordDefinition recordDefinition, final Writer out,
    final char fieldSeparator, final boolean useQuotes, final boolean ewkt) {
    try {
      this.recordDefinition = recordDefinition;
      this.out = new BufferedWriter(out);
      this.fieldSeparator = fieldSeparator;
      this.useQuotes = useQuotes;
      this.ewkt = ewkt;
      for (int i = 0; i < recordDefinition.getFieldCount(); i++) {
        if (i > 0) {
          this.out.write(fieldSeparator);
        }
        final String name = recordDefinition.getFieldName(i);
        string(name);
      }
      this.out.write('\n');
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  public void close() {
    FileUtil.closeSilent(this.out);
  }

  @Override
  public void flush() {
    try {
      this.out.flush();
    } catch (final IOException e) {
      throw new WrappedException(e);
    }

  }

  private void string(final Object value) throws IOException {
    final Writer out = this.out;
    final String string = value.toString();
    if (this.useQuotes) {
      out.write('"');
      for (int i = 0; i < string.length(); i++) {
        final char c = string.charAt(i);
        if (c == '"') {
          out.write('"');
        }
        out.write(c);
      }
      out.write('"');
    } else {
      out.write(string, 0, string.length());
    }
  }

  @Override
  public void write(final Record object) {
    try {
      final Writer out = this.out;
      final RecordDefinition recordDefinition = this.recordDefinition;
      final int fieldCount = recordDefinition.getFieldCount();
      final char fieldSeparator = this.fieldSeparator;
      for (int i = 0; i < fieldCount; i++) {
        if (i > 0) {
          out.write(fieldSeparator);
        }
        final Object value = object.getValue(i);
        if (value instanceof Geometry) {
          final Geometry geometry = (Geometry)value;
          final String text = EWktWriter.toString(geometry, this.ewkt);
          string(text);
        } else if (value != null) {
          final String name = recordDefinition.getFieldName(i);
          final DataType dataType = recordDefinition.getFieldType(name);

          @SuppressWarnings("unchecked")
          final Class<Object> dataTypeClass = (Class<Object>)dataType.getJavaClass();
          final StringConverter<Object> converter = StringConverterRegistry.getInstance()
            .getConverter(dataTypeClass);
          if (converter == null) {
            string(value);
          } else {
            final String stringValue = converter.toString(value);
            if (converter.requiresQuotes()) {
              string(stringValue);
            } else {
              out.write(stringValue, 0, stringValue.length());
            }
          }
        }
      }
      out.write('\n');
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }

}
