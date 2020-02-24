package com.revolsys.geopackage;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jeometry.common.io.PathName;

import com.revolsys.io.FileUtil;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.AbstractRecordStore;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.record.schema.RecordStoreSchemaElement;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.manager.GeoPackageManager;

public class GeoPackageRecordStore extends AbstractRecordStore {

  private static final List<String> IGNORE_TABLE_NAMES = Arrays.asList("ogr_empty_table");

  private final File file;

  private boolean createMissingRecordStore = false;

  private GeoPackage geoPackage;

  public GeoPackageRecordStore(final File file) {
    this.file = file;
    setConnectionProperties(Collections.singletonMap("url", FileUtil.toUrl(file).toString()));
  }

  private void addRecordDefinition(final Map<PathName, RecordStoreSchemaElement> elementsByPath,
    final GeoPackageRecordDefinition recordDefinition) {
    final PathName pathName = recordDefinition.getPathName();
    elementsByPath.put(pathName, recordDefinition);
  }

  @Override
  public void close() {
    try {
      super.close();
    } finally {
      final GeoPackage geoPackage = this.geoPackage;
      this.geoPackage = null;
      if (geoPackage != null) {
        geoPackage.close();
      }
    }
  }

  @Override
  public int getRecordCount(final Query query) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getRecordStoreType() {
    return GeoPackageFactory.DESCRIPTION;
  }

  @Override
  protected void initializeDo() {
    if (!this.file.exists()) {
      if (this.createMissingRecordStore) {
        FileUtil.createParentDirectories(this.file);
        if (!GeoPackageManager.create(this.file)) {
          throw new IllegalStateException("Unable to create GeoPackage: " + this.file);
        }
      } else {
        throw new IllegalStateException("GeoPackage not found : " + this.file);
      }
    }
    this.geoPackage = GeoPackageManager.open(this.file);
  }

  public boolean isCreateMissingRecordStore() {
    return this.createMissingRecordStore;
  }

  @Override
  public RecordWriter newRecordWriter(final boolean throwExceptions) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected Map<PathName, ? extends RecordStoreSchemaElement> refreshSchemaElements(
    final RecordStoreSchema schema) {
    synchronized (schema) {
      final Map<PathName, RecordStoreSchemaElement> elementsByPath = new TreeMap<>();
      for (final String tableName : this.geoPackage.getTables()) {
        if (IGNORE_TABLE_NAMES.contains(tableName)) {
        } else if (this.geoPackage.isFeatureTable(tableName)) {
          final FeatureDao featureDao = this.geoPackage.getFeatureDao(tableName);
          final GeoPackageFeatureTable recordDefinition = new GeoPackageFeatureTable(schema,
            featureDao);
          addRecordDefinition(elementsByPath, recordDefinition);
        } else if (this.geoPackage.isAttributeTable(tableName)) {
          final AttributesDao attributesDao = this.geoPackage.getAttributesDao(tableName);
          final GeoPackageAttributesTable recordDefinition = new GeoPackageAttributesTable(schema,
            attributesDao);
          addRecordDefinition(elementsByPath, recordDefinition);
        }
      }

      return elementsByPath;
    }
  }

  public GeoPackageRecordStore setCreateMissingRecordStore(final boolean createMissingRecordStore) {
    this.createMissingRecordStore = createMissingRecordStore;
    return this;
  }

}
