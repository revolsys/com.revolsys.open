package com.revolsys.gdal.record;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.Layer;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.RecordState;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.count.LabelCounters;

public class OgrQueryIterator extends AbstractIterator<Record> {

  private int count;

  private GeometryFactory geometryFactory;

  private final String idFieldName;

  private Layer layer;

  private final int limit;

  private final int offset;

  private Query query;

  private RecordDefinition recordDefinition;

  private RecordFactory<Record> recordFactory;

  private OgrRecordStore recordStore;

  private LabelCounters labelCountMap;

  protected OgrQueryIterator(final OgrRecordStore recordStore, final Query query) {
    this.recordStore = recordStore;
    this.query = query;
    RecordFactory<Record> recordFactory = query.getRecordFactory();
    if (recordFactory == null) {
      recordFactory = recordStore.getRecordFactory();
    }
    this.recordFactory = recordFactory;
    this.recordDefinition = query.getRecordDefinition();
    this.offset = query.getOffset();
    this.limit = query.getLimit();
    this.labelCountMap = query.getStatistics();
    this.geometryFactory = this.recordDefinition.getGeometryFactory();
    this.idFieldName = recordStore.getIdFieldName(this.recordDefinition);
  }

  @Override
  protected synchronized void closeDo() {
    if (this.layer != null) {
      this.recordStore.releaseLayerToClose(this.layer);
    }
    this.geometryFactory = null;
    this.layer = null;
    this.query = null;
    this.recordDefinition = null;
    this.recordFactory = null;
    this.recordStore = null;
    this.labelCountMap = null;
  }

  protected Calendar getCalendar(final Feature feature, final int fieldIndex) {
    final int[] year = new int[1];
    final int[] month = new int[1];
    final int[] day = new int[1];
    final int[] hour = new int[1];
    final int[] minute = new int[1];
    final float[] second = new float[1];
    final int[] timeZoneId = new int[1];
    feature.GetFieldAsDateTime(fieldIndex, year, month, day, hour, minute, second, timeZoneId);
    TimeZone timeZone;
    if (timeZoneId[0] == 100) {
      timeZone = TimeZone.getTimeZone("GMT");
    } else {
      timeZone = TimeZone.getDefault();
    }
    final Calendar calendar = new GregorianCalendar(timeZone);
    calendar.set(Calendar.YEAR, year[0]);
    calendar.set(Calendar.MONTH, month[0]);
    calendar.set(Calendar.DAY_OF_MONTH, day[0]);
    calendar.set(Calendar.HOUR, hour[0]);
    calendar.set(Calendar.MINUTE, minute[0]);
    calendar.set(Calendar.SECOND, (int)Math.floor(second[0]));
    calendar.set(Calendar.MILLISECOND, (int)Math.floor(second[0] * 1000));
    return calendar;
  }

