package com.revolsys.record.io.format.json;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionFactory;
import com.revolsys.spring.resource.ClassPathResource;
import com.revolsys.spring.resource.Resource;

public class JsonResourceRecordDefinitionFactory extends BaseObjectWithProperties
  implements RecordDefinitionFactory {
  private final Map<String, RecordDefinition> recordDefinitionMap = new HashMap<>();

  public JsonResourceRecordDefinitionFactory(final Resource resource) {
    for (final Resource childResource : resource.getChildren((fileName) -> {
      return fileName.endsWith(".json");
    })) {
      final RecordDefinition recordDefinition = MapObjectFactory
        .toObject(Resource.getResource(childResource));
      final String name = recordDefinition.getPath();
      this.recordDefinitionMap.put(name, recordDefinition);
    }
  }

  public JsonResourceRecordDefinitionFactory(final String locationPattern) {
    this(new ClassPathResource(locationPattern));
  }

  @Override
  public RecordDefinition getRecordDefinition(final String typePath) {
    return this.recordDefinitionMap.get(typePath);
  }
}
