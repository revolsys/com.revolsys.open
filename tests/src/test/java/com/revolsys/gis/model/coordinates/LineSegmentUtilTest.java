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
      Coordinates line1C1 = getCoordinates(testCase, "line1", "1");
      Coordinates line1C2 = getCoordinates(testCase, "line1", "2");
      Coordinates line2C1 = getCoordinates(testCase, "line2", "1");
      Coordinates line2C2 = getCoordinates(testCase, "line2", "2");
      final CoordinatesPrecisionModel precisionModel = new SimpleCoordinatesPrecisionModel(
        1, 1);
      List<Coordinates> expectedResult = getResults(testCase);
      List<Coordinates> reverseExpectedResult = new ArrayList<Coordinates>(expectedResult);
      Collections.reverse(reverseExpectedResult);
      
      // Start -> Start
      List<Coordinates> result1 = LineSegmentUtil.intersection(precisionModel,
        line1C1, line1C2, line2C1, line2C2);
      Assert.assertEquals(name + " start -> start", expectedResult, result1);
      
      // Start -> End
      List<Coordinates> result2 = LineSegmentUtil.intersection(precisionModel,
        line1C1, line1C2, line2C2, line2C1);
      Assert.assertEquals(name + " start -> end", expectedResult, result2);
      
      // End -> Start
      List<Coordinates> result3 = LineSegmentUtil.intersection(precisionModel,
        line1C2, line1C1, line2C1, line2C2);
      Assert.assertEquals(name + " end -> start", reverseExpectedResult, result3);
      
      // End -> End
      List<Coordinates> result4 = LineSegmentUtil.intersection(precisionModel,
        line1C2, line1C1, line2C2, line2C1);
      Assert.assertEquals(name + " end -> end", reverseExpectedResult, result4);
 
    }
  }

  private List<Coordinates> getResults(
    Map<String, Object> testCase) {
    List<Coordinates> results = new ArrayList<Coordinates>();
    Coordinates resultC1 = getCoordinates(testCase, "result", "1");
    if (resultC1 != null) {
      results.add(resultC1);
    }
    Coordinates resultC2 = getCoordinates(testCase, "result", "2");
    if (resultC2 != null) {
      results.add(resultC2);
    }
    return results;
  }

  private Coordinates getCoordinates(
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
