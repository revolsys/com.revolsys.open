package com.revolsys.swing.menu;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JPopupMenu.Separator;

import org.jeometry.common.logging.Logs;

import com.revolsys.beans.ClassRegistry;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.swing.Icons;
import com.revolsys.swing.action.AbstractAction;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.ComponentFactory;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.tree.TreeNodes;
import com.revolsys.util.Property;

public class MenuFactory extends BaseObjectWithProperties implements ComponentFactory<JMenuItem> {
  private static final ClassRegistry<MenuFactory> CLASS_MENUS = new ClassRegistry<>();

  private static final ClassRegistry<List<Consumer<MenuFactory>>> CLASS_MENUS_INITIALIZER = new ClassRegistry<>();

  private static final String KEY_POPUP_MENU = MenuFactory.class.getName() + ".popup";

  private static Reference<MenuSourceHolder> menuSourceHolder = new WeakReference<>(null);

  public static void addMenuInitializer(final Class<?> clazz,
    final Consumer<MenuFactory> initializer) {
    synchronized (CLASS_MENUS) {
      List<Consumer<MenuFactory>> list = CLASS_MENUS_INITIALIZER.get(clazz);
      if (list == null) {
        list = new ArrayList<>();
        CLASS_MENUS_INITIALIZER.put(clazz, list);
      }
      list.add(initializer);
    }
  }

  public static void addToComponent(final JComponent component, final MenuFactory menuFactory) {
    component.putClientProperty(KEY_POPUP_MENU, menuFactory);
    ShowMenuMouseListener.addListener(component, menuFactory::newJPopupMenu, true);
  }

  public static void clearMenuSourceHolder(final MenuSourceHolder menuSourceHolder) {
    synchronized (MenuSourceHolder.class) {
      if (MenuFactory.menuSourceHolder.get() == menuSourceHolder) {
        MenuFactory.menuSourceHolder = new WeakReference<>(null);
      }
    }
  }

  public static <V> EnableCheck enableCheck(final Predicate<V> filter) {
    if (filter == null) {
      return null;
    } else {
      return () -> {
        final V node = getMenuSource();
        if (node == null) {
          return false;
        } else {
          try {
            return filter.test(node);
          } catch (final Throwable e) {
            Logs.debug(TreeNodes.class, "Exception processing enable check", e);
            return false;
          }
        }
      };
    }
  }

  public static MenuFactory findMenu(final Class<?> clazz) {
    synchronized (CLASS_MENUS) {
      return CLASS_MENUS.find(clazz);
    }
  }

  public static MenuFactory findMenu(final Object object) {
    if (object == null) {
      return null;
    } else {
      final Class<?> clazz = object.getClass();
      return findMenu(clazz);
    }
  }

