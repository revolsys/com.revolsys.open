package com.revolsys.jump.ui.swing;

import java.util.List;

import javax.swing.Icon;

import org.openjump.swing.factory.component.ComponentFactory;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.registry.Registry;

/**
 * Utility class to manage the list of {@link ComponentFactory} in the
 * {@link Registry} for the classification.
 * 
 * @author Paul Austin
 * @see ComponentFactory
 */
public final class ComponentFactoryRegistry {
  private ComponentFactoryRegistry() {
  }

  /**
   * Add a new component factory for the component class to the registry for the
   * classification.
   * 
   * @param context The workbench context that contains the registry.
   * @param classification The classification to add the factory to.
   * @param componentClass The component class.
   * @see WorkbenchContextComponentFactory
   */
  public static void addComponentFactory(final WorkbenchContext context,
    final String classification, final Class<?> componentClass, final String name, final Icon icon,
    final String toolTip) {
    WorkbenchContextComponentFactory factory = new WorkbenchContextComponentFactory(
      componentClass, context, name, icon, toolTip);
    addComponentFactory(context, classification, factory);
  }

  /**
   * Add the component factory to the registry for the classification.
   * 
   * @param context The workbench context that contains the registry.
   * @param classification The classification to add the factory to.
   * @param factory The factory that creates the components.
   */
  public static void addComponentFactory(final WorkbenchContext context,
    final  String classification, final ComponentFactory<?> factory) {
    Registry registry = context.getRegistry();
    registry.createEntry(classification, factory);
  }

  /**
   * Get the component factories for the classification from the registry.
   * 
   * @param context The workbench context that contains the registry.
   * @param classification The classification to add the factory to.
   * @return The list of component factories
   * @see ComponentFactory
   */
  @SuppressWarnings("unchecked")
  public static List<ComponentFactory<?>> getComponentFactories(
    final WorkbenchContext context, final String classification) {
    Registry registry = context.getRegistry();

    List<ComponentFactory<?>> factories = registry.getEntries(classification);
    return factories;
  }

}
