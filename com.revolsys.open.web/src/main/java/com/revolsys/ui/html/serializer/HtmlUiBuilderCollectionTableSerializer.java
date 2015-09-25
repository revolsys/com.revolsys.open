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

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.builder.HtmlUiBuilder;

public class HtmlUiBuilderCollectionTableSerializer implements RowsTableSerializer {
  private final HtmlUiBuilder builder;

  private final int colCount;

  private final List keys;

  private int rowCount;

  private final List rows = new ArrayList();

  public HtmlUiBuilderCollectionTableSerializer(final HtmlUiBuilder builder, final List keys) {
    this.builder = builder;
    this.keys = keys;
    this.colCount = keys.size();
  }

  @Override
  public String getBodyCssClass(final int row, final int col) {
    return null;
  }

  @Override
  public int getBodyRowCount() {
    return this.rowCount;
  }

  @Override
  public int getColumnCount() {
    return this.colCount;
  }

  @Override
  public String getFooterCssClass(final int row, final int col) {
    return null;
  }

  @Override
  public int getFooterRowCount() {
    return 0;
  }

  @Override
  public String getHeaderCssClass(final int col) {
    if (col < this.colCount) {
      return (String)this.keys.get(col);
    } else {
      return "";
    }
  }

  @Override
  public void serializeBodyCell(final XmlWriter out, final int row, final int col) {
    if (col < this.colCount) {
      final Object object = this.rows.get(row);
      this.builder.serialize(out, object, (String)this.keys.get(col));
    } else {
      out.entityRef("nbsp");
    }
  }

  @Override
  public void serializeFooterCell(final XmlWriter out, final int row, final int col) {
  }

  @Override
  public void serializeHeaderCell(final XmlWriter out, final int col) {
    if (col < this.colCount) {
      final String key = (String)this.keys.get(col);
      out.text(this.builder.getLabel(key));
    } else {
      out.entityRef("nbsp");
    }
  }

  @Override
  public void setRows(final Collection rows) {
    this.rows.clear();
    if (rows != null) {
      this.rows.addAll(rows);
    }
    this.rowCount = this.rows.size();
  }
}
