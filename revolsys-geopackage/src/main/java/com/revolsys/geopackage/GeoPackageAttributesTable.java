package com.revolsys.geopackage;

import com.revolsys.record.schema.RecordStoreSchema;

import mil.nga.geopackage.attributes.AttributesDao;

public class GeoPackageAttributesTable extends GeoPackageRecordDefinition<AttributesDao> {

  public GeoPackageAttributesTable(final RecordStoreSchema schema,
    final AttributesDao attributesDao) {
    super(schema, attributesDao);
  }
}
