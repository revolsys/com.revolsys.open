package com.revolsys.jump.util;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.openjump.core.ui.plugin.AbstractUiPlugIn;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.ui.EnableableToolBar;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.WorkbenchToolBar;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

public final class WorkbenchUtil {
  private WorkbenchUtil() {
  }

  public static void replaceMenuItem(final WorkbenchContext workbenchContext,
    final JPopupMenu menu, final String oldMenuText,
    final AbstractUiPlugIn plugin, final ImageIcon icon,
    final EnableCheck enableCheck) {
    int i = 0;
    boolean found = false;
    while (!found && i < menu.getComponentCount()) {
      Component component = menu.getComponent(i);
      if (component instanceof JMenuItem) {
        JMenuItem item = (JMenuItem)component;
        if (item.getText().equals(oldMenuText)) {
          found = true;
          menu.remove(i);
        } else {
          i++;
        }
      } else {
        i++;
      }
    }
    FeatureInstaller featureInstaller = new FeatureInstaller(workbenchContext);
    featureInstaller.addPopupMenuItem(menu, plugin, plugin.getName()
      + "...{pos:" + i + "}", false, GUIUtil.toSmallIcon(icon), enableCheck);

  }

  public static void replaceToolbar(final WorkbenchContext workbenchContext,
    final WorkbenchToolBar toolBar, final int index,
    final AbstractUiPlugIn plugin, final Icon icon,
    final EnableCheck enableCheck) {
    toolBar.remove(index);
    addPlugIn(toolBar, icon, plugin, enableCheck, workbenchContext, index);

  }

  public static void replaceMenuItem(final WorkbenchContext workbenchContext,
    final String menuName, final String oldMenuText,
    final AbstractUiPlugIn plugin, final Icon icon,
    final EnableCheck enableCheck) {
    String newMenuText = oldMenuText;
    replaceMenuItem(workbenchContext, menuName, oldMenuText, newMenuText,
      plugin, icon, enableCheck);

  }

  public static void replaceMenuItem(final WorkbenchContext workbenchContext,
    final String menuName, final String oldMenuText, final String newMenuText,
    final AbstractUiPlugIn plugin, final Icon icon,
    final EnableCheck enableCheck) {
    FeatureInstaller featureInstaller = new FeatureInstaller(workbenchContext);
    int i = removeMenuItem(featureInstaller, menuName, oldMenuText);
    featureInstaller.addMainMenuItemWithJava14Fix(plugin, new String[] {
      menuName
    }, newMenuText + "{pos:" + i + "}", false, icon, enableCheck);
  }

  public static void replaceMenuItem(final WorkbenchContext workbenchContext,
    final JPopupMenu menu, final String oldMenuText, final String newMenuText,
    final AbstractUiPlugIn plugin, final Icon icon,
    final EnableCheck enableCheck) {
    FeatureInstaller featureInstaller = new FeatureInstaller(workbenchContext);
    int i = removeMenuItem(featureInstaller, menu, oldMenuText);
    featureInstaller.addPopupMenuItem(menu, plugin, newMenuText + "{pos:" + i
      + "}", false, icon, enableCheck);
  }

  public static int removeMenuItem(final FeatureInstaller featureInstaller,
    final String parentMenuName, final String menuName) {
    JMenu menu = featureInstaller.menuBarMenu(parentMenuName);
    int menuItemIndex = getMenuItemIndex(menu, menuName);
    if (menuItemIndex != -1) {
      menu.remove(menuItemIndex);
    }
    return menuItemIndex;
  }

  public static int removeMenuItem(final FeatureInstaller featureInstaller,
    final JPopupMenu menu, final String menuName) {
    int menuItemIndex = getMenuItemIndex(menu, menuName);
    if (menuItemIndex != -1) {
      menu.remove(menuItemIndex);
    }
    return menuItemIndex;
  }

  public static int getMenuItemIndex(final JPopupMenu menu,
    final String menuName) {
    int i = 0;
    while (i < menu.getComponentCount()) {
      Component component = menu.getComponent(i);
      if (component instanceof JMenuItem) {
        JMenuItem item = (JMenuItem)component;
        if (item.getText().equals(menuName)) {
          return i;
        } else {
          i++;
        }
      } else {
        i++;
      }
    }
    return -1;
  }

  public static int getMenuItemIndex(final JMenu menu, final String menuName) {
    int i = 0;
    while (i < menu.getMenuComponentCount()) {
      Component component = menu.getMenuComponent(i);
      if (component instanceof JMenuItem) {
        JMenuItem item = (JMenuItem)component;
        if (item.getText().equals(menuName)) {
          return i;
        } else {
          i++;
        }
      } else {
        i++;
      }
    }
    return -1;
  }

  public static void moveMenuItemAfter(final FeatureInstaller featureInstaller,
    final String parentMenuName, final String menuName,
    final String otherMenuName) {
    JMenu menu = featureInstaller.menuBarMenu(parentMenuName);
    moveMenuItemAfter(menu, menuName, otherMenuName);
  }

  private static void moveMenuItemAfter(final JMenu menu,
    final String menuName, final String otherMenuName) {
    int oldIndex = getMenuItemIndex(menu, menuName);
    int newIndex = getMenuItemIndex(menu, otherMenuName) + 1;
    moveMenuItem(menu, oldIndex, newIndex);
  }

  public static void moveMenuItem(final JMenu menu, final int oldIndex,
    final int newIndex) {
    JMenuItem oldItem = (JMenuItem)menu.getMenuComponent(oldIndex);
    menu.remove(oldIndex);
    if (oldIndex < newIndex) {
      menu.insert(oldItem, newIndex - 1);
    } else {
      menu.insert(oldItem, newIndex);
    }
  }

  public static JMenu addMainMenu(final FeatureInstaller featureInstaller,
    final String[] menuPath, final String menuName, final int index) {
    JMenu menu = new JMenu(menuName);
    JMenu parent = featureInstaller.createMenusIfNecessary(
      featureInstaller.menuBarMenu(menuPath[0]),
      featureInstaller.behead(menuPath));
    parent.insert(menu, index);
    return menu;
  }

  public static void add(final EnableableToolBar toolbar,
    final AbstractButton button, final String tooltip, final Icon icon,
    final ActionListener actionListener, final EnableCheck enableCheck,
    final int index) {
    if (enableCheck != null) {
      toolbar.setEnableCheck(button, enableCheck);
    }
    button.setIcon(icon);
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setToolTipText(tooltip);
    button.addActionListener(actionListener);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        toolbar.updateEnabledState();
      }
    });
    toolbar.add(button, index);
  }

  public static JButton addPlugIn(final EnableableToolBar toolbar,
    final Icon icon, final AbstractUiPlugIn plugIn,
    final EnableCheck enableCheck, final WorkbenchContext workbenchContext,
    final int index) {
    JButton button = new JButton();
    add(toolbar, button, plugIn.getName(), icon, plugIn, enableCheck, index);

    return button;
  }

}
