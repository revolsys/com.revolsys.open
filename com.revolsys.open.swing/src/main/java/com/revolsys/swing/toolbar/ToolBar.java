package com.revolsys.swing.toolbar;

import java.awt.Component;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.action.InvokeMethodAction;

@SuppressWarnings("serial")
public class ToolBar extends JToolBar {
  private final Map<String, List<Component>> groups = new LinkedHashMap<String, List<Component>>();

  public ToolBar() {
    setRollover(true);
  }

  @Override
  public JButton add(final Action action) {
    final JButton button = super.add(action);
    addComponent("default", button);
    return button;
  }

  @Override
  public Component add(final Component component) {
    if (component instanceof Separator) {
      super.add(component);
    } else {
      addComponent("default", component);
    }
    return component;
  }

  public JButton addButton(final String groupName, final String name,
    final String title, final Icon icon, final Object object,
    final String methodName, final Object... parameters) {
    final InvokeMethodAction action = new InvokeMethodAction(name, title, icon,
      object, methodName, parameters);

    final JButton button = createActionComponent(action);
    button.setFocusPainted(false);
    button.setAction(action);
    addComponent(groupName, button);
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
    final List<Component> components = getGroup(groupName);
    components.add(component);
    updateComponents();
  }

  public void addGroup(final String groupName) {
    getGroup(groupName);
  }

  protected List<Component> getGroup(final String groupName) {
    List<Component> components = groups.get(groupName);
    if (components == null) {
      components = new ArrayList<Component>();
      groups.put(groupName, components);
    }
    return components;
  }

  public void setGroupEnabled(final String groupName, final boolean enabled) {
    final List<Component> components = getGroup(groupName);
    for (final Component component : components) {
      component.setEnabled(enabled);
    }
  }

  private void updateComponents() {
    removeAll();
    boolean first = true;
    for (final List<Component> components : groups.values()) {
      if (!components.isEmpty()) {
        if (first) {
          first = false;
        } else {
          addSeparator();
        }
        for (final Component component : components) {
          super.add(component);
        }
      }
    }
  }
}
