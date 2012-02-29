package com.revolsys.ui.html.serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.serializer.key.KeySerializer;

public class KeySerializerTableSerializer implements RowsTableSerializer {
  private int colCount = 0;

  private final List<KeySerializer> serializers;

  private int rowCount;

  private final List<Object> rows = new ArrayList<Object>();

  public KeySerializerTableSerializer(final List<KeySerializer> serializers) {
    this.serializers = serializers;
    if (serializers != null) {
      this.colCount = serializers.size();
    }
  }

  public String getBodyCssClass(final int row, final int col) {
    final KeySerializer serializer = getSerializer(col);
     return serializer.getName();
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
      final KeySerializer serializer = getSerializer(col);
      return serializer.getName();
    } else {
      return "";
    }
  }

  public KeySerializer getSerializer(final int col) {
    final KeySerializer serializer = serializers.get(col);
    return serializer;
  }

  public void serializeBodyCell(
    final XmlWriter out,
    final int row,
    final int col) {
    if (col < colCount) {
      final Object object = rows.get(row);
      final KeySerializer serializer = getSerializer(col);
      serializer.serialize(out, object);
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
      final KeySerializer serializer = getSerializer(col);
      out.text(serializer.getLabel());
    } else {
      out.entityRef("nbsp");
    }
  }

  public void setRows(final Collection<? extends Object> rows) {
    this.rows.clear();
    if (rows != null) {
      this.rows.addAll(rows);
    }
    this.rowCount = this.rows.size();
  }

  public List<KeySerializer> getSerializers() {
    return serializers;
  }
}
