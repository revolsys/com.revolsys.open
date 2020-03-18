package com.revolsys.swing.builder;

import java.util.LinkedHashMap;
import java.util.Map;

public class UiBuilderRegistry {

  private static final StringUiBuilder DEFAULT_RENDERER = new StringUiBuilder();

  private static UiBuilderRegistry INSTANCE = new UiBuilderRegistry();

  public static UiBuilderRegistry getInstance() {
    return INSTANCE;
  }

  private final Map<Class<?>, UiBuilder> builders = new LinkedHashMap<>();

  public void addBuilder(final Class<?> clazz, final UiBuilder builder) {
    this.builders.put(clazz, builder);
    builder.setRegistry(this);
  }

  public UiBuilder getBuilder(final Class<?> clazz) {
    final UiBuilder renderer = getBuilderPrivate(clazz);
    if (renderer != null) {
      return renderer;
    } else {
      return DEFAULT_RENDERER;
    }
  }

  private UiBuilder getBuilderPrivate(final Class<?> clazz) {
    if (clazz != null) {
      UiBuilder renderer = this.builders.get(clazz);
      if (renderer == null) {
        final Class<?> superClass = clazz.getSuperclass();
        if (superClass != Object.class) {
          renderer = getBuilderPrivate(superClass);
        }
        final Class<?>[] interfaces = clazz.getInterfaces();
        for (int i = 0; i < interfaces.length && renderer == null; i++) {
          final Class<?> interfaceClass = interfaces[i];
          renderer = getBuilderPrivate(interfaceClass);
        }
      }
      return renderer;
    } else {
      return null;
    }
  }

  public String toHtml(final Object object) {
    if (object != null) {
      final Class<?> objectClass = object.getClass();
      final UiBuilder renderer = getBuilder(objectClass);
      final String html = renderer.toHtml(object);
      return html;
    } else {
      return null;
    }
  }
}
