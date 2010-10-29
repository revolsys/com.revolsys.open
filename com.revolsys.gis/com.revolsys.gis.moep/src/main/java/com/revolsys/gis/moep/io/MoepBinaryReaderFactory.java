package com.revolsys.gis.moep.io;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryReaderFactory;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObjectFactory;

public class MoepBinaryReaderFactory extends AbstractDataObjectAndGeometryReaderFactory {

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
  }

  public DataObjectReader createDataObjectReader(
    final Resource resource,
    final DataObjectFactory dataObjectFactory) {
    return new MoepBinaryReader(null, resource, dataObjectFactory);
  }
}
