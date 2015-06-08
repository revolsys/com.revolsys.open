package com.revolsys.factory;

import java.util.ArrayList;
import java.util.List;

public class ArrayListFactory<V> implements Factory<List<V>> {

  @SuppressWarnings("rawtypes")
  private static final ArrayListFactory INSTANCE = new ArrayListFactory<>();

  @SuppressWarnings("unchecked")
  public static <V1> ArrayListFactory<V1> get() {
    return INSTANCE;
  }

  @Override
  public List<V> create() {
    return new ArrayList<>();
  }
}
