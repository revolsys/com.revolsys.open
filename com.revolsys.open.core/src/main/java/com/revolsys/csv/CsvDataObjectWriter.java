package com.revolsys.csv;

import java.io.PrintWriter;
import java.io.Writer;

import com.revolsys.converter.string.StringConverter;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.FileUtil;

public class CsvDataObjectWriter extends AbstractWriter<DataObject> {
  /** The writer */
  private final PrintWriter out;

  private DataObjectMetaData metaData;

  /**
   * Constructs CSVReader with supplied separator and quote char.
   * 
   * @param reader The reader to the CSV file.
   */
  public CsvDataObjectWriter(DataObjectMetaData metaData, final Writer out) {
    this.metaData = metaData;
    this.out = new PrintWriter(out);
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      if (i > 0) {
        this.out.print(',');
      }
      String name = metaData.getAttributeName(i);
      string(name);
    }
    this.out.println();
  }

  /**
   * Closes the underlying reader.
   */
  public void close() {
    FileUtil.closeSilent(out);
  }

  public void flush() {
    out.flush();
  }

  public void write(final DataObject object) {
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      if (i > 0) {
        out.print(',');
      }
      final Object value = object.getValue(i);
      if (value != null) {
        String name = metaData.getAttributeName(i);
        DataType dataType = metaData.getAttributeType(name);

        @SuppressWarnings("unchecked")
        Class<Object> dataTypeClass = (Class<Object>)dataType.getJavaClass();
        StringConverter<Object> converter = StringConverterRegistry.INSTANCE.getConverter(dataTypeClass);
        if (converter == null) {
          string(value);
        } else {
          String stringValue = converter.toString(value);
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

  private void string(final Object value) {
    final String string = value.toString().replaceAll("\"", "\"\"");
    out.print('"');
    out.print(string);
    out.print('"');
  }

}
