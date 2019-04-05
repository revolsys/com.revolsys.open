package com.revolsys.swing.dnd.transferhandler;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;

import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.TableModel;

import org.jeometry.common.logging.Logs;

import com.revolsys.util.Reorderable;

public class TableRowTransferHandler extends TransferHandler {
  private static final long serialVersionUID = 1L;

  private final DataFlavor localObjectFlavor = new DataFlavor(Integer.class, "Integer Row Index");

  private final String mimeType = this.localObjectFlavor.getMimeType();

  private final JTable table;

  public TableRowTransferHandler(final JTable table) {
    this.table = table;
  }

  @Override
  public boolean canImport(final TransferHandler.TransferSupport info) {
    if (info.getComponent() == this.table) {
      if (info.isDrop()) {
        if (info.isDataFlavorSupported(this.localObjectFlavor)) {
          this.table.setCursor(DragSource.DefaultMoveDrop);
          return true;
        }
      }
    }
    this.table.setCursor(DragSource.DefaultMoveNoDrop);
    return false;
  }

  @Override
  protected Transferable createTransferable(final JComponent c) {
    assert c == this.table;
    final int selectedRow = this.table.getSelectedRow();
    if (c == this.table) {

      return new DataHandler(selectedRow, this.mimeType);
    } else {
      return null;
    }
  }

  @Override
  protected void exportDone(final JComponent c, final Transferable t, final int action) {
    if (action == TransferHandler.MOVE) {
      this.table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
  }

  @Override
  public int getSourceActions(final JComponent c) {
    return TransferHandler.COPY_OR_MOVE;
  }

  @Override
  public boolean importData(final TransferHandler.TransferSupport info) {
    final JTable target = (JTable)info.getComponent();
    final JTable.DropLocation dropLocation = (JTable.DropLocation)info.getDropLocation();
    int index = dropLocation.getRow();
    final TableModel model = this.table.getModel();
    final int max = model.getRowCount();
    if (index < 0 || index > max) {
      index = max;
    }
    target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    try {
      final Transferable transferable = info.getTransferable();
      final Integer rowFrom = (Integer)transferable.getTransferData(this.localObjectFlavor);
      if (rowFrom != -1 && rowFrom != index) {
        ((Reorderable)model).reorder(rowFrom, index);
        if (index > rowFrom) {
          index--;
        }
        target.getSelectionModel().addSelectionInterval(index, index);
        return true;
      }
    } catch (final Exception e) {
      Logs.error(this, "Unexpected error", e);
    }
    return false;
  }

}
