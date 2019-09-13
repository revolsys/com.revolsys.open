package com.revolsys.swing.map.layer.record.component.recordmerge;

import java.awt.Color;
import java.awt.Component;

import org.jeometry.common.awt.WebColors;

import com.revolsys.util.CaseConverter;

enum MergeFieldMatchType {
  ERROR(WebColors.Pink, WebColors.Red), //
  OVERRIDDEN(WebColors.Moccasin, WebColors.DarkOrange), //
  WAS_NULL(WebColors.PaleTurquoise, WebColors.DarkTurquoise), //
  ALLOWED_NOT_EQUAL(WebColors.PaleTurquoise, WebColors.DarkTurquoise), //
  EQUAL(WebColors.LightGreen, WebColors.Green);

  private String label;

  private Color color;

  private Color colorSelected;

  private Color colorOdd;

  private Color colorSelectedOdd;

  private MergeFieldMatchType(final Color color, final Color colorSelected) {
    this.label = CaseConverter.toCapitalizedWords(name());
    this.color = color;
    this.colorSelected = colorSelected;
    this.colorOdd = new Color(Math.max((int)(color.getRed() * 0.9), 0),
      Math.max((int)(color.getGreen() * 0.9), 0), Math.max((int)(color.getBlue() * 0.9), 0));
    this.colorSelected = colorSelected;
    this.colorSelectedOdd = new Color(Math.max((int)(colorSelected.getRed() * 0.9), 0),
      Math.max((int)(colorSelected.getGreen() * 0.9), 0),
      Math.max((int)(colorSelected.getBlue() * 0.9), 0));
  }

  public Color getColor() {
    return this.color;
  }

  public Color getColorOdd() {
    return this.colorOdd;
  }

  public Color getColorSelected() {
    return this.colorSelected;
  }

  public Color getColorSelectedOdd() {
    return this.colorSelectedOdd;
  }

  public Component setColor(final Component renderer, final boolean selected, final boolean even) {
    if (selected) {
      if (even) {
        renderer.setBackground(this.colorSelected);
      } else {
        renderer.setBackground(this.colorSelectedOdd);
      }
      renderer.setForeground(WebColors.White);
    } else {
      if (even) {
        renderer.setBackground(this.color);
      } else {
        renderer.setBackground(this.colorOdd);
      }
      renderer.setForeground(WebColors.Black);
    }
    return renderer;
  }

  @Override
  public String toString() {
    return this.label;
  }
}
