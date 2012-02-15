package com.revolsys.gis.parallel;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.Writer;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInProcess;

public class WriterProcess extends BaseInProcess<DataObject> {
  private Writer<DataObject> writer;

  public WriterProcess() {
  }

  public WriterProcess(final Channel<DataObject> in,
    final Writer<DataObject> writer) {
    super(in);
    this.writer = writer;
  }

  public WriterProcess(final Writer<DataObject> writer) {
    this.writer = writer;
  }

  public WriterProcess(final Writer<DataObject> writer, final int inBufferSize) {
    super(inBufferSize);
    this.writer = writer;
  }

  /**
   * @return the writer
   */
  public Writer<DataObject> getWriter() {
    return writer;
  }

  @Override
  protected void postRun(final Channel<DataObject> in) {
    writer.close();
  }

  @Override
  protected void process(final Channel<DataObject> in, final DataObject object) {
    writer.write(object);
  }

  public void setWriter(final Writer<DataObject> writer) {
    this.writer = writer;
  }

}
