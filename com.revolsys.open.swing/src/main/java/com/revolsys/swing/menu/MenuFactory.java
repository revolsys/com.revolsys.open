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
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.swing.Icons;
import com.revolsys.swing.action.AbstractAction;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.ComponentFactory;
import com.revolsys.util.Property;

public class MenuFactory extends BaseObjectWithProperties implements ComponentFactory<JMenuItem> {

  private static final ClassRegistry<MenuFactory> CLASS_MENUS = new ClassRegistry<>();

  private static Object menuSource;

  public static MenuFactory findMenu(final Class<?> clazz) {
    synchronized (CLASS_MENUS) {
      return CLASS_MENUS.find(clazz);
    }
  }

  public static MenuFactory findMenu(final Object object) {
    final Class<?> clazz = object.getClass();
    return findMenu(clazz);
  }

  public static MenuFactory getMenu(final Class<?> clazz) {
    if (clazz == null) {
      return new MenuFactory();
    } else {
      synchronized (CLASS_MENUS) {
        MenuFactory menu = CLASS_MENUS.get(clazz);
        if (menu == null) {
          final Class<?> superClass = clazz.getSuperclass();
          final MenuFactory parentMenu = getMenu(superClass);
          menu = new MenuFactory(clazz.getName(), parentMenu);
          CLASS_MENUS.put(clazz, menu);
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

  public static void showMenu(final JPopupMenu menu, final Component component, final int x,
    final int y) {
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
        menu.validate();
        menu.show(component, x, y);
      }
    }
  }

  public static void showMenu(final Object object, final Component component, final int x,
    final int y) {
    if (object != null) {
      final MenuFactory menu = findMenu(object);
      if (menu != null) {
        menu.show(object, component, x, y);
      }
    }
  }

  private EnableCheck enableCheck;

  private final List<String> groupNames = new ArrayList<String>();

  private final Map<String, List<ComponentFactory<?>>> groups = new HashMap<>();

  private String name;

  public MenuFactory() {
  }

  public MenuFactory(final MenuFactory menuFactory) {
    this(null, menuFactory);
  }

  public MenuFactory(final String name) {
    this(name, null);
  }

  public MenuFactory(final String name, final MenuFactory menuFactory) {
    this.name = name;
    if (menuFactory != null) {
      for (final String groupName : menuFactory.getGroupNames()) {
        for (final ComponentFactory<?> factory : menuFactory.getGroup(groupName)) {
          final ComponentFactory<?> cloneFactory = factory.clone();
          addComponentFactory(groupName, cloneFactory);
        }
      }
    }
  }

  public void addCheckboxMenuItem(final String groupName, final AbstractAction action,
    final EnableCheck itemChecked) {
    final ActionMainMenuItemFactory factory = new ActionMainMenuItemFactory(itemChecked, action);
    addComponentFactory(groupName, factory);

  }

  public void addComponent(final Component component) {
    addComponent("default", component);
  }

  public void addComponent(final String groupName, final Component component) {
    addComponentFactory(groupName, new ComponentComponentFactory(component));
  }

  public void addComponentFactory(final String groupName, final ComponentFactory<?> factory) {
    final List<ComponentFactory<?>> factories = getGroup(groupName);
    factories.add(factory);
  }

  public void addComponentFactory(final String groupName, int index,
    final ComponentFactory<?> factory) {
    final List<ComponentFactory<?>> factories = getGroup(groupName);
    if (index < 0) {
      index = factories.size();
    }
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

  public void addMenuItem(final AbstractAction action) {
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

  public void addMenuItem(final String groupName, final AbstractAction action) {
    final ActionMainMenuItemFactory factory = new ActionMainMenuItemFactory(action);
    addComponentFactory(groupName, factory);
  }

  public void addMenuItem(final String groupName, final int index, final AbstractAction action) {
    final ActionMainMenuItemFactory factory = new ActionMainMenuItemFactory(action);
    addComponentFactory(groupName, index, factory);
  }

  public void addMenuItem(final String groupName, final int index, final String title,
    final String iconName, final Runnable runnable) {
    final ImageIcon icon = Icons.getIcon(iconName);
    final RunnableAction menuItem = createMenuItem(title, title, icon, null, runnable);
    addComponentFactory(groupName, index, menuItem);
  }

  public void addMenuItem(final String groupName, final String title, final String iconName,
    final Runnable runnable) {
    final ImageIcon icon = Icons.getIcon(iconName);
    final RunnableAction menuItem = createMenuItem(title, title, icon, null, runnable);
    addComponentFactory(groupName, menuItem);
  }

  public void addMenuItem(final String groupName, final String title, final String toolTip,
    final String iconName, final Runnable runnable) {
    final ImageIcon icon = Icons.getIcon(iconName);
    final RunnableAction menuItem = createMenuItem(title, toolTip, icon, null, runnable);
    addComponentFactory(groupName, menuItem);
  }

  public RunnableAction addMenuItemTitleIcon(final String groupName, final CharSequence name,
    final String iconName, final EnableCheck enableCheck, final Runnable runnable) {
    final ImageIcon icon = Icons.getIcon(iconName);
    final RunnableAction action = createMenuItem(name, null, icon, enableCheck, runnable);
    addComponentFactory(groupName, action);
    return action;
  }

  public RunnableAction addMenuItemTitleIcon(final String groupName, final CharSequence name,
    final String iconName, final Runnable runnable) {
    final ImageIcon icon = Icons.getIcon(iconName);
    final RunnableAction action = createMenuItem(name, null, icon, null, runnable);
    addComponentFactory(groupName, action);
    return action;
  }

  public RunnableAction addMenuItemTitleIcon(final String groupName, final int index,
    final CharSequence name, final String iconName, final EnableCheck enableCheck,
    final Runnable runnable) {
    final ImageIcon icon = Icons.getIcon(iconName);
    final RunnableAction action = createMenuItem(name, null, icon, enableCheck, runnable);
    addComponentFactory(groupName, index, action);
    return action;
  }

  @Override
  public MenuFactory clone() {
    final MenuFactory clone = new MenuFactory(this);

    return clone;
  }

  @Override
  public void close(final Component component) {
  }

  @Override
  public JMenu createComponent() {
    return createJMenu();
  }

  public JMenu createJMenu() {
    return createJMenu(false);
  }

  public JMenu createJMenu(final boolean forceEnable) {
    final String name = this.name;
    return createJMenu(name, forceEnable);
  }

  public JMenu createJMenu(final String name, final boolean forceEnable) {
    final JMenu menu = new JMenu(name);
    if (this.enableCheck != null) {
      final boolean enabled = this.enableCheck.isEnabled();
      menu.setEnabled(enabled);
    }
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
            if (forceEnable) {
              component.setEnabled(true);
            }
            menu.add(component);
          }
        }
      }
    }
    return menu;
  }

