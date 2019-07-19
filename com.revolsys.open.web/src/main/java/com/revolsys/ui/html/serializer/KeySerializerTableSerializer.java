package com.revolsys.ui.html.serializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.serializer.key.KeySerializer;
import com.revolsys.util.Property;

public class KeySerializerTableSerializer implements RowsTableSerializer {
  private int colCount = 0;

  private int rowCount;

  private final List<Object> rows = new ArrayList<>();

  private final List<KeySerializer> serializers;

  public KeySerializerTableSerializer(final List<KeySerializer> serializers) {
    this.serializers = serializers;
    if (serializers != null) {
      this.colCount = serializers.size();
    }
  }

  public KeySerializerTableSerializer(final List<KeySerializer> serializers,
    final Collection<? extends Object> rows) {
    this(serializers);
    setRows(rows);
  }

  @Override
  public String getBodyCssClass(final int row, final int col) {
    final KeySerializer serializer = getSerializer(col);
    if (serializer != null) {
      final String name = serializer.getName();
      if (Property.hasValue(name)) {
        return name.replaceAll("\\.", "_");
      }
    }
    return "";
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
    return "";
  }

  @Override
  public int getFooterRowCount() {
    return 0;
  }

  @Override
  public String getHeaderCssClass(final int col) {
    if (col < this.colCount) {
      final KeySerializer serializer = getSerializer(col);
      final String name = serializer.getName();
      if (Property.hasValue(name)) {
        return name.replaceAll("\\.", "_");
      }
    }
    return "";
  }

  public KeySerializer getSerializer(final int col) {
    final KeySerializer serializer = this.serializers.get(col);
    return serializer;
  }

  public List<KeySerializer> getSerializers() {
    return this.serializers;
  }

  @Override
  public void serializeBodyCell(final XmlWriter out, final int row, final int col) {
    if (col < this.colCount) {
      final Object object = this.rows.get(row);
      final KeySerializer serializer = getSerializer(col);
      serializer.serialize(out, object);
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
      final KeySerializer serializer = getSerializer(col);
      out.text(serializer.getLabel());
    } else {
      out.entityRef("nbsp");
    }
  }

  @Override
  public void setRows(final Collection<? extends Object> rows) {
    this.rows.clear();
    if (rows != null) {
      this.rows.addAll(rows);
    }
    this.rowCount = this.rows.size();
  }
}
