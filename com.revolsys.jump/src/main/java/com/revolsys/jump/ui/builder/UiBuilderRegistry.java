package com.revolsys.jump.ui.builder;

import java.util.LinkedHashMap;
import java.util.Map;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.WorkbenchContext;

public class UiBuilderRegistry {
  private static final String BLACKBOARD_KEY = UiBuilderRegistry.class.getName();

  private static final StringUiBuilder DEFAULT_RENDERER = new StringUiBuilder();

  private Map<Class<?>, UiBuilder> builders = new LinkedHashMap<Class<?>, UiBuilder>();

  public static UiBuilderRegistry getInstance(final WorkbenchContext context) {
    Blackboard blackboard = context.getBlackboard();
    UiBuilderRegistry repository = (UiBuilderRegistry)blackboard.get(BLACKBOARD_KEY);
    if (repository == null) {
      repository = new UiBuilderRegistry();
      blackboard.put(BLACKBOARD_KEY, repository);
    }
    return repository;
  }

  public void addBuilder(final Class<?> clazz, final UiBuilder builder) {
    builders.put(clazz, builder);
    builder.setRegistry(this);
  }

  public UiBuilder getBuilder(final Class<?> clazz) {
    UiBuilder renderer = getBuilderPrivate(clazz);
    if (renderer != null) {
      return renderer;
    } else {
      return DEFAULT_RENDERER;
    }
  }

  private UiBuilder getBuilderPrivate(final Class<?> clazz) {
    if (clazz != null) {
      UiBuilder renderer = builders.get(clazz);
      if (renderer == null) {
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != Object.class) {
          renderer = getBuilderPrivate(superClass);
        }
        Class<?>[] interfaces = clazz.getInterfaces();
        for (int i = 0; i < interfaces.length && renderer == null; i++) {
          Class<?> interfaceClass = interfaces[i];
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
      Class<?> objectClass = object.getClass();
      UiBuilder renderer = getBuilder(objectClass);
      String html = renderer.toHtml(object);
      return html;
    } else {
      return null;
    }
  }
}
