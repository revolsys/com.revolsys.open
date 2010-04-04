package com.revolsys.jump.model;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jump.feature.filter.FeatureFilter;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;

public class FeatureCollectionFilter {
  private FeatureFilter filter;

  public FeatureCollectionFilter(final FeatureFilter filter) {
    this.filter = filter;
  }

  @SuppressWarnings("unchecked")
  public FeatureCollection filter(final FeatureCollection featureCollection) {
    List<Feature> features = featureCollection.getFeatures();
    List<Feature> filteredFeatures = filter(features);
    FeatureSchema featureSchema = featureCollection.getFeatureSchema();
    return new FeatureDataset(filteredFeatures, featureSchema);
  }

  public List<Feature> filter(final List<Feature> features) {
    List<Feature> filteredFeatures = new ArrayList<Feature>();
    for (Feature feature : features) {
      if (filter.accept(feature)) {
        filteredFeatures.add(feature);
      }
    }
    return filteredFeatures;
  }
}
