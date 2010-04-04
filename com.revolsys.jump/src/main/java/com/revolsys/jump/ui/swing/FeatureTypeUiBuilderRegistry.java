package com.revolsys.jump.ui.swing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.WorkbenchContext;

public class FeatureTypeUiBuilderRegistry {
  public static final String KEY = FeatureTypeUiBuilderRegistry.class.getName();

  private Map<FeatureSchema, List<ValueUiBuilder>> uiBuilders = new HashMap<FeatureSchema, List<ValueUiBuilder>>();

  public static FeatureTypeUiBuilderRegistry getInstance(
    final WorkbenchContext context) {
    Blackboard blackboard = context.getBlackboard();
    FeatureTypeUiBuilderRegistry instance = (FeatureTypeUiBuilderRegistry)blackboard.get(KEY);
    if (instance == null) {
      instance = new FeatureTypeUiBuilderRegistry();
      blackboard.put(KEY, instance);
    }
    return instance;
  }

  public void addValueUiBuilder(final FeatureSchema schema, final int index,
    final ValueUiBuilder builder) {
    List<ValueUiBuilder> builders = getUiBuilders(schema);
    builders.set(index, builder);
  }

  public ValueUiBuilder getValueUiBuilder(final FeatureSchema schema,
    final String attributeName) {
    return getValueUiBuilder(schema, schema.getAttributeIndex(attributeName));

  }

  public ValueUiBuilder getValueUiBuilder(final FeatureSchema schema,
    final int index) {
    List<ValueUiBuilder> builders = getUiBuilders(schema);
    return builders.get(index);
  }

  private List<ValueUiBuilder> getUiBuilders(final FeatureSchema schema) {
    List<ValueUiBuilder> builders = uiBuilders.get(schema);
    if (builders == null) {
      builders = new ArrayList<ValueUiBuilder>(schema.getAttributeCount());
      for (int i = 0; i < schema.getAttributeCount(); i++) {
        builders.add(null);
      }
      uiBuilders.put(schema, builders);
    }
    return builders;
  }
}