  public JPopupMenu createJPopupMenu() {
    return createJPopupMenu(false);
  }

  public JPopupMenu createJPopupMenu(final boolean forceEnable) {
    final JPopupMenu menu = new JPopupMenu(this.name);
    if (this.enableCheck != null) {
      final boolean enabled = this.enableCheck.isEnabled();
      menu.setEnabled(enabled);
    }
    boolean first = true;
    for (final String groupName : this.groupNames) {
      boolean groupHasItem = false;
      final List<ComponentFactory<?>> factories = this.groups.get(groupName);
      if (!factories.isEmpty()) {

        for (final ComponentFactory<?> factory : factories) {

          final Component component = factory.createComponent();
          if (component != null) {
            if (forceEnable) {
              component.setEnabled(true);
            }
            if (!groupHasItem) {
              groupHasItem = true;
              if (first) {
                first = false;
              } else {
                menu.addSeparator();
              }
            }
            menu.add(component);
            if (component instanceof JMenu) {
              final JMenu childMenu = (JMenu)component;
              if (childMenu.getMenuComponentCount() == 0) {
                childMenu.setEnabled(false);
              }
            }
          }
        }
      }
    }
    return menu;
  }

  public RunnableAction createMenuItem(final CharSequence name, final String toolTip,
    final Icon icon, final EnableCheck enableCheck, final Runnable runnable) {
    final RunnableAction action = new RunnableAction(name, toolTip, icon, runnable);
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

  /*
   * public void setGroupEnabled(final String groupName, final boolean enabled)
   * { final List<Component> components = getGroup(groupName); for (final
   * Component component : components) { component.setEnabled(enabled); } }
   */

  public EnableCheck getEnableCheck() {
    return this.enableCheck;
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

  public List<String> getGroupNames() {
    return this.groupNames;
  }

  public Map<String, List<ComponentFactory<?>>> getGroups() {
    return this.groups;
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getIconName() {
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

  public void setEnableCheck(final EnableCheck enableCheck) {
    this.enableCheck = enableCheck;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void show(final Object source, final Component component, final int x, final int y) {
    setMenuSource(source);
    final JPopupMenu menu = createJPopupMenu();
    showMenu(menu, component, x, y);
  }

  @Override
  public String toString() {
    if (Property.hasValue(this.name)) {
      return this.name;
    } else {
      return super.toString();
    }
  }
}
