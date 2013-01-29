package com.revolsys.swing.list;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.List;

import javax.swing.AbstractListModel;

import com.revolsys.beans.PropertyChangeSupportProxy;

public class ExternalListModel<T> extends AbstractListModel implements
  Serializable, PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private List<T> list;

  public ExternalListModel(List<T> list) {
    this.list = list;
    if (list instanceof PropertyChangeSupportProxy) {
      PropertyChangeSupportProxy proxy = (PropertyChangeSupportProxy)list;
      proxy.getPropertyChangeSupport().addPropertyChangeListener(this);
    }
  }

  @Override
  public T getElementAt(int index) {
    if (index < list.size()) {
      return list.get(index);
    } else {
      return null;
    }
  }

  @Override
  public int getSize() {
    return list.size();
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    fireContentsChanged(this, 0, list.size());
  }
}
