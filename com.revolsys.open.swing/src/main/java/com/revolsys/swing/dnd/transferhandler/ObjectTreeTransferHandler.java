package com.revolsys.swing.dnd.transferhandler;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import org.slf4j.LoggerFactory;

import com.revolsys.swing.dnd.transferable.TreePathListTransferable;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.ObjectTreeNodeModel;

public class ObjectTreeTransferHandler extends TransferHandler {

  public static boolean isDndCopyAction(final int dropAction) {
    return (dropAction & DnDConstants.ACTION_COPY) == DnDConstants.ACTION_COPY;
  }

  public static boolean isDndCopyAction(final TransferSupport support) {
    final int dropAction = support.getDropAction();
    return isDndCopyAction(dropAction);
  }

  public static boolean isDndMoveAction(final int dropAction) {
    return (dropAction & DnDConstants.ACTION_MOVE) == DnDConstants.ACTION_MOVE;
  }

  public static boolean isDndMoveAction(final TransferSupport support) {
    final int dropAction = support.getDropAction();
    return isDndMoveAction(dropAction);
  }

  public static boolean isDndNoneAction(final int dropAction) {
    return dropAction == DnDConstants.ACTION_NONE;
  }

  public static boolean isDndNoneAction(final TransferSupport support) {
    final int dropAction = support.getDropAction();
    return isDndNoneAction(dropAction);
  }

  private static final long serialVersionUID = 1L;

  private final ObjectTreeModel model;

  public ObjectTreeTransferHandler(final ObjectTreeModel model) {
    this.model = model;
  }

  @Override
  public boolean canImport(final TransferSupport support) {
    final Component component = support.getComponent();
    if (component instanceof JTree) {
      final JTree.DropLocation loc = (JTree.DropLocation)support.getDropLocation();
      final TreePath path = loc.getPath();
      if (path != null) {
        final ObjectTreeNodeModel<Object, Object> nodeModel = this.model.getNodeModel(path);
        if (nodeModel != null) {
          if (nodeModel.isDndCanImport(path, support)) {
            return true;
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
      final TreePathListTransferable transferable = new TreePathListTransferable(
        selectedPaths);
      return transferable;
    } else {
      return null;
    }
  }

  @Override
  protected void exportDone(final JComponent component,
    final Transferable transferable, final int action) {
    if (isDndMoveAction(action)) {
      try {
        final Object data = transferable.getTransferData(TreePathListTransferable.FLAVOR);
        if (data instanceof TreePathListTransferable) {
          final TreePathListTransferable pathTransferable = (TreePathListTransferable)data;
          final List<TreePath> pathList = pathTransferable.getPaths();
          for (final TreePath treePath : pathList) {
            final TreePath parentPath = treePath.getParentPath();
            final Object parent = parentPath.getLastPathComponent();
            final ObjectTreeNodeModel<Object, Object> nodeModel = this.model.getNodeModel(parentPath);
            if (nodeModel != null) {
              if (pathTransferable.isSameParent(treePath)) {
                this.model.fireTreeNodesChanged(treePath);
              } else {
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
      return COPY_OR_MOVE;
    } else {
      return NONE;
    }
  }

  @Override
  public boolean importData(final TransferSupport support) {
    final Component component = support.getComponent();
    if (component instanceof ObjectTree) {
      final JTree.DropLocation loc = (JTree.DropLocation)support.getDropLocation();
      final TreePath path = loc.getPath();
      final int index = loc.getChildIndex();
      if (path != null) {
        final ObjectTreeNodeModel<Object, Object> nodeModel = this.model.getNodeModel(path);
        if (nodeModel != null) {
          try {
            final Object node = path.getLastPathComponent();
            nodeModel.dndImportData(support, node, index);
          } catch (final Exception e) {
            LoggerFactory.getLogger(getClass()).error("Cannot import data", e);
            return false;
          }
        }
        component.repaint();
      }
    }
    return false;
  }
}
