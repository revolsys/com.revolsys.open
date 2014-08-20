package com.revolsys.io.zip;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.io.DelegatingWriter;

public class ZipRecordWriter extends DelegatingWriter<Record> {

  public ZipRecordWriter(final Resource zipResource, final String fileType) {

  }

  @Override
  public void close() {
    super.close();
  }
}
