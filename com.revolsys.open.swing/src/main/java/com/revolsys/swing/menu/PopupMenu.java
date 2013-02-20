package com.revolsys.swing.menu;

import java.awt.Component;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.component.ComponentGroup;

@SuppressWarnings("serial")
public class PopupMenu extends JPopupMenu {
  private final ComponentGroup groups = new ComponentGroup();

  public PopupMenu() {
  }

  public PopupMenu(final String title) {
    super(title);
  }

  public void addComponent(final Component component) {
    groups.addComponent(this, component);
  }

  public void addComponent(final String groupName, final Component component) {
    groups.addComponent(this, component);
  }

  public void addGroup(final String groupName) {
    groups.addGroup(groupName);
  }

  public JMenuItem addMenuItem(final Action action) {
    return addMenuItem("default", action);
  }

  public JMenuItem addMenuItem(final String groupName, final Action action) {
    final JMenuItem item = super.add(action);
    addComponent(groupName, item);
    return item;
  }

  public JMenuItem addMenuItem(final JMenuItem menuItem) {
    addComponent(menuItem);
    return menuItem;
  }

  public JMenuItem addMenuItem(final String title) {
    final JMenuItem menuItem = new JMenuItem(title);
    return add(menuItem);
  }

  public JMenuItem addMenuItem(final String groupName, final String name,
    final String title, final Icon icon, final Object object,
    final String methodName, final Object... parameters) {
    final InvokeMethodAction action = new InvokeMethodAction(name, title, icon,
      object, methodName, parameters);

    final JMenuItem button = createActionComponent(action);
    button.setFocusPainted(false);
    button.setAction(action);
    addComponent(groupName, button);
    return button;
  }

  public JMenuItem addMenuItemTitleIcon(final String groupName,
    final String title, final String iconName, final Object object,
    final String methodName, final Object... parameters) {
    final ImageIcon icon = SilkIconLoader.getIcon(iconName);
    return addMenuItem(groupName, iconName, title, icon, object, methodName,
      parameters);
  }

  public List<Component> getGroup(final String groupName) {
    return groups.getGroup(groupName);
  }

  public void setGroupEnabled(final String groupName, final boolean enabled) {
    groups.setGroupEnabled(groupName, enabled);
  }

  public void addMenuItem(String groupName, JMenuItem menuItem) {
    groups.addComponent(this, groupName, menuItem);
  }
}
