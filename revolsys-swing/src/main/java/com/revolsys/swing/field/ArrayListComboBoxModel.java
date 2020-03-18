package com.revolsys.swing.field;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.MutableComboBoxModel;

import org.jeometry.common.data.type.DataType;

import com.revolsys.swing.list.ArrayListModel;

public class ArrayListComboBoxModel<T> extends ArrayListModel<T>
  implements MutableComboBoxModel<T>, Serializable {
  private static final long serialVersionUID = 1L;

  private T selectedObject;

  public ArrayListComboBoxModel() {
  }

  public ArrayListComboBoxModel(final Collection<T> elements) {
    super(elements);
    if (!isEmpty()) {
      this.selectedObject = getElementAt(0);
    }
  }

  public ArrayListComboBoxModel(@SuppressWarnings("unchecked") final T... elements) {
    this(Arrays.asList(elements));
  }

  @Override
  public void add(final int index, final T element) {
    super.add(index, element);
    if (this.selectedObject == null && element != null && !isEmpty()) {
      setSelectedItem(element);
    }
  }

  @Override
  public void addElement(final T element) {
    add(element);
  }

  @Override
  public void clear() {
    super.clear();
    this.selectedObject = null;
  }

  @Override
  public T getSelectedItem() {
    return this.selectedObject;
  }

  @Override
  public void insertElementAt(final T element, final int index) {
    add(index, element);
  }

  @Override
  public T remove(final int index) {
    final T oldValue = super.get(index);
    if (oldValue == this.selectedObject) {
      T selectedObject = this.selectedObject;
      if (index == 0) {
        if (getSize() == 1) {
          selectedObject = null;
        } else {
          selectedObject = get(index + 1);
        }
      } else {
        selectedObject = get(index - 1);
      }
      setSelectedItem(selectedObject);
    }

    return super.remove(index);
  }

  @Override
  public void removeElement(final Object element) {
    remove(element);
  }

  @Override
  public void removeElementAt(final int index) {
    remove(index);
  }

  @Override
  public T set(final int index, final T element) {
    final T oldValue = super.set(index, element);
    if (DataType.equal(oldValue, this.selectedObject)) {
      setSelectedItem(element);
    }
    return oldValue;
  }

  @Override
  public void setAll(final Iterable<? extends T> elements) {
    super.setAll(elements);
    if (!contains(this.selectedObject)) {
      if (isEmpty()) {
        setSelectedItem(null);
      } else {
        setSelectedItem(get(0));
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setSelectedItem(final Object element) {
    if (!DataType.equal(this.selectedObject, element)) {
      this.selectedObject = (T)element;
      if (!isEmpty()) {
        fireContentsChanged(0, size());
      }
    }
  }

}
