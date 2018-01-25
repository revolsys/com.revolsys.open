package com.revolsys.geometry.cs.esri;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.Map.Entry;

import com.revolsys.geometry.cs.AngularUnit;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.GeodeticDatum;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.LinearUnit;
import com.revolsys.geometry.cs.PrimeMeridian;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.Projection;
import com.revolsys.geometry.cs.Spheroid;
import com.revolsys.util.Exceptions;

public class EsriCsWktWriter {

  protected static int incrementIndent(final int indentLevel) {
    if (indentLevel < 0) {
      return -1;
    } else {
      return indentLevel + 1;
    }
  }

  public static void indent(final Writer out, final int indentLevel) throws IOException {
    if (indentLevel >= 0) {
      out.write('\n');
      for (int i = 0; i < indentLevel; i++) {
        out.write("  ");
      }
    }
  }

  public static String toString(final CoordinateSystem coordinateSystem) {
    final StringWriter string = new StringWriter();
    write(string, coordinateSystem, 0);
    return string.toString();
  }

  public static String toWkt(final CoordinateSystem coordinateSystem) {
    final StringWriter string = new StringWriter();
    write(string, coordinateSystem, -1);
    return string.toString();
  }

  public static void write(final Writer out, final AngularUnit unit, final int indentLevel)
    throws IOException {
    out.write(",");
    indent(out, indentLevel);
    out.write("UNIT[");
    write(out, unit.getName(), -1);
    out.write(',');
    write(out, unit.getConversionFactor(), -1);
    out.write(']');
  }

  public static void write(final Writer out, final CoordinateSystem coordinateSystem,
    final int indentLevel) {
    try {
      if (coordinateSystem instanceof ProjectedCoordinateSystem) {
        final ProjectedCoordinateSystem projCs = (ProjectedCoordinateSystem)coordinateSystem;
        write(out, projCs, indentLevel);
      } else if (coordinateSystem instanceof GeographicCoordinateSystem) {
        final GeographicCoordinateSystem geoCs = (GeographicCoordinateSystem)coordinateSystem;
        write(out, geoCs, indentLevel);
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  public static void write(final Writer out, final GeodeticDatum geodeticDatum, final int indentLevel)
    throws IOException {
    out.write("DATUM[");
    write(out, geodeticDatum.getName(), incrementIndent(indentLevel));
    final Spheroid spheroid = geodeticDatum.getSpheroid();
    if (spheroid != null) {
      out.write(",");
      indent(out, incrementIndent(indentLevel));
      write(out, spheroid, incrementIndent(indentLevel));
    }
    indent(out, indentLevel);
    out.write(']');
  }

  public static void write(final Writer out, final GeographicCoordinateSystem coordinateSystem,
    final int indentLevel) throws IOException {
    out.write("GEOGCS[");
    write(out, coordinateSystem.getCoordinateSystemName(), incrementIndent(indentLevel));
    final GeodeticDatum geodeticDatum = coordinateSystem.getDatum();
    if (geodeticDatum != null) {
      out.write(",");
      indent(out, incrementIndent(indentLevel));
      write(out, geodeticDatum, incrementIndent(indentLevel));
    }
    final PrimeMeridian primeMeridian = coordinateSystem.getPrimeMeridian();
    if (primeMeridian != null) {
      out.write(",");
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

  public static void write(final Writer out, final LinearUnit unit, final int indentLevel)
    throws IOException {
    out.write(",");
    indent(out, indentLevel);
    out.write("UNIT[");
    write(out, unit.getName(), -1);
    out.write(',');
    write(out, unit.getConversionFactor(), -1);
    out.write(']');
  }

  private static void write(final Writer out, final Number number, final int indentLevel)
    throws IOException {
    indent(out, indentLevel);
    out.write(new DecimalFormat("#0.0###############").format(number));

  }

  public static void write(final Writer out, final PrimeMeridian primeMeridian,
    final int indentLevel) throws IOException {
    out.write("PRIMEM[");
    write(out, primeMeridian.getName(), incrementIndent(indentLevel));
    out.write(',');
    final double longitude = primeMeridian.getLongitude();
    write(out, longitude, incrementIndent(indentLevel));
    indent(out, indentLevel);
    out.write(']');
  }

  public static void write(final Writer out, final ProjectedCoordinateSystem coordinateSystem,
    final int indentLevel) throws IOException {
    out.write("PROJCS[");
    write(out, coordinateSystem.getCoordinateSystemName(), incrementIndent(indentLevel));
    final GeographicCoordinateSystem geoCs = coordinateSystem.getGeographicCoordinateSystem();
    if (geoCs != null) {
      out.write(",");
      indent(out, incrementIndent(indentLevel));
      write(out, geoCs, incrementIndent(indentLevel));
    }
    final Projection projection = coordinateSystem.getProjection();
    if (projection != null) {
      out.write(",");
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

  public static void write(final Writer out, final Projection projection, final int indentLevel)
    throws IOException {
    out.write("PROJECTION[");
    write(out, projection.getName(), incrementIndent(indentLevel));
    indent(out, indentLevel);
    out.write(']');
  }

  public static void write(final Writer out, final Spheroid spheroid, final int indentLevel)
    throws IOException {
    out.write("SPHEROID[");
    write(out, spheroid.getName(), incrementIndent(indentLevel));
    out.write(',');
    final double semiMajorAxis = spheroid.getSemiMajorAxis();
    write(out, semiMajorAxis, incrementIndent(indentLevel));
    out.write(',');
    final double inverseFlattening = spheroid.getInverseFlattening();
    write(out, inverseFlattening, incrementIndent(indentLevel));
    indent(out, indentLevel);
    out.write(']');
  }

  public static void write(final Writer out, final String value, final int indentLevel)
    throws IOException {
    indent(out, indentLevel);
    out.write('"');
    if (value != null) {
      out.write(value);
    }
    out.write('"');
  }

  public static void write(final Writer out, final String name, final Object value,
    final int indentLevel) throws IOException {
    out.write(",");
    indent(out, indentLevel);
    out.write("PARAMETER[");
    write(out, name, -1);
    out.write(',');
    if (value instanceof Number) {
      final Number number = (Number)value;
      write(out, number, -1);
    } else {
      out.write(value.toString());
    }
    out.write(']');
  }
}
