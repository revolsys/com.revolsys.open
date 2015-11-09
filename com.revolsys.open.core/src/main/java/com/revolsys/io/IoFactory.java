package com.revolsys.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.revolsys.record.Available;

public interface IoFactory extends Available {
  static <C extends IoFactory> List<C> factories(final Class<C> factoryClass) {
    final IoFactoryRegistry registry = IoFactoryRegistry.getInstance();
    return new ArrayList<>(registry.getFactories(factoryClass));
  }

  /**
   * Get the {@link IoFactory} for the given source.
   * @param factoryClass The class or interface to get the factory for.
   * @param source The source to create the factory for.
   * @return The factory.
   * @throws IllegalArgumentException If the source is not a supported class.
   */
  static <C extends IoFactory> C factory(final Class<C> factoryClass, final Object source) {
    final IoFactoryRegistry registry = IoFactoryRegistry.getInstance();
    return registry.getFactory(factoryClass, source);
  }

  static <C extends IoFactory> boolean hasFactory(final Class<C> factoryClass,
    final Object source) {
    final C factory = factory(factoryClass, source);
    return factory != null;
  }

  default String getFileExtension(final String mediaType) {
    return null;
  }

  default List<String> getFileExtensions() {
    return Collections.emptyList();
  }

  default String getMediaType(final String fileExtension) {
    return null;
  }

  default Set<String> getMediaTypes() {
    return Collections.emptySet();
  }

  String getName();

  default void init() {
  }

  static <F extends IoFactory> boolean isAvailable(final Class<F> factoryClass,
    final Object source) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    return ioFactoryRegistry.isFileExtensionSupported(factoryClass, source);
  }
}
