package com.revolsys.gdal.record;

import java.util.List;

import javax.annotation.PreDestroy;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.GeomFieldDefn;
import org.gdal.ogr.Layer;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.logging.Logs;
import org.jeometry.common.number.Integers;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class OgrRecordWriter extends AbstractRecordWriter {
  private DataSource dataSource;

  private OgrRecordStore recordStore;

  OgrRecordWriter(final OgrRecordStore recordStore) {
    super(null);
    this.recordStore = recordStore;
    this.dataSource = recordStore.newDataSource(true);
  }

  private void addParts(final org.gdal.ogr.Geometry ogrGeometry, final Geometry geometry,
    final int geometryType, final int axisCount) {
    for (final Geometry part : geometry.geometries()) {
      final org.gdal.ogr.Geometry ogrRing = toOgrGeometry(part, geometryType, axisCount);
      ogrGeometry.AddGeometry(ogrRing);
    }
  }

  @Override
  @PreDestroy
  public synchronized void close() {
    try {
      if (this.dataSource != null) {
        this.dataSource.delete();
      }
    } finally {
      this.dataSource = null;
      this.recordStore = null;
    }
  }

  private void delete(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final Layer layer = getLayer(recordDefinition);
    final String driverName = this.recordStore.getDriverName();
    if (OgrRecordStore.SQLITE.equals(driverName) || OgrRecordStore.GEO_PAKCAGE.equals(driverName)) {
      final Integer fid = record.getInteger(OgrRecordStore.ROWID);
      if (fid != null) {
        layer.DeleteFeature(fid);
      }
    }
  }

  protected Layer getLayer(final RecordDefinition recordDefinition) {
    final String typePath = recordDefinition.getPath();
    final String layerName = this.recordStore.getLayerName(typePath);
    final Layer layer = this.dataSource.GetLayer(layerName);
    return layer;
  }

  private void insert(final Record record) {
    final RecordDefinition sourceRecordDefinition = record.getRecordDefinition();
    final RecordDefinition recordDefinition = this.recordStore
      .getRecordDefinition(sourceRecordDefinition);
    final String typePath = sourceRecordDefinition.getPath();
    final List<FieldDefinition> attributes = recordDefinition.getFields();
    for (final FieldDefinition attribute : attributes) {
      final String name = attribute.getName();
      if (!recordDefinition.isIdField(name)) {
        if (attribute.isRequired()) {
          final Object value = record.getValue(name);
          if (value == null) {
            throw new IllegalArgumentException(
              "Atribute " + typePath + "." + name + " is required");
          }
        }
      }
    }
    try {
      final Layer layer = getLayer(sourceRecordDefinition);
      final FeatureDefn featureDefinition = layer.GetLayerDefn();
      final Feature feature = new Feature(featureDefinition);
      try {
        setFieldValues(featureDefinition, record, feature);
        setGeometries(featureDefinition, record, feature);
        layer.CreateFeature(feature);
        final String driverName = this.recordStore.getDriverName();
        if (OgrRecordStore.SQLITE.equals(driverName)
          || OgrRecordStore.GEO_PAKCAGE.equals(driverName)) {
          record.setValue(OgrRecordStore.ROWID, feature.GetFieldAsInteger(OgrRecordStore.ROWID));
        }
        record.setState(RecordState.PERSISTED);
      } finally {
        feature.delete();
        this.recordStore.addStatistic("Insert", record);
      }
    } catch (final IllegalArgumentException e) {
      throw new RuntimeException(
        "Unable to insert row " + e.getMessage() + "\n" + record.toString(), e);
    } catch (final RuntimeException e) {
      Logs.debug(OgrRecordWriter.class, "Unable to insert row \n:" + record.toString());
      throw new RuntimeException("Unable to insert row", e);
    }

  }

  @SuppressWarnings("deprecation")
  protected void setFieldValues(final FeatureDefn featureDefinition, final Record record,
    final Feature feature) {
    final int fieldCount = featureDefinition.GetFieldCount();
    for (int fieldIndex = 0; fieldIndex < fieldCount; fieldIndex++) {
      final FieldDefn fieldDefinition = featureDefinition.GetFieldDefn(fieldIndex);
      final String name = fieldDefinition.GetName();
      final Object value = record.getValue(name);
      if (value != null) {
        final int fieldType = fieldDefinition.GetFieldType();
        switch (fieldType) {
          case 0:
            final Integer intValue = Integers.toValid(value);
            if (intValue != null) {
              feature.SetField(fieldIndex, intValue);
            }
          break;
          case 1:
          // value = feature.GetFieldAsIntegerList(fieldIndex);
          break;
          case 2:
            final Double doubleValue = DataTypes.DOUBLE.toObject(value);
            if (doubleValue != null) {
              feature.SetField(fieldIndex, doubleValue);
            }
          break;
          case 3:
          // value = feature.GetFieldAsDoubleList(fieldIndex);
          break;
          case 4:
          case 6:
            final String string = DataTypes.toString(value);
            feature.SetField(fieldIndex, string);
          break;
          case 5:
          case 7:
          // value = feature.GetFieldAsStringList(fieldIndex);
          break;
          case 8:
          // binary
          break;
          case 9:
          case 10:
          case 11:
            final java.util.Date date = DataTypes.DATE_TIME.toObject(value);
            final int year = 1900 + date.getYear();
            final int month = date.getMonth();
            final int day = date.getDay();
            final int hours = date.getHours();
            final int minutes = date.getMinutes();
            final int seconds = date.getSeconds();
            final int timezoneOffset = date.getTimezoneOffset();
            feature.SetField(fieldIndex, year, month, day, hours, minutes, seconds, timezoneOffset);
          break;

          default:
            final String string2 = DataTypes.toString(value);
            feature.SetField(fieldIndex, string2);
          break;
        }
      }
    }
  }

  private void setGeometries(final FeatureDefn featureDefinition, final Record record,
    final Feature feature) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final int geometryFieldCount = featureDefinition.GetGeomFieldCount();
    for (int fieldIndex = 0; fieldIndex < geometryFieldCount; fieldIndex++) {
      final GeomFieldDefn fieldDefinition = featureDefinition.GetGeomFieldDefn(fieldIndex);
      final String name = fieldDefinition.GetName();
      Geometry geometry = record.getValue(name);
      if (geometry != null) {
        final FieldDefinition attribute = recordDefinition.getField(name);
        final GeometryFactory geometryFactory = attribute.getGeometryFactory();
        geometry = geometry.convertGeometry(geometryFactory);
        final int geometryType = fieldDefinition.GetFieldType();
        final int axisCount = geometryFactory.getAxisCount();
        final org.gdal.ogr.Geometry ogrGeometry = toOgrGeometry(geometry, geometryType, axisCount);
        feature.SetGeomField(fieldIndex, ogrGeometry);
      }
    }
  }

  protected org.gdal.ogr.Geometry toOgrGeometry(final Geometry geometry, final int geometryType,
    final int axisCount) {
    final org.gdal.ogr.Geometry ogrGeometry = new org.gdal.ogr.Geometry(geometryType);
    if (!geometry.isEmpty()) {
      switch (geometryType) {
        case 1:
        case 0x80000000 + 1: {
          final Point point = (Point)geometry;
          final double x = point.getX();
          final double y = point.getY();
          if (axisCount == 2) {
            ogrGeometry.AddPoint(x, y);
          } else {
            final double z = point.getZ();
            ogrGeometry.AddPoint(x, y, z);
          }
        }
        break;

        case 2:
        case 0x80000000 + 2:
          final LineString line = (LineString)geometry;
          final int vertexCount = line.getVertexCount();
          for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
            final double x = line.getX(vertexIndex);
            final double y = line.getY(vertexIndex);
            if (axisCount == 2) {
              ogrGeometry.AddPoint(x, y);
            } else {
              final double z = line.getZ(vertexIndex);
              ogrGeometry.AddPoint(x, y, z);
            }
          }
        break;

        case 3:
        case 0x80000000 + 3:
          for (final LinearRing ring : ((Polygon)geometry).rings()) {
            final org.gdal.ogr.Geometry ogrRing = toOgrGeometry(ring, 101, axisCount);
            ogrGeometry.AddGeometry(ogrRing);
          }
        break;
        case 4:
          addParts(ogrGeometry, geometry, 1, axisCount);
        break;

        case 0x80000000 + 4:
          addParts(ogrGeometry, geometry, 0x80000000 + 1, axisCount);
        break;

        case 5:
          addParts(ogrGeometry, geometry, 2, axisCount);
        break;

        case 0x80000000 + 5:
          addParts(ogrGeometry, geometry, 0x80000000 + 2, axisCount);
        break;

        case 6:
          addParts(ogrGeometry, geometry, 3, axisCount);
        break;

        case 0x80000000 + 6:
          addParts(ogrGeometry, geometry, 0x80000000 + 3, axisCount);
        break;

        // case 0x80000000 + 7:
        // final List<Geometry> parts = new ArrayList<>();
        // for (int partIndex = 0; partIndex < geometry. GetGeometryCount();
        // partIndex++) {
        // final org.gdal.ogr.Geometry ogrPart = geometry.
        // GetGeometryRef(partIndex);
        // final Geometry part = toGeometry(ogrPart);
        // parts.add(part);
        // }
        // return this.geometryFactory.geometry(parts);
        case 101:
          for (final Vertex vertex : geometry.vertices()) {
            final double x = vertex.getX();
            final double y = vertex.getY();
            if (axisCount == 2) {
              ogrGeometry.AddPoint(x, y);
            } else {
              final double z = vertex.getZ();
              ogrGeometry.AddPoint(x, y, z);
            }
          }
        break;
        default:
          return null;
      }
    }
    if (axisCount == 2) {
      ogrGeometry.FlattenTo2D();
    }

    return ogrGeometry;
  }

  private void update(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final Layer layer = getLayer(recordDefinition);
    final String driverName = this.recordStore.getDriverName();
    if (OgrRecordStore.SQLITE.equals(driverName) || OgrRecordStore.GEO_PAKCAGE.equals(driverName)) {
      final Integer fid = record.getInteger(OgrRecordStore.ROWID);
      if (fid != null) {
        final Feature feature = layer.GetFeature(fid);
        if (feature != null) {
          final FeatureDefn featureDefinition = layer.GetLayerDefn();
          setFieldValues(featureDefinition, record, feature);
          layer.SetFeature(feature);
        }
      }
    }

  }

  @Override
  public synchronized void write(final Record record) {
    try {
      final RecordDefinition recordDefinition = record.getRecordDefinition();
      final RecordStore recordStore = recordDefinition.getRecordStore();
      if (recordStore == this.recordStore) {
        switch (record.getState()) {
          case NEW:
            insert(record);
          break;
          case MODIFIED:
            update(record);
          break;
          case PERSISTED:
          // No action required
          break;
          case DELETED:
            delete(record);
          break;
          default:
            throw new IllegalStateException("State not known");
        }
      } else {
        insert(record);
      }
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Error e) {
      throw e;
    } catch (final Exception e) {
      throw new RuntimeException("Unable to write", e);
    }
  }
}
