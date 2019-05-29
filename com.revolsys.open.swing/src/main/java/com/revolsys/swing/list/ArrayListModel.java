package com.revolsys.swing.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.jeometry.common.data.type.DataType;

import com.revolsys.collection.list.Lists;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;
import com.revolsys.util.Reorderable;

public class ArrayListModel<T> extends ArrayList<T>
  implements ListModel<T>, Serializable, Reorderable {
  private static final long serialVersionUID = 1L;

  private final List<ListDataListener> listeners = new ArrayList<>();

  public ArrayListModel() {
  }

  public ArrayListModel(final Collection<? extends T> values) {
    addAll(values);
  }

  @Override
  public void add(final int index, final T value) {
    Invoke.andWait(() -> {
      super.add(index, value);
      fireIntervalAdded(index, index);
    });
  }

  @Override
  public boolean add(final T value) {
    final int index = size();
    add(index, value);
    return true;
  }

  @Override
  public boolean addAll(final Collection<? extends T> values) {
    final int index = size();
    return addAll(index, values);
  }

  @Override
  public boolean addAll(final int index, final Collection<? extends T> values) {
    if (Property.hasValue(values)) {
      Invoke.andWait(() -> {
        super.addAll(index, values);
        fireIntervalAdded(index, index + values.size());
      });
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void addListDataListener(final ListDataListener listener) {
    synchronized (this.listeners) {
      this.listeners.add(listener);
    }
  }

  @Override
  public void clear() {
    Invoke.andWait(() -> {
      if (size() > 0) {
        final int index1 = size() - 1;
        super.clear();
        if (index1 >= 0) {
          fireIntervalRemoved(0, index1);
        }
      }
    });
  }

  protected void fireContentsChanged(final int index0, final int index1) {
    Invoke.later(() -> {
      synchronized (this.listeners) {
        if (!this.listeners.isEmpty()) {
          final ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index0,
            index1);
          for (int i = 0; i < this.listeners.size(); i++) {
            final ListDataListener listener = this.listeners.get(i);
            listener.contentsChanged(e);
          }
        }
      }
    });
  }

  protected void fireIntervalAdded(final int index0, final int index1) {
    Invoke.later(() -> {
      synchronized (this.listeners) {
        if (!this.listeners.isEmpty()) {
          final ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index0,
            index1);
          for (int i = 0; i < this.listeners.size(); i++) {
            final ListDataListener listener = this.listeners.get(i);
            try {
              listener.intervalAdded(e);
            } catch (final Throwable t) {
            }
          }
        }
      }
    });
  }

  protected void fireIntervalRemoved(final int index0, final int index1) {
    Invoke.later(() -> {
      synchronized (this.listeners) {
        if (!this.listeners.isEmpty()) {
          final ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index0,
            index1);
          for (int i = 0; i < this.listeners.size(); i++) {
            final ListDataListener listener = this.listeners.get(i);
            listener.intervalRemoved(e);
          }
        }
      }
    });
  }

  @Override
  public T getElementAt(final int index) {
    if (index >= 0) {
      return super.get(index);
    } else {
      return null;
    }
  }

  public List<T> getElements() {
    return Lists.toArray(this);
  }

  @Override
  public int getSize() {
    return size();
  }

  @Override
  public T remove(final int index) {
    final T oldValue = get(index);
    Invoke.andWait(() -> {
      super.remove(index);
      fireIntervalRemoved(index, index);
    });
    return oldValue;
  }

  @Override
  public boolean remove(final Object element) {
    final int index = indexOf(element);
    final T value = remove(index);
    return value != null;
  }

  @Override
  public boolean removeAll(final Collection<?> values) {
    boolean removed = false;
    for (final Object object : values) {
      removed |= remove(object);
    }
    return removed;
  }

  public boolean removeAll(final Object... values) {
    boolean removed = false;
    for (final Object object : values) {
      removed |= remove(object);
    }
    return removed;
  }

  @Override
  public void removeListDataListener(final ListDataListener listener) {
    synchronized (this.listeners) {
      this.listeners.remove(listener);
    }
  }

  @Override
  public void removeRange(final int fromIndex, final int toIndex) {
    if (fromIndex > toIndex) {
      throw new IllegalArgumentException("fromIndex must be <= toIndex");
    }
    Invoke.andWait(() -> {
      super.removeRange(fromIndex, toIndex);
      fireIntervalRemoved(fromIndex, toIndex);
    });
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
    Invoke.andWait(() -> {
      super.set(index, element);
      fireContentsChanged(index, index);
    });
    return oldValue;
  }

  public void setAll(final Iterable<? extends T> elements) {
    Invoke.andWait(() -> {
      if (Property.isEmpty(elements)) {
        clear();
      } else if (!DataType.equal(elements, this)) {
        final int oldSize = size();
        super.clear();
        super.addAll(Lists.toArray(elements));

        final int newSize = size();

        if (newSize > oldSize) {
          fireIntervalAdded(oldSize, newSize - 1);
        } else if (newSize < oldSize) {
          fireIntervalRemoved(newSize, oldSize - 1);
        }
        if (newSize > 0) {
          fireContentsChanged(0, newSize - 1);
        }
      }
    });
  }
}
