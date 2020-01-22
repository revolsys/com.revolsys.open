package com.revolsys.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.EventObject;

import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.BorderHighlighter;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jeometry.common.awt.WebColors;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.function.BiFunctionInt;

import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.dnd.ClipboardUtil;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.editor.BaseTableCellEditor;
import com.revolsys.swing.table.highlighter.OddEvenColorHighlighter;
import com.revolsys.swing.table.highlighter.TableModelHighlighter;
import com.revolsys.util.Property;

public class BaseJTable extends JXTable {
  private static final long serialVersionUID = 1L;

  private boolean initializingColumnWidths = false;

  private EventObject editEvent;

  private BaseTableCellEditor tableCellEditor;

  public BaseJTable() {
  }

  public BaseJTable(final AbstractTableModel model) {
    super(model, model.newTableColumnModel(), model.newListSelectionModel());
    this.tableCellEditor = newTableCellEditor();
    this.tableCellEditor.addCellEditorListener(model);

    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    setGridColor(new Color(191, 191, 191));
    setShowGrid(false, true);
    addHighlighter(new ColorHighlighter(HighlightPredicate.ODD, new Color(223, 223, 223),
      WebColors.Black, WebColors.Navy, WebColors.White));
    addHighlighter(new ColorHighlighter(HighlightPredicate.EVEN, WebColors.White, WebColors.Black,
      WebColors.Blue, WebColors.White));

    addLastRowBorderPredicate();

    final TableCellRenderer headerRenderer = new SortableTableCellHeaderRenderer();
    final JTableHeader tableHeader = getTableHeader();
    tableHeader.setDefaultRenderer(headerRenderer);
    tableHeader.setReorderingAllowed(true);
    setFont(SwingUtil.FONT);

    final InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "copy");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.META_DOWN_MASK), "copy");

    SwingUtil.addAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK),
      "selectPreviousColumnCell", () -> editRelativeCell(0, -1));
    SwingUtil.addAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "selectNextColumnCell",
      () -> editRelativeCell(0, 1));
    SwingUtil.addAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK),
      "selectPreviousRowCell", () -> editRelativeCell(-1, 0));
    SwingUtil.addAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterPressed",
      () -> editRelativeCell(1, 0));

    putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
  }

  public OddEvenColorHighlighter addColorHighlighter(final BiFunctionInt<Boolean> filter,
    final Color color1, final Color color2) {
    final HighlightPredicate predicate = newPredicateModelRowColumn(filter);
    final OddEvenColorHighlighter highlighter = new OddEvenColorHighlighter(predicate, color1,
      color2);
    addHighlighter(highlighter);
    return highlighter;
  }

  public void addHighlighter(final TableModelHighlighter highlighter) {
    addHighlighter(new AbstractHighlighter() {
      @Override
      protected Component doHighlight(final Component component, final ComponentAdapter adapter) {
        final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
        final int columnIndex = adapter.convertColumnIndexToModel(adapter.column);
        return highlighter.highlight(component, adapter, rowIndex, columnIndex);
      }
    });
  }

  protected void addLastRowBorderPredicate() {
    final HighlightPredicate lastPredicate = (final Component renderer,
      final ComponentAdapter adapter) -> {
      final int row = adapter.row;
      final int lastRowIndex = getRowCount() - 1;
      return row == lastRowIndex;
    };
    addHighlighter(new BorderHighlighter(lastPredicate,
      BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(191, 191, 191))));
  }

  public void addRowSorterListener(final RowSorterListener listener) {
    super.getRowSorter().addRowSorterListener(listener);
  }

  @Override
  protected void adjustComponentOrientation(final Component stamp) {
    if (stamp != null) {
      super.adjustComponentOrientation(stamp);
    }
  }

  public void cancelCellEditing() {
    final TableCellEditor editor = getCellEditor();
    if (editor != null) {
      editor.cancelCellEditing();
    }
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

  private void copyCurrentCell() {
    final TableModel model = getTableModel();
    final Object value = model.getValueAt(TablePanel.getEventRow(), TablePanel.getEventColumn());

    final String copyValue;
    if (model instanceof AbstractTableModel) {
      final AbstractTableModel tableModel = (AbstractTableModel)model;
      copyValue = tableModel.toCopyValue(TablePanel.getEventRow(), TablePanel.getEventColumn(),
        value);
    } else {
      copyValue = DataTypes.toString(value);
    }
    final StringSelection transferable = new StringSelection(copyValue);
    ClipboardUtil.setContents(transferable);
  }

  public void copyFieldValue() {
    if (isEditingCurrentCell()) {
      if (!getCellEditor().stopCellEditing()) {
        return;
      }
    }
    copyCurrentCell();
  }

  @Override
  protected void createDefaultRenderers() {
    super.createDefaultRenderers();
    setDefaultRenderer(Object.class, new DefaultTableRenderer(new StringConverterValue()));

  }

  @Override
  protected RowSorter<? extends TableModel> createDefaultRowSorter() {
    final TableModel model = getModel();
    return new BaseRowSorter(model);
  }

  public void cutFieldValue() {
    if (isEditingCurrentCell()) {
      if (!getCellEditor().stopCellEditing()) {
        return;
      }
    }
    copyCurrentCell();
    final int eventRow = TablePanel.getEventRow();
    final int eventColumn = TablePanel.getEventColumn();
    final TableModel tableModel = getTableModel();
    if (eventRow > -1 && eventColumn > -1 && tableModel.isCellEditable(eventRow, eventColumn)) {
      tableModel.setValueAt(null, eventRow, eventColumn);
    }
  }

  public void dispose() {
    this.tableCellEditor = null;
    for (final Highlighter highlighter : getHighlighters()) {
      removeHighlighter(highlighter);
    }
  }

  public void editCell(final int rowIndex, final int columnIndex) {

    if (rowIndex >= 0 && rowIndex < getRowCount() && columnIndex >= 0
      && columnIndex < getColumnCount()) {
      requestFocusInWindow();
      changeSelection(rowIndex, columnIndex, false, false);
      editCellAt(rowIndex, columnIndex);

    }
  }

  @Override
  public boolean editCellAt(final int rowIndex, final int columnIndex, final EventObject e) {
    if (rowIndex >= 0 && rowIndex < getRowCount() && columnIndex >= 0
      && columnIndex < getColumnCount()) {
      // requestFocusInWindow();
      changeSelection(rowIndex, columnIndex, false, false);
      this.editEvent = e;
      try {
        if (super.editCellAt(rowIndex, columnIndex, e)) {
          // final Component editor = getEditorComponent();
          // if (editor != null) {
          // editor.requestFocusInWindow();
          // }
          return true;
        } else {
          return false;
        }
      } finally {
        this.editEvent = null;
      }
    } else {
      return false;
    }
  }

  public void editRelativeCell(final int rowDelta, final int columnDelta) {
    final int selectedRow = getSelectedRow();
    final int selectedColumn = getSelectedColumn();
    final int rowIndex = selectedRow + rowDelta;
    final int columnIndex = selectedColumn + columnDelta;
    editCellAt(rowIndex, columnIndex);
  }

  public BaseTableCellEditor getCellEditor(final int columnIndex) {
    final AbstractTableModel tableModel = getTableModel();
    final BaseTableCellEditor editor = tableModel.getCellEditor(columnIndex);
    if (editor == null) {
      return this.tableCellEditor;
    } else {
      return editor;
    }
  }

  @Override
  public TableCellEditor getCellEditor(final int rowIndex, final int columnIndex) {
    final AbstractTableModel tableModel = getTableModel();
    final TableCellEditor editor = tableModel.getCellEditor(rowIndex, columnIndex);
    if (editor == null) {
      return super.getCellEditor(rowIndex, columnIndex);
    } else {
      return editor;
    }
  }

  public EventObject getEditEvent() {
    return this.editEvent;
  }

  public int getPreferedSize(TableCellRenderer renderer, final Class<?> columnClass,
    final Object value) {
    if (renderer == null) {
      renderer = getDefaultRenderer(columnClass);
    }
    final Component comp = renderer.getTableCellRendererComponent(this, value, false, false, 0, -1);
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

  public int getSelectedRowInModel() {
    int selectedRow = getSelectedRow();
    if (selectedRow != -1) {
      selectedRow = convertRowIndexToModel(selectedRow);
    }
    return selectedRow;
  }

  public int getSelectedRowInView() {
    int selectedRow = getSelectedRow();
    if (selectedRow != -1) {
      selectedRow = convertRowIndexToView(selectedRow);
    }
    return selectedRow;
  }

  public int[] getSelectedRowsInModel() {
    final int[] selectedRows = getSelectedRows();
    for (int i = 0; i < selectedRows.length; i++) {
      final int row = selectedRows[i];
      selectedRows[i] = convertRowIndexToModel(row);
    }
    Arrays.sort(selectedRows);
    return selectedRows;
  }

  public BaseTableCellEditor getTableCellEditor() {
    return this.tableCellEditor;
  }

  @SuppressWarnings("unchecked")
  public <V extends TableModel> V getTableModel() {
    return (V)getModel();
  }

  @Override
  public void initializeColumnWidths() {
    try {
      this.initializingColumnWidths = true;
      super.initializeColumnWidths();
    } finally {
      this.initializingColumnWidths = false;
    }
  }

  @Override
  public boolean isCellEditable(final int row, final int column) {
    try {
      return super.isCellEditable(row, column);
    } catch (final IndexOutOfBoundsException e) {
      return false;
    }
  }

  public boolean isEditingCurrentCell() {
    if (isEditing()) {
      final int eventRow = TablePanel.getEventRow();
      final int eventColumn = TablePanel.getEventColumn();
      if (eventRow > -1 && eventRow == getEditingRow()) {
        if (eventColumn > -1 && eventColumn == getEditingColumn()) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isInitializingColumnWidths() {
    return this.initializingColumnWidths;
  }

  public HighlightPredicate newPredicateModelRowColumn(final BiFunctionInt<Boolean> filter) {
    return (renderer, adapter) -> {
      try {
        final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
        final int columnIndex = adapter.convertColumnIndexToModel(adapter.column);
        return filter.accept(rowIndex, columnIndex);
      } catch (final IndexOutOfBoundsException e) {
      }
      return false;
    };
  }

  protected BaseTableCellEditor newTableCellEditor() {
    return new BaseTableCellEditor(this);
  }

  public void pasteFieldValue() {
    if (isEditingCurrentCell()) {
      if (!getCellEditor().stopCellEditing()) {
        return;
      }
    }
    final String value = ClipboardUtil.getContents(DataFlavor.stringFlavor);
    if (Property.hasValue(value)) {
      final TableModel tableModel = getTableModel();
      final int eventRow = TablePanel.getEventRow();
      final int eventColumn = TablePanel.getEventColumn();
      if (tableModel.isCellEditable(eventRow, eventColumn)) {
        tableModel.setValueAt(value, eventRow, eventColumn);
      }
    }
  }

  @Override
  public Component prepareRenderer(final TableCellRenderer renderer, final int row,
    final int column) {
    try {
      return super.prepareRenderer(renderer, row, column);
    } catch (final IndexOutOfBoundsException e) {
      return new JLabel("...");
    }
  }

  public void removeRowSorterListener(final RowSorterListener listener) {
    super.getRowSorter().removeRowSorterListener(listener);
  }

  @Override
  public void repaint() {
    Invoke.later(super::repaint);
  }

  public void resizeColumnsToContent() {
    final TableModel model = getModel();
    final int columnCount = getColumnCount();
    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
      final TableColumnExt column = getColumnExt(columnIndex);

      final TableCellRenderer headerRenderer = column.getHeaderRenderer();
      final String columnName = model.getColumnName(columnIndex);
      int maxPreferedWidth = getPreferedSize(headerRenderer, String.class, columnName);

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
      column.setMinWidth(maxPreferedWidth + 25);
      column.setPreferredWidth(maxPreferedWidth + 25);
    }
  }

  @Override
  public void setColumnFactory(final ColumnFactory columnFactory) {
    super.setColumnFactory(columnFactory);
    final TableModelEvent e = new TableModelEvent(getModel(), TableModelEvent.HEADER_ROW);
    tableChanged(e);
  }

  public BaseJTable setColumnPreferredWidth(final int i, final int width) {
    final TableColumnExt column = getColumnExt(i);
    column.setWidth(width);
    column.setPreferredWidth(width);
    return this;
  }

  public BaseJTable setColumnWidth(final int i, final int width) {
    final TableColumnExt column = getColumnExt(i);
    column.setMinWidth(width);
    column.setWidth(width);
    column.setMaxWidth(width);
    return this;
  }

  public void setMinWidth(final int columnIndex, final int width) {
    final TableColumnExt column = getColumnExt(columnIndex);
    column.setMinWidth(width);
  }

  @Override
  public void setModel(final TableModel model) {
    if (model instanceof AbstractTableModel) {
      final AbstractTableModel tableModel = (AbstractTableModel)model;
      tableModel.setTable(this);
    }
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
  public <R extends TableModel> void setRowFilter(
    final RowFilter<? super R, ? super Integer> filter) {
    try {
      super.setRowFilter(filter);
    } catch (final NegativeArraySizeException e) {
    }
    firePropertyChange("rowFilterChanged", false, true);
  }

  @Override
  public void setSortable(final boolean sortable) {
    super.setSortable(sortable);
    if (sortable) {
      RowSorter<? extends TableModel> rowSorter = getRowSorter();
      if (rowSorter == null) {
        rowSorter = createDefaultRowSorter();
        setRowSorter(rowSorter);
      }
    } else {
      setRowSorter(null);
    }
  }

  public boolean stopCellEditing() {
    final TableCellEditor editor = getCellEditor();
    if (editor != null) {
      return editor.stopCellEditing();
    }
    return true;
  }
}
