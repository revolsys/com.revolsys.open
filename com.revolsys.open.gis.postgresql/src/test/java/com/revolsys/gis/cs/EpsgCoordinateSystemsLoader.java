package com.revolsys.gis.cs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.revolsys.gis.cs.epsg.EpsgAuthority;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreFactoryRegistry;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.query.Query;
import com.revolsys.io.Reader;
import com.revolsys.io.csv.CsvWriter;
import com.revolsys.io.json.JsonMapIoFactory;
import com.vividsolutions.jts.geom.Envelope;

public final class EpsgCoordinateSystemsLoader {

  private Map<Integer, GeographicCoordinateSystem> geographicCoordinateSystems = new TreeMap<Integer, GeographicCoordinateSystem>();

  private Map<Integer, ProjectedCoordinateSystem> projectedCoordinateSystems = new TreeMap<Integer, ProjectedCoordinateSystem>();

  private DataObjectStore dataStore;

  public EpsgCoordinateSystemsLoader() {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("url", "jdbc:postgresql://localhost:5432/epsg");
    parameters.put("username", "epsg");
    parameters.put("password", "epsg");
    dataStore = DataObjectStoreFactoryRegistry.createDataObjectStore(parameters);
  }

  public static void main(String[] args) {
    new EpsgCoordinateSystemsLoader().load();
  }

  protected void load() {
    try {
      final Map<Integer, LinearUnit> linearUnits = new HashMap<Integer, LinearUnit>();
      final Map<Integer, AngularUnit> angularUnits = new HashMap<Integer, AngularUnit>();

      loadUnits(linearUnits, angularUnits);
      final Map<Integer, Area> areas = loadAreas();

      final Map<Integer, List<Axis>> coordinateAxises = loadCoordinateAxises(
        linearUnits, angularUnits);
      final Map<Integer, Datum> datums = loadDatums();
      loadGeographicCoordinateSystems(areas, datums, angularUnits,
        coordinateAxises);
      loadProjectedCoordinateSystems(areas, coordinateAxises, linearUnits);
    } catch (final Throwable t) {
      t.printStackTrace();
    }
  }

  private Map<Integer, Area> loadAreas() throws FileNotFoundException,
    IOException {
    final Map<Integer, Area> areas = new HashMap<Integer, Area>();
    Query query = new Query("/public/epsg_area");
    query.addOrderBy("area_code", true);
    Reader<DataObject> reader = dataStore.query(query);
    File file = new File(
      "../com.revolsys.open.core/src/main/resources/com/revolsys/gis/cs/epsg/area.csv");
    CsvWriter writer = new CsvWriter(new OutputStreamWriter(
      new FileOutputStream(file)));
    try {
      writer.write("ID", "NAME", "MIN_X", "MIN_Y", "MAX_X", "MAX_Y",
        "DEPRECATED");
      for (DataObject object : reader) {
        final Integer code = object.getInteger("area_code");
        final String name = object.getValue("area_name");
        final Double minY = object.getDouble("area_south_bound_lat");
        final Double maxY = object.getDouble("area_north_bound_lat");
        final Double minX = object.getDouble("area_west_bound_lon");
        final Double maxX = object.getDouble("area_east_bound_lon");
        final boolean deprecated = isDeprecated(object);
        final Authority authority = new EpsgAuthority(code);

        Envelope envelope;
        if (minY == null) {
          envelope = new Envelope();
        } else {
          envelope = new Envelope(minX, maxX, minY, maxY);
        }
        final Area area = new Area(name, envelope, authority, deprecated);
        areas.put(code, area);
        writer.write(code, name, minX, minY, maxX, maxY, deprecated);
      }
      return areas;
    } finally {
      reader.close();
      writer.close();
    }
  }

  protected boolean isDeprecated(DataObject object) {
    return object.getInteger("deprecated") == 1;
  }

