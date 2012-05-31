package com.revolsys.io.saif;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryReaderFactory;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObjectFactory;

public class SaifIoFactory extends AbstractDataObjectAndGeometryReaderFactory {

  public SaifIoFactory() {
    super("SAIF", false);
    addMediaTypeAndFileExtension("zip/x-saif", "saf");
  }

  public DataObjectReader createDataObjectReader(
    final Resource resource,
    final DataObjectFactory dataObjectFactory) {
    final SaifReader reader = new SaifReader(resource);
    return reader;
  }
}
