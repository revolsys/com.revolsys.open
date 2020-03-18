package com.revolsys.swing.table;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class TableRowCount extends JLabel implements TableModelListener, PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private TableModel model;

  private BaseJTable table;

  private int rowCount;

  public TableRowCount(final BaseJTable table) {
    setToolTipText("Record Count");
    setBorder(
      BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),
        BorderFactory.createEmptyBorder(2, 5, 2, 5)));
    setHorizontalAlignment(RIGHT);
    this.table = table;
    table.addPropertyChangeListener("rowFilterChanged", this);
    this.model = table.getModel();
    // Table events are in reverse order. The following moves this listener to
    // be performed after that table's listener. Otherwise the counts maybe
    // wrong.
    this.model.removeTableModelListener(table);
    this.model.addTableModelListener(this);
    this.model.addTableModelListener(table);
    tableChanged(null);
  }

  public int getRowCount() {
    return this.rowCount;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    updateCount();
  }

  @Override
  public void removeNotify() {
    if (this.table != null) {
      this.table.removePropertyChangeListener("rowFilterChanged", this);
      this.table = null;
    }
    if (this.model != null) {
      this.model.removeTableModelListener(this);
      this.model = null;
    }
    super.removeNotify();
  }

  @Override
  public void tableChanged(final TableModelEvent e) {
    updateCount();
  }

  private void updateCount() {
    final Dimension size = new Dimension(100, 22);
    setPreferredSize(size);
    setMaximumSize(size);
    final NumberFormat format = new DecimalFormat("#,##0");
    final int tableRowCount = this.table.getRowCount();
    final int modelRowCount = this.model.getRowCount();
    final int oldValue = this.rowCount;
    this.rowCount = tableRowCount;
    final String text;
    if (tableRowCount == modelRowCount) {
      text = format.format(tableRowCount);
      setToolTipText(null);
    } else {
      text = "<html><b color='red'>" + format.format(tableRowCount) + "</b> of "
        + format.format(modelRowCount) + "</html>";
      setToolTipText("The table is filtered so some records are hidden from view.");
    }
    setText(text);
    firePropertyChange("rowCount", oldValue, this.rowCount);
  }
}
