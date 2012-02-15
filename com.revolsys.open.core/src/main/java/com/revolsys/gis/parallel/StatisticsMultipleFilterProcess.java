package com.revolsys.gis.parallel;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.io.Statistics;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.MultipleFilterProcess;

public class StatisticsMultipleFilterProcess extends
  MultipleFilterProcess<DataObject> {

  private final Map<Filter<DataObject>, Statistics> statisticsMap = new HashMap<Filter<DataObject>, Statistics>();

  private String statisticsName;

  private boolean useStatistics;

  @Override
  protected void destroy() {
    super.destroy();
    for (final Statistics stats : statisticsMap.values()) {
      stats.disconnect();
    }
  }

  /**
   * @return the statisticsName
   */
  public String getStatisticsName() {
    return statisticsName;
  }

  /**
   * @return the useStatistics
   */
  public boolean isUseStatistics() {
    return useStatistics;
  }

  @Override
  protected boolean processFilter(
    final DataObject object,
    final Filter<DataObject> filter,
    final Channel<DataObject> filterOut) {
    if (super.processFilter(object, filter, filterOut)) {
      if (useStatistics) {
        Statistics stats = statisticsMap.get(filter);
        String name;
        if (stats == null) {
          if (statisticsName != null) {
            name = statisticsName + " " + filter.toString();
          } else {
            name = filter.toString();
          }
          stats = new Statistics(name);
          stats.connect();
          statisticsMap.put(filter, stats);
        }
        stats.add(object);
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * @param statisticsName the statisticsName to set
   */
  public void setStatisticsName(final String statisticsName) {
    this.statisticsName = statisticsName;
  }

  /**
   * @param useStatistics the useStatistics to set
   */
  public void setUseStatistics(final boolean useStatistics) {
    this.useStatistics = useStatistics;
  }

}
