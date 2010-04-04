package com.revolsys.jump.ui.model;

import java.util.List;

import com.vividsolutions.jump.workbench.model.Layerable;

public interface ThemedLayerable extends Layerable {
  List<Theme> getThemes();
}
