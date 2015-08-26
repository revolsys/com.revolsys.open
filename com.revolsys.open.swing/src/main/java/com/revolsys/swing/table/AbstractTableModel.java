package com.revolsys.swing.table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;

import javax.annotation.PreDestroy;
import javax.swing.JComponent;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;

public abstract class AbstractTableModel extends javax.swing.table.AbstractTableModel
  implements PropertyChangeSupportProxy {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private MenuFactory menu = new MenuFactory(getClass().getName());

  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  public AbstractTableModel() {
  }

  @PreDestroy
  public void dispose() {
    this.propertyChangeSupport = null;
    this.menu = null;
  }

  protected void firePropertyChange(final Object source, final String name, final Object oldValue,
    final Object newValue) {
    final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
    if (propertyChangeSupport != null) {
      final PropertyChangeEvent event = new PropertyChangeEvent(source, name, oldValue, newValue);
      propertyChangeSupport.firePropertyChange(event);
    }
  }

  protected void firePropertyChange(final PropertyChangeEvent event) {
    final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
    if (propertyChangeSupport != null) {
      propertyChangeSupport.firePropertyChange(event);
    }
  }

  protected void firePropertyChange(final String propertyName, final int index,
    final Object oldValue, final Object newValue) {
    final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
    if (propertyChangeSupport != null) {
      propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }
  }

  protected void firePropertyChange(final String propertyName, final Object oldValue,
    final Object newValue) {
    final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
    if (propertyChangeSupport != null) {
      propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  @Override
  public final void fireTableStructureChanged() {
    Invoke.later(() -> super.fireTableStructureChanged());
  }

  public JComponent getEditorField(final int rowIndex, final int columnIndex, final Object value) {
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

  public boolean isEmpty() {
    return getRowCount() == 0;
  }

  public void setMenu(final MenuFactory menu) {
    this.menu = menu;
  }

  public String toCopyValue(final int row, final int column, final Object value) {
    return StringConverterRegistry.toString(value);
  }
}
