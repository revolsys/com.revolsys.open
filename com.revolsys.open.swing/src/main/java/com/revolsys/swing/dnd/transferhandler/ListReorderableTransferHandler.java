package com.revolsys.swing.dnd.transferhandler;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;

import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

import org.slf4j.LoggerFactory;

import com.revolsys.util.Reorderable;

public class ListReorderableTransferHandler extends TransferHandler {
  private static final long serialVersionUID = 1L;

  private final DataFlavor localObjectFlavor = new DataFlavor(int[].class,
    "Integer[]");

  private final String mimeType = localObjectFlavor.getMimeType();

  private final JList list;

  public ListReorderableTransferHandler(final JList list) {
    this.list = list;
  }

  @Override
  public boolean canImport(final TransferHandler.TransferSupport info) {
    if (info.getComponent() == list) {
      if (info.isDrop()) {
        if (info.isDataFlavorSupported(localObjectFlavor)) {
          list.setCursor(DragSource.DefaultMoveDrop);
          return true;
        }
      }
    }
    list.setCursor(DragSource.DefaultMoveNoDrop);
    return false;
  }

  @Override
  protected Transferable createTransferable(final JComponent c) {
    assert (c == list);
    final int[] selectedRows = list.getSelectedIndices();
    if (c == list) {
      return new DataHandler(selectedRows, mimeType);
    } else {
      return null;
    }
  }

  @Override
  protected void exportDone(final JComponent c, final Transferable t,
    final int action) {
    if (action == TransferHandler.MOVE) {
      list.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
  }

  @Override
  public int getSourceActions(final JComponent c) {
    return TransferHandler.COPY_OR_MOVE;
  }

  @Override
  public boolean importData(final TransferHandler.TransferSupport info) {
    final JList target = (JList)info.getComponent();
    final JList.DropLocation dropLocation = (JList.DropLocation)info.getDropLocation();
    int dropIndex = dropLocation.getIndex();
    final ListModel model = list.getModel();
    final int max = model.getSize();
    if (dropIndex < 0 || dropIndex > max) {
      dropIndex = max;
    }
    target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    try {
      final Transferable transferable = info.getTransferable();
      final int[] indices = (int[])transferable.getTransferData(localObjectFlavor);
      if (indices.length > 0) {
        final Reorderable reorderable = (Reorderable)model;
        int currentIndex = dropIndex;
        int count = 0;
        for (int index : indices) {
          if (count > 0) {
            if (currentIndex > index) {
              index -= count;
            }
          }
          count++;
          if (index == currentIndex) {
            currentIndex++;
          } else {
            reorderable.reorder(index, currentIndex);
            if (index > currentIndex) {
              currentIndex++;
            }
          }
        }
        final ListSelectionModel selectionModel = target.getSelectionModel();
        selectionModel.clearSelection();
        selectionModel.addSelectionInterval(currentIndex - indices.length,
          currentIndex - 1);
        return true;
      }
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error("Unexpected error", e);
    }
    return false;
  }

}
