package com.revolsys.swing.map.layer.dataobject.symbolizer;

import java.awt.Graphics2D;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.symbolizer.Graphic;
import com.revolsys.swing.map.symbolizer.GraphicSymbol;
import com.revolsys.swing.map.symbolizer.ImageGraphic;
import com.revolsys.swing.map.symbolizer.PointSymbolizer;
import com.revolsys.swing.map.symbolizer.WellKnownMarkGraphicSymbol;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class PointSymbolizerRenderer extends
  AbstractGeometrySymbolizerRenderer<PointSymbolizer> {

  public PointSymbolizerRenderer() {
    super(false);
  }

  @Override
  protected void render(
    final Viewport2D viewport,
    final Graphics2D graphics,
    final PointSymbolizer style,
    final DataObject dataObject,
    final Geometry geometry) {
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      final Geometry part = geometry.getGeometryN(i);
      if (part instanceof Point) {
        final Point point = (Point)part;
        render(viewport, graphics, style, point);
      }
    }
  }

  protected void render(
    final Viewport2D viewport,
    final Graphics2D graphics,
    final PointSymbolizer style,
    final Point point) {
    final Coordinate coordinate = point.getCoordinate();
    final double[] location = viewport.toViewCoordinates(coordinate.x,
      coordinate.y);
    final Graphic graphic = style.getGraphic();
    final GraphicSymbol symbol = graphic.getSymbols().get(0);
    if (symbol instanceof WellKnownMarkGraphicSymbol) {
      final WellKnownMarkGraphicSymbol markSymbol = (WellKnownMarkGraphicSymbol)symbol;
      new WellKnownMarkGraphicSymbolRenderer().render(viewport, graphics,
        graphic, markSymbol, location[0], location[1]);
    } else if (symbol instanceof ImageGraphic) {
      final ImageGraphic imageGraphic = (ImageGraphic)symbol;
      new ImageGraphicSymbolRenderer().render(viewport, graphics, graphic,
        imageGraphic, location[0], location[1]);
    }
  }
}
