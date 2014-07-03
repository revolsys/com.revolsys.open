package com.revolsys.gis.parallel;

import org.apache.log4j.Logger;

import com.revolsys.data.record.Record;
import com.revolsys.gis.io.DataObjectIterator;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;
import com.revolsys.parallel.process.AbstractOutProcess;

public class ReaderProcess extends AbstractOutProcess<Record> {
  private DataObjectIterator reader;

  public ReaderProcess() {

  }

  public ReaderProcess(final Channel<Record> out,
    final DataObjectIterator reader) {
    super(out);
    this.reader = reader;
  }

  public ReaderProcess(final DataObjectIterator reader) {
    this.reader = reader;
  }

  public ReaderProcess(final DataObjectIterator reader, final int bufferSize) {
    super(bufferSize);
    this.reader = reader;
  }

  /**
   * @return the reader
   */
  public DataObjectIterator getReader() {
    return reader;
  }

  @Override
  protected void run(final Channel<Record> out) {
    final Logger log = Logger.getLogger(getClass());
    try {
      reader.open();

      log.debug("Opened");
      while (reader.hasNext()) {
        final Record object = reader.next();
        out.write(object);
      }

      log.debug("Closed");
    } catch (final ClosedException c) {
      throw c;
    } catch (final Throwable t) {
      log.error("Error reading", t);
    } finally {
      reader.close();
    }
  }

  /**
   * @param reader the reader to set
   */
  public void setReader(final DataObjectIterator reader) {
    this.reader = reader;
  }

}
