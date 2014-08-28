package com.revolsys.io.csv;

import java.io.PrintWriter;
import java.io.Writer;

import com.revolsys.converter.string.StringConverter;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.wkt.EWktWriter;
import com.revolsys.jts.geom.Geometry;

public class CsvRecordWriter extends AbstractRecordWriter {
  private final char fieldSeparator;

  /** The writer */
  private final PrintWriter out;

  private final RecordDefinition recordDefinition;

  private final boolean useQuotes;

  private final boolean ewkt;

  public CsvRecordWriter(final RecordDefinition recordDefinition,
    final Writer out, final boolean ewkt) {
    this(recordDefinition, out, CsvConstants.FIELD_SEPARATOR, true, ewkt);

  }

  public CsvRecordWriter(final RecordDefinition recordDefinition,
    final Writer out, final char fieldSeparator, final boolean useQuotes,
    final boolean ewkt) {
    this.recordDefinition = recordDefinition;
    this.out = new PrintWriter(out);
    this.fieldSeparator = fieldSeparator;
    this.useQuotes = useQuotes;
    this.ewkt = ewkt;
    for (int i = 0; i < recordDefinition.getAttributeCount(); i++) {
      if (i > 0) {
        this.out.print(fieldSeparator);
      }
      final String name = recordDefinition.getAttributeName(i);
      string(name);
    }
    this.out.println();
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
    this.out.flush();
  }

  private void string(final Object value) {
    String string = value.toString();
    if (this.useQuotes) {
      string = string.replaceAll("\"", "\"\"");
      this.out.print('"');
      this.out.print(string);
      this.out.print('"');
    } else {
      this.out.print(string);
    }
  }

  @Override
  public void write(final Record object) {
    for (int i = 0; i < this.recordDefinition.getAttributeCount(); i++) {
      if (i > 0) {
        this.out.print(this.fieldSeparator);
      }
      final Object value = object.getValue(i);
      if (value instanceof Geometry) {
        final Geometry geometry = (Geometry)value;
        final String text = EWktWriter.toString(geometry, this.ewkt);
        string(text);
      } else if (value != null) {
        final String name = this.recordDefinition.getAttributeName(i);
        final DataType dataType = this.recordDefinition.getAttributeType(name);

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
            this.out.print(stringValue);
          }
        }
      }
    }
    this.out.println();
  }

}
