package com.revolsys.swing.map.layer.dataobject;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.GeometryOperation;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.symbolizer.FilterSymbolizerRenderer;
import com.revolsys.swing.map.layer.dataobject.symbolizer.LineSymbolizerRenderer;
import com.revolsys.swing.map.layer.dataobject.symbolizer.PointSymbolizerRenderer;
import com.revolsys.swing.map.layer.dataobject.symbolizer.PolygonSymbolizerRenderer;
import com.revolsys.swing.map.layer.dataobject.symbolizer.Symbolizer2DRenderer;
import com.revolsys.swing.map.layer.dataobject.symbolizer.TextPointSymbolizerRenderer;
import com.revolsys.swing.map.symbolizer.FilterSymbolizer;
import com.revolsys.swing.map.symbolizer.LineSymbolizer;
import com.revolsys.swing.map.symbolizer.PointSymbolizer;
import com.revolsys.swing.map.symbolizer.PolygonSymbolizer;
import com.revolsys.swing.map.symbolizer.Symbolizer;
import com.revolsys.swing.map.symbolizer.TextPointSymbolizer;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectLayerRenderer implements
  LayerRenderer<AbstractDataObjectLayer> {
  private static final Map<Class<?>, Symbolizer2DRenderer<?>> SYMBOLIZER_RENDERER = new HashMap<Class<?>, Symbolizer2DRenderer<?>>();

  public static Symbolizer2DRenderer<Symbolizer> getSymbolizerRenderer(
    final Symbolizer symbolizer) {
    return (Symbolizer2DRenderer<Symbolizer>)SYMBOLIZER_RENDERER.get(symbolizer.getClass());
  }

  {
    SYMBOLIZER_RENDERER.put(PointSymbolizer.class,
      new PointSymbolizerRenderer());
    SYMBOLIZER_RENDERER.put(LineSymbolizer.class, new LineSymbolizerRenderer());
    SYMBOLIZER_RENDERER.put(PolygonSymbolizer.class,
      new PolygonSymbolizerRenderer());
    SYMBOLIZER_RENDERER.put(TextPointSymbolizer.class,
      new TextPointSymbolizerRenderer());
    SYMBOLIZER_RENDERER.put(FilterSymbolizer.class,
      new FilterSymbolizerRenderer());
  }

  public DataObjectLayerRenderer() {

  }

  private List<DataObject> getProjectedObjects(
    final GeometryFactory layerGeometryFactory,
    final GeometryFactory mapGeometryFactory, final List<DataObject> dataObjects) {
    final GeometryOperation operation = ProjectionFactory.getGeometryOperation(
      layerGeometryFactory, mapGeometryFactory);
    if (operation == null) {
      return dataObjects;
    } else {
      final List<DataObject> projectedObjects = new ArrayList<DataObject>();
      for (final DataObject dataObject : dataObjects) {
        final DataObject projectedObject = dataObject.clone();
        for (final int geometryIndex : dataObject.getMetaData()
          .getGeometryAttributeIndexes()) {
          try {
            final Geometry geometry = dataObject.getValue(geometryIndex);
            final Geometry projectedGeometry = operation.perform(geometry);
            projectedObject.setValue(geometryIndex, projectedGeometry);
          } catch (final ClassCastException c) {
            LoggerFactory.getLogger(getClass()).error("Not a geometry", c);
          }
        }
        projectedObjects.add(projectedObject);
      }
      return projectedObjects;
    }
  }

  @Override
  public void render(final Viewport2D viewport, Graphics2D graphics,
    final AbstractDataObjectLayer layer) {
    final double scale = viewport.getScale();
    if (layer.isVisible(scale)) {
      viewport.setUseModelCoordinates(true, graphics);
      final BoundingBox boundingBox = viewport.getBoundingBox();
      List<DataObject> dataObjects = layer.getDataObjects(boundingBox);

      if (layer.isEditable()) {
        dataObjects.removeAll(layer.getEditingObjects());
      }
      dataObjects.removeAll(layer.getHiddenObjects());

      final GeometryFactory mapGeometryFactory = boundingBox.getGeometryFactory();
      final GeometryFactory layerGeometryFactory = layer.getGeometryFactory();
      if (layerGeometryFactory != null
        && !mapGeometryFactory.equals(layerGeometryFactory)) {
        dataObjects = getProjectedObjects(layerGeometryFactory,
          mapGeometryFactory, dataObjects);
      }
      renderObjects(viewport, graphics, layer, dataObjects);
    }
  }

  private void renderObject(final Viewport2D viewport,
    final Graphics2D graphics, final BoundingBox visibleArea,
    final AbstractDataObjectLayer layer, final DataObject dataObject) {
    final List<Symbolizer> symbolizers = layer.getSymbolizers(dataObject);
    for (int i = symbolizers.size() - 1; i >= 0; i--) {
      final Symbolizer symbolizer = symbolizers.get(i);
      final Symbolizer2DRenderer<Symbolizer> symbolizerRenderer = getSymbolizerRenderer(symbolizer);
      symbolizerRenderer.render(viewport, graphics, dataObject, symbolizer);
    }

  }

  private void renderObjects(final Viewport2D viewport,
    final Graphics2D graphics, final AbstractDataObjectLayer layer,
    final List<DataObject> dataObjects) {
    final BoundingBox visibleArea = viewport.getBoundingBox();
    for (final DataObject dataObject : dataObjects) {
      renderObject(viewport, graphics, visibleArea, layer, dataObject);
    }
  }
}
