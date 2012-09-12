package com.revolsys.collection;

import java.lang.ref.Reference;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

public class ReferenceEntryIterator<K, V> extends AbstractIterator<Entry<K, V>> {
  private Iterator<Entry<K, Reference<V>>> iterator;

  public ReferenceEntryIterator(Collection<Entry<K, Reference<V>>> collection) {
    this.iterator = collection.iterator();
  }

  @Override
  protected Entry<K, V> getNext() throws NoSuchElementException {
    while (iterator.hasNext()) {
      Entry<K, Reference<V>> entry = iterator.next();
      K key = entry.getKey();
      Reference<V> reference = entry.getValue();
      if (reference != null) {
        V value = reference.get();
        if (value != null) {
          return new SimpleImmutableEntry<K,V>(key,value);
        }
      }
    }
    throw new NoSuchElementException();
  }
}
