package com.revolsys.swing.map.layer.dataobject.style.panel;


public class FillPanel {
//  /**
//   * 
//   */
//  private static final long serialVersionUID = 4862590558729520017L;
//
//  private final ColorChooserPanel colorField;
//
//  private final Fill fill;
//
//  private Fill currentFill;
//
//  public FillPanel(final Fill fill, final Stroke stroke, final Unit<Length> unit) {
//    super(stroke, unit);
//    this.fill = fill;
//    if (fill != null) {
//      this.currentFill = fill.clone();
//    }
//    setLayout(new SpringLayout());
//
//    final CharSequence color = fill.getColorString();
//    colorField = new ColorChooserPanel(color, fill.getAlpha());
//    colorField.setBorder(BorderFactory.createTitledBorder("Fill Color"));
//    colorField.addPropertyChangeListener("color", this);
//    add(colorField, 0);
//
//    SpringLayoutUtil.makeRows(this, 0, 0, 5, 5, 3, 3);
//  }
//
//  public Fill getCurrentFill() {
//    return currentFill;
//  }
//
//  public Fill getFill() {
//    return fill;
//  }
//
//  @Override
//  public void propertyChange(final PropertyChangeEvent event) {
//    super.propertyChange(event);
//    if (currentFill != null) {
//      final Object oldValue = this.currentFill;
//      this.currentFill = this.currentFill.clone();
//      updateFill(this.currentFill);
//      firePropertyChange("fill", oldValue, this.currentFill);
//    }
//  }
//
//  @Override
//  public void save() {
//    super.save();
//    updateFill(currentFill);
//  }
//
//  private void updateFill(final Fill fill) {
//    final CharSequence color = colorField.getCssColor();
//    fill.setColor(color);
//
//    final Number opacity = colorField.getOpacity();
//    fill.setOpacity(opacity);
//  }
}
