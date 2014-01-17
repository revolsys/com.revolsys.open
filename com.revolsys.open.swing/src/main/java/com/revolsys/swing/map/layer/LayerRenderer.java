package com.revolsys.swing.map.layer;

import java.awt.Graphics2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

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

  @Override
  PropertyChangeSupport getPropertyChangeSupport();

  boolean isVisible();

  void render(Viewport2D viewport, Graphics2D graphics);

  void setEditing(boolean editing);

  void setLayer(T layer);

  void setParent(LayerRenderer<?> parent);

  void setVisible(boolean visible);
}
