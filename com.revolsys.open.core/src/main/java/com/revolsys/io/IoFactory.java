package com.revolsys.io;

import java.util.List;
import java.util.Set;

import com.revolsys.record.Available;

public interface IoFactory extends Available {
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

  String getFileExtension(String mediaType);

  List<String> getFileExtensions();

  String getMediaType(String fileExtension);

  Set<String> getMediaTypes();

  String getName();

  void init();
}
