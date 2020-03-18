package com.revolsys.swing.component;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

public class ComponentGroup {

  private final Map<String, ButtonGroup> buttonGroups = new HashMap<>();

  private final List<String> groupNames = new ArrayList<>();

  private final Map<String, List<Component>> groups = new HashMap<>();

  public ComponentGroup() {
  }

  public void addComponent(final JComponent container, final Component component) {
    addComponent(container, "default", component);
  }

  public void addComponent(final JComponent container, final String groupName,
    final Component component) {
    final List<Component> components = getGroup(groupName);
    components.add(component);
    updateComponents(container);
  }

  public void addComponent(final JComponent container, final String groupName, final int index,
    final Component component) {
    final List<Component> components = getGroup(groupName);
    if (index < 0) {
      components.add(component);
    } else {
      components.add(index, component);
    }
    updateComponents(container);
  }

  public void addGroup(final String groupName) {
    getGroup(groupName);
  }

  public void clear() {
    this.buttonGroups.clear();
    this.groupNames.clear();
    this.groups.clear();
  }

  public ButtonGroup getButtonGroup(final String groupName) {
    ButtonGroup buttonGroup = this.buttonGroups.get(groupName);
    if (buttonGroup == null) {
      buttonGroup = new ButtonGroup();
      this.buttonGroups.put(groupName, buttonGroup);
    }
    return buttonGroup;
  }

  public List<Component> getGroup(final String groupName) {
    List<Component> components = this.groups.get(groupName);
    if (components == null) {
      components = new ArrayList<>();
      this.groups.put(groupName, components);
      this.groupNames.add(groupName);
    }
    return components;
  }

  public void removeComponent(final JComponent container, final String groupName, final int index) {
    final List<Component> components = getGroup(groupName);
    if (index < components.size()) {
      components.remove(index);
    }
    updateComponents(container);
  }

  public void removeGroup(final Container container, final String groupName) {
    this.buttonGroups.remove(groupName);
    this.groupNames.remove(groupName);
    for (final Component component : this.groups.remove(groupName)) {
      container.remove(component);
    }
  }

  public void setGroupEnabled(final String groupName, final boolean enabled) {
    final List<Component> components = getGroup(groupName);
    for (final Component component : components) {
      component.setEnabled(enabled);
    }
  }

  public void updateComponents(final JComponent container) {
    container.removeAll();
    boolean first = true;
    for (final String groupName : this.groupNames) {
      final List<Component> components = this.groups.get(groupName);
      if (!components.isEmpty()) {
        if (first) {
          first = false;
        } else {
          if (container instanceof JMenu) {
            final JMenu menu = (JMenu)container;
            menu.addSeparator();
          } else if (container instanceof JPopupMenu) {
            final JPopupMenu menu = (JPopupMenu)container;
            menu.addSeparator();
          } else if (container instanceof JToolBar) {
            final JToolBar toolBar = (JToolBar)container;
            toolBar.addSeparator();
          }
        }
        for (final Component component : components) {
          container.add(component);
        }
      }
    }
    container.revalidate();
  }
}
