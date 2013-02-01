package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;

public abstract class AbstractDataObjectLayerRenderer implements
  LayerRenderer<DataObjectLayer> {

  public static LayerRenderer<DataObjectLayer> getRenderer(
    final Map<String, Object> style) {
    final Map<String, Object> defaults = Collections.emptyMap();
    return getRenderer(defaults, style);
  }

  public static AbstractDataObjectLayerRenderer getRenderer(
    final Map<String, Object> defaults, final Map<String, Object> style) {
    final String type = (String)style.get("type");
    if ("geometryStyle".equals(type)) {
      return new GeometryStyleRenderer(defaults,style);
    } else if ("textStyle".equals(type)) {
      return new TextStyleRenderer(defaults,style);
    } else if ("multipleStyle".equals(type)) {
      return new MultipleRenderer(defaults,style);
    } else if ("scaleStyle".equals(type)) {
      return new ScaleMultipleRenderer(defaults,style);
    }
    LoggerFactory.getLogger(AbstractDataObjectLayerRenderer.class).error(
      "Unknown style type: " + style);
    return null;
  }

  private final Map<String, Object> defaults = new HashMap<String, Object>();

  public AbstractDataObjectLayerRenderer() {
  }

  @SuppressWarnings("unchecked")
  public AbstractDataObjectLayerRenderer(final Map<String, Object> defaults,
    final Map<String, Object> style) {
    if (defaults != null) {
      this.defaults.putAll(defaults);
    }
    final Map<String, Object> styleDefaults = (Map<String, Object>)style.get("defaults");
    if (styleDefaults != null) {
      this.defaults.putAll(styleDefaults);
    }
  }

  public Map<String, Object> getDefaults() {
    return defaults;
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final DataObjectLayer layer) {
    final double scale = viewport.getScale();
    if (layer.isVisible(scale)) {
      viewport.setUseModelCoordinates(true, graphics);
      final BoundingBox boundingBox = viewport.getBoundingBox();
      final List<DataObject> dataObjects = layer.getDataObjects(boundingBox);

      if (layer.isEditable()) {
        dataObjects.removeAll(layer.getEditingObjects());
      }
      dataObjects.removeAll(layer.getHiddenObjects());

      renderObjects(viewport, graphics, layer, dataObjects);
    }
  }

  protected void renderObject(final Viewport2D viewport,
    final Graphics2D graphics, final BoundingBox visibleArea,
    final DataObjectLayer layer, final DataObject dataObject) {
  }

  protected void renderObjects(final Viewport2D viewport,
    final Graphics2D graphics, final DataObjectLayer layer,
    final List<DataObject> dataObjects) {
    final BoundingBox visibleArea = viewport.getBoundingBox();
    for (final DataObject dataObject : dataObjects) {
      renderObject(viewport, graphics, visibleArea, layer, dataObject);
    }
  }
  
  public Map<String,Object> toMap() {
    return Collections.emptyMap();
  }
  
  @Override
  public String toString() {
    return toMap().toString();
  }
}
