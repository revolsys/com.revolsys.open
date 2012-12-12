package com.revolsys.swing.list;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;

import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.TransferHandler;

import com.revolsys.util.Reorderable;

public class ListReorderableTransferHandler extends TransferHandler {
  private static final long serialVersionUID = 1L;

  private final DataFlavor localObjectFlavor = new DataFlavor(Integer.class,
    "Integer Row Index");

 private  String mimeType = localObjectFlavor.getMimeType();

  private JList list;

  public ListReorderableTransferHandler(JList list) {
    this.list = list;
  }

  @Override
  protected Transferable createTransferable(JComponent c) {
    assert (c == list);
    int selectedRow = list.getSelectedIndex();
    if (c == list) {

      return new DataHandler(selectedRow, mimeType);
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
    int index = dropLocation.getIndex();
    ListModel model = list.getModel();
    int max = model.getSize();
    if (index < 0 || index > max) {
      index = max;
    }
    target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    try {
      Transferable transferable = info.getTransferable();
      Integer rowFrom = (Integer)transferable.getTransferData(localObjectFlavor);
      if (rowFrom != -1 && rowFrom != index) {
        ((Reorderable)model).reorder(rowFrom, index);
        if (index > rowFrom) {
          index--;
        }
        target.getSelectionModel().addSelectionInterval(index, index);
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