  private Map<Integer, List<Axis>> loadCoordinateAxises(
    final Map<Integer, LinearUnit> linearUnits,
    final Map<Integer, AngularUnit> angularUnits) throws FileNotFoundException,
    IOException {
    final Map<Integer, String> coordinateAxisNames = loadCoordinateAxisNames();
    final Map<Integer, List<Axis>> coordinateAxises = new HashMap<Integer, List<Axis>>();
    Query query = new Query("/public/epsg_coordinateaxis");
    query.addOrderBy("coord_sys_code", true);
    query.addOrderBy("coord_axis_order", true);
    Reader<DataObject> reader = dataStore.query(query);

    try {
      for (DataObject object : reader) {
        final Integer coordSysCode = object.getInteger("coord_sys_code");
        final Integer nameCode = object.getInteger("coord_axis_name_code");
        final String direction = object.getValue("coord_axis_orientation");
        final String name = coordinateAxisNames.get(nameCode);
        final Integer uomCode = object.getInteger("uom_code");
        final LinearUnit linearUnit = linearUnits.get(uomCode);
        if (linearUnit != null) {
          linearUnits.put(coordSysCode, linearUnit);
        }
        final AngularUnit angularUnit = angularUnits.get(uomCode);
        if (angularUnit != null) {
          angularUnits.put(coordSysCode, angularUnit);
        }
        final int order = object.getInteger("coord_axis_order");
        List<Axis> axises = coordinateAxises.get(coordSysCode);
        if (axises == null) {
          axises = new ArrayList<Axis>();
          coordinateAxises.put(coordSysCode, axises);
        }
        while (axises.size() < order) {
          axises.add(null);
        }
        axises.set(order - 1, new Axis(name, direction));
      }

    } finally {
      reader.close();

    }
    File file = new File(
      "../com.revolsys.open.core/src/main/resources/com/revolsys/gis/cs/epsg/axis.csv");
    CsvWriter writer = new CsvWriter(new OutputStreamWriter(
      new FileOutputStream(file)));
    try {
      writer.write("ID", "NAME_1", "DIRECTION_1", "NAME_2", "DIRECTION_2",
        "NAME_3", "DIRECTION_3");
      for (Entry<Integer, List<Axis>> entry : coordinateAxises.entrySet()) {
        List<String> values = new ArrayList<String>();
        Integer id = entry.getKey();
        values.add(id.toString());
        List<Axis> axisList = entry.getValue();
        for (int i = 0; i < 3; i++) {
          if (i >= axisList.size()) {
            values.add("");
            values.add("");
          } else {
            Axis axis = axisList.get(i);
            values.add(axis.getName());
            values.add(axis.getDirection());
          }
        }
        writer.write(values);
      }
    } finally {
      writer.close();
    }
    return coordinateAxises;
  }

  private Map<Integer, String> loadCoordinateAxisNames() {
    final Map<Integer, String> coordinateAxisNames = new HashMap<Integer, String>();
    Reader<DataObject> reader = dataStore.query("/public/epsg_coordinateaxisname");
    try {
      for (DataObject object : reader) {
        final Integer code = object.getInteger("coord_axis_name_code");
        final String name = object.getValue("coord_axis_name");
        coordinateAxisNames.put(code, name);
      }

    } finally {
      reader.close();
    }
    return coordinateAxisNames;
  }

  private Map<Integer, String> loadCoordinateOperationMethodNames() {
    final Map<Integer, String> coordinateOperationMethodNames = new HashMap<Integer, String>();
    Reader<DataObject> reader = dataStore.query("/public/epsg_coordoperationmethod");
    try {
      for (DataObject object : reader) {
        final Integer code = object.getInteger("coord_op_method_code");
        final String name = ((String)object.getValue("coord_op_method_name")).replace(
          " ", "_");
        coordinateOperationMethodNames.put(code, name);
      }

    } finally {
      reader.close();
    }
    return coordinateOperationMethodNames;
  }

