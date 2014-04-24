package com.revolsys.gis.cs.epsg;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
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
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;

public final class EpsgCoordinateSystems {
  private static Set<CoordinateSystem> coordinateSystems;

  private static Map<CoordinateSystem, CoordinateSystem> coordinateSystemsByCoordinateSystem = new LinkedHashMap<CoordinateSystem, CoordinateSystem>();

  private static Map<Integer, CoordinateSystem> coordinateSystemsById = new TreeMap<Integer, CoordinateSystem>();

  private static Map<String, CoordinateSystem> coordinateSystemsByName = new TreeMap<String, CoordinateSystem>();

  private static boolean initialized = false;

  private static int nextSrid = 2000000;

  private static void addCoordinateSystem(
    final CoordinateSystem coordinateSystem) {
    final Integer id = coordinateSystem.getId();
    final String name = coordinateSystem.getName();
    coordinateSystemsById.put(id, coordinateSystem);
    coordinateSystemsByCoordinateSystem.put(coordinateSystem, coordinateSystem);
    coordinateSystemsByName.put(name, coordinateSystem);
  }

  public static void clear() {
    coordinateSystems = null;
    coordinateSystemsByCoordinateSystem.clear();
    coordinateSystemsById.clear();
    coordinateSystemsByName.clear();
  }

  public synchronized static CoordinateSystem getCoordinateSystem(
    final CoordinateSystem coordinateSystem) {
    initialize();
    if (coordinateSystem == null) {
      return null;
    } else {
      int srid = coordinateSystem.getId();
      CoordinateSystem coordinateSystem2 = coordinateSystemsById.get(srid);
      if (coordinateSystem2 == null) {
        coordinateSystem2 = coordinateSystemsByName.get(coordinateSystem.getName());
        if (coordinateSystem2 == null) {
          coordinateSystem2 = coordinateSystemsByCoordinateSystem.get(coordinateSystem);
          if (coordinateSystem2 == null) {
            if (srid <= 0) {
              srid = nextSrid++;
            }
            final String name = coordinateSystem.getName();
            final List<Axis> axis = coordinateSystem.getAxis();
            final Area area = coordinateSystem.getArea();
            final Authority authority = coordinateSystem.getAuthority();
            final boolean deprecated = coordinateSystem.isDeprecated();
            if (coordinateSystem instanceof GeographicCoordinateSystem) {
              final GeographicCoordinateSystem geographicCs = (GeographicCoordinateSystem)coordinateSystem;
              final Datum datum = geographicCs.getDatum();
              final PrimeMeridian primeMeridian = geographicCs.getPrimeMeridian();
              final AngularUnit angularUnit = geographicCs.getAngularUnit();
              final GeographicCoordinateSystem newCs = new GeographicCoordinateSystem(
                srid, name, datum, primeMeridian, angularUnit, axis, area,
                authority, deprecated);
              addCoordinateSystem(newCs);
              return newCs;
            } else if (coordinateSystem instanceof ProjectedCoordinateSystem) {
              final ProjectedCoordinateSystem projectedCs = (ProjectedCoordinateSystem)coordinateSystem;
              GeographicCoordinateSystem geographicCs = projectedCs.getGeographicCoordinateSystem();
              geographicCs = (GeographicCoordinateSystem)getCoordinateSystem(geographicCs);
              final Projection projection = projectedCs.getProjection();
              final Map<String, Object> parameters = projectedCs.getParameters();
              final LinearUnit linearUnit = projectedCs.getLinearUnit();
              final ProjectedCoordinateSystem newCs = new ProjectedCoordinateSystem(
                srid, name, geographicCs, area, projection, parameters,
                linearUnit, axis, authority, deprecated);
              addCoordinateSystem(newCs);
              return newCs;
            }
            return coordinateSystem;
          }
        }
      }
      return coordinateSystem2;
    }
  }

