package com.revolsys.gis.cs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import com.revolsys.data.io.RecordStore;
import com.revolsys.data.io.RecordStoreFactoryRegistry;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.gis.cs.epsg.EpsgUtil;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Reader;
import com.revolsys.io.csv.CsvWriter;
import com.revolsys.io.json.JsonMapIoFactory;

/**
 * Make sure to watch out for parameter value conversions.
 * 
 * @author paustin
 */
public final class EpsgCoordinateSystemsLoader {

  public static void main(final String[] args) {
    new EpsgCoordinateSystemsLoader().load();
  }

  private final RecordStore dataStore;

  private final Map<Integer, Integer> coordinateSystemUnitMap = new HashMap<Integer, Integer>();

  private final Map<Integer, Unit<Length>> linearUnits = new HashMap<Integer, Unit<Length>>();

  private final Map<Integer, Unit<Angle>> angularUnits = new HashMap<Integer, Unit<Angle>>();

  public EpsgCoordinateSystemsLoader() {
    final Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("url", "jdbc:postgresql://localhost:5432/epsg");
    parameters.put("username", "epsg");
    parameters.put("password", "epsg");
    dataStore = RecordStoreFactoryRegistry.createRecordStore(parameters);
  }

  protected Double getDoubleNaN(final Record object, final String name) {
    Double value = object.getDouble(name);
    if (value == null) {
      value = Double.NaN;
    }
    return value;
  }

  protected boolean isDeprecated(final Record object) {
    return object.getInteger("deprecated") == 1;
  }

  protected void load() {
    try {
      loadUnits();
      loadAreas();
      loadCoordinateAxises();
      loadSpheroids();
      loadPrimeMeridians();
      loadDatums();
      loadGeographicCoordinateSystems();
      loadProjectedCoordinateSystems();
    } catch (final Throwable t) {
      t.printStackTrace();
    }
  }

  private void loadAreas() throws IOException {
    final Query query = new Query("/public/epsg_area");
    query.addOrderBy("area_code", true);
    final Reader<Record> reader = dataStore.query(query);
    final File file = new File(
      "../com.revolsys.open.core/src/main/resources/com/revolsys/gis/cs/epsg/area.csv");
    final CsvWriter writer = new CsvWriter(
      FileUtil.createUtf8Writer(new FileOutputStream(file)));
    try {
      writer.write("ID", "NAME", "MIN_X", "MIN_Y", "MAX_X", "MAX_Y",
        "DEPRECATED");
      for (final Record object : reader) {
        final Integer code = object.getInteger("area_code");
        final String name = object.getValue("area_name");
        final Double minY = object.getDouble("area_south_bound_lat");
        final Double maxY = object.getDouble("area_north_bound_lat");
        final Double minX = object.getDouble("area_west_bound_lon");
        final Double maxX = object.getDouble("area_east_bound_lon");
        final boolean deprecated = isDeprecated(object);

        writer.write(code, name, minX, minY, maxX, maxY, deprecated);
      }
    } finally {
      reader.close();
      writer.close();
    }
  }

