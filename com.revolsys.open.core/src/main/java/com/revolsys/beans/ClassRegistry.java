package com.revolsys.beans;

import java.util.HashMap;
import java.util.Map;

public class ClassRegistry<T> {
  /** The registry of classes to values. */
  private final Map<Class<?>, T> registry = new HashMap<Class<?>, T>();

  /** The cache for super class matches. */
  private final Map<Class<?>, T> findCache = new HashMap<Class<?>, T>();

  /**
   * Clear the cache used by the {@link ClassRegistry#find(Class)} method.
   */
  private void clearFindCache() {
    findCache.clear();
  }

  /**
   * Find the value by class. If no direct match was found, a match for the
   * super class will be found until a match is found. Returns null if no match
   * was found on any super class.
   * 
   * @param clazz The class.
   * @return The class if a match was found for this class or one of the super
   *         classes or null if no match was found.
   */
  public T find(final Class<?> clazz) {
    if (clazz == null) {
      return null;
    } else {
      T value = get(clazz);
      if (value == null) {
        value = findCache.get(clazz);
        if (value == null) {
          final Class<?> superClass = clazz.getSuperclass();
          value = find(superClass);
          if (value != null) {
            findCache.put(clazz, value);
          }
        }
      }
      return value;
    }
  }

  /**
   * Get the value from the registry using the key. Returns null if an exact
   * match by class is not found.
   * 
   * @param clazz The class.
   * @return The value, or null if no value has been registered for this class.
   */
  public T get(final Class<?> clazz) {
    return registry.get(clazz);
  }

  /**
   * Register the value for the specified class.
   * 
   * @param clazz The class.
   * @param value The value.
   */
  public void put(final Class<?> clazz, final T value) {
    if (get(clazz) != value) {
      registry.put(clazz, value);
      clearFindCache();
    }
  }
}
