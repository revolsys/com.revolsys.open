package com.revolsys.swing.tree;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.MenuElement;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.revolsys.swing.tree.model.node.AbstractTreeNode;

public final class TreeUtil {
  public static void fireTreeNodeRemoved(final AbstractTreeNode node) {
    final DefaultTreeModel treeModel = node.getTreeModel();
    final TreeModelListener[] listeners = treeModel.getTreeModelListeners();
    final JTree tree = node.getTree();
    final TreePath treePath = node.getTreePath();
    final TreeModelEvent event = new TreeModelEvent(tree, treePath);
    for (final TreeModelListener listener : listeners) {
      listener.treeNodesRemoved(event);
    }
  }

  @SuppressWarnings("unchecked")
  public static <L> L getFirstSelectedNode(final Object source,
    final Class<L> nodeClass) {
    final List<Object> nodes = getSelectedNodes(source);
    if (!nodes.isEmpty()) {
      final Object node = nodes.get(0);
      final Class<?> clazz = node.getClass();
      if (nodeClass.isAssignableFrom(clazz)) {
        return (L)node;
      }
    }
    return null;
  }

  public static Component getPopupMenuInvoker(final Component component) {
    if (component instanceof JPopupMenu) {
      final JPopupMenu popupMenu = (JPopupMenu)component;
      final Component invoker = popupMenu.getInvoker();
      if (invoker == null) {
        return component;
      } else {
        return getPopupMenuInvoker(invoker);
      }
    } else if (component instanceof MenuElement) {
      final Container parent = component.getParent();
      if (parent == null) {
        return component;
      } else {
        return getPopupMenuInvoker(parent);
      }
    } else {
      return component;
    }
  }

  public static List<Object> getSelectedNodes(final Object source) {
    final List<Object> nodes = new ArrayList<Object>();
    if (source instanceof JMenuItem) {
      final JMenuItem menuItem = (JMenuItem)source;
      final Component invoker = getPopupMenuInvoker(menuItem);
      if (invoker instanceof JTree) {
        final JTree tree = (JTree)invoker;
        final TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
          for (final TreePath treePath : paths) {
            final Object node = treePath.getLastPathComponent();
            nodes.add(node);
          }
        }
      }
    }
    return nodes;
  }

  @SuppressWarnings("unchecked")
  public static <L> List<L> getSelectedNodes(final Object source,
    final Class<L> nodeClass) {
    final List<L> nodes = new ArrayList<L>();
    final List<Object> selectedNodes = getSelectedNodes(source);
    for (final Object node : selectedNodes) {
      final Class<?> clazz = node.getClass();
      if (nodeClass.isAssignableFrom(clazz)) {
        nodes.add((L)node);
      }
    }
    return nodes;
  }

  private TreeUtil() {

  }
}
