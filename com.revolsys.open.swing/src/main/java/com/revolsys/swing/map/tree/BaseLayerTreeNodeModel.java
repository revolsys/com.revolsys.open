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
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.tree.renderer.LayerTreeCellRenderer;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class BaseLayerTreeNodeModel extends
  AbstractObjectTreeNodeModel<AbstractLayer, AbstractLayer> implements
  MouseListener {

  private final static Map<Class<?>, JPopupMenu> MENUS = new HashMap<Class<?>, JPopupMenu>();

  private final Set<Class<?>> SUPPORTED_CHILD_CLASSES = Collections.<Class<?>> singleton(AbstractLayer.class);

  public static BaseLayerTreeNodeModel create(final String name,
    final Class<? extends AbstractLayer> layerClass) {
    final BaseLayerTreeNodeModel model = new BaseLayerTreeNodeModel(name,
      layerClass);
    return model;
  }

  public static JPopupMenu getMenu(
    final Class<? extends AbstractLayer> layerClass) {
    synchronized (MENUS) {
      JPopupMenu menu = MENUS.get(layerClass);
      if (menu == null) {
        menu = new JPopupMenu();
        MENUS.put(layerClass, menu);
      }
      return menu;
    }
  }

  public static void addMenuItem(
    final Class<? extends AbstractLayer> layerClass, JMenuItem menuItem) {
    JPopupMenu menu = getMenu(layerClass);
    menu.add(menuItem);
  }

  static {
//  TODO  for (Class<? extends AbstractLayer> layerClass : Arrays.asList(
//      LocalFeatureLayer.class, ListDataObjectLayer.class)) {
//      addMenuItem(layerClass, new SetLayerScaleMenu(false));
//      addMenuItem(layerClass, new SetLayerScaleMenu(true));
//    }
  }

  private final String name;

  public BaseLayerTreeNodeModel(final String name,
    final Class<?>... supportedClasses) {
    this.name = name;
    if (supportedClasses.length == 0) {
      setSupportedClasses(AbstractLayer.class);
    } else {
      setSupportedClasses(supportedClasses);
    }

    setLeaf(true);
    setSupportedChildClasses(SUPPORTED_CHILD_CLASSES);
    setRenderer(new LayerTreeCellRenderer());
    setMouseListener(this);
  }

  @Override
  public JPopupMenu getMenu(final AbstractLayer layer) {
    if (layer == null) {
      return null;
    } else {
      synchronized (MENUS) {
        final Class<? extends AbstractLayer> layerClass = layer.getClass();
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
          if (node instanceof Layer) {
            final Layer layer = (Layer)node;
            final TreeUI ui = tree.getUI();
            final Rectangle bounds = ui.getPathBounds(tree, path);
            final int cX = x - bounds.x;
            final int index = cX / 21;
            int offset = 0;
            if (index == offset) {
              layer.setVisible(!layer.isVisible());
            }
            offset++;
            // if (layer.isQuerySupported()) {
            // if (index == offset) {
            // layer.setQueryable(!layer.isQueryable());
            // }
            // offset++;
            // }

            if (layer.isSelectSupported()) {
              if (index == offset) {
                layer.setSelectable(!layer.isSelectable());
              }
              offset++;
            }

            if (!layer.isReadOnly()) {
              if (index == offset) {
                layer.setEditable(!layer.isEditable());
              }
              offset++;
            }

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
  public String toString() {
    return name;
  }

}
