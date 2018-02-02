package com.revolsys.swing.map.component;

import java.beans.PropertyChangeListener;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.swing.map.ComponentViewport2D;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.util.Property;

public class SelectMapCoordinateSystem extends CoordinateSystemField {
  private static final long serialVersionUID = 1L;

  private ComponentViewport2D viewport;

  private PropertyChangeListener geometryFactoryListener;

  public SelectMapCoordinateSystem(final MapPanel map) {
    super("srid");

    this.viewport = map.getViewport();
    final GeometryFactory geometryFactory = this.viewport.getGeometryFactory();
    setGeometryFactory(geometryFactory);
    this.geometryFactoryListener = Property.addListenerNewValueSource(this.viewport,
      "geometryFactory", this::setGeometryFactory);
  }

  @Override
  protected void geometryFactoryChanged(final GeometryFactory geometryFactory) {
    this.viewport.setGeometryFactory(geometryFactory);
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    Property.removeListener(this.viewport, "geometryFactory", this.geometryFactoryListener);
    this.viewport = null;
    this.geometryFactoryListener = null;
  }
}
