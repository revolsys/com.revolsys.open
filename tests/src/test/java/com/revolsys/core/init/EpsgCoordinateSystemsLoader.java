package com.revolsys.core.init;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;
import org.jeometry.common.number.Doubles;
import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems.EpsgCoordinateSystemType;
import org.jeometry.coordinatesystem.model.unit.UnitOfMeasure;

import com.revolsys.collection.map.IntHashMap;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;

/**
 * Make sure to watch out for parameter value conversions.
 *
 * @author paustin
 */
public final class EpsgCoordinateSystemsLoader {

  public static final List<String> COORDINATE_REFERENCE_SYSTEM_TYPES = Arrays.asList("geocentric",
    "geographic 3D", "geographic 2D", "projected", "engineering", "vertical", "compound");

  public static final List<String> COORDINATE_OPERATION_TYPES = Arrays.asList("conversion",
    "transformation", "concatenated operation");

  public static final List<String> PARAM_SIGN_REVERSAL = Arrays.asList(null, "No", "Yes");

  public static void main(final String[] args) {
    new EpsgCoordinateSystemsLoader().load();
  }

  private final Resource baseResource = new PathResource(
    "../../jeometry/jeometry-coordinatesystem/src/main/resources/org/jeometry/coordinatesystem/epsg");

  private final RecordStore recordStore;

  private final IntHashMap<MapEx> areaById = new IntHashMap<>();

  private final IntHashMap<String> axisNameById = new IntHashMap<>();

  private final IntHashMap<MapEx> aliasById = new IntHashMap<>();

  private final IntHashMap<MapEx> coordinateSystemById = new IntHashMap<>();

  private final IntHashMap<MapEx> coordinateOperationById = new IntHashMap<>();

  private final IntHashMap<String> coordinateOperationMethodNameById = new IntHashMap<>();

  private final IntHashMap<String> parameterNameById = new IntHashMap<>();

  private final IntHashMap<Map<String, Boolean>> coordinateOperationMethodParamReversals = new IntHashMap<>();

  private final IntHashMap<List<String>> coordinateOperationMethodParamNames = new IntHashMap<>();

  private final IntHashMap<MapEx> coordinateOperationPathById = new IntHashMap<>();

  private final IntHashMap<MapEx> datumById = new IntHashMap<>();

  private final IntHashMap<MapEx> ellipsoidById = new IntHashMap<>();

  private final IntHashMap<MapEx> primeMeridianById = new IntHashMap<>();

  private final IntHashMap<String> unitOfMeasureNameById = new IntHashMap<>();

  private final IntHashMap<MapEx> coordinateReferenceSystemById = new IntHashMap<>();

  public EpsgCoordinateSystemsLoader() {
    final Map<String, Object> parameters = new HashMap<>();
    parameters.put("url", "jdbc:postgresql://localhost:5432/epsg");
    parameters.put("user", "epsg");
    parameters.put("password", "epsg");
    this.recordStore = RecordStore.newRecordStore(parameters);
  }

  private MapEx getCoordinateReferenceSystemById(final int id) {
    MapEx coordinateReferenceSystem = this.coordinateReferenceSystemById.get(id);
    if (coordinateReferenceSystem == null) {
      coordinateReferenceSystem = JsonObject.hash() //
        .add("id", id);
      this.coordinateReferenceSystemById.put(id, coordinateReferenceSystem);
    }
    return coordinateReferenceSystem;
  }

  protected boolean isDeprecated(final Record object) {
    return object.getInteger("deprecated") == 1;
  }

  protected void load() {
    try {
      loadUnitOfMeasure();
      loadAlias();
      loadArea();
      loadCoordinateAxisName();
      loadCoordinateSystem();
      loadCoordinateAxis();
      loadCoordOperationMethod();
      loadCoordOperationParam();
      loadCoordOperationParamUsage();
      loadCoordOperation();
      loadCoordOperationParamValue();
      loadCoordOperationPath();
      loadEllipsoid();
      loadPrimeMeridian();
      loadDatum();
      loadCoordinateReferenceSystem();

      writeJson();
    } catch (final Throwable t) {
      t.printStackTrace();
    }

    // validateEsri();
  }

