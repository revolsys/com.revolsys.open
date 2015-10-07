package com.revolsys.swing.menu;

import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import javax.swing.Icon;

import com.revolsys.swing.tree.node.BaseTreeNode;

public class MenuSourceAction extends AbstractActionMainMenuItemFactory {
  private static final long serialVersionUID = 1L;

  @SuppressWarnings("rawtypes")
  private final Consumer action;

  private String iconName;

  MenuSourceAction(final CharSequence name, final String toolTip, final Icon icon,
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
