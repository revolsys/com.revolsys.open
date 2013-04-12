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

  public void addComponent(final Component component) {
    addComponent("default", component);
  }

  public void addComponentFactory(final String groupName,
    final ComponentFactory<?> factory) {
    final List<ComponentFactory<?>> factories = getGroup(groupName);
    factories.add(factory);
  }

  public void addComponent(final String groupName, final Component component) {
    addComponentFactory(groupName, new ComponentComponentFactory(component));
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
    final ActionMainMenuItemFactory factory = new ActionMainMenuItemFactory(
      action);
    addComponentFactory(groupName, factory);
  }

  public void addMenuItem(final String groupName, final String name,
    final String title, final Icon icon, final Object object,
    final String methodName, final Object... parameters) {
    final InvokeMethodAction action = new InvokeMethodAction(name, title, icon,
      object, methodName, parameters);

    addComponentFactory(groupName, action);
  }

  public void addMenuItem(final String groupName, final String name,
    final String title, final Icon icon, EnableCheck enableCheck,
    final Object object, final String methodName, final Object... parameters) {
    final InvokeMethodAction action = new InvokeMethodAction(name, title, icon,
      object, methodName, parameters);
    action.setEnableCheck(enableCheck);
    addComponentFactory(groupName, action);
  }

  public void addMenuItemTitleIcon(final String groupName, final String title,
    final String iconName, final Object object, final String methodName,
    final Object... parameters) {
    final ImageIcon icon = SilkIconLoader.getIcon(iconName);
    addMenuItem(groupName, title, title, icon, object, methodName, parameters);
  }

  public void addMenuItemTitleIcon(final String groupName, final String title,
    final String iconName, EnableCheck enableCheck, final Object object,
    final String methodName, final Object... parameters) {
    final ImageIcon icon = SilkIconLoader.getIcon(iconName);
    addMenuItem(groupName, title, title, icon, enableCheck, object, methodName,
      parameters);
  }

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
          Component component = factory.createComponent();
          menu.add(component);
        }
      }
    }
    return menu;
  }

  public JPopupMenu createJPopupMenu() {
    final JPopupMenu menu = new JPopupMenu(name);
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
          Component component = factory.createComponent();
          menu.add(component);
        }
      }
    }
    return menu;
  }

  public List<ComponentFactory<?>> getGroup(final String groupName) {
    List<ComponentFactory<?>> factories = groups.get(groupName);
    if (factories == null) {
      factories = new ArrayList<ComponentFactory<?>>();
      groups.put(groupName, factories);
      groupNames.add(groupName);
    }
    return factories;
  }

  /*
   * public void setGroupEnabled(final String groupName, final boolean enabled)
   * { final List<Component> components = getGroup(groupName); for (final
   * Component component : components) { component.setEnabled(enabled); } }
   */

  @Override
  public void close(Component component) {
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getToolTip() {
    return null;
  }

  public void addCheckboxMenuItem(String groupName,
    Action action, EnableCheck itemChecked) {
    final ActionMainMenuItemFactory factory = new ActionMainMenuItemFactory(
      itemChecked, action);
    addComponentFactory(groupName, factory);
   
  }

  // public void setGroupEnabled(final String groupName, final boolean enabled)
  // {
  // groups.setGroupEnabled(groupName, enabled);
  // }

}
