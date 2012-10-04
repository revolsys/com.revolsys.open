package com.revolsys.swing.table;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;

import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.TableModel;

public class TableRowTransferHandler extends TransferHandler {
  private static final long serialVersionUID = 1L;

  private final DataFlavor localObjectFlavor = new DataFlavor(Integer.class,
    "Integer Row Index");

 private  String mimeType = localObjectFlavor.getMimeType();

  private JTable table;

  public TableRowTransferHandler(JTable table) {
    this.table = table;
  }

  @Override
  protected Transferable createTransferable(JComponent c) {
    assert (c == table);
    int selectedRow = table.getSelectedRow();
    if (c == table) {

      return new DataHandler(selectedRow, mimeType);
    } else {
      return null;
    }
  }

  @Override
  public boolean canImport(TransferHandler.TransferSupport info) {
    if (info.getComponent() == table) {
      if (info.isDrop()) {
        if (info.isDataFlavorSupported(localObjectFlavor)) {
          table.setCursor(DragSource.DefaultMoveDrop);
          return true;
        }
      }
    }
    table.setCursor(DragSource.DefaultMoveNoDrop);
    return false;
  }

  @Override
  public int getSourceActions(JComponent c) {
    return TransferHandler.COPY_OR_MOVE;
  }

  @Override
  public boolean importData(TransferHandler.TransferSupport info) {
    JTable target = (JTable)info.getComponent();
    JTable.DropLocation dropLocation = (JTable.DropLocation)info.getDropLocation();
    int index = dropLocation.getRow();
    TableModel model = table.getModel();
    int max = model.getRowCount();
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
      table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
  }

}
