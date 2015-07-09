package com.revolsys.format.saif;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.io.AbstractRecordIoFactory;
import com.revolsys.data.record.io.RecordReader;

public class Saif extends AbstractRecordIoFactory {

  public Saif() {
    super("SAIF");
    addMediaTypeAndFileExtension("zip/x-saif", "saf");
  }

  @Override
  public RecordReader createRecordReader(final Resource resource,
    final RecordFactory recordFactory) {
    final SaifReader reader = new SaifReader(resource);
    return reader;
  }

  @Override
  public boolean isBinary() {
    return true;
  }
}
