package com.revolsys.geometry.cs;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;

public class CompoundCoorindateSystemTest {

  @Test
  public void testStandard() {
    final CompoundCoordinateSystem compoundCoordinateSystem = EpsgCoordinateSystems
      .getCompound(4269, 5703);
    Assert.assertEquals(5498, compoundCoordinateSystem.getCoordinateSystemId());
  }
}
