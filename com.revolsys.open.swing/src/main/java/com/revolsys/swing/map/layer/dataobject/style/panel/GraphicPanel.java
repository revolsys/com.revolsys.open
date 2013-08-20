package com.revolsys.swing.map.layer.dataobject.style.panel;

import com.revolsys.swing.component.ValueField;

public class GraphicPanel extends ValueField
// implements PropertyChangeListener
{

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  // /**
  // *
  // */
  // private static final long serialVersionUID = -4633988961396971031L;
  //
  // private FillPanel fillPanel;
  //
  // private StrokePanel strokePanel;
  //
  // private JComboBox markChooser;
  //
  // private final LengthMeasurePanel sizeField;
  //
  // public GraphicPanel(final Graphic graphic, final Unit<Length> unit) {
  // setValue(graphic);
  // setLayout(new SpringLayout());
  //
  // final Measure<Length> size = graphic.getSize();
  // sizeField = new LengthMeasurePanel(size, 0, 300, 50, true, unit);
  // sizeField.setBorder(BorderFactory.createTitledBorder("Graphic Size"));
  // add(sizeField);
  //
  // final GraphicSymbol symbol = graphic.getSymbols().get(0);
  // if (symbol instanceof WellKnownMarkGraphicSymbol) {
  // final WellKnownMarkGraphicSymbol wkmSymbol =
  // (WellKnownMarkGraphicSymbol)symbol;
  // final WellKnownMarkGraphicSymbolPanel wkmPanel = new
  // WellKnownMarkGraphicSymbolPanel(
  // wkmSymbol, unit);
  // wkmPanel.addPropertyChangeListener("stroke", this);
  // wkmPanel.addPropertyChangeListener("fill", this);
  // wkmPanel.addPropertyChangeListener("shape", this);
  // add(wkmPanel);
  // }
  //
  // SpringLayoutUtil.makeColumns(this, 1, 0, 0, 0, 0);
  // }
  //
  // @Override
  // public void propertyChange(final PropertyChangeEvent event) {
  // firePropertyChange(event.getPropertyName(), event.getOldValue(),
  // event.getNewValue());
  // }
  //
  // @Override
  // public void save() {
  // super.save();
  // final Graphic graphic = getValue();
  // final Measure<Length> size = sizeField.getValue();
  // graphic.setSize(size);
  // }
}
