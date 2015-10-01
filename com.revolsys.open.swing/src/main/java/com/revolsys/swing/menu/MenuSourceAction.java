package com.revolsys.swing.menu;

import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.swing.Icons;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.tree.node.BaseTreeNode;

public class MenuSourceAction extends AbstractActionMainMenuItemFactory {
  private static final long serialVersionUID = 1L;

  public static <C> MenuSourceAction addCheckboxMenuItem(final MenuFactory menu,
    final String groupName, final CharSequence name, final String iconName,
    final EnableCheck enableCheck, final Consumer<C> consumer, final EnableCheck itemChecked) {
    final MenuSourceAction action = createAction(name, iconName, enableCheck, consumer);
    menu.addCheckboxMenuItem(groupName, action, itemChecked);
    return action;
  }

  public static <C> MenuSourceAction addMenuItem(final MenuFactory menu, final String groupName,
    final CharSequence name, final Icon icon, final Consumer<C> consumer) {
    final MenuSourceAction action = createAction(name, icon, consumer);
    menu.addMenuItem(groupName, action);
    return action;
  }

  public static <C> MenuSourceAction addMenuItem(final MenuFactory menu, final String groupName,
    final CharSequence name, final String iconName, final Consumer<C> consumer) {
    final MenuSourceAction action = createAction(name, iconName, consumer);
    menu.addMenuItem(groupName, action);
    return action;
  }

  public static <C> MenuSourceAction addMenuItem(final MenuFactory menu, final String groupName,
    final CharSequence name, final String iconName, final EnableCheck enableCheck,
    final Consumer<C> consumer) {
    final MenuSourceAction action = createAction(name, iconName, enableCheck, consumer);
    menu.addMenuItem(groupName, action);
    return action;
  }

  public static <C> MenuSourceAction addMenuItem(final MenuFactory menu, final String groupName,
    final int index, final CharSequence name, final String iconName, final Consumer<C> consumer) {
    final MenuSourceAction action = createAction(name, iconName, consumer);
    menu.addMenuItem(groupName, index, action);
    return action;
  }

  public static <C> MenuSourceAction addMenuItem(final MenuFactory menu, final String groupName,
    final int index, final CharSequence name, final String iconName, final EnableCheck enableCheck,
    final Consumer<C> consumer) {
    final MenuSourceAction action = createAction(name, iconName, enableCheck, consumer);
    menu.addMenuItem(groupName, index, action);
    return action;
  }

  public static <C> MenuSourceAction createAction(final CharSequence name, final Icon icon,
    final Consumer<C> consumer) {
    final MenuSourceAction action = createAction(name, icon, null, consumer);
    return action;
  }

  public static <C> MenuSourceAction createAction(final CharSequence name, final Icon icon,
    final EnableCheck enableCheck, final Consumer<C> consumer) {
    final MenuSourceAction action = new MenuSourceAction(name, null, icon, consumer);
    action.setEnableCheck(enableCheck);
    return action;
  }

  public static <C> MenuSourceAction createAction(final CharSequence name, final String iconName,
    final Consumer<C> consumer) {
    final ImageIcon icon = Icons.getIcon(iconName);
    final MenuSourceAction action = createAction(name, icon, null, consumer);
    action.setIconName(iconName);
    return action;
  }

  public static <C> MenuSourceAction createAction(final CharSequence name, final String iconName,
    final EnableCheck enableCheck, final Consumer<C> consumer) {
    final ImageIcon icon = Icons.getIcon(iconName);
    final MenuSourceAction action = createAction(name, icon, enableCheck, consumer);
    action.setIconName(iconName);
    return action;
  }

  @SuppressWarnings("rawtypes")
  private final Consumer action;

  private String iconName;

  private MenuSourceAction(final CharSequence name, final String toolTip, final Icon icon,
    final Consumer<?> action) {
    super(name, toolTip, icon);
    this.action = action;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void actionPerformed(final ActionEvent e) {
    Object item = MenuFactory.getMenuSource();
    if (item instanceof BaseTreeNode) {
      final BaseTreeNode node = (BaseTreeNode)item;
      item = node.getUserObject();
    }
    this.action.accept(item);
  }

  @Override
  public String getIconName() {
    return this.iconName;
  }

  public void setIconName(final String iconName) {
    this.iconName = iconName;
  }
}
