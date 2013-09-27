package com.revolsys.swing.tree;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.ObjectTreeNodeModel;
import com.revolsys.swing.tree.renderer.ObjectModelTreeCellRenderer;
import com.revolsys.util.JavaBeanUtil;

public class ObjectTree extends JTree implements PropertyChangeListener,
  MouseListener {

  private static final long serialVersionUID = 1L;

  private static Object mouseClickItem = null;

  public static TreePath createTreePath(final Collection<? extends Object> path) {
    final Object[] pathArray = path.toArray();
    return new TreePath(pathArray);
  }

  public static TreePath createTreePath(final Object... path) {
    return new TreePath(path);
  }

  public static TreePath createTreePathReverse(
    final Collection<? extends Object> path) {
    final List<Object> pathList = new ArrayList<Object>(path);
    Collections.reverse(pathList);
    final Object[] pathArray = pathList.toArray();
    return new TreePath(pathArray);
  }

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

  private final ObjectTreeModel model;

  private boolean menuEnabled = true;

  public ObjectTree(final ObjectTreeModel model) {
    super(model);
    this.model = model;
    setToggleClickCount(0);
    setRowHeight(0);
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
    if (this.model != null) {
      if (value != null) {
        final TreePath path = this.model.getPath(value);
        if (path != null) {
          final ObjectTreeNodeModel<Object, Object> nodeModel = this.model.getNodeModel(path);
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

  public void expandPath(final Object... objects) {
    final TreePath path = new TreePath(objects);
    expandPath(path);
  }

  public void expandPath(final Object object) {
    final TreePath path = model.getPath(object);
    if (path != null) {
      expandPath(path);
    }
  }

  @Override
  public void expandPath(final TreePath path) {
    if (path != null) {
      this.model.initializePath(path);
      super.expandPath(path);
    }
  }

  public void expandPaths(final Collection<Class<?>> expectedClasses,
    final Object object) {
    if (SwingUtilities.isEventDispatchThread()) {
      if (object instanceof PropertyChangeEvent) {
        expandPaths(expectedClasses, (PropertyChangeEvent)object);
      } else if (object != null) {
        if (JavaBeanUtil.isAssignableFrom(expectedClasses, object)) {
          final TreePath path = model.getPath(object);
          if (path != null) {
            expandPath(object);
            final ObjectTreeNodeModel<Object, Object> nodeModel = model.getNodeModel(path);
            if (nodeModel != null) {
              if (nodeModel.isLeaf(object)) {
                model.fireTreeNodesChanged(path);
              } else {
                for (int i = 0; i < model.getChildCount(object); i++) {
                  final Object child = model.getChild(object, i);
                  expandPaths(expectedClasses, child);
                }
              }
            }
          }
        }
      }
    } else {
      Invoke.later(this, "expandPaths", expectedClasses, object);
    }
  }

  public void expandPaths(final Collection<Class<?>> expectedClasses,
    final PropertyChangeEvent event) {
    if (SwingUtilities.isEventDispatchThread()) {
      final Object source = event.getSource();
      if (source != null) {
        if (JavaBeanUtil.isAssignableFrom(expectedClasses, source)) {
          expandPath(source);
          final Object newValue = event.getNewValue();
          expandPaths(expectedClasses, newValue);
        }
      }
    } else {
      Invoke.later(this, "expandPaths", expectedClasses, event);
    }
  }

  public void expandPaths(final IndexedPropertyChangeEvent event) {
    expandPath(event.getSource());
    final Object newValue = event.getNewValue();
    expandPaths(newValue);
  }

  public void expandPaths(final Object parent) {
    final TreePath path = model.getPath(parent);
    if (path != null) {
      expandPath(parent);
      final ObjectTreeNodeModel<Object, Object> nodeModel = model.getNodeModel(path);
      if (nodeModel != null) {
        if (nodeModel.isLeaf(parent)) {
          model.fireTreeNodesChanged(path);
        } else {
          for (int i = 0; i < model.getChildCount(parent); i++) {
            final Object child = model.getChild(parent, i);
            expandPaths(child);
          }
        }
      }
    }
  }

  @Override
  public ObjectTreeModel getModel() {
    return model;
  }

  @Override
  public Rectangle getPathBounds(final TreePath path) {
    if (model.isVisible(path.getLastPathComponent())) {
      return super.getPathBounds(path);
    } else {
      return null;
    }
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
    return this.menuEnabled;
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    final int x = e.getX();
    final int y = e.getY();
    final TreePath path = getPathForLocation(x, y);
    if (path != null) {
      final ObjectTreeNodeModel<Object, Object> nodeModel = this.model.getNodeModel(path);
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
      popup(this.model, e);
      repaint();
    }
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if (e.isPopupTrigger()) {
      popup(this.model, e);
      repaint();
    }
  }

  private void popup(final ObjectTreeModel model, final MouseEvent e) {
    if (this.menuEnabled) {
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
    if (SwingUtilities.isEventDispatchThread()) {
      if (event instanceof IndexedPropertyChangeEvent) {
        final IndexedPropertyChangeEvent indexedEvent = (IndexedPropertyChangeEvent)event;
        final Object source = event.getSource();
        final int index = indexedEvent.getIndex();
        final Object oldValue = event.getOldValue();
        final Object newValue = event.getNewValue();
        if (oldValue == null) {
          final TreePath path = this.model.getPath(source);
          if (path != null) {
            this.model.fireTreeNodesInserted(path, index, newValue);
          }
        } else if (newValue == null) {
          final TreePath path = this.model.getPath(source);
          if (path != null) {
            try {
              this.model.fireTreeNodesRemoved(path, index, oldValue);
            } catch (final ArrayIndexOutOfBoundsException e) {
            }
          }
        } else if (oldValue != newValue) {
          final TreePath path = this.model.getPath(source);
          if (path != null) {
            this.model.fireTreeNodesRemoved(path, index, oldValue);
            this.model.fireTreeNodesInserted(path, index, newValue);
          }
        }

      } else {
        final Object source = event.getSource();
        final TreePath path = this.model.getPath(source);
        if (path != null) {
          this.model.fireTreeNodesChanged(path);
        }
      }
    } else {
      Invoke.later(this, "propertyChange", event);
    }
  }

  public void setMenuEnabled(final boolean menuEnabled) {
    this.menuEnabled = menuEnabled;
  }

  public void setRoot(final Object object) {
    final Object oldRoot = model.getRoot();
    if (oldRoot != null) {
      collapsePath(new TreePath(oldRoot));
    }
    setSelectionPath(null);
    model.setRoot(object);
    if (object != null) {
      final TreePath newPath = new TreePath(object);
      setSelectionPath(newPath);
      expandPath(newPath);
    }
  }

  public void setVisible(final Object object, final boolean visible) {
    model.setVisible(object, visible);
  }
}
