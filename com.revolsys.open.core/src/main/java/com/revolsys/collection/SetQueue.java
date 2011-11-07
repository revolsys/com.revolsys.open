package com.revolsys.collection;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class SetQueue<E> extends AbstractQueue<E> {

  private LinkedHashSet<E> set = new LinkedHashSet<E>();

  public boolean offer(E o) {
    set.add(o);
    return true;
  }

  public E peek() {
    Iterator<E> iterator = iterator();
    if (iterator.hasNext()) {
      E value = iterator.next();
      return value;
    } else {
      return null;
    }
  }

  public E poll() {
    Iterator<E> iterator = iterator();
    if (iterator.hasNext()) {
      E value = iterator.next();
      iterator.remove();
      return value;
    } else {
      return null;
    }
  }

  @Override
  public Iterator<E> iterator() {
    return set.iterator();
  }

  @Override
  public int size() {
    return set.size();
  }
}
