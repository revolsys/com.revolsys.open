package com.revolsys.swing.tree;

import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.swing.Icons;
import com.revolsys.swing.action.AbstractAction;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.action.enablecheck.AbstractEnableCheck;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.node.BaseTreeNode;

public class MenuSourceRunnable extends InvokeMethodRunnable {

  @SuppressWarnings("unchecked")
  public static AbstractAction create(final Map<String, Object> config) {
    final String name = (String)config.get("name");
    final String iconName = (String)config.get("icon");
    final String methodName = (String)config.get("methodName");
    final Object[] parameters = new Object[0]; // TODO
    final Map<String, Object> enableCheckConfig = (Map<String, Object>)config.get("enableCheck");
    final EnableCheck enableCheck = AbstractEnableCheck.enableCheck(enableCheckConfig);
    return createAction(name, iconName, enableCheck, methodName, parameters);
  }

  public static InvokeMethodAction createAction(final CharSequence name, final Icon icon,
    final EnableCheck enableCheck, final String methodName, final Object... parameters) {
    final MenuSourceRunnable runnable = new MenuSourceRunnable(methodName, parameters);
    final InvokeMethodAction action = new InvokeMethodAction(name, null, icon, true, runnable);
    action.setEnableCheck(enableCheck);

    return action;
  }

  public static InvokeMethodAction createAction(final CharSequence name, final Icon icon,
    final String methodName, final Object... parameters) {
    final InvokeMethodAction action = createAction(name, icon, null, methodName, parameters);
    return action;
  }

  public static InvokeMethodAction createAction(final CharSequence name, final String iconName,
    final EnableCheck enableCheck, final String methodName, final Object... parameters) {
    final ImageIcon icon = Icons.getIcon(iconName);
    final InvokeMethodAction action = createAction(name, icon, enableCheck, methodName, parameters);
    action.setIconName(iconName);
    return action;
  }

  public static InvokeMethodAction createAction(final CharSequence name, final String iconName,
    final String methodName, final Object... parameters) {
    final ImageIcon icon = Icons.getIcon(iconName);
    final InvokeMethodAction action = createAction(name, icon, null, methodName, parameters);
    action.setIconName(iconName);
    return action;
  }

  protected MenuSourceRunnable(final String methodName, final Object[] parameters) {
    super(methodName, parameters);
  }

  @Override
  public Object getObject() {
    final Object item = MenuFactory.getMenuSource();
    if (item instanceof BaseTreeNode) {
      final BaseTreeNode node = (BaseTreeNode)item;
      return node.getUserObject();
    }
    return item;
  }

}
