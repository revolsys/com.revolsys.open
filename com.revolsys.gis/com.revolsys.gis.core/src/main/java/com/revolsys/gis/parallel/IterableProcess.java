package com.revolsys.gis.parallel;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.AbstractOutProcess;

public class IterableProcess extends AbstractOutProcess<DataObject> {
  private Iterable<DataObject> iterable;

  public IterableProcess() {

  }

  public IterableProcess(
    final Channel<DataObject> out,
    final Iterable<DataObject> iterable) {
    super(out);
    this.iterable = iterable;
  }

  public IterableProcess(
    final Iterable<DataObject> iterable) {
    this.iterable = iterable;
  }

  public IterableProcess(
    final Iterable<DataObject> iterable,
    final int bufferSize) {
    super(bufferSize);
    this.iterable = iterable;
  }

  /**
   * @return the iterable
   */
  public Iterable<DataObject> getIterable() {
    return iterable;
  }

  @Override
  protected void run(
    final Channel<DataObject> out) {
    for (final DataObject object : iterable) {
      out.write(object);
    }
  }

  /**
   * @param iterable the iterable to set
   */
  public void setIterable(
    final Iterable<DataObject> iterable) {
    this.iterable = iterable;
  }

}
