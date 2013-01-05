package com.revolsys.swing.list;

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

import com.revolsys.util.Reorderable;

public class ListReorderableTransferHandler extends TransferHandler {
  private static final long serialVersionUID = 1L;

  private final DataFlavor localObjectFlavor = new DataFlavor(int[].class,
    "Integer[]");

  private String mimeType = localObjectFlavor.getMimeType();

  private JList list;

  public ListReorderableTransferHandler(JList list) {
    this.list = list;
  }

  @Override
  protected Transferable createTransferable(JComponent c) {
    assert (c == list);
    int[] selectedRows = list.getSelectedIndices();
    if (c == list) {
      return new DataHandler(selectedRows, mimeType);
    } else {
      return null;
    }
  }

  @Override
  public boolean canImport(TransferHandler.TransferSupport info) {
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
  public int getSourceActions(JComponent c) {
    return TransferHandler.COPY_OR_MOVE;
  }

  @Override
  public boolean importData(TransferHandler.TransferSupport info) {
    JList target = (JList)info.getComponent();
    JList.DropLocation dropLocation = (JList.DropLocation)info.getDropLocation();
    int dropIndex = dropLocation.getIndex();
    ListModel model = list.getModel();
    int max = model.getSize();
    if (dropIndex < 0 || dropIndex > max) {
      dropIndex = max;
    }
    target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    try {
      Transferable transferable = info.getTransferable();
      int[] indices = (int[])transferable.getTransferData(localObjectFlavor);
      if (indices.length > 0) {
        Reorderable reorderable = (Reorderable)model;
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
        ListSelectionModel selectionModel = target.getSelectionModel();
        selectionModel.clearSelection();
        selectionModel.addSelectionInterval(currentIndex - indices.length,
          currentIndex - 1);
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  protected void exportDone(JComponent c, Transferable t, int action) {
    if (action == TransferHandler.MOVE) {
      list.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
  }

}
