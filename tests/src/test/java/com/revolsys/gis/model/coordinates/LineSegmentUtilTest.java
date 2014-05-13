package com.revolsys.gis.model.coordinates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.MapReaderFactory;
import com.revolsys.io.Reader;
import com.revolsys.spring.InputStreamResource;

public class LineSegmentUtilTest {

  @Test
  public void intersection()
    throws Throwable {
    Resource resource = new ClassPathResource(getClass().getName().replaceAll(
      "\\.", "/")
      + ".intersection.csv");
    final IoFactoryRegistry registry = IoFactoryRegistry.INSTANCE;
    final String filename = resource.getFilename();
    final String extension = FileUtil.getFileNameExtension(filename);
    final MapReaderFactory factory = registry.getFactoryByFileExtension(
      MapReaderFactory.class, extension);
    final Reader<Map<String, Object>> reader = factory.createMapReader(new InputStreamResource(filename,
      resource.getInputStream()));
    for (Map<String, Object> testCase : reader) {
      String name = (String)testCase.get("name");
      Point line1C1 = getCoordinates(testCase, "line1", "1");
      Point line1C2 = getCoordinates(testCase, "line1", "2");
      Point line2C1 = getCoordinates(testCase, "line2", "1");
      Point line2C2 = getCoordinates(testCase, "line2", "2");
      final GeometryFactory precisionModel = new SimpleGeometryFactory(
        1, 1);
      List<Point> expectedResult = getResults(testCase);
      List<Point> reverseExpectedResult = new ArrayList<Point>(expectedResult);
      Collections.reverse(reverseExpectedResult);
      
      // Start -> Start
      List<Point> result1 = LineSegmentUtil.intersection(precisionModel,
        line1C1, line1C2, line2C1, line2C2);
      Assert.assertEquals(name + " start -> start", expectedResult, result1);
      
      // Start -> End
      List<Point> result2 = LineSegmentUtil.intersection(precisionModel,
        line1C1, line1C2, line2C2, line2C1);
      Assert.assertEquals(name + " start -> end", expectedResult, result2);
      
      // End -> Start
      List<Point> result3 = LineSegmentUtil.intersection(precisionModel,
        line1C2, line1C1, line2C1, line2C2);
      Assert.assertEquals(name + " end -> start", reverseExpectedResult, result3);
      
      // End -> End
      List<Point> result4 = LineSegmentUtil.intersection(precisionModel,
        line1C2, line1C1, line2C2, line2C1);
      Assert.assertEquals(name + " end -> end", reverseExpectedResult, result4);
 
    }
  }

  private List<Point> getResults(
    Map<String, Object> testCase) {
    List<Point> results = new ArrayList<Point>();
    Point resultC1 = getCoordinates(testCase, "result", "1");
    if (resultC1 != null) {
      results.add(resultC1);
    }
    Point resultC2 = getCoordinates(testCase, "result", "2");
    if (resultC2 != null) {
      results.add(resultC2);
    }
    return results;
  }

  private Point getCoordinates(
    Map<String, Object> testCase,
    String prefix,
    String suffix) {
    double x1 = getDouble(testCase, prefix + "X" + suffix);
    if (Double.isNaN(x1)) {
      return null;
    } else {
      double y1 = getDouble(testCase, prefix + "Y" + suffix);
      return new DoubleCoordinates(x1, y1);
    }
  }

  private double getDouble(
    Map<String, Object> object,
    String name) {
    final String value = (String)object.get(name);
    if (value == null || value.equals("")) {
      return Double.NaN;
    } else {
      return Double.valueOf(value);
    }
  }
}
