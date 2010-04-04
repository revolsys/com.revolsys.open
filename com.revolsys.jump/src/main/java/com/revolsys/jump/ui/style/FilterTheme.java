package com.revolsys.jump.ui.style;

import java.awt.Color;

import com.revolsys.jump.feature.filter.FeatureFilter;
import com.revolsys.jump.feature.filter.NameValueFeatureFilter;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;

public class FilterTheme extends AbstractLayerTheme {
  private FeatureFilter filter;

  public FilterTheme() {
    this("", new NameValueFeatureFilter(), new BasicStyle(Color.gray));
  }

  public FilterTheme(final String label, final FeatureFilter filter,
    final BasicStyle style) {
    super(label, style);
    this.filter = filter;
  }

  public FeatureFilter getFilter() {
    return filter;
  }

  public void setFilter(final FeatureFilter filter) {
    this.filter = filter;
  }
}