  protected double[] getCoordinates(final org.gdal.ogr.Geometry ogrGeometry, final int axisCount) {
    final int vertexCount = ogrGeometry.GetPointCount();
    final double[] coordinates = new double[vertexCount * axisCount];
    int coordinateIndex = 0;
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      coordinates[coordinateIndex++] = ogrGeometry.GetX(vertexIndex);
      coordinates[coordinateIndex++] = ogrGeometry.GetY(vertexIndex);
      if (axisCount > 2) {
        coordinates[coordinateIndex++] = ogrGeometry.GetZ(vertexIndex);
      }
    }
    return coordinates;
  }

  @Override
  protected Record getNext() throws NoSuchElementException {
    if (this.layer == null) {
      throw new NoSuchElementException();
    } else {
      while (this.offset > 0 && this.count < this.offset) {
        final Feature feature = this.layer.GetNextFeature();
        if (feature == null) {
          throw new NoSuchElementException();
        } else {
          feature.delete();
        }
        this.count++;
      }
      if (this.count - this.offset >= this.limit) {
        throw new NoSuchElementException();
      }
      final Feature feature = this.layer.GetNextFeature();
      this.count++;

      if (feature == null) {
        throw new NoSuchElementException();
      } else {
        try {
          final Record record = this.recordFactory.newRecord(this.recordDefinition);
          record.setState(RecordState.INITIALIZING);
          if (this.labelCountMap == null) {
            this.recordStore.addStatistic("query", record);
          } else {
            this.labelCountMap.addCount(record);
          }

          final int fieldCount = feature.GetFieldCount();
          for (int fieldIndex = 0; fieldIndex < fieldCount; fieldIndex++) {
            final String fieldName = feature.GetFieldDefnRef(fieldIndex).GetName();
            if (feature.IsFieldSet(fieldIndex)) {
              final int fieldType = feature.GetFieldType(fieldIndex);
              Object value;
              switch (fieldType) {
                case 0:
                  value = feature.GetFieldAsInteger(fieldIndex);
                break;
                case 1:
                  value = feature.GetFieldAsIntegerList(fieldIndex);
                break;
                case 2:
                  value = feature.GetFieldAsDouble(fieldIndex);
                break;
                case 3:
                  value = feature.GetFieldAsDoubleList(fieldIndex);
                break;
                case 4:
                case 6:
                  value = feature.GetFieldAsString(fieldIndex);
                break;
                case 5:
                case 7:
                  value = feature.GetFieldAsStringList(fieldIndex);
                break;
                case 8:
                  value = null;
                // binary
                break;
                case 9:
                  final Calendar date = getCalendar(feature, fieldIndex);
                  value = new Date(date.getTimeInMillis());
                break;
                case 10:
                  value = null;
                // time
                break;
                case 11:
                  final Calendar dateTime = getCalendar(feature, fieldIndex);
                  value = new java.util.Date(dateTime.getTimeInMillis());
                break;

                default:
                  value = null;
                break;
              }
              record.setValue(fieldName, value);
            }
          }
          final int geometryCount = feature.GetGeomFieldCount();
          for (int geometryIndex = 0; geometryIndex < geometryCount; geometryIndex++) {
            final String fieldName = feature.GetGeomFieldDefnRef(geometryIndex).GetName();
            final org.gdal.ogr.Geometry ogrGeometry = feature.GetGeomFieldRef(geometryIndex);
            final Geometry geometry = toGeometry(ogrGeometry);
            record.setValue(fieldName, geometry);
          }
          record.setState(RecordState.PERSISTED);
          return record;
        } finally {
          feature.delete();
        }
      }
    }
  }

  @Override
  protected synchronized void initDo() {
    if (this.recordStore != null) {
      final DataSource dataSource = this.recordStore.getDataSource();
      if (dataSource != null) {
        final String sql = this.recordStore.getSql(this.query);
        this.layer = dataSource.ExecuteSQL(sql);
        this.recordStore.addLayerToClose(this.layer);
      }
    }
  }

  protected Geometry toGeometry(final org.gdal.ogr.Geometry ogrGeometry) {
    if (ogrGeometry == null) {
      return null;
    } else {
      final int geometryType = ogrGeometry.GetGeometryType();
      final int axisCount = ogrGeometry.GetCoordinateDimension();
      switch (geometryType) {
        case 1:
        case 0x80000000 + 1:
          final double[] pointCoordinates = getCoordinates(ogrGeometry, axisCount);
          return this.geometryFactory.point(pointCoordinates);
        case 2:
        case 0x80000000 + 2:
          final double[] lineCoordinates = getCoordinates(ogrGeometry, axisCount);
          return this.geometryFactory.lineString(axisCount, lineCoordinates);
        case 3:
        case 0x80000000 + 3:
          final List<LineString> rings = new ArrayList<>();
          for (int partIndex = 0; partIndex < ogrGeometry.GetGeometryCount(); partIndex++) {
            final org.gdal.ogr.Geometry ogrRing = ogrGeometry.GetGeometryRef(partIndex);
            final double[] ringCoordinates = getCoordinates(ogrRing, axisCount);
            final LinearRing ring = this.geometryFactory.linearRing(axisCount, ringCoordinates);
            rings.add(ring);
          }
          return this.geometryFactory.polygon(rings);
        case 4:
        case 0x80000000 + 4:
        case 5:
        case 0x80000000 + 5:
        case 6:
        case 0x80000000 + 6:
        case 7:
        case 0x80000000 + 7:
          final List<Geometry> parts = new ArrayList<>();
          for (int partIndex = 0; partIndex < ogrGeometry.GetGeometryCount(); partIndex++) {
            final org.gdal.ogr.Geometry ogrPart = ogrGeometry.GetGeometryRef(partIndex);
            final Geometry part = toGeometry(ogrPart);
            parts.add(part);
          }
          return this.geometryFactory.geometry(parts);
        case 101:
          final double[] ringCoordinates = getCoordinates(ogrGeometry, axisCount);
          return this.geometryFactory.linearRing(axisCount, ringCoordinates);
        default:
          return null;
      }
    }
  }

  @Override
  public String toString() {
    if (this.query == null) {
      return super.toString();
    } else {
      return this.query.toString();
    }
  }

}
