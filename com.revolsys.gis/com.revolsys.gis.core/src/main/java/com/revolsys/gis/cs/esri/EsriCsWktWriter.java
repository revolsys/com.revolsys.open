package com.revolsys.gis.cs.esri;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Map.Entry;

import com.revolsys.gis.cs.AngularUnit;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.Datum;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.LinearUnit;
import com.revolsys.gis.cs.PrimeMeridian;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.Projection;
import com.revolsys.gis.cs.Spheroid;

public class EsriCsWktWriter {
  public static void write(
    final PrintWriter out,
    final AngularUnit unit) {
    out.print(",UNIT[");
    write(out, unit.getName());
    out.write(',');
    out.print(unit.getConversionFactor());
    out.write(']');
  }

  public static void write(
    final PrintWriter out,
    final CoordinateSystem coordinateSystem) {
    if (coordinateSystem instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projCs = (ProjectedCoordinateSystem)coordinateSystem;
      write(out, projCs);
    } else if (coordinateSystem instanceof GeographicCoordinateSystem) {
      final GeographicCoordinateSystem geoCs = (GeographicCoordinateSystem)coordinateSystem;
      write(out, geoCs);
    }
  }

  public static void write(
    final PrintWriter out,
    final Datum datum) {
    out.print("DATUM[");
    write(out, datum.getName());
    final Spheroid spheroid = datum.getSpheroid();
    if (spheroid != null) {
      out.print(",");
      write(out, spheroid);
    }
    out.write(']');
  }

  public static void write(
    final PrintWriter out,
    final GeographicCoordinateSystem coordinateSystem) {
    out.print("GEOGCS[");
    write(out, coordinateSystem.getName());
    final Datum datum = coordinateSystem.getDatum();
    if (datum != null) {
      out.print(",");
      write(out, datum);
    }
    final PrimeMeridian primeMeridian = coordinateSystem.getPrimeMeridian();
    if (primeMeridian != null) {
      out.print(",");
      write(out, primeMeridian);
    }
    final AngularUnit unit = coordinateSystem.getAngularUnit();
    if (unit != null) {
      write(out, unit);
    }
    out.write(']');
  }

  public static void write(
    final PrintWriter out,
    final LinearUnit unit) {
    out.print(",UNIT[");
    write(out, unit.getName());
    out.write(',');
    write(out, unit.getConversionFactor());
    out.write(']');
  }

  private static void write(
    final PrintWriter out,
    final Number number) {
    out.print(new DecimalFormat("#0.################").format(number));

  }

  public static void write(
    final PrintWriter out,
    final PrimeMeridian primeMeridian) {
    out.print("PRIMEM[");
    write(out, primeMeridian.getName());
    out.write(',');
    final double longitude = primeMeridian.getLongitude();
    write(out, longitude);
    out.write(']');
  }

  public static void write(
    final PrintWriter out,
    final ProjectedCoordinateSystem coordinateSystem) {
    out.print("PROJCS[");
    write(out, coordinateSystem.getName());
    final GeographicCoordinateSystem geoCs = coordinateSystem.getGeographicCoordinateSystem();
    if (geoCs != null) {
      out.print(",");
      write(out, geoCs);
    }
    final Projection projection = coordinateSystem.getProjection();
    if (projection != null) {
      out.print(",");
      write(out, projection);
    }
    for (final Entry<String, Object> parameter : coordinateSystem.getParameters()
      .entrySet()) {
      final String name = parameter.getKey();
      final Object value = parameter.getValue();
      write(out, name, value);
    }
    final LinearUnit unit = coordinateSystem.getLinearUnit();
    if (unit != null) {
      write(out, unit);
    }
    out.write(']');
  }

  public static void write(
    final PrintWriter out,
    final Projection projection) {
    out.print("PROJECTION[");
    write(out, projection.getName());
    out.write(']');
  }

  public static void write(
    final PrintWriter out,
    final Spheroid spheroid) {
    out.print("SPHEROID[");
    write(out, spheroid.getName());
    out.write(',');
    final double semiMajorAxis = spheroid.getSemiMajorAxis();
    write(out, semiMajorAxis);
    out.print(',');
    final double inverseFlattening = spheroid.getInverseFlattening();
    write(out, inverseFlattening);
    out.write(']');
  }

  public static void write(
    final PrintWriter out,
    final String value) {
    out.write('"');
    if (value != null) {
      out.print(value);
    }
    out.write('"');
  }

  public static void write(
    final PrintWriter out,
    final String name,
    final Object value) {
    out.print(",PARAMETER[");
    write(out, name);
    out.write(',');
    if (value instanceof Number) {
      final Number number = (Number)value;
      write(out, number);
    } else {
      out.print(value);
    }
    out.write(']');
  }
}
