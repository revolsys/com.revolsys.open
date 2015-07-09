package com.revolsys.gdal.record;

import java.sql.Date;
import java.util.List;

import javax.annotation.PreDestroy;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.GeomFieldDefn;
import org.gdal.ogr.Layer;
import org.slf4j.LoggerFactory;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.property.FieldProperties;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.vertex.Vertex;

public class OgrRecordWriter extends AbstractRecordWriter {
  private OgrRecordStore recordStore;

  private DataSource dataSource;

  OgrRecordWriter(final OgrRecordStore recordStore) {
    this.recordStore = recordStore;
    this.dataSource = recordStore.createDataSource(true);
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
    final List<String> idFieldNames = recordDefinition.getIdFieldNames();
    for (final FieldDefinition attribute : attributes) {
      final String name = attribute.getName();
      if (!idFieldNames.contains(name)) {
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
        record.setState(RecordState.Persisted);
      } finally {
        feature.delete();
        this.recordStore.addStatistic("Insert", record);
      }
    } catch (final IllegalArgumentException e) {
      throw new RuntimeException(
        "Unable to insert row " + e.getMessage() + "\n" + record.toString(), e);
    } catch (final RuntimeException e) {
      if (LoggerFactory.getLogger(OgrRecordWriter.class).isDebugEnabled()) {
        LoggerFactory.getLogger(OgrRecordWriter.class)
          .debug("Unable to insert row \n:" + record.toString());
      }
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
            final Integer intValue = StringConverterRegistry.toObject(Integer.class, value);
            if (intValue != null) {
              feature.SetField(fieldIndex, intValue);
            }
          break;
          case 1:
          // value = feature.GetFieldAsIntegerList(fieldIndex);
          break;
          case 2:
            final Double doubleValue = StringConverterRegistry.toObject(Double.class, value);
            if (doubleValue != null) {
              feature.SetField(fieldIndex, doubleValue);
            }
          break;
          case 3:
          // value = feature.GetFieldAsDoubleList(fieldIndex);
          break;
          case 4:
          case 6:
            final String string = StringConverterRegistry.toString(value);
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
            final java.util.Date date = StringConverterRegistry.toObject(Date.class, value);
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
            final String string2 = StringConverterRegistry.toString(value);
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
        final GeometryFactory geometryFactory = attribute
          .getProperty(FieldProperties.GEOMETRY_FACTORY);
        geometry = geometry.convert(geometryFactory);
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
          final Vertex vertex = geometry.getVertex(0);
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

        case 2:
        case 0x80000000 + 2:
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
          case New:
            insert(record);
          break;
          case Modified:
            update(record);
          break;
          case Persisted:
          // No action required
          break;
          case Deleted:
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
