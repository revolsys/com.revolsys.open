package com.revolsys.gis.cs.esri;

import java.io.PrintWriter;
import java.io.StringWriter;
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

  protected static int incrementIndent(final int indentLevel) {
    if (indentLevel < 0) {
      return -1;
    } else {
      return indentLevel + 1;
    }
  }

  public static void indent(final PrintWriter out, final int indentLevel) {
    if (indentLevel >= 0) {
      out.println();
      for (int i = 0; i < indentLevel; i++) {
        out.print("  ");
      }
    }
  }

  public static String toString(final CoordinateSystem coordinateSystem) {
    final StringWriter string = new StringWriter();
    final PrintWriter out = new PrintWriter(string);
    write(out, coordinateSystem, 0);
    return string.toString();
  }

  public static String toWkt(final CoordinateSystem coordinateSystem) {
    final StringWriter stringWriter = new StringWriter();
    final PrintWriter out = new PrintWriter(stringWriter);
    write(out, coordinateSystem, -1);
    return stringWriter.toString();
  }

  public static void write(final PrintWriter out, final AngularUnit unit, final int indentLevel) {
    out.print(",");
    indent(out, indentLevel);
    out.print("UNIT[");
    write(out, unit.getName(), -1);
    out.write(',');
    write(out, unit.getConversionFactor(), -1);
    out.write(']');
  }

  public static void write(final PrintWriter out, final CoordinateSystem coordinateSystem,
    final int indentLevel) {
    if (coordinateSystem instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projCs = (ProjectedCoordinateSystem)coordinateSystem;
      write(out, projCs, indentLevel);
    } else if (coordinateSystem instanceof GeographicCoordinateSystem) {
      final GeographicCoordinateSystem geoCs = (GeographicCoordinateSystem)coordinateSystem;
      write(out, geoCs, indentLevel);
    }
  }

  public static void write(final PrintWriter out, final Datum datum, final int indentLevel) {
    out.print("DATUM[");
    write(out, datum.getName(), incrementIndent(indentLevel));
    final Spheroid spheroid = datum.getSpheroid();
    if (spheroid != null) {
      out.print(",");
      indent(out, incrementIndent(indentLevel));
      write(out, spheroid, incrementIndent(indentLevel));
    }
    indent(out, indentLevel);
    out.write(']');
  }

  public static void write(final PrintWriter out,
    final GeographicCoordinateSystem coordinateSystem, final int indentLevel) {
    out.print("GEOGCS[");
    write(out, coordinateSystem.getName(), incrementIndent(indentLevel));
    final Datum datum = coordinateSystem.getDatum();
    if (datum != null) {
      out.print(",");
      indent(out, incrementIndent(indentLevel));
      write(out, datum, incrementIndent(indentLevel));
    }
    final PrimeMeridian primeMeridian = coordinateSystem.getPrimeMeridian();
    if (primeMeridian != null) {
      out.print(",");
      indent(out, incrementIndent(indentLevel));
      write(out, primeMeridian, incrementIndent(indentLevel));
    }
    final AngularUnit unit = coordinateSystem.getAngularUnit();
    if (unit != null) {
      write(out, unit, incrementIndent(indentLevel));
    }
    indent(out, indentLevel);
    out.write(']');
  }

  public static void write(final PrintWriter out, final LinearUnit unit, final int indentLevel) {
    out.print(",");
    indent(out, indentLevel);
    out.print("UNIT[");
    write(out, unit.getName(), -1);
    out.write(',');
    write(out, unit.getConversionFactor(), -1);
    out.write(']');
  }

  private static void write(final PrintWriter out, final Number number, final int indentLevel) {
    indent(out, indentLevel);
    out.print(new DecimalFormat("#0.0###############").format(number));

  }

  public static void write(final PrintWriter out, final PrimeMeridian primeMeridian,
    final int indentLevel) {
    out.print("PRIMEM[");
    write(out, primeMeridian.getName(), incrementIndent(indentLevel));
    out.write(',');
    final double longitude = primeMeridian.getLongitude();
    write(out, longitude, incrementIndent(indentLevel));
    indent(out, indentLevel);
    out.write(']');
  }

  public static void write(final PrintWriter out, final ProjectedCoordinateSystem coordinateSystem,
    final int indentLevel) {
    out.print("PROJCS[");
    write(out, coordinateSystem.getName(), incrementIndent(indentLevel));
    final GeographicCoordinateSystem geoCs = coordinateSystem.getGeographicCoordinateSystem();
    if (geoCs != null) {
      out.print(",");
      indent(out, incrementIndent(indentLevel));
      write(out, geoCs, incrementIndent(indentLevel));
    }
    final Projection projection = coordinateSystem.getProjection();
    if (projection != null) {
      out.print(",");
      indent(out, incrementIndent(indentLevel));
      write(out, projection, incrementIndent(indentLevel));
    }
    for (final Entry<String, Object> parameter : coordinateSystem.getParameters().entrySet()) {
      final String name = parameter.getKey();
      final Object value = parameter.getValue();
      write(out, name, value, incrementIndent(indentLevel));
    }
    final LinearUnit unit = coordinateSystem.getLinearUnit();
    if (unit != null) {
      write(out, unit, incrementIndent(indentLevel));
    }
    indent(out, indentLevel);
    out.write(']');
  }

  public static void write(final PrintWriter out, final Projection projection, final int indentLevel) {
    out.print("PROJECTION[");
    write(out, projection.getName(), incrementIndent(indentLevel));
    indent(out, indentLevel);
    out.write(']');
  }

  public static void write(final PrintWriter out, final Spheroid spheroid, final int indentLevel) {
    out.print("SPHEROID[");
    write(out, spheroid.getName(), incrementIndent(indentLevel));
    out.write(',');
    final double semiMajorAxis = spheroid.getSemiMajorAxis();
    write(out, semiMajorAxis, incrementIndent(indentLevel));
    out.print(',');
    final double inverseFlattening = spheroid.getInverseFlattening();
    write(out, inverseFlattening, incrementIndent(indentLevel));
    indent(out, indentLevel);
    out.write(']');
  }

  public static void write(final PrintWriter out, final String value, final int indentLevel) {
    indent(out, indentLevel);
    out.write('"');
    if (value != null) {
      out.print(value);
    }
    out.write('"');
  }

  public static void write(final PrintWriter out, final String name, final Object value,
    final int indentLevel) {
    out.print(",");
    indent(out, indentLevel);
    out.print("PARAMETER[");
    write(out, name, -1);
    out.write(',');
    if (value instanceof Number) {
      final Number number = (Number)value;
      write(out, number, -1);
    } else {
      out.print(value);
    }
    out.write(']');
  }
}
