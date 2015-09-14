package com.revolsys.gis.parallel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import com.revolsys.gis.io.Statistics;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.MultiplePredicateProcess;
import com.revolsys.record.Record;

public class StatisticsMultipleFilterProcess extends MultiplePredicateProcess<Record> {

  private final Map<Predicate<Record>, Statistics> statisticsMap = new HashMap<Predicate<Record>, Statistics>();

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
  protected boolean processPredicate(final Record object, final Predicate<Record> filter,
    final Channel<Record> filterOut) {
    if (super.processPredicate(object, filter, filterOut)) {
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
