package com.revolsys.gis.parallel;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.Writer;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.AbstractInProcess;

public class WriterProcess extends AbstractInProcess<DataObject> {
  private Writer<DataObject> writer;

  public WriterProcess() {
  }

  public WriterProcess(
    final Channel<DataObject> in,
    final Writer<DataObject> writer) {
    super(in);
    this.writer = writer;
  }

  public WriterProcess(
    final Writer<DataObject> writer) {
    this.writer = writer;
  }

  public WriterProcess(
    final Writer<DataObject> writer,
    final int inBufferSize) {
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
  protected void run(
    final Channel<DataObject> in) {
    try {
      for (DataObject object = in.read(); object != null; object = in.read()) {
        writer.write(object);
      }
    } finally {
      writer.close();
    }
  }

  public void setWriter(
    final Writer<DataObject> writer) {
    this.writer = writer;
  }

}
