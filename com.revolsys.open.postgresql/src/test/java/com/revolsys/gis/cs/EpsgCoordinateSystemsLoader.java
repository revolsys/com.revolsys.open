package com.revolsys.gis.cs;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.cs.CoordinateOperationMethod;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.CoordinateSystemType;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.ParameterName;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.cs.esri.EsriCoordinateSystems;
import com.revolsys.geometry.cs.unit.UnitOfMeasure;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.PathName;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.util.Property;

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
    "../com.revolsys.open.coordinatesystems/src/main/resources/CoordinateSystems/epsg");

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
      loadAlias();
      loadArea();
      loadCoordinateAxis();
      loadCoordinateAxisName();
      loadCoordinateReferenceSystem();
      loadCoordinateSystem();
      loadCoordOperation();
      loadCoordOperationMethod();
      loadCoordOperationParam();
      loadCoordOperationParamUsage();
      loadCoordOperationParamValue();
      loadCoordOperationPath();
      loadDatum();
      loadEllipsoid();
      loadPrimeMeridian();
      loadUnitOfMeasure();
    } catch (final Throwable t) {
      t.printStackTrace();
    }

    validateEsri();
  }

  private void loadAlias() {
    try (
      final RecordReader reader = newReader("/public/epsg_alias", "alias_code");
      ChannelWriter writer = newWriter("alias")) {
      for (final Record record : reader) {
        writeInt(writer, record, "alias_code");
        writeString(writer, record, "object_table_name");
        writeInt(writer, record, "object_code");
        writeInt(writer, record, "naming_system_code");
        writeString(writer, record, "alias");
      }
    }
  }

  private void loadArea() {
    try (
      final RecordReader reader = newReader("/public/epsg_area", "area_code");
      ChannelWriter writer = newWriter("area")) {
      for (final Record record : reader) {
        writeInt(writer, record, "area_code");
        writeString(writer, record, "area_name");
        writeDouble(writer, record, "area_west_bound_lon");
        writeDouble(writer, record, "area_south_bound_lat");
        writeDouble(writer, record, "area_east_bound_lon");
        writeDouble(writer, record, "area_north_bound_lat");
        writeDeprecated(writer, record);
      }
    }
  }

  private void loadCoordinateAxis() {
    try (
      final RecordReader reader = newReader("/public/epsg_coordinateaxis", "coord_sys_code",
        "coord_axis_order");
      ChannelWriter writer = newWriter("coordinateAxis");) {
      for (final Record record : reader) {
        writeInt(writer, record, "coord_sys_code");
        writeInt(writer, record, "coord_axis_name_code");
        writeString(writer, record, "coord_axis_orientation");
        final String abbreviation = record.getValue("coord_axis_abbreviation");
        writer.putByte((byte)abbreviation.charAt(0));
        writeInt(writer, record, "uom_code");
      }
    }
  }

  private void loadCoordinateAxisName() {
    try (
      final RecordReader reader = newReader("/public/epsg_coordinateaxisname",
        "coord_axis_name_code");
      ChannelWriter writer = newWriter("coordinateAxisName");) {
      for (final Record record : reader) {
        writeInt(writer, record, "coord_axis_name_code");
        writeString(writer, record, "coord_axis_name");
      }

    }
  }

  private void loadCoordinateReferenceSystem() throws IOException {
    final Map<Integer, List<Record>> recordByKind = new TreeMap<>();
    try (
      final RecordReader reader = newReader("/public/epsg_coordinatereferencesystem",
        "coord_ref_sys_code")) {
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
          final int id = writeInt(writer, record, "coord_ref_sys_code");
          writeString(writer, record, "coord_ref_sys_name");
          writeInt(writer, record, "area_of_use_code");
          writeCodeByte(writer, record, "coord_ref_sys_kind", COORDINATE_REFERENCE_SYSTEM_TYPES);
          writeInt(writer, record, "coord_sys_code", 0);
          writeInt(writer, record, "datum_code", 0);
          writeInt(writer, record, "source_geogcrs_code", 0);
          writeInt(writer, record, "projection_conv_code", 0);
          writeInt(writer, record, "cmpd_horizcrs_code", 0);
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
        writeInt(writer, record, "coord_sys_code");
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
      RecordReader reader = newReader("/public/epsg_coordoperationparam", "parameter_code");
      ChannelWriter writer = newWriter("coordOperationParam");) {
      for (final Record record : reader) {
        writeInt(writer, record, "parameter_code");
        writeString(writer, record, "parameter_name");
        writeDeprecated(writer, record);
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
      ChannelWriter writer = newWriter("coordOperationParamUsage");) {
      for (final Record record : reader) {
        writeInt(writer, record, "coord_op_method_code");
        writeInt(writer, record, "parameter_code");
        writeInt(writer, record, "sort_order");
        writeCodeByte(writer, record, "param_sign_reversal", PARAM_SIGN_REVERSAL);
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

  private void loadCoordOperationPath() {
    try (
      final RecordReader reader = newReader("/public/epsg_coordoperationpath",
        "concat_operation_code", "op_path_step");
      ChannelWriter writer = newWriter("coordOperationPath");) {
      for (final Record record : reader) {
        writeInt(writer, record, "concat_operation_code");
        writeInt(writer, record, "single_operation_code");
        writeInt(writer, record, "op_path_step");
      }
    }
  }

  private void loadDatum() throws IOException {
    final List<String> datumTypes = Arrays.asList("geodetic", "vertical", "engineering");
    try (
      final RecordReader reader = newReader("/public/epsg_datum", "datum_code");
      ChannelWriter writer = newWriter("datum")) {
      for (final Record record : reader) {
        writeInt(writer, record, "datum_code");
        writeString(writer, record, "datum_name");
        writeCodeByte(writer, record, "datum_type", datumTypes);
        writeInt(writer, record, "ellipsoid_code", 0);
        writeInt(writer, record, "prime_meridian_code", 0);
        writeInt(writer, record, "area_of_use_code");
        writeDeprecated(writer, record);
      }
    }
  }

  private void loadEllipsoid() throws IOException {
    try (
      final RecordReader reader = newReader("/public/epsg_ellipsoid", "ellipsoid_code");
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

  private void loadPrimeMeridian() throws IOException {
    try (
      final RecordReader reader = newReader("/public/epsg_primemeridian", "prime_meridian_code");
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

  private void loadUnitOfMeasure() throws IOException {

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
        loadUnitOfMeasure(unitsByBase, record, writer);
      }
    }
  }

  private void loadUnitOfMeasure(final Map<Integer, List<Record>> unitsByBase, final Record record,
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
        loadUnitOfMeasure(unitsByBase, derivedRecord, writer);
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

  private void validateEsri() {
    EsriCoordinateSystems.getCoordinateSystem(0);
    for (final CoordinateSystem coordinateSytstem : EpsgCoordinateSystems.getCoordinateSystems()) {
      if (coordinateSytstem instanceof GeographicCoordinateSystem) {
        final GeographicCoordinateSystem geoCs = (GeographicCoordinateSystem)coordinateSytstem;
        final int id = coordinateSytstem.getCoordinateSystemId();
        final GeographicCoordinateSystem esri = EsriCoordinateSystems.getCoordinateSystem(id);
        if (esri != null && !geoCs.equalsExact(esri)) {
          // System.out.println(id + coordinateSytstem.getCoordinateSystemName());
        }
      } else if (coordinateSytstem instanceof ProjectedCoordinateSystem) {
        final ProjectedCoordinateSystem projectedCs = (ProjectedCoordinateSystem)coordinateSytstem;
        final int id = coordinateSytstem.getCoordinateSystemId();
        final String wkt = new UrlResource(
          "http://spatialreference.org/ref/epsg/" + id + "/esriwkt/").contentsAsString();
        final ProjectedCoordinateSystem esri = GeometryFactory.floating2d(wkt)
          .getHorizontalCoordinateSystem();
        final CoordinateOperationMethod coordinateOperationMethod = esri
          .getCoordinateOperationMethod();
        if (esri != null && !projectedCs.equals(esri) && coordinateOperationMethod != null
          && Property.hasValue(coordinateOperationMethod.getName())
          && !projectedCs.isDeprecated()) {
          final Map<ParameterName, Object> p1 = projectedCs.getParameters();
          final Map<ParameterName, Object> p2 = esri.getParameters();
          final Set<ParameterName> n1 = p1.keySet();
          final Set<ParameterName> n2 = p2.keySet();
          if (!n1.equals(n2)) {

            final TreeSet<ParameterName> nm1 = new TreeSet<>(n1);
            nm1.removeAll(n2);
            final TreeSet<ParameterName> nm2 = new TreeSet<>(n2);
            nm2.removeAll(n1);
            final String m = id + "\t" + coordinateSytstem.getCoordinateSystemName() + "\t" + nm1
              + "\t" + nm2;
            // System.out.println(m);
          }
        }
      }
    }
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
      writer.putByte((byte)1);
    } else {
      writer.putByte((byte)0);
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
}
