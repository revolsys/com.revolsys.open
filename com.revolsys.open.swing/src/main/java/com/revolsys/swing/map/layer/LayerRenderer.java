package com.revolsys.swing.map.layer;

import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.Icon;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.map.Viewport2D;

public interface LayerRenderer<T extends Layer> extends ObjectWithProperties,
  PropertyChangeListener, PropertyChangeSupportProxy, MapSerializer, Cloneable {

  LayerRenderer<T> clone();

  Icon getIcon();

  T getLayer();

  String getName();

  LayerRenderer<?> getParent();

  List<String> getPathNames();

  List<LayerRenderer<?>> getPathRenderers();

  <V extends LayerRenderer<?>> V getRenderer(final List<String> path);

  boolean isEditing();

  boolean isOpen();

  boolean isVisible();

  Form newStylePanel();

  void render(Viewport2D viewport);

  void setEditing(boolean editing);

  void setLayer(T layer);

  void setOpen(boolean open);

  void setParent(LayerRenderer<?> parent);

  void setVisible(boolean visible);
}
