package com.revolsys.swing.map.layer.dataobject.style.panel;

import com.revolsys.swing.component.ValuePanel;

public class PolygonSymbolizerPanel extends ValuePanel<Object> {
  // TODO displacementX
  // TODO displacementY
  // TODO fillPattern;
  // TODO offset

//  /**
//   * 
//   */
//  private static final long serialVersionUID = 3381121558744199324L;
//
//  private final FillPanel fillPanel;
//
//  public PolygonSymbolizerPanel(final PolygonSymbolizer polygonSymbolizer) {
//    setValue(polygonSymbolizer);
//    setLayout(new SpringLayout());
//    setBorder(BorderFactory.createTitledBorder(
//      BorderFactory.createLineBorder(Color.BLACK), "Polygon"));
//
//    final JPanel stylePanel = new JPanel(new SpringLayout());
//    add(stylePanel);
//    final Stroke stroke = polygonSymbolizer.getStroke();
//    final Unit<Length> unit = polygonSymbolizer.getUnit();
//
//    final Fill fill = polygonSymbolizer.getFill();
//    fillPanel = new FillPanel(fill, stroke, unit);
//    stylePanel.add(fillPanel);
//    SpringLayoutUtil.makeColumns(stylePanel, 1, 0, 0, 0, 5);
//
//    final ShapePreviewPanel preview = new ShapePreviewPanel(stroke, fill);
//    preview.setBorder(BorderFactory.createEtchedBorder());
//    add(preview);
//
//    fillPanel.addPropertyChangeListener("stroke", preview);
//    fillPanel.addPropertyChangeListener("fill", preview);
//    SpringLayoutUtil.makeColumns(this, 2, 0, 0, 5, 5);
//
//  }
//
//  @Override
//  public void save() {
//    super.save();
//    final PolygonSymbolizer polygonSymbolizer = getValue();
//    polygonSymbolizer.setFill(fillPanel.getCurrentFill());
//    polygonSymbolizer.setStroke(fillPanel.getValue());
//
//  }
}
