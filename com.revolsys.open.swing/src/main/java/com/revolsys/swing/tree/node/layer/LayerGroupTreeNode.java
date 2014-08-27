package com.revolsys.swing.tree.node.layer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.tree.TreePath;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.tree.node.BaseTreeNode;

public class LayerGroupTreeNode extends AbstractLayerTreeNode {
  public static final Icon ICON = SilkIconLoader.getIcon("folder");

  public LayerGroupTreeNode(final LayerGroup layerGroup) {
    super(layerGroup);
    setName(layerGroup.getName());
  }

  @Override
  public int addChild(final int index, final Object object) {
    if (object instanceof Layer) {
      final Layer layer = (Layer)object;
      final LayerGroup group = getGroup();
      group.add(index, layer);
      return index;
    } else {
      return -1;
    }
  }

  @Override
  public int addChild(final Object object) {
    if (object instanceof Layer) {
      final Layer layer = (Layer)object;
      final LayerGroup group = getGroup();
      group.add(layer);
      return getChildCount();
    } else {
      return -1;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean dndImportData(final TransferSupport support, final int index)
    throws IOException, UnsupportedFlavorException {
    final LayerGroup group = getGroup();
    if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
      final Transferable transferable = support.getTransferable();
      final List<File> files = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
      group.openFiles(index, files);
      return true;
    } else {
      return super.dndImportData(support, index);
    }
  }

  @Override
  protected List<BaseTreeNode> doLoadChildren() {
    final List<BaseTreeNode> nodes = new ArrayList<>();
    final LayerGroup group = getGroup();
    for (final Layer child : group) {
      if (child instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)child;
        nodes.add(new LayerGroupTreeNode(childGroup));
      } else {
        nodes.add(new LayerTreeNode(child));
      }
    }
    return nodes;
  }

  @Override
  public void doPropertyChange(final PropertyChangeEvent e) {
    super.doPropertyChange(e);
    final Object source = e.getSource();
    if (source == getLayer()) {
      final String propertyName = e.getPropertyName();
      if ("layers".equals(propertyName)) {
        refresh();
      }
    }
  }

  @Override
  protected List<Class<?>> getChildClasses() {
    return Arrays.<Class<?>> asList(AbstractLayer.class, LayerGroup.class,
      Layer.class);
  }

  public LayerGroup getGroup() {
    return (LayerGroup)super.getLayer();
  }

  @Override
  public Icon getIcon() {
    return ICON;
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
  public boolean removeChild(final Object child) {
    if (child instanceof Layer) {
      final Layer layer = (Layer)child;
      final LayerGroup group = getGroup();
      return group.remove(layer);
    } else {
      return false;
    }
  }
}
