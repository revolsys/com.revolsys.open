package com.revolsys.io.csv;

import java.io.PrintWriter;
import java.io.Writer;

import com.revolsys.converter.string.StringConverter;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.FileUtil;

public class CsvDataObjectWriter extends AbstractWriter<Record> {
  private final char fieldSeparator = ',';

  /** The writer */
  private final PrintWriter out;

  private final RecordDefinition metaData;

  private final boolean useQuotes;

  public CsvDataObjectWriter(final RecordDefinition metaData, final Writer out) {
    this(metaData, out, CsvConstants.FIELD_SEPARATOR, true);

  }

  public CsvDataObjectWriter(final RecordDefinition metaData,
    final Writer out, final char fieldSeparator, final boolean useQuotes) {
    this.metaData = metaData;
    this.out = new PrintWriter(out);
    this.useQuotes = useQuotes;
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      if (i > 0) {
        this.out.print(fieldSeparator);
      }
      final String name = metaData.getAttributeName(i);
      string(name);
    }
    this.out.println();
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  public void close() {
    FileUtil.closeSilent(out);
  }

  @Override
  public void flush() {
    out.flush();
  }

  private void string(final Object value) {
    String string = value.toString();
    if (useQuotes) {
      string = string.replaceAll("\"", "\"\"");
      out.print('"');
      out.print(string);
      out.print('"');
    } else {
      out.print(string);
    }
  }

  @Override
  public void write(final Record object) {
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      if (i > 0) {
        out.print(fieldSeparator);
      }
      final Object value = object.getValue(i);
      if (value != null) {
        final String name = metaData.getAttributeName(i);
        final DataType dataType = metaData.getAttributeType(name);

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
            out.print(stringValue);
          }
        }
      }
    }
    out.println();
  }

}
