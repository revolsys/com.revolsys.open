package com.revolsys.gis.esri.gdb.file;

import java.nio.file.Path;

import com.revolsys.gis.parallel.WriterProcess;
import com.revolsys.io.Writer;
import com.revolsys.io.file.Paths;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.record.Record;
import com.revolsys.util.count.LabelCountWriter;
import com.revolsys.util.count.LabelCounters;

public class FileGdbWriterProcess extends WriterProcess {

  private final Path path;

  private LabelCounters counts;

  private final FileGdbRecordStore recordStore;

  public FileGdbWriterProcess(final Path path) {
    this.path = path;
    if (!Paths.deleteDirectories(this.path)) {
      throw new RuntimeException("Unable to delete: " + this.path);
    }
    this.recordStore = FileGdbRecordStoreFactory.newRecordStoreInitialized(path);
  }

  @Override
  public void close() {
    this.recordStore.close();
  }

  public FileGdbRecordStore getRecordStore() {
    return this.recordStore;
  }

  @Override
  protected void run(final Channel<Record> in) {
    try (
      Writer<Record> fgdbWriter = this.recordStore.newRecordWriter()) {
      Writer<Record> writer;
      if (this.counts == null) {
        writer = fgdbWriter;
      } else {
        writer = new LabelCountWriter(fgdbWriter) //
          .setCounts(this.counts);
      }
      setWriter(writer);
      super.run(in);
    }
  }

  public FileGdbWriterProcess setCounts(final LabelCounters counts) {
    this.counts = counts;
    return this;
  }

  @Override
  public String toString() {
    return this.path.toString();
  }
}
