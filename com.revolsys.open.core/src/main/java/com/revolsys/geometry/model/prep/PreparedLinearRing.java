package com.revolsys.geometry.model.prep;

import com.revolsys.geometry.model.LinearRing;

public class PreparedLinearRing extends PreparedLineString implements LinearRing {

  private static final long serialVersionUID = 1L;

  public PreparedLinearRing(final LinearRing ring) {
    super(ring);
  }

  @Override
  public LinearRing clone() {
    return (LinearRing)super.clone();
  }

  @Override
  public LinearRing prepare() {
    return this;
  }
}
