package com.revolsys.io;

import com.revolsys.record.Record;
import com.revolsys.record.io.RecordWriter;

public abstract class AbstractRecordWriter extends AbstractWriter<Record> implements RecordWriter {
  private boolean writeNulls = false;

  private boolean indent = false;

  @Override
  public boolean isIndent() {
    return this.indent;
  }

  @Override
  public boolean isWriteNulls() {
    return this.writeNulls;
  }

  @Override
  public void setIndent(final boolean indent) {
    this.indent = indent;
  }

  @Override
  public void setWriteNulls(final boolean writeNulls) {
    this.writeNulls = writeNulls;
  }
}
