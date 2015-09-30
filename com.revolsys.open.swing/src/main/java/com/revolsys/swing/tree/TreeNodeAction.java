package com.revolsys.swing.tree;

import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.swing.Icons;
import com.revolsys.swing.action.AbstractAction;
import com.revolsys.swing.action.ConsumerObjectAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.node.BaseTreeNode;

@SuppressWarnings("serial")
public class TreeNodeAction<T extends BaseTreeNode> extends ConsumerObjectAction<T> {
  public static <V extends BaseTreeNode> void addMenuItem(final MenuFactory menu,
    final String groupName, final CharSequence name, final String iconName,
    final Consumer<V> consumer) {
    final AbstractAction action = createAction(name, iconName, null, consumer);
    menu.addMenuItem(groupName, action);
  }
  public static <V extends BaseTreeNode> void addMenuItem(final MenuFactory menu,
    final String groupName, final CharSequence name, final String iconName,
    final EnableCheck enableCheck,  final Consumer<V> consumer) {
    final AbstractAction action = createAction(name, iconName, enableCheck, consumer);
    menu.addMenuItem(groupName, action);
  }

  public static <V extends BaseTreeNode> AbstractAction createAction(final CharSequence name,
    final Icon icon, final EnableCheck enableCheck, final Consumer<V> consumer) {
    final TreeNodeAction<V> action = new TreeNodeAction<V>(name, name.toString(), icon, true,
      consumer);
    action.setEnableCheck(enableCheck);

    return action;
  }

  public static <V extends BaseTreeNode> AbstractAction createAction(final CharSequence name,
    final String iconName, final Consumer<V> consumer) {
    final ImageIcon icon = Icons.getIcon(iconName);
    return createAction(name, icon, null, consumer);
  }

  public static <V extends BaseTreeNode> AbstractAction createAction(final CharSequence name,
    final String iconName, final EnableCheck enableCheck, final Consumer<V> consumer) {
    final ImageIcon icon = Icons.getIcon(iconName);
    return createAction(name, icon, enableCheck, consumer);
  }

  protected TreeNodeAction(final CharSequence name, final String toolTipText, final Icon icon,
    final boolean invokeLater, final Consumer<T> consumer) {
    super(name, toolTipText, icon, invokeLater, consumer);
  }

  @Override
  public T getObject() {
    return BaseTree.getMenuNode();
  }
}
