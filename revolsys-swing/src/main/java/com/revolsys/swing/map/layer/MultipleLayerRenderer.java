package com.revolsys.swing.map.layer;

import java.util.List;

import org.jeometry.common.data.type.DataType;

import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.util.Property;

/**
 *
 *
 * @param <C> The type of child
 */
public interface MultipleLayerRenderer<L extends Layer, C extends LayerRenderer<L>> {

  default int addRenderer(final C renderer) {
    return addRenderer(-1, renderer);
  }

  int addRenderer(int index, final C renderer);

  default void addRendererEdit(final C renderer) {
    addRenderer(-1, renderer);
    final Object item = MenuFactory.getMenuSource();
    if (item instanceof BaseTreeNode) {
      final BaseTreeNode node = (BaseTreeNode)item;
      final BaseTree tree = node.getTree();
      if (tree.isPropertyEqual("treeType", Project.class.getName())) {
        final Layer layer = renderer.getLayer();
        layer.showRendererProperties(renderer);
      }
    }
  }

  boolean canAddChild(Object object);

  @SuppressWarnings("unchecked")
  default <V extends LayerRenderer<?>> V getRenderer(final String name) {
    if (Property.hasValue(name)) {
      for (final LayerRenderer<?> renderer : getRenderers()) {
        final String rendererName = renderer.getName();
        if (DataType.equal(name, rendererName)) {
          return (V)renderer;
        }
      }
    }
    return null;
  }

  List<C> getRenderers();

  default boolean hasRendererWithSameName(final LayerRenderer<?> renderer, final String name) {
    for (final C otherRenderer : getRenderers()) {
      if (renderer != otherRenderer) {
        final String layerName = otherRenderer.getName();
        if (name.equals(layerName)) {
          return true;
        }
      }
    }
    return false;
  }

  boolean isSameLayer(Layer layer);

  default boolean isSameLayer(final LayerRenderer<?> renderer) {
    final Layer layer = renderer.getLayer();
    return isSameLayer(layer);
  }

  int removeRenderer(final C renderer);

}
