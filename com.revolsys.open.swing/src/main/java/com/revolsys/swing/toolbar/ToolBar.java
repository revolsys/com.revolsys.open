package com.revolsys.swing.toolbar;

import java.awt.Component;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.component.ComponentGroup;

@SuppressWarnings("serial")
public class ToolBar extends JToolBar {
  private final ComponentGroup groups = new ComponentGroup();

  public ToolBar() {
    setRollover(true);
  }

   public JButton addButton(final Action action) {
    final JButton button = super.add(action);
    groups.addComponent(this, button);
    return button;
  }

  public JButton addButton(final String groupName, final String name,
    final String title, final Icon icon, final Object object,
    final String methodName, final Object... parameters) {
    final InvokeMethodAction action = new InvokeMethodAction(name, title, icon,
      object, methodName, parameters);

    final JButton button = createActionComponent(action);
    button.setFocusPainted(false);
    button.setAction(action);
    groups.addComponent(this,groupName, button);
    return button;
  }

  public JButton addButtonTitleIcon(final String groupName, final String title,
    final String iconName, final Object object, final String methodName,
    final Object... parameters) {
    final ImageIcon icon = SilkIconLoader.getIcon(iconName);
    return addButton(groupName, iconName, title, icon, object, methodName,
      parameters);
  }

  public void addComponent(final String groupName, final Component component) {
   groups.addComponent(this,groupName,component);
  }

  public void addComponent( final Component component) {
   groups.addComponent(this,component);
  }

  public void addGroup(final String groupName) {
    groups.addGroup(groupName);
  }

  public List<Component> getGroup(final String groupName) {
     return groups.getGroup(groupName);
  }

  public void setGroupEnabled(final String groupName, final boolean enabled) {
    groups.setGroupEnabled(groupName, enabled);
  }

}
