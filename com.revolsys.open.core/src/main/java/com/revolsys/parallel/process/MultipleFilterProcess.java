package com.revolsys.parallel.process;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.filter.Filter;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;

public class MultipleFilterProcess<T> extends BaseInOutProcess<T, T> {
  /** The map of filters to channels. */
  private final Map<Filter<T>, Channel<T>> filters = new LinkedHashMap<Filter<T>, Channel<T>>();

  /**
   * Add the filter with the channel to write the data object to if the filter
   * matches.
   *
   * @param filter The filter.
   * @param channel The channel.
   */
  private void addFiler(final Filter<T> filter, final Channel<T> channel) {
    this.filters.put(filter, channel);
    if (channel != null) {
      channel.writeConnect();
    }
  }

  @Override
  protected void destroy() {
    for (final Channel<T> channel : this.filters.values()) {
      if (channel != null) {
        channel.writeDisconnect();
      }
    }
  }

  /**
   * @return the filters
   */
  public Map<Filter<T>, Channel<T>> getFilters() {
    return this.filters;
  }

  @Override
  protected void preRun(final Channel<T> in, final Channel<T> out) {
  }

  @Override
  protected void process(final Channel<T> in, final Channel<T> out, final T object) {
    for (final Entry<Filter<T>, Channel<T>> entry : this.filters.entrySet()) {
      final Filter<T> filter = entry.getKey();
      final Channel<T> filterOut = entry.getValue();
      if (processFilter(object, filter, filterOut)) {
        return;
      }
    }
    if (out != null) {
      out.write(object);
    }
  }

  protected boolean processFilter(final T object, final Filter<T> filter, final Channel<T> filterOut) {
    if (filter.accept(object)) {
      if (filterOut != null) {
        try {
          filterOut.write(object);
        } catch (final ClosedException e) {
        }
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * @param filters the filters to set
   */
  public void setFilters(final Map<Filter<T>, Channel<T>> filters) {
    for (final Entry<Filter<T>, Channel<T>> filterEntry : filters.entrySet()) {
      final Filter<T> filter = filterEntry.getKey();
      final Channel<T> channel = filterEntry.getValue();
      addFiler(filter, channel);
    }
  }

}
