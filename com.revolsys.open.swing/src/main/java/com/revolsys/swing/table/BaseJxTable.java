package com.revolsys.swing.table;

import java.awt.Component;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.RowSorter;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.TableColumnExt;

import com.revolsys.awt.WebColors;
import com.revolsys.swing.SwingUtil;

public class BaseJxTable extends JXTable {
  private static final long serialVersionUID = 1L;

  public BaseJxTable() {

  }

  public BaseJxTable(final TableModel model) {
    super(model);
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    addHighlighter(new ColorHighlighter(HighlightPredicate.ODD,
      WebColors.LightSteelBlue, WebColors.Black, WebColors.Navy,
      WebColors.White));
    addHighlighter(new ColorHighlighter(HighlightPredicate.EVEN,
      WebColors.White, WebColors.Black, WebColors.Blue, WebColors.White));

    final TableCellRenderer headerRenderer = new SortableTableCellHeaderRenderer();
    final JTableHeader tableHeader = getTableHeader();
    tableHeader.setDefaultRenderer(headerRenderer);
    tableHeader.setReorderingAllowed(true);
    setFont(SwingUtil.FONT);

    SwingUtil.addAction(this,
      KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK),
      "selectPreviousColumnCell", this, "editRelativeCell", 0, -1);
    SwingUtil.addAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
      "selectNextColumnCell", this, "editRelativeCell", 0, 1);
    SwingUtil.addAction(this,
      KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK),
      "selectPreviousRowCell", this, "editRelativeCell", -1, 0);
    SwingUtil.addAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
      "enterPressed", this, "editRelativeCell", 1, 0);
  }

  @Override
  public int convertColumnIndexToModel(final int columnIndex) {
    try {
      return super.convertColumnIndexToModel(columnIndex);
    } catch (final IndexOutOfBoundsException e) {
      return columnIndex;
    }
  }

  @Override
  public int convertColumnIndexToView(final int columnIndex) {
    try {
      return super.convertColumnIndexToView(columnIndex);
    } catch (final IndexOutOfBoundsException e) {
      return columnIndex;
    }
  }

  @Override
  public int convertRowIndexToModel(final int rowIndex) {
    try {
      return super.convertRowIndexToModel(rowIndex);
    } catch (final IndexOutOfBoundsException e) {
      return rowIndex;
    }
  }

  @Override
  public int convertRowIndexToView(final int rowIndex) {
    try {
      return super.convertRowIndexToView(rowIndex);
    } catch (final IndexOutOfBoundsException e) {
      return rowIndex;
    }
  }

  @Override
  protected void createDefaultRenderers() {
    super.createDefaultRenderers();
    setDefaultRenderer(Object.class, new DefaultTableRenderer(
      new StringConverterValue()));

  }

  @Override
  protected RowSorter<? extends TableModel> createDefaultRowSorter() {
    return new BaseRowSorter(getModel());
  }

  public void editCell(final int rowIndex, final int columnIndex) {

    if (rowIndex >= 0 && rowIndex < getRowCount() && columnIndex >= 0
      && columnIndex < getColumnCount()) {
      requestFocusInWindow();
      changeSelection(rowIndex, columnIndex, false, false);
      editCellAt(rowIndex, columnIndex);
      final Component editor = getEditorComponent();
      if (editor != null) {
        editor.requestFocusInWindow();
      }
    }
  }

  public void editRelativeCell(final int rowDelta, final int columnDelta) {
    final int rowIndex = getSelectedRow() + rowDelta;
    final int columnIndex = getSelectedColumn() + columnDelta;
    editCell(rowIndex, columnIndex);
  }

  public int getPreferedSize(TableCellRenderer renderer,
    final Class<?> columnClass, final Object value) {
    if (renderer == null) {
      renderer = getDefaultRenderer(columnClass);
    }
    final Component comp = renderer.getTableCellRendererComponent(this, value,
      false, false, 0, -1);
    final int width = comp.getPreferredSize().width;
    return width;
  }

  @Override
  public RowSorter<? extends TableModel> getRowSorter() {
    if (isSortable()) {
      return super.getRowSorter();
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public <V extends TableModel> V getTableModel() {
    return (V)getModel();
  }

  @Override
  public boolean isCellEditable(final int row, final int column) {
    try {
      return super.isCellEditable(row, column);
    } catch (final IndexOutOfBoundsException e) {
      return false;
    }
  }

  @Override
  public Component prepareRenderer(final TableCellRenderer renderer,
    final int row, final int column) {
    try {
      return super.prepareRenderer(renderer, row, column);
    } catch (final IndexOutOfBoundsException e) {
      return new JLabel("...");
    }
  }

  public void resizeColumnsToContent() {
    final TableModel model = getModel();
    final int columnCount = getColumnCount();
    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
      final TableColumnExt column = getColumnExt(columnIndex);

      final TableCellRenderer headerRenderer = column.getHeaderRenderer();
      final String columnName = model.getColumnName(columnIndex);
      int maxPreferedWidth = getPreferedSize(headerRenderer, String.class,
        columnName);

      for (int rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++) {
        final Object value = model.getValueAt(rowIndex, columnIndex);
        if (value != null) {
          final TableCellRenderer renderer = column.getCellRenderer();
          final Class<?> columnClass = model.getColumnClass(columnIndex);
          final int width = getPreferedSize(renderer, columnClass, value);
          if (width > maxPreferedWidth) {
            maxPreferedWidth = width;
          }
        }
      }
      column.setMinWidth(maxPreferedWidth + 5);
      column.setPreferredWidth(maxPreferedWidth + 5);
    }
  }

  public void setColumnWidth(final int i, final int width) {
    final TableColumnExt column = getColumnExt(i);
    column.setMinWidth(width);
    column.setWidth(width);
    column.setMaxWidth(width);
  }

  @Override
  public void setModel(final TableModel model) {
    final boolean createColumns = getAutoCreateColumnsFromModel();
    if (createColumns) {
      setAutoCreateColumnsFromModel(false);
    }
    try {
      super.setModel(model);
    } finally {
      if (createColumns) {
        setAutoCreateColumnsFromModel(true);
      }
      if (isSortable()) {
        setRowSorter(createDefaultRowSorter());
      }
    }
    initializeColumnWidths();
  }

  @Override
  public void setSortable(final boolean sortable) {
    super.setSortable(sortable);
    if (sortable) {
      if (getRowSorter() == null) {
        setRowSorter(createDefaultRowSorter());
      }
    } else {
      setRowSorter(null);
    }
  }
}
