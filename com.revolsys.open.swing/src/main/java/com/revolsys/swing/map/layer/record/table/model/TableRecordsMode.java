package com.revolsys.swing.map.layer.record.table.model;

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

  void exportRecords(final Query query, final Object target);

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