  private Map<Integer, String> loadCoordinateOperationParamNames() {
    Map<Integer, String> names = new HashMap<Integer, String>();
    Reader<DataObject> reader = dataStore.query("/public/epsg_coordoperationparam");
    try {
      for (DataObject object : reader) {
        final Integer code = object.getInteger("parameter_code");
        final String name = object.getValue("parameter_name".replace(" ", "_")
          .toLowerCase());
        names.put(code, name);
      }

    } finally {
      reader.close();
    }
    return names;
  }

  private Map<Integer, Map<String, Object>> loadCoordinateOperationParamValues(
    final Map<Integer, String> coordinateOperationParamNames) {
    final Map<Integer, Map<String, Object>> coordinateOperationParamValues = new HashMap<Integer, Map<String, Object>>();
    Reader<DataObject> reader = dataStore.query("/public/epsg_coordoperationparamvalue");
    try {
      for (DataObject object : reader) {
        final Integer code = object.getInteger("coord_op_code");
        final Integer nameCode = object.getInteger("parameter_code");
        final Double value = getDoubleNaN(object, "parameter_value");
        Map<String, Object> parameters = coordinateOperationParamValues.get(code);
        if (parameters == null) {
          parameters = new HashMap<String, Object>();
          coordinateOperationParamValues.put(code, parameters);
        }
        String paramName = coordinateOperationParamNames.get(nameCode);
        paramName = paramName.toLowerCase().replace(' ', '_');
        parameters.put(paramName, value);
      }

    } finally {
      reader.close();
    }
    return coordinateOperationParamValues;
  }

  private Map<Integer, Integer> loadCoordinateOperations() {

    final Map<Integer, Integer> coordinateOperationMethods = new HashMap<Integer, Integer>();
    Query query = new Query("/public/epsg_coordoperation");
    query.addOrderBy("coord_op_code", true);
    Reader<DataObject> reader = dataStore.query(query);
    try {
      for (DataObject object : reader) {
        final Integer code = object.getInteger("coord_op_code");
        final Integer methodCode = object.getInteger("coord_op_method_code");
        coordinateOperationMethods.put(code, methodCode);
      }

    } finally {
      reader.close();
    }
    return coordinateOperationMethods;
  }

  private Map<Integer, Datum> loadDatums() throws FileNotFoundException,
    IOException {
    final Map<Integer, Datum> datums = new HashMap<Integer, Datum>();
    final Map<Integer, Spheroid> spheroids = loadSpheroids();
    final Map<Integer, PrimeMeridian> primeMeridians = loadPrimeMeridians();
    Query query = new Query("/public/epsg_datum");
    query.addOrderBy("datum_code", true);
    Reader<DataObject> reader = dataStore.query(query);
    File file = new File(
      "../com.revolsys.open.core/src/main/resources/com/revolsys/gis/cs/epsg/datum.csv");
    CsvWriter writer = new CsvWriter(new OutputStreamWriter(
      new FileOutputStream(file)));
    try {
      writer.write("ID", "NAME", "SPHEROID_ID", "PRIME_MERIDIAN_ID",
        "DEPRECATED");
      for (DataObject object : reader) {
        final String datumType = object.getValue("datum_type");
        final boolean deprecated = isDeprecated(object);
        if (datumType.equals("geodetic")) {
          final Integer code = object.getInteger("datum_code");
          final String name = object.getValue("datum_name");
          final Integer spheroidCode = object.getInteger("ellipsoid_code");
          final Integer primeMeridianCode = object.getInteger("prime_meridian_code");

          final Authority authority = new EpsgAuthority(code);
          final Spheroid spheroid = spheroids.get(spheroidCode);
          final PrimeMeridian primeMeridian = primeMeridians.get(primeMeridianCode);

          final Datum datum = new Datum(name, spheroid, primeMeridian,
            authority, deprecated);
          datums.put(code, datum);
          writer.write(code, name, spheroidCode, primeMeridianCode,
            isDeprecated(object));
        }
      }

    } finally {
      reader.close();
      writer.close();
    }
    return datums;
  }

