package com.revolsys.swing.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;

import javax.swing.ListModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.revolsys.util.Property;
import com.revolsys.util.Reorderable;

public class BaseListModel<T> extends ArrayList<T> implements ListModel,
Serializable, Reorderable {
  private static final long serialVersionUID = 1L;

  protected EventListenerList listenerList = new EventListenerList();

  public BaseListModel() {
  }

  public BaseListModel(final Collection<? extends T> values) {
    addAll(values);
  }

  @Override
  public void add(final int index, final T element) {
    super.add(index, element);
    fireIntervalAdded(this, index, index);
  }

  @Override
  public boolean add(final T e) {
    final int index = size();
    final boolean result = super.add(e);
    fireIntervalAdded(this, index, index);
    return result;
  }

  @Override
  public boolean addAll(final Collection<? extends T> c) {
    if (Property.hasValue(c)) {
      final int index = size();
      final boolean result = super.addAll(c);
      fireIntervalAdded(this, index, index + c.size() - 1);
      return result;
    }
    return false;
  }

  @Override
  public boolean addAll(final int index, final Collection<? extends T> c) {
    if (Property.hasValue(c)) {
      final boolean result = super.addAll(index, c);
      fireIntervalAdded(this, index, index + c.size());
      return result;
    }
    return false;
  }

  @Override
  public void addListDataListener(final ListDataListener l) {
    this.listenerList.add(ListDataListener.class, l);
  }

  @Override
  public void clear() {
    if (size() > 0) {
      final int index1 = size() - 1;
      super.clear();
      if (index1 >= 0) {
        fireIntervalRemoved(this, 0, index1);
      }
    }
  }

  protected void fireContentsChanged(final Object source, final int index0,
    final int index1) {
    final Object[] listeners = this.listenerList.getListenerList();
    ListDataEvent e = null;

    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ListDataListener.class) {
        if (e == null) {
          e = new ListDataEvent(source, ListDataEvent.CONTENTS_CHANGED, index0,
            index1);
        }
        ((ListDataListener)listeners[i + 1]).contentsChanged(e);
      }
    }
  }

  protected void fireIntervalAdded(final Object source, final int index0,
    final int index1) {
    final Object[] listeners = this.listenerList.getListenerList();
    ListDataEvent e = null;

    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ListDataListener.class) {
        if (e == null) {
          e = new ListDataEvent(source, ListDataEvent.INTERVAL_ADDED, index0,
            index1);
        }
        ((ListDataListener)listeners[i + 1]).intervalAdded(e);
      }
    }
  }

  protected void fireIntervalRemoved(final Object source, final int index0,
    final int index1) {
    final Object[] listeners = this.listenerList.getListenerList();
    ListDataEvent e = null;

    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ListDataListener.class) {
        if (e == null) {
          e = new ListDataEvent(source, ListDataEvent.INTERVAL_REMOVED, index0,
            index1);
        }
        ((ListDataListener)listeners[i + 1]).intervalRemoved(e);
      }
    }
  }

  @Override
  public T getElementAt(final int index) {
    return super.get(index);
  }

  public ListDataListener[] getListDataListeners() {
    return this.listenerList.getListeners(ListDataListener.class);
  }

  public <V extends EventListener> V[] getListeners(final Class<V> listenerType) {
    return this.listenerList.getListeners(listenerType);
  }

  @Override
  public int getSize() {
    return size();
  }

  @Override
  public T remove(final int index) {
    final T oldValue = get(index);
    super.remove(index);
    fireIntervalRemoved(this, index, index);
    return oldValue;
  }

  @Override
  public boolean remove(final Object obj) {
    final int index = indexOf(obj);
    final boolean rv = super.remove(obj);
    if (index >= 0) {
      fireIntervalRemoved(this, index, index);
    }
    return rv;
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    boolean removed = false;
    for (final Object object : c) {
      removed |= remove(object);
    }
    return removed;
  }

  public boolean removeAll(final Object... c) {
    boolean removed = false;
    for (final Object object : c) {
      removed |= remove(object);
    }
    return removed;
  }

  @Override
  public void removeListDataListener(final ListDataListener l) {
    this.listenerList.remove(ListDataListener.class, l);
  }

  @Override
  public void removeRange(final int fromIndex, final int toIndex) {
    if (fromIndex > toIndex) {
      throw new IllegalArgumentException("fromIndex must be <= toIndex");
    }
    super.removeRange(fromIndex, toIndex);
    fireIntervalRemoved(this, fromIndex, toIndex);
  }

  @Override
  public void reorder(final int fromIndex, int toIndex) {
    if (fromIndex < toIndex) {
      toIndex--;
    }
    final T value = get(fromIndex);
    remove(fromIndex);
    add(toIndex, value);
  }

  @Override
  public T set(final int index, final T element) {
    final T oldValue = get(index);
    super.set(index, element);
    fireContentsChanged(this, index, index);
    return oldValue;
  }

  public void setAll(final Collection<? extends T> c) {
    clear();
    addAll(c);
  }
}
