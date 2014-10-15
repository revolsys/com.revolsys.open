package com.revolsys.swing.table;

import java.beans.PropertyChangeSupport;

import javax.swing.JComponent;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.menu.MenuFactory;

public abstract class AbstractTableModel extends
javax.swing.table.AbstractTableModel implements PropertyChangeSupportProxy {

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  private MenuFactory menu = new MenuFactory(getClass().getName());

  public AbstractTableModel() {
  }

  public JComponent getEditorField(final int rowIndex, final int columnIndex,
    final Object value) {
    final Class<?> clazz = getColumnClass(columnIndex);
    return SwingUtil.createField(clazz, "field", value);
  }

  public MenuFactory getMenu() {
    return this.menu;
  }

  public MenuFactory getMenu(final int rowIndex, final int columnIndex) {
    return this.menu;
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  public void setMenu(final MenuFactory menu) {
    this.menu = menu;
  }

  public String toCopyValue(final int row, final int column, final Object value) {
    return StringConverterRegistry.toString(value);
  }
}
