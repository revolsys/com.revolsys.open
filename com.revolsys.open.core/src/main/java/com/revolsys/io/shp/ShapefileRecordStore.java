package com.revolsys.io.shp;

import java.io.File;
import java.util.Map;

import com.revolsys.data.io.AbstractRecordStore;
import com.revolsys.data.io.RecordStoreSchema;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.ArrayRecord;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;

public class ShapefileRecordStore extends AbstractRecordStore {

  private final File directory;

  private ShapefileDirectoryWriter writer;

  public ShapefileRecordStore(final File directory) {
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
  public Record create(final RecordDefinition metaData) {
    final String typePath = metaData.getPath();
    final RecordDefinition savedMetaData = getRecordDefinition(typePath);
    if (savedMetaData == null) {
      return new ArrayRecord(metaData);
    } else {
      return new ArrayRecord(savedMetaData);
    }
  }

  @Override
  public Writer<Record> createWriter() {
    return writer;
  }

  @Override
  public RecordDefinition getRecordDefinition(final String typePath) {
    return writer.getMetaData(typePath);
  }

  @Override
  public int getRowCount(final Query query) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void insert(final Record dataObject) {
    writer.write(dataObject);
  }

  @Override
  protected void loadSchemaRecordDefinitions(
    final RecordStoreSchema schema,
    final Map<String, RecordDefinition> metaDataMap) {
  }

  @Override
  protected void loadSchemas(final Map<String, RecordStoreSchema> schemaMap) {
  }

}
