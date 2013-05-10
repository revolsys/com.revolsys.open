package com.revolsys.swing.map.form;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.slf4j.LoggerFactory;

import com.revolsys.swing.component.MapTransferable;

public class DataObjectFormTransferHandler extends TransferHandler {
  private final DataObjectForm form;

  private static final long serialVersionUID = 1L;

  public DataObjectFormTransferHandler(final DataObjectForm form) {
    this.form = form;
  }

  @Override
  public boolean canImport(final JComponent comp,
    final DataFlavor[] transferFlavors) {
    for (final DataFlavor dataFlavor : transferFlavors) {
      if (MapTransferable.MAP_FLAVOR.equals(dataFlavor)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected Transferable createTransferable(final JComponent component) {
    final Map<String, Object> values = form.getValues();
    final Transferable transferable = new MapTransferable(values);
    return transferable;
  }

  @Override
  public int getSourceActions(final JComponent component) {
    return COPY;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean importData(final JComponent comp,
    final Transferable transferable) {
    if (transferable.isDataFlavorSupported(MapTransferable.MAP_FLAVOR)) {
      try {
        final Map<String, Object> map = (Map<String, Object>)transferable.getTransferData(MapTransferable.MAP_FLAVOR);
        form.pasteValues(map);
        return true;
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error("Unable to paste data",
          transferable);
        return false;
      }
    } else {
      return false;
    }
  }
}
