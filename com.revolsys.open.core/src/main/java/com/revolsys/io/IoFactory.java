package com.revolsys.io;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.Available;

public interface IoFactory extends Available {
  static <C extends IoFactory> C factory(final Class<C> factoryClass, final Path path) {
    final IoFactoryRegistry registry = IoFactoryRegistry.getInstance();
    return registry.getFactory(factoryClass, path);
  }

  static <C extends IoFactory> C factory(final Class<C> factoryClass, final Resource resource) {
    final IoFactoryRegistry registry = IoFactoryRegistry.getInstance();
    return registry.getFactory(factoryClass, resource);
  }

  static <C extends IoFactory> C factory(final Class<C> factoryClass, final String fileName) {
    final IoFactoryRegistry registry = IoFactoryRegistry.getInstance();
    return registry.getFactoryByFileName(factoryClass, fileName);
  }

  static <C extends IoFactory> boolean hasFactory(final Class<C> factoryClass, final Path path) {
    final C factory = factory(factoryClass, path);
    return factory != null;
  }

  static <C extends IoFactory> boolean hasFactory(final Class<C> factoryClass,
    final Resource resource) {
    final C factory = factory(factoryClass, resource);
    return factory != null;
  }

  static <C extends IoFactory> boolean hasFactory(final Class<C> factoryClass,
    final String fileName) {
    final C factory = factory(factoryClass, fileName);
    return factory != null;
  }

  static <C extends IoFactory> boolean hasFactory(final Class<C> factoryClass, final URL url) {
    final String fileName = url.toString();
    return hasFactory(factoryClass, fileName);
  }

  String getFileExtension(String mediaType);

  List<String> getFileExtensions();

  String getMediaType(String fileExtension);

  Set<String> getMediaTypes();

  String getName();

  void init();

}
