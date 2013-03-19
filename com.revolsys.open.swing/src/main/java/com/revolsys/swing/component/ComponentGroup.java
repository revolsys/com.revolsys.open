package com.revolsys.swing.component;

import java.awt.Component;
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

  private final Map<String, List<Component>> groups = new HashMap<String, List<Component>>();

  private final Map<String, ButtonGroup> buttonGroups = new HashMap<String, ButtonGroup>();

  private final List<String> groupNames = new ArrayList<String>();

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

  public void addGroup(final String groupName) {
    getGroup(groupName);
  }

  public List<Component> getGroup(final String groupName) {
    List<Component> components = groups.get(groupName);
    if (components == null) {
      components = new ArrayList<Component>();
      groups.put(groupName, components);
      groupNames.add(groupName);
    }
    return components;
  }

  public ButtonGroup getButtonGroup(final String groupName) {
    ButtonGroup buttonGroup = buttonGroups.get(groupName);
    if (buttonGroup == null) {
      buttonGroup = new ButtonGroup();
      buttonGroups.put(groupName, buttonGroup);
    }
    return buttonGroup;
  }

  public void setGroupEnabled(final String groupName, final boolean enabled) {
    final List<Component> components = getGroup(groupName);
    for (final Component component : components) {
      component.setEnabled(enabled);
    }
  }

  public void updateComponents(JComponent container) {
    container.removeAll();
    boolean first = true;
    for (final String groupName : groupNames) {
      final List<Component> components = groups.get(groupName);
      if (!components.isEmpty()) {
        if (first) {
          first = false;
        } else {
          if (container instanceof JMenu) {
            JMenu menu = (JMenu)container;
            menu.addSeparator();
          } else if (container instanceof JPopupMenu) {
            JPopupMenu menu = (JPopupMenu)container;
            menu.addSeparator();
          } else if (container instanceof JToolBar) {
            JToolBar toolBar = (JToolBar)container;
            toolBar.addSeparator();
          }
        }
        for (final Component component : components) {
          container.add(component);
        }
      }
    }
  }
}
