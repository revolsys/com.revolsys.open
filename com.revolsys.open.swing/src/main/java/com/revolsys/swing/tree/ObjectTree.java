package com.revolsys.swing.tree;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.tree.TreePath;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.swing.dnd.transferhandler.ObjectTreeTransferHandler;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.ObjectTreeNodeModel;
import com.revolsys.swing.tree.renderer.ObjectModelTreeCellRenderer;

@SuppressWarnings("serial")
public class ObjectTree extends JTree implements PropertyChangeListener,
  MouseListener {

  @SuppressWarnings("unchecked")
  public static <V> V getMouseClickItem() {
    return (V)mouseClickItem;
  }

  private final ObjectTreeModel model;

  private boolean menuEnabled = true;

  private static Object mouseClickItem = null;

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

  public ObjectTree(final ObjectTreeModel model) {
    super(model);
    this.model = model;
    setToggleClickCount(0);
    final Object root = model.getRoot();
    if (root instanceof PropertyChangeSupportProxy) {
      final PropertyChangeSupportProxy propProxy = (PropertyChangeSupportProxy)root;
      propProxy.getPropertyChangeSupport().addPropertyChangeListener(this);

    }
    final ObjectTreeNodeModel<Object, Object> rootModel = model.getNodeModel(root);
    final TreePath rootPath = new TreePath(root);
    model.initializeNode(rootPath, rootModel, root);

    addTreeExpansionListener(model);
    addTreeWillExpandListener(model);
    model.treeExpanded(new TreeExpansionEvent(this, rootPath));

    setCellRenderer(new ObjectModelTreeCellRenderer(model));
    setLayout(new BorderLayout());
    setTransferHandler(new ObjectTreeTransferHandler(model));
    setDragEnabled(true);
    setDropMode(DropMode.ON_OR_INSERT);

    addMouseListener(this);
  }

  @Override
  public String convertValueToText(final Object value, final boolean selected,
    final boolean expanded, final boolean leaf, final int row,
    final boolean hasFocus) {
    if (model != null) {
      if (value != null) {
        final TreePath path = model.getPath(value);
        if (path != null) {
          final ObjectTreeNodeModel<Object, Object> nodeModel = model.getNodeModel(path);
          if (nodeModel != null) {
            return nodeModel.convertValueToText(value, selected, expanded,
              leaf, row, hasFocus);
          }
        }
      }
    }
    return super.convertValueToText(value, selected, expanded, leaf, row,
      hasFocus);
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> getSelectedItems(final Class<T> requiredClass) {
    final List<T> values = new ArrayList<T>();
    final TreePath[] selectionPaths = getSelectionPaths();
    if (selectionPaths != null) {
      for (final TreePath path : selectionPaths) {
        final Object object = path.getLastPathComponent();
        final Class<? extends Object> objectClass = object.getClass();
        if (requiredClass.isAssignableFrom(objectClass)) {
          values.add((T)object);
        }
      }
    }
    return values;
  }

  public boolean isMenuEnabled() {
    return menuEnabled;
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    final int x = e.getX();
    final int y = e.getY();
    final TreePath path = ObjectTree.this.getPathForLocation(x, y);
    if (path != null) {
      final ObjectTreeNodeModel<Object, Object> nodeModel = model.getNodeModel(path);
      if (nodeModel != null) {
        final Object node = path.getLastPathComponent();
        final MouseListener listener = nodeModel.getMouseListener(node);
        if (listener != null) {
          try {
            mouseClickItem = node;
            listener.mouseClicked(e);
          } finally {
          }
        }
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
      popup(model, e);
      repaint();
    }
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if (e.isPopupTrigger()) {
      popup(model, e);
      repaint();
    }
  }

  private void popup(final ObjectTreeModel model, final MouseEvent e) {
    if (menuEnabled) {
      final int x = e.getX();
      final int y = e.getY();
      final TreePath path = ObjectTree.this.getPathForLocation(x, y);
      if (path != null) {

        TreePath[] selectionPaths = getSelectionPaths();
        if (selectionPaths == null
          || !Arrays.asList(selectionPaths).contains(path)) {
          selectionPaths = new TreePath[] {
            path
          };
          setSelectionPaths(selectionPaths);
        }

        final Object node = path.getLastPathComponent();
        final ObjectTreeNodeModel<Object, Object> nodeModel = model.getNodeModel(path);
        if (nodeModel != null) {
          final MenuFactory menu = nodeModel.getMenu(node);
          showMenu(menu, node, this, x, y);
        }
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (event instanceof IndexedPropertyChangeEvent) {
      final IndexedPropertyChangeEvent indexedEvent = (IndexedPropertyChangeEvent)event;
      final Object source = event.getSource();
      final int index = indexedEvent.getIndex();
      final Object oldValue = event.getOldValue();
      final Object newValue = event.getNewValue();
      if (oldValue == null) {
        final TreePath path = model.getPath(source);
        if (path != null) {
          model.fireTreeNodesInserted(path, index, newValue);
        }
      } else if (newValue == null) {
        final TreePath path = model.getPath(source);
        if (path != null) {
          model.fireTreeNodesRemoved(path, index, oldValue);
        }
      }

    }
  }

  public void setMenuEnabled(final boolean menuEnabled) {
    this.menuEnabled = menuEnabled;
  }
}
