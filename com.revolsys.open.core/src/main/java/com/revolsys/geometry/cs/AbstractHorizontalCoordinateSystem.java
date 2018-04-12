package com.revolsys.geometry.cs;

import java.util.List;

public abstract class AbstractHorizontalCoordinateSystem extends AbstractCoordinateSystem
  implements HorizontalCoordinateSystem {
  private static final long serialVersionUID = 1L;

  public AbstractHorizontalCoordinateSystem(final int id, final String name, final List<Axis> axis,
    final Area area, final boolean deprecated) {
    super(id, name, axis, area, deprecated);
  }

  public AbstractHorizontalCoordinateSystem(final int id, final String name, final List<Axis> axis,
    final Area area, final boolean deprecated, final Authority authority) {
    super(id, name, axis, area, deprecated, authority);
  }

  public AbstractHorizontalCoordinateSystem(final int id, final String name, final List<Axis> axis,
    final Authority authority) {
    super(id, name, axis, authority);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <C extends CoordinateSystem> C getHorizontalCoordinateSystem() {
    return (C)this;
  }

  @Override
  public int getHorizontalCoordinateSystemId() {
    return getCoordinateSystemId();
  }
}
