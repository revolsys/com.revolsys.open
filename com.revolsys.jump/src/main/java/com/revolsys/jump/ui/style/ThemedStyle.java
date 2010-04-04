package com.revolsys.jump.ui.style;

import java.util.List;

import com.revolsys.jump.ui.model.Theme;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public interface ThemedStyle<T extends Theme> extends Style {
  /**
   * Get the list of themes from the style.
   * 
   * @return The list of themes.
   */
  List<T> getThemes();
}
