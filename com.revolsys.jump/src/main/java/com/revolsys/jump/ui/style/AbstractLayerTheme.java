package com.revolsys.jump.ui.style;

import java.util.LinkedHashSet;
import java.util.Set;

import com.revolsys.jump.ui.model.Theme;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class AbstractLayerTheme implements Theme {

  private String label;

  private Set<Style> styles = new LinkedHashSet<Style>();

  private BasicStyle style;

  private boolean visible;

  public AbstractLayerTheme(final String label, final BasicStyle style) {
    this.label = label;
    this.style = style;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public void addStyle(final Style style) {
    styles.add(style);
  }

  public BasicStyle getBasicStyle() {
    return style;
  }

  public void setBasicStyle(final BasicStyle style) {
    this.style = style;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(final boolean visible) {
    this.visible = visible;
  }

}
