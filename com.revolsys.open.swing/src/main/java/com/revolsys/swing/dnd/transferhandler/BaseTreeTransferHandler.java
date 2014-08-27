package com.revolsys.swing.dnd.transferhandler;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import org.slf4j.LoggerFactory;

import com.revolsys.swing.dnd.transferable.TreePathListTransferable;
import com.revolsys.swing.tree.node.BaseTreeNode;

public class BaseTreeTransferHandler extends TransferHandler {

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

  public BaseTreeTransferHandler() {
  }

  @Override
  public boolean canImport(final TransferSupport support) {
    final Component component = support.getComponent();
    if (component instanceof JTree) {
      final JTree.DropLocation loc = (JTree.DropLocation)support.getDropLocation();
      final TreePath path = loc.getPath();
      if (path != null) {
        final Object pathItem = path.getLastPathComponent();
        if (pathItem instanceof BaseTreeNode) {
          final BaseTreeNode node = (BaseTreeNode)pathItem;
          return node.isDndCanImport(path, support);
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
      if (selectedPaths != null) {
        final TreePathListTransferable transferable = new TreePathListTransferable(
          selectedPaths);
        return transferable;
      }
    }
    return null;
  }

  @Override
  protected void exportDone(final JComponent component,
    final Transferable transferable, final int action) {
    if (isDndMoveAction(action)) {
      try {
        final Object data = transferable.getTransferData(TreePathListTransferable.FLAVOR);
        if (data instanceof TreePathListTransferable) {
          final TreePathListTransferable pathTransferable = (TreePathListTransferable)data;
          final Collection<TreePath> paths = pathTransferable.getPaths();
          for (final TreePath path : paths) {
            final TreePath parentPath = path.getParentPath();
            final Object parent = parentPath.getLastPathComponent();
            if (parent instanceof BaseTreeNode) {
              final BaseTreeNode parentNode = (BaseTreeNode)parent;
              boolean removed = false;
              if (pathTransferable.isMoved(path)) {
                removed = parentNode.removeChild(path);
              }
              // if (!removed) {
              // this.model.fireTreeNodesChanged(parentPath);
              // this.model.fireTreeNodesChanged(path);
              // }
            }

          }
          pathTransferable.reset();
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
    if (component instanceof JTree) {
      final JTree.DropLocation loc = (JTree.DropLocation)support.getDropLocation();
      final TreePath path = loc.getPath();
      final int index = loc.getChildIndex();
      if (path != null) {
        try {
          final Object pathItem = path.getLastPathComponent();
          if (pathItem instanceof BaseTreeNode) {
            final BaseTreeNode node = (BaseTreeNode)pathItem;
            return node.dndImportData(support, index);
          }
        } catch (final Exception e) {
          LoggerFactory.getLogger(getClass()).error("Cannot import data", e);
          return false;
        }
      }
    }
    return false;
  }
}
