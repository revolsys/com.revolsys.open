package com.revolsys.gis.parallel;

import org.apache.log4j.Logger;

import com.revolsys.data.record.Record;
import com.revolsys.gis.io.RecordIterator;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;
import com.revolsys.parallel.process.AbstractOutProcess;

public class ReaderProcess extends AbstractOutProcess<Record> {
  private RecordIterator reader;

  public ReaderProcess() {

  }

  public ReaderProcess(final Channel<Record> out,
    final RecordIterator reader) {
    super(out);
    this.reader = reader;
  }

  public ReaderProcess(final RecordIterator reader) {
    this.reader = reader;
  }

  public ReaderProcess(final RecordIterator reader, final int bufferSize) {
    super(bufferSize);
    this.reader = reader;
  }

  /**
   * @return the reader
   */
  public RecordIterator getReader() {
    return this.reader;
  }

  @Override
  protected void run(final Channel<Record> out) {
    final Logger log = Logger.getLogger(getClass());
    try {
      this.reader.open();

      log.debug("Opened");
      while (this.reader.hasNext()) {
        final Record object = this.reader.next();
        out.write(object);
      }

      log.debug("Closed");
    } catch (final ClosedException c) {
      throw c;
    } catch (final Throwable t) {
      log.error("Error reading", t);
    } finally {
      this.reader.close();
    }
  }

  /**
   * @param reader the reader to set
   */
  public void setReader(final RecordIterator reader) {
    this.reader = reader;
  }

}
