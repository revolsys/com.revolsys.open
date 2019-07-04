package com.revolsys.core.test.geometry.cs;

import org.jeometry.coordinatesystem.model.CompoundCoordinateSystem;
import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;
import org.jeometry.coordinatesystem.model.systems.EpsgId;
import org.junit.Assert;
import org.junit.Test;

public class CompoundCoorindateSystemTest {

  @Test
  public void testStandard() {
    final CompoundCoordinateSystem compoundCoordinateSystem = EpsgCoordinateSystems
      .getCompound(EpsgId.NAD83, 5703);
    Assert.assertEquals(5498, compoundCoordinateSystem.getCoordinateSystemId());
  }
}
