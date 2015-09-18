package com.revolsys.swing.tree;

import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.swing.Icons;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.menu.AbstractActionMainMenuItemFactory;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.node.BaseTreeNode;

public class MenuSourceAction extends AbstractActionMainMenuItemFactory {
  private static final long serialVersionUID = 1L;

  public static MenuSourceAction createAction(final CharSequence name, final Icon icon,
    final Consumer<?> consumer) {
    final MenuSourceAction action = createAction(name, icon, null, consumer);
    return action;
  }

  public static MenuSourceAction createAction(final CharSequence name, final Icon icon,
    final EnableCheck enableCheck, final Consumer<?> consumer) {
    final MenuSourceAction action = new MenuSourceAction(name, null, icon, consumer);
    action.setEnableCheck(enableCheck);
    return action;
  }

  public static MenuSourceAction createAction(final CharSequence name, final String iconName,
    final Consumer<?> consumer) {
    final ImageIcon icon = Icons.getIcon(iconName);
    final MenuSourceAction action = createAction(name, icon, null, consumer);
    action.setIconName(iconName);
    return action;
  }

  public static MenuSourceAction createAction(final CharSequence name, final String iconName,
    final EnableCheck enableCheck, final Consumer<?> consumer) {
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
