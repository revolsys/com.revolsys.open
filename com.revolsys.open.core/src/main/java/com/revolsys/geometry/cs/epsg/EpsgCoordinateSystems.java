package com.revolsys.geometry.cs.epsg;

import java.io.EOFException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.revolsys.collection.map.IntHashMap;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.cs.Area;
import com.revolsys.geometry.cs.Authority;
import com.revolsys.geometry.cs.Axis;
import com.revolsys.geometry.cs.AxisName;
import com.revolsys.geometry.cs.CompoundCoordinateSystem;
import com.revolsys.geometry.cs.CoordinateOperationMethod;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.CoordinateSystemType;
import com.revolsys.geometry.cs.Ellipsoid;
import com.revolsys.geometry.cs.EngineeringCoordinateSystem;
import com.revolsys.geometry.cs.GeocentricCoordinateSystem;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.HorizontalCoordinateSystem;
import com.revolsys.geometry.cs.ParameterName;
import com.revolsys.geometry.cs.ParameterNames;
import com.revolsys.geometry.cs.ParameterValue;
import com.revolsys.geometry.cs.ParameterValueNumber;
import com.revolsys.geometry.cs.ParameterValueString;
import com.revolsys.geometry.cs.PrimeMeridian;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.VerticalCoordinateSystem;
import com.revolsys.geometry.cs.datum.Datum;
import com.revolsys.geometry.cs.datum.EngineeringDatum;
import com.revolsys.geometry.cs.datum.GeodeticDatum;
import com.revolsys.geometry.cs.datum.VerticalDatum;
import com.revolsys.geometry.cs.unit.AngularUnit;
import com.revolsys.geometry.cs.unit.Degree;
import com.revolsys.geometry.cs.unit.DegreeSexagesimalDMS;
import com.revolsys.geometry.cs.unit.Grad;
import com.revolsys.geometry.cs.unit.LinearUnit;
import com.revolsys.geometry.cs.unit.Metre;
import com.revolsys.geometry.cs.unit.Radian;
import com.revolsys.geometry.cs.unit.ScaleUnit;
import com.revolsys.geometry.cs.unit.TimeUnit;
import com.revolsys.geometry.cs.unit.UnitOfMeasure;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.logging.Logs;
import com.revolsys.spring.resource.ClassPathResource;
import com.revolsys.spring.resource.NoSuchResourceException;
import com.revolsys.util.Dates;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Property;
import com.revolsys.util.WrappedException;

public final class EpsgCoordinateSystems {

  private static final IntHashMap<Area> AREA_BY_ID = new IntHashMap<>();

  private static final Map<String, AxisName> AXIS_NAME_BY_NAME = new HashMap<>();

  private static final IntHashMap<AxisName> AXIS_NAMES = new IntHashMap<>();

  private static final IntHashMap<CoordinateSystem> COORDINATE_SYSTEM_BY_ID = new IntHashMap<>();

  private static final Map<String, CoordinateSystem> COORDINATE_SYSTEM_BY_NAME = new TreeMap<>();

  private static final IntHashMap<CoordinateSystemType> COORDINATE_SYSTEM_TYPE_BY_ID = new IntHashMap<>();

  private static final IntHashMap<List<CoordinateSystem>> COORDINATE_SYSTEMS_BY_HASH_CODE = new IntHashMap<>();

  private static Set<CoordinateSystem> coordinateSystems;

  private static final IntHashMap<Datum> DATUM_BY_ID = new IntHashMap<>();

  private static final EpsgCoordinateSystems INSTANCE = new EpsgCoordinateSystems();

  private static boolean initialized = false;

  private static int nextSrid = 2000000;

  private static final IntHashMap<CoordinateOperation> OPERATION_BY_ID = new IntHashMap<>();

  private static final IntHashMap<ParameterName> PARAM_NAME_BY_ID = new IntHashMap<>();

  private static final IntHashMap<PrimeMeridian> PRIME_MERIDIAN_BY_ID = new IntHashMap<>();;

  private static final EpsgSystemOfUnits SYSTEM_OF_UNITS = new EpsgSystemOfUnits();

  private static final IntHashMap<UnitOfMeasure> UNIT_BY_ID = new IntHashMap<>();

  private static final Map<String, UnitOfMeasure> UNIT_BY_NAME = new TreeMap<>();

