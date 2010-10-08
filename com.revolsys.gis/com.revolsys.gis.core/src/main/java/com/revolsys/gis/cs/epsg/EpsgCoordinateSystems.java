package com.revolsys.gis.cs.epsg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.revolsys.gis.cs.AngularUnit;
import com.revolsys.gis.cs.Area;
import com.revolsys.gis.cs.Authority;
import com.revolsys.gis.cs.Axis;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.Datum;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.LinearUnit;
import com.revolsys.gis.cs.PrimeMeridian;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.Projection;
import com.revolsys.gis.cs.Spheroid;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public final class EpsgCoordinateSystems {
  private static Map<Integer, AngularUnit> angularCoordinateSystemUnits = new HashMap<Integer, AngularUnit>();

  private static Map<Integer, AngularUnit> angularUnits = new HashMap<Integer, AngularUnit>();

  private static Map<Integer, Area> areas = new HashMap<Integer, Area>();

  private static Map<Integer, List<Axis>> coordinateAxises = new HashMap<Integer, List<Axis>>();

  private static Map<Integer, String> coordinateAxisNames = new HashMap<Integer, String>();

  private static Map<Integer, String> coordinateOperationMethodNames = new HashMap<Integer, String>();

  private static Map<Integer, Integer> coordinateOperationMethods = new HashMap<Integer, Integer>();

  private static Map<Integer, String> coordinateOperationParamNames = new HashMap<Integer, String>();

  private static Map<Integer, Map<String, Object>> coordinateOperationParamValues = new HashMap<Integer, Map<String, Object>>();

  private static Set<CoordinateSystem> coordinateSystems;

  private static Map<CoordinateSystem, CoordinateSystem> coordinateSystemsByCoordinateSystem = new HashMap<CoordinateSystem, CoordinateSystem>();

  private static Map<Integer, CoordinateSystem> coordinateSystemsById = new TreeMap<Integer, CoordinateSystem>();

  private static Map<Datum, PrimeMeridian> datumPrimeMeridians = new HashMap<Datum, PrimeMeridian>();

  private static Map<Integer, Datum> datums = new HashMap<Integer, Datum>();

  private static Map<Integer, LinearUnit> linearCoordinateSystemUnits = new HashMap<Integer, LinearUnit>();

  private static Map<Integer, LinearUnit> linearUnits = new HashMap<Integer, LinearUnit>();

  private static Map<Integer, PrimeMeridian> primeMeridians = new HashMap<Integer, PrimeMeridian>();

  private static Map<Integer, Spheroid> spheroids = new HashMap<Integer, Spheroid>();

  static {
    loadUnits();
    loadAreas();
    loadCoordinateAxisNames();
    loadCoordinateAxises();
    loadCoordinateOperationMethodNames();
    loadCoordinateOperationParamNames();
    loadCoordinateOperationParamValues();
    loadCoordinateOperations();
    loadSpheroids();
    loadPrimeMeridians();
    loadDatums();
    loadGeographicCoordinateSystems();
    loadProjectedCoordinateSystems();
    coordinateSystems = Collections.unmodifiableSet(new LinkedHashSet<CoordinateSystem>(
      coordinateSystemsByCoordinateSystem.values()));
  }

  public static Map<Integer, Area> getAreas() {
    return Collections.unmodifiableMap(areas);
  }

  public static Map<Integer, List<Axis>> getCoordinateAxises() {
    return Collections.unmodifiableMap(coordinateAxises);
  }

  public static Map<Integer, String> getCoordinateAxisNames() {
    return Collections.unmodifiableMap(coordinateAxisNames);
  }

  public static CoordinateSystem getCoordinateSystem(
    final CoordinateSystem coordinateSystem) {
    final CoordinateSystem coordinateSystem2 = coordinateSystemsByCoordinateSystem.get(coordinateSystem);
    if (coordinateSystem2 == null) {
      return coordinateSystem;
    } else {
      return coordinateSystem2;
    }
  }

  public static CoordinateSystem getCoordinateSystem(
    final Geometry geometry) {
    return getCoordinateSystem(geometry.getSRID());
  }

  public static CoordinateSystem getCoordinateSystem(
    final int crsId) {
    final CoordinateSystem coordinateSystem = coordinateSystemsById.get(crsId);
    return coordinateSystem;
  }

  public static Set<CoordinateSystem> getCoordinateSystems() {
    return coordinateSystems;
  }

  /**
   * Get the coordinate systems for the list of coordinate system identifiers.
   * Null identifiers will be ignored. If the coordinate system does not exist
   * then it will be ignored.
   * 
   * @param coordinateSystemIds The coordinate system identifiers.
   * @return The list of coordinate systems.
   */
  public static List<CoordinateSystem> getCoordinateSystems(
    final Collection<Integer> coordinateSystemIds) {
    final List<CoordinateSystem> coordinateSystems = new ArrayList<CoordinateSystem>();
    for (final Integer coordinateSystemId : coordinateSystemIds) {
      if (coordinateSystemId != null) {
        final CoordinateSystem coordinateSystem = getCoordinateSystem(coordinateSystemId);
        if (coordinateSystem != null) {
          coordinateSystems.add(coordinateSystem);
        }
      }
    }
    return coordinateSystems;
  }

  public static Map<Integer, CoordinateSystem> getCoordinateSystemsById() {
    return Collections.unmodifiableMap(coordinateSystemsById);
  }

  public static int getCrsId(
    final CoordinateSystem coordinateSystem) {
    final Authority authority = coordinateSystem.getAuthority();
    if (authority != null) {
      final String name = authority.getName();
      final String code = authority.getCode();
      if (name.equals("EPSG")) {
        return Integer.parseInt(code);
      }
    }
    return 0;
  }

  public static String getCrsName(
    final CoordinateSystem coordinateSystem) {
    final Authority authority = coordinateSystem.getAuthority();
    final String name = authority.getName();
    final String code = authority.getCode();
    if (name.equals("EPSG")) {
      return name + ":" + code;
    } else {
      return null;
    }
  }

  public static Map<Integer, Datum> getDatums() {
    return Collections.unmodifiableMap(datums);
  }

  private static double getDouble(
    final String value) {
    if (value.equals("")) {
      return Double.NaN;
    } else {
      return Double.valueOf(value);
    }
  }

  private static Integer getInteger(
    final String value) {
    if (value.equals("")) {
      return null;
    } else {
      return Integer.valueOf(value);
    }
  }

  public static Map<Integer, LinearUnit> getLinearUnits() {
    return Collections.unmodifiableMap(linearUnits);
  }

  public static Map<Integer, PrimeMeridian> getPrimeMeridians() {
    return Collections.unmodifiableMap(primeMeridians);
  }

  public static Map<Integer, Spheroid> getSpheroids() {
    return Collections.unmodifiableMap(spheroids);
  }

  private static String getString(
    final String string) {
    return new String(string);
  }

  private static void loadAreas() {
    final String resourceName = "/com/revolsys/gis/cs/epsg/area.txt";

    final InputStream in = EpsgCoordinateSystems.class.getResourceAsStream(resourceName);
    if (in != null) {
      try {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
          in));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
          final String[] fields = line.split("\t");
          final Integer code = getInteger(fields[0]);
          final String name = getString(fields[1]);
          final Double minY = getDouble(fields[2]);
          final Double maxY = getDouble(fields[3]);
          final Double minX = getDouble(fields[4]);
          final Double maxX = getDouble(fields[5]);
          final boolean deprecated = fields[6].equals("1");
          final Authority authority = new Authority("EPSG", code);

          final Area area = new Area(name,
            new Envelope(minX, maxX, minY, maxY), authority, deprecated);
          areas.put(code, area);
        }

      } catch (final IOException e) {

      }
    }
  }

  private static void loadCoordinateAxises() {
    final String resourceName = "/com/revolsys/gis/cs/epsg/coordinateaxis.txt";

    final InputStream in = EpsgCoordinateSystems.class.getResourceAsStream(resourceName);
    if (in != null) {
      try {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
          in));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
          final String[] fields = line.split("\t");
          final Integer code = getInteger(fields[0]);
          final Integer nameCode = getInteger(fields[1]);
          final String direction = getString(fields[2]);
          final String name = coordinateAxisNames.get(nameCode);
          final Integer uomCode = getInteger(fields[3]);
          final LinearUnit linearUnit = linearUnits.get(uomCode);
          if (linearUnit != null) {
            linearCoordinateSystemUnits.put(code, linearUnit);
          }
          final AngularUnit angularUnit = angularUnits.get(uomCode);
          if (angularUnit != null) {
            angularCoordinateSystemUnits.put(code, angularUnit);
          }
          final int order = getInteger(fields[4]);
          List<Axis> axises = coordinateAxises.get(code);
          if (axises == null) {
            axises = new ArrayList<Axis>();
            coordinateAxises.put(code, axises);
          }
          while (axises.size() < order) {
            axises.add(null);
          }
          axises.set(order - 1, new Axis(name, direction));
        }
      } catch (final IOException e) {

      }
    }
  }

  private static void loadCoordinateAxisNames() {
    final String resourceName = "/com/revolsys/gis/cs/epsg/coordinateaxisname.txt";

    final InputStream in = EpsgCoordinateSystems.class.getResourceAsStream(resourceName);
    if (in != null) {
      try {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
          in));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
          final String[] fields = line.split("\t");
          final Integer code = getInteger(fields[0]);
          final String name = getString(fields[1]);
          coordinateAxisNames.put(code, name);
        }
      } catch (final IOException e) {

      }
    }
  }

  private static void loadCoordinateOperationMethodNames() {
    final String resourceName = "/com/revolsys/gis/cs/epsg/coordoperationmethod.txt";

    final InputStream in = EpsgCoordinateSystems.class.getResourceAsStream(resourceName);
    if (in != null) {
      try {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
          in));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
          final String[] fields = line.split("\t");
          final Integer code = getInteger(fields[0]);
          final String name = getString(fields[1]).replace(" ", "_");
          coordinateOperationMethodNames.put(code, name);
        }
      } catch (final IOException e) {

      }
    }
  }

  private static void loadCoordinateOperationParamNames() {
    final String resourceName = "/com/revolsys/gis/cs/epsg/coordoperationparam.txt";

    final InputStream in = EpsgCoordinateSystems.class.getResourceAsStream(resourceName);
    if (in != null) {
      try {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
          in));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
          final String[] fields = line.split("\t");
          final Integer code = getInteger(fields[0]);
          final String name = getString(fields[1].replace(" ", "_")
            .toLowerCase());
          coordinateOperationParamNames.put(code, name);
        }
      } catch (final IOException e) {

      }
    }
  }

  private static void loadCoordinateOperationParamValues() {
    final String resourceName = "/com/revolsys/gis/cs/epsg/coordoperationparamvalue.txt";

    final InputStream in = EpsgCoordinateSystems.class.getResourceAsStream(resourceName);
    if (in != null) {
      try {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
          in));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
          final String[] fields = line.split("\t");
          if (fields.length == 3) {

            final Integer code = getInteger(fields[0]);
            final Integer nameCode = getInteger(fields[1]);
            final String valueString = getString(fields[2]);
            Map<String, Object> parameters = coordinateOperationParamValues.get(code);
            if (parameters == null) {
              parameters = new HashMap<String, Object>();
              coordinateOperationParamValues.put(code, parameters);
            }
            final String paramName = coordinateOperationParamNames.get(nameCode);
            parameters.put(paramName, Double.parseDouble(valueString));
          }
        }
      } catch (final IOException e) {

      }
    }
  }

  private static void loadCoordinateOperations() {
    final String resourceName = "/com/revolsys/gis/cs/epsg/coordoperation.txt";

    final InputStream in = EpsgCoordinateSystems.class.getResourceAsStream(resourceName);
    if (in != null) {
      try {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
          in));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
          final String[] fields = line.split("\t");
          if (fields.length == 2) {
            final Integer code = getInteger(fields[0]);
            final Integer methodCode = getInteger(fields[1]);
            coordinateOperationMethods.put(code, methodCode);
          }
        }
      } catch (final IOException e) {

      }
    }
  }

  private static void loadDatums() {
    final String resourceName = "/com/revolsys/gis/cs/epsg/datum.txt";

    final InputStream in = EpsgCoordinateSystems.class.getResourceAsStream(resourceName);
    if (in != null) {
      try {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
          in));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
          final String[] fields = line.split("\t");
          final String datumType = getString(fields[2]);
          final boolean deprecated = fields[5].equals("1");
          if (datumType.equals("geodetic")) {
            final Integer code = getInteger(fields[0]);
            final String name = getString(fields[1]);
            final Integer spheroidCode = getInteger(fields[3]);
            final Integer primeMeridianCode = getInteger(fields[4]);

            final Authority authority = new Authority("EPSG", code);
            final Spheroid spheroid = spheroids.get(spheroidCode);

            final Datum datum = new Datum(name, spheroid, authority, deprecated);
            datums.put(code, datum);
            final PrimeMeridian primeMerdian = primeMeridians.get(primeMeridianCode);
            datumPrimeMeridians.put(datum, primeMerdian);
          }
        }
      } catch (final IOException e) {

      }
    }
  }

  private static void loadGeographicCoordinateSystems() {
    final String resourceName = "/com/revolsys/gis/cs/epsg/coordinatesystem.txt";

    final InputStream in = EpsgCoordinateSystems.class.getResourceAsStream(resourceName);
    if (in != null) {
      try {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
          in));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
          final String[] fields = line.split("\t");
          final String type = getString(fields[4]);
          final boolean deprecated = fields[8].equals("1");
          if (type.equals("geographic 2D")) {
            final Integer datumCode = getInteger(fields[5]);
            final Datum datum = datums.get(datumCode);

            final Integer code = getInteger(fields[0]);
            final String name = getString(fields[1]);

            final Integer coordSysCode = getInteger(fields[2]);

            final Integer areaCode = getInteger(fields[3]);
            final Area area = areas.get(areaCode);

            final Authority authority = new Authority("EPSG", code);
            final AngularUnit angularUnit = angularCoordinateSystemUnits.get(coordSysCode);
            final List<Axis> axis = coordinateAxises.get(coordSysCode);
            final PrimeMeridian primeMeridian = datumPrimeMeridians.get(datum);

            final GeographicCoordinateSystem cs = new GeographicCoordinateSystem(
              code, name, datum, primeMeridian, angularUnit, axis, area,
              authority, deprecated);
            coordinateSystemsById.put(code, cs);
            coordinateSystemsByCoordinateSystem.put(cs, cs);
          }
        }
      } catch (final IOException e) {

      }
    }
  }

  private static void loadPrimeMeridians() {
    final String resourceName = "/com/revolsys/gis/cs/epsg/primemeridian.txt";

    final InputStream in = EpsgCoordinateSystems.class.getResourceAsStream(resourceName);
    if (in != null) {
      try {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
          in));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
          final String[] fields = line.split("\t");
          final Integer code = getInteger(fields[0]);
          final String name = fields[1];
          final String longitudeString = fields[2];
          final boolean deprecated = fields[3].equals("1");
          double longitude = Double.NaN;
          if (!longitudeString.equals("")) {
            longitude = Double.parseDouble(longitudeString);
          }
          final Authority authority = new Authority("EPSG", code);

          final PrimeMeridian primeMeridian = new PrimeMeridian(name,
            longitude, authority, deprecated);
          primeMeridians.put(code, primeMeridian);
        }
      } catch (final IOException e) {

      }
    }
  }

  private static void loadProjectedCoordinateSystems() {
    final String resourceName = "/com/revolsys/gis/cs/epsg/coordinatesystem.txt";

    final InputStream in = EpsgCoordinateSystems.class.getResourceAsStream(resourceName);
    if (in != null) {
      try {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
          in));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
          final String[] fields = line.split("\t");

          final String type = getString(fields[4]);
          if (type.equals("projected")) {
            final Integer code = getInteger(fields[0]);
            final String name = getString(fields[1]);
            final Integer coordSysCode = getInteger(fields[2]);
            final Integer areaCode = getInteger(fields[3]);
            final Integer sourceGeogcrsCode = getInteger(fields[6]);
            final Integer projectionConvCode = getInteger(fields[7]);
            final boolean deprecated = fields[8].equals("1");

            final Authority authority = new Authority("EPSG", code);
            final LinearUnit linearUnit = linearCoordinateSystemUnits.get(coordSysCode);
            final List<Axis> axis = coordinateAxises.get(coordSysCode);
            final CoordinateSystem sourceCs = coordinateSystemsById.get(sourceGeogcrsCode);
            if (sourceCs instanceof GeographicCoordinateSystem) {
              final GeographicCoordinateSystem geographicCs = (GeographicCoordinateSystem)sourceCs;
              final Map<String, Object> parameters = coordinateOperationParamValues.get(projectionConvCode);
              final Integer methodCode = coordinateOperationMethods.get(projectionConvCode);
              final String methodName = coordinateOperationMethodNames.get(methodCode);
              final Projection projection = new Projection(methodName,
                new Authority("EPSG", methodCode));
              final Area area = areas.get(areaCode);

              final ProjectedCoordinateSystem cs = new ProjectedCoordinateSystem(
                code, name, geographicCs, area, projection, parameters,
                linearUnit, axis, authority, deprecated);
              coordinateSystemsById.put(code, cs);
              coordinateSystemsByCoordinateSystem.put(cs, cs);
            }
          }
        }
      } catch (final IOException e) {

      }
    }
  }

  private static void loadSpheroids() {
    final String resourceName = "/com/revolsys/gis/cs/epsg/spheroid.txt";

    final InputStream in = EpsgCoordinateSystems.class.getResourceAsStream(resourceName);
    if (in != null) {
      try {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
          in));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
          final String[] fields = line.split("\t");
          final Integer code = getInteger(fields[0]);
          final boolean deprecated = fields[5].equals("1");
          final String name = fields[1];
          final String semiMajorAxisString = fields[2];
          final String invFlatteningString = fields[3];
          final String semiMinorAxisString = fields[4];
          final double semiMajorAxis = Double.parseDouble(semiMajorAxisString);
          double inverseFlattening = Double.NaN;
          if (!invFlatteningString.equals("")) {
            inverseFlattening = Double.parseDouble(invFlatteningString);
          }
          double semiMinorAxis = Double.NaN;
          if (!semiMinorAxisString.equals("")) {
            semiMinorAxis = Double.parseDouble(semiMinorAxisString);
          }
          final Authority authority = new Authority("EPSG", code);

          final Spheroid spheroid = new Spheroid(name, semiMajorAxis,
            semiMinorAxis, inverseFlattening, authority, deprecated);
          spheroids.put(code, spheroid);
        }
      } catch (final IOException e) {

      }
    }
  }

  private static void loadUnits() {
    final String resourceName = "/com/revolsys/gis/cs/epsg/unit.txt";

    final InputStream in = EpsgCoordinateSystems.class.getResourceAsStream(resourceName);
    if (in != null) {
      try {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
          in));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
          final String[] fields = line.split("\t");
          final Integer code = getInteger(fields[0]);
          final String name = fields[1];
          final Integer baseUnitCode = getInteger(fields[2]);
          final String conversionFactorString = fields[3];
          final String conversionFactorCString = fields[4];
          final boolean deprecated = fields[5].equals("1");
          double conversionFactor = Double.NaN;
          if (!conversionFactorString.equals("")) {
            conversionFactor = Double.parseDouble(conversionFactorString);
          }
          if (!conversionFactorCString.equals("")) {
            final double conversionFactorC = Double.parseDouble(conversionFactorCString);
            conversionFactor = conversionFactor / conversionFactorC;
          }
          final Authority authority = new Authority("EPSG", code);
          if (baseUnitCode == 9101 || baseUnitCode == 9102) {
            AngularUnit baseUnit = null;
            if (!code.equals(baseUnitCode)) {
              baseUnit = angularUnits.get(baseUnitCode);
            }

            final AngularUnit unit = new AngularUnit(name, baseUnit,
              conversionFactor, authority, deprecated);
            angularUnits.put(code, unit);
          } else {
            LinearUnit baseUnit = null;
            if (!code.equals(baseUnitCode)) {
              baseUnit = linearUnits.get(baseUnitCode);
            }

            final LinearUnit unit = new LinearUnit(name, baseUnit,
              conversionFactor, authority, deprecated);
            linearUnits.put(code, unit);
          }
        }

      } catch (final Throwable e) {
        e.printStackTrace();
      }
    }
  }

  private EpsgCoordinateSystems() {
  }
}
