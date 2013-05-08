package com.revolsys.swing.map.tree;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.TreePath;

import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.tree.renderer.LayerTreeCellRenderer;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class BaseLayerTreeNodeModel extends
  AbstractObjectTreeNodeModel<AbstractLayer, LayerRenderer<Layer>> implements
  MouseListener {

  public static BaseLayerTreeNodeModel create(final String name,
    final Class<? extends AbstractLayer> layerClass) {
    final BaseLayerTreeNodeModel model = new BaseLayerTreeNodeModel(name,
      layerClass);
    return model;
  }

  @Override
  protected List<LayerRenderer<Layer>> getChildren(AbstractLayer node) {
    LayerRenderer<Layer> renderer = node.getRenderer();
    if (renderer == null) {
      return Collections.emptyList();
    } else {
      return Collections.singletonList(renderer);
    }
  }

  private final Set<Class<?>> SUPPORTED_CHILD_CLASSES = Collections.<Class<?>> singleton(LayerRenderer.class);

  private final String name;

  public BaseLayerTreeNodeModel(final String name,
    final Class<?>... supportedClasses) {
    this.name = name;
    if (supportedClasses.length == 0) {
      setSupportedClasses(AbstractLayer.class);
    } else {
      setSupportedClasses(supportedClasses);
    }

    setSupportedChildClasses(SUPPORTED_CHILD_CLASSES);
    setObjectTreeNodeModels(new MultipleLayerRendererTreeNodeModel(),
      new BaseLayerRendererTreeNodeModel());
    setRenderer(new LayerTreeCellRenderer());
    setMouseListener(this);
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    final Object source = e.getSource();
    if (source instanceof ObjectTree) {
      final ObjectTree tree = (ObjectTree)source;
      int clickCount = e.getClickCount();
      if (clickCount == 2 && SwingUtilities.isLeftMouseButton(e)) {
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
