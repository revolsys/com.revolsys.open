package com.revolsys.jts.test.geometry;

import junit.framework.Assert;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryReaderFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.Reader;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;

public class TestUtil {

  public static void doTestGeometry(final Class<?> clazz, final String file) {
    boolean valid = true;
    final Resource resource = new ClassPathResource(file, clazz);
    try (
      Reader<DataObject> reader = AbstractDataObjectAndGeometryReaderFactory.dataObjectReader(resource)) {
      int i = 0;
      for (final DataObject object : reader) {
        final int srid = object.getInteger("srid");
        final int numAxis = object.getInteger("numAxis");
        final double scaleXy = object.getInteger("scaleXy");
        final double scaleZ = object.getInteger("scaleZ");
        final GeometryFactory geometryFactory = GeometryFactory.getFactory(
          srid, numAxis, scaleXy, scaleZ);
        final String wkt = object.getValue("wkt");
        final Geometry geometry = geometryFactory.createGeometry(wkt);
        valid &= equalsExpectedWkt(i, object, geometry);
        i++;
      }
    }
    if (!valid) {
      Assert.fail("Has Errors");
    }
  }

  public static boolean equalsExpectedWkt(final int i, final DataObject object,
    final Geometry actualGeometry) {
    final GeometryFactory geometryFactory = GeometryFactory.getFactory();
    final String wkt = object.getValue("expectedWkt");
    final Geometry expectedGeometry = geometryFactory.createGeometry(wkt, true);
    final int actualSrid = actualGeometry.getSrid();
    final int expectedSrid = expectedGeometry.getSrid();
    if (actualSrid != expectedSrid) {
      System.err.println(i + "\tEquals Srid\t" + expectedSrid + "\t"
        + actualSrid);
      return false;
    } else if (actualGeometry.equalsExact(expectedGeometry)) {
      return true;
    } else {
      System.err.println(i + "\tEquals Exact\t" + expectedGeometry + "\t"
        + actualGeometry);
      return false;
    }
  }

}
