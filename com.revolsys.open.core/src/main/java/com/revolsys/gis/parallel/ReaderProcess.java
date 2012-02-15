package com.revolsys.gis.parallel;

import org.apache.log4j.Logger;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.io.DataObjectReader;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;
import com.revolsys.parallel.process.AbstractOutProcess;

public class ReaderProcess extends AbstractOutProcess<DataObject> {
  private DataObjectReader reader;

  public ReaderProcess() {

  }

  public ReaderProcess(final Channel<DataObject> out,
    final DataObjectReader reader) {
    super(out);
    this.reader = reader;
  }

  public ReaderProcess(final DataObjectReader reader) {
    this.reader = reader;
  }

  public ReaderProcess(final DataObjectReader reader, final int bufferSize) {
    super(bufferSize);
    this.reader = reader;
  }

  /**
   * @return the reader
   */
  public DataObjectReader getReader() {
    return reader;
  }

  @Override
  protected void run(final Channel<DataObject> out) {
    final Logger log = Logger.getLogger(getClass());
    try {
      reader.open();

      log.debug("Opened");
      while (reader.hasNext()) {
        final DataObject object = reader.next();
        out.write(object);
      }

      log.debug("Closed");
    } catch (final ClosedException c) {
      throw c;
    } catch (final Throwable t) {
      log.error(t);
    } finally {
      reader.close();
    }
  }

  /**
   * @param reader the reader to set
   */
  public void setReader(final DataObjectReader reader) {
    this.reader = reader;
  }

}
