package com.revolsys.swing.map.tree;

import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.TreePath;

import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.tree.renderer.LayerRendererTreeCellRenderer;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class BaseLayerRendererTreeNodeModel extends
  AbstractObjectTreeNodeModel<AbstractLayerRenderer<? extends Layer>, Void>
  implements MouseListener {

  private final static Map<Class<?>, JPopupMenu> MENUS = new HashMap<Class<?>, JPopupMenu>();

  static {
    // TODO for (Class<? extends AbstractLayer> layerClass : Arrays.asList(
    // LocalFeatureLayer.class, ListDataObjectLayer.class)) {
    // addMenuItem(layerClass, new SetLayerScaleMenu(false));
    // addMenuItem(layerClass, new SetLayerScaleMenu(true));
    // }
  }

  public static void addMenuItem(
    final Class<? extends AbstractLayerRenderer<? extends Layer>> layerClass,
    final JMenuItem menuItem) {
    final JPopupMenu menu = getMenu(layerClass);
    menu.add(menuItem);
  }

  public static JPopupMenu getMenu(
    final Class<? extends AbstractLayerRenderer<? extends Layer>> layerClass) {
    synchronized (MENUS) {
      JPopupMenu menu = MENUS.get(layerClass);
      if (menu == null) {
        menu = new JPopupMenu();
        MENUS.put(layerClass, menu);
      }
      return menu;
    }
  }

  private final Set<Class<?>> SUPPORTED_CHILD_CLASSES = Collections.<Class<?>> singleton(AbstractLayerRenderer.class);

  public BaseLayerRendererTreeNodeModel() {
    setSupportedClasses(AbstractLayerRenderer.class);
    setSupportedChildClasses(SUPPORTED_CHILD_CLASSES);
    setRenderer(new LayerRendererTreeCellRenderer());
    setMouseListener(this);
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  public JPopupMenu getMenu(
    final AbstractLayerRenderer<? extends Layer> renderer) {
    if (renderer == null) {
      return null;
    } else {
      synchronized (MENUS) {
        final Class layerClass = renderer.getClass();
        return getMenu(layerClass);
      }
    }
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    final Object source = e.getSource();
    if (source instanceof JTree) {
      final JTree tree = (JTree)source;
      if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2
        && e.getModifiers() == InputEvent.BUTTON1_MASK) {
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
