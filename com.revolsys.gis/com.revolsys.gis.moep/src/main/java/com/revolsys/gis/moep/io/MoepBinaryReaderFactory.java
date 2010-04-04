package com.revolsys.gis.moep.io;

import java.io.File;
import java.net.URL;

import com.revolsys.gis.data.io.AbstractDataObjectReaderFactory;
import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;

public class MoepBinaryReaderFactory extends AbstractDataObjectReaderFactory {

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
    super("MOEP (BC Ministry of Environment and Parks)");
    addMediaTypeAndFileExtension("application/x-bcgov-moep-bin", "bin");
  }

  public Reader<DataObject> createDataObjectReader(
    java.io.Reader in,
    DataObjectFactory factory) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Reader<DataObject> createDataObjectReader(
    final File file,
    final DataObjectFactory dataObjectFactory) {
    return new MoepBinaryReader(null, file, dataObjectFactory);
  }


  @Override
  public Reader<DataObject> createDataObjectReader(
    final URL url,
    final DataObjectFactory dataObjectFactory) {
    return new MoepBinaryReader(url, dataObjectFactory);
  }

}
