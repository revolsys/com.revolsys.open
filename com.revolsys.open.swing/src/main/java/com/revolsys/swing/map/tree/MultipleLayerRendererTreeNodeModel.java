package com.revolsys.swing.map.tree;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.TreePath;

import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.renderer.AbstractMultipleRenderer;
import com.revolsys.swing.map.layer.record.renderer.AbstractRecordLayerRenderer;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class MultipleLayerRendererTreeNodeModel
extends
AbstractObjectTreeNodeModel<AbstractMultipleRenderer, AbstractRecordLayerRenderer>
implements MouseListener {

  private final Set<Class<?>> SUPPORTED_CHILD_CLASSES = Collections.<Class<?>> singleton(AbstractRecordLayerRenderer.class);

  public MultipleLayerRendererTreeNodeModel() {
    setSupportedClasses(AbstractMultipleRenderer.class);

    setSupportedChildClasses(this.SUPPORTED_CHILD_CLASSES);
    setObjectTreeNodeModels(this, new BaseLayerRendererTreeNodeModel(), this);
    setMouseListener(this);
  }

  @Override
  public int addChild(final AbstractMultipleRenderer node,
    final AbstractRecordLayerRenderer renderer) {
    return node.addRenderer(renderer);
  }

  @Override
  public int addChild(final AbstractMultipleRenderer node, final int index,
    final AbstractRecordLayerRenderer renderer) {
    return node.addRenderer(index, renderer);
  }

  @Override
  protected List<AbstractRecordLayerRenderer> getChildren(
    final AbstractMultipleRenderer node) {
    return node.getRenderers();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getParent(final AbstractMultipleRenderer node) {
    if (node == null) {
      return null;
    } else {
      final LayerRenderer<?> parent = node.getParent();
      if (parent == null) {
        return (T)node.getLayer();
      } else {
        return (T)parent;
      }
    }
  }

  @Override
  public Component getRenderer(final AbstractMultipleRenderer node,
    final JTree tree, final boolean selected, final boolean expanded,
    final boolean leaf, final int row, final boolean hasFocus) {
    final Component renderer = super.getRenderer(node, tree, selected,
      expanded, leaf, row, hasFocus);
    if (renderer instanceof JLabel) {
      final JLabel label = (JLabel)renderer;
      final Icon icon = node.getIcon();
      label.setIcon(icon);
    }
    renderer.setEnabled(node.isVisible());
    return renderer;
  }

  @Override
  protected boolean isDndDropSupported(final TransferSupport support,
    final TreePath dropPath, final AbstractMultipleRenderer node,
    final TreePath childPath, final Object child) {
    if (super.isDndDropSupported(support, dropPath, node, childPath, child)) {

      if (child instanceof AbstractRecordLayerRenderer) {
        final AbstractRecordLayerRenderer childRenderer = (AbstractRecordLayerRenderer)child;
        final Layer nodeLayer = node.getLayer();
        final Layer childLayer = childRenderer.getLayer();
        if (childLayer != nodeLayer) {
          if (isCopySupported(childRenderer)) {
            support.setDropAction(DnDConstants.ACTION_COPY);
          } else {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    final Object source = e.getSource();
    if (source instanceof JTree) {
      final JTree tree = (JTree)source;
      final int clickCount = e.getClickCount();
      if (clickCount == 2 && SwingUtilities.isLeftMouseButton(e)) {
        final int x = e.getX();
        final int y = e.getY();
        final TreePath path = tree.getPathForLocation(x, y);
        if (path != null) {
          final Object node = path.getLastPathComponent();
          if (node instanceof LayerRenderer) {
            final LayerRenderer<?> renderer = (LayerRenderer<?>)node;
            final TreeUI ui = tree.getUI();
            final Rectangle bounds = ui.getPathBounds(tree, path);
            final int cX = x - bounds.x;
            final int index = cX / 21;
            int offset = 0;
            if (index == offset) {
              renderer.setVisible(!renderer.isVisible());
            }
            offset++;
            tree.repaint();
          }
        }
      }
    }
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mousePressed(final MouseEvent e) {
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
  }

  @Override
  public boolean removeChild(final AbstractMultipleRenderer node,
    final AbstractRecordLayerRenderer renderer) {
    node.removeRenderer(renderer);
    return true;
  }

}
