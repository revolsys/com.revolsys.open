package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;

public abstract class AbstractDataObjectLayerRenderer implements
  LayerRenderer<DataObjectLayer> {

  public static AbstractDataObjectLayerRenderer getRenderer(
    final Map<String, Object> style) {
    final String type = (String)style.get("type");
    if ("basicStyle".equals(type)) {
      return new StyleRenderer(style);
    } else if ("multipleStyle".equals(type)) {
      return new MultipleStyleRenderer(style);
    }
    return null;
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
}
