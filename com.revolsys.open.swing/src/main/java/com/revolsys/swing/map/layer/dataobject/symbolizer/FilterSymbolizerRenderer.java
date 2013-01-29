package com.revolsys.swing.map.layer.dataobject.symbolizer;

import java.awt.Graphics2D;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.symbolizer.FilterSymbolizer;
import com.revolsys.swing.map.symbolizer.Symbolizer;

public class FilterSymbolizerRenderer implements
  Symbolizer2DRenderer<FilterSymbolizer> {

  public FilterSymbolizerRenderer() {
  }

  @Override
  public void render(
    final Viewport2D viewport,Graphics2D graphics,
    final DataObject dataObject,
    final FilterSymbolizer filterSymbolizer) {
  /*  final Filter<DataObject> filter = filterSymbolizer.getFilter();
    if (filter.accept(dataObject)) {
      final Symbolizer symbolizer = filterSymbolizer.getSymbolizer();
      final Symbolizer2DRenderer<Symbolizer> renderer = DataObjectLayerRenderer.getSymbolizerRenderer(symbolizer);
      renderer.render(viewport, graphics,dataObject, symbolizer);
    }
*/
  }

}
