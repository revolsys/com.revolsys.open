package com.revolsys.jump.ui;

import java.util.List;

import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layerable;

public final class CategoryUtil {
  private CategoryUtil() {
  }

  @SuppressWarnings("unchecked")
  public static void setVisible(final Category category, final boolean visible) {
    List<Layerable> layerables = category.getLayerables();
    for (Layerable layerable : layerables) {
      layerable.setVisible(visible);
    }

  }

}
