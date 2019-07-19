/*
 * Copyright 2004-2005 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.ui.model.writer;

import java.io.IOException;
import java.io.Writer;

import com.revolsys.ui.model.LabelValueListModel;
import com.revolsys.ui.model.TableModel;

/**
 * @author paustin
 * @version 1.0
 */
public class CsvWriter {
  private boolean firstCol = true;

  private final Writer out;

  public CsvWriter(final Writer out) {
    this.out = out;
  }

  public void print(final LabelValueListModel model) throws IOException {
    for (int row = 0; row < model.getSize(); row++) {
      print(model.getLabel(row));
      print(model.getValue(row));
      println();
    }
  }

  public void print(final String value) throws IOException {
    if (this.firstCol) {
      this.firstCol = false;
    } else {
      this.out.write(",");
    }
    this.out.write("\"");
    if (value != null) {
      this.out.write(value.replaceAll("\"", "\"\""));
    }
    this.out.write("\"");
  }

  public void print(final TableModel model) throws IOException {
    printHeadings(model);
    printRows(model);
  }

  private void printHeadings(final TableModel model) throws IOException {
    for (int col = 0; col < model.getColumnCount(); col++) {
      final String heading = model.getHeaderCell(col);
      print(heading);
    }
    println();
  }

  public void println() throws IOException {
    this.out.write('\n');
    this.firstCol = true;
  }

  private void printRow(final TableModel model, final int row) throws IOException {
    final int colCount = model.getColumnCount();
    for (int col = 0; col < colCount; col++) {
      final String value = model.getBodyCell(row, col);
      print(value);
    }
    println();
  }

  private void printRows(final TableModel model) throws IOException {
    for (int row = 0; row < model.getBodyRowCount(); row++) {
      printRow(model, row);
    }
  }
}
