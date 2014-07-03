package com.revolsys.io.moep;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.AbstractDataObjectAndGeometryReaderFactory;
import com.revolsys.data.io.DataObjectReader;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.RecordDefinition;

public class MoepBinaryReaderFactory extends
  AbstractDataObjectAndGeometryReaderFactory {
  public MoepBinaryReaderFactory() {
    super("MOEP (BC Ministry of Environment and Parks)", true);
    addMediaTypeAndFileExtension("application/x-bcgov-moep-bin", "bin");
    setCustomAttributionSupported(false);
  }

  public DataObjectReader createDataObjectReader(
    final RecordDefinition metaData, final Resource resource,
    final RecordFactory dataObjectFactory) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DataObjectReader createDataObjectReader(final Resource resource,
    final RecordFactory dataObjectFactory) {
    return new MoepBinaryReader(null, resource, dataObjectFactory);
  }
}
