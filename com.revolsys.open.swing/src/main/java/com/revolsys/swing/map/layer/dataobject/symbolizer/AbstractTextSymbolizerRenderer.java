package com.revolsys.swing.map.layer.dataobject.symbolizer;

import java.awt.Font;
import java.awt.Graphics2D;

import javax.measure.unit.SI;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.symbolizer.AbstractTextSymbolizer;
import com.revolsys.swing.map.symbolizer.AbstractTextSymbolizer.FontStyle;
import com.revolsys.swing.map.symbolizer.AbstractTextSymbolizer.FontWeight;

public abstract class AbstractTextSymbolizerRenderer<T extends AbstractTextSymbolizer>
  extends AbstractGeometrySymbolizerRenderer<T> {

  public AbstractTextSymbolizerRenderer() {
    super(false);

  }

  protected Font getFont(final T textStyle) {
    // TODO cache fonts
    final CharSequence name = textStyle.getFontFamily();
    int style = 0;
    if (textStyle.getFontWeight() == FontWeight.BOLD) {
      style += Font.BOLD;
    }
    if (textStyle.getFontStyle() == FontStyle.ITALIC) {
      style += Font.ITALIC;
    }
    final String fontName = name.toString();
    final int fontSize = (int)textStyle.getFontSize().doubleValue(SI.METRE);
    return new Font(fontName, style, fontSize);
  };

  @Override
  public void render(
    final Viewport2D viewport,Graphics2D graphics,
    final DataObject dataObject,
    final T style) {
    SymbolizerJexlContext.setDataObject(dataObject);
    final Font savedFont = graphics.getFont();
    graphics.setFont(getFont(style));
    super.render(viewport, graphics,dataObject, style);
    graphics.setFont(savedFont);
    SymbolizerJexlContext.setDataObject(null);
  }

}
