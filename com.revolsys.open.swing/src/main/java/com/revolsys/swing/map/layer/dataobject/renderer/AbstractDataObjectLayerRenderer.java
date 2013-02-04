package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;

public abstract class AbstractDataObjectLayerRenderer extends  AbstractLayerRenderer<DataObjectLayer> {

  public static LayerRenderer<DataObjectLayer> getRenderer(DataObjectLayer layer,
    final Map<String, Object> style) {
    return getRenderer(layer, null, style);
  }

  public static AbstractDataObjectLayerRenderer getRenderer(DataObjectLayer layer,
    final LayerRenderer<?> parent, final Map<String, Object> style) {
    final String type = (String)style.get("type");
    if ("geometryStyle".equals(type)) {
      return new GeometryStyleRenderer(layer,parent, style);
    } else if ("textStyle".equals(type)) {
      return new TextStyleRenderer(layer,parent, style);
    } else if ("multipleStyle".equals(type)) {
      return new MultipleRenderer(layer,parent, style);
    } else if ("scaleStyle".equals(type)) {
      return new ScaleMultipleRenderer(layer,parent, style);
    }
    LoggerFactory.getLogger(AbstractDataObjectLayerRenderer.class).error(
      "Unknown style type: " + style);
    return null;
  }

  public AbstractDataObjectLayerRenderer(final String type,final DataObjectLayer layer,final LayerRenderer<?> parent) {
    super(type,layer,parent);
  }

  public AbstractDataObjectLayerRenderer(final String type,final DataObjectLayer layer,final LayerRenderer<?> parent,
     final Map<String, Object> style) {
    super(type,layer, parent,style);
   
  }


  public AbstractDataObjectLayerRenderer(String type, DataObjectLayer layer) {
    super(type,layer);
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final DataObjectLayer layer) {
      viewport.setUseModelCoordinates(true, graphics);
      final BoundingBox boundingBox = viewport.getBoundingBox();
      final List<DataObject> dataObjects = layer.getDataObjects(boundingBox);

      if (layer.isEditable()) {
        dataObjects.removeAll(layer.getEditingObjects());
      }
      dataObjects.removeAll(layer.getHiddenObjects());

      renderObjects(viewport, graphics, layer, dataObjects);
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
