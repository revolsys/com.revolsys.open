package com.revolsys.swing.map.layer.dataobject.symbolizer;

import java.awt.Graphics2D;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.symbolizer.GeometrySymbolizer;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractGeometrySymbolizerRenderer<T extends GeometrySymbolizer>
  implements Symbolizer2DRenderer<T> {

  private final boolean useModelUnits;

  public AbstractGeometrySymbolizerRenderer(final boolean useModelUnits) {
    this.useModelUnits = useModelUnits;
  }

  @Override
  public void render(
    final Viewport2D viewport,
    final Graphics2D graphics,
    final DataObject dataObject,
    final T style) {
    SymbolizerJexlContext.setDataObject(dataObject);
    final boolean savedUseModelUnits = viewport.isUseModelCoordinates();
    viewport.setUseModelCoordinates(useModelUnits, graphics);
    final CharSequence geometryPropertyName = style.getGeometryPropertyName();
    if (geometryPropertyName != null) {
      final Object object = dataObject.getValue(geometryPropertyName);
      if (object instanceof Geometry) {
        final Geometry geometry = (Geometry)object;
        render(viewport, graphics, style, dataObject, geometry);
      }
    } else {
      for (final int index : dataObject.getMetaData()
        .getGeometryAttributeIndexes()) {
        final Object object = dataObject.getValue(index);
        if (object instanceof Geometry) {
          final Geometry geometry = (Geometry)object;
          render(viewport, graphics, style, dataObject, geometry);
        }
      }
    }
    viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
    SymbolizerJexlContext.setDataObject(null);

  }

  protected abstract void render(
    Viewport2D viewport,
    Graphics2D graphics,
    T style,
    DataObject dataObject,
    Geometry geometry);

}
