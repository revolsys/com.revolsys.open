package com.revolsys.gis.cs;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.cs.AxisName;
import com.revolsys.geometry.cs.CoordinateSystemType;
import com.revolsys.geometry.cs.UnitOfMeasure;
import com.revolsys.io.PathName;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;

/**
 * Make sure to watch out for parameter value conversions.
 *
 * @author paustin
 */
public final class EpsgCoordinateSystemsLoader {

  public static final List<String> COORDINATE_REFERENCE_SYSTEM_TYPES = Arrays.asList(
    "geographic 2D", "geographic 3D", "geocentric", "projected", "engineering", "vertical",
    "compound");

  public static final List<String> COORDINATE_OPERATION_TYPES = Arrays.asList("conversion",
    "transformation", "concatenated operation");

  private static NumberFormat getFormat() {
    return new DecimalFormat("#0.00000##########################");
  }

  public static void main(final String[] args) {
    new EpsgCoordinateSystemsLoader().load();
  }

  public static double toDecimalFromSexagesimalDegrees(final double sexagesimal) {
    final String string = getFormat().format(sexagesimal);
    final int dotIndex = string.indexOf('.');

    final int degrees = Integer.parseInt(string.substring(0, dotIndex));
    final int minutes = Integer.parseInt(string.substring(dotIndex + 1, dotIndex + 3));
    final double seconds = Double.parseDouble(
      string.substring(dotIndex + 3, dotIndex + 5) + "." + string.substring(dotIndex + 5));
    double decimal;
    if (sexagesimal < 0) {
      decimal = degrees - minutes / 60.0 - seconds / 3600.0;
    } else {
      decimal = degrees + minutes / 60.0 + seconds / 3600.0;
    }
    return decimal;
  }

  private final Resource baseResource = new PathResource(
    "../com.revolsys.open.core/src/main/resources/com/revolsys/gis/cs/epsg");

  private final Map<Integer, Integer> coordinateSystemUnitMap = new HashMap<>();

  private final RecordStore recordStore;

  public EpsgCoordinateSystemsLoader() {
    final Map<String, Object> parameters = new HashMap<>();
    parameters.put("url", "jdbc:postgresql://localhost:5432/epsg");
    parameters.put("user", "epsg");
    parameters.put("password", "epsg");
    this.recordStore = RecordStore.newRecordStore(parameters);
  }

  protected boolean isDeprecated(final Record object) {
    return object.getInteger("deprecated") == 1;
  }

  protected void load() {
    try {
      loadUnitsOfMeasure();
      loadArea();
      loadCoordinateAxisName();
      loadCoordinateAxis();
      loadCoordinateSystem();
      loadEllipsoid();
      loadPrimeMeridians();
      loadDatum();
      loadCoordOperation();
      loadCoordOperationMethod();
      loadCoordOperationParam();
      loadCoordOperationParamValue();

      loadCoordinateReferenceSystem();
    } catch (final Throwable t) {
      t.printStackTrace();
    }
  }

  private void loadArea() throws IOException {
    final Query query = new Query("/public/epsg_area") //
      .setOrderBy("area_code");
    try (
      final RecordReader reader = this.recordStore.getRecords(query);
      ChannelWriter writer = newWriter("area")) {
      for (final Record record : reader) {
        final int code = record.getInteger("area_code");
        final String name = record.getValue("area_name");
        final double minY = record.getDouble("area_south_bound_lat", Double.NaN);
        final double maxY = record.getDouble("area_north_bound_lat", Double.NaN);
        final double minX = record.getDouble("area_west_bound_lon", Double.NaN);
        final double maxX = record.getDouble("area_east_bound_lon", Double.NaN);

        writer.putInt(code);
        writer.putStringUtf8ByteCount(name);
        writer.putDouble(minX);
        writer.putDouble(minY);
        writer.putDouble(maxX);
        writer.putDouble(maxY);
        writeDeprecated(writer, record);
      }
    }
  }

