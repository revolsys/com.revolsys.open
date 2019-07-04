package com.revolsys.swing.map.layer.record.table.model;

import java.awt.Color;
import java.util.Collection;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.ListSelectionModel;

import com.revolsys.record.query.Query;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.map.layer.record.LayerRecord;

public interface TableRecordsMode {
  default void activate() {
  }

  default void deactivate() {
  }

  void exportRecords(final Query query, final Collection<String> fieldNames, final Object target);

  void forEachRecord(Query query, final Consumer<? super LayerRecord> action);

  Color getBorderColor();

  default EnableCheck getEnableCheck() {
    return EnableCheck.ENABLED;
  }

  Icon getIcon();

  String getKey();

  LayerRecord getRecord(int index);

  int getRecordCount();

  ListSelectionModel getSelectionModel();

  String getTitle();

  default boolean isEnabled() {
    return getEnableCheck().isEnabled();
  }

  default boolean isFilterByBoundingBoxSupported() {
    return false;
  }

  default boolean isSortable() {
    return true;
  }

  void refresh();
}