  private void loadGeographicCoordinateSystems(final Map<Integer, Area> areas,
    final Map<Integer, Datum> datums,
    final Map<Integer, AngularUnit> angularCoordinateSystemUnits,
    final Map<Integer, List<Axis>> coordinateAxises)
    throws FileNotFoundException, IOException {
    Reader<DataObject> reader = dataStore.query("/public/epsg_coordinatereferencesystem");
    File file = new File(
      "../com.revolsys.open.core/src/main/resources/com/revolsys/gis/cs/epsg/geographic.csv");
    CsvWriter writer = new CsvWriter(new OutputStreamWriter(
      new FileOutputStream(file)));
    try {
      writer.write("ID", "NAME", "DATUM_ID", "UNIT_ID", "AXIS_ID", "AREA_ID",
        "DEPRECATED");
      for (DataObject object : reader) {
        final String type = object.getValue("coord_ref_sys_kind");
        final boolean deprecated = isDeprecated(object);
        if (type.equals("geographic 2D")) {
          final Integer datumCode = object.getInteger("datum_code");
          final Datum datum = datums.get(datumCode);

          final Integer code = object.getInteger("coord_ref_sys_code");
          final String name = object.getValue("coord_ref_sys_name");

          final Integer coordSysCode = object.getInteger("coord_sys_code");

          final Integer areaCode = object.getInteger("area_of_use_code");
          final Area area = areas.get(areaCode);

          final Authority authority = new EpsgAuthority(code);
          final AngularUnit angularUnit = angularCoordinateSystemUnits.get(coordSysCode);
          final List<Axis> axis = coordinateAxises.get(coordSysCode);

          final GeographicCoordinateSystem cs = new GeographicCoordinateSystem(
            code, name, datum, angularUnit, axis, area, authority, deprecated);
          geographicCoordinateSystems.put(code, cs);

          String unitId = angularUnit.getAuthority().getCode();
          Integer axisId = coordSysCode;
          writer.write(code, name, datumCode, unitId, axisId, areaCode,
            deprecated);
        }
      }

    } finally {
      reader.close();
      writer.close();
    }
  }

  private Map<Integer, PrimeMeridian> loadPrimeMeridians()
    throws FileNotFoundException, IOException {
    final Map<Integer, PrimeMeridian> primeMeridians = new HashMap<Integer, PrimeMeridian>();
    Query query = new Query("/public/epsg_primemeridian");
    query.addOrderBy("prime_meridian_code", true);
    Reader<DataObject> reader = dataStore.query(query);
    File file = new File(
      "../com.revolsys.open.core/src/main/resources/com/revolsys/gis/cs/epsg/primemeridian.csv");
    CsvWriter writer = new CsvWriter(new OutputStreamWriter(
      new FileOutputStream(file)));
    try {
      writer.write("ID", "NAME", "LONGITUDE", "DEPRECATED");
      for (DataObject object : reader) {
        final Integer code = object.getInteger("prime_meridian_code");
        final String name = object.getValue("prime_meridian_name");
        final boolean deprecated = isDeprecated(object);
        Double longitude = getDoubleNaN(object, "greenwich_longitude");
        final Authority authority = new EpsgAuthority(code);

        final PrimeMeridian primeMeridian = new PrimeMeridian(name, longitude,
          authority, deprecated);
        primeMeridians.put(code, primeMeridian);
        writer.write(code, name, longitude, deprecated);
      }

    } finally {
      reader.close();
      writer.close();
    }
    return primeMeridians;
  }

  protected Double getDoubleNaN(DataObject object, String name) {
    Double value = object.getDouble(name);
    if (value == null) {
      value = Double.NaN;
    }
    return value;
  }

