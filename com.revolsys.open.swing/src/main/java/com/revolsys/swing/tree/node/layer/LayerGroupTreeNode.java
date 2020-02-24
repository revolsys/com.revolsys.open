package com.revolsys.swing.tree.node.layer;

import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.TreePath;

import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.node.file.PathTreeNode;

public class LayerGroupTreeNode extends AbstractLayerTreeNode implements MouseListener {

  public LayerGroupTreeNode(final LayerGroup layerGroup) {
    super(layerGroup);
  }

  @Override
  public int addChild(final int index, final Object object) {
    if (object instanceof Layer) {
      final Layer layer = (Layer)object;
      final LayerGroup group = getGroup();
      group.addLayer(index, layer);
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
      group.addLayer(layer);
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
      final List<File> files = (List<File>)transferable
        .getTransferData(DataFlavor.javaFileListFlavor);
      group.openFiles(index, files);
      return true;
    } else {
      return super.dndImportData(support, index);
    }
  }

  @Override
  protected List<Class<?>> getChildClasses() {
    return Arrays.<Class<?>> asList(AbstractLayer.class, LayerGroup.class, Layer.class);
  }

  public LayerGroup getGroup() {
    return (LayerGroup)super.getLayer();
  }

  @Override
  public Icon getIcon() {
    final Layer layer = getLayer();
    if (layer.isVisible()) {
      return PathTreeNode.getIconFolder();
    } else {
      return Icons.getDisabledIcon(PathTreeNode.getIconFolder());
    }
  }

  @Override
  public boolean isDndCanImport(final TreePath path, final TransferSupport support) {
    if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
      support.setDropAction(DnDConstants.ACTION_COPY);
      support.setShowDropLocation(true);
      return true;
    } else {
      return super.isDndCanImport(path, support);
    }
  }

  @Override
  protected List<BaseTreeNode> loadChildrenDo() {
    final List<BaseTreeNode> children = new ArrayList<>();
    final LayerGroup group = getGroup();
    group.forEach((child) -> {
      final BaseTreeNode childNode = newTreeNode(child);
      children.add(childNode);
    });
    return children;
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    final Object source = e.getSource();
    final JTree tree = getTree();
    if (source == tree) {
      final int clickCount = e.getClickCount();
      if (clickCount == 2 && SwingUtilities.isLeftMouseButton(e)) {
        final int x = e.getX();
        final int y = e.getY();
        final TreePath path = tree.getPathForLocation(x, y);
        final Layer layer = getLayer();
        final TreeUI ui = tree.getUI();
        final Rectangle bounds = ui.getPathBounds(tree, path);
        final int cX = x - bounds.x;
        final int index = cX / 21;
        int offset = 0;
        if (index == offset) {
          layer.setVisible(!layer.isVisible());
        }
        offset++;
      }
      e.consume();
    }
  }

  protected BaseTreeNode newTreeNode(final Layer layer) {
    BaseTreeNode childNode;
    if (layer instanceof LayerGroup) {
      final LayerGroup childGroup = (LayerGroup)layer;
      childNode = new LayerGroupTreeNode(childGroup);
    } else {
      childNode = new LayerTreeNode(layer);
    }
    return childNode;
  }

  @Override
  public void propertyChangeDo(final PropertyChangeEvent e) {
    super.propertyChangeDo(e);
    final Object source = e.getSource();
    final LayerGroup layerGroup = getGroup();
    if (source == layerGroup) {
      final String propertyName = e.getPropertyName();

      if ("reset".equals(propertyName)) {
        refresh();
      } else if ("layers".equals(propertyName)) {
        if (e instanceof IndexedPropertyChangeEvent) {
          final IndexedPropertyChangeEvent indexedEvent = (IndexedPropertyChangeEvent)e;
          final int index = indexedEvent.getIndex();
          final Layer oldLayer = (Layer)e.getOldValue();
          final Layer newLayer = (Layer)e.getNewValue();
          final List<BaseTreeNode> children = new ArrayList<>(getChildren());
          if (newLayer == null) {
            if (oldLayer != null) {
              children.remove(index);
              setChildren(children);
            }
          } else if (oldLayer == null) {
            final BaseTreeNode newTreeNode = newTreeNode(newLayer);
            children.add(index, newTreeNode);
            setChildren(children);
          } else {
            this.refresh();
          }
        } else {
          this.refresh();
        }
      } else if ("visible".equals(propertyName)) {
        if (isChildrenInitialized()) {
          for (final BaseTreeNode childNode : getChildren()) {
            if (childNode instanceof LayerTreeNode) {
              final LayerTreeNode childLayerNode = (LayerTreeNode)childNode;
              childLayerNode.refreshIcon();
            }
          }
        }
      }
    }
  }

  @Override
  public boolean removeChild(final Object child) {
    if (child instanceof Layer) {
      final Layer layer = (Layer)child;
      final LayerGroup group = getGroup();
      return group.removeLayer(layer);
    } else {
      return false;
    }
  }
}
