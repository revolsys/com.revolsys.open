package com.revolsys.swing.dnd.transferable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class StringTransferable implements Transferable {

  private final String value;

  private final DataFlavor dataFlavor;

  public StringTransferable(final DataFlavor dataFlavor, final String value) {
    this.dataFlavor = dataFlavor;
    this.value = value;
  }

  @Override
  public Object getTransferData(final DataFlavor flavor)
      throws UnsupportedFlavorException, IOException {
    if (isDataFlavorSupported(flavor)) {
      return this.value;
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {
      this.dataFlavor
    };
  }

  @Override
  public boolean isDataFlavorSupported(final DataFlavor dataFlavor) {
    return this.dataFlavor.equals(dataFlavor);
  }

}