  private void loadCoordinateAxis() throws IOException {
    final Query query = new Query("/public/epsg_coordinateaxis") //
      .setOrderByFieldNames("coord_sys_code", "coord_axis_order");

    try (
      final RecordReader reader = this.recordStore.getRecords(query);
      ChannelWriter writer = newWriter("coordinateAxis");) {
      for (final Record object : reader) {
        final int coordSysCode = object.getInteger("coord_sys_code");
        final int nameCode = object.getInteger("coord_axis_name_code");
        final String orientation = object.getValue("coord_axis_orientation");
        final String abbreviation = object.getValue("coord_axis_abbreviation");
        final int uomCode = object.getInteger("uom_code");
        this.coordinateSystemUnitMap.put(coordSysCode, uomCode);

        writer.putInt(coordSysCode);
        writer.putInt(nameCode);
        writer.putStringUtf8ByteCount(orientation);
        writer.putByte((byte)abbreviation.charAt(0));
        writer.putInt(uomCode);
      }

    }

  }

  private Map<Integer, AxisName> loadCoordinateAxisName() {
    final Map<Integer, AxisName> coordinateAxisNames = new HashMap<>();
    try (
      final RecordReader reader = newReader("/public/epsg_coordinateaxisname");
      ChannelWriter writer = newWriter("coordinateAxisName");) {
      for (final Record object : reader) {
        final int code = object.getInteger("coord_axis_name_code");
        final String name = object.getValue("coord_axis_name");
        coordinateAxisNames.put(code, new AxisName(code, name));
        writer.putInt(code);
        writer.putStringUtf8ByteCount(name);
      }

    }
    return coordinateAxisNames;
  }

  private void loadCoordinateReferenceSystem() throws IOException {
    final Query query = new Query("/public/epsg_coordinatereferencesystem") //
      .addOrderBy("coord_ref_sys_code");

    final Map<Integer, List<Record>> recordByKind = new TreeMap<>();

    try (
      final RecordReader reader = this.recordStore.getRecords(query)) {
      for (final Record record : reader) {
        final String code = record.getValue("coord_ref_sys_kind");
        final int kind = COORDINATE_REFERENCE_SYSTEM_TYPES.indexOf(code);
        Maps.addToList(recordByKind, kind, record);
      }
    }
    try (
      ChannelWriter writer = newWriter("coordinateReferenceSystem")) {
      for (final List<Record> records : recordByKind.values()) {
        for (final Record record : records) {
          writeInt(writer, record, "coord_ref_sys_code");
          writeString(writer, record, "coord_ref_sys_name");
          writeInt(writer, record, "area_of_use_code");
          writeCodeByte(writer, record, "coord_ref_sys_kind", COORDINATE_REFERENCE_SYSTEM_TYPES);
          writeInt(writer, record, "coord_sys_code", 0);
          writeInt(writer, record, "datum_code", 0);
          writeInt(writer, record, "source_geogcrs_code", 0);
          writeInt(writer, record, "projection_conv_code", 0);
          writeInt(writer, record, "cmpd_hoizcrs_code", 0);
          writeInt(writer, record, "cmpd_vertcrs_code", 0);
          writeDeprecated(writer, record);
        }
      }
    }
  }

  private void loadCoordinateSystem() throws IOException {
    try (
      final RecordReader reader = newReader("/public/epsg_coordinatesystem");
      ChannelWriter writer = newWriter("coordinateSystem")) {
      for (final Record record : reader) {
        final int id = record.getInteger("coord_sys_code");
        writer.putInt(id);
        writeCodeByte(writer, record, "coord_sys_type", CoordinateSystemType.TYPE_NAMES);
        writeDeprecated(writer, record);
      }
    }
  }

