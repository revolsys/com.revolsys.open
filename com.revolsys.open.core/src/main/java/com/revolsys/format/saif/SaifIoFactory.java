package com.revolsys.format.saif;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.AbstractRecordAndGeometryReaderFactory;
import com.revolsys.data.io.RecordReader;
import com.revolsys.data.record.RecordFactory;

public class SaifIoFactory extends AbstractRecordAndGeometryReaderFactory {

  public SaifIoFactory() {
    super("SAIF", false);
    addMediaTypeAndFileExtension("zip/x-saif", "saf");
  }

  @Override
  public RecordReader createRecordReader(final Resource resource,
    final RecordFactory recordFactory) {
    final SaifReader reader = new SaifReader(resource);
    return reader;
  }
}
