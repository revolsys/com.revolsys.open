/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI for
 * visualizing and manipulating spatial features with geometry and attributes.
 * 
 * Copyright (C) 2003 Vivid Solutions
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * For more information, contact:
 * 
 * Vivid Solutions Suite #1A 2328 Government Street Victoria BC V8T 5G5 Canada
 * 
 * (250)385-6040 www.vividsolutions.com
 */
package com.revolsys.jump.ui.style;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.jump.feature.filter.FeatureFilter;
import com.revolsys.jump.ui.model.Theme;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class FilterThemingStyle implements ThemedStyle<FilterTheme> {

  private boolean enabled = true;

  private Layer layer;

  /** The list of {@link FilterTheme}. */
  private List<FilterTheme> themes = new ArrayList<FilterTheme>();

  public FilterThemingStyle() {
  }

  public FilterThemingStyle(final List<FilterTheme> themes, final Layer layer) {
    this(layer);
    this.themes = themes;
  }

  public FilterThemingStyle(final Layer layer) {
    this.layer = layer;
  }

  public void paint(final Feature feature, final Graphics2D g,
    final Viewport viewport) throws Exception {
    Theme theme = getTheme(feature);
    if (theme != null) {
      if (theme.isVisible()) {
        Style style = theme.getBasicStyle();
        style.paint(feature, g, viewport);
      }
    } else {
      layer.getBasicStyle().paint(feature, g, viewport);
    }
  }

  private Theme getTheme(final Feature feature) {
    for (FilterTheme theme : themes) {
      FeatureFilter filter = theme.getFilter();
      if (filter.accept(feature)) {
        return theme;
      }
    }
    return null;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static FilterThemingStyle get(final Layer layer) {
    FilterThemingStyle style = (FilterThemingStyle)layer.getStyle(FilterThemingStyle.class);
    if (style == null) {
      style = new FilterThemingStyle(layer);
      layer.addStyle(style);
    }
    return style;
  }

  public FilterTheme getTheme(final int i) {
    return themes.get(i);
  }

  public void clearThemes() {
    themes.clear();
  }

  public List<FilterTheme> getThemes() {
    return themes;
  }

  public void setThemes(final List<FilterTheme> themes) {
    this.themes = themes;
  }

  public void initialize(final Layer layer) {
  }

}
