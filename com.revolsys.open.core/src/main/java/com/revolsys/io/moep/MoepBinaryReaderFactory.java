package com.revolsys.io.moep;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.AbstractRecordAndGeometryReaderFactory;
import com.revolsys.data.io.RecordReader;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.RecordDefinition;

public class MoepBinaryReaderFactory extends
  AbstractRecordAndGeometryReaderFactory {
  public MoepBinaryReaderFactory() {
    super("MOEP (BC Ministry of Environment and Parks)", true);
    addMediaTypeAndFileExtension("application/x-bcgov-moep-bin", "bin");
    setCustomAttributionSupported(false);
  }

  public RecordReader createRecordReader(
    final RecordDefinition metaData, final Resource resource,
    final RecordFactory dataObjectFactory) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RecordReader createRecordReader(final Resource resource,
    final RecordFactory dataObjectFactory) {
    return new MoepBinaryReader(null, resource, dataObjectFactory);
  }
}
