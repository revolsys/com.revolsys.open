package com.revolsys.csv;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

import com.revolsys.io.FileUtil;

public class CsvWriter {

  /** The writer */
  private PrintWriter out;

  /**
   * Constructs CSVReader with supplied separator and quote char.
   * 
   * @param reader The reader to the CSV file.
   * @throws IOException
   */
  public CsvWriter(final Writer out) throws IOException {
    this.out = new PrintWriter(out);
  }

  public void write(List values) {
    write(values.toArray());
  }

  public void write(Object[] values) {
    for (int i = 0; i < values.length; i++) {
      Object value = values[i];
      if (value != null) {
        String string = value.toString().replaceAll("\"", "\"\"");
        out.write('"');
        out.write(string);
        out.write('"');
      }
      if (i < values.length-1) {
        out.write(',');
      }
    }
    out.println();
  }

  /**
   * Closes the underlying reader.
   * 
   * @throws IOException if the close fails
   */
  public void close()  {
    FileUtil.closeSilent(out);
  }
}