  private void loadCoordinateAxises() throws IOException {
    final Map<Integer, String> coordinateAxisNames = loadCoordinateAxisNames();
    final Map<Integer, List<Axis>> coordinateAxises = new HashMap<Integer, List<Axis>>();
    final Query query = new Query("/public/epsg_coordinateaxis");
    query.addOrderBy("coord_sys_code", true);
    query.addOrderBy("coord_axis_order", true);
    final Reader<Record> reader = dataStore.query(query);

    try {
      for (final Record object : reader) {
        final Integer coordSysCode = object.getInteger("coord_sys_code");
        final Integer nameCode = object.getInteger("coord_axis_name_code");
        final String direction = object.getValue("coord_axis_orientation");
        final String name = coordinateAxisNames.get(nameCode);
        final Integer uomCode = object.getInteger("uom_code");
        coordinateSystemUnitMap.put(coordSysCode, uomCode);
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
    final File file = new File(
      "../com.revolsys.open.core/src/main/resources/com/revolsys/gis/cs/epsg/axis.csv");
    final CsvWriter writer = new CsvWriter(
      FileUtil.createUtf8Writer(new FileOutputStream(file)));
    try {
      writer.write("ID", "NAME_1", "DIRECTION_1", "NAME_2", "DIRECTION_2",
        "NAME_3", "DIRECTION_3");
      for (final Entry<Integer, List<Axis>> entry : coordinateAxises.entrySet()) {
        final List<String> values = new ArrayList<String>();
        final Integer id = entry.getKey();
        values.add(id.toString());
        final List<Axis> axisList = entry.getValue();
        for (int i = 0; i < 3; i++) {
          if (i >= axisList.size()) {
            values.add("");
            values.add("");
          } else {
            final Axis axis = axisList.get(i);
            values.add(axis.getName());
            values.add(axis.getDirection());
          }
        }
        writer.write(values);
      }
    } finally {
      writer.close();
    }
  }

  private Map<Integer, String> loadCoordinateAxisNames() {
    final Map<Integer, String> coordinateAxisNames = new HashMap<Integer, String>();
    final Reader<Record> reader = dataStore.query("/public/epsg_coordinateaxisname");
    try {
      for (final Record object : reader) {
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
    final Reader<Record> reader = dataStore.query("/public/epsg_coordoperationmethod");
    try {
      for (final Record object : reader) {
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
    final Map<Integer, String> names = new HashMap<Integer, String>();
    final Reader<Record> reader = dataStore.query("/public/epsg_coordoperationparam");
    try {
      for (final Record object : reader) {
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
    final Reader<Record> reader = dataStore.query("/public/epsg_coordoperationparamvalue");
    try {
      for (final Record object : reader) {
        final Integer code = object.getInteger("coord_op_code");
        final Integer nameCode = object.getInteger("parameter_code");
        final Integer uomCode = object.getInteger("uom_code");
        double value = getDoubleNaN(object, "parameter_value");
        value = normalizeValue(uomCode, value);
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
    final Query query = new Query("/public/epsg_coordoperation");
    query.addOrderBy("coord_op_code", true);
    final Reader<Record> reader = dataStore.query(query);
    try {
      for (final Record object : reader) {
        final Integer code = object.getInteger("coord_op_code");
        final Integer methodCode = object.getInteger("coord_op_method_code");
        coordinateOperationMethods.put(code, methodCode);
      }

    } finally {
      reader.close();
    }
    return coordinateOperationMethods;
  }

  private void loadDatums() throws IOException {
    final Query query = new Query("/public/epsg_datum");
    query.addOrderBy("datum_code", true);
    final Reader<Record> reader = dataStore.query(query);
    final File file = new File(
      "../com.revolsys.open.core/src/main/resources/com/revolsys/gis/cs/epsg/datum.csv");
    final CsvWriter writer = new CsvWriter(
      FileUtil.createUtf8Writer(new FileOutputStream(file)));
    try {
      writer.write("ID", "NAME", "SPHEROID_ID", "PRIME_MERIDIAN_ID",
        "DEPRECATED");
      for (final Record object : reader) {
        final String datumType = object.getValue("datum_type");
        final boolean deprecated = isDeprecated(object);
        if (datumType.equals("geodetic")) {
          final Integer code = object.getInteger("datum_code");
          final String name = object.getValue("datum_name");
          final Integer spheroidCode = object.getInteger("ellipsoid_code");
          final Integer primeMeridianCode = object.getInteger("prime_meridian_code");

          writer.write(code, name, spheroidCode, primeMeridianCode, deprecated);
        }
      }

    } finally {
      reader.close();
      writer.close();
    }
  }

  private void loadGeographicCoordinateSystems() throws IOException {
    final Reader<Record> reader = dataStore.query("/public/epsg_coordinatereferencesystem");
    final File file = new File(
      "../com.revolsys.open.core/src/main/resources/com/revolsys/gis/cs/epsg/geographic.csv");
    final CsvWriter writer = new CsvWriter(
      FileUtil.createUtf8Writer(new FileOutputStream(file)));
    try {
      writer.write("ID", "NAME", "DATUM_ID", "UNIT_ID", "AXIS_ID", "AREA_ID",
        "DEPRECATED");
      for (final Record object : reader) {
        final String type = object.getValue("coord_ref_sys_kind");
        final boolean deprecated = isDeprecated(object);
        if (type.equals("geographic 2D")) {
          final Integer datumCode = object.getInteger("datum_code");
          final Integer code = object.getInteger("coord_ref_sys_code");
          final String name = object.getValue("coord_ref_sys_name");
          final Integer coordSysCode = object.getInteger("coord_sys_code");
          final Integer areaCode = object.getInteger("area_of_use_code");
          final Integer unitId = coordinateSystemUnitMap.get(coordSysCode);

          final Integer axisId = coordSysCode;
          writer.write(code, name, datumCode, unitId, axisId, areaCode,
            deprecated);
        }
      }

    } finally {
      reader.close();
      writer.close();
    }
  }

  private void loadPrimeMeridians() throws IOException {
    final Query query = new Query("/public/epsg_primemeridian");
    query.addOrderBy("prime_meridian_code", true);
    final Reader<Record> reader = dataStore.query(query);
    final File file = new File(
      "../com.revolsys.open.core/src/main/resources/com/revolsys/gis/cs/epsg/primemeridian.csv");
    final CsvWriter writer = new CsvWriter(
      FileUtil.createUtf8Writer(new FileOutputStream(file)));
    try {
      writer.write("ID", "NAME", "LONGITUDE", "DEPRECATED");
      for (final Record object : reader) {
        final Integer code = object.getInteger("prime_meridian_code");
        final int uomCode = object.getInteger("uom_code");
        final String name = object.getValue("prime_meridian_name");
        final boolean deprecated = isDeprecated(object);
        double longitude = getDoubleNaN(object, "greenwich_longitude");
        longitude = toDegrees(uomCode, longitude);
        writer.write(code, name, longitude, deprecated);
      }

    } finally {
      reader.close();
      writer.close();
    }
  }

  private void loadProjectedCoordinateSystems() throws IOException {
    final Map<Integer, String> coordinateOperationMethodNames = loadCoordinateOperationMethodNames();
    final Map<Integer, String> coordinateOperationParamNames = loadCoordinateOperationParamNames();
    final Map<Integer, Map<String, Object>> coordinateOperationParamValues = loadCoordinateOperationParamValues(coordinateOperationParamNames);
    final Map<Integer, Integer> coordinateOperationMethods = loadCoordinateOperations();
    final Reader<Record> reader = dataStore.query("/public/epsg_coordinatereferencesystem");
    final File file = new File(
      "../com.revolsys.open.core/src/main/resources/com/revolsys/gis/cs/epsg/projected.csv");
    final CsvWriter writer = new CsvWriter(
      FileUtil.createUtf8Writer(new FileOutputStream(file)));
    try {
      writer.write("ID", "NAME", "GEO_CS_ID", "UNIT_ID", "PROJECTION_ID",
        "PROJECTION_NAME", "PARAMETERS", "AXIS_ID", "AREA_ID", "DEPRECATED");
      for (final Record object : reader) {

        final String type = object.getValue("coord_ref_sys_kind");
        if (type.equals("projected")) {
          final Integer code = object.getInteger("coord_ref_sys_code");
          final String name = object.getValue("coord_ref_sys_name");
          final Integer coordSysCode = object.getInteger("coord_sys_code");
          final Integer areaCode = object.getInteger("area_of_use_code");
          final Integer sourceGeogcrsCode = object.getInteger("source_geogcrs_code");
          final Integer projectionConvCode = object.getInteger("projection_conv_code");
          final boolean deprecated = isDeprecated(object);

          final Map<String, Object> parameters = coordinateOperationParamValues.get(projectionConvCode);
          final Integer methodCode = coordinateOperationMethods.get(projectionConvCode);
          final String methodName = coordinateOperationMethodNames.get(methodCode);
          if (methodCode == null) {
            System.err.println("Unknown coordinate operation:\n" + object);
          } else {

            final Integer unitId = coordinateSystemUnitMap.get(coordSysCode);
            final Integer axisId = coordSysCode;
            final String parametersString = JsonMapIoFactory.toString(parameters);
            writer.write(code, name, sourceGeogcrsCode, unitId, methodCode,
              methodName, parametersString, axisId, areaCode, deprecated);
          }
        }
      }
    } finally {
      reader.close();
      writer.close();
    }
  }

  private void loadSpheroids() throws IOException {
    final Query query = new Query("/public/epsg_ellipsoid");
    query.addOrderBy("ellipsoid_code", true);
    final Reader<Record> reader = dataStore.query(query);
    final File file = new File(
      "../com.revolsys.open.core/src/main/resources/com/revolsys/gis/cs/epsg/spheroid.csv");
    final CsvWriter writer = new CsvWriter(
      FileUtil.createUtf8Writer(new FileOutputStream(file)));
    try {
      writer.write("ID", "NAME", "SEMI_MAJOR_AXIS", "SEMI_MINOR_AXIS",
        "INVERSE_FLATTENING", "DEPRECATED");
      for (final Record object : reader) {
        final Integer code = object.getInteger("ellipsoid_code");
        final boolean deprecated = isDeprecated(object);
        final String name = object.getValue("ellipsoid_name");
        final Integer uomCode = object.getInteger("ellipsoid_code");
        final double semiMajorAxis = normalizeValue(uomCode,
          getDoubleNaN(object, "semi_major_axis"));
        final double inverseFlattening = normalizeValue(uomCode,
          getDoubleNaN(object, "inv_flattening"));
        final double semiMinorAxis = normalizeValue(uomCode,
          getDoubleNaN(object, "semi_minor_axis"));

        writer.write(code, name, semiMajorAxis, semiMinorAxis,
          inverseFlattening, deprecated);
      }

    } finally {
      reader.close();
      writer.close();
    }
  }

  private void loadUnits() throws IOException {
    final Query query = new Query("/public/epsg_unitofmeasure");
    query.addOrderBy("uom_code", true);
    final Reader<Record> reader = dataStore.query(query);
    final File linearFile = new File(
      "../com.revolsys.open.core/src/main/resources/com/revolsys/gis/cs/epsg/linearunit.csv");
    final CsvWriter linearWriter = new CsvWriter(
      FileUtil.createUtf8Writer(new FileOutputStream(linearFile)));
    final File angularFile = new File(
      "../com.revolsys.open.core/src/main/resources/com/revolsys/gis/cs/epsg/angularunit.csv");
    final CsvWriter angularWriter = new CsvWriter(
      FileUtil.createUtf8Writer(new FileOutputStream(angularFile)));
    try {
      linearWriter.write("ID", "NAME", "BASE_ID", "CONVERSION_FACTOR",
        "DEPRECATED");
      angularWriter.write("ID", "NAME", "BASE_ID", "CONVERSION_FACTOR",
        "DEPRECATED");
      for (final Record object : reader) {
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
        if (type.equals("angle")) {
          Unit<Angle> baseUnit = null;
          if (!code.equals(baseUnitCode)) {
            baseUnit = angularUnits.get(baseUnitCode);
          }

          final Unit<Angle> unit = AngularUnit.getUnit(baseUnit,
            conversionFactor);
          angularUnits.put(code, unit);
          angularWriter.write(code, name, baseUnitCode, conversionFactor,
            deprecated);
        } else if (type.equals("length")) {
          Unit<Length> baseUnit = null;
          if (!code.equals(baseUnitCode)) {
            baseUnit = linearUnits.get(baseUnitCode);
          }

          final Unit<Length> unit = LinearUnit.getUnit(baseUnit,
            conversionFactor);
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

  private double normalizeValue(final Integer sourceUomCode, final double value) {
    if (sourceUomCode == null) {
      return value;
    } else if (sourceUomCode < 9000) {
      return value;
    } else if (sourceUomCode < 9100) {
      return toMetres(sourceUomCode, value);
    } else if (sourceUomCode < 9200) {
      return toDegrees(sourceUomCode, value);
    } else {
      return value;
    }
  }

  private double toDegrees(final int sourceUomCode, final double value) {
    double degrees;
    if (sourceUomCode == 9101) {
      degrees = Math.toDegrees(value);
    } else if (sourceUomCode == 9102) {
      degrees = value;
    } else if (sourceUomCode == 9110) {
      degrees = EpsgUtil.toDecimalFromSexagesimalDegrees(value);
    } else {
      final Unit<Angle> angularUnit = angularUnits.get(sourceUomCode);
      if (angularUnit == null) {
        throw new IllegalArgumentException("Angular unit of measure not found "
          + sourceUomCode);
      } else {
        final UnitConverter converter = angularUnit.getConverterTo(NonSI.DEGREE_ANGLE);
        degrees = converter.convert(value);
      }
    }
    return degrees;
  }

  private double toMetres(final int sourceUomCode, final double value) {
    double metres;
    if (sourceUomCode == 9001) {
      metres = value;
    } else {
      final Unit<Length> linearUnit = linearUnits.get(sourceUomCode);
      if (linearUnit == null) {
        throw new IllegalArgumentException("Linear unit of measure not found "
          + sourceUomCode);
      } else {
        final UnitConverter converter = linearUnit.getConverterTo(SI.METRE);
        metres = converter.convert(value);
      }
    }
    return metres;
  }
}