  private void loadProjectedCoordinateSystems(final Map<Integer, Area> areas,
    final Map<Integer, List<Axis>> coordinateAxises,
    final Map<Integer, LinearUnit> linearCoordinateSystemUnits)
    throws FileNotFoundException, IOException {
    final Map<Integer, String> coordinateOperationMethodNames = loadCoordinateOperationMethodNames();
    final Map<Integer, String> coordinateOperationParamNames = loadCoordinateOperationParamNames();
    final Map<Integer, Map<String, Object>> coordinateOperationParamValues = loadCoordinateOperationParamValues(coordinateOperationParamNames);
    final Map<Integer, Integer> coordinateOperationMethods = loadCoordinateOperations();
    Reader<DataObject> reader = dataStore.query("/public/epsg_coordinatereferencesystem");
    File file = new File(
      "../com.revolsys.open.core/src/main/resources/com/revolsys/gis/cs/epsg/projected.csv");
    CsvWriter writer = new CsvWriter(new OutputStreamWriter(
      new FileOutputStream(file)));
    try {
      writer.write("ID", "NAME", "GEO_CS_ID", "UNIT_ID", "PROJECTION_ID",
        "PROJECTION_NAME", "PARAMETERS", "AXIS_ID", "AREA_ID", "DEPRECATED");
      for (DataObject object : reader) {

        final String type = object.getValue("coord_ref_sys_kind");
        if (type.equals("projected")) {
          final Integer code = object.getInteger("coord_ref_sys_code");
          final String name = object.getValue("coord_ref_sys_name");
          final Integer coordSysCode = object.getInteger("coord_sys_code");
          final Integer areaCode = object.getInteger("area_of_use_code");
          final Integer sourceGeogcrsCode = object.getInteger("source_geogcrs_code");
          final Integer projectionConvCode = object.getInteger("projection_conv_code");
          final boolean deprecated = isDeprecated(object);

          final Authority authority = new EpsgAuthority(code);
          final LinearUnit linearUnit = linearCoordinateSystemUnits.get(coordSysCode);
          final List<Axis> axis = coordinateAxises.get(coordSysCode);
          final CoordinateSystem sourceCs = geographicCoordinateSystems.get(sourceGeogcrsCode);
          if (sourceCs instanceof GeographicCoordinateSystem) {
            final GeographicCoordinateSystem geographicCs = (GeographicCoordinateSystem)sourceCs;
            final Map<String, Object> parameters = coordinateOperationParamValues.get(projectionConvCode);
            final Integer methodCode = coordinateOperationMethods.get(projectionConvCode);
            final String methodName = coordinateOperationMethodNames.get(methodCode);
            if (methodCode == null) {
              System.err.println("Unknown coordinate operation:\n" + object);
            } else {
              final Authority projectionAuthority = new EpsgAuthority(
                methodCode);
              final Projection projection = new Projection(methodName,
                projectionAuthority);
              final Area area = areas.get(areaCode);

              final ProjectedCoordinateSystem cs = new ProjectedCoordinateSystem(
                code, name, geographicCs, area, projection, parameters,
                linearUnit, axis, authority, deprecated);
              projectedCoordinateSystems.put(code, cs);

              String unitId = linearUnit.getAuthority().getCode();
              Integer axisId = coordSysCode;
              String parametersString = JsonMapIoFactory.toString(parameters);
              writer.write(code, name, sourceGeogcrsCode, unitId, methodCode,
                methodName, parametersString, axisId, areaCode, deprecated);
            }
          }
        }
      }
    } finally {
      reader.close();
      writer.close();
    }
  }