  private static void addCoordinateSystem(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem != null) {
      final Integer id = coordinateSystem.getCoordinateSystemId();
      final String name = coordinateSystem.getCoordinateSystemName();
      COORDINATE_SYSTEM_BY_ID.put(id, coordinateSystem);
      final int hashCode = coordinateSystem.hashCode();
      List<CoordinateSystem> coordinateSystems = COORDINATE_SYSTEMS_BY_HASH_CODE.get(hashCode);
      if (coordinateSystems == null) {
        coordinateSystems = new ArrayList<>();
        COORDINATE_SYSTEMS_BY_HASH_CODE.put(hashCode, coordinateSystems);
      }
      coordinateSystems.add(coordinateSystem);
      COORDINATE_SYSTEM_BY_NAME.put(name, coordinateSystem);
    }
  }

  public static synchronized void clear() {
    initialized = false;
    coordinateSystems = null;
    COORDINATE_SYSTEMS_BY_HASH_CODE.clear();
    COORDINATE_SYSTEM_BY_ID.clear();
    COORDINATE_SYSTEM_BY_NAME.clear();
  }

  public static AxisName getAxisName(final String name) {
    if (name == null) {
      return null;
    } else {
      final AxisName axisName = AXIS_NAME_BY_NAME.get(name.toLowerCase());
      if (axisName == null) {
        return new AxisName(0, name);
      } else {
        return axisName;
      }
    }
  }

  private static <V> V getCode(final IntHashMap<V> valueById, final int id) {
    if (id == 0) {
      return null;
    } else {
      final V value = valueById.get(id);
      if (value == null) {
        throw new IllegalArgumentException("Invalid code for id=" + id);
      }
      return value;
    }
  }

  public static CompoundCoordinateSystem getCompound(final int horizontalCoordinateSystemId,
    final int verticalCoordinateSystemId) {
    final HorizontalCoordinateSystem horizontalCoordinateSystem = getCoordinateSystem(
      horizontalCoordinateSystemId);
    final VerticalCoordinateSystem verticalCoordinateSystem = getCoordinateSystem(
      verticalCoordinateSystemId);
    if (horizontalCoordinateSystem == null) {
      throw new IllegalArgumentException(
        "horizontalCoordinateSystemId=" + horizontalCoordinateSystemId + " doesn't exist");
    }
    if (verticalCoordinateSystem == null) {
      throw new IllegalArgumentException(
        "verticalCoordinateSystemId=" + verticalCoordinateSystemId + " doesn't exist");
    }
    final CompoundCoordinateSystem compoundCoordinateSystem = new CompoundCoordinateSystem(
      horizontalCoordinateSystem, verticalCoordinateSystem);
    return getCoordinateSystem(compoundCoordinateSystem);
  }

  @SuppressWarnings("unchecked")
  public synchronized static <C extends CoordinateSystem> C getCoordinateSystem(
    final C coordinateSystem) {
    initialize();
    if (coordinateSystem == null) {
      return null;
    } else {
      int srid = coordinateSystem.getCoordinateSystemId();
      CoordinateSystem matchedCoordinateSystem = COORDINATE_SYSTEM_BY_ID.get(srid);
      if (matchedCoordinateSystem == null) {
        matchedCoordinateSystem = COORDINATE_SYSTEM_BY_NAME
          .get(coordinateSystem.getCoordinateSystemName());
        if (matchedCoordinateSystem == null) {
          final int hashCode = coordinateSystem.hashCode();
          int matchCoordinateSystemId = 0;
          final List<CoordinateSystem> coordinateSystems = COORDINATE_SYSTEMS_BY_HASH_CODE
            .get(hashCode);
          if (coordinateSystems != null) {
            for (final CoordinateSystem coordinateSystem3 : coordinateSystems) {
              if (coordinateSystem3.equals(coordinateSystem)) {
                final int srid3 = coordinateSystem3.getCoordinateSystemId();
                if (matchedCoordinateSystem == null) {
                  matchedCoordinateSystem = coordinateSystem3;
                  matchCoordinateSystemId = srid3;
                } else if (srid3 < matchCoordinateSystemId) {
                  if (!coordinateSystem3.isDeprecated() || matchedCoordinateSystem.isDeprecated()) {
                    matchedCoordinateSystem = coordinateSystem3;
                    matchCoordinateSystemId = srid3;
                  }
                }
              }
            }
          }

          if (matchedCoordinateSystem == null) {
            if (srid <= 0) {
              srid = nextSrid++;
            }
            final String name = coordinateSystem.getCoordinateSystemName();
            final List<Axis> axis = coordinateSystem.getAxis();
            final Area area = coordinateSystem.getArea();
            final Authority authority = coordinateSystem.getAuthority();
            final boolean deprecated = coordinateSystem.isDeprecated();
            if (coordinateSystem instanceof GeographicCoordinateSystem) {
              final GeographicCoordinateSystem geographicCs = (GeographicCoordinateSystem)coordinateSystem;
              final GeodeticDatum geodeticDatum = geographicCs.getDatum();
              final PrimeMeridian primeMeridian = geographicCs.getPrimeMeridian();
              final CoordinateSystem sourceCoordinateSystem = geographicCs
                .getSourceCoordinateSystem();
              final CoordinateOperation coordinateOperation = geographicCs.getCoordinateOperation();
              final GeographicCoordinateSystem newCs = new GeographicCoordinateSystem(srid, name,
                geodeticDatum, primeMeridian, axis, area, sourceCoordinateSystem,
                coordinateOperation, deprecated);
              addCoordinateSystem(newCs);
              return (C)newCs;
            } else if (coordinateSystem instanceof ProjectedCoordinateSystem) {
              final ProjectedCoordinateSystem projectedCs = (ProjectedCoordinateSystem)coordinateSystem;
              GeographicCoordinateSystem geographicCs = projectedCs.getGeographicCoordinateSystem();
              geographicCs = getCoordinateSystem(geographicCs);
              final CoordinateOperationMethod coordinateOperationMethod = projectedCs
                .getCoordinateOperationMethod();
              final Map<ParameterName, ParameterValue> parameters = projectedCs
                .getParameterValues();
              final LinearUnit linearUnit = projectedCs.getLinearUnit();
              final ProjectedCoordinateSystem newCs = new ProjectedCoordinateSystem(srid, name,
                geographicCs, area, coordinateOperationMethod, parameters, linearUnit, axis,
                authority, deprecated);
              addCoordinateSystem(newCs);
              return (C)newCs;
            }
            return coordinateSystem;
          }
        }
      }
      return (C)matchedCoordinateSystem;
    }
  }

  @SuppressWarnings("unchecked")
  public static <C extends CoordinateSystem> C getCoordinateSystem(final Geometry geometry) {
    return (C)getCoordinateSystem(geometry.getCoordinateSystemId());
  }

  @SuppressWarnings("unchecked")
  public static <C extends CoordinateSystem> C getCoordinateSystem(final int crsId) {
    if (crsId > 0) {
      initialize();
      return (C)COORDINATE_SYSTEM_BY_ID.get(crsId);
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public static <C extends CoordinateSystem> C getCoordinateSystem(final String name) {
    initialize();
    return (C)COORDINATE_SYSTEM_BY_NAME.get(name);
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
    final List<CoordinateSystem> coordinateSystems = new ArrayList<>();
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
    return new TreeMap<>(COORDINATE_SYSTEM_BY_ID);
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
    if (authority != null) {
      final String name = authority.getName();
      final String code = authority.getCode();
      if (name.equals("EPSG")) {
        return name + ":" + code;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static <D extends Datum> D getDatum(final int id) {
    initialize();
    return (D)DATUM_BY_ID.get(id);
  }

  public static List<GeographicCoordinateSystem> getGeographicCoordinateSystems() {
    final List<GeographicCoordinateSystem> coordinateSystems = new ArrayList<>();
    for (final CoordinateSystem coordinateSystem : COORDINATE_SYSTEM_BY_NAME.values()) {
      if (coordinateSystem instanceof GeographicCoordinateSystem) {
        final GeographicCoordinateSystem geographicCoordinateSystem = (GeographicCoordinateSystem)coordinateSystem;
        coordinateSystems.add(geographicCoordinateSystem);
      }
    }
    return coordinateSystems;
  }

  public static HorizontalCoordinateSystem getHorizontalCoordinateSystem(final int crsId) {
    final CoordinateSystem coordinateSystem = getCoordinateSystem(crsId);
    if (coordinateSystem instanceof HorizontalCoordinateSystem) {
      return (HorizontalCoordinateSystem)coordinateSystem;
    } else {
      return null;
    }
  }

  public static List<HorizontalCoordinateSystem> getHorizontalCoordinateSystems() {
    final List<HorizontalCoordinateSystem> coordinateSystems = new ArrayList<>();
    for (final CoordinateSystem coordinateSystem : COORDINATE_SYSTEM_BY_NAME.values()) {
      if (coordinateSystem instanceof HorizontalCoordinateSystem) {
        final HorizontalCoordinateSystem projectedCoordinateSystem = (HorizontalCoordinateSystem)coordinateSystem;
        coordinateSystems.add(projectedCoordinateSystem);
      }
    }
    return coordinateSystems;
  }

  @SuppressWarnings("unchecked")
  public static <U extends UnitOfMeasure> U getLinearUnit(final String name) {
    initialize();
    return (U)UNIT_BY_NAME.get(name);
  }

  public static List<ProjectedCoordinateSystem> getProjectedCoordinateSystems() {
    final List<ProjectedCoordinateSystem> coordinateSystems = new ArrayList<>();
    for (final CoordinateSystem coordinateSystem : COORDINATE_SYSTEM_BY_NAME.values()) {
      if (coordinateSystem instanceof ProjectedCoordinateSystem) {
        final ProjectedCoordinateSystem projectedCoordinateSystem = (ProjectedCoordinateSystem)coordinateSystem;
        coordinateSystems.add(projectedCoordinateSystem);
      }
    }
    return coordinateSystems;
  }

  @SuppressWarnings("unchecked")
  public static <U extends UnitOfMeasure> U getUnit(final int id) {
    initialize();
    return (U)UNIT_BY_ID.get(id);
  }

  public static VerticalCoordinateSystem getVerticalCoordinateSystem(final int crsId) {
    final CoordinateSystem coordinateSystem = getCoordinateSystem(crsId);
    if (coordinateSystem instanceof VerticalCoordinateSystem) {
      return (VerticalCoordinateSystem)coordinateSystem;
    } else {
      return null;
    }
  }

  public static List<VerticalCoordinateSystem> getVerticalCoordinateSystems() {
    final List<VerticalCoordinateSystem> coordinateSystems = new ArrayList<>();
    for (final CoordinateSystem coordinateSystem : COORDINATE_SYSTEM_BY_NAME.values()) {
      if (coordinateSystem instanceof VerticalCoordinateSystem) {
        final VerticalCoordinateSystem projectedCoordinateSystem = (VerticalCoordinateSystem)coordinateSystem;
        coordinateSystems.add(projectedCoordinateSystem);
      }
    }
    return coordinateSystems;
  }

  public synchronized static void initialize() {
    if (!initialized) {
      initialized = true;
      final long startTime = System.currentTimeMillis();
      try {
        loadUnitOfMeasure();
        loadCoordinateAxisNames();
        final IntHashMap<List<Axis>> axisMap = loadCoordinateAxis();
        loadArea();
        loadPrimeMeridians();
        loadDatum();
        loadCoordOperationParam();
        final IntHashMap<List<ParameterName>> paramOrderByMethodId = new IntHashMap<>();
        final IntHashMap<List<Byte>> paramReversalByMethodId = new IntHashMap<>();
        loadCoordOperationParamUsage(paramOrderByMethodId, paramReversalByMethodId);
        final IntHashMap<CoordinateOperationMethod> methodById = loadCoordOperationMethod(
          paramOrderByMethodId, paramReversalByMethodId);
        final IntHashMap<Map<ParameterName, ParameterValue>> operationParameters = new IntHashMap<>();
        loadCoordOperationParamValue(methodById, operationParameters, paramReversalByMethodId);
        loadCoordOperation(methodById, operationParameters, paramReversalByMethodId);
        loadCoordinateSystem();
        loadCoordinateReferenceSystem(axisMap);

        final ProjectedCoordinateSystem worldMercator = (ProjectedCoordinateSystem)COORDINATE_SYSTEM_BY_ID
          .get(3857);
        COORDINATE_SYSTEM_BY_ID.put(900913, worldMercator);
        coordinateSystems = Collections
          .unmodifiableSet(new LinkedHashSet<>(COORDINATE_SYSTEM_BY_ID.values()));
        Dates.debugEllapsedTime(EpsgCoordinateSystems.class, "initialize", startTime);
      } catch (final Throwable t) {
        t.printStackTrace();
      }
    }
  }

  private static void loadArea() {
    try (
      ChannelReader reader = newChannelReader("area")) {
      while (true) {
        final int code = reader.getInt();
        final String name = reader.getStringUtf8ByteCount();
        double minX = reader.getDouble();
        final double minY = reader.getDouble();
        final double maxX = reader.getDouble();
        final double maxY = reader.getDouble();
        final boolean deprecated = readBoolean(reader);
        final Authority authority = new EpsgAuthority(code);

        if (minX > maxX) {
          if (minX > 0) {
            minX -= 360;
          }
        }
        BoundingBox boundingBox;
        if (Double.isFinite(minX) || Double.isFinite(minX) || Double.isFinite(minX)
          || Double.isFinite(minX)) {
          boundingBox = new BoundingBoxDoubleXY(minX, minY, maxX, maxY);
        } else {
          boundingBox = BoundingBox.empty();
        }
        final Area area = new Area(name, boundingBox, authority, deprecated);
        AREA_BY_ID.put(code, area);
      }
    } catch (final NoSuchResourceException e) {
    } catch (final WrappedException e) {
      if (Exceptions.isException(e, EOFException.class)) {
      } else {
        throw e;
      }
    }
  }

  private static IntHashMap<List<Axis>> loadCoordinateAxis() {
    final IntHashMap<List<Axis>> axisesByCoordinateSystemId = new IntHashMap<>();
    try (
      ChannelReader reader = newChannelReader("coordinateAxis")) {
      while (true) {
        final int coordinateSystemId = reader.getInt();
        final AxisName axisName = readCode(reader, AXIS_NAMES);
        final String orientation = reader.getStringUtf8ByteCount();
        final Character abbreviation = (char)reader.getByte();
        final UnitOfMeasure unitOfMeasure = readCode(reader, UNIT_BY_ID);

        final Axis axis = new Axis(axisName, orientation, abbreviation.toString(), unitOfMeasure);
        List<Axis> axises = axisesByCoordinateSystemId.get(coordinateSystemId);
        if (axises == null) {
          axises = new ArrayList<>();
          axisesByCoordinateSystemId.put(coordinateSystemId, axises);
        }
        axises.add(axis);
      }
    } catch (final NoSuchResourceException e) {
    } catch (final WrappedException e) {
      if (Exceptions.isException(e, EOFException.class)) {
      } else {
        throw e;
      }
    }
    return axisesByCoordinateSystemId;
  }

  private static void loadCoordinateAxisNames() {
    try (
      ChannelReader reader = newChannelReader("coordinateAxisName")) {
      while (true) {
        final int code = reader.getInt();
        final String name = reader.getStringUtf8ByteCount();

        final AxisName axisName = new AxisName(code, name);
        AXIS_NAMES.putInt(code, axisName);
        AXIS_NAME_BY_NAME.put(name.toLowerCase(), axisName);
      }
    } catch (final NoSuchResourceException e) {
    } catch (final WrappedException e) {
      if (Exceptions.isException(e, EOFException.class)) {
      } else {
        throw e;
      }
    }
  }

  private static void loadCoordinateReferenceSystem(final IntHashMap<List<Axis>> axisMap) {
    try (
      ChannelReader reader = newChannelReader("coordinateReferenceSystem")) {
      while (true) {
        final int id = reader.getInt();
        final String name = reader.getStringUtf8ByteCount();
        final Area area = readCode(reader, AREA_BY_ID);
        final int type = reader.getByte();
        final CoordinateSystemType coordinateSystemType = readCode(reader,
          COORDINATE_SYSTEM_TYPE_BY_ID);
        final Datum datum = readCode(reader, DATUM_BY_ID);
        final CoordinateSystem sourceCoordinateSystem = readCode(reader, COORDINATE_SYSTEM_BY_ID);

        final CoordinateOperation operation = readCode(reader, OPERATION_BY_ID);

        final HorizontalCoordinateSystem horizontalCoordinateSystem = (HorizontalCoordinateSystem)readCode(
          reader, COORDINATE_SYSTEM_BY_ID);
        final VerticalCoordinateSystem verticalCoordinateSystem = (VerticalCoordinateSystem)readCode(
          reader, COORDINATE_SYSTEM_BY_ID);
        final boolean deprecated = readBoolean(reader);
        final List<Axis> axis;
        if (coordinateSystemType == null) {
          axis = null;
        } else {
          axis = axisMap.get(coordinateSystemType.getId());
        }
        CoordinateSystem coordinateSystem = null;
        if (type == 0) {
          // geocentric
          coordinateSystem = newCoordinateSystemGeocentric(id, name, datum, axis, area, deprecated);
        } else if (type == 1) {
          // geographic 3D
          coordinateSystem = new GeographicCoordinateSystem(id, name, (GeodeticDatum)datum, axis,
            area, sourceCoordinateSystem, operation, deprecated);
        } else if (type == 2) {
          // geographic 2D
          coordinateSystem = new GeographicCoordinateSystem(id, name, (GeodeticDatum)datum, axis,
            area, sourceCoordinateSystem, operation, deprecated);
        } else if (type == 3) {
          // projected
          coordinateSystem = newCoordinateSystemProjected(id, name, area, sourceCoordinateSystem,
            operation, axis, deprecated);
        } else if (type == 4) {
          // engineering
          coordinateSystem = new EngineeringCoordinateSystem(id, name, (EngineeringDatum)datum,
            axis, area, deprecated);
        } else if (type == 5) {
          // vertical
          coordinateSystem = new VerticalCoordinateSystem(id, name, (VerticalDatum)datum, axis,
            area, deprecated);
        } else if (type == 6) {
          coordinateSystem = new CompoundCoordinateSystem(id, name, horizontalCoordinateSystem,
            verticalCoordinateSystem, area, deprecated);
        } else {
          coordinateSystem = null;
        }

        addCoordinateSystem(coordinateSystem);
      }
    } catch (final NoSuchResourceException e) {
    } catch (final WrappedException e) {
      if (Exceptions.isException(e, EOFException.class)) {
      } else {
        throw e;
      }
    }
  }

  private static void loadCoordinateSystem() {
    try (
      ChannelReader reader = newChannelReader("coordinateSystem")) {
      while (true) {
        final int id = reader.getInt();
        final int type = reader.getByte();
        final boolean deprecated = readBoolean(reader);

        final CoordinateSystemType coordinateSystemType = new CoordinateSystemType(id, type,
          deprecated);
        COORDINATE_SYSTEM_TYPE_BY_ID.put(id, coordinateSystemType);
      }
    } catch (final NoSuchResourceException e) {
    } catch (final WrappedException e) {
      if (Exceptions.isException(e, EOFException.class)) {
      } else {
        throw e;
      }
    }
  }

  private static void loadCoordOperation(final IntHashMap<CoordinateOperationMethod> methodById,
    final IntHashMap<Map<ParameterName, ParameterValue>> operationParameters,
    final IntHashMap<List<Byte>> paramReversal) {
    try (
      ChannelReader reader = newChannelReader("coordOperation")) {
      while (true) {
        final int id = reader.getInt();
        final CoordinateOperationMethod method = readCode(reader, methodById);
        final String name = reader.getStringUtf8ByteCount();
        final byte type = reader.getByte();
        final int sourceCrsCode = reader.getInt();
        final int targetCrsCode = reader.getInt();
        final String transformationVersion = reader.getStringUtf8ByteCount();
        final int variant = reader.getInt();
        final Area area = readCode(reader, AREA_BY_ID);
        final double accuracy = reader.getDouble();
        final boolean deprecated = readBoolean(reader);

        final Map<ParameterName, ParameterValue> parameters = operationParameters.getOrDefault(id,
          Collections.emptyMap());
        final CoordinateOperation coordinateOperation = new CoordinateOperation(id, method, name,
          type, sourceCrsCode, targetCrsCode, transformationVersion, variant, area, accuracy,
          parameters, deprecated);
        OPERATION_BY_ID.put(id, coordinateOperation);

      }
    } catch (final NoSuchResourceException e) {
    } catch (final WrappedException e) {
      if (Exceptions.isException(e, EOFException.class)) {
      } else {
        Logs.error(EpsgCoordinateSystems.class, "Error loading coordOperation", e);
      }
    }
  }

  private static IntHashMap<CoordinateOperationMethod> loadCoordOperationMethod(
    final IntHashMap<List<ParameterName>> paramOrderByMethodId,
    final IntHashMap<List<Byte>> paramReversalByMethodId) {
    final IntHashMap<CoordinateOperationMethod> methodById = new IntHashMap<>();

    try (
      ChannelReader reader = newChannelReader("coordOperationMethod")) {
      while (true) {
        final int id = reader.getInt();
        final String name = reader.getStringUtf8ByteCount();
        final boolean reverse = readBoolean(reader);
        final boolean deprecated = readBoolean(reader);

        final List<ParameterName> parameterNames = paramOrderByMethodId.getOrDefault(id,
          Collections.emptyList());
        final List<Byte> parameterReversal = paramReversalByMethodId.getOrDefault(id,
          Collections.emptyList());
        final CoordinateOperationMethod method = new CoordinateOperationMethod(id, name, reverse,
          deprecated, parameterNames, parameterReversal);
        methodById.put(id, method);
      }
    } catch (final NoSuchResourceException e) {
    } catch (final WrappedException e) {
      if (Exceptions.isException(e, EOFException.class)) {
      } else {
        throw e;
      }
    }
    return methodById;
  }

  private static void loadCoordOperationParam() {
    try (
      ChannelReader reader = newChannelReader("coordOperationParam")) {
      while (true) {
        final int id = reader.getInt();
        String name = reader.getStringUtf8ByteCount();
        if (name != null) {
          name = name.toLowerCase().replaceAll(" ", "_");
        }
        readBoolean(reader);
        final ParameterName parameterName = ParameterNames.getParameterName(id, name);
        PARAM_NAME_BY_ID.put(id, parameterName);
      }
    } catch (final NoSuchResourceException e) {
    } catch (final WrappedException e) {
      if (Exceptions.isException(e, EOFException.class)) {
      } else {
        throw e;
      }
    }
  }

  private static void loadCoordOperationParamUsage(
    final IntHashMap<List<ParameterName>> paramOrderByMethodId,
    final IntHashMap<List<Byte>> paramReversal) {
    try (
      ChannelReader reader = newChannelReader("coordOperationParamUsage")) {
      while (true) {
        final int methodId = reader.getInt();
        final ParameterName parameterName = readCode(reader, PARAM_NAME_BY_ID);
        final int sortOrder = reader.getInt();
        final byte signReversal = reader.getByte();
        Maps.addToList(paramOrderByMethodId, methodId, parameterName);
        Maps.addToList(paramReversal, methodId, signReversal);
      }
    } catch (final NoSuchResourceException e) {
    } catch (final WrappedException e) {
      if (Exceptions.isException(e, EOFException.class)) {
      } else {
        Logs.error(EpsgCoordinateSystems.class, "Error loading coordOperationParamValue", e);
      }
    }
  }

  private static void loadCoordOperationParamValue(
    final IntHashMap<CoordinateOperationMethod> methodById,
    final IntHashMap<Map<ParameterName, ParameterValue>> operationParameters,
    final IntHashMap<List<Byte>> paramReversal) {
    try (
      ChannelReader reader = newChannelReader("coordOperationParamValue")) {
      while (true) {
        final int operationId = reader.getInt();
        final CoordinateOperationMethod method = readCode(reader, methodById);
        final ParameterName parameterName = readCode(reader, PARAM_NAME_BY_ID);
        final double value = reader.getDouble();
        final String fileRef = reader.getStringUtf8ByteCount();
        final UnitOfMeasure unit = readCode(reader, UNIT_BY_ID);
        final ParameterValue parameterValue;
        if (Double.isFinite(value)) {
          if (Property.hasValue(fileRef)) {
            throw new IllegalArgumentException(
              "Cannot have a value and fileRef for coordOperationParamValue=" + operationId + " "
                + parameterName);
          } else {
            parameterValue = new ParameterValueNumber(unit, value);
          }
        } else {
          if (Property.hasValue(fileRef)) {
            parameterValue = new ParameterValueString(fileRef);
          } else {
            parameterValue = null;
          }
        }
        Map<ParameterName, ParameterValue> parameterValues = operationParameters.get(operationId);
        if (parameterValues == null) {
          parameterValues = Maps.newLinkedHash();
          final List<ParameterName> parameterOrder = method.getParameterNames();
          for (final ParameterName orderParameterName : parameterOrder) {
            parameterValues.put(orderParameterName, null);
          }
          operationParameters.put(operationId, parameterValues);
        }
        method.setParameter(parameterValues, parameterName, parameterValue);
      }
    } catch (final NoSuchResourceException e) {
    } catch (final WrappedException e) {
      if (Exceptions.isException(e, EOFException.class)) {
      } else {
        Logs.error(EpsgCoordinateSystems.class, "Error loading coordOperationParamValue", e);
      }
    }
  }

  private static void loadDatum() {
    final IntHashMap<Ellipsoid> ellipsoids = loadEllipsoid();

    try (
      ChannelReader reader = newChannelReader("datum")) {
      while (true) {

        final int id = reader.getInt();
        final String name = reader.getStringUtf8ByteCount();
        final int datumType = reader.getByte();
        final Ellipsoid ellipsoid = readCode(reader, ellipsoids);
        final PrimeMeridian primeMeridian = readCode(reader, PRIME_MERIDIAN_BY_ID);
        final Area area = readCode(reader, AREA_BY_ID);

        final boolean deprecated = readBoolean(reader);
        final EpsgAuthority authority = new EpsgAuthority(id);

        Datum datum;
        if (datumType == 0) {
          datum = new GeodeticDatum(authority, name, area, deprecated, ellipsoid, primeMeridian);
        } else if (datumType == 1) {
          datum = new VerticalDatum(authority, name, area, deprecated);
        } else if (datumType == 2) {
          datum = new EngineeringDatum(authority, name, area, deprecated);
        } else {
          throw new IllegalArgumentException("Unknown datumType=" + datumType);
        }
        DATUM_BY_ID.put(id, datum);

      }
    } catch (final NoSuchResourceException e) {
    } catch (final WrappedException e) {
      if (Exceptions.isException(e, EOFException.class)) {
      } else {
        throw e;
      }
    }
  }

  private static IntHashMap<Ellipsoid> loadEllipsoid() {
    final IntHashMap<Ellipsoid> ellipsoids = new IntHashMap<>();
    try (
      ChannelReader reader = newChannelReader("ellipsoid")) {
      while (true) {
        final int id = reader.getInt();
        final String name = reader.getStringUtf8ByteCount();
        final int unitId = reader.getInt();
        final LinearUnit unit = (LinearUnit)UNIT_BY_ID.get(unitId);
        final double semiMinorAxis = unit.toBase(reader.getDouble());
        final double semiMajorAxis = unit.toBase(reader.getDouble());
        final double inverseFlattening = unit.toBase(reader.getDouble());
        final int ellipsoidShape = reader.getByte();
        final boolean deprecated = readBoolean(reader);
        final EpsgAuthority authority = new EpsgAuthority(id);
        final Ellipsoid ellipsoid = new Ellipsoid(name, semiMajorAxis, semiMinorAxis,
          inverseFlattening, authority, deprecated);
        ellipsoids.put(id, ellipsoid);
      }
    } catch (final NoSuchResourceException e) {
    } catch (final WrappedException e) {
      if (Exceptions.isException(e, EOFException.class)) {
      } else {
        throw e;
      }
    }
    return ellipsoids;
  }

  private static void loadPrimeMeridians() {
    try (
      ChannelReader reader = newChannelReader("primeMeridian")) {
      while (true) {
        final int id = reader.getInt();
        final String name = reader.getStringUtf8ByteCount();
        final AngularUnit unit = (AngularUnit)readCode(reader, UNIT_BY_ID);
        final double longitude = reader.getDouble();
        final double longitudeDegrees = unit.toDegrees(longitude);
        final EpsgAuthority authority = new EpsgAuthority(id);
        final PrimeMeridian primeMeridian = new PrimeMeridian(name, longitudeDegrees, authority,
          false);
        PRIME_MERIDIAN_BY_ID.put(id, primeMeridian);
      }
    } catch (final NoSuchResourceException e) {
    } catch (final WrappedException e) {
      if (Exceptions.isException(e, EOFException.class)) {
      } else {
        throw e;
      }
    }
  }

  private static void loadUnitOfMeasure() {
    try (
      ChannelReader reader = newChannelReader("unitOfMeasure")) {
      if (reader != null) {
        while (true) {
          final int id = reader.getInt();
          final byte type = reader.getByte();
          final int baseId = reader.getInt();
          final boolean deprecated = readBoolean(reader);
          final double conversionFactorB = reader.getDouble();
          final double conversionFactorC = reader.getDouble();
          double conversionFactor;
          if (Double.isFinite(conversionFactorB)) {
            if (Double.isFinite(conversionFactorC)) {
              conversionFactor = conversionFactorB / conversionFactorC;
            } else {
              conversionFactor = conversionFactorB;
            }
          } else {
            conversionFactor = conversionFactorC;
          }

          final String name = reader.getStringUtf8ByteCount();
          final EpsgAuthority authority = new EpsgAuthority(id);

          UnitOfMeasure unit;
          switch (type) {
            case 0:
              final ScaleUnit baseScaleUnit = (ScaleUnit)UNIT_BY_ID.get(baseId);
              unit = new ScaleUnit(name, baseScaleUnit, conversionFactor, authority, deprecated);
            break;
            case 1:
              final LinearUnit baseLinearUnit = (LinearUnit)UNIT_BY_ID.get(baseId);
              if (id == 9001) {
                unit = new Metre(name, baseLinearUnit, conversionFactor, authority, deprecated);
              } else {
                unit = new LinearUnit(name, baseLinearUnit, conversionFactor, authority,
                  deprecated);
              }
            break;
            case 2:
              final AngularUnit baseAngularUnit = (AngularUnit)UNIT_BY_ID.get(baseId);
              if (id == 9101) {
                unit = new Radian(name, baseAngularUnit, conversionFactor, authority, deprecated);
              } else if (id == 9102) {
                unit = new Degree(name, baseAngularUnit, conversionFactor, authority, deprecated);
                SYSTEM_OF_UNITS.addUnit(unit, "Degree", "deg");
              } else if (id == 9105) {
                unit = new Grad(name, baseAngularUnit, conversionFactor, authority, deprecated);
              } else if (id == 9110) {
                unit = new DegreeSexagesimalDMS(name, baseAngularUnit, conversionFactor, authority,
                  deprecated);
              } else if (id == 9122) {
                unit = new Degree(name, baseAngularUnit, conversionFactor, authority, deprecated);
              } else {
                unit = new AngularUnit(name, baseAngularUnit, conversionFactor, authority,
                  deprecated);
              }
            break;
            case 3:
              final TimeUnit baseTimeUnit = (TimeUnit)UNIT_BY_ID.get(baseId);
              unit = new TimeUnit(name, baseTimeUnit, conversionFactor, authority, deprecated);

            break;

            default:
              throw new IllegalArgumentException("Invalid unitId=" + id);
          }
          UNIT_BY_NAME.put(name, unit);
          UNIT_BY_ID.put(id, unit);

        }
      }
    } catch (final NoSuchResourceException e) {
    } catch (final WrappedException e) {
      if (Exceptions.isException(e, EOFException.class)) {
      } else {
        throw e;
      }
    }
  }

  private static ChannelReader newChannelReader(final String fileName) {
    return new ClassPathResource("CoordinateSystems/epsg/" + fileName + ".bin").newChannelReader();
  }

  private static GeocentricCoordinateSystem newCoordinateSystemGeocentric(final int id,
    final String name, final Datum datum, final List<Axis> axis, final Area area,
    final boolean deprecated) {
    final EpsgAuthority authority = new EpsgAuthority(id);
    final LinearUnit linearUnit = (LinearUnit)axis.get(0).getUnit();
    final GeodeticDatum geodeticDatum = (GeodeticDatum)datum;
    return new GeocentricCoordinateSystem(id, name, geodeticDatum, linearUnit, axis, area,
      authority, deprecated);
  }

  private static ProjectedCoordinateSystem newCoordinateSystemProjected(final int id,
    final String name, final Area area, final CoordinateSystem sourceCoordinateSystem,
    final CoordinateOperation operation, final List<Axis> axis, final boolean deprecated) {
    final EpsgAuthority authority = new EpsgAuthority(id);
    final LinearUnit linearUnit = (LinearUnit)axis.get(0).getUnit();
    final CoordinateOperationMethod method = operation.getMethod();
    final Map<ParameterName, ParameterValue> parameterValues = operation.getParameterValues();
    if (sourceCoordinateSystem instanceof GeographicCoordinateSystem) {
      final GeographicCoordinateSystem geographicCoordinateSystem = (GeographicCoordinateSystem)sourceCoordinateSystem;
      return new ProjectedCoordinateSystem(id, name, geographicCoordinateSystem, area, method,
        parameterValues, linearUnit, axis, authority, deprecated);
    } else if (!Arrays.asList(5819, 5820, 5821).contains(id)) {
      Logs.error(EpsgCoordinateSystems.class,
        id + " " + name + " has a projected coordinate system");
      return null;
    } else {
      return null;
    }
  }

  private static boolean readBoolean(final ChannelReader reader) {
    return reader.getByte() == (byte)1;
  }

  private static <V> V readCode(final ChannelReader reader, final IntHashMap<V> valueById) {
    final int id = reader.getInt();
    return getCode(valueById, id);
  }

  public static String toWkt(final CoordinateSystem coordinateSystem) {
    final StringWriter stringWriter = new StringWriter();
    final PrintWriter out = new PrintWriter(stringWriter);
    EpsgCsWktWriter.write(out, coordinateSystem);
    return stringWriter.toString();
  }

  public static GeographicCoordinateSystem wgs84() {
    return EpsgCoordinateSystems.getCoordinateSystem(EpsgId.WGS84);
  }
}
