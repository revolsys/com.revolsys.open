package com.revolsys.swing.menu;

import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.Icon;

import org.slf4j.LoggerFactory;

import com.revolsys.swing.Icons;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.tree.TreeNodes;

public interface Menus {

  static <V> MenuSourceAction addCheckboxMenuItem(final MenuFactory menu, final String groupName,
    final CharSequence name, final String iconName, final Predicate<V> enableCheck,
    final Consumer<V> consumer, final Predicate<V> itemChecked, boolean runInBackground) {
    final MenuSourceAction action = Menus.newAction(name, iconName, enableCheck, consumer,
      runInBackground);
    final EnableCheck itemCheckedEnableCheck = enableCheck(itemChecked);
    menu.addCheckboxMenuItem(groupName, action, itemCheckedEnableCheck);
    return action;
  }

  static <V> MenuSourceAction addMenuItem(final MenuFactory menu, final String groupName,
    final CharSequence name, final Icon icon, final Consumer<V> consumer, boolean runInBackground) {
    return addMenuItem(menu, groupName, -1, name, icon, null, consumer, runInBackground);
  }

  static <V> MenuSourceAction addMenuItem(final MenuFactory menu, final String groupName,
    final CharSequence name, final String iconName, final Consumer<V> consumer, boolean runInBackground) {
    return addMenuItem(menu, groupName, -1, name, iconName, null, consumer, runInBackground);
  }

  static <V> MenuSourceAction addMenuItem(final MenuFactory menu, final String groupName,
    final CharSequence name, final String iconName, final Predicate<V> enabledFilter,
    final Consumer<V> consumer, boolean runInBackground) {
    return addMenuItem(menu, groupName, -1, name, iconName, enabledFilter, consumer, runInBackground);
  }

  static <V> MenuSourceAction addMenuItem(final MenuFactory menu, final String groupName,
    final int index, final CharSequence name, final Icon icon, final Predicate<V> enabledFilter,
    final Consumer<V> consumer, boolean runInBackground) {
    final MenuSourceAction action = Menus.newAction(name, icon, enabledFilter, consumer,
      runInBackground);
    menu.addMenuItem(groupName, index, action);
    return action;
  }

  static <V> MenuSourceAction addMenuItem(final MenuFactory menu, final String groupName,
    final int index, final CharSequence name, final String iconName, final Consumer<V> consumer, boolean runInBackground) {
    return addMenuItem(menu, groupName, index, name, iconName, null, consumer, runInBackground);
  }

  static <V> MenuSourceAction addMenuItem(final MenuFactory menu, final String groupName,
    final int index, final CharSequence name, final String iconName, final Predicate<V> enableCheck,
    final Consumer<V> consumer, boolean runInBackground) {
    final MenuSourceAction action = Menus.newAction(name, iconName, enableCheck, consumer,
      runInBackground);
    menu.addMenuItem(groupName, index, action);
    return action;
  }

  static <V> EnableCheck enableCheck(final Predicate<V> filter) {
    if (filter == null) {
      return null;
    } else {
      return () -> {
        final V node = MenuFactory.getMenuSource();
        if (node == null) {
          return false;
        } else {
          try {
            return filter.test(node);
          } catch (final Throwable e) {
            LoggerFactory.getLogger(TreeNodes.class).debug("Exception processing enable check", e);
            return false;
          }
        }
      };
    }
  }

  static <V> MenuSourceAction newAction(final CharSequence name, final Icon icon,
    final Predicate<V> enabledFilter, final Consumer<V> consumer, final boolean runInBackground) {
    final EnableCheck enableCheck = enableCheck(enabledFilter);
    final MenuSourceAction action = new MenuSourceAction(name, null, icon, consumer,
      runInBackground);
    action.setEnableCheck(enableCheck);
    return action;
  }

  static <V> MenuSourceAction newAction(final CharSequence name, final String iconName,
    final Predicate<V> enabledFilter, final Consumer<V> consumer, final boolean runInBackground) {
    final Icon icon = Icons.getIcon(iconName);
    final MenuSourceAction action = newAction(name, icon, enabledFilter, consumer, runInBackground);
    action.setIconName(iconName);
    return action;
  }
}
