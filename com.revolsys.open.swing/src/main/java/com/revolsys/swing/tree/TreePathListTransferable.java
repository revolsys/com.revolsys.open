package com.revolsys.swing.tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.TreePath;

public class TreePathListTransferable implements Transferable {

  public static final DataFlavor FLAVOR;
  static {
    try {
      FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
    } catch (final ClassNotFoundException e) {
      throw new RuntimeException("Flavour not supported");
    }

  }

  private final List<TreePath> paths;

  private final Map<TreePath, Boolean> sameParent = new HashMap<TreePath, Boolean>();

  public TreePathListTransferable(final List<TreePath> paths) {
    this.paths = paths;
  }

  public TreePathListTransferable(final TreePath... paths) {
    this(Arrays.asList(paths));
  }

  public List<TreePath> getPaths() {
    return paths;
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

  public boolean isSameParent(final TreePath path) {
    return sameParent.containsKey(path);
  }

  public void setSameParent(final TreePath path) {
    sameParent.put(path, Boolean.TRUE);
  }

}
