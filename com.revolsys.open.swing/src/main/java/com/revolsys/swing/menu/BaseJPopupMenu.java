package com.revolsys.swing.menu;

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.revolsys.swing.action.RunnableAction;

public class BaseJPopupMenu extends JPopupMenu {
  private static final long serialVersionUID = 1L;

  public BaseJPopupMenu() {
  }

  public BaseJPopupMenu(final String name) {
    super(name);
  }

  public JMenuItem addMenuItem(final String title, final Runnable runnable) {
    return addMenuItem(title, null, runnable);
  }

  public JMenuItem addMenuItem(final String title, final String iconName, final Runnable runnable) {
    final JMenuItem menuItem = RunnableAction.newMenuItem(title, iconName, runnable);
    add(menuItem);
    return menuItem;
  }

  public void showMenu(final Component component, final int x, final int y) {
    MenuFactory.showMenu(this, component, x, y);
  }

  public boolean showMenu(final MouseEvent e) {
    if (e.isPopupTrigger() && !e.isConsumed()) {
      final Component component = e.getComponent();
      final int x = e.getX();
      final int y = e.getY();
      showMenu(component, x + 5, y);
      return true;
    } else {
      return false;
    }
  }
}
