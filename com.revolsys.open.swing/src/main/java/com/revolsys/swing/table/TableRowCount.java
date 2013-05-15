package com.revolsys.swing.table;

import java.awt.Dimension;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class TableRowCount extends JLabel implements TableModelListener {
  private TableModel model;

  public TableRowCount(TableModel model) {
    setToolTipText("Number of records");
    setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createBevelBorder(BevelBorder.LOWERED),
      BorderFactory.createEmptyBorder(2, 5, 2, 5)));
    setHorizontalAlignment(RIGHT);
    this.model = model;
    model.addTableModelListener(this);
    tableChanged(null);
  }

  @Override
  public void tableChanged(TableModelEvent e) {
    setMaximumSize(new Dimension(100, 50));
      NumberFormat FORMAT = new DecimalFormat("#,##0");
    String text = FORMAT.format(model.getRowCount());
    setText(text);
  }
}
