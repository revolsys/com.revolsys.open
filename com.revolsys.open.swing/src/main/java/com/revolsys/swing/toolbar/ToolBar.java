package com.revolsys.swing.toolbar;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.action.InvokeMethodAction;

@SuppressWarnings("serial")
public class ToolBar extends JToolBar {
  public JButton add(String name, String title, Icon icon, Object object,
    String methodName, Object... parameters) {
    InvokeMethodAction action = new InvokeMethodAction(name, title, icon,
      object, methodName, parameters);
    return add(action);
  }

  public JButton add(String name, String title, String iconName, Object object,
    String methodName, Object... parameters) {
    ImageIcon icon = SilkIconLoader.getIcon(iconName);
    return add(iconName, title, icon, object, methodName, parameters);
  }
  public JButton addTitleIcon(String title, String iconName, Object object,
    String methodName, Object... parameters) {
    ImageIcon icon = SilkIconLoader.getIcon(iconName);
    return add(iconName, title, icon, object, methodName, parameters);
  }
}
