package com.revolsys.swing.table;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import javax.annotation.PreDestroy;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableColumnModel;

import com.revolsys.beans.PropertyChangeSupport;
import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.datatype.DataTypes;
import com.revolsys.logging.Logs;
import com.revolsys.record.io.format.tsv.Tsv;
import com.revolsys.record.io.format.tsv.TsvWriter;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.function.IntBiConsumer;

public abstract class AbstractTableModel extends javax.swing.table.AbstractTableModel
  implements PropertyChangeSupportProxy {

  private static final long serialVersionUID = 1L;

  private MenuFactory menu = new MenuFactory(getClass().getName());

  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  private BaseJTable table;

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
  protected void addMenuItem(final String groupName, final int index, final String title,
    final String iconName, final Consumer<BaseJTable> action) {
    getMenu().addMenuItem(groupName, index, title, iconName, () -> {
      final BaseJTable eventTable = TablePanel.getEventTable();
      if (eventTable != null) {
        action.accept(eventTable);
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
  protected void addMenuItem(final String groupName, final int index, final String title,
    final String iconName, final IntBiConsumer action) {
    getMenu().addMenuItem(groupName, index, title, iconName, () -> {
      final int eventRow = TablePanel.getEventRow();
      final int eventColumn = TablePanel.getEventColumn();
      if (eventRow > -1 && eventColumn > -1) {
        action.accept(eventRow, eventColumn);
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
  protected void addMenuItem(final String groupName, final int index, final String title,
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
   * Add a menu item that will invoke the specific action with the {@link TablePanel#getEventTable()}.
   *
   * @param groupName
   * @param index
   * @param title
   * @param iconName
   * @param action
   */
  protected void addMenuItem(final String groupName, final String title, final String iconName,
    final Consumer<BaseJTable> action) {
    addMenuItem(groupName, -1, title, iconName, action);
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
  protected void addMenuItem(final String groupName, final String title, final String iconName,
    final IntBiConsumer action) {
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
  protected void addMenuItem(final String groupName, final String title, final String iconName,
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
  public void fireTableChanged(final TableModelEvent e) {
    Invoke.later(() -> {
      try {
        super.fireTableChanged(e);
      } catch (final Throwable t) {
        Logs.debug(getClass(), "Error refreshing table", t);
      }
    });
  }

  public JComponent getEditorField(final int rowIndex, final int columnIndex, final Object value) {
    final Class<?> clazz = getColumnClass(columnIndex);
    return SwingUtil.newField(clazz, "field", value);
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

  public boolean isEmpty() {
    return getRowCount() == 0;
  }

  public ListSelectionModel newListSelectionModel() {
    return null;
  }

  public TableColumnModel newTableColumnModel() {
    return null;
  }

  public void setMenu(final MenuFactory menu) {
    this.menu = menu;
  }

  public void setTable(final BaseJTable table) {
    this.table = table;
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
