package com.revolsys.swing.tree;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.tree.TreePath;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.ObjectTreeNodeModel;
import com.revolsys.swing.tree.renderer.ObjectModelTreeCellRenderer;

@SuppressWarnings("serial")
public class ObjectTree extends JTree implements PropertyChangeListener {

  @SuppressWarnings("unchecked")
  public static <V> V getMouseClickItem() {
    return (V)mouseClickItem;
  }

  private ObjectTreeModel model;

  private DragSource dragSource;

  private boolean menuEnabled = true;

  private static Object mouseClickItem = null;

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

    addMouseListener(new MouseAdapter() {
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
      public void mousePressed(final MouseEvent e) {
        final int x = e.getX();
        final int y = e.getY();
        final TreePath path = ObjectTree.this.getPathForLocation(x, y);
        if (path != null) {
          final ObjectTreeNodeModel<Object, Object> nodeModel = model.getNodeModel(path);
          if (nodeModel != null) {
            final Object node = path.getLastPathComponent();
            if (e.isPopupTrigger()) {
              mouseClickItem = node;
              popup(model, e);
              repaint();
            }
          }
        }
      }

      @Override
      public void mouseReleased(final MouseEvent e) {
        if (e.isPopupTrigger()) {
          popup(model, e);
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
              final JPopupMenu menu = nodeModel.getMenu(node)
                .createJPopupMenu();
              int numItems = menu.getSubElements().length;
              if (menu != null && numItems > 0) {
                mouseClickItem = node;
                menu.show(ObjectTree.this, x, y);
                // TODO add listener to set item=null
              }
            }
          }
        }
      }
    });
    ObjectTree.this.setDropMode(DropMode.ON_OR_INSERT);
    ObjectTree.this.setTransferHandler(new TransferHandler() {
      @Override
      public boolean canImport(final TransferSupport support) {
        if (support.isDataFlavorSupported(TreePathListTransferable.FLAVOR)) {
          final Component c = support.getComponent();
          if (c instanceof JTree) {
            final JTree.DropLocation loc = (JTree.DropLocation)support.getDropLocation();
            final TreePath path = loc.getPath();
            if (path != null) {
              final ObjectTreeNodeModel<Object, Object> nodeModel = model.getNodeModel(path);
              if (nodeModel != null) {
                final Set<Class<?>> supportedClasses = nodeModel.getSupportedChildClasses();
                try {
                  final Transferable transferable = support.getTransferable();
                  final Object data = transferable.getTransferData(TreePathListTransferable.FLAVOR);
                  if (data instanceof TreePathListTransferable) {
                    final TreePathListTransferable pathTransferable = (TreePathListTransferable)data;
                    final List<TreePath> pathList = pathTransferable.getPaths();
                    for (final TreePath treePath : pathList) {
                      if (!isDropSupported(treePath, supportedClasses)) {
                        return false;
                      }
                    }
                  }
                  support.setShowDropLocation(true);
                  return true;

                } catch (final Exception e) {
                  return false;
                }
              }
            }

          }
        }
        return false;
      }

      @Override
      protected Transferable createTransferable(final JComponent c) {
        if (c instanceof JTree) {
          final JTree tree = (JTree)c;
          final TreePath[] selectedPaths = tree.getSelectionPaths();
          return new TreePathListTransferable(selectedPaths);
        } else {
          return null;
        }
      }

      @Override
      protected void exportDone(final JComponent c,
        final Transferable transferable, final int action) {
        try {
          final Object data = transferable.getTransferData(TreePathListTransferable.FLAVOR);
          if (data instanceof TreePathListTransferable) {
            final TreePathListTransferable pathTransferable = (TreePathListTransferable)data;
            final List<TreePath> pathList = pathTransferable.getPaths();
            for (final TreePath treePath : pathList) {
              final TreePath parentPath = treePath.getParentPath();
              final Object parent = parentPath.getLastPathComponent();
              if (!pathTransferable.isSameParent(treePath)) {
                final ObjectTreeNodeModel<Object, Object> nodeModel = model.getNodeModel(parentPath);
                if (nodeModel != null) {
                  final Object child = treePath.getLastPathComponent();
                  nodeModel.removeChild(parent, child);
                }
              }
            }
          }
        } catch (final UnsupportedFlavorException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (final IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

      @Override
      public int getSourceActions(final JComponent c) {
        if (c instanceof JTree) {
          return MOVE;

        } else {
          return NONE;
        }
      }

      @Override
      public boolean importData(final TransferSupport support) {
        if (support.isDataFlavorSupported(TreePathListTransferable.FLAVOR)) {
          final Component c = support.getComponent();
          if (c instanceof JTree) {
            final JTree.DropLocation loc = (JTree.DropLocation)support.getDropLocation();
            final TreePath path = loc.getPath();
            int index = loc.getChildIndex();
            if (path != null) {
              final Object node = path.getLastPathComponent();
              final ObjectTreeNodeModel<Object, Object> nodeModel = model.getNodeModel(path);
              if (nodeModel != null) {
                try {
                  final Transferable transferable = support.getTransferable();
                  final Object data = transferable.getTransferData(TreePathListTransferable.FLAVOR);
                  if (data instanceof TreePathListTransferable) {
                    final TreePathListTransferable pathTransferable = (TreePathListTransferable)data;
                    final List<TreePath> pathList = pathTransferable.getPaths();
                    for (final TreePath treePath : pathList) {
                      final Object child = treePath.getLastPathComponent();
                      final int childIndex = nodeModel.getIndexOfChild(node,
                        child);
                      if (childIndex > -1) {
                        nodeModel.removeChild(node, child);
                        pathTransferable.setSameParent(treePath);
                      }
                      if (index != -1) {
                        if (childIndex > -1 && childIndex < index) {
                          index--;
                        }
                        nodeModel.addChild(node, index, child);
                        index++;
                      } else {
                        nodeModel.addChild(node, child);
                      }
                    }
                  }
                  return true;

                } catch (final Exception e) {
                  e.printStackTrace();
                  return false;
                }
              }
            }
          }
        }
        return false;
      }

      private boolean isDropSupported(final TreePath treePath,
        final Set<Class<?>> supportedClasses) {
        final Object value = treePath.getLastPathComponent();
        final Class<?> valueClass = value.getClass();
        for (final Class<?> supportedClass : supportedClasses) {
          if (supportedClass.isAssignableFrom(valueClass)) {
            return true;
          }
        }
        return false;
      }
    });
    ObjectTree.this.setDragEnabled(true);
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
