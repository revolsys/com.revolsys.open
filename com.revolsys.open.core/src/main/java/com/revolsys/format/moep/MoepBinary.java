package com.revolsys.format.moep;

import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.io.AbstractRecordIoFactory;
import com.revolsys.data.record.io.RecordReader;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

public class MoepBinary extends AbstractRecordIoFactory {
  public MoepBinary() {
    super("MOEP (BC Ministry of Environment and Parks)");
    addMediaTypeAndFileExtension("application/x-bcgov-moep-bin", "bin");
  }

  public RecordReader createRecordReader(final RecordDefinition recordDefinition,
    final Resource resource, final RecordFactory recordFactory) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RecordReader createRecordReader(final Resource resource,
    final RecordFactory recordFactory) {
    return new MoepBinaryReader(null, resource, recordFactory);
  }

  @Override
  public boolean isBinary() {
    return true;
  }

  @Override
  public boolean isCustomFieldsSupported() {
    return false;
  }
}
