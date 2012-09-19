package com.revolsys.gis.cs.epsg;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.util.StringUtils;

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
import com.revolsys.io.FileUtil;
import com.revolsys.io.csv.CsvIterator;
import com.revolsys.io.json.JsonMapIoFactory;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public final class EpsgCoordinateSystems {
  private static Set<CoordinateSystem> coordinateSystems;

  private static Map<CoordinateSystem, CoordinateSystem> coordinateSystemsByCoordinateSystem = new HashMap<CoordinateSystem, CoordinateSystem>();

  private static Map<Integer, CoordinateSystem> coordinateSystemsById = new HashMap<Integer, CoordinateSystem>();

  private static Map<String, CoordinateSystem> coordinateSystemsByName = new HashMap<String, CoordinateSystem>();

  private static boolean initialized = false;

  public static void clear() {
    coordinateSystems = null;
    coordinateSystemsByCoordinateSystem.clear();
    coordinateSystemsById.clear();
    coordinateSystemsByName.clear();
  }

  public static CoordinateSystem getCoordinateSystem(
    final CoordinateSystem coordinateSystem) {
    initialize();
    final CoordinateSystem coordinateSystem2 = coordinateSystemsByCoordinateSystem.get(coordinateSystem);
    if (coordinateSystem2 == null) {
      return coordinateSystem;
    } else {
      return coordinateSystem2;
    }
  }

  @SuppressWarnings("unchecked")
  public static <C extends CoordinateSystem> C getCoordinateSystem(
    final Geometry geometry) {
    return (C)getCoordinateSystem(geometry.getSRID());
  }

  @SuppressWarnings("unchecked")
  public static <C extends CoordinateSystem> C getCoordinateSystem(
    final int crsId) {
    initialize();
    final CoordinateSystem coordinateSystem = coordinateSystemsById.get(crsId);
    return (C)coordinateSystem;
  }

  public static CoordinateSystem getCoordinateSystem(final String name) {
    initialize();
    return coordinateSystemsByName.get(name);
  }

  public static Set<CoordinateSystem> getCoordinateSystems() {
    initialize();
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
    initialize();
    return new TreeMap<Integer, CoordinateSystem>(coordinateSystemsById);
  }

  public static int getCrsId(final CoordinateSystem coordinateSystem) {
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

  public static String getCrsName(final CoordinateSystem coordinateSystem) {
    final Authority authority = coordinateSystem.getAuthority();
    final String name = authority.getName();
    final String code = authority.getCode();
    if (name.equals("EPSG")) {
      return name + ":" + code;
    } else {
      return null;
    }
  }

  private static double getDouble(final String value) {
    if (value == null || value.equals("") || value.equals("NaN")) {
      return Double.NaN;
    } else {
      return Double.valueOf(value);
    }
  }

  private static Integer getInteger(final String value) {
    if (value == null || value.equals("")) {
      return null;
    } else {
      return Integer.valueOf(value);
    }
  }

  private static String getString(final String string) {
    return new String(string);
  }

  public synchronized static void initialize() {
    if (!initialized) {
      try {
        Map<Integer, List<Axis>> axisMap = loadAxis();
        Map<Integer, Area> areas = loadAreas();
        loadGeographicCoordinateSystems(axisMap, areas);
        loadProjectedCoordinateSystems(axisMap, areas);
        coordinateSystems = Collections.unmodifiableSet(new LinkedHashSet<CoordinateSystem>(
          coordinateSystemsByCoordinateSystem.values()));
        initialized = true;
      } catch (final Throwable t) {
        t.printStackTrace();
      }
    }
  }

  private static Map<Integer, Spheroid> loadSpheroids() {
    Map<Integer, Spheroid> spheroids = new HashMap<Integer, Spheroid>();
    InputStream resource = EpsgCoordinateSystems.class.getResourceAsStream("/com/revolsys/gis/cs/epsg/spheroid.csv");
    if (resource != null) {
      try {

        java.io.Reader reader = new InputStreamReader(resource);
        CsvIterator csv = new CsvIterator(reader);
        if (csv.hasNext()) {
          csv.next();
          while (csv.hasNext()) {
            List<String> values = csv.next();
            int id = Integer.parseInt(values.get(0));
            String name = values.get(1);
            double semiMajorAxis = getDouble(values.get(2));
            double semiMinorAxis = getDouble(values.get(3));
            double inverseFlattening = getDouble(values.get(4));
            boolean deprecated = Boolean.parseBoolean(values.get(5));
            EpsgAuthority authority = new EpsgAuthority(id);
            Spheroid spheroid = new Spheroid(name, semiMajorAxis,
              semiMinorAxis, inverseFlattening, authority, deprecated);
            spheroids.put(id, spheroid);
          }
        }
      } finally {
        FileUtil.closeSilent(resource);
      }
    }
    return spheroids;
  }

  private static Map<Integer, Datum> loadDatums() {
    Map<Integer, Spheroid> spheroids = loadSpheroids();
    Map<Integer, PrimeMeridian> primeMeridians = loadPrimeMeridians();
    Map<Integer, Datum> datums = new HashMap<Integer, Datum>();
    InputStream resource = EpsgCoordinateSystems.class.getResourceAsStream("/com/revolsys/gis/cs/epsg/datum.csv");
    if (resource != null) {
      try {
        java.io.Reader reader = new InputStreamReader(resource);
        CsvIterator csv = new CsvIterator(reader);
        if (csv.hasNext()) {
          csv.next();
          while (csv.hasNext()) {
            List<String> values = csv.next();
            int id = Integer.parseInt(values.get(0));
            String name = values.get(1);
            int spheroidId = Integer.parseInt(values.get(2));
            int primeMeridianId = Integer.parseInt(values.get(3));
            boolean deprecated = Boolean.parseBoolean(values.get(4));
            Spheroid spheroid = spheroids.get(spheroidId);
            PrimeMeridian primeMeridian = primeMeridians.get(primeMeridianId);
            EpsgAuthority authority = new EpsgAuthority(id);
            Datum datum = new Datum(name, spheroid, primeMeridian, authority,
              deprecated);
            datums.put(id, datum);
          }
        }
      } finally {
        FileUtil.closeSilent(resource);
      }
    }
    return datums;
  }

  private static Map<Integer, AngularUnit> loadAngularUnits() {
    Map<Integer, AngularUnit> units = new HashMap<Integer, AngularUnit>();
    InputStream resource = EpsgCoordinateSystems.class.getResourceAsStream("/com/revolsys/gis/cs/epsg/angularunit.csv");
    if (resource != null) {
      try {

        java.io.Reader reader = new InputStreamReader(resource);
        CsvIterator csv = new CsvIterator(reader);
        if (csv.hasNext()) {
          csv.next();
          while (csv.hasNext()) {
            List<String> values = csv.next();
            int id = getInteger(values.get(0));
            String name = values.get(1);
            Integer baseId = getInteger(values.get(2));
            double conversionFactor = getDouble(values.get(3));
            boolean deprecated = Boolean.parseBoolean(values.get(4));
            AngularUnit baseUnit = units.get(baseId);
            EpsgAuthority authority = new EpsgAuthority(id);
            AngularUnit unit = new AngularUnit(name, baseUnit,
              conversionFactor, authority, deprecated);
            units.put(id, unit);
          }
        }
      } finally {
        FileUtil.closeSilent(resource);
      }
    }
    return units;
  }

  private static Map<Integer, LinearUnit> loadLinearUnits() {
    Map<Integer, LinearUnit> units = new HashMap<Integer, LinearUnit>();
    InputStream resource = EpsgCoordinateSystems.class.getResourceAsStream("/com/revolsys/gis/cs/epsg/linearunit.csv");
    if (resource != null) {
      try {
        java.io.Reader reader = new InputStreamReader(resource);
        CsvIterator csv = new CsvIterator(reader);
        if (csv.hasNext()) {
          csv.next();
          while (csv.hasNext()) {
            List<String> values = csv.next();
            int id = getInteger(values.get(0));
            String name = values.get(1);
            Integer baseId = getInteger(values.get(2));
            double conversionFactor = getDouble(values.get(3));
            boolean deprecated = Boolean.parseBoolean(values.get(4));
            LinearUnit baseUnit = units.get(baseId);
            EpsgAuthority authority = new EpsgAuthority(id);
            LinearUnit unit = new LinearUnit(name, baseUnit, conversionFactor,
              authority, deprecated);
            units.put(id, unit);
          }
        }
      } finally {
        FileUtil.closeSilent(resource);
      }
    }
    return units;
  }

  private static Map<Integer, PrimeMeridian> loadPrimeMeridians() {
    Map<Integer, PrimeMeridian> primeMeridians = new HashMap<Integer, PrimeMeridian>();
    InputStream resource = EpsgCoordinateSystems.class.getResourceAsStream("/com/revolsys/gis/cs/epsg/primemeridian.csv");
    if (resource != null) {
      try {
        java.io.Reader reader = new InputStreamReader(resource);
        CsvIterator csv = new CsvIterator(reader);
        if (csv.hasNext()) {
          csv.next();
          while (csv.hasNext()) {
            List<String> values = csv.next();
            int id = Integer.parseInt(values.get(0));
            String name = values.get(1);
            double longitude = getDouble(values.get(2));
            boolean deprecated = Boolean.parseBoolean(values.get(3));
            EpsgAuthority authority = new EpsgAuthority(id);
            PrimeMeridian primeMeridian = new PrimeMeridian(name, longitude,
              authority, deprecated);
            primeMeridians.put(id, primeMeridian);
          }
        }
      } finally {
        FileUtil.closeSilent(resource);
      }
    }
    return primeMeridians;
  }

  private static void loadGeographicCoordinateSystems(
    Map<Integer, List<Axis>> axisMap, Map<Integer, Area> areas) {
    Map<Integer, Datum> datums = loadDatums();
    Map<Integer, AngularUnit> angularUnits = loadAngularUnits();
    InputStream resource = EpsgCoordinateSystems.class.getResourceAsStream("/com/revolsys/gis/cs/epsg/geographic.csv");
    if (resource != null) {
      try {
        java.io.Reader reader = new InputStreamReader(resource);
        CsvIterator csv = new CsvIterator(reader);
        if (csv.hasNext()) {
          csv.next();
          while (csv.hasNext()) {
            List<String> values = csv.next();
            int id = Integer.parseInt(values.get(0));
            String name = values.get(1);
            Integer datumId = getInteger(values.get(2));
            Integer unitId = getInteger(values.get(3));
            Integer axisId = getInteger(values.get(4));
            Integer areaId = getInteger(values.get(5));
            boolean deprecated = Boolean.parseBoolean(values.get(6));
            Datum datum = datums.get(datumId);
            EpsgAuthority authority = new EpsgAuthority(id);
            AngularUnit angularUnit = angularUnits.get(unitId);
            List<Axis> axis = axisMap.get(axisId);
            Area area = areas.get(areaId);
            GeographicCoordinateSystem coordinateSystem = new GeographicCoordinateSystem(
              id, name, datum, angularUnit, axis, area, authority, deprecated);
            addCoordinateSystem(coordinateSystem);
          }
        }
      } finally {
        FileUtil.closeSilent(resource);
      }
    }
  }

  private static void loadProjectedCoordinateSystems(
    Map<Integer, List<Axis>> axisMap, Map<Integer, Area> areas) {
    Map<Integer, LinearUnit> linearUnits = loadLinearUnits();
    InputStream resource = EpsgCoordinateSystems.class.getResourceAsStream("/com/revolsys/gis/cs/epsg/projected.csv");
    if (resource != null) {
      try {
        java.io.Reader reader = new InputStreamReader(resource);
        CsvIterator csv = new CsvIterator(reader);
        if (csv.hasNext()) {
          csv.next();
          while (csv.hasNext()) {
            List<String> values = csv.next();
            int id = Integer.parseInt(values.get(0));
            String name = values.get(1);
            Integer geoCsId = getInteger(values.get(2));
            Integer unitId = getInteger(values.get(3));
            Integer methodCode = getInteger(values.get(4));
            String methodName = values.get(5);
            String parametersString = values.get(6);
            Integer axisId = getInteger(values.get(7));
            Integer areaId = getInteger(values.get(8));
            boolean deprecated = Boolean.parseBoolean(values.get(9));
            GeographicCoordinateSystem geographicCoordinateSystem = (GeographicCoordinateSystem)coordinateSystemsById.get(geoCsId);
            EpsgAuthority authority = new EpsgAuthority(id);
            LinearUnit linearUnit = linearUnits.get(unitId);
            final Authority projectionAuthority = new EpsgAuthority(methodCode);
            final Projection projection = new Projection(methodName,
              projectionAuthority);
            Map<String, Object> parameters = getParameters(parametersString);
            List<Axis> axis = axisMap.get(axisId);
            Area area = areas.get(areaId);
            ProjectedCoordinateSystem coordinateSystem = new ProjectedCoordinateSystem(
              id, name, geographicCoordinateSystem, area, projection,
              parameters, linearUnit, axis, authority, deprecated);

            addCoordinateSystem(coordinateSystem);
          }
        }
      } finally {
        FileUtil.closeSilent(resource);
      }
    }
  }

  private static Map<String, Object> getParameters(String parametersString) {
    Map<String, Object> parameters = new TreeMap<String, Object>();
    Map<String, Object> jsonParams = JsonMapIoFactory.toObjectMap(parametersString);
    for (Entry<String, Object> parameter : jsonParams.entrySet()) {
      String key = parameter.getKey();
      Object value = parameter.getValue();
      if (value instanceof BigDecimal) {
        BigDecimal decimal = (BigDecimal)value;
        parameters.put(key, decimal.doubleValue());
      } else {
        parameters.put(key, value);
      }
    }
    return parameters;
  }

  private static Map<Integer, Area> loadAreas() {
    final Map<Integer, Area> areas = new HashMap<Integer, Area>();
    InputStream resource = EpsgCoordinateSystems.class.getResourceAsStream("/com/revolsys/gis/cs/epsg/area.csv");
    if (resource != null) {
      try {

        java.io.Reader reader = new InputStreamReader(resource);
        CsvIterator csv = new CsvIterator(reader);
        if (csv.hasNext()) {
          csv.next();
          while (csv.hasNext()) {
            List<String> values = csv.next();
            final Integer code = getInteger(values.get(0));
            final String name = getString(values.get(1));
            final Double minX = getDouble(values.get(2));
            final Double minY = getDouble(values.get(3));
            final Double maxX = getDouble(values.get(4));
            final Double maxY = getDouble(values.get(5));
            final boolean deprecated = Boolean.parseBoolean(values.get(6));
            final Authority authority = new EpsgAuthority(code);

            final Area area = new Area(name, new Envelope(minX, maxX, minY,
              maxY), authority, deprecated);
            areas.put(code, area);
          }
        }
      } finally {
        FileUtil.closeSilent(resource);
      }
    }
    return areas;

  }

  private static Map<Integer, List<Axis>> loadAxis() {
    final Map<Integer, List<Axis>> axisMap = new HashMap<Integer, List<Axis>>();
    InputStream resource = EpsgCoordinateSystems.class.getResourceAsStream("/com/revolsys/gis/cs/epsg/axis.csv");
    if (resource != null) {
      try {

        java.io.Reader reader = new InputStreamReader(resource);
        CsvIterator csv = new CsvIterator(reader);
        if (csv.hasNext()) {
          csv.next();
          while (csv.hasNext()) {
            List<String> values = csv.next();
            final Integer id = getInteger(values.get(0));
            List<Axis> axisList = new ArrayList<Axis>();
            for (int i = 1; i < values.size(); i += 2) {
              String name = values.get(i);
              if (StringUtils.hasText(name)) {
                String direction = values.get(i + 1);
                Axis axis = new Axis(name, direction);
                axisList.add(axis);
              }
            }
            axisMap.put(id, axisList);
          }
        }
      } finally {
        FileUtil.closeSilent(resource);
      }
    }
    return axisMap;

  }

  private static void addCoordinateSystem(
    final CoordinateSystem coordinateSystem) {
    Integer id = coordinateSystem.getId();
    String name = coordinateSystem.getName();
    coordinateSystemsById.put(id, coordinateSystem);
    coordinateSystemsByCoordinateSystem.put(coordinateSystem, coordinateSystem);
    coordinateSystemsByName.put(name, coordinateSystem);
  }

  private EpsgCoordinateSystems() {
  }
}
