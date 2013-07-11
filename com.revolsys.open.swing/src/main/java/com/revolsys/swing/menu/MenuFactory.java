package com.revolsys.swing.menu;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.ComponentFactory;

public class MenuFactory implements ComponentFactory<JMenuItem> {

  private final Map<String, List<ComponentFactory<?>>> groups = new HashMap<String, List<ComponentFactory<?>>>();

  private final List<String> groupNames = new ArrayList<String>();

  private String name;

  public MenuFactory() {
  }

  public void addCheckboxMenuItem(final String groupName, final Action action,
    final EnableCheck itemChecked) {
    final ActionMainMenuItemFactory factory = new ActionMainMenuItemFactory(
      itemChecked, action);
    addComponentFactory(groupName, factory);

  }

  public void addComponent(final Component component) {
    addComponent("default", component);
  }

  public void addComponent(final String groupName, final Component component) {
    addComponentFactory(groupName, new ComponentComponentFactory(component));
  }

  public void addComponentFactory(final String groupName,
    final ComponentFactory<?> factory) {
    final List<ComponentFactory<?>> factories = getGroup(groupName);
    factories.add(factory);
  }

  public void addComponentFactory(final String groupName, final int index,
    final ComponentFactory<?> factory) {
    final List<ComponentFactory<?>> factories = getGroup(groupName);
    factories.add(index, factory);
  }

  public void addGroup(final int index, final String groupName) {
    if (!groupNames.contains(groupName)) {
      groupNames.add(index, groupName);
    }

  }

  public void addGroup(final String groupName) {
    getGroup(groupName);
  }

  public void addMenuItem(final Action action) {
    addMenuItem("default", action);
  }

  public JMenuItem addMenuItem(final JMenuItem menuItem) {
    addComponent(menuItem);
    return menuItem;
  }

  public JMenuItem addMenuItem(final String title) {
    final JMenuItem menuItem = new JMenuItem(title);
    addComponent("default", menuItem);
    return menuItem;
  }

  public void addMenuItem(final String groupName, final Action action) {
    if (action == this) {
      throw new RuntimeException("Cannot recursively add");
    }
    final ActionMainMenuItemFactory factory = new ActionMainMenuItemFactory(
      action);
    addComponentFactory(groupName, factory);
  }

  public void addMenuItem(final String groupName, final int index,
    final Action action) {
    if (action == this) {
      throw new RuntimeException("Cannot recursively add");
    }
    final ActionMainMenuItemFactory factory = new ActionMainMenuItemFactory(
      action);
    addComponentFactory(groupName, index, factory);
  }

  public void addMenuItem(final String groupName, final int index,
    final String title, final String iconName, final EnableCheck enableCheck,
    final Object object, final String methodName, final Object... parameters) {
    final ImageIcon icon = SilkIconLoader.getIcon(iconName);
    final InvokeMethodAction menuItem = createMenuItem(title, title, icon,
      enableCheck, object, methodName, parameters);
    addComponentFactory(groupName, index, menuItem);
  }

  public void addMenuItem(final String groupName, final int index,
    final String title, final String iconName, final Object object,
    final String methodName, final Object... parameters) {
    final ImageIcon icon = SilkIconLoader.getIcon(iconName);
    final InvokeMethodAction menuItem = createMenuItem(title, title, icon,
      null, object, methodName, parameters);
    addComponentFactory(groupName, index, menuItem);
  }

  public void addMenuItem(final String groupName, final String name,
    final String title, final Icon icon, final EnableCheck enableCheck,
    final Object object, final String methodName, final Object... parameters) {
    final InvokeMethodAction action = createMenuItem(name, title, icon,
      enableCheck, object, methodName, parameters);
    addComponentFactory(groupName, action);
  }

  public void addMenuItem(final String groupName, final String name,
    final String title, final Icon icon, final Object object,
    final String methodName, final Object... parameters) {
    final InvokeMethodAction action = new InvokeMethodAction(name, title, icon,
      object, methodName, parameters);

    addComponentFactory(groupName, action);
  }

  public void addMenuItemTitleIcon(final String groupName, final String title,
    final String iconName, final EnableCheck enableCheck, final Object object,
    final String methodName, final Object... parameters) {
    final ImageIcon icon = SilkIconLoader.getIcon(iconName);
    addMenuItem(groupName, title, title, icon, enableCheck, object, methodName,
      parameters);
  }

  public void addMenuItemTitleIcon(final String groupName, final String title,
    final String iconName, final Object object, final String methodName,
    final Object... parameters) {
    final ImageIcon icon = SilkIconLoader.getIcon(iconName);
    final InvokeMethodAction menuItem = createMenuItem(title, title, icon,
      null, object, methodName, parameters);
    addComponentFactory(groupName, menuItem);
  }

  @Override
  public MenuFactory clone() {
    final MenuFactory clone = new MenuFactory();
    for (final String groupName : groupNames) {
      for (final ComponentFactory<?> factory : groups.get(groupName)) {
        final ComponentFactory<?> cloneFactory = factory.clone();
        clone.addComponentFactory(groupName, cloneFactory);
      }
    }
    return clone;
  }

  @Override
  public void close(final Component component) {
  }

  /*
   * public void setGroupEnabled(final String groupName, final boolean enabled)
   * { final List<Component> components = getGroup(groupName); for (final
   * Component component : components) { component.setEnabled(enabled); } }
   */

  @Override
  public JMenu createComponent() {
    final JMenu menu = new JMenu(name);
    boolean first = true;
    for (final String groupName : groupNames) {
      final List<ComponentFactory<?>> factories = groups.get(groupName);
      if (!factories.isEmpty()) {
        if (first) {
          first = false;
        } else {
          menu.addSeparator();
        }
        for (final ComponentFactory<?> factory : factories) {
          final Component component = factory.createComponent();
          if (component != null) {
            menu.add(component);
          }
        }
      }
    }
    return menu;
  }

  public JPopupMenu createJPopupMenu() {
    final JPopupMenu menu = new JPopupMenu(name);
    boolean first = true;
    for (final String groupName : groupNames) {
      boolean groupHasItem = false;
      final List<ComponentFactory<?>> factories = groups.get(groupName);
      if (!factories.isEmpty()) {

        for (final ComponentFactory<?> factory : factories) {

          final Component component = factory.createComponent();
          if (component != null) {
            if (!groupHasItem) {
              groupHasItem = true;
              if (first) {
                first = false;
              } else {
                menu.addSeparator();
              }
            }
            menu.add(component);
          }
        }
      }
    }
    return menu;
  }

  public InvokeMethodAction createMenuItem(final String name,
    final String title, final Icon icon, final EnableCheck enableCheck,
    final Object object, final String methodName, final Object... parameters) {
    final InvokeMethodAction action = new InvokeMethodAction(name, title, icon,
      object, methodName, parameters);
    action.setEnableCheck(enableCheck);
    return action;
  }

  public List<ComponentFactory<?>> getGroup(final String groupName) {
    List<ComponentFactory<?>> factories = groups.get(groupName);
    if (factories == null) {
      factories = new ArrayList<ComponentFactory<?>>();
      groups.put(groupName, factories);
      if (!groupNames.contains(groupName)) {
        groupNames.add(groupName);
      }
    }
    return factories;
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getToolTip() {
    return null;
  }
}
