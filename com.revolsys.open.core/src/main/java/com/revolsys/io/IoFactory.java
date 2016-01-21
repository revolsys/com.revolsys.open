package com.revolsys.io;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.Maps;
import com.revolsys.io.file.Paths;
import com.revolsys.record.Available;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;
import com.revolsys.util.UrlUtil;

public interface IoFactory extends Available {
  @SuppressWarnings("unchecked")
  static <C extends IoFactory> List<C> factories(final Class<C> factoryClass) {
    return Lists.<C> array((Set<C>)IoFactoryRegistry.factoriesByClass.get(factoryClass));
  }

  /**
  * Get the {@link IoFactory} for the given source. The source can be one of the following
  * classes.
  *
  * <ul>
  *   <li>{@link PathUtil}</li>
  *   <li>{@link File}</li>
  *   <li>{@link Resource}</li>
  * </ul>
  * @param factoryClass The class or interface to get the factory for.
  * @param source The source to create the factory for.
  * @return The factory.
  * @throws IllegalArgumentException If the source is not a supported class.
  */
  static <C extends IoFactory> C factory(final Class<C> factoryClass, final Object source) {
    final String fileName = fileName(source);
    return factoryByFileName(factoryClass, fileName);
  }

  @SuppressWarnings("unchecked")
  static <F extends IoFactory> F factoryByFileExtension(final Class<F> factoryClass,
    String fileExtension) {
    fileExtension = fileExtension.toLowerCase();
    if (Property.hasValue(fileExtension)) {
      return (F)Maps.getMap(IoFactoryRegistry.factoryByClassAndFileExtension, factoryClass,
        fileExtension);
    }
    return null;
  }

  static <C extends IoFactory> C factoryByFileName(final Class<C> factoryClass,
    final String fileName) {
    for (final String fileExtension : FileUtil.getFileNameExtensions(fileName)) {
      final C factory = factoryByFileExtension(factoryClass, fileExtension);
      if (factory != null) {
        return factory;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static <F extends IoFactory> F factoryByMediaType(final Class<F> factoryClass,
    final String mediaType) {
    if (Property.hasValue(mediaType)) {
      if (mediaType.contains("/")) {
        return (F)Maps.getMap(IoFactoryRegistry.factoryByClassAndMediaType, factoryClass,
          mediaType);
      } else {
        return factoryByFileExtension(factoryClass, mediaType);
      }
    }
    return null;
  }

  static String fileExtensionByMediaType(final String mediaType) {
    final RecordWriterFactory writerFactory = factoryByMediaType(RecordWriterFactory.class,
      mediaType);
    if (writerFactory == null) {
      return null;
    } else {
      return writerFactory.getFileExtension(mediaType);
    }
  }

  static List<String> fileExtensions(final Class<? extends IoFactory> factoryClass) {
    return Lists.array(IoFactoryRegistry.fileExtensionsByClass.get(factoryClass));
  }

  public static String fileName(final Object source) {
    String fileName = null;
    if (Property.hasValue(source)) {
      if (source instanceof Resource) {
        fileName = ((Resource)source).getFilename();
      } else if (source instanceof Path) {
        fileName = Paths.getFileName((Path)source);
      } else if (source instanceof File) {
        fileName = FileUtil.getFileName((File)source);
      } else if (source instanceof URL) {
        fileName = UrlUtil.getFileName((URL)source);
      } else if (source instanceof URI) {
        fileName = UrlUtil.getFileName((URI)source);
      } else if (source instanceof String) {
        fileName = FileUtil.getFileName((String)source);
      } else {
        throw new IllegalArgumentException(source.getClass() + " is not supported");
      }
    }
    return fileName;
  }

  static <C extends IoFactory> boolean hasFactory(final Class<C> factoryClass,
    final Object source) {
    final C factory = factory(factoryClass, source);
    return factory != null;
  }

  static <F extends IoFactory> boolean isAvailable(final Class<F> factoryClass,
    final Object source) {
    if (factoryClass != null) {
      final List<String> fileExtensions = fileExtensions(factoryClass);
      if (Property.hasValue(fileExtensions)) {
        try {
          final String fileName = fileName(source);
          for (final String fileExtension : FileUtil.getFileNameExtensions(fileName)) {
            if (Property.hasValue(fileExtension)
              && fileExtensions.contains(fileExtension.toLowerCase())) {
              return true;
            }
          }
        } catch (final IllegalArgumentException e) {
        }
      }
    }
    return false;
  }

  static Map<String, String> mediaTypeByFileExtension() {
    return new HashMap<>(IoFactoryRegistry.mediaTypeByFileExtension);
  }

  static <F extends IoFactory> List<String> mediaTypes(final Class<F> factoryClass) {
    return Lists.array(IoFactoryRegistry.mediaTypesByClass.get(factoryClass));
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
}
