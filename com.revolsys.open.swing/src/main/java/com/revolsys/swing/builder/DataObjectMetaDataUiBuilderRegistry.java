package com.revolsys.swing.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;

public class DataObjectMetaDataUiBuilderRegistry {

  private static final String NAME = UiBuilderRegistry.class.getName()
    + "/builder";

  private static DataObjectMetaDataUiBuilderRegistry INSTANCE = new DataObjectMetaDataUiBuilderRegistry();

  public static DataObjectMetaDataUiBuilderRegistry getInstance() {
    return INSTANCE;
  }

  public static void setBuilder(final Attribute attribute,
    final ValueUiBuilder builder) {
    attribute.setProperty(NAME, builder);
  }

  private final Map<DataObjectMetaData, List<ValueUiBuilder>> uiBuilders = new HashMap<DataObjectMetaData, List<ValueUiBuilder>>();

  public void addValueUiBuilder(final DataObjectMetaData schema,
    final int index, final ValueUiBuilder builder) {
    final List<ValueUiBuilder> builders = getUiBuilders(schema);
    builders.set(index, builder);
  }

  public void addValueUiBuilder(final DataObjectMetaData metaData,
    final String fieldName, final ValueUiBuilder builder) {
    final int attributeIndex = metaData.getAttributeIndex(fieldName);
    addValueUiBuilder(metaData, attributeIndex, builder);
  }

  private List<ValueUiBuilder> getUiBuilders(final DataObjectMetaData schema) {
    List<ValueUiBuilder> builders = this.uiBuilders.get(schema);
    if (builders == null) {
      builders = new ArrayList<ValueUiBuilder>(schema.getAttributeCount());
      for (int i = 0; i < schema.getAttributeCount(); i++) {
        builders.add(null);
      }
      this.uiBuilders.put(schema, builders);
    }
    return builders;
  }

  public ValueUiBuilder getValueUiBuilder(final DataObjectMetaData metaData,
    final int index) {
    final Attribute attribute = metaData.getAttribute(index);
    final ValueUiBuilder builder = attribute.getProperty(NAME);
    if (builder != null) {
      return builder;
    } else {
      final List<ValueUiBuilder> builders = getUiBuilders(metaData);
      return builders.get(index);
    }
  }

  public ValueUiBuilder getValueUiBuilder(final DataObjectMetaData schema,
    final String attributeName) {
    return getValueUiBuilder(schema, schema.getAttributeIndex(attributeName));

  }
}