  private void loadCoordOperation() {
    try (
      final RecordReader reader = newReader("/public/epsg_coordoperation", "coord_op_code");
      ChannelWriter writer = newWriter("coordOperation");) {
      for (final Record record : reader) {
        writeInt(writer, record, "coord_op_code");
        writeInt(writer, record, "coord_op_method_code", 0);
        writeString(writer, record, "coord_op_name");
        writeCodeByte(writer, record, "coord_op_type", COORDINATE_OPERATION_TYPES);
        writeInt(writer, record, "source_crs_code", 0);
        writeInt(writer, record, "target_crs_code", 0);
        writeString(writer, record, "coord_tfm_version");
        writeInt(writer, record, "coord_op_variant", 0);
        writeInt(writer, record, "area_of_use", 0);
        writeDouble(writer, record, "coord_op_accuracy");
        writeDeprecated(writer, record);
      }
    }
  }

  private void loadCoordOperationMethod() {
    try (
      RecordReader reader = newReader("/public/epsg_coordoperationmethod");
      ChannelWriter writer = newWriter("coordOperationMethod");) {
      for (final Record record : reader) {
        writeInt(writer, record, "coord_op_method_code");
        writeString(writer, record, "coord_op_method_name");
        writeByte(writer, record, "reverse_op");
        writeDeprecated(writer, record);
      }
    }
  }

  private void loadCoordOperationParam() {
    try (
      RecordReader reader = newReader("/public/epsg_coordoperationparam");
      ChannelWriter writer = newWriter("coordOperationParam");) {
      for (final Record record : reader) {
        writeInt(writer, record, "parameter_code");
        writeString(writer, record, "parameter_name");
        writeDeprecated(writer, record);
      }
    }
  }

  private void loadCoordOperationParamValue() {
    try (
      RecordReader reader = newReader("/public/epsg_coordoperationparamvalue");
      ChannelWriter writer = newWriter("coordOperationParamValue");) {
      for (final Record record : reader) {
        writeInt(writer, record, "coord_op_code");
        writeInt(writer, record, "coord_op_method_code");
        writeInt(writer, record, "parameter_code");
        writeDouble(writer, record, "parameter_value");
        writeString(writer, record, "param_value_file_ref");
        writeInt(writer, record, "uom_code", 0);

      }
    }
  }

  private void loadDatum() throws IOException {
    final Query query = new Query("/public/epsg_datum") //
      .setOrderBy("datum_code");
    final List<String> datumTypes = Arrays.asList("geodetic", "vertical", "engineering");
    try (
      final RecordReader reader = this.recordStore.getRecords(query);
      ChannelWriter writer = newWriter("datum")) {
      for (final Record object : reader) {
        writeInt(writer, object, "datum_code");
        writeString(writer, object, "datum_name");
        writeCodeByte(writer, object, "datum_type", datumTypes);
        writeInt(writer, object, "ellipsoid_code", 0);
        writeInt(writer, object, "prime_meridian_code", 0);
        writeInt(writer, object, "area_of_use_code");
        writeDeprecated(writer, object);
      }
    }
  }

  private void loadEllipsoid() throws IOException {
    final Query query = new Query("/public/epsg_ellipsoid") //
      .setOrderBy("ellipsoid_code");
    try (
      final RecordReader reader = this.recordStore.getRecords(query);
      ChannelWriter writer = newWriter("ellipsoid")) {
      for (final Record record : reader) {
        writeInt(writer, record, "ellipsoid_code");
        writeString(writer, record, "ellipsoid_name");
        writeInt(writer, record, "uom_code");
        writeDouble(writer, record, "semi_minor_axis");
        writeDouble(writer, record, "semi_major_axis");
        writeDouble(writer, record, "inv_flattening");
        writeByte(writer, record, "ellipsoid_shape");
        writeDeprecated(writer, record);
      }
    }
  }

  private void loadPrimeMeridians() throws IOException {
    final Query query = new Query("/public/epsg_primemeridian") //
      .setOrderBy("prime_meridian_code");
    try (
      final RecordReader reader = this.recordStore.getRecords(query);
      ChannelWriter writer = newWriter("primeMeridian")) {
      for (final Record record : reader) {
        writeInt(writer, record, "prime_meridian_code");
        writeString(writer, record, "prime_meridian_name");
        writeInt(writer, record, "uom_code");
        writeDouble(writer, record, "greenwich_longitude");

        if (isDeprecated(record)) {
          System.err.println("Add deprecated support to prime meridian");
        }
      }
    }
  }

