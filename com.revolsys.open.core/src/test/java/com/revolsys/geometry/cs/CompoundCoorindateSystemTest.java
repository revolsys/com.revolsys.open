package com.revolsys.geometry.cs;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.cs.epsg.EpsgId;

public class CompoundCoorindateSystemTest {

  @Test
  public void testStandard() {
    final CompoundCoordinateSystem compoundCoordinateSystem = EpsgCoordinateSystems
      .getCompound(EpsgId.NAD83, 5703);
    Assert.assertEquals(5498, compoundCoordinateSystem.getHorizontalCoordinateSystemId());
  }
}
