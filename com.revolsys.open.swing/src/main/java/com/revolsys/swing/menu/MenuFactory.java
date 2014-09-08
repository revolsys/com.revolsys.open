package com.revolsys.swing.menu;

import java.awt.Component;
import java.awt.Window;
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
import javax.swing.SwingUtilities;

import com.revolsys.beans.ClassRegistry;
import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.ComponentFactory;

public class MenuFactory extends AbstractObjectWithProperties implements
ComponentFactory<JMenuItem> {

  public static MenuFactory findMenu(final Class<?> clazz) {
    synchronized (CLASS_MENUS) {
      return CLASS_MENUS.find(clazz);
    }
  }

  public static MenuFactory findMenu(final Object object) {
    final Class<?> clazz = object.getClass();
    return findMenu(clazz);
  }

  public static MenuFactory getMenu(final Class<?> layerClass) {
    if (layerClass == null) {
      return new MenuFactory();
    } else {
      synchronized (CLASS_MENUS) {
        MenuFactory menu = CLASS_MENUS.get(layerClass);
        if (menu == null) {
          final Class<?> superClass = layerClass.getSuperclass();
          final MenuFactory parentMenu = getMenu(superClass);
          menu = parentMenu.clone();
          CLASS_MENUS.put(layerClass, menu);
        }
        return menu;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static <V> V getMenuSource() {
    return (V)menuSource;
  }

  public static void setMenuSource(final Object menuSource) {
    MenuFactory.menuSource = menuSource;
  }

  public static void showMenu(final Object object, final Component component,
    final int x, final int y) {
    if (object != null) {
      final MenuFactory menu = findMenu(object);
      if (menu != null) {
        menu.show(object, component, x, y);
      }
    }
  }

  private static Object menuSource;

  private final Map<String, List<ComponentFactory<?>>> groups = new HashMap<String, List<ComponentFactory<?>>>();

  private final List<String> groupNames = new ArrayList<String>();

  private String name;

  private static final ClassRegistry<MenuFactory> CLASS_MENUS = new ClassRegistry<MenuFactory>();

  public MenuFactory() {
  }

  public MenuFactory(final String name) {
    this.name = name;
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
    if (!this.groupNames.contains(groupName)) {
      this.groupNames.add(index, groupName);
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
    for (final String groupName : this.groupNames) {
      for (final ComponentFactory<?> factory : this.groups.get(groupName)) {
        final ComponentFactory<?> cloneFactory = factory.clone();
        clone.addComponentFactory(groupName, cloneFactory);
      }
    }
    return clone;
  }

  @Override
  public void close(final Component component) {
  }

  @Override
  public JMenu createComponent() {
    final JMenu menu = new JMenu(this.name);
    boolean first = true;
    for (final String groupName : this.groupNames) {
      final List<ComponentFactory<?>> factories = this.groups.get(groupName);
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

  /*
   * public void setGroupEnabled(final String groupName, final boolean enabled)
   * { final List<Component> components = getGroup(groupName); for (final
   * Component component : components) { component.setEnabled(enabled); } }
   */

  public JPopupMenu createJPopupMenu() {
    final JPopupMenu menu = new JPopupMenu(this.name);
    boolean first = true;
    for (final String groupName : this.groupNames) {
      boolean groupHasItem = false;
      final List<ComponentFactory<?>> factories = this.groups.get(groupName);
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

  public void deleteMenuItem(final String groupName, final String menuTitle) {
    final List<ComponentFactory<?>> items = this.groups.get(groupName);
    if (items != null) {
      for (final ComponentFactory<?> item : items) {
        boolean delete = false;
        if (item instanceof MenuFactory) {
          final MenuFactory menuFactory = (MenuFactory)item;
          if (menuTitle.equals(menuFactory.getName())) {
            delete = true;
          }
        } else if (item instanceof ActionMainMenuItemFactory) {
          final ActionMainMenuItemFactory actionFactory = (ActionMainMenuItemFactory)item;
          if (menuTitle.equals(actionFactory.getName())) {
            delete = true;
          }
        } else if (item instanceof Action) {
          final Action action = (Action)item;
          if (menuTitle.equals(action.getValue(Action.NAME))) {
            delete = true;
          }
        }
        if (delete) {
          items.remove(item);
          return;
        }
      }
    }
  }

  public MenuFactory getFactory(final String name) {
    for (final List<ComponentFactory<?>> group : this.groups.values()) {
      for (final ComponentFactory<?> factory : group) {
        if (factory instanceof MenuFactory) {
          final MenuFactory menuFactory = (MenuFactory)factory;
          final String factoryName = menuFactory.getName();
          if (name.equals(factoryName)) {
            return menuFactory;
          }
        }
      }
    }
    return null;
  }

  public List<ComponentFactory<?>> getGroup(final String groupName) {
    List<ComponentFactory<?>> factories = this.groups.get(groupName);
    if (factories == null) {
      factories = new ArrayList<ComponentFactory<?>>();
      this.groups.put(groupName, factories);
      if (!this.groupNames.contains(groupName)) {
        this.groupNames.add(groupName);
      }
    }
    return factories;
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  public int getItemCount() {
    int count = 0;
    for (final List<ComponentFactory<?>> factories : this.groups.values()) {
      count += factories.size();
    }
    return count;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getToolTip() {
    return null;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void show(final Object source, final Component component, final int x,
    final int y) {
    setMenuSource(source);
    final JPopupMenu menu = createJPopupMenu();
    if (menu != null) {
      final int numItems = menu.getSubElements().length;
      if (menu != null && numItems > 0) {
        final Window window = SwingUtilities.windowForComponent(component);
        if (window != null) {
          if (window.isAlwaysOnTop()) {
            window.setAlwaysOnTop(true);
            window.setAlwaysOnTop(false);
          }
          window.toFront();
        }
        menu.show(component, x, y);
      }
    }
  }
}
