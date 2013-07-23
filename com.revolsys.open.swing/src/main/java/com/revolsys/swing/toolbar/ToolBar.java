package com.revolsys.swing.toolbar;

import java.awt.Component;
import java.util.List;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.ComponentGroup;

@SuppressWarnings("serial")
public class ToolBar extends JToolBar {
  private final ComponentGroup groups = new ComponentGroup();

  public ToolBar() {
    setRollover(false);
    setFloatable(false);
  }

  public JButton addButton(final Action action) {
    final JButton button = createButton(action);
    groups.addComponent(this, button);
    return button;
  }

  public JButton addButton(final String groupName, final Action action) {
    final JButton button = createButton(action);
    button.setAction(action);
    groups.addComponent(this, groupName, button);
    return button;
  }

  public JButton addButton(final String groupName, final String title,
    final String iconName, final EnableCheck enableCheck, final Object object,
    final String methodName, final Object... parameters) {
    final Icon icon = SilkIconLoader.getIcon(iconName);
    final InvokeMethodAction action = new InvokeMethodAction(null, title, icon,
      enableCheck, object, methodName, parameters);

    return addButton(groupName, action);
  }

  public JButton addButton(final String groupName, final String name,
    final String title, final Icon icon, final Object object,
    final String methodName, final Object... parameters) {
    final InvokeMethodAction action = new InvokeMethodAction(name, title, icon,
      object, methodName, parameters);

    return addButton(groupName, action);
  }

  public JButton addButtonTitleIcon(final String groupName, final String title,
    final String iconName, final Object object, final String methodName,
    final Object... parameters) {
    final ImageIcon icon = SilkIconLoader.getIcon(iconName);
    return addButton(groupName, iconName, title, icon, object, methodName,
      parameters);
  }

  public void addComponent(final Component component) {
    groups.addComponent(this, component);
  }

  public void addComponent(final String groupName, final Component component) {
    groups.addComponent(this, groupName, component);
  }

  public void addGroup(final String groupName) {
    groups.addGroup(groupName);
  }

  public JToggleButton addToggleButton(final String groupName, final int index,
    final String title, final String iconName, final EnableCheck enableCheck,
    final Object object, final String methodName, final Object... parameters) {
    final ImageIcon icon = SilkIconLoader.getIcon(iconName);
    return addToggleButton(groupName, index, iconName, title, icon,
      enableCheck, object, methodName, parameters);
  }

  public JToggleButton addToggleButton(final String groupName, final int index,
    final String name, final String title, final Icon icon,
    final EnableCheck enableCheck, final Object object,
    final String methodName, final Object... parameters) {
    final InvokeMethodAction action = new InvokeMethodAction(name, title, icon,
      object, methodName, parameters);
    action.setEnableCheck(enableCheck);

    final JToggleButton button = createToggleButton(action);
    groups.addComponent(this, groupName, index, button);
    final ButtonGroup buttonGroup = getButtonGroup(groupName);
    buttonGroup.add(button);
    return button;
  }

  public JToggleButton addToggleButtonTitleIcon(final String groupName,
    final int index, final String title, final String iconName,
    final Object object, final String methodName, final Object... parameters) {
    final ImageIcon icon = SilkIconLoader.getIcon(iconName);
    return addToggleButton(groupName, index, iconName, title, icon, null,
      object, methodName, parameters);
  }

  protected JButton createButton(final Action action) {
    final JButton button = new JButton(action);
    if (action != null
      && (action.getValue(Action.SMALL_ICON) != null || action.getValue(Action.LARGE_ICON_KEY) != null)) {
      button.setHideActionText(true);
    }
    button.setHorizontalTextPosition(SwingConstants.CENTER);
    button.setVerticalTextPosition(SwingConstants.BOTTOM);
    button.setFocusPainted(false);
    button.setBorderPainted(false);
    return button;
  }

  protected JToggleButton createToggleButton(final Action action) {
    final JToggleButton button = new JToggleButton(action);
    if (action != null
      && (action.getValue(Action.SMALL_ICON) != null || action.getValue(Action.LARGE_ICON_KEY) != null)) {
      button.setHideActionText(true);
    }
    button.setHorizontalTextPosition(SwingConstants.CENTER);
    button.setVerticalTextPosition(SwingConstants.BOTTOM);
    button.setFocusPainted(false);
    button.setBorderPainted(true);
    return button;
  }

  public ButtonGroup getButtonGroup(final String groupName) {
    return groups.getButtonGroup(groupName);
  }

  public List<Component> getGroup(final String groupName) {
    return groups.getGroup(groupName);
  }

  public void removeComponent(final String groupName, final int index) {
    groups.removeComponent(this, groupName, index);
  }

  public void removeGroup(final String groupName) {
    groups.removeGroup(this, groupName);

  }

  public void setGroupEnabled(final String groupName, final boolean enabled) {
    groups.setGroupEnabled(groupName, enabled);
  }

}
