package com.revolsys.io.moep;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryReaderFactory;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;

public class MoepBinaryReaderFactory extends
  AbstractDataObjectAndGeometryReaderFactory {

  /** The factory instance. */
  public static final MoepBinaryReaderFactory INSTANCE = new MoepBinaryReaderFactory();

  /**
   * Get the factory instance.
   * 
   * @return The instance.
   */
  public static MoepBinaryReaderFactory get() {
    return INSTANCE;
  }

  public MoepBinaryReaderFactory() {
    super("MOEP (BC Ministry of Environment and Parks)", true);
    addMediaTypeAndFileExtension("application/x-bcgov-moep-bin", "bin");
    setCustomAttributionSupported(false);
  }

  public DataObjectReader createDataObjectReader(final Resource resource,
    final DataObjectFactory dataObjectFactory) {
    return new MoepBinaryReader(null, resource, dataObjectFactory);
  }

  public DataObjectReader createDataObjectReader(DataObjectMetaData metaData,
    final Resource resource, final DataObjectFactory dataObjectFactory) {
    throw new UnsupportedOperationException();
  }
}
