package com.revolsys.swing.map.layer;

import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.Icon;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.map.Viewport2D;

public interface LayerRenderer<T extends Layer> extends PropertyChangeListener,
  PropertyChangeSupportProxy, MapSerializer, Cloneable {

  LayerRenderer<T> clone();

  ValueField createStylePanel();

  Icon getIcon();

  T getLayer();

  String getName();

  LayerRenderer<?> getParent();

  List<String> getPathNames();

  List<LayerRenderer<?>> getPathRenderers();

  <V extends LayerRenderer<?>> V getRenderer(final List<String> path);

  boolean isEditing();

  boolean isVisible();

  void render(Viewport2D viewport);

  void setEditing(boolean editing);

  void setLayer(T layer);

  void setParent(LayerRenderer<?> parent);

  void setVisible(boolean visible);
}
