package com.revolsys.jump.ui.swing.view;

import java.awt.Component;

import org.openjump.swing.factory.component.ComponentFactory;

import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.DockableState;
import com.javadocking.dockable.DockingMode;

public class FactoryDockable extends DefaultDockable {

  private ComponentFactory<Component> factory;

  public FactoryDockable(final ComponentFactory<Component> factory) {
    super(factory.getClass().getName(), factory.createComponent(),
      factory.getName(), factory.getIcon(), DockingMode.ALL);
    this.factory = factory;

    setPossibleStates(DockableState.NORMAL | DockableState.MINIMIZED
      | DockableState.EXTERNALIZED);
  }

  /**
   * @return the factory
   */
  public ComponentFactory<Component> getFactory() {
    return factory;
  }

}
