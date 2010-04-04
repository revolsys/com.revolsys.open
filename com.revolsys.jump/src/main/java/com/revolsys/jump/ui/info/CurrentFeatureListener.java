package com.revolsys.jump.ui.info;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;

public interface CurrentFeatureListener {
  void featureSelected(Layer layer, Feature feature);
}
