package com.revolsys.jump.ui.swing.view;

import java.awt.Component;

import net.infonode.docking.View;

import org.openjump.swing.factory.component.ComponentFactory;

@SuppressWarnings("serial")
public class FactoryView extends View {

  private ComponentFactory<Component> factory;

  public FactoryView(
    final ComponentFactory<Component> factory) {
    super(factory.getName(), factory.getIcon(), factory.createComponent());
    this.factory = factory;
  }

  /**
   * @return the factory
   */
  public ComponentFactory<Component> getFactory() {
    return factory;
  }

  
  public void finalize() {
  }
}
