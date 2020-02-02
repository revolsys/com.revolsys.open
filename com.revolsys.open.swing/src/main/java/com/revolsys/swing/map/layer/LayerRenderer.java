package com.revolsys.swing.map.layer;

import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.Icon;

import org.jeometry.common.data.type.DataType;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.util.BaseCloneable;

public interface LayerRenderer<T extends Layer> extends ObjectWithProperties,
  PropertyChangeListener, PropertyChangeSupportProxy, MapSerializer, BaseCloneable {

  @Override
  LayerRenderer<T> clone();

  Icon getIcon();

  T getLayer();

  String getName();

  LayerRenderer<?> getParent();

  List<String> getPathNames();

  List<LayerRenderer<?>> getPathRenderers();

  @SuppressWarnings("unchecked")
  default <V extends LayerRenderer<?>> V getRenderer(final List<String> path) {
    LayerRenderer<?> renderer = this;
    final int pathSize = path.size();
    for (int i = 0; i < pathSize; i++) {
      final String name = path.get(i);
      final String rendererName = renderer.getName();
      if (DataType.equal(name, rendererName)) {
        if (i < pathSize - 1) {
          final String childName = path.get(i + 1);
          if (renderer instanceof MultipleLayerRenderer) {
            final MultipleLayerRenderer<?, ?> multipleRenderer = (MultipleLayerRenderer<?, ?>)renderer;
            renderer = multipleRenderer.getRenderer(childName);
          }
        }
      } else {
        return null;
      }
    }
    return (V)renderer;
  }

  boolean isEditing();

  boolean isOpen();

  boolean isVisible();

  boolean isVisible(final ViewRenderer view);

  Form newStylePanel();

  default void refresh() {
  }

  void render(ViewRenderer view);

  void setEditing(boolean editing);

  void setLayer(T layer);

  void setName(String name);

  void setOpen(boolean open);

  void setParent(LayerRenderer<?> parent);

  void setVisible(boolean visible);
}
