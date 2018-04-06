package com.revolsys.geometry.cs.gsb;

import org.junit.Test;

import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;

public class GridShiftTest {

  @Test
  public void testGridShift() {
    final BinaryGridShiftFile file = new BinaryGridShiftFile(
      "ftp://ftp.gdbc.gov.bc.ca/sections/outgoing/gsr/NTv2.0/BC_27_05.GSB", false);

    final CoordinatesOperationPoint point = new CoordinatesOperationPoint(-123, 49);
    System.out.println(point);
    file.getForwardOperation().perform(point);
    System.out.println(point);
    file.getReverseOperation().perform(point);
    System.out.println(point);
  }
}
