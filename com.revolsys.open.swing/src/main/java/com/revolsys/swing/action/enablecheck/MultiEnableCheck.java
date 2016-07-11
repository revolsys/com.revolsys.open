package com.revolsys.swing.action.enablecheck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class MultiEnableCheck extends AbstractEnableCheck
  implements Iterable<EnableCheck> {

  private List<EnableCheck> enableChecks = new ArrayList<>();

  public MultiEnableCheck() {
  }

  public MultiEnableCheck(final Collection<? extends EnableCheck> enableChecks) {
    setEnableChecks(enableChecks);
  }

  public MultiEnableCheck(final EnableCheck... enableChecks) {
    this(Arrays.asList(enableChecks));
  }

  public List<EnableCheck> getEnableChecks() {
    return this.enableChecks;
  }

  @Override
  public Iterator<EnableCheck> iterator() {
    return this.enableChecks.iterator();
  }

  public void setEnableChecks(final Collection<? extends EnableCheck> enableChecks) {
    this.enableChecks = new ArrayList<>(enableChecks);
    isEnabled();
  }

  @Override
  public String toString() {
    return this.enableChecks.toString();
  }
}
