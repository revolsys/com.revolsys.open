package com.revolsys.swing.map.layer.dataobject.style.panel;

import com.revolsys.swing.component.ValuePanel;

public class PointSymbolizerPanel extends ValuePanel<Object> {
//
//  /**
//   * 
//   */
//  private static final long serialVersionUID = -2071867858852293270L;
//
//  private final GraphicPanel graphicPanel;
//
//  public PointSymbolizerPanel(final PointSymbolizer pointSymbolizer) {
//    setValue(pointSymbolizer);
//    setBorder(BorderFactory.createTitledBorder(
//      BorderFactory.createLineBorder(Color.BLACK), "Point"));
//    setLayout(new SpringLayout());
//    final Graphic graphic = pointSymbolizer.getGraphic();
//
//    graphicPanel = new GraphicPanel(graphic, pointSymbolizer.getUnit());
//    add(graphicPanel);
//
//    final ShapePreviewPanel preview = new ShapePreviewPanel(null, null);
//    preview.setBorder(BorderFactory.createEtchedBorder());
//    add(preview);
//
//    graphicPanel.addPropertyChangeListener("stroke", preview);
//    graphicPanel.addPropertyChangeListener("fill", preview);
//    graphicPanel.addPropertyChangeListener("shape", preview);
//    SpringLayoutUtil.makeColumns(this, 2, 0, 0, 0, 0);
//  }
//
//  @Override
//  public void save() {
//    super.save();
//    final PointSymbolizer pointSymbolizer = getValue();
//
//    final Graphic graphic = graphicPanel.getValue();
//    pointSymbolizer.setGraphic(graphic);
//
//  }
}
