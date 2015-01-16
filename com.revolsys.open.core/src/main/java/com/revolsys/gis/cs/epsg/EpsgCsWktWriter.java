package com.revolsys.gis.cs.epsg;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map.Entry;

import com.revolsys.gis.cs.AngularUnit;
import com.revolsys.gis.cs.Authority;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.Datum;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.LinearUnit;
import com.revolsys.gis.cs.PrimeMeridian;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.Projection;
import com.revolsys.gis.cs.Spheroid;
import com.revolsys.util.MathUtil;

public class EpsgCsWktWriter {

  public static String toWkt(final CoordinateSystem coordinateSystem) {
    final StringWriter stringWriter = new StringWriter();
    final PrintWriter out = new PrintWriter(stringWriter);
    write(out, coordinateSystem);
    return stringWriter.toString();
  }

  public static void write(final PrintWriter out, final AngularUnit unit) {
    if (unit != null) {
      out.print(",UNIT[");
      write(out, unit.getName());
      out.write(',');
      out.print(unit.getConversionFactor());
      final Authority authority = unit.getAuthority();
      write(out, authority);
      out.write(']');
    }
  }

  public static void write(final PrintWriter out, final Authority authority) {
    if (authority != null) {
      out.print(",AUTHORITY[");
      write(out, authority.getName());
      out.write(",\"");
      out.print(authority.getCode());
      out.write("\"]");
    }
  }

  public static void write(final PrintWriter out,
    final CoordinateSystem coordinateSystem) {
    if (coordinateSystem instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projCs = (ProjectedCoordinateSystem)coordinateSystem;
      write(out, projCs);
    } else if (coordinateSystem instanceof GeographicCoordinateSystem) {
      final GeographicCoordinateSystem geoCs = (GeographicCoordinateSystem)coordinateSystem;
      write(out, geoCs);
    }
  }

  public static void write(final PrintWriter out, final Datum datum) {
    if (datum != null) {
      out.print(",DATUM[");
      write(out, datum.getName());
      final Spheroid spheroid = datum.getSpheroid();
      if (spheroid != null) {
        write(out, spheroid);
      }
      final Authority authority = datum.getAuthority();
      write(out, authority);
      out.write(']');
    }
  }

  public static void write(final PrintWriter out,
    final GeographicCoordinateSystem coordinateSystem) {
    if (coordinateSystem != null) {
      out.print("GEOGCS[");
      write(out, coordinateSystem.getName());
      final Datum datum = coordinateSystem.getDatum();
      write(out, datum);
      final PrimeMeridian primeMeridian = coordinateSystem.getPrimeMeridian();
      write(out, primeMeridian);
      final AngularUnit unit = coordinateSystem.getAngularUnit();
      write(out, unit);
      final Authority authority = coordinateSystem.getAuthority();
      write(out, authority);
      out.write(']');
    }
  }

  public static void write(final PrintWriter out, final LinearUnit unit) {
    if (unit != null) {
      out.print(",UNIT[");
      write(out, unit.getName());
      out.write(',');
      write(out, unit.getConversionFactor());
      final Authority authority = unit.getAuthority();
      write(out, authority);
      out.write(']');
    }
  }

  private static void write(final PrintWriter out, final Number number) {
    out.print(MathUtil.toString(number));

  }

  public static void write(final PrintWriter out,
    final PrimeMeridian primeMeridian) {
    if (primeMeridian != null) {
      out.print(",PRIMEM[");
      write(out, primeMeridian.getName());
      out.write(',');
      final double longitude = primeMeridian.getLongitude();
      write(out, longitude);
      final Authority authority = primeMeridian.getAuthority();
      write(out, authority);
      out.write(']');
    }
  }

  public static void write(final PrintWriter out,
    final ProjectedCoordinateSystem coordinateSystem) {
    if (coordinateSystem != null) {
      out.print("PROJCS[");
      write(out, coordinateSystem.getName());
      final GeographicCoordinateSystem geoCs = coordinateSystem.getGeographicCoordinateSystem();
      out.print(",");
      write(out, geoCs);
      final Projection projection = coordinateSystem.getProjection();
      write(out, projection);
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
      final Authority authority = coordinateSystem.getAuthority();
      write(out, authority);
      out.write(']');
    }
  }

  public static void write(final PrintWriter out, final Projection projection) {
    if (projection != null) {
      out.print(",PROJECTION[");
      write(out, projection.getName());
      out.write(']');
    }
  }

  public static void write(final PrintWriter out, final Spheroid spheroid) {
    if (spheroid != null) {
      out.print(",SPHEROID[");
      write(out, spheroid.getName());
      out.write(',');
      final double semiMajorAxis = spheroid.getSemiMajorAxis();
      write(out, semiMajorAxis);
      out.print(',');
      final double inverseFlattening = spheroid.getInverseFlattening();
      write(out, inverseFlattening);
      final Authority authority = spheroid.getAuthority();
      write(out, authority);
      out.write(']');
    }
  }

  public static void write(final PrintWriter out, final String value) {
    out.write('"');
    if (value != null) {
      out.print(value);
    }
    out.write('"');
  }

  public static void write(final PrintWriter out, final String name,
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
