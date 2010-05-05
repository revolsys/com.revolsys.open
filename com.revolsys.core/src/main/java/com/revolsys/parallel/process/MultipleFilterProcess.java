package com.revolsys.parallel.process;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.filter.Filter;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;

public class MultipleFilterProcess<T> extends BaseInOutProcess<T> {
  /** The map of filters to channels. */
  private Map<Filter<T>, Channel<T>> filters = new LinkedHashMap<Filter<T>, Channel<T>>();

  protected void process(
    Channel<T> in,
    Channel<T> out,
    T object) {
    for (Entry<Filter<T>, Channel<T>> entry : filters.entrySet()) {
      Filter<T> filter = entry.getKey();
      Channel<T> filterOut = entry.getValue();
      if (processFilter(object, filter, filterOut)) {
        return;
      }
    }
    if (out != null) {
      out.write(object);
    }
  }

  @Override
  protected void destroy() {
    for (Iterator<Channel<T>> channels = filters.values().iterator(); channels.hasNext();) {
      Channel<T> channel = channels.next();
      if (channel != null) {
        channel.writeDisconnect();
      }
    }
  }

  protected boolean processFilter(
    final T object,
    final Filter<T> filter,
    final Channel<T> filterOut) {
    if (filter.accept(object)) {
      if (filterOut != null) {
        try {
          filterOut.write(object);
        } catch (ClosedException e) {
        }
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * Add the filter with the channel to write the data object to if the filter
   * matches.
   * 
   * @param filter The filter.
   * @param channel The channel.
   */
  private void addFiler(
    final Filter<T> filter,
    final Channel<T> channel) {
    filters.put(filter, channel);
    if (channel != null) {
      channel.writeConnect();
    }
  }

  /**
   * @return the filters
   */
  public Map<Filter<T>, Channel<T>> getFilters() {
    return filters;
  }

  /**
   * @param filters the filters to set
   */
  public void setFilters(
    Map<Filter<T>, Channel<T>> filters) {
    for (Entry<Filter<T>, Channel<T>> filterEntry : filters.entrySet()) {
      Filter<T> filter = filterEntry.getKey();
      Channel<T> channel = filterEntry.getValue();
      addFiler(filter, channel);
    }
  }

}
