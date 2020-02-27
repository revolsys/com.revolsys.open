package com.revolsys.geopackage;

import java.io.OutputStream;
import java.nio.charset.Charset;

import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.record.io.RecordStoreRecordAndGeometryWriterFactory;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.format.OutputStreamRecordWriter;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

public class GeoPackageReadWriteFactory extends RecordStoreRecordAndGeometryWriterFactory {

  public static final String MIME_TYPE = "application/geopackage+vnd.sqlite3";

  public GeoPackageReadWriteFactory() {
    super(GeoPackage.DESCRIPTION, MIME_TYPE, true, true, GeoPackage.FILE_EXTENSION);
    IoFactoryRegistry.addFactory(this);
  }

  @Override
  public RecordWriter newRecordWriter(final RecordDefinition recordDefinition,
    final Resource resource) {
    final GeoPackageRecordStore recordStore = GeoPackage.createRecordStore(resource);
    if (recordStore == null) {
      return null;
    } else {
      return new GeoPackageRecordWriter(recordStore, recordDefinition);
    }
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    return new OutputStreamRecordWriter(recordDefinition, baseName, GeoPackage.FILE_EXTENSION,
      outputStream);
  }
}
