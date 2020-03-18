package com.revolsys.swing.list;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.List;

import javax.swing.AbstractListModel;

import com.revolsys.util.Property;

public class ExternalListModel<T> extends AbstractListModel
  implements Serializable, PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private final List<T> list;

  public ExternalListModel(final List<T> list) {
    this.list = list;
    Property.addListener(list, this);
  }

  @Override
  public T getElementAt(final int index) {
    if (index < this.list.size()) {
      return this.list.get(index);
    } else {
      return null;
    }
  }

  @Override
  public int getSize() {
    return this.list.size();
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    fireContentsChanged(this, 0, this.list.size());
  }
}
