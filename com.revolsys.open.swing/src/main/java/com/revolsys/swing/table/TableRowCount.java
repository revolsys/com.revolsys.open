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
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private final TableModel model;

  public TableRowCount(final TableModel model) {
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
  public void tableChanged(final TableModelEvent e) {
    setPreferredSize(new Dimension(100, 25));
    setMaximumSize(new Dimension(100, 25));
    final NumberFormat FORMAT = new DecimalFormat("#,##0");
    final String text = FORMAT.format(this.model.getRowCount());
    setText(text);
  }
}
