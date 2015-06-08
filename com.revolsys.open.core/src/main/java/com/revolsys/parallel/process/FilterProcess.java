package com.revolsys.parallel.process;

import com.revolsys.filter.Filter;
import com.revolsys.parallel.channel.Channel;

public class FilterProcess<T> extends BaseInOutProcess<T, T> {
  private Filter<T> filter;

  private boolean invert = false;

  public Filter<T> getFilter() {
    return this.filter;
  }

  public boolean isInvert() {
    return this.invert;
  }

  protected void postAccept(final T object) {
  }

  protected void postReject(final T object) {
  }

  @Override
  protected void process(final Channel<T> in, final Channel<T> out, final T object) {
    boolean accept = this.filter.accept(object);
    if (this.invert) {
      accept = !accept;
    }
    if (accept) {
      out.write(object);
      postAccept(object);
    } else {
      postReject(object);
    }
  }

  public void setFilter(final Filter<T> filter) {
    this.filter = filter;
  }

  public void setInvert(final boolean invert) {
    this.invert = invert;
  }

}
