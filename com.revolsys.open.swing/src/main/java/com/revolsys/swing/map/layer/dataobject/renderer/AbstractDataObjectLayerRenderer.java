package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.filter.AcceptAllFilter;
import com.revolsys.filter.Filter;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.filter.MultipleAttributeValuesFilter;
import com.revolsys.gis.data.model.filter.SpringExpresssionLanguageFilter;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;

public abstract class AbstractDataObjectLayerRenderer extends
  AbstractLayerRenderer<DataObjectLayer> {

  private static final AcceptAllFilter<DataObject> DEFAULT_FILTER = new AcceptAllFilter<DataObject>();

  private static final Map<String, Object> FILTER_VARIABLES = new HashMap<String, Object>();

  public static void addFilterVariable(final String name, final Object value) {
    FILTER_VARIABLES.put(name, value);
  }

  public static Filter<DataObject> getFilter(final Map<String, Object> style) {
    @SuppressWarnings("unchecked")
    Map<String, Object> filterDefinition = (Map<String, Object>)style.get("filter");
    if (filterDefinition != null) {
      filterDefinition = new LinkedHashMap<String, Object>(filterDefinition);
      final String type = (String)filterDefinition.remove("type");
      if ("valueFilter".equals(type)) {
        return new MultipleAttributeValuesFilter(filterDefinition);
      } else if ("queryFilter".equals(type)) {
        final String query = (String)filterDefinition.remove("query");
        return new SpringExpresssionLanguageFilter(query, FILTER_VARIABLES);
      } else {
        LoggerFactory.getLogger(AbstractDataObjectLayerRenderer.class).error(
          "Unknown filter type " + type);
      }
    }
    return DEFAULT_FILTER;
  }

  public static AbstractDataObjectLayerRenderer getRenderer(
    final DataObjectLayer layer, final LayerRenderer<?> parent,
    final Map<String, Object> style) {
    final String type = (String)style.get("type");
    if ("geometryStyle".equals(type)) {
      return new GeometryStyleRenderer(layer, parent, style);
    } else if ("textStyle".equals(type)) {
      return new TextStyleRenderer(layer, parent, style);
    } else if ("markerStyle".equals(type)) {
      return new MarkerStyleRenderer(layer, parent, style);
    } else if ("multipleStyle".equals(type)) {
      return new MultipleRenderer(layer, parent, style);
    } else if ("scaleStyle".equals(type)) {
      return new ScaleMultipleRenderer(layer, parent, style);
    } else if ("filterStyle".equals(type)) {
      return new FilterMultipleRenderer(layer, parent, style);
    }
    LoggerFactory.getLogger(AbstractDataObjectLayerRenderer.class).error(
      "Unknown style type: " + style);
    return null;
  }

  public static LayerRenderer<DataObjectLayer> getRenderer(
    final DataObjectLayer layer, final Map<String, Object> style) {
    return getRenderer(layer, null, style);
  }

  private Filter<DataObject> filter = DEFAULT_FILTER;

  public AbstractDataObjectLayerRenderer(final String type,
    final DataObjectLayer layer) {
    super(type, layer);
  }

  public AbstractDataObjectLayerRenderer(final String type,
    final DataObjectLayer layer, final LayerRenderer<?> parent) {
    super(type, layer, parent);
  }

  public AbstractDataObjectLayerRenderer(final String type,
    final DataObjectLayer layer, final LayerRenderer<?> parent,
    final Map<String, Object> style) {
    super(type, layer, parent, style);
    filter = getFilter(style);
  }

  protected boolean isVisible(final DataObject object) {
    return filter.accept(object);
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
    final DataObjectLayer layer, final DataObject object) {
  }

  protected void renderObjects(final Viewport2D viewport,
    final Graphics2D graphics, final DataObjectLayer layer,
    final List<DataObject> objects) {
    final BoundingBox visibleArea = viewport.getBoundingBox();
    for (final DataObject object : objects) {
      if (isVisible(object)) {
        renderObject(viewport, graphics, visibleArea, layer, object);
      }
    }
  }

}