  private void loadAlias() {
    try (
      final RecordReader reader = newReader("/public/epsg_alias", "alias_code");
      final RecordWriter recordWriter = newTsvWriter(reader);
      ChannelWriter writer = newWriter("alias")) {
      for (final Record record : reader) {
        recordWriter.write(record);
        final int id = writeInt(writer, record, "alias_code");
        writeString(writer, record, "object_table_name");
        writeInt(writer, record, "object_code");
        writeInt(writer, record, "naming_system_code");
        writeString(writer, record, "alias");
        this.aliasById.put(id, record);
      }
    }
  }

  private void loadArea() {
    try (
      final RecordReader reader = newReader("/public/epsg_area", "area_code");
      final RecordWriter recordWriter = newTsvWriter(reader);
      ChannelWriter writer = newWriter("area")) {
      for (final Record record : reader) {
        recordWriter.write(record);
        final int id = writeInt(writer, record, "area_code");
        final String name = writeString(writer, record, "area_name");
        final double minX = writeDouble(writer, record, "area_west_bound_lon");
        final double minY = writeDouble(writer, record, "area_south_bound_lat");
        final double maxX = writeDouble(writer, record, "area_east_bound_lon");
        final double maxY = writeDouble(writer, record, "area_north_bound_lat");
        writeDeprecated(writer, record);
        final String bbox = BoundingBox.bboxToWkt(minX, minY, maxX, maxY);
        this.areaById.put(id, JsonObject.hash() //
          .add("id", id) //
          .add("name", name) //
          .add("bbox", bbox) //
        );
      }
    }
  }

  private void loadCoordinateAxis() {
    try (
      final RecordReader reader = newReader("/public/epsg_coordinateaxis", "coord_sys_code",
        "coord_axis_order");
      final RecordWriter recordWriter = newTsvWriter(reader);
      ChannelWriter writer = newWriter("coordinateAxis");) {
      for (final Record record : reader) {
        recordWriter.write(record);
        final int coordinateSystemId = writeInt(writer, record, "coord_sys_code");
        final int axisNameId = writeInt(writer, record, "coord_axis_name_code");
        final String orientation = writeString(writer, record, "coord_axis_orientation");
        final String abbreviation = record.getValue("coord_axis_abbreviation");
        writer.putByte((byte)abbreviation.charAt(0));
        final int uomId = writeInt(writer, record, "uom_code");
        final MapEx axis = JsonObject.hash() //
          .add("name", this.axisNameById.get(axisNameId))//
          .add("abbreviation", abbreviation)//
          .add("orientation", orientation)//
          .add("units", this.unitOfMeasureNameById.get(uomId))//
        ;
        final MapEx coordinateSystem = this.coordinateSystemById.get(coordinateSystemId);
        final List<MapEx> axes = coordinateSystem.getValue("axes");
        axes.add(axis);
      }
    }
  }

  private void loadCoordinateAxisName() {
    try (
      final RecordReader reader = newReader("/public/epsg_coordinateaxisname",
        "coord_axis_name_code");
      final RecordWriter recordWriter = newTsvWriter(reader);
      ChannelWriter writer = newWriter("coordinateAxisName");) {
      for (final Record record : reader) {
        recordWriter.write(record);
        final int id = writeInt(writer, record, "coord_axis_name_code");
        final String name = writeString(writer, record, "coord_axis_name");
        this.axisNameById.put(id, name);
      }

    }
  }

