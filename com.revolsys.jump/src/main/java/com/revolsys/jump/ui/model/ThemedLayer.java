package com.revolsys.jump.ui.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.jump.ui.style.ThemedStyle;
import com.revolsys.jump.ui.swing.FeatureTypeUiBuilderRegistry;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class ThemedLayer extends Layer implements ThemedLayerable {
  private FeatureTypeUiBuilderRegistry uiBuilderRegistry = new FeatureTypeUiBuilderRegistry();

  public ThemedLayer() {
    super();
  }

  public ThemedLayer(final String name, final Color fillColor,
    final FeatureCollection featureCollection, final LayerManager layerManager) {
    super(name, fillColor, featureCollection, layerManager);
  }

  /**
   * Get all the themes for the layer from the {@link ThemedStyle} styles.
   * 
   * @return The {@link Theme}
   */
  @SuppressWarnings("unchecked")
  public List<Theme> getThemes() {
    List<Theme> themes = new ArrayList<Theme>();
    for (Style style : (List<Style>)getStyles()) {
      if (style instanceof ThemedStyle) {
        ThemedStyle themedStyle = (ThemedStyle)style;
        themes.addAll(themedStyle.getThemes());
      }
    }
    return themes;
  }

  public FeatureTypeUiBuilderRegistry getUiBuilderRegistry() {
    return uiBuilderRegistry;
  }

  public void setUiBuilderRegistry(
    final FeatureTypeUiBuilderRegistry uiBuilderRegistry) {
    this.uiBuilderRegistry = uiBuilderRegistry;
  }

}
