package com.revolsys.swing.tree;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.node.LazyLoadTreeNode;
import com.revolsys.swing.tree.node.OpenStateTreeNode;

public class BaseTreeListener
  implements MouseListener, TreeExpansionListener, TreeModelListener, TreeWillExpandListener {

  private BaseTree tree;

  public BaseTreeListener(final BaseTree baseTree) {
    this.tree = baseTree;
    baseTree.addMouseListener(this);
    baseTree.addTreeExpansionListener(this);
    baseTree.addTreeWillExpandListener(this);
    final DefaultTreeModel model = baseTree.getModel();
    model.addTreeModelListener(this);
  }

  public void close() {
    if (this.tree != null) {
      this.tree.removeMouseListener(this);
      this.tree.removeTreeExpansionListener(this);
      this.tree.removeTreeWillExpandListener(this);
      final DefaultTreeModel model = this.tree.getModel();
      model.removeTreeModelListener(this);
      this.tree = null;
    }
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    final int x = e.getX();
    final int y = e.getY();
    final TreePath path = this.tree.getPathForLocation(x, y);
    if (path != null) {
      final Object node = path.getLastPathComponent();
      if (node instanceof MouseListener) {
        final MouseListener listener = (MouseListener)node;
        Object userObject = null;
        if (node instanceof BaseTreeNode) {
          final BaseTreeNode treeNode = (BaseTreeNode)node;
          userObject = treeNode.getUserObject();
        }
        Object menuSource = userObject;
        if (menuSource == null) {
          menuSource = node;
        }
        listener.mouseClicked(e);
      }
    }
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    if (e.isPopupTrigger()) {
      popup(e);
    }
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if (e.isPopupTrigger()) {
      popup(e);
    }
  }

  private void popup(final MouseEvent e) {
    if (this.tree.isMenuEnabled()) {
      final int x = e.getX() + 5;
      final int y = e.getY();
      final TreePath path = this.tree.getPathForLocation(x, y);
      if (path != null) {

        TreePath[] selectionPaths = this.tree.getSelectionPaths();
        if (selectionPaths == null || !Arrays.asList(selectionPaths).contains(path)) {
          selectionPaths = new TreePath[] {
            path
          };
          this.tree.setSelectionPaths(selectionPaths);
        }

        final MenuFactory menu = this.tree.getMenuFactory(path);
        final BaseTreeNode node = (BaseTreeNode)path.getLastPathComponent();
        BaseTree.setMenuNode(node);
        Object userObject = node.getUserObject();
        if (userObject == null) {
          userObject = node;
        }
        if (menu != null) {
          menu.showMenu(userObject, this.tree, x, y);
        }
      }
      this.tree.repaint();
    }
  }

  @Override
  public void treeCollapsed(final TreeExpansionEvent event) {
  }

  @Override
  public void treeExpanded(final TreeExpansionEvent event) {
  }

  @Override
  public void treeNodesChanged(final TreeModelEvent e) {
  }

  @Override
  public void treeNodesInserted(final TreeModelEvent e) {
    final TreePath treePath = e.getTreePath();
    for (final Object child : e.getChildren()) {
      if (child instanceof OpenStateTreeNode) {
        final OpenStateTreeNode openState = (OpenStateTreeNode)child;
        if (openState.isOpen()) {
          final TreePath childTreePath = treePath.pathByAddingChild(child);
          Invoke.laterQueue(() -> this.tree.setExpandedState(childTreePath, true));
        }
      }
    }
  }

  @Override
  public void treeNodesRemoved(final TreeModelEvent e) {
  }

  @Override
  public void treeStructureChanged(final TreeModelEvent e) {
  }

  @Override
  public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
  }

  @Override
  public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
    final TreePath path = event.getPath();
    final Object node = path.getLastPathComponent();
    if (node instanceof LazyLoadTreeNode) {
      final LazyLoadTreeNode lazyLoadTreeNode = (LazyLoadTreeNode)node;
      lazyLoadTreeNode.loadChildren();
    }
  }
}