  private void loadCoordinateReferenceSystem() throws IOException {
    final Map<Integer, List<Record>> recordByKind = new TreeMap<>();
    try (
      final RecordReader reader = newReader("/public/epsg_coordinatereferencesystem",
        "coord_ref_sys_code");
      final RecordWriter recordWriter = newTsvWriter(reader);) {
      for (final Record record : reader) {
        recordWriter.write(record);
        final String code = record.getValue("coord_ref_sys_kind");
        final int kind = COORDINATE_REFERENCE_SYSTEM_TYPES.indexOf(code);
        Maps.addToList(recordByKind, kind, record);
      }
    }
    try (
      ChannelWriter writer = newWriter("coordinateReferenceSystem")) {
      for (final List<Record> records : recordByKind.values()) {
        for (final Record record : records) {
          final int id = writeInt(writer, record, "coord_ref_sys_code");
          final String name = writeString(writer, record, "coord_ref_sys_name");
          final int areaId = writeInt(writer, record, "area_of_use_code");
          final String type = writeCodeByte(writer, record, "coord_ref_sys_kind",
            COORDINATE_REFERENCE_SYSTEM_TYPES);
          final int coordinateSystemId = writeInt(writer, record, "coord_sys_code", 0);
          final int datumId = writeInt(writer, record, "datum_code", 0);
          writeInt(writer, record, "source_geogcrs_code", 0);
          final int coordinateOperationId = writeInt(writer, record, "projection_conv_code", 0);
          final int horizId = writeInt(writer, record, "cmpd_horizcrs_code", 0);
          final int verticalId = writeInt(writer, record, "cmpd_vertcrs_code", 0);
          writeDeprecated(writer, record);

          final MapEx coordinateReferenceSystem = getCoordinateReferenceSystemById(id) //
            .add("name", name) //
            .add("type", type) //
            .add("area", this.areaById.get(areaId)) //
            .add("coordinateSystem", this.coordinateSystemById.get(coordinateSystemId)) //
          ;
          if (datumId > 0) {
            coordinateReferenceSystem.add("datum", this.datumById.get(datumId));
          }
          if (horizId > 0) {
            coordinateReferenceSystem.add("horizontalCoordinateSystem",
              this.coordinateReferenceSystemById.get(horizId));
          }
          if (verticalId > 0) {
            coordinateReferenceSystem.add("verticalCoordinateSystem",
              this.coordinateReferenceSystemById.get(verticalId));
          }
          if (coordinateOperationId > 0) {
            coordinateReferenceSystem.add("coordinateOperation",
              this.coordinateOperationById.get(coordinateOperationId));
          }

        }
      }
    }
  }

  private void loadCoordinateSystem() throws IOException {
    try (
      final RecordReader reader = newReader("/public/epsg_coordinatesystem");
      final RecordWriter recordWriter = newTsvWriter(reader);
      ChannelWriter writer = newWriter("coordinateSystem")) {
      for (final Record record : reader) {
        recordWriter.write(record);
        final int id = writeInt(writer, record, "coord_sys_code");
        final String type = writeCodeByte(writer, record, "coord_sys_type",
          EpsgCoordinateSystemType.TYPE_NAMES);
        writeDeprecated(writer, record);

        final MapEx coordinateSystem = JsonObject.hash() //
          .add("id", id) //
          .add("name", record.get("coord_sys_name")) //
          .add("type", type) //
          .add("axes", new ArrayList<MapEx>());
        this.coordinateSystemById.put(id, coordinateSystem);
      }
    }
  }

  private void loadCoordOperation() {
    try (
      final RecordReader reader = newReader("/public/epsg_coordoperation", "coord_op_code");
      final RecordWriter recordWriter = newTsvWriter(reader);
      ChannelWriter writer = newWriter("coordOperation");) {
      for (final Record record : reader) {
        recordWriter.write(record);
        final int id = writeInt(writer, record, "coord_op_code");
        final int methodId = writeInt(writer, record, "coord_op_method_code", 0);
        final String name = writeString(writer, record, "coord_op_name");
        final String type = writeCodeByte(writer, record, "coord_op_type",
          COORDINATE_OPERATION_TYPES);
        final int sourceCoordinateSystemId = writeInt(writer, record, "source_crs_code", 0);
        final int targetCoordinateSystemId = writeInt(writer, record, "target_crs_code", 0);
        writeString(writer, record, "coord_tfm_version");
        writeInt(writer, record, "coord_op_variant", 0);
        writeInt(writer, record, "area_of_use", 0);
        writeDouble(writer, record, "coord_op_accuracy");
        writeDeprecated(writer, record);
        final JsonObject parameters = JsonObject.hash();
        final List<String> parameterNames = this.coordinateOperationMethodParamNames.get(methodId);
        if (parameterNames != null) {
          for (final String parameterName : parameterNames) {
            parameters.put(parameterName, null);
          }
        }
        final MapEx coordinateOperation = JsonObject.hash() //
          .add("id", id) //
          .add("name", name) //
          .add("type", type);

        if (sourceCoordinateSystemId > 0) {
          coordinateOperation.add("sourceCoordinateSystemId", sourceCoordinateSystemId);
        }
        if (targetCoordinateSystemId > 0) {
          coordinateOperation.add("targetCoordinateSystemId", targetCoordinateSystemId);
        }
        if (methodId > 0) {
          coordinateOperation.add("method", this.coordinateOperationMethodNameById.get(methodId));
        }
        if (!parameters.isEmpty()) {
          coordinateOperation.add("parameters", parameters);
        }
        this.coordinateOperationById.put(id, coordinateOperation);
      }
    }
  }

