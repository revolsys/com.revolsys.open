package com.revolsys.swing.toolbar;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import com.revolsys.swing.Icons;
import com.revolsys.swing.action.AbstractAction;
import com.revolsys.swing.action.ConsumerAction;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.ComponentGroup;
import com.revolsys.swing.menu.ToggleButton;
import com.revolsys.util.Property;

public class ToolBar extends JToolBar {
  private static final long serialVersionUID = 1L;

  private final ComponentGroup groups = new ComponentGroup();

  public ToolBar() {
    this(HORIZONTAL);
  }

  public ToolBar(final int orientation) {
    super(orientation);
    setOpaque(true);
    setRollover(false);
    setFloatable(false);
  }

  public JButton addButton(final AbstractAction action) {
    final JButton button = action.newButton();
    button.setBorderPainted(false);
    this.groups.addComponent(this, button);
    return button;
  }

  public JButton addButton(final String groupName, final AbstractAction action) {
    final JButton button = action.newButton();
    button.setBorderPainted(false);
    this.groups.addComponent(this, groupName, button);
    return button;
  }

  public JButton addButton(final String groupName, final int index, final AbstractAction action) {
    final JButton button = action.newButton();
    button.setBorderPainted(false);
    this.groups.addComponent(this, groupName, index, button);
    return button;
  }

  public JButton addButton(final String groupName, final int index, final String name,
    final String title, final Icon icon, final EnableCheck enableCheck, final Runnable runnable) {
    final RunnableAction action = new RunnableAction(name, title, icon, runnable);
    action.setEnableCheck(enableCheck);

    final JButton button = action.newButton();
    button.setBorderPainted(false);
    this.groups.addComponent(this, groupName, index, button);
    return button;
  }

  public JButton addButton(final String groupName, final String title,
    final EnableCheck enableCheck, final Runnable runnable) {
    final RunnableAction action = new RunnableAction(title, null, null, enableCheck, runnable);
    return addButton(groupName, action);
  }

  public JButton addButton(final String groupName, final String title, final Runnable runnable) {
    final RunnableAction action = new RunnableAction(title, runnable);
    return addButton(groupName, action);
  }

  public JButton addButton(final String groupName, String title, final String iconName,
    final EnableCheck enableCheck, final Runnable runnable) {
    String name = null;
    Icon icon = null;
    if (Property.hasValue(iconName)) {
      icon = Icons.getIcon(iconName);
    } else {
      name = title;
      title = null;
    }

    final RunnableAction action = new RunnableAction(name, title, icon, enableCheck, runnable);
    return addButton(groupName, action);
  }

  public JButton addButton(final String groupName, final String name, final String title,
    final Icon icon, final Runnable runnable) {
    final RunnableAction action = new RunnableAction(name, title, icon, runnable);
    return addButton(groupName, action);
  }

  public JButton addButton(final String groupName, final String title, final String iconName,
    final Runnable runnable) {
    return addButton(groupName, title, iconName, (EnableCheck)null, runnable);
  }

  public JButton addButtonTitleIcon(final String groupName, final int index, final String title,
    final String iconName, final Runnable runnable) {
    final Icon icon = Icons.getIcon(iconName);
    return addButton(groupName, index, null, title, icon, null, runnable);
  }

  public JButton addButtonTitleIcon(final String groupName, final String title,
    final String iconName, final EnableCheck enableCheck, final Runnable runnable) {
    final Icon icon = Icons.getIcon(iconName);
    return addButton(groupName, -1, null, title, icon, enableCheck, runnable);
  }

  public JButton addButtonTitleIcon(final String groupName, final String title,
    final String iconName, final Runnable runnable) {
    final Icon icon = Icons.getIcon(iconName);
    return addButton(groupName, -1, iconName, title, icon, null, runnable);
  }

  public void addComponent(final Component component) {
    this.groups.addComponent(this, component);
  }

  public void addComponent(final String groupName, final Component component) {
    this.groups.addComponent(this, groupName, component);
  }

  public void addGroup(final String groupName) {
    this.groups.addGroup(groupName);
  }

  public ToggleButton addRadioButton(final String groupName, final int index, final String name,
    final String title, final Icon icon, final EnableCheck enableCheck,
    final Consumer<ActionEvent> handler) {
    final ConsumerAction action = new ConsumerAction(name, title, icon, enableCheck, false,
      handler);

    final ToggleButton button = action.newToggleButton();
    button.setBorderPainted(true);
    this.groups.addComponent(this, groupName, index, button);
    return button;
  }

  public ToggleButton addToggleButton(final String groupName, final int index, final String name,
    final String title, final Icon icon, final EnableCheck enableCheck,
    final Consumer<ActionEvent> handler) {
    final ConsumerAction action = new ConsumerAction(name, title, icon, enableCheck, false,
      handler);

    final ToggleButton button = action.newToggleButton();
    button.setBorderPainted(true);
    this.groups.addComponent(this, groupName, index, button);
    final ButtonGroup buttonGroup = getButtonGroup(groupName);
    buttonGroup.add(button);
    return button;
  }

  public ToggleButton addToggleButton(final String groupName, final int index, final String name,
    final String title, final Icon icon, final EnableCheck enableCheck, final Runnable runnable) {
    final RunnableAction action = new RunnableAction(name, title, icon, runnable);
    action.setEnableCheck(enableCheck);

    final ToggleButton button = action.newToggleButton();
    button.setBorderPainted(true);
    this.groups.addComponent(this, groupName, index, button);
    final ButtonGroup buttonGroup = getButtonGroup(groupName);
    buttonGroup.add(button);
    return button;
  }

  public ToggleButton addToggleButtonTitleIcon(final String groupName, final int index,
    final String title, final String iconName, final EnableCheck enableCheck,
    final Runnable runnable) {
    final Icon icon = Icons.getIcon(iconName);
    return addToggleButton(groupName, index, iconName, title, icon, enableCheck, runnable);
  }

  public ToggleButton addToggleButtonTitleIcon(final String groupName, final int index,
    final String title, final String iconName, final Runnable runnable) {
    final Icon icon = Icons.getIcon(iconName);
    return addToggleButton(groupName, index, iconName, title, icon, null, runnable);
  }

  public void clear() {
    super.removeAll();
    this.groups.clear();
  }

  public ButtonGroup getButtonGroup(final String groupName) {
    return this.groups.getButtonGroup(groupName);
  }

  public List<Component> getGroup(final String groupName) {
    return this.groups.getGroup(groupName);
  }

  public void removeComponent(final String groupName, final int index) {
    this.groups.removeComponent(this, groupName, index);
  }

  public void removeGroup(final String groupName) {
    this.groups.removeGroup(this, groupName);

  }

  public void setGroupEnabled(final String groupName, final boolean enabled) {
    this.groups.setGroupEnabled(groupName, enabled);
  }

}
