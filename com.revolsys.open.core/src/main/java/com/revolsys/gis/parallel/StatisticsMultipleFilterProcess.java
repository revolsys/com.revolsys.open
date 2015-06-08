package com.revolsys.gis.parallel;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.data.record.Record;
import com.revolsys.filter.Filter;
import com.revolsys.gis.io.Statistics;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.MultipleFilterProcess;

public class StatisticsMultipleFilterProcess extends MultipleFilterProcess<Record> {

  private final Map<Filter<Record>, Statistics> statisticsMap = new HashMap<Filter<Record>, Statistics>();

  private String statisticsName;

  private boolean useStatistics;

  @Override
  protected void destroy() {
    super.destroy();
    for (final Statistics stats : this.statisticsMap.values()) {
      stats.disconnect();
    }
  }

  /**
   * @return the statisticsName
   */
  public String getStatisticsName() {
    return this.statisticsName;
  }

  /**
   * @return the useStatistics
   */
  public boolean isUseStatistics() {
    return this.useStatistics;
  }

  @Override
  protected boolean processFilter(final Record object, final Filter<Record> filter,
    final Channel<Record> filterOut) {
    if (super.processFilter(object, filter, filterOut)) {
      if (this.useStatistics) {
        Statistics stats = this.statisticsMap.get(filter);
        String name;
        if (stats == null) {
          if (this.statisticsName != null) {
            name = this.statisticsName + " " + filter.toString();
          } else {
            name = filter.toString();
          }
          stats = new Statistics(name);
          stats.connect();
          this.statisticsMap.put(filter, stats);
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
