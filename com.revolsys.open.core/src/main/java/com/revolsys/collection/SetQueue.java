package com.revolsys.collection;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class SetQueue<E> extends AbstractQueue<E> {

  private final LinkedHashSet<E> set = new LinkedHashSet<E>();

  @Override
  public Iterator<E> iterator() {
    return set.iterator();
  }

  public boolean offer(final E o) {
    set.add(o);
    return true;
  }

  public E peek() {
    final Iterator<E> iterator = iterator();
    if (iterator.hasNext()) {
      final E value = iterator.next();
      return value;
    } else {
      return null;
    }
  }

  public E poll() {
    final Iterator<E> iterator = iterator();
    if (iterator.hasNext()) {
      final E value = iterator.next();
      iterator.remove();
      return value;
    } else {
      return null;
    }
  }

  @Override
  public int size() {
    return set.size();
  }
}
