package com.revolsys.io.shp;

import java.io.File;
import java.util.Map;

import com.revolsys.gis.data.io.AbstractDataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.ArrayRecord;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Query;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;

public class ShapefileDataObjectStore extends AbstractDataObjectStore {

  private final File directory;

  private ShapefileDirectoryWriter writer;

  public ShapefileDataObjectStore(final File directory) {
    this.directory = directory;
    directory.mkdirs();
    writer = new ShapefileDirectoryWriter(directory);
    writer.setLogCounts(false);
  }

  @Override
  public void close() {
    super.close();
    FileUtil.closeSilent(writer);
    writer = null;
  }

  @Override
  public DataObject create(final DataObjectMetaData metaData) {
    final String typePath = metaData.getPath();
    final DataObjectMetaData savedMetaData = getMetaData(typePath);
    if (savedMetaData == null) {
      return new ArrayRecord(metaData);
    } else {
      return new ArrayRecord(savedMetaData);
    }
  }

  @Override
  public Writer<DataObject> createWriter() {
    return writer;
  }

  @Override
  public DataObjectMetaData getMetaData(final String typePath) {
    return writer.getMetaData(typePath);
  }

  @Override
  public int getRowCount(final Query query) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void insert(final DataObject dataObject) {
    writer.write(dataObject);
  }

  @Override
  protected void loadSchemaDataObjectMetaData(
    final DataObjectStoreSchema schema,
    final Map<String, DataObjectMetaData> metaDataMap) {
  }

  @Override
  protected void loadSchemas(final Map<String, DataObjectStoreSchema> schemaMap) {
  }

}
