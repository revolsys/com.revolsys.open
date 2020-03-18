package com.revolsys.swing.dnd.transferhandler;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.jeometry.common.logging.Logs;

import com.revolsys.swing.dnd.transferable.MapTransferable;
import com.revolsys.swing.dnd.transferable.RecordTransferable;
import com.revolsys.swing.map.form.LayerRecordForm;

public class RecordLayerFormTransferHandler extends TransferHandler {
  private static final long serialVersionUID = 1L;

  private final Reference<LayerRecordForm> form;

  public RecordLayerFormTransferHandler(final LayerRecordForm form) {
    this.form = new WeakReference<>(form);
  }

  @Override
  public boolean canImport(final JComponent comp, final DataFlavor[] transferFlavors) {
    for (final DataFlavor dataFlavor : transferFlavors) {
      if (MapTransferable.MAP_FLAVOR.equals(dataFlavor)) {
        return true;
      } else if (RecordTransferable.DATA_OBJECT_FLAVOR.equals(dataFlavor)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected Transferable createTransferable(final JComponent component) {
    final Map<String, Object> values = getForm().getValues();
    final Transferable transferable = new MapTransferable(values);
    return transferable;
  }

  public LayerRecordForm getForm() {
    return this.form.get();
  }

  @Override
  public int getSourceActions(final JComponent component) {
    return COPY;
  }

  @Override
  public boolean importData(final JComponent comp, final Transferable transferable) {
    for (final DataFlavor dataFlavor : Arrays.asList(RecordTransferable.DATA_OBJECT_FLAVOR,
      MapTransferable.MAP_FLAVOR)) {
      if (pasteValues(transferable, dataFlavor)) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public boolean pasteValues(final Transferable transferable, final DataFlavor dataFlavor) {
    if (transferable.isDataFlavorSupported(dataFlavor)) {
      try {
        final Map<String, Object> map = (Map<String, Object>)transferable
          .getTransferData(dataFlavor);
        getForm().pasteValues(map);
        return true;
      } catch (final Throwable e) {
        Logs.error(this, "Unable to paste data:", e);
        return false;
      }
    } else {
      return false;
    }
  }
}
