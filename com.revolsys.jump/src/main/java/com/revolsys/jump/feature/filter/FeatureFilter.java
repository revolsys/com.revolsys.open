package com.revolsys.jump.feature.filter;

import com.vividsolutions.jump.feature.Feature;

public interface FeatureFilter {
  boolean accept(Feature feature);
}
