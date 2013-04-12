package com.revolsys.collection.bplus;

import java.util.AbstractList;
import java.util.Map;

import org.springframework.util.comparator.ComparableComparator;

import com.revolsys.io.page.PageManager;
import com.revolsys.io.page.PageValueManager;

public class BPlusTreeList<T> extends AbstractList<T> {

  public static <T> BPlusTreeList<T> create(final PageManager pageManager,
    final PageValueManager<T> valueManager) {
    return new BPlusTreeList<T>(pageManager, valueManager);
  }

  private final Map<Integer, T> tree;

  int size = 0;

  public BPlusTreeList(final PageManager pageManager,
    final PageValueManager<T> valueSerializer) {
    final ComparableComparator<Integer> comparator = new ComparableComparator<Integer>();
    tree = BPlusTreeMap.create(pageManager, comparator, PageValueManager.INT,
      valueSerializer);
  }

  @Override
  public void add(final int index, final T value) {
    if (index < 0) {
      throw new IndexOutOfBoundsException("Index must be > 0 not " + index);
    } else if (index > size()) {
      throw new IndexOutOfBoundsException("Index must be <= " + size()
        + " not " + index);
    } else {
      if (index < size) {
        for (int i = size; size > index; i--) {
          final T oldValue = get(i - 1);
          tree.put(i, oldValue);
        }
      }
      tree.put(index, value);
    }
    size++;
  }

  @Override
  public T get(final int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException("Index must be > 0 not " + index);
    } else if (index >= size()) {
      throw new IndexOutOfBoundsException("Index must be < " + size() + " not "
        + index);
    } else {
      return tree.get(index);
    }
  }

  @Override
  public T set(final int index, final T value) {
    if (index < 0) {
      throw new IndexOutOfBoundsException("Index must be > 0 not " + index);
    } else if (index >= size()) {
      throw new IndexOutOfBoundsException("Index must be < " + size() + " not "
        + index);
    } else {
      final T oldValue = tree.get(index);
      tree.put(index, value);
      return oldValue;
    }
  }

  @Override
  public int size() {
    return size;
  }

}
