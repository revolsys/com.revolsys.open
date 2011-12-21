package com.revolsys.comparator;

import java.util.Comparator;

import com.revolsys.util.MathUtil;

public class VersionComparator implements Comparator<String> {

  public int compare(String version1, String version2) {
    double[] parts1 = MathUtil.toDoubleArraySplit(version1, "\\.");
    double[] parts2 = MathUtil.toDoubleArraySplit(version2, "\\.");
    for (int i = 0; i < Math.max(parts1.length, parts2.length); i++) {
      double v1 = 0;
      if (i < parts1.length) {
        v1 = parts1[i];
      }
      double v2 = 0;
      if (i < parts2.length) {
        v2 = parts2[i];
      }
      int partCompare = Double.compare(v1, v2);
      if (partCompare != 0) {
        return partCompare;
      }
    }
    return 0;
  }

}
