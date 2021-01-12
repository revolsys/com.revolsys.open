package com.revolsys.swing.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.collection.LazyValueHolder;

public class LazyMenuFactory extends LazyValueHolder<MenuFactory> {

  private final String name;

  private final List<Consumer<MenuFactory>> initializers = new ArrayList<>();

  public LazyMenuFactory(final String name) {
    this.name = name;
    setValueSupplier(this::initMenus);
  }

  public synchronized void addInitializer(final Consumer<MenuFactory> initializer) {
    if (isInitialized()) {
      final MenuFactory menuFactory = getValue();
      initializer.accept(menuFactory);
    } else {
      this.initializers.add(initializer);
    }
  }

  private MenuFactory initMenus() {
    final MenuFactory menuFactory = new MenuFactory(this.name);
    for (final Consumer<MenuFactory> initializer : this.initializers) {
      initializer.accept(menuFactory);
    }
    this.initializers.clear();
    return menuFactory;
  }
}
