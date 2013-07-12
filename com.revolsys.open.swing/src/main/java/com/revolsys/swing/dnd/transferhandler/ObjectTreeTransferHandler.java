package com.revolsys.swing.dnd.transferhandler;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import org.slf4j.LoggerFactory;

import com.revolsys.swing.dnd.transferable.TreePathListTransferable;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.ObjectTreeNodeModel;

public class ObjectTreeTransferHandler extends TransferHandler {
  private static final long serialVersionUID = 1L;

  private final ObjectTreeModel model;

  public ObjectTreeTransferHandler(ObjectTreeModel model) {
    this.model = model;
  }

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
      TreePathListTransferable transferable = new TreePathListTransferable(
        selectedPaths);
      return transferable;
    } else {
      return null;
    }
  }

  @Override
  protected void exportDone(final JComponent c,
    final Transferable transferable, final int action) {
    if ((action & MOVE) == MOVE) {
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
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error("Cannot export data", e);
      }
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
                  final int childIndex = nodeModel.getIndexOfChild(node, child);
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
              LoggerFactory.getLogger(getClass())
                .error("Cannot import data", e);
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
}
