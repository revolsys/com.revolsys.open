package com.revolsys.collection.set;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.factory.Factory;

public class Sets {
  @SafeVarargs
  public static <V> void addAll(final Set<V> set, final Collection<? extends V>... collections) {
    for (final Collection<? extends V> collection : collections) {
      if (collection != null) {
        set.addAll(collection);
      }
    }
  }

  public static <V> void addAll(final Set<V> set, final Iterable<? extends V> values) {
    if (set != null && values != null) {
      for (final V value : values) {
        set.add(value);
      }
    }
  }

  @SafeVarargs
  public static <V> Set<V> all(final Factory<Set<V>> factory,
    final Collection<? extends V>... collections) {
    final Set<V> set = factory.create();
    addAll(set, collections);
    return set;
  }

  public static <V> HashSet<V> hash(final Iterable<V> values) {
    final HashSet<V> set = new HashSet<>();
    for (final V value : values) {
      set.add(value);
    }
    return set;
  }

  public static <V> HashSet<V> hash(@SuppressWarnings("unchecked") final V... values) {
    final HashSet<V> set = new HashSet<>();
    for (final V value : values) {
      set.add(value);
    }
    return set;
  }

  public static <V> LinkedHashSet<V> linkedHash(final Iterable<V> values) {
    final LinkedHashSet<V> set = new LinkedHashSet<>();
    addAll(set, values);
    return set;
  }

  public static <V> LinkedHashSet<V> linkedHash(@SuppressWarnings("unchecked") final V... values) {
    final LinkedHashSet<V> set = new LinkedHashSet<>();
    for (final V value : values) {
      set.add(value);
    }
    return set;
  }

  public static <V> LinkedHashSet<V> linkedHash(final V value) {
    final LinkedHashSet<V> set = new LinkedHashSet<>();
    if (value != null) {
      set.add(value);
    }
    return set;
  }

  public static <V> TreeSet<V> tree(final V value) {
    final TreeSet<V> set = new TreeSet<>();
    if (value != null) {
      set.add(value);
    }
    return set;
  }

  @SafeVarargs
  public static <V> TreeSet<V> treeAll(final Collection<? extends V>... collections) {
    final TreeSet<V> set = new TreeSet<>();
    addAll(set, collections);
    return set;
  }
}
