package com.revolsys.swing.tree.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.TreePath;

public class TreePathListTransferable implements Transferable {

  public static final DataFlavor FLAVOR;

  public static final DataFlavor[] FLAVORS;

  static {
    try {
      final String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=\""
        + TreePathListTransferable.class.getName() + "\"";
      FLAVOR = new DataFlavor(mimeType);
    } catch (final ClassNotFoundException e) {
      throw new RuntimeException("Flavour not supported");
    }
    FLAVORS = new DataFlavor[] {
      FLAVOR
    };
  }

  private final Set<TreePath> copiedPaths = new HashSet<>();

  private final Set<TreePath> movedPaths = new HashSet<>();

  private final List<TreePath> paths;

  private final Map<TreePath, Boolean> sameParent = new HashMap<>();

  public TreePathListTransferable(final List<TreePath> paths) {
    this.paths = paths;
  }

  public TreePathListTransferable(final TreePath... paths) {
    this(Arrays.asList(paths));
  }

  public void addCopiedPath(final TreePath path) {
    this.copiedPaths.add(path);
  }

  public void addMovedPath(final TreePath path) {
    this.movedPaths.add(path);
  }

  public Set<TreePath> getCopiedPaths() {
    return this.copiedPaths;
  }

  public Set<TreePath> getMovedPaths() {
    return this.movedPaths;
  }

  public List<TreePath> getPaths() {
    return this.paths;
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

  public boolean isCopied(final TreePath path) {
    return this.copiedPaths.contains(path);
  }

  @Override
  public boolean isDataFlavorSupported(final DataFlavor flavor) {
    final boolean supported = FLAVOR.equals(flavor);
    return supported;
  }

  public boolean isMoved(final TreePath path) {
    return this.movedPaths.contains(path);
  }

  public boolean isSameParent(final TreePath path) {
    return this.sameParent.containsKey(path);
  }

  public void reset() {
    this.copiedPaths.clear();
    this.movedPaths.clear();
  }

  public void setSameParent(final TreePath path) {
    this.sameParent.put(path, Boolean.TRUE);
  }
}