  @SuppressWarnings("unchecked")
  public static <C extends CoordinateSystem> C getCoordinateSystem(
    final Geometry geometry) {
    return (C)getCoordinateSystem(geometry.getSrid());
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

  public static List<GeographicCoordinateSystem> getGeographicCoordinateSystems() {
    final List<GeographicCoordinateSystem> coordinateSystems = new ArrayList<GeographicCoordinateSystem>();
    for (final CoordinateSystem coordinateSystem : coordinateSystemsByName.values()) {
      if (coordinateSystem instanceof GeographicCoordinateSystem) {
        final GeographicCoordinateSystem geographicCoordinateSystem = (GeographicCoordinateSystem)coordinateSystem;
        coordinateSystems.add(geographicCoordinateSystem);
      }
    }
    return coordinateSystems;
  }

  private static Integer getInteger(final String value) {
    if (value == null || value.equals("")) {
      return null;
    } else {
      return Integer.valueOf(value);
    }
  }

  private static Map<String, Object> getParameters(final String parametersString) {
    final Map<String, Object> parameters = new TreeMap<String, Object>();
    final Map<String, Object> jsonParams = JsonMapIoFactory.toObjectMap(parametersString);
    for (final Entry<String, Object> parameter : jsonParams.entrySet()) {
      final String key = parameter.getKey();
      final Object value = parameter.getValue();
      if (value instanceof BigDecimal) {
        final BigDecimal decimal = (BigDecimal)value;
        parameters.put(key, decimal.doubleValue());
      } else {
        parameters.put(key, value);
      }
    }
    return parameters;
  }

  public static List<ProjectedCoordinateSystem> getProjectedCoordinateSystems() {
    final List<ProjectedCoordinateSystem> coordinateSystems = new ArrayList<ProjectedCoordinateSystem>();
    for (final CoordinateSystem coordinateSystem : coordinateSystemsByName.values()) {
      if (coordinateSystem instanceof ProjectedCoordinateSystem) {
        final ProjectedCoordinateSystem projectedCoordinateSystem = (ProjectedCoordinateSystem)coordinateSystem;
        coordinateSystems.add(projectedCoordinateSystem);
      }
    }
    return coordinateSystems;
  }

  private static String getString(final String string) {
    return new String(string);
  }

  public synchronized static void initialize() {
    if (!initialized) {
      try {
        final Map<Integer, List<Axis>> axisMap = loadAxis();
        final Map<Integer, Area> areas = loadAreas();
        final Map<Integer, AngularUnit> angularUnits = loadAngularUnits();
        final Map<Integer, LinearUnit> linearUnits = loadLinearUnits();
        loadGeographicCoordinateSystems(angularUnits, axisMap, areas);
        loadProjectedCoordinateSystems(axisMap, areas, linearUnits);
        coordinateSystems = Collections.unmodifiableSet(new LinkedHashSet<CoordinateSystem>(
          coordinateSystemsByCoordinateSystem.values()));
        initialized = true;
      } catch (final Throwable t) {
        t.printStackTrace();
      }
    }
  }

  private static Map<Integer, AngularUnit> loadAngularUnits() {
    final Map<Integer, AngularUnit> angularUnits = new LinkedHashMap<Integer, AngularUnit>();
    final InputStream resource = EpsgCoordinateSystems.class.getResourceAsStream("/com/revolsys/gis/cs/epsg/angularunit.csv");
    if (resource != null) {
      try {

        final java.io.Reader reader = FileUtil.createUtf8Reader(resource);
        final CsvIterator csv = new CsvIterator(reader);
        if (csv.hasNext()) {
          csv.next();
          while (csv.hasNext()) {
            final List<String> values = csv.next();
            final int id = getInteger(values.get(0));
            final String name = values.get(1);
            final Integer baseId = getInteger(values.get(2));
            final double conversionFactor = getDouble(values.get(3));
            final boolean deprecated = Boolean.parseBoolean(values.get(4));
            final AngularUnit baseUnit = angularUnits.get(baseId);
            final EpsgAuthority authority = new EpsgAuthority(id);
            final AngularUnit unit = new AngularUnit(name, baseUnit,
              conversionFactor, authority, deprecated);
            angularUnits.put(id, unit);
          }
        }
      } finally {
        FileUtil.closeSilent(resource);
      }
    }
    return angularUnits;
  }

  private static Map<Integer, Area> loadAreas() {
    final Map<Integer, Area> areas = new LinkedHashMap<Integer, Area>();
    final InputStream resource = EpsgCoordinateSystems.class.getResourceAsStream("/com/revolsys/gis/cs/epsg/area.csv");
    if (resource != null) {
      try {

        final java.io.Reader reader = FileUtil.createUtf8Reader(resource);
        final CsvIterator csv = new CsvIterator(reader);
        if (csv.hasNext()) {
          csv.next();
          while (csv.hasNext()) {
            final List<String> values = csv.next();
            final Integer code = getInteger(values.get(0));
            final String name = getString(values.get(1));
            final Double minX = getDouble(values.get(2));
            final Double minY = getDouble(values.get(3));
            final Double maxX = getDouble(values.get(4));
            final Double maxY = getDouble(values.get(5));
            final boolean deprecated = Boolean.parseBoolean(values.get(6));
            final Authority authority = new EpsgAuthority(code);

            final Area area = new Area(name, new Envelope(2, minX, minY,
              maxX, maxY), authority, deprecated);
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
    final Map<Integer, List<Axis>> axisMap = new LinkedHashMap<Integer, List<Axis>>();
    final InputStream resource = EpsgCoordinateSystems.class.getResourceAsStream("/com/revolsys/gis/cs/epsg/axis.csv");
    if (resource != null) {
      try {

        final java.io.Reader reader = FileUtil.createUtf8Reader(resource);
        final CsvIterator csv = new CsvIterator(reader);
        if (csv.hasNext()) {
          csv.next();
          while (csv.hasNext()) {
            final List<String> values = csv.next();
            final Integer id = getInteger(values.get(0));
            final List<Axis> axisList = new ArrayList<Axis>();
            for (int i = 1; i < values.size(); i += 2) {
              final String name = values.get(i);
              if (StringUtils.hasText(name)) {
                final String direction = values.get(i + 1);
                final Axis axis = new Axis(name, direction);
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

  private static Map<Integer, Datum> loadDatums() {
    final Map<Integer, Spheroid> spheroids = loadSpheroids();
    final Map<Integer, PrimeMeridian> primeMeridians = loadPrimeMeridians();
    final Map<Integer, Datum> datums = new LinkedHashMap<Integer, Datum>();
    final InputStream resource = EpsgCoordinateSystems.class.getResourceAsStream("/com/revolsys/gis/cs/epsg/datum.csv");
    if (resource != null) {
      try {
        final java.io.Reader reader = FileUtil.createUtf8Reader(resource);
        final CsvIterator csv = new CsvIterator(reader);
        if (csv.hasNext()) {
          csv.next();
          while (csv.hasNext()) {
            final List<String> values = csv.next();
            final int id = Integer.parseInt(values.get(0));
            final String name = values.get(1);
            final int spheroidId = Integer.parseInt(values.get(2));
            final int primeMeridianId = Integer.parseInt(values.get(3));
            final boolean deprecated = Boolean.parseBoolean(values.get(4));
            final Spheroid spheroid = spheroids.get(spheroidId);
            final PrimeMeridian primeMeridian = primeMeridians.get(primeMeridianId);
            final EpsgAuthority authority = new EpsgAuthority(id);
            final Datum datum = new Datum(name, spheroid, primeMeridian,
              authority, deprecated);
            datums.put(id, datum);
          }
        }
      } finally {
        FileUtil.closeSilent(resource);
      }
    }
    return datums;
  }

  private static void loadGeographicCoordinateSystems(
    final Map<Integer, AngularUnit> angularUnits,
    final Map<Integer, List<Axis>> axisMap, final Map<Integer, Area> areas) {
    final Map<Integer, Datum> datums = loadDatums();
    final InputStream resource = EpsgCoordinateSystems.class.getResourceAsStream("/com/revolsys/gis/cs/epsg/geographic.csv");
    if (resource != null) {
      try {
        final java.io.Reader reader = FileUtil.createUtf8Reader(resource);
        final CsvIterator csv = new CsvIterator(reader);
        if (csv.hasNext()) {
          csv.next();
          while (csv.hasNext()) {
            final List<String> values = csv.next();
            final int id = Integer.parseInt(values.get(0));
            final String name = values.get(1);
            final Integer datumId = getInteger(values.get(2));
            final Integer unitId = getInteger(values.get(3));
            final Integer axisId = getInteger(values.get(4));
            final Integer areaId = getInteger(values.get(5));
            final boolean deprecated = Boolean.parseBoolean(values.get(6));
            final Datum datum = datums.get(datumId);
            final EpsgAuthority authority = new EpsgAuthority(id);
            final AngularUnit angularUnit = angularUnits.get(unitId);
            final List<Axis> axis = axisMap.get(axisId);
            final Area area = areas.get(areaId);
            final GeographicCoordinateSystem coordinateSystem = new GeographicCoordinateSystem(
              id, name, datum, angularUnit, axis, area, authority, deprecated);
            addCoordinateSystem(coordinateSystem);
          }
        }
      } finally {
        FileUtil.closeSilent(resource);
      }
    }
  }

  private static Map<Integer, LinearUnit> loadLinearUnits() {
    final Map<Integer, LinearUnit> linearUnits = new LinkedHashMap<Integer, LinearUnit>();
    final InputStream resource = EpsgCoordinateSystems.class.getResourceAsStream("/com/revolsys/gis/cs/epsg/linearunit.csv");
    if (resource != null) {
      try {
        final java.io.Reader reader = FileUtil.createUtf8Reader(resource);
        final CsvIterator csv = new CsvIterator(reader);
        if (csv.hasNext()) {
          csv.next();
          while (csv.hasNext()) {
            final List<String> values = csv.next();
            final int id = getInteger(values.get(0));
            final String name = values.get(1);
            final Integer baseId = getInteger(values.get(2));
            final double conversionFactor = getDouble(values.get(3));
            final boolean deprecated = Boolean.parseBoolean(values.get(4));
            final LinearUnit baseUnit = linearUnits.get(baseId);
            final EpsgAuthority authority = new EpsgAuthority(id);
            final LinearUnit unit = new LinearUnit(name, baseUnit,
              conversionFactor, authority, deprecated);
            linearUnits.put(id, unit);
          }
        }
      } finally {
        FileUtil.closeSilent(resource);
      }
    }
    return linearUnits;
  }

  private static Map<Integer, PrimeMeridian> loadPrimeMeridians() {
    final Map<Integer, PrimeMeridian> primeMeridians = new LinkedHashMap<Integer, PrimeMeridian>();
    final InputStream resource = EpsgCoordinateSystems.class.getResourceAsStream("/com/revolsys/gis/cs/epsg/primemeridian.csv");
    if (resource != null) {
      try {
        final java.io.Reader reader = FileUtil.createUtf8Reader(resource);
        final CsvIterator csv = new CsvIterator(reader);
        if (csv.hasNext()) {
          csv.next();
          while (csv.hasNext()) {
            final List<String> values = csv.next();
            final int id = Integer.parseInt(values.get(0));
            final String name = values.get(1);
            final double longitude = getDouble(values.get(2));
            final boolean deprecated = Boolean.parseBoolean(values.get(3));
            final EpsgAuthority authority = new EpsgAuthority(id);
            final PrimeMeridian primeMeridian = new PrimeMeridian(name,
              longitude, authority, deprecated);
            primeMeridians.put(id, primeMeridian);
          }
        }
      } finally {
        FileUtil.closeSilent(resource);
      }
    }
    return primeMeridians;
  }

  private static void loadProjectedCoordinateSystems(
    final Map<Integer, List<Axis>> axisMap, final Map<Integer, Area> areas,
    final Map<Integer, LinearUnit> linearUnits) {
    final InputStream resource = EpsgCoordinateSystems.class.getResourceAsStream("/com/revolsys/gis/cs/epsg/projected.csv");
    if (resource != null) {
      try {
        final java.io.Reader reader = FileUtil.createUtf8Reader(resource);
        final CsvIterator csv = new CsvIterator(reader);
        if (csv.hasNext()) {
          csv.next();
          while (csv.hasNext()) {
            final List<String> values = csv.next();
            final int id = Integer.parseInt(values.get(0));
            final String name = values.get(1);
            final Integer geoCsId = getInteger(values.get(2));
            final Integer unitId = getInteger(values.get(3));
            final Integer methodCode = getInteger(values.get(4));
            final String methodName = values.get(5);
            final String parametersString = values.get(6);
            final Integer axisId = getInteger(values.get(7));
            final Integer areaId = getInteger(values.get(8));
            final boolean deprecated = Boolean.parseBoolean(values.get(9));
            final CoordinateSystem referencedCoordinateSystem = coordinateSystemsById.get(geoCsId);
            if (referencedCoordinateSystem instanceof GeographicCoordinateSystem) {
              final GeographicCoordinateSystem geographicCoordinateSystem = (GeographicCoordinateSystem)referencedCoordinateSystem;
              EpsgAuthority authority = new EpsgAuthority(id);
              final LinearUnit linearUnit = linearUnits.get(unitId);
              final Authority projectionAuthority = new EpsgAuthority(
                methodCode);
              final Projection projection = new Projection(methodName,
                projectionAuthority);
              final Map<String, Object> parameters = getParameters(parametersString);
              final List<Axis> axis = axisMap.get(axisId);
              final Area area = areas.get(areaId);
              final ProjectedCoordinateSystem coordinateSystem = new ProjectedCoordinateSystem(
                id, name, geographicCoordinateSystem, area, projection,
                parameters, linearUnit, axis, authority, deprecated);

              addCoordinateSystem(coordinateSystem);
              if (id == 3857) {
                authority = new EpsgAuthority(102100);
                final ProjectedCoordinateSystem webMercator = new ProjectedCoordinateSystem(
                  102100, name, geographicCoordinateSystem, area, projection,
                  parameters, linearUnit, axis, authority, deprecated);

                addCoordinateSystem(webMercator);
              }
            }
          }
        }
      } finally {
        FileUtil.closeSilent(resource);
      }
    }
  }

  private static Map<Integer, Spheroid> loadSpheroids() {
    final Map<Integer, Spheroid> spheroids = new LinkedHashMap<Integer, Spheroid>();
    final InputStream resource = EpsgCoordinateSystems.class.getResourceAsStream("/com/revolsys/gis/cs/epsg/spheroid.csv");
    if (resource != null) {
      try {

        final java.io.Reader reader = FileUtil.createUtf8Reader(resource);
        final CsvIterator csv = new CsvIterator(reader);
        if (csv.hasNext()) {
          csv.next();
          while (csv.hasNext()) {
            final List<String> values = csv.next();
            final int id = Integer.parseInt(values.get(0));
            final String name = values.get(1);
            final double semiMajorAxis = getDouble(values.get(2));
            final double semiMinorAxis = getDouble(values.get(3));
            final double inverseFlattening = getDouble(values.get(4));
            final boolean deprecated = Boolean.parseBoolean(values.get(5));
            final EpsgAuthority authority = new EpsgAuthority(id);
            final Spheroid spheroid = new Spheroid(name, semiMajorAxis,
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

  public static CoordinateSystem wgs84() {
    return EpsgCoordinateSystems.<CoordinateSystem> getCoordinateSystem(4326);
  }

  private EpsgCoordinateSystems() {
  }
}
