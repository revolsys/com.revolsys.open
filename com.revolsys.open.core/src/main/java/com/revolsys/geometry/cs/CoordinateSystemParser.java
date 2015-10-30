package com.revolsys.geometry.cs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.io.FileUtil;

public class CoordinateSystemParser {
  public static List<GeographicCoordinateSystem> getGeographicCoordinateSystems(
    final String authorityName, final InputStream in) {
    final Map<String, AngularUnit> angularUnitsByName = new TreeMap<String, AngularUnit>();
    final List<GeographicCoordinateSystem> coordinateSystems = new ArrayList<GeographicCoordinateSystem>();
    final BufferedReader reader = new BufferedReader(FileUtil.newUtf8Reader(in));
    try {
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        final String[] fields = line.split("\t");
        final String id = fields[0];
        final String csName = fields[1];
        final String datumName = fields[2];
        final String spheroidName = fields[3];
        final double semiMajorAxis = Double.parseDouble(fields[4]);
        final double inverseFlattening = Double.parseDouble(fields[5]);
        final String primeMeridianName = fields[6];
        final double longitude = Double.parseDouble(fields[7]);
        final String angularUnitName = fields[8];
        final double conversionFactor = Double.parseDouble(fields[9]);

        final Spheroid spheroid = new Spheroid(spheroidName, semiMajorAxis, inverseFlattening,
          null);
        final Datum datum = new Datum(datumName, spheroid, null);
        final PrimeMeridian primeMeridian = new PrimeMeridian(primeMeridianName, longitude, null);

        AngularUnit angularUnit = angularUnitsByName.get(angularUnitName);
        if (angularUnit == null) {
          angularUnit = new AngularUnit(angularUnitName, conversionFactor, null);
          angularUnitsByName.put(angularUnitName, angularUnit);
        }

        final Authority authority = new BaseAuthority(authorityName, id);
        final GeographicCoordinateSystem cs = new GeographicCoordinateSystem(Integer.parseInt(id),
          csName, datum, primeMeridian, angularUnit, null, authority);
        coordinateSystems.add(cs);
      }
    } catch (final IOException e) {
      e.printStackTrace();
    } finally {
      try {
        reader.close();
      } catch (final IOException e) {
      }
    }
    return coordinateSystems;
  }

  public static List<ProjectedCoordinateSystem> getProjectedCoordinateSystems(
    final Map<Integer, CoordinateSystem> geoCsById, final String authorityName,
    final InputStream in) {
    final Map<String, LinearUnit> linearUnitsByName = new TreeMap<String, LinearUnit>();
    final List<ProjectedCoordinateSystem> coordinateSystems = new ArrayList<ProjectedCoordinateSystem>();
    final BufferedReader reader = new BufferedReader(FileUtil.newUtf8Reader(in));
    try {
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        final String[] fields = line.split("\t");
        try {
          final String id = fields[0];
          final String csName = fields[1];
          final int geoCsId = Integer.parseInt(fields[2]);
          final GeographicCoordinateSystem geoCs = (GeographicCoordinateSystem)geoCsById
            .get(geoCsId);
          final String projectionName = fields[3];
          final String parameterString = fields[4];
          final String unitName = fields[5];
          final double conversionFactor = Double.parseDouble(fields[6]);

          final Map<String, Object> parameters = new LinkedHashMap<>();
          for (final String param : parameterString.substring(1, parameterString.length() - 1)
            .split(",")) {
            final String[] paramValues = param.split("=");
            if (paramValues.length == 2) {
              final String key = new String(paramValues[0].trim());
              final Double value = Double.valueOf(paramValues[1].trim());
              parameters.put(key, value);
            }
          }

          final Projection projection = EpsgCoordinateSystems.getProjection(projectionName);
          LinearUnit unit = linearUnitsByName.get(unitName);
          if (unit == null) {
            unit = new LinearUnit(unitName, conversionFactor, null);
            linearUnitsByName.put(unitName, unit);
          }
          final Authority authority = new BaseAuthority(authorityName, id);
          final ProjectedCoordinateSystem cs = new ProjectedCoordinateSystem(Integer.parseInt(id),
            csName, geoCs, projection, parameters, unit, null, authority);
          coordinateSystems.add(cs);
        } catch (final Throwable t) {

          t.printStackTrace();
        }
      }
    } catch (final Throwable e) {
      e.printStackTrace();
    } finally {
      try {
        reader.close();
      } catch (final IOException e) {
      }
    }
    return coordinateSystems;
  }
}
