package com.revolsys.swing.table;

import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import javax.annotation.PreDestroy;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableColumnModel;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.function.BiConsumerInt;
import org.jeometry.common.logging.Logs;

import com.revolsys.beans.PropertyChangeSupport;
import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.io.format.json.JsonType;
import com.revolsys.record.io.format.tsv.Tsv;
import com.revolsys.record.io.format.tsv.TsvWriter;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.editor.BaseTableCellEditor;
import com.revolsys.swing.undo.AbstractUndoableEdit;
import com.revolsys.swing.undo.UndoManager;

public abstract class AbstractTableModel extends javax.swing.table.AbstractTableModel
  implements PropertyChangeSupportProxy, CellEditorListener {

  private static final long serialVersionUID = 1L;

  private MenuFactory menu = new MenuFactory(getClass().getName());

  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  private BaseJTable table;

  private final UndoManager undoManager = new UndoManager();

  public AbstractTableModel() {
  }

  /**
   * Add a menu item that will invoke the specific action with the {@link TablePanel#getEventRow()}
   * and {@link TablePanel#getEventColumn()}.
   *
   * @param groupName
   * @param index
   * @param title
   * @param iconName
   * @param action
   */
  public void addMenuItem(final String groupName, final int index, final String title,
    final String iconName, final BiConsumerInt action) {
    getMenu().addMenuItem(groupName, index, title, iconName, () -> {
      final int eventRow = TablePanel.getEventRow();
      final int eventColumn = TablePanel.getEventColumn();
      if (eventRow > -1 && eventColumn > -1) {
        action.accept(eventRow, eventColumn);
      }
    });
  }

  /**
  * Add a menu item that will invoke the specific action with the {@link TablePanel#getEventRow()}
  * and {@link TablePanel#getEventColumn()}.
  *
  * @param groupName
  * @param index
  * @param title
  * @param iconName
  * @param action
  */
  public void addMenuItem(final String groupName, final int index, final String title,
    final String iconName, final Consumer<BaseJTable> action) {
    getMenu().addMenuItem(groupName, index, title, iconName, () -> {
      final BaseJTable eventTable = TablePanel.getEventTable();
      if (eventTable != null) {
        action.accept(eventTable);
      }
    });
  }

  /**
  * Add a menu item that will invoke the specific action with the {@link TablePanel#getEventRow()}.
  *
  * @param groupName
  * @param index
  * @param title
  * @param iconName
  * @param action
  */
  public void addMenuItem(final String groupName, final int index, final String title,
    final String iconName, final IntConsumer action) {
    getMenu().addMenuItem(groupName, index, title, iconName, () -> {
      final int eventRow = TablePanel.getEventRow();
      final int eventColumn = TablePanel.getEventColumn();
      if (eventRow > -1 && eventColumn > -1) {
        action.accept(eventRow);
      }
    });
  }

  /**
   * Add a menu item that will invoke the specific action with the {@link TablePanel#getEventRow()}
   * and {@link TablePanel#getEventColumn()}.
   *
   * @param groupName
   * @param index
   * @param title
   * @param iconName
   * @param action
   */
  public void addMenuItem(final String groupName, final String title, final String iconName,
    final BiConsumerInt action) {
    addMenuItem(groupName, -1, title, iconName, action);
  }

  /**
   * Add a menu item that will invoke the specific action with the {@link TablePanel#getEventTable()}.
   *
   * @param groupName
   * @param index
   * @param title
   * @param iconName
   * @param action
   */
  public void addMenuItem(final String groupName, final String title, final String iconName,
    final Consumer<BaseJTable> action) {
    addMenuItem(groupName, -1, title, iconName, action);
  }

  /**
   * Add a menu item that will invoke the specific action with the {@link TablePanel#getEventRow()}.
   *
   * @param groupName
   * @param index
   * @param title
   * @param iconName
   * @param action
   */
  public void addMenuItem(final String groupName, final String title, final String iconName,
    final IntConsumer action) {
    addMenuItem(groupName, -1, title, iconName, action);
  }

  @PreDestroy
  public void dispose() {
    this.table = null;
    this.propertyChangeSupport = null;
    this.menu = null;
  }

  @Override
  public void editingCanceled(final ChangeEvent event) {
  }

  @Override
  public void editingStopped(final ChangeEvent event) {
  }

  @Override
  public void fireTableChanged(final TableModelEvent e) {
    Invoke.later(() -> {
      try {
        super.fireTableChanged(e);
      } catch (final Throwable t) {
        Logs.debug(getClass(), "Error refreshing table", t);
      }
    });
  }

  public BaseTableCellEditor getCellEditor(final int columnIndex) {
    return null;
  }

  public BaseTableCellEditor getCellEditor(final int rowIndex, final int columnIndex) {
    return null;
  }

  public JComponent getEditorField(final int rowIndex, final int columnIndex, final Object value) {
    final Class<?> clazz = getColumnClass(columnIndex);
    return SwingUtil.newField(clazz, "field", value);
  }

  public MenuFactory getHeaderMenuFactory(final int columnIndex) {
    return null;
  }

  public MenuFactory getMenu() {
    return this.menu;
  }

  public BaseJPopupMenu getMenu(final int rowIndex, final int columnIndex) {
    if (rowIndex >= 0 && rowIndex < getRowCount()) {
      return this.menu.newJPopupMenu();
    } else {
      return null;
    }
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  public Object getPrototypeValue(final int columnIndex) {
    return null;
  }

  public BaseJTable getTable() {
    return this.table;
  }

  public UndoManager getUndoManager() {
    return this.undoManager;
  }

  public boolean isColumnSortable(final int columnIndex) {
    final Class<?> columnClass = getColumnClass(columnIndex);
    if (Geometry.class.isAssignableFrom(columnClass)) {
      return false;
    } else if (Clob.class.isAssignableFrom(columnClass)) {
      return false;
    } else if (Blob.class.isAssignableFrom(columnClass)) {
      return false;
    } else if (JsonType.class.isAssignableFrom(columnClass)) {
      return false;
    } else {
      return true;
    }
  }

  public boolean isEmpty() {
    return getRowCount() == 0;
  }

  public ListSelectionModel newListSelectionModel() {
    return null;
  }

  public BaseJTable newTable() {
    final BaseJTable table = new BaseJTable(this);
    this.undoManager.addKeyMap(table);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setAutoCreateColumnsFromModel(true);
    return table;
  }

  public TableColumnModel newTableColumnModel() {
    return null;
  }

  public TablePanel newTablePanel() {
    final BaseJTable table = newTable();
    return new TablePanel(table);
  }

  public void setMenu(final MenuFactory menu) {
    this.menu = menu;
  }

  public void setTable(final BaseJTable table) {
    this.table = table;
  }

  public void setValueUndo(final Object value, final int rowIndex, final int columnIndex) {
    final Object oldValue = getValueAt(rowIndex, columnIndex);
    if (!DataType.equal(value, oldValue)) {
      this.undoManager.addEdit(new AbstractUndoableEdit() {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean canRedo() {
          final Object currentValue = getValueAt(rowIndex, columnIndex);
          return DataType.equal(currentValue, oldValue);
        }

        @Override
        public boolean canUndo() {
          final Object currentValue = getValueAt(rowIndex, columnIndex);
          return DataType.equal(currentValue, value);
        }

        @Override
        protected void redoDo() {
          setValueAt(value, rowIndex, columnIndex);
        }

        @Override
        protected void undoDo() {
          setValueAt(oldValue, rowIndex, columnIndex);
        }
      });
    }
  }

  public String toCopyValue(final int row, final int column, final Object value) {
    return DataTypes.toString(value);
  }

  public void toTsv(final Writer out) {
    try (
      TsvWriter tsv = Tsv.plainWriter(out)) {
      final List<Object> values = new ArrayList<>();
      for (int i = 0; i < getColumnCount(); i++) {
        final Object value = getColumnName(i);
        values.add(value);
      }
      tsv.write(values);
      for (int rowIndex = 0; rowIndex < getRowCount(); rowIndex++) {
        for (int columnIndex = 0; columnIndex < getColumnCount(); columnIndex++) {
          final Object value = getValueAt(rowIndex, columnIndex);
          values.set(columnIndex, value);
        }
        tsv.write(values);
      }
    }
  }
}
