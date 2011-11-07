package com.revolsys.logging.log4j;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class NdcFilter extends Filter {
  private boolean acceptOnMatch = true;

  private String ndc;

  @Override
  public int decide(
    final LoggingEvent logEvent) {
    final String logNdc = logEvent.getNDC();
    if ((ndc == logNdc || (ndc != null && ndc.equals(logNdc))) && acceptOnMatch) {
      return Filter.ACCEPT;
    } else {
      return Filter.DENY;
    }
  }

  /**
   * @return the ndc
   */
  public String getNdc() {
    return ndc;
  }

  /**
   * @return the acceptOnMatch
   */
  public boolean isAcceptOnMatch() {
    return acceptOnMatch;
  }

  /**
   * @param acceptOnMatch the acceptOnMatch to set
   */
  public void setAcceptOnMatch(
    final boolean acceptOnMatch) {
    this.acceptOnMatch = acceptOnMatch;
  }

  /**
   * @param ndc the ndc to set
   */
  public void setNdc(
    final String ndc) {
    this.ndc = ndc;
  }

}
