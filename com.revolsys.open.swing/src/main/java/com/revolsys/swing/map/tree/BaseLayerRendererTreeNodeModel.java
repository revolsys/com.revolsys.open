package com.revolsys.swing.map.tree;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.TreePath;

import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.tree.renderer.LayerRendererTreeCellRenderer;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class BaseLayerRendererTreeNodeModel extends
  AbstractObjectTreeNodeModel<AbstractLayerRenderer<? extends Layer>, Void>
  implements MouseListener {

  private final Set<Class<?>> SUPPORTED_CHILD_CLASSES = Collections.<Class<?>> singleton(AbstractLayerRenderer.class);

  public BaseLayerRendererTreeNodeModel() {
    setSupportedClasses(AbstractLayerRenderer.class);
    setSupportedChildClasses(this.SUPPORTED_CHILD_CLASSES);
    setRenderer(new LayerRendererTreeCellRenderer());
    setMouseListener(this);
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
