package com.revolsys.format.moep;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.io.AbstractRecordAndGeometryReaderFactory;
import com.revolsys.data.record.io.RecordReader;
import com.revolsys.data.record.schema.RecordDefinition;

public class MoepBinaryReaderFactory extends AbstractRecordAndGeometryReaderFactory {
  public MoepBinaryReaderFactory() {
    super("MOEP (BC Ministry of Environment and Parks)");
    addMediaTypeAndFileExtension("application/x-bcgov-moep-bin", "bin");
  }

  public RecordReader createRecordReader(final RecordDefinition recordDefinition,
    final Resource resource, final RecordFactory recordFactory) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RecordReader createRecordReader(final Resource resource, final RecordFactory recordFactory) {
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
