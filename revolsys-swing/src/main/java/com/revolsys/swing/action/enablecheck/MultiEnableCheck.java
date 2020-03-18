package com.revolsys.swing.action.enablecheck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.util.Property;

public abstract class MultiEnableCheck extends AbstractEnableCheck
  implements Iterable<EnableCheck> {

  private final List<EnableCheck> enableChecks = new ArrayList<>();

  public MultiEnableCheck() {
  }

  public MultiEnableCheck(final Collection<? extends EnableCheck> enableChecks) {
    setEnableChecks(enableChecks);
  }

  public MultiEnableCheck(final EnableCheck... enableChecks) {
    this(Arrays.asList(enableChecks));
  }

  public void addEnableCheck(final EnableCheck enableCheck) {
    addEnableCheckInternal(enableCheck);
    isEnabled();
  }

  protected void addEnableCheckInternal(final EnableCheck enableCheck) {
    this.enableChecks.add(enableCheck);
    Property.addListener(enableCheck, this);
  }

  public List<EnableCheck> getEnableChecks() {
    return this.enableChecks;
  }

  @Override
  public Iterator<EnableCheck> iterator() {
    return this.enableChecks.iterator();
  }

  public void setEnableChecks(final Collection<? extends EnableCheck> enableChecks) {
    for (final EnableCheck enableCheck : this.enableChecks) {
      Property.removeListener(enableCheck, this);
    }
    this.enableChecks.clear();
    for (final EnableCheck enableCheck : enableChecks) {
      addEnableCheckInternal(enableCheck);
    }
    isEnabled();
  }

  @Override
  public String toString() {
    return this.enableChecks.toString();
  }
}
