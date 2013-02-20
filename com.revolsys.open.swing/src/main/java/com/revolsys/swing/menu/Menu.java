package com.revolsys.swing.menu;

import java.awt.Component;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.component.ComponentGroup;

@SuppressWarnings("serial")
public class Menu extends JMenu {
  private final ComponentGroup groups = new ComponentGroup();

  public Menu() {
  }

  public Menu(final String title) {
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
    final JMenuItem item = super.add(action);
    addComponent("default", item);
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
}
