package com.revolsys.record.io.format.esri.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.Maps;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.RecordState;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.json.JsonParser;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;
import com.revolsys.util.function.Function2;

public class ArcGisRestServerFeatureIterator extends AbstractIterator<Record>
  implements RecordReader {
  private static Map<DataType, Function2<GeometryFactory, Map<String, Object>, Geometry>> GEOMETRY_CONVERTER_BY_TYPE = new HashMap<>();

  static {
    GEOMETRY_CONVERTER_BY_TYPE.put(DataTypes.POINT, ArcGisRestServerFeatureIterator::parsePoint);
  }

  private static Geometry parsePoint(final GeometryFactory geometryFactory,
    final Map<String, Object> properties) {
    final double x = Maps.getDouble(properties, "x");
    final double y = Maps.getDouble(properties, "y");
    final double z = Maps.getDouble(properties, "z", Double.NaN);
    final double m = Maps.getDouble(properties, "m", Double.NaN);
    if (Double.isNaN(m)) {
      if (Double.isNaN(z)) {
        return geometryFactory.point(x, y);
      } else {
        return geometryFactory.point(x, y, z);
      }
    } else {
      return geometryFactory.point(x, y, z, m);
    }
  }

  private JsonParser in;

  private RecordDefinition recordDefinition;

  private RecordFactory<?> recordFacory;

  private Function2<GeometryFactory, Map<String, Object>, Geometry> geometryConverter;

  private GeometryFactory geometryFactory;

  public ArcGisRestServerFeatureIterator(final RecordDefinition recordDefinition,
    final Resource resource, final RecordFactory<?> recordFactory) {
    this.recordDefinition = recordDefinition;
    this.in = new JsonParser(resource);
    this.recordFacory = recordFactory;
    if (recordDefinition.hasGeometryField()) {
      final DataType geometryType = recordDefinition.getGeometryField().getDataType();
      this.geometryConverter = GEOMETRY_CONVERTER_BY_TYPE.get(geometryType);
      this.geometryFactory = recordDefinition.getGeometryFactory();
    }
  }

  @Override
  protected void closeDo() {
    FileUtil.closeSilent(this.in);
    this.in = null;
    this.recordDefinition = null;
    this.recordFacory = null;
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Record getNext() throws NoSuchElementException {
    if (this.in.skipToNextObjectInArray()) {
      final Map<String, Object> values = this.in.getMap();
      final Record record = this.recordFacory.newRecord(this.recordDefinition);
      record.setState(RecordState.INITIALIZING);

      final Map<String, Object> attributes = (Map<String, Object>)values.get("attributes");
      record.setValues(attributes);
      if (this.geometryConverter != null) {
        final Map<String, Object> geometryProperties = (Map<String, Object>)values.get("geometry");
        if (Property.hasValue(geometryProperties)) {
          final Geometry geometry = this.geometryConverter.apply(this.geometryFactory,
            geometryProperties);
          record.setGeometryValue(geometry);
        }
      }
      if (this.in.hasNext()) {
        this.in.next();
      }
      record.setState(RecordState.PERSISTED);
      return record;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  @Override
  protected void initDo() {
    this.in.skipToAttribute("features");
  }

}