  public static Window getCurrentWindow() {
    final MenuSourceHolder menuSourceHolder = MenuFactory.menuSourceHolder.get();
    if (menuSourceHolder == null) {
      return null;
    } else {
      return menuSourceHolder.getWindow();
    }
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
          final List<Consumer<MenuFactory>> initializers = CLASS_MENUS_INITIALIZER.get(clazz);
          if (initializers != null) {
            for (final Consumer<MenuFactory> initializer : initializers) {
              initializer.accept(menu);
            }
          }
        }
        return menu;
      }
    }
  }

  public static MenuFactory getMenu(final Object object) {
    final Class<?> clazz = object.getClass();
    return getMenu(clazz);
  }

  public static <S> S getMenuSource() {
    final MenuSourceHolder menuSourceHolder = MenuFactory.menuSourceHolder.get();
    if (menuSourceHolder == null) {
      return null;
    } else {
      return menuSourceHolder.getSource();
    }
  }

  public static MenuSourceHolder getMenuSourceHolder() {
    return menuSourceHolder.get();
  }

  public static MenuFactory getPopupMenuFactory(final JComponent component) {
    synchronized (component) {
      MenuFactory menuFactory = (MenuFactory)component.getClientProperty(KEY_POPUP_MENU);
      if (menuFactory == null) {
        String name = "Field";
        if (component instanceof Field) {
          final Field field = (Field)component;
          name += " " + field.getFieldName();
        }
        menuFactory = new MenuFactory(name);
        addToComponent(component, menuFactory);
      }
      return menuFactory;
    }
  }

  public static <V> MenuSourceAction newAction(final CharSequence name, final Icon icon,
    final Predicate<V> enabledFilter, final Consumer<V> consumer, final boolean runInBackground) {
    final EnableCheck enableCheck = enableCheck(enabledFilter);
    final MenuSourceAction action = new MenuSourceAction(name, null, icon, consumer,
      runInBackground);
    action.setEnableCheck(enableCheck);
    return action;
  }

  public static <V> MenuSourceAction newAction(final CharSequence name, final String iconName,
    final Predicate<V> enabledFilter, final Consumer<V> consumer, final boolean runInBackground) {
    final Icon icon = Icons.getIcon(iconName);
    final MenuSourceAction action = newAction(name, icon, enabledFilter, consumer, runInBackground);
    action.setIconName(iconName);
    return action;
  }

  public static RunnableAction newMenuItem(final CharSequence name, final String toolTip,
    final Icon icon, final EnableCheck enableCheck, final Runnable runnable) {
    final RunnableAction action = new RunnableAction(name, toolTip, icon, runnable);
    action.setEnableCheck(enableCheck);
    return action;
  }

  public static void setMenuSourceHolder(final MenuSourceHolder menuSourceHolder) {
    synchronized (MenuSourceHolder.class) {
      MenuFactory.menuSourceHolder = new WeakReference<>(menuSourceHolder);
    }
  }

  private EnableCheck enableCheck;

  private final List<String> groupNames = new ArrayList<>();

  private final Map<String, List<ComponentFactory<?>>> groups = new HashMap<>();

  private String name;

  private String iconName;

  public MenuFactory() {
  }

  public MenuFactory(final MenuFactory menuFactory) {
    this(menuFactory.name, menuFactory);
    this.iconName = menuFactory.iconName;
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

  public <V> MenuSourceAction addCheckboxMenuItem(final String groupName, final CharSequence name,
    final String iconName, final Predicate<V> enableCheck, final Consumer<V> consumer,
    final Predicate<V> itemChecked, final boolean runInBackground) {
    final MenuSourceAction action = MenuFactory.newAction(name, iconName, enableCheck, consumer,
      runInBackground);
    final EnableCheck itemCheckedEnableCheck = enableCheck(itemChecked);
    addCheckboxMenuItem(groupName, action, itemCheckedEnableCheck);
    return action;
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

  public <V> MenuSourceAction addMenuItem(final MenuFactory menu, final String groupName,
    final CharSequence name, final String iconName, final Predicate<V> enabledFilter,
    final Consumer<V> consumer, final boolean runInBackground) {
    return addMenuItem(groupName, -1, name, iconName, enabledFilter, consumer, runInBackground);
  }

  public <V> MenuSourceAction addMenuItem(final MenuFactory menu, final String groupName,
    final int index, final CharSequence name, final String iconName, final Consumer<V> consumer,
    final boolean runInBackground) {
    return addMenuItem(groupName, index, name, iconName, null, consumer, runInBackground);
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

  public <V> MenuSourceAction addMenuItem(final String groupName, final CharSequence name,
    final Icon icon, final Consumer<V> consumer, final boolean runInBackground) {
    return addMenuItem(groupName, -1, name, icon, null, consumer, runInBackground);
  }

  public <V> MenuSourceAction addMenuItem(final String groupName, final CharSequence name,
    final String iconName, final Consumer<V> consumer, final boolean runInBackground) {
    return addMenuItem(groupName, -1, name, iconName, null, consumer, runInBackground);
  }

  public void addMenuItem(final String groupName, final int index, final AbstractAction action) {
    final ActionMainMenuItemFactory factory = new ActionMainMenuItemFactory(action);
    addComponentFactory(groupName, index, factory);
  }

  public <V> MenuSourceAction addMenuItem(final String groupName, final int index,
    final CharSequence name, final Icon icon, final Predicate<V> enabledFilter,
    final Consumer<V> consumer, final boolean runInBackground) {
    final MenuSourceAction action = MenuFactory.newAction(name, icon, enabledFilter, consumer,
      runInBackground);
    addMenuItem(groupName, index, action);
    return action;
  }

  public <V> MenuSourceAction addMenuItem(final String groupName, final int index,
    final CharSequence name, final String iconName, final Predicate<V> enableCheck,
    final Consumer<V> consumer, final boolean runInBackground) {
    final MenuSourceAction action = MenuFactory.newAction(name, iconName, enableCheck, consumer,
      runInBackground);
    addMenuItem(groupName, index, action);
    return action;
  }

  public void addMenuItem(final String groupName, final int index, final String title,
    final String iconName, final Runnable runnable) {
    final Icon icon = Icons.getIcon(iconName);
    final RunnableAction menuItem = newMenuItem(title, title, icon, null, runnable);
    addComponentFactory(groupName, index, menuItem);
  }

  public void addMenuItem(final String groupName, final String title, final String iconName,
    final EnableCheck enableCheck, final Runnable runnable) {
    final Icon icon = Icons.getIcon(iconName);
    final RunnableAction menuItem = newMenuItem(title, title, icon, enableCheck, runnable);
    addComponentFactory(groupName, menuItem);
  }

  public RunnableAction addMenuItem(final String groupName, final String title,
    final String iconName, final Runnable runnable) {
    final Icon icon = Icons.getIcon(iconName);
    final RunnableAction menuItem = newMenuItem(title, null, icon, null, runnable);
    addComponentFactory(groupName, menuItem);
    return menuItem;
  }

  public void addMenuItem(final String groupName, final String title, final String toolTip,
    final String iconName, final Runnable runnable) {
    final Icon icon = Icons.getIcon(iconName);
    final RunnableAction menuItem = newMenuItem(title, toolTip, icon, null, runnable);
    addComponentFactory(groupName, menuItem);
  }

  public RunnableAction addMenuItemTitleIcon(final String groupName, final CharSequence name,
    final String iconName, final EnableCheck enableCheck, final Runnable runnable) {
    final Icon icon = Icons.getIcon(iconName);
    final RunnableAction action = newMenuItem(name, null, icon, enableCheck, runnable);
    addComponentFactory(groupName, action);
    return action;
  }

  public RunnableAction addMenuItemTitleIcon(final String groupName, final CharSequence name,
    final String iconName, final Runnable runnable) {
    final Icon icon = Icons.getIcon(iconName);
    final RunnableAction action = newMenuItem(name, null, icon, null, runnable);
    addComponentFactory(groupName, action);
    return action;
  }

  public RunnableAction addMenuItemTitleIcon(final String groupName, final int index,
    final CharSequence name, final String iconName, final EnableCheck enableCheck,
    final Runnable runnable) {
    final Icon icon = Icons.getIcon(iconName);
    final RunnableAction action = newMenuItem(name, null, icon, enableCheck, runnable);
    addComponentFactory(groupName, index, action);
    return action;
  }

  private boolean canAddSeparator(final JMenu menu) {
    final int componentCount = menu.getMenuComponentCount();
    if (componentCount == 0) {
      return false;
    } else {
      return !(menu.getMenuComponent(componentCount - 1) instanceof Separator);
    }
  }

  private boolean canAddSeparator(final JPopupMenu menu) {
    final int componentCount = menu.getComponentCount();
    if (componentCount == 0) {
      return false;
    } else {
      return !(menu.getComponent(componentCount - 1) instanceof Separator);
    }
  }

  public void clear() {
    this.groups.clear();
  }

  @Override
  public MenuFactory clone() {
    return new MenuFactory(this);
  }

  @Override
  public void close(final Component component) {
  }

  public void deleteGroup(final String groupName) {
    this.groupNames.remove(groupName);
    this.groups.remove(groupName);
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
      factories = new ArrayList<>();
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

  @SuppressWarnings("unchecked")
  public <A extends AbstractAction> A getMenuByName(final String name) {
    for (final List<ComponentFactory<?>> menuItems : this.groups.values()) {
      for (final ComponentFactory<?> menuItem : menuItems) {
        AbstractAction action = null;
        if (menuItem instanceof ActionMainMenuItemFactory) {
          final ActionMainMenuItemFactory itemFactory = (ActionMainMenuItemFactory)menuItem;
          action = itemFactory.getAction();
        } else if (menuItem instanceof AbstractAction) {
          action = (AbstractAction)menuItem;
        }
        if (action != null) {
          if (name.equals(action.getName())) {
            return (A)action;
          }
        }
      }
    }
    return null;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getToolTip() {
    return null;
  }

  @Override
  public JMenu newComponent() {
    return newJMenu();
  }

  public JMenu newJMenu() {
    return newJMenu(false);
  }

  public JMenu newJMenu(final boolean forceEnable) {
    return newJMenu(this.name, forceEnable);
  }

  public JMenu newJMenu(final String name, final boolean forceEnable) {
    final JMenu menu = new JMenu(name);
    if (this.iconName != null) {
      final Icon icon = Icons.getIcon(this.iconName);
      menu.setIcon(icon);
      final Icon disabledIcon = Icons.getDisabledIcon(this.iconName);
      menu.setDisabledIcon(disabledIcon);
    }
    if (this.enableCheck != null) {
      final boolean enabled = this.enableCheck.isEnabled();
      menu.setEnabled(enabled);
    }
    for (final String groupName : this.groupNames) {
      final List<ComponentFactory<?>> factories = this.groups.get(groupName);
      if (factories != null && !factories.isEmpty()) {
        if (canAddSeparator(menu)) {
          menu.addSeparator();
        }
        for (final ComponentFactory<?> factory : factories) {
          final Component component = factory.newComponent();
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

  public BaseJPopupMenu newJPopupMenu() {
    return newJPopupMenu(false);
  }

  public BaseJPopupMenu newJPopupMenu(final boolean forceEnable) {
    final BaseJPopupMenu menu = new BaseJPopupMenu(this.name);
    if (this.enableCheck != null) {
      final boolean enabled = this.enableCheck.isEnabled();
      menu.setEnabled(enabled);
    }
    for (final String groupName : this.groupNames) {
      boolean groupHasItem = false;
      final List<ComponentFactory<?>> factories = this.groups.get(groupName);
      if (!Property.isEmpty(factories)) {
        for (final ComponentFactory<?> factory : factories) {
          final Component component = factory.newComponent();
          if (component != null) {
            if (forceEnable) {
              component.setEnabled(true);
            }
            if (!groupHasItem) {
              groupHasItem = true;
              if (canAddSeparator(menu)) {
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

  public MenuFactory setEnableCheck(final EnableCheck enableCheck) {
    this.enableCheck = enableCheck;
    return this;
  }

  public MenuFactory setIconName(final String iconName) {
    this.iconName = iconName;
    return this;
  }

  public MenuFactory setName(final String name) {
    this.name = name;
    return this;
  }

  public BaseJPopupMenu showMenu(final Object source, final Component component, final int x,
    final int y) {
    return BaseJPopupMenu.showMenu(this::newJPopupMenu, source, component, x, y);
  }

  public BaseJPopupMenu showMenu(final Object source, final MouseEvent e) {
    if (e.isPopupTrigger() && !e.isConsumed()) {
      final Component component = e.getComponent();
      final int x = e.getX();
      final int y = e.getY();
      return showMenu(source, component, x + 5, y);
    } else {
      return null;
    }
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