  private void loadCoordOperationMethod() {
    try (
      RecordReader reader = newReader("/public/epsg_coordoperationmethod");
      final RecordWriter recordWriter = newTsvWriter(reader);
      ChannelWriter writer = newWriter("coordOperationMethod");) {
      for (final Record record : reader) {
        recordWriter.write(record);
        final int id = writeInt(writer, record, "coord_op_method_code");
        final String name = writeString(writer, record, "coord_op_method_name");
        writeByte(writer, record, "reverse_op");
        writeDeprecated(writer, record);
        this.coordinateOperationMethodNameById.put(id, name);
      }
    }
  }

  private void loadCoordOperationParam() {
    try (
      RecordReader reader = newReader("/public/epsg_coordoperationparam", "parameter_code");
      final RecordWriter recordWriter = newTsvWriter(reader);
      ChannelWriter writer = newWriter("coordOperationParam");) {
      for (final Record record : reader) {
        recordWriter.write(record);
        final int id = writeInt(writer, record, "parameter_code");
        final String name = writeString(writer, record, "parameter_name");
        writeDeprecated(writer, record);
        this.parameterNameById.put(id, name);
      }
    }
  }

  /**
   * Order of parameters for a coordOperation
   */
  private void loadCoordOperationParamUsage() {
    try (
      RecordReader reader = newReader("/public/epsg_coordoperationparamusage",
        "coord_op_method_code", "sort_order");
      final RecordWriter recordWriter = newTsvWriter(reader);
      ChannelWriter writer = newWriter("coordOperationParamUsage");) {
      for (final Record record : reader) {
        recordWriter.write(record);
        final int methodId = writeInt(writer, record, "coord_op_method_code");
        final int parameterId = writeInt(writer, record, "parameter_code");
        writeInt(writer, record, "sort_order");
        final String reserse = writeCodeByte(writer, record, "param_sign_reversal",
          PARAM_SIGN_REVERSAL);

        final String parameterName = this.parameterNameById.get(parameterId);
        Map<String, Boolean> reversals = this.coordinateOperationMethodParamReversals.get(methodId);
        if (reversals == null) {
          reversals = new LinkedHashMap<>();
          this.coordinateOperationMethodParamReversals.put(methodId, reversals);
        }
        reversals.put(parameterName, "Yes".equals(reserse));
        List<String> names = this.coordinateOperationMethodParamNames.get(methodId);
        if (names == null) {
          names = new ArrayList<>();
          this.coordinateOperationMethodParamNames.put(methodId, names);
        }
        names.add(parameterName);
      }
    }
  }

  private void loadCoordOperationParamValue() {
    try (
      RecordReader reader = newReader("/public/epsg_coordoperationparamvalue");
      final RecordWriter recordWriter = newTsvWriter(reader);
      ChannelWriter writer = newWriter("coordOperationParamValue");) {
      for (final Record record : reader) {
        recordWriter.write(record);
        final int opertaionId = writeInt(writer, record, "coord_op_code");
        final int methodId = writeInt(writer, record, "coord_op_method_code");
        final int parameterId = writeInt(writer, record, "parameter_code");
        double parameterValue = writeDouble(writer, record, "parameter_value");
        final String fileValue = writeString(writer, record, "param_value_file_ref");
        final int uomCode = writeInt(writer, record, "uom_code", 0);

        final MapEx operation = this.coordinateOperationById.get(opertaionId);
        final MapEx parameters = operation.getValue("parameters");

        final String parameterName = this.parameterNameById.get(parameterId);
        String value;
        if (Double.isFinite(parameterValue)) {
          final Map<String, Boolean> reversals = this.coordinateOperationMethodParamReversals
            .get(methodId);
          if (reversals.get(parameterName) == Boolean.TRUE) {
            parameterValue = -parameterValue;
          }
          value = Doubles.toString(parameterValue) + " " + this.unitOfMeasureNameById.get(uomCode);
        } else {
          value = fileValue;
        }
        parameters.put(parameterName, value);
      }
    }
  }

