package com.revolsys.swing.map.tree;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.TreePath;

import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class BaseLayerRendererTreeNodeModel extends
AbstractObjectTreeNodeModel<AbstractLayerRenderer<? extends Layer>, Void>
implements MouseListener {

  public BaseLayerRendererTreeNodeModel() {
    setSupportedClasses(AbstractLayerRenderer.class);
    setMouseListener(this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getParent(final AbstractLayerRenderer<? extends Layer> node) {
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
  public Component getRenderer(
    final AbstractLayerRenderer<? extends Layer> node, final JTree tree,
    final boolean selected, final boolean expanded, final boolean leaf,
    final int row, final boolean hasFocus) {
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
  public boolean isLeaf(final AbstractLayerRenderer<? extends Layer> node) {
    return true;
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
}
