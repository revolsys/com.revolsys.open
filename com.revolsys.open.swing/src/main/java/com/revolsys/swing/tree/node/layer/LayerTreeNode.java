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
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.TreePath;

import com.revolsys.collection.set.Sets;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.tree.BaseTreeNode;

public class LayerTreeNode extends AbstractLayerTreeNode implements MouseListener {
  private static final Icon EDIT_ICON = Icons.getIcon("pencil");

  private static final Map<List<Icon>, Icon> ICON_CACHE = new HashMap<>();

  private static final Icon NOT_EXISTS_ICON = Icons.getIcon("error");

  private static final Icon SELECT_ICON = Icons.getIcon("map_select");

  private static final Set<String> REFRESH_ICON_PROPERTY_NAMES = Sets.newHash("visible",
    "selectSupported", "selectable", "exists", "initialized", "icon", "readOnly", "editable");

  public LayerTreeNode(final Layer layer) {
    super(layer);
    refreshIcon();
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
  public Icon getDisabledIcon() {
    return getIcon();
  }

  @Override
  public boolean isExists() {
    final Layer layer = getLayer();
    return layer.isExists();
  }

  @Override
  protected List<BaseTreeNode> loadChildrenDo() {
    final Layer layer = getLayer();
    final LayerRenderer<? extends Layer> renderer = layer.getRenderer();
    if (renderer == null) {
      return Collections.emptyList();
    } else {
      final LayerRendererTreeNode rendererNode = new LayerRendererTreeNode(renderer);
      return Collections.<BaseTreeNode> singletonList(rendererNode);
    }
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
  protected void propertyChangeDo(final PropertyChangeEvent e) {
    super.propertyChangeDo(e);
    final Object source = e.getSource();
    if (source == getLayer()) {
      final String propertyName = e.getPropertyName();
      if (propertyName.equals("renderer")) {
        refreshIcon();
        refresh();
      } else if (REFRESH_ICON_PROPERTY_NAMES.contains(propertyName)) {
        refreshIcon();
      }
    }
  }

  public void refreshIcon() {
    Icon icon;
    final Layer layer = getLayer();
    final List<Icon> icons = new ArrayList<>();
    if (!layer.isExists() && layer.isInitialized()) {
      icon = NOT_EXISTS_ICON;
    } else {
      final Icon layerIcon = layer.getIcon();
      if (layer.getRenderer() == null) {
        Icons.addIcon(icons, layerIcon, true);
      } else {
        final boolean visible = layer.isVisible();
        Icons.addIcon(icons, layerIcon, visible);
        if (layer.isSelectSupported()) {

          final boolean selectable = layer.isSelectable();
          Icons.addIcon(icons, SELECT_ICON, selectable);
        }
      }
      if (!layer.isReadOnly()) {
        final boolean editable = layer.isEditable();
        Icons.addIcon(icons, EDIT_ICON, editable);
      }
      if (icons.isEmpty()) {
        icon = null;
      } else if (icons.size() == 1) {
        icon = icons.get(0);
      } else {
        icon = ICON_CACHE.get(icons);
        if (icon == null) {
          icon = Icons.merge(icons, 5);
          ICON_CACHE.put(icons, icon);
        }
      }
    }
    setIcon(icon);

  }

}
