package com.revolsys.swing.tree.node.layer;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.TreePath;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.tree.node.BaseTreeNode;

public class LayerTreeNode extends AbstractLayerTreeNode implements
MouseListener {

  private static final Icon EDIT_ICON = SilkIconLoader.getIcon("pencil");

  private static final Icon EDIT_DISABLED_ICON = SilkIconLoader.getIcon("pencil_gray");

  private static final Icon SELECT_ICON = SilkIconLoader.getIcon("map_select");

  private static final Icon SELECT_DISABLED_ICON = SilkIconLoader.getIcon("map_select_gray");

  private static final Icon VISIBLE_ICON = SilkIconLoader.getIcon("map");

  private static final Icon NOT_EXISTS_ICON = SilkIconLoader.getIcon("error");

  private static final Icon VISIBLE_DISABLED_ICON = SilkIconLoader.getIcon("map_gray");

  private static final Map<List<Icon>, Icon> ICON_CACHE = new HashMap<List<Icon>, Icon>();

  public LayerTreeNode(final Layer layer) {
    super(layer);
  }

  @Override
  public int addChild(final int index, final Object child) {
    if (child instanceof LayerRenderer<?>) {
      final LayerRenderer<?> childRenderer = (LayerRenderer<?>)child;
      final AbstractLayer layer = getLayer();
      return layer.addRenderer(childRenderer, index);
    } else {
      return -1;
    }
  }

  @Override
  public int addChild(final Object child) {
    if (child instanceof LayerRenderer<?>) {
      final LayerRenderer<?> childRenderer = (LayerRenderer<?>)child;
      final AbstractLayer layer = getLayer();
      return layer.addRenderer(childRenderer);
    } else {
      return -1;
    }
  }

  @Override
  protected List<BaseTreeNode> doLoadChildren() {
    final Layer layer = getLayer();
    final LayerRenderer<? extends Layer> renderer = layer.getRenderer();
    if (renderer == null) {
      return Collections.emptyList();
    } else {
      return Collections.<BaseTreeNode> singletonList(new LayerRendererTreeNode(
        renderer));
    }
  }

  @Override
  protected void doPropertyChange(final PropertyChangeEvent e) {
    super.doPropertyChange(e);
    if (e.getSource() == getLayer()) {
      if (e.getPropertyName().equals("renderer")) {
        refresh();
      }
    }
  }

  @Override
  public Icon getIcon() {
    final Layer layer = getLayer();
    final List<Icon> icons = new ArrayList<Icon>();
    if (!layer.isExists() && layer.isInitialized()) {

      return NOT_EXISTS_ICON;
    } else if (layer.getRenderer() != null) {
      if (layer.isVisible()) {
        icons.add(VISIBLE_ICON);
      } else {
        icons.add(VISIBLE_DISABLED_ICON);
      }
      if (layer.isSelectSupported()) {
        if (layer.isSelectable()) {
          icons.add(SELECT_ICON);
        } else {
          icons.add(SELECT_DISABLED_ICON);
        }
      }
    }
    if (!layer.isReadOnly()) {
      if (layer.isEditable()) {
        icons.add(EDIT_ICON);
      } else {
        icons.add(EDIT_DISABLED_ICON);
      }
    }
    if (icons.isEmpty()) {
      return null;
    } else if (icons.size() == 1) {
      return icons.get(0);
    } else {
      Icon icon = ICON_CACHE.get(icons);
      if (icon == null) {
        icon = SilkIconLoader.merge(icons, 5);
        ICON_CACHE.put(icons, icon);
      }
      return icon;
    }
  }

  @Override
  public boolean isExists() {
    final Layer layer = getLayer();
    return layer.isExists();
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
      }
      e.consume();
    }
  }

  @Override
  public boolean removeChild(final TreePath path) {
    // TODO Auto-generated method stub
    return super.removeChild(path);
  }

}