  private void loadCoordOperationPath() {
    try (
      final RecordReader reader = newReader("/public/epsg_coordoperationpath",
        "concat_operation_code", "op_path_step");
      final RecordWriter recordWriter = newTsvWriter(reader);
      ChannelWriter writer = newWriter("coordOperationPath");) {
      for (final Record record : reader) {
        recordWriter.write(record);
        final int id = writeInt(writer, record, "concat_operation_code");
        final int stepOperationId = writeInt(writer, record, "single_operation_code");
        writeInt(writer, record, "op_path_step");
        final MapEx coordinateOperation = this.coordinateOperationById.get(id);
        List<MapEx> operations = coordinateOperation.getValue("operations");
        if (operations == null) {
          operations = new ArrayList<>();
          coordinateOperation.put("operations", operations);
        }
        final MapEx step = this.coordinateOperationById.get(stepOperationId);
        if (step == null) {
          Logs.error(this, "Cannot find operation " + stepOperationId);
        }
        operations.add(step);
      }
    }
  }

  private void loadDatum() throws IOException {
    final List<String> datumTypes = Arrays.asList("geodetic", "vertical", "engineering");
    try (
      final RecordReader reader = newReader("/public/epsg_datum", "datum_code");
      final RecordWriter recordWriter = newTsvWriter(reader);
      ChannelWriter writer = newWriter("datum")) {
      for (final Record record : reader) {
        recordWriter.write(record);
        final int id = writeInt(writer, record, "datum_code");
        final String name = writeString(writer, record, "datum_name");
        final String type = writeCodeByte(writer, record, "datum_type", datumTypes);
        final int ellipsoidId = writeInt(writer, record, "ellipsoid_code", 0);
        final int primeMeridianId = writeInt(writer, record, "prime_meridian_code", 0);
        final int areaId = writeInt(writer, record, "area_of_use_code");
        writeDeprecated(writer, record);

        final MapEx datum = JsonObject.hash() //
          .add("id", id) //
          .add("name", name) //
          .add("type", type) //
        ;
        if (ellipsoidId > 0) {
          datum.add("ellipsoid", this.ellipsoidById.get(ellipsoidId));
        }
        if (primeMeridianId > 0) {
          datum.add("primeMeridian", this.primeMeridianById.get(primeMeridianId));
        }
        datum.add("area", this.areaById.get(areaId));
        this.datumById.put(id, datum);
      }
    }
  }

  private void loadEllipsoid() throws IOException {
    try (
      final RecordReader reader = newReader("/public/epsg_ellipsoid", "ellipsoid_code");
      final RecordWriter recordWriter = newTsvWriter(reader);
      ChannelWriter writer = newWriter("ellipsoid")) {
      for (final Record record : reader) {
        recordWriter.write(record);
        final int id = writeInt(writer, record, "ellipsoid_code");
        final String name = writeString(writer, record, "ellipsoid_name");
        final int uomId = writeInt(writer, record, "uom_code");
        final double semiMinorAxis = writeDouble(writer, record, "semi_minor_axis");
        final double semiMajorAxis = writeDouble(writer, record, "semi_major_axis");
        final double inverseFlattening = writeDouble(writer, record, "inv_flattening");
        writeByte(writer, record, "ellipsoid_shape");
        writeDeprecated(writer, record);
        final MapEx ellipsoid = JsonObject.hash() //
          .add("id", id) //
          .add("name", name) //
          .add("units", this.unitOfMeasureNameById.get(uomId))//
          .add("semiMajorAxis", semiMajorAxis)//
        ;

        if (Double.isFinite(semiMinorAxis)) {
          ellipsoid.add("semiMinorAxis", semiMinorAxis);
        }
        if (Double.isFinite(inverseFlattening)) {
          ellipsoid.add("inverseFlattening", inverseFlattening);
        }

        this.ellipsoidById.put(id, ellipsoid);
      }
    }
  }

  private void loadPrimeMeridian() throws IOException {
    try (
      final RecordReader reader = newReader("/public/epsg_primemeridian", "prime_meridian_code");
      final RecordWriter recordWriter = newTsvWriter(reader);
      ChannelWriter writer = newWriter("primeMeridian")) {
      for (final Record record : reader) {
        recordWriter.write(record);
        final int id = writeInt(writer, record, "prime_meridian_code");
        final String name = writeString(writer, record, "prime_meridian_name");
        final int uomId = writeInt(writer, record, "uom_code");
        final double longitude = writeDouble(writer, record, "greenwich_longitude");

        final MapEx primeMeridan = JsonObject.hash() //
          .add("id", id) //
          .add("name", name) //
          .add("units", this.unitOfMeasureNameById.get(uomId))//
          .add("longitude", longitude)//
        ;

        this.primeMeridianById.put(id, primeMeridan);
        if (isDeprecated(record)) {
          System.err.println("Add deprecated support to prime meridian");
        }
      }
    }
  }

