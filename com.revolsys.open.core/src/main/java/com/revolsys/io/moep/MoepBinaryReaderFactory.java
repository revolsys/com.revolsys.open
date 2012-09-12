package com.revolsys.io.moep;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryReaderFactory;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;

public class MoepBinaryReaderFactory extends
  AbstractDataObjectAndGeometryReaderFactory {
  public MoepBinaryReaderFactory() {
    super("MOEP (BC Ministry of Environment and Parks)", true);
    addMediaTypeAndFileExtension("application/x-bcgov-moep-bin", "bin");
    setCustomAttributionSupported(false);
  }

  public DataObjectReader createDataObjectReader(
    final DataObjectMetaData metaData, final Resource resource,
    final DataObjectFactory dataObjectFactory) {
    throw new UnsupportedOperationException();
  }

  @Override
  public DataObjectReader createDataObjectReader(final Resource resource,
    final DataObjectFactory dataObjectFactory) {
    return new MoepBinaryReader(null, resource, dataObjectFactory);
  }
}
