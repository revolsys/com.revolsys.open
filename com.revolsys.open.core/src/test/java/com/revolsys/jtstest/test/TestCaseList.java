
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.jtstest.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @version 1.7
 */
public class TestCaseList {
  ArrayList tests = new ArrayList();

  int countError = 0;

  int countTotal = 0;

  public TestCaseList() {
  }

  public void add(final Testable tc) {
    tests.add(tc);
  }

  public void add(final TestCaseList tcl) {
    for (final Iterator i = tcl.tests.iterator(); i.hasNext();) {
      tests.add(i.next());
    }
  }

  public List getList() {
    return tests;
  }

  public void run() {
    for (int i = 0; i < tests.size(); i++) {
      boolean failed = true;
      String failedMsg = "";
      final Testable tc = (Testable)tests.get(i);
      String resultStr = "Passed";
      countTotal++;
      try {
        tc.runTest();
        failed = tc.isFailed();
        failedMsg = tc.getFailedMsg();
      } catch (final Exception e) {
        failedMsg = e.getMessage();
        System.out.println(e.getMessage());
        e.printStackTrace();
        failed = true;
      }
      if (failed) {
        countError++;
        resultStr = "FAILED! - " + failedMsg;
      }
      System.out.println(countTotal + ". " + tc.getName() + " - " + resultStr);
    }
    String errReport = "  All tests passed";
    if (countError > 0) {
      errReport = "  Failed: " + countError;
    }
    System.out.print("\nTotal tests: " + countTotal + errReport);
  }
}