  private void loadUnitsOfMeasure() throws IOException {

    final Map<Integer, Record> baseUnits = new TreeMap<>();
    final Map<Integer, List<Record>> unitsByBase = new TreeMap<>();

    try (
      final RecordReader reader = newReader("/public/epsg_unitofmeasure")) {

      for (final Record record : reader) {
        final int code = record.getInteger("uom_code");
        final int baseUnitCode = record.getInteger("target_uom_code");
        if (code == baseUnitCode) {
          baseUnits.put(code, record);
        } else {
          Maps.addToList(unitsByBase, baseUnitCode, record);
        }
      }
    }
    try (
      ChannelWriter writer = newWriter("unitOfMeasure")) {
      for (final Entry<Integer, Record> entry : baseUnits.entrySet()) {
        final int code = entry.getKey();
        final Record record = baseUnits.get(code);
        writeUnitOfMeasure(unitsByBase, record, writer);
      }
    }
  }

  private RecordReader newReader(final String path) {
    return this.recordStore.getRecords(PathName.newPathName(path));
  }

  private RecordReader newReader(final String path, final String... orderByFieldNames) {
    final Query query = new Query(path);
    for (final String fieldName : orderByFieldNames) {
      query.addOrderBy(fieldName);
    }
    return this.recordStore.getRecords(query);
  }

  private ChannelWriter newWriter(final String name) {
    return this.baseResource.newChildResource(name + ".bin").newChannelWriter();
  }

  private void writeByte(final ChannelWriter writer, final Record record, final String fieldName) {
    writer.putByte(record.getByte(fieldName));
  }

  private void writeCodeByte(final ChannelWriter writer, final Record object,
    final String fieldName, final List<String> codes) {
    final String code = object.getValue(fieldName);
    final int index = codes.indexOf(code);
    writer.putByte((byte)index);
  }

  private void writeDeprecated(final ChannelWriter writer, final Record record) {
    final boolean deprecated = isDeprecated(record);
    if (deprecated) {
      writer.putByte((byte)0);
    } else {
      writer.putByte((byte)1);
    }
  }

  private void writeDouble(final ChannelWriter writer, final Record record,
    final String fieldName) {
    final double semiMinorAxis = record.getDouble(fieldName, Double.NaN);
    writer.putDouble(semiMinorAxis);
  }

  private int writeInt(final ChannelWriter writer, final Record record, final String fieldName) {
    final int value = record.getInteger(fieldName);
    writer.putInt(value);
    return value;
  }

  private int writeInt(final ChannelWriter writer, final Record record, final String fieldName,
    final int defaultValue) {
    final int value = record.getInteger(fieldName, defaultValue);
    writer.putInt(value);
    return value;
  }

  private String writeString(final ChannelWriter writer, final Record record,
    final String fieldName) {
    final String value = record.getValue(fieldName);
    writer.putStringUtf8ByteCount(value);
    return value;
  }

  private void writeUnitOfMeasure(final Map<Integer, List<Record>> unitsByBase, final Record record,
    final ChannelWriter writer) {
    final int code = writeInt(writer, record, "uom_code");
    writeCodeByte(writer, record, "unit_of_meas_type", UnitOfMeasure.TYPE_NAMES);
    writeInt(writer, record, "target_uom_code");
    writeDeprecated(writer, record);
    writeDouble(writer, record, "factor_b");
    writeDouble(writer, record, "factor_c");
    writeString(writer, record, "unit_of_meas_name");

    final List<Record> derivedUnits = unitsByBase.get(code);
    if (derivedUnits != null) {
      for (final Record derivedRecord : derivedUnits) {
        writeUnitOfMeasure(unitsByBase, derivedRecord, writer);
      }
    }
  }
}
