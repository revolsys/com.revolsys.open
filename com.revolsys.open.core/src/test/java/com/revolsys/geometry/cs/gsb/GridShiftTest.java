package com.revolsys.geometry.cs.gsb;

import java.util.Arrays;

import org.junit.Test;

public class GridShiftTest {

  @Test
  public void testGridShift() {
    final BindaryGridShiftFile file = new BindaryGridShiftFile(
      "ftp://ftp.gdbc.gov.bc.ca/sections/outgoing/gsr/NTv2.0/BC_27_05.GSB", false);

    final double[] coordinates = {
      -123, 49
    };
    System.out.println(Arrays.toString(coordinates));
    file.getForwardOperation().perform(2, coordinates, 2, coordinates);
    System.out.println(Arrays.toString(coordinates));
    file.getReverseOperation().perform(2, coordinates, 2, coordinates);
    System.out.println(Arrays.toString(coordinates));
  }
}
