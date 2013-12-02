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
package com.revolsys.ui.html.serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.builder.HtmlUiBuilder;

public class HtmlUiBuilderCollectionTableSerializer implements
  RowsTableSerializer {
  private final int colCount;

  private final List keys;

  private final HtmlUiBuilder builder;

  private int rowCount;

  private final List rows = new ArrayList();

  public HtmlUiBuilderCollectionTableSerializer(final HtmlUiBuilder builder,
    final List keys) {
    this.builder = builder;
    this.keys = keys;
    this.colCount = keys.size();
  }

  public String getBodyCssClass(final int row, final int col) {
    return null;
  }

  public int getBodyRowCount() {
    return rowCount;
  }

  public int getColumnCount() {
    return colCount;
  }

  public String getFooterCssClass(final int row, final int col) {
    return null;
  }

  public int getFooterRowCount() {
    return 0;
  }

  public String getHeaderCssClass(final int col) {
    if (col < colCount) {
      return (String)keys.get(col);
    } else {
      return "";
    }
  }

  public void serializeBodyCell(
    final XmlWriter out,
    final int row,
    final int col) {
    if (col < colCount) {
      final Object object = rows.get(row);
      builder.serialize(out, object, (String)keys.get(col));
    } else {
      out.entityRef("nbsp");
    }
  }

  public void serializeFooterCell(
    final XmlWriter out,
    final int row,
    final int col) {
  }

  public void serializeHeaderCell(final XmlWriter out, final int col) {
    if (col < colCount) {
      final String key = (String)keys.get(col);
      out.text(builder.getLabel(key));
    } else {
      out.entityRef("nbsp");
    }
  }

  public void setRows(final Collection rows) {
    this.rows.clear();
    if (rows != null) {
      this.rows.addAll(rows);
    }
    this.rowCount = this.rows.size();
  }
}
