package com.revolsys.swing.map.tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.util.List;

import javax.swing.TransferHandler.TransferSupport;
import javax.swing.tree.TreePath;

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

  @Override
  public boolean canImport(final TreePath path, final TransferSupport support) {
    if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
      support.setDropAction(DnDConstants.ACTION_COPY);
      support.setShowDropLocation(true);
      return true;
    } else {
      return super.canImport(path, support);
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
  public boolean isLeaf(final LayerGroup node) {
    return false;
  }

  @Override
  public boolean removeChild(final LayerGroup parent, final Layer layer) {
    parent.remove(layer);
    return true;
  }
}
