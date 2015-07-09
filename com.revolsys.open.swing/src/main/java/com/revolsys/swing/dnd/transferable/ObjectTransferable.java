package com.revolsys.swing.dnd.transferable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ObjectTransferable<T> implements Transferable {

  public static final DataFlavor FLAVOR;

  static {
    try {
      FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
    } catch (final ClassNotFoundException e) {
      throw new RuntimeException("Flavour not supported");
    }

  }

  private final T object;

  private final Object owner;

  public ObjectTransferable(final Object owner, final T object) {
    this.owner = owner;
    this.object = object;
  }

  public T getObject() {
    return this.object;
  }

  public Object getOwner() {
    return this.owner;
  }

  @Override
  public Object getTransferData(final DataFlavor flavor)
    throws UnsupportedFlavorException, IOException {
    if (isDataFlavorSupported(flavor)) {
      return this;
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {
      FLAVOR
    };
  }

  @Override
  public boolean isDataFlavorSupported(final DataFlavor flavor) {
    return FLAVOR.equals(flavor);
  }

}
