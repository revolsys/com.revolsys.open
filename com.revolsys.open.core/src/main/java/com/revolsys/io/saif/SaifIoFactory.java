package com.revolsys.io.saif;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.AbstractDataObjectAndGeometryReaderFactory;
import com.revolsys.data.io.RecordReader;
import com.revolsys.data.record.RecordFactory;

public class SaifIoFactory extends AbstractDataObjectAndGeometryReaderFactory {

  public SaifIoFactory() {
    super("SAIF", false);
    addMediaTypeAndFileExtension("zip/x-saif", "saf");
  }

  @Override
  public RecordReader createRecordReader(final Resource resource,
    final RecordFactory dataObjectFactory) {
    final SaifReader reader = new SaifReader(resource);
    return reader;
  }
}
