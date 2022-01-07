package com.revolsys.odata.model;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jeometry.common.io.PathName;

import com.revolsys.http.ApacheHttpRequestBuilderFactory;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.AbstractRecordStore;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.record.schema.RecordStoreSchemaElement;
import com.revolsys.util.Debug;
import com.revolsys.util.UrlUtil;

public class ODataRecordStore extends AbstractRecordStore {

  private final ApacheHttpRequestBuilderFactory requestFactory;

  private final URI uri;

  public ODataRecordStore(final ApacheHttpRequestBuilderFactory requestFactory, final URI uri) {
    this.requestFactory = requestFactory;
    this.uri = uri;
  }

  @Override
  public int getRecordCount(final Query query) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getRecordStoreType() {
    return "OData";
  }

  @Override
  public RecordWriter newRecordWriter(final boolean throwExceptions) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected RecordStoreSchema newRootSchema() {
    return new ODataRecordStoreSchema(this);
  }

  @Override
  protected Map<PathName, ? extends RecordStoreSchemaElement> refreshSchemaElements(
    final RecordStoreSchema schema) {
    final Map<PathName, RecordStoreSchemaElement> elements = new LinkedHashMap<>();
    final PathName pathName = schema.getPathName();
    final String schemaName = pathName.toString().substring(1).replace('/', '.');
    final URI uri = UrlUtil.appendPath(this.uri, "$metadata");
    final JsonObject json = this.requestFactory.get(uri).addParameter("$format", "json").getJson();
    int startIndex;
    if (PathName.ROOT.equals(pathName)) {
      startIndex = 0;
    } else {
      startIndex = schemaName.length() + 1;
    }
    for (final String name : json.keySet()) {
      if (name.equals(schemaName)) {
        refreshSchemaRecordDefinitions((ODataRecordStoreSchema)schema, elements, pathName, json,
          name);
      } else if (!name.startsWith("$")) {
        String childName = name.substring(startIndex);
        final int endIndex = childName.indexOf('.');
        if (endIndex != -1) {
          childName = childName.substring(0, endIndex);
        }
        final PathName childPath = pathName.newChild(name);
        final ODataRecordStoreSchema childSchema = new ODataRecordStoreSchema(
          (ODataRecordStoreSchema)schema, childPath);
        elements.put(childPath, childSchema);
      }
    }

    return elements;
  }

  private void refreshSchemaRecordDefinitions(final ODataRecordStoreSchema schema,
    final Map<PathName, RecordStoreSchemaElement> elements, final PathName pathName,
    final JsonObject metadata, final String name) {
    final JsonObject schemaMap = metadata.getJsonObject(name);
    final JsonObject containerMap = schemaMap.getJsonObject("Container");
    if (containerMap.equalValue("$Kind", "EntityContainer")) {

      for (final String entitySetName : containerMap.keySet()) {
        if (!entitySetName.startsWith("$")) {
          final JsonObject entitySet = containerMap.getJsonObject(entitySetName);
          if (entitySet.equalValue("$Kind", "EntitySet")) {
            final String entityType = entitySet.getString("$Type");
            final PathName childName = pathName.newChild(entitySetName);
            final ODataRecordDefinition recordDefinition = new ODataRecordDefinition(schema,
              childName, metadata, entityType);
            elements.put(childName, recordDefinition);
          } else {
            Debug.noOp();
          }
        }
      }
    } else {
      Debug.noOp();
    }
  }

  @Override
  public String toString() {
    return this.uri.toString();
  }
}
