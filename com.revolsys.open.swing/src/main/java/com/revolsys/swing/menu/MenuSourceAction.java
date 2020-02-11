package com.revolsys.swing.menu;

import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import javax.swing.Icon;

import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.BaseTreeNode;

public class MenuSourceAction extends AbstractActionMainMenuItemFactory {
  private static final long serialVersionUID = 1L;

  @SuppressWarnings("rawtypes")
  private final Consumer action;

  private final boolean runInBackground;

  MenuSourceAction(final CharSequence name, final String toolTip, final Icon icon,
    final Consumer<?> action, final boolean runInBackground) {
    super(name, toolTip, icon);
    this.action = action;
    this.runInBackground = runInBackground;
  }

  @SuppressWarnings("unchecked")
  private void actionDo() {
    Object item = MenuFactory.getMenuSource();
    if (item instanceof BaseTreeNode) {
      final BaseTreeNode node = (BaseTreeNode)item;
      item = node.getUserObject();
    }
    if (this.action != null && item != null) {
      this.action.accept(item);
    }
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    if (this.runInBackground) {
      final String name = getName();
      final Runnable runnable = this::actionDo;
      Invoke.background(name, runnable);
    } else {
      actionDo();
    }
  }

}