  private Map<Integer, Spheroid> loadSpheroids() throws FileNotFoundException,
    IOException {
    final Map<Integer, Spheroid> spheroids = new HashMap<Integer, Spheroid>();
    Query query = new Query("/public/epsg_ellipsoid");
    query.addOrderBy("ellipsoid_code", true);
    Reader<DataObject> reader = dataStore.query(query);
    File file = new File(
      "../com.revolsys.open.core/src/main/resources/com/revolsys/gis/cs/epsg/spheroid.csv");
    CsvWriter writer = new CsvWriter(new OutputStreamWriter(
      new FileOutputStream(file)));
    try {
      writer.write("ID", "NAME", "SEMI_MAJOR_AXIS", "SEMI_MINOR_AXIS",
        "INVERSE_FLATTENING", "DEPRECATED");
      for (DataObject object : reader) {
        final Integer code = object.getInteger("ellipsoid_code");
        final boolean deprecated = isDeprecated(object);
        final String name = object.getValue("ellipsoid_name");
        final double semiMajorAxis = getDoubleNaN(object, "semi_major_axis");
        final double inverseFlattening = getDoubleNaN(object, "inv_flattening");
        final double semiMinorAxis = getDoubleNaN(object, "semi_minor_axis");

        final Authority authority = new EpsgAuthority(code);

        final Spheroid spheroid = new Spheroid(name, semiMajorAxis,
          semiMinorAxis, inverseFlattening, authority, deprecated);
        spheroids.put(code, spheroid);
        writer.write(code, name, semiMajorAxis, semiMinorAxis,
          inverseFlattening, deprecated);
      }

    } finally {
      reader.close();
      writer.close();
    }
    return spheroids;
  }

  private void loadUnits(final Map<Integer, LinearUnit> linearUnits,
    final Map<Integer, AngularUnit> angularUnits) throws IOException {

    Query query = new Query("/public/epsg_unitofmeasure");
    query.addOrderBy("uom_code", true);
    Reader<DataObject> reader = dataStore.query(query);
    File linearFile = new File(
      "../com.revolsys.open.core/src/main/resources/com/revolsys/gis/cs/epsg/linearunit.csv");
    CsvWriter linearWriter = new CsvWriter(new OutputStreamWriter(
      new FileOutputStream(linearFile)));
    File angularFile = new File(
      "../com.revolsys.open.core/src/main/resources/com/revolsys/gis/cs/epsg/angularunit.csv");
    CsvWriter angularWriter = new CsvWriter(new OutputStreamWriter(
      new FileOutputStream(angularFile)));
    try {
      linearWriter.write("ID", "NAME", "BASE_ID", "CONVERSION_FACTOR",
        "DEPRECATED");
      angularWriter.write("ID", "NAME", "BASE_ID", "CONVERSION_FACTOR",
        "DEPRECATED");
      for (DataObject object : reader) {
        final Integer code = object.getInteger("uom_code");
        final String name = object.getValue("unit_of_meas_name");
        final String type = object.getValue("unit_of_meas_type");
        final Integer baseUnitCode = object.getInteger("target_uom_code");
        Double conversionFactor = object.getDouble("factor_b");
        final Double conversionFactorC = object.getDouble("factor_c");
        final boolean deprecated = isDeprecated(object);
        if (conversionFactor == null) {
          conversionFactor = Double.NaN;
        }
        if (conversionFactorC != null) {
          conversionFactor = conversionFactor / conversionFactorC;
        }
        final Authority authority = new EpsgAuthority(code);
        if (type.equals("angle")) {
          AngularUnit baseUnit = null;
          if (!code.equals(baseUnitCode)) {
            baseUnit = angularUnits.get(baseUnitCode);
          }

          final AngularUnit unit = new AngularUnit(name, baseUnit,
            conversionFactor, authority, deprecated);
          angularUnits.put(code, unit);
          angularWriter.write(code, name, baseUnitCode, conversionFactor,
            deprecated);
        } else if (type.equals("length")) {
          LinearUnit baseUnit = null;
          if (!code.equals(baseUnitCode)) {
            baseUnit = linearUnits.get(baseUnitCode);
          }

          final LinearUnit unit = new LinearUnit(name, baseUnit,
            conversionFactor, authority, deprecated);
          linearUnits.put(code, unit);
          linearWriter.write(code, name, baseUnitCode, conversionFactor,
            deprecated);
        }
      }

    } finally {
      reader.close();
      linearWriter.close();
      angularWriter.close();
    }
  }
}
