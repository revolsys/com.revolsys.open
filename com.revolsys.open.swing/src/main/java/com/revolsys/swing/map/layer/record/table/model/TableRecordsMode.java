package com.revolsys.swing.map.layer.record.table.model;

import java.awt.Color;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.ListSelectionModel;

import com.revolsys.record.query.Query;
import com.revolsys.swing.map.layer.record.LayerRecord;

public interface TableRecordsMode {
  default void activate() {
  }

  default void deactivate() {
  }

  void exportRecords(final Query query, final Object target, boolean tableColumnsOnly);

  void forEachRecord(Query query, final Consumer<? super LayerRecord> action);

  Color getBorderColor();

  Icon getIcon();

  String getKey();

  LayerRecord getRecord(int index);

  int getRecordCount();

  ListSelectionModel getSelectionModel();

  String getTitle();

  default boolean isFilterByBoundingBoxSupported() {
    return false;
  }

  default boolean isSortable() {
    return true;
  }

  void refresh();
}
