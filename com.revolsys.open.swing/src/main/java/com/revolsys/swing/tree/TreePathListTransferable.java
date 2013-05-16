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

  public static final DataFlavor[] FLAVORS;
  static {
    try {
      String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=\""
        + TreePathListTransferable.class.getName() + "\"";
      FLAVOR = new DataFlavor(mimeType);
    } catch (final ClassNotFoundException e) {
      throw new RuntimeException("Flavour not supported");
    }
    FLAVORS = new DataFlavor[] {
      FLAVOR
    };
  }

  private final List<TreePath> paths;

  private final Map<TreePath, Boolean> sameParent = new HashMap<TreePath, Boolean>();

  public TreePathListTransferable(final List<TreePath> paths) {
    this.paths = paths;
  }

  public TreePathListTransferable(final TreePath... paths) {
    this(Arrays.asList(paths));
  }

  protected List<TreePath> getPaths() {
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
    return FLAVORS;
  }

  @Override
  public boolean isDataFlavorSupported(final DataFlavor flavor) {
    boolean supported = FLAVOR.equals(flavor);
    return supported;
  }

  protected boolean isSameParent(final TreePath path) {
    return sameParent.containsKey(path);
  }

  protected void setSameParent(final TreePath path) {
    sameParent.put(path, Boolean.TRUE);
  }

}
