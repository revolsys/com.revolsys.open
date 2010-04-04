package com.revolsys.gis.parallel;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.AbstractInOutProcess;

public final class Delta extends AbstractInOutProcess<DataObject> {

  /** The second output Channel */
  private Channel<DataObject> out2;

  public Delta() {
  }

  /**
   * Construct a new Delta process with the input Channel in and the output
   * Channels out1 and out2. The ordering of the Channels out1 and out2 make no
   * difference to the functionality of this process.
   * 
   * @param in The input channel
   * @param out1 The first output Channel
   * @param out2 The second output Channel
   */
  public Delta(
    final Channel<DataObject> in,
    final Channel<DataObject> out,
    final Channel<DataObject> out2) {
    super(in, out);
    this.out2 = out2;
  }

  /**
   * @return the out
   */
  public Channel<DataObject> getOut2() {
    if (out2 == null) {
      setOut2(new Channel<DataObject>());
    }
    return out2;
  }

  @Override
  protected void run(
    final Channel<DataObject> in,
    final Channel<DataObject> out) {
    try {
      while (true) {
        final DataObject feature = in.read();
        // TODO should write in parallel
        out.write(feature);
        out2.write(feature);
      }
    } finally {
      if (out2 != null) {
        out2.writeDisconnect();
      }
    }

  }

  /**
   * @param out the out to set
   */
  public void setOut2(
    final Channel<DataObject> out2) {
    this.out2 = out2;
    out2.writeConnect();

  }

}
