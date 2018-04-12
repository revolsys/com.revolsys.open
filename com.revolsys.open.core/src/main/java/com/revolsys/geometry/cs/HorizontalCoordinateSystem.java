package com.revolsys.geometry.cs;

import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;

public interface HorizontalCoordinateSystem
  extends CoordinateSystem, HorizontalCoordinateSystemProxy {

  @SuppressWarnings("unchecked")
  default <C extends CoordinateSystem> C getCompound(
    final VerticalCoordinateSystem verticalCoordinateSystem) {
    if (verticalCoordinateSystem == null) {
      return (C)this;
    } else {
      final CompoundCoordinateSystem compoundCoordinateSystem = new CompoundCoordinateSystem(this,
        verticalCoordinateSystem);
      return (C)EpsgCoordinateSystems.getCoordinateSystem(compoundCoordinateSystem);
    }
  }
}
