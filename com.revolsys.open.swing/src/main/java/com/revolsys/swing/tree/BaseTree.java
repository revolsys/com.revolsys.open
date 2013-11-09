package com.revolsys.swing.tree;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.AbstractTreeNode;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;

public class BaseTree extends JTree implements MouseListener,
  TreeWillExpandListener, TreeExpansionListener {
  private static final long serialVersionUID = 1L;

  private boolean menuEnabled = true;

  static Object mouseClickItem = null;

  @SuppressWarnings("unchecked")
  public static <V> V getMouseClickItem() {
    return (V)mouseClickItem;
  }

  public static void showMenu(final MenuFactory menuFactory,
    final Object object, final Component component, final int x, final int y) {
    if (menuFactory != null) {
      mouseClickItem = object;
      final JPopupMenu menu = menuFactory.createJPopupMenu();
      if (menu != null) {
        final int numItems = menu.getSubElements().length;
        if (menu != null && numItems > 0) {
          final Window window = SwingUtilities.windowForComponent(component);
          if (window != null) {
            if (window.isAlwaysOnTop()) {
              window.setAlwaysOnTop(true);
              window.setAlwaysOnTop(false);
            }
            window.toFront();
          }
          menu.show(component, x, y);
          // TODO add listener to set item=null
        }
      }
    }
  }

  public static void showMenu(final Object object, final Component component,
    final int x, final int y) {
    if (object != null) {
      final MenuFactory menu = ObjectTreeModel.findMenu(object);
      showMenu(menu, object, component, x, y);
    }
  }

  public BaseTree() {
    this(getDefaultTreeModel());
  }

  public BaseTree(final TreeModel newModel) {
    super(newModel);
    final Object root = newModel.getRoot();
    if (root instanceof AbstractTreeNode) {
      final AbstractTreeNode treeNode = (AbstractTreeNode)root;
      treeNode.setTree(this);
    }
    addTreeWillExpandListener(this);
    addTreeExpansionListener(this);
    addMouseListener(this);
  }

  @Override
  public void collapsePath(final TreePath path) {
    final Object node = path.getLastPathComponent();
    if (node instanceof AbstractTreeNode) {
      final AbstractTreeNode treeNode = (AbstractTreeNode)node;
      treeNode.collapseChildren();
      treeNode.nodeCollapsed(treeNode);
    }
    super.collapsePath(path);
  }

  public MenuFactory getMenuFactory(final TreePath path) {
    final Object node = path.getLastPathComponent();
    if (node instanceof AbstractTreeNode) {
      final AbstractTreeNode treeNode = (AbstractTreeNode)node;
      return treeNode.getMenu();
    } else {
      return null;
    }
  }

  public boolean isMenuEnabled() {
    return menuEnabled;
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
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
      final int x = e.getX();
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
        final Object node = path.getLastPathComponent();
        showMenu(menu, node, this, x, y);
      }
    }
  }

  public void setMenuEnabled(final boolean menuEnabled) {
    this.menuEnabled = menuEnabled;
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