  private void loadUnitOfMeasure() throws IOException {

    final Map<Integer, Record> baseUnits = new TreeMap<>();
    final Map<Integer, List<Record>> unitsByBase = new TreeMap<>();

    try (
      final RecordReader reader = newReader("/public/epsg_unitofmeasure")) {
      final RecordWriter recordWriter = newTsvWriter(reader);
      for (final Record record : reader) {
        recordWriter.write(record);
        final int id = record.getInteger("uom_code");
        final int baseUnitCode = record.getInteger("target_uom_code");
        if (id == baseUnitCode) {
          baseUnits.put(id, record);
        } else {
          Maps.addToList(unitsByBase, baseUnitCode, record);
        }
      }
    }
    try (
      ChannelWriter writer = newWriter("unitOfMeasure")) {
      for (final Entry<Integer, Record> entry : baseUnits.entrySet()) {
        final int id = entry.getKey();
        final Record record = baseUnits.get(id);
        loadUnitOfMeasure(unitsByBase, record, writer);
      }
    }
  }

  private void loadUnitOfMeasure(final Map<Integer, List<Record>> unitsByBase, final Record record,
    final ChannelWriter writer) {
    final int id = writeInt(writer, record, "uom_code");
    writeCodeByte(writer, record, "unit_of_meas_type", UnitOfMeasure.TYPE_NAMES);
    writeInt(writer, record, "target_uom_code");
    writeDeprecated(writer, record);
    writeDouble(writer, record, "factor_b");
    writeDouble(writer, record, "factor_c");
    final String name = writeString(writer, record, "unit_of_meas_name");

    final List<Record> derivedUnits = unitsByBase.get(id);
    if (derivedUnits != null) {
      for (final Record derivedRecord : derivedUnits) {
        loadUnitOfMeasure(unitsByBase, derivedRecord, writer);
      }
    }
    this.unitOfMeasureNameById.put(id, name);

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

  private RecordWriter newTsvWriter(final RecordReader reader) {
    final RecordDefinition recordDefinition = reader.getRecordDefinition();
    final String name = recordDefinition.getPathName().getName();
    final Resource resource = Resource.getResource("/opt/data/EPSG/" + name + ".tsv");
    resource.createParentDirectories();
    return RecordWriter.newRecordWriter(recordDefinition, resource);
  }

  private ChannelWriter newWriter(final String name) {
    return this.baseResource.newChildResource(name + ".bin").newChannelWriter();
  }

  private void writeByte(final ChannelWriter writer, final Record record, final String fieldName) {
    writer.putByte(record.getByte(fieldName));
  }

  private String writeCodeByte(final ChannelWriter writer, final Record object,
    final String fieldName, final List<String> codes) {
    final String code = object.getValue(fieldName);
    final int index = codes.indexOf(code);
    writer.putByte((byte)index);
    return code;
  }

  private void writeDeprecated(final ChannelWriter writer, final Record record) {
    final boolean deprecated = isDeprecated(record);
    if (deprecated) {
      writer.putByte((byte)1);
    } else {
      writer.putByte((byte)0);
    }
  }

  private double writeDouble(final ChannelWriter writer, final Record record,
    final String fieldName) {
    final double value = record.getDouble(fieldName, Double.NaN);
    writer.putDouble(value);
    return value;
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

  private void writeJson() {
    writeJson("cs", this.coordinateReferenceSystemById);
    writeJson("operation", this.coordinateOperationById);
  }

  private void writeJson(final String name, final IntHashMap<MapEx> valuesById) {
    for (final MapEx coordinateOperation : valuesById.values()) {
      final int id = coordinateOperation.getInteger("id");
      final Resource resource = Resource.getResource("/opt/data/EPSG/" + name + "/" + id + ".json");
      resource.createParentDirectories();
      Json.writeMap(coordinateOperation, resource);
    }
  }

  private String writeString(final ChannelWriter writer, final Record record,
    final String fieldName) {
    final String value = record.getValue(fieldName);
    writer.putStringUtf8ByteCount(value);
    return value;
  }
}
