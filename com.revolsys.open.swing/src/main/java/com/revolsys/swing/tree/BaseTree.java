package com.revolsys.swing.tree;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.revolsys.swing.dnd.transferhandler.BaseTreeTransferHandler;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.LazyLoadTreeNode;

public class BaseTree extends JTree implements MouseListener,
  TreeWillExpandListener, TreeExpansionListener {
  private static final long serialVersionUID = 1L;

  private boolean menuEnabled = true;

  public BaseTree(final BaseTreeNode root) {
    super(new DefaultTreeModel(root, true));
    root.setTree(this);
    addTreeWillExpandListener(this);
    addTreeExpansionListener(this);
    addMouseListener(this);
    setRootVisible(true);
    setShowsRootHandles(true);
    setLargeModel(true);
    setToggleClickCount(0);
    setRowHeight(0);
    setCellRenderer(new BaseTreeCellRenderer());
    setExpandsSelectedPaths(true);
    setTransferHandler(new BaseTreeTransferHandler());
    setDragEnabled(true);
    setDropMode(DropMode.ON_OR_INSERT);
    final DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
    selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    setSelectionModel(selectionModel);
  }

  public void collapsePath(final List<Object> items) {
    final TreePath path = getTreePath(items);
    collapsePath(path);
  }

  public void collapsePath(final Object... items) {
    if (items != null) {
      collapsePath(Arrays.asList(items));
    }
  }

  @Override
  public void collapsePath(final TreePath path) {
    final Object node = path.getLastPathComponent();
    if (node instanceof BaseTreeNode) {
      final BaseTreeNode treeNode = (BaseTreeNode)node;
      treeNode.collapseChildren();
      treeNode.nodeCollapsed(treeNode);
    }
    super.collapsePath(path);
  }

  public void expandAllNodes() {
    expandAllNodes(0, getRowCount());
  }

  private void expandAllNodes(final int startingIndex, final int rowCount) {
    for (int i = startingIndex; i < rowCount; ++i) {
      expandRow(i);
    }

    if (getRowCount() != rowCount) {
      expandAllNodes(rowCount, getRowCount());
    }
  }

  public void expandPath(final List<?> items) {
    final TreePath path = getTreePath(items);
    expandPath(path);
  }

  public void expandPath(final Object... items) {
    if (items != null) {
      expandPath(Arrays.asList(items));
    }
  }

  public MenuFactory getMenuFactory(final TreePath path) {
    final Object node = path.getLastPathComponent();
    if (node instanceof BaseTreeNode) {
      final BaseTreeNode treeNode = (BaseTreeNode)node;
      return treeNode.getMenu();
    } else {
      return null;
    }
  }

  public BaseTreeNode getRootNode() {
    final TreeModel model = getModel();
    final BaseTreeNode root = (BaseTreeNode)model.getRoot();
    return root;
  }

  public TreePath getTreePath(final List<?> items) {
    final BaseTreeNode root = getRootNode();
    if (root == null) {
      return null;
    } else {
      return root.getTreePath(items);
    }

  }

  // private void initializePath(final TreePath path) {
  // final TreePath parent = path.getParentPath();
  // if (parent != null) {
  // initializePath(parent);
  // }
  // final Object object = BaseTreeNode.getUserData(path);
  // synchronized (this.objectPathMap) {
  // if (!this.objectPathMap.containsKey(object)) {
  // this.objectPathMap.put(object, path);
  // }
  // }
  // }

  public boolean isMenuEnabled() {
    return this.menuEnabled;
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    final int x = e.getX();
    final int y = e.getY();
    final TreePath path = getPathForLocation(x, y);
    if (path != null) {
      final Object node = path.getLastPathComponent();
      if (node instanceof MouseListener) {
        final MouseListener listener = (MouseListener)node;
        Object userObject = null;
        if (node instanceof BaseTreeNode) {
          final BaseTreeNode treeNode = (BaseTreeNode)node;
          userObject = treeNode.getUserObject();
        }
        if (userObject == null) {
          MenuFactory.setMenuSource(userObject);
        } else {
          MenuFactory.setMenuSource(node);
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
      repaint();
    }
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if (e.isPopupTrigger()) {
      popup(e);
      repaint();
    }
  }

  private void popup(final MouseEvent e) {
    if (this.menuEnabled) {
      final int x = e.getX() + 5;
      final int y = e.getY();
      final TreePath path = getPathForLocation(x, y);
      if (path != null) {

        TreePath[] selectionPaths = getSelectionPaths();
        if (selectionPaths == null
          || !Arrays.asList(selectionPaths).contains(path)) {
          selectionPaths = new TreePath[] {
            path
          };
          setSelectionPaths(selectionPaths);
        }

        final MenuFactory menu = getMenuFactory(path);
        final BaseTreeNode node = (BaseTreeNode)path.getLastPathComponent();
        Object userObject = node.getUserObject();
        if (userObject == null) {
          userObject = node;
        }
        menu.show(userObject, this, x, y);
      }
    }
  }

  public void setMenuEnabled(final boolean menuEnabled) {
    this.menuEnabled = menuEnabled;
  }

  public void setVisible(final Object object, final boolean visible) {
    // final boolean oldVisible = !this.hiddenObjects.containsKey(object);
    // if (visible) {
    // this.hiddenObjects.remove(object);
    // } else {
    // this.hiddenObjects.put(object, true);
    // }
    // if (visible != oldVisible) {
    // final TreePath path = getPath(object);
    // if (path != null) {
    // getModel().fireTreeNodesChanged(path);
    // }
    // }
  }

  @Override
  public void treeCollapsed(final TreeExpansionEvent event) {

  }

  @Override
  public void treeExpanded(final TreeExpansionEvent event) {
  }

  @Override
  public void treeWillCollapse(final TreeExpansionEvent event)
    throws ExpandVetoException {
  }

  @Override
  public void treeWillExpand(final TreeExpansionEvent event)
    throws ExpandVetoException {
    final TreePath path = event.getPath();
    final Object node = path.getLastPathComponent();
    if (node instanceof LazyLoadTreeNode) {
      final LazyLoadTreeNode lazyLoadTreeNode = (LazyLoadTreeNode)node;
      lazyLoadTreeNode.loadChildren();
    }
  }
}
