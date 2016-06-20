package com.revolsys.swing.map.layer.record.table.model;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.ListSelectionModel;

import com.revolsys.record.query.Query;
import com.revolsys.swing.map.layer.record.LayerRecord;

public class ModeEmpty implements TableRecordsMode {
  @Override
  public void exportRecords(final Query query, final Object target) {
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getKey() {
    return "empty";
  }

  @Override
  public LayerRecord getRecord(final int index) {
    return null;
  }

  @Override
  public int getRecordCount() {
    return 0;
  }

  @Override
  public ListSelectionModel getSelectionModel() {
    return new DefaultListSelectionModel();
  }

  @Override
  public String getTitle() {
    return "Empty";
  }

  @Override
  public void refresh() {
  }
}
