package com.revolsys.core.test.geometry.cs.esri;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.cs.CoordinateOperationMethod;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.ParameterName;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.VerticalCoordinateSystem;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.cs.esri.EsriCoordinateSystems;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.util.Property;

public class EsriCoordinateSystemsTest {

  private void doFileTest(final String type) {
    try (
      RecordReader reader = RecordReader.newRecordReader(
        "../com.revolsys.open.coordinatesystems/src/main/data/esri/esri" + type + "Cs.tsv");) {
      for (final Record record : reader) {
        final int id = record.getInteger("ID");
        final String wkt = record.getString("WKT");
        final GeometryFactory geometryFactory = GeometryFactory.floating2d(wkt);
        final int coordinateSystemId = geometryFactory.getHorizontalCoordinateSystemId();
        final String coordinateSystemName = geometryFactory.getCoordinateSystemName();
        Assert.assertEquals(coordinateSystemName + " ID", id, coordinateSystemId);
        Assert.assertEquals(coordinateSystemName + " axisCount", 2, geometryFactory.getAxisCount());
        final String actualWkt = geometryFactory.toWktCs();
        Assert.assertEquals(coordinateSystemName + " WKT", wkt, actualWkt);
      }
    }
  }

  @Test
  public void geographicCoordinateSystems() {
    doFileTest("Geographic");
  }

  @Test
  public void projectedCoordinateSystems() {
    doFileTest("Projected");
  }

  private void validateEsri() {
    EsriCoordinateSystems.getCoordinateSystem(0);
    for (final CoordinateSystem coordinateSytstem : EpsgCoordinateSystems.getCoordinateSystems()) {
      if (coordinateSytstem instanceof GeographicCoordinateSystem) {
        final GeographicCoordinateSystem geoCs = (GeographicCoordinateSystem)coordinateSytstem;
        final int id = coordinateSytstem.getHorizontalCoordinateSystemId();
        final GeographicCoordinateSystem esri = EsriCoordinateSystems.getCoordinateSystem(id);
        if (esri != null && !geoCs.equalsExact(esri)) {
          // System.out.println(id + coordinateSytstem.getCoordinateSystemName());
        }
      } else if (coordinateSytstem instanceof ProjectedCoordinateSystem) {
        final ProjectedCoordinateSystem projectedCs = (ProjectedCoordinateSystem)coordinateSytstem;
        final int id = coordinateSytstem.getHorizontalCoordinateSystemId();
        final String wkt = new UrlResource(
          "http://spatialreference.org/ref/epsg/" + id + "/esriwkt/").contentsAsString();
        final ProjectedCoordinateSystem esri = GeometryFactory.floating2d(wkt)
          .getHorizontalCoordinateSystem();
        final CoordinateOperationMethod coordinateOperationMethod = esri
          .getCoordinateOperationMethod();
        if (esri != null && !projectedCs.equals(esri) && coordinateOperationMethod != null
          && Property.hasValue(coordinateOperationMethod.getName())
          && !projectedCs.isDeprecated()) {
          final Map<ParameterName, Object> p1 = projectedCs.getParameters();
          final Map<ParameterName, Object> p2 = esri.getParameters();
          final Set<ParameterName> n1 = p1.keySet();
          final Set<ParameterName> n2 = p2.keySet();
          if (!n1.equals(n2)) {

            final TreeSet<ParameterName> nm1 = new TreeSet<>(n1);
            nm1.removeAll(n2);
            final TreeSet<ParameterName> nm2 = new TreeSet<>(n2);
            nm2.removeAll(n1);
            final String m = id + "\t" + coordinateSytstem.getCoordinateSystemName() + "\t" + nm1
              + "\t" + nm2;
            // System.out.println(m);
          }
        }
      }
    }
  }

  @Test
  public void verticalCoordinateSystems() {
    try (
      RecordReader reader = RecordReader.newRecordReader(
        "../com.revolsys.open.coordinatesystems/src/main/data/esri/esriVerticalCs.tsv");) {
      for (final Record record : reader) {
        final int id = record.getInteger("ID");
        final String wkt = record.getString("WKT");
        if (wkt.contains("VDATUM")) {
          final VerticalCoordinateSystem coordinateSystem = CoordinateSystem
            .getCoordinateSystem(wkt);
          final int coordinateSystemId = coordinateSystem.getCoordinateSystemId();
          Assert.assertEquals(coordinateSystem.getCoordinateSystemName() + " ID", id,
            coordinateSystemId);
          final String actualWkt = coordinateSystem.toEsriWktCs();
          Assert.assertEquals(coordinateSystem.getCoordinateSystemName() + " WKT", wkt, actualWkt);
        }
      }
    }
  }
}
