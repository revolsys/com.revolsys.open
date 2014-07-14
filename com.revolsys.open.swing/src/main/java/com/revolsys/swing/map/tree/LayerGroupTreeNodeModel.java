package com.revolsys.swing.map.tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.TransferHandler.TransferSupport;
import javax.swing.tree.TreePath;

import com.revolsys.swing.dnd.transferhandler.ObjectTreeTransferHandler;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.tree.renderer.LayerGroupTreeCellRenderer;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class LayerGroupTreeNodeModel extends
  AbstractObjectTreeNodeModel<LayerGroup, Layer> {

  public LayerGroupTreeNodeModel() {
    setSupportedClasses(LayerGroup.class);
    setSupportedChildClasses(AbstractLayer.class, LayerGroup.class, Layer.class);
    setObjectTreeNodeModels(this, new BaseLayerTreeNodeModel("Layer"));
    setRenderer(new LayerGroupTreeCellRenderer());
  }

  @Override
  public int addChild(final LayerGroup parent, final int index,
    final Layer layer) {
    parent.add(index, layer);
    return index;
  }

  @Override
  public int addChild(final LayerGroup parent, final Layer layer) {
    parent.add(layer);
    return getChildCount(parent);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean dndImportData(final TransferSupport support,
    final LayerGroup group, final int index) throws IOException,
    UnsupportedFlavorException {
    if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
      final Transferable transferable = support.getTransferable();
      final List<File> files = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
      group.openFiles(files);
      return true;
    } else {
      return super.dndImportData(support, group, index);
    }
  }

  @Override
  protected List<Layer> getChildren(final LayerGroup parent) {
    return parent.getLayers();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getParent(final LayerGroup node) {
    if (node == null) {
      return null;
    } else {
      return (T)node.getLayerGroup();
    }
  }

  @Override
  public boolean isDndCanImport(final TreePath path,
    final TransferSupport support) {
    if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
      support.setDropAction(DnDConstants.ACTION_COPY);
      support.setShowDropLocation(true);
      return true;
    } else {
      return super.isDndCanImport(path, support);
    }
  }

  @Override
  protected boolean isDndDropSupported(final TransferSupport support,
    final LayerGroup node, final Object value) {
    if (value instanceof AbstractLayer) {
      final AbstractLayer layer = (AbstractLayer)value;
      if (ObjectTreeTransferHandler.isDndCopyAction(support)) {
        if (layer.isClonable()) {
          support.setDropAction(DnDConstants.ACTION_COPY);
          return true;
        }
        support.setDropAction(DnDConstants.ACTION_MOVE);
      }
    }
    return super.isDndDropSupported(support, node, value);
  }

  @Override
  public boolean isLeaf(final LayerGroup node) {
    return false;
  }

  @Override
  public boolean removeChild(final LayerGroup parent, final Layer layer) {
    parent.remove(layer);
    return true;
  }
}
