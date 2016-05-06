package com.revolsys.record.io.format.esri.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.io.FileUtil;
import com.revolsys.logging.Logs;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.RecordState;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.esri.rest.map.RecordLayerDescription;
import com.revolsys.record.io.format.json.JsonParser;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;
import com.revolsys.util.UrlUtil;
import com.revolsys.util.function.Function2;

public class ArcGisRestServerFeatureIterator extends AbstractIterator<Record>
  implements RecordReader {
  private static Map<DataType, Function2<GeometryFactory, MapEx, Geometry>> GEOMETRY_CONVERTER_BY_TYPE = new HashMap<>();

  static {
    GEOMETRY_CONVERTER_BY_TYPE.put(DataTypes.POINT, ArcGisRestServerFeatureIterator::parsePoint);
    GEOMETRY_CONVERTER_BY_TYPE.put(DataTypes.MULTI_POINT,
      ArcGisRestServerFeatureIterator::parseMultiPoint);
    GEOMETRY_CONVERTER_BY_TYPE.put(DataTypes.MULTI_LINE_STRING,
      ArcGisRestServerFeatureIterator::parseMultiLineString);
    GEOMETRY_CONVERTER_BY_TYPE.put(DataTypes.MULTI_POLYGON,
      ArcGisRestServerFeatureIterator::parseMultiPolygon);
  }

  private static Geometry parseMultiLineString(final GeometryFactory geometryFactory,
    final MapEx properties) {
    final List<LineString> lines = new ArrayList<>();
    final List<List<List<Number>>> paths = properties.getValue("paths", Collections.emptyList());
    for (final List<List<Number>> points : paths) {
      final LineString lineString = geometryFactory.lineString(points);
      lines.add(lineString);
    }
    return geometryFactory.geometry(lines);
  }

  private static Geometry parseMultiPoint(final GeometryFactory geometryFactory,
    final MapEx properties) {
    final List<Point> lines = new ArrayList<>();
    final List<List<Number>> paths = properties.getValue("paths", Collections.emptyList());
    for (final List<Number> pointCoordinates : paths) {
      final Point point = geometryFactory.point(pointCoordinates);
      lines.add(point);
    }
    return geometryFactory.geometry(lines);
  }

  private static Geometry parseMultiPolygon(final GeometryFactory geometryFactory,
    final MapEx properties) {
    final List<Polygon> polygons = new ArrayList<>();
    final List<LinearRing> rings = new ArrayList<>();
    final List<List<List<Number>>> paths = properties.getValue("rings", Collections.emptyList());
    for (final List<List<Number>> points : paths) {
      final LinearRing ring = geometryFactory.linearRing(points);
      if (ring.isClockwise()) {
        if (!rings.isEmpty()) {
          final Polygon polygon = geometryFactory.polygon(rings);
          polygons.add(polygon);
        }
        rings.clear();
      }
      rings.add(ring);
    }
    if (!rings.isEmpty()) {
      final Polygon polygon = geometryFactory.polygon(rings);
      polygons.add(polygon);
    }
    return geometryFactory.geometry(polygons);
  }

  private static Geometry parsePoint(final GeometryFactory geometryFactory,
    final MapEx properties) {
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

  private JsonParser parser;

  private boolean closed;

  private RecordDefinition recordDefinition;

  private RecordFactory<?> recordFacory;

  private Function2<GeometryFactory, MapEx, Geometry> geometryConverter;

  private GeometryFactory geometryFactory;

  private int totalRecordCount = 0;

  private int currentRecordCount = 0;

  private final String baseQueryUrl;

  private Map<String, Object> queryParameters;

  private final int queryOffset;

  private final int queryLimit;

  private int serverLimit;

  private final boolean supportsPaging;

  public ArcGisRestServerFeatureIterator(final RecordLayerDescription layer,
    final String baseQueryUrl, final Map<String, Object> queryParameters, final int offset,
    final int limit, final RecordFactory<?> recordFactory) {
    this.baseQueryUrl = baseQueryUrl;
    this.queryParameters = queryParameters;
    this.queryOffset = offset;
    this.queryLimit = limit;
    this.serverLimit = layer.getMaxRecordCount();
    if (this.queryLimit < this.serverLimit) {
      this.serverLimit = this.queryLimit;
    }
    this.recordDefinition = layer.getRecordDefinition();
    this.recordFacory = recordFactory;
    if (this.recordDefinition.hasGeometryField()) {
      final DataType geometryType = this.recordDefinition.getGeometryField().getDataType();
      this.geometryConverter = GEOMETRY_CONVERTER_BY_TYPE.get(geometryType);
      this.geometryFactory = this.recordDefinition.getGeometryFactory();
      if (this.geometryConverter == null) {
        Logs.error(this, "Unsupported geometry type " + geometryType);
        throw new IllegalArgumentException("Unsupported geometry type " + geometryType);
      }
    }
    this.supportsPaging = layer.getCurrentVersion() >= 10.3;
  }

  @Override
  protected void closeDo() {
    FileUtil.closeSilent(this.parser);
    this.parser = null;
    this.geometryConverter = null;
    this.geometryFactory = null;
    this.queryParameters = null;
    this.recordDefinition = null;
    this.recordFacory = null;
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  @Override
  protected Record getNext() throws NoSuchElementException {
    if (this.closed) {
      throw new NoSuchElementException();
    } else {
      if (this.totalRecordCount < this.queryLimit) {
        JsonParser parser = this.parser;
        if (parser == null) {
          parser = newParser();
        }
        if (!parser.skipToNextObjectInArray()) {
          if (this.supportsPaging && this.currentRecordCount == this.serverLimit) {
            parser = newParser();
          }
          if (!parser.skipToNextObjectInArray()) {
            throw new NoSuchElementException();
          }
        }
      } else {
        throw new NoSuchElementException();
      }
      if (!this.closed) {
        try {
          final MapEx recordMap = this.parser.getMap();
          final Record record = this.recordFacory.newRecord(this.recordDefinition);
          record.setState(RecordState.INITIALIZING);

          final MapEx fieldValues = recordMap.getValue("attributes");
          record.setValues(fieldValues);
          if (this.geometryConverter != null) {
            final MapEx geometryProperties = recordMap.getValue("geometry");
            if (Property.hasValue(geometryProperties)) {
              final Geometry geometry = this.geometryConverter.apply(this.geometryFactory,
                geometryProperties);
              record.setGeometryValue(geometry);
            }
          }
          if (this.parser.hasNext()) {
            this.parser.next();
          }
          record.setState(RecordState.PERSISTED);
          this.currentRecordCount++;
          this.totalRecordCount++;
          return record;
        } catch (final Throwable e) {
          if (!this.closed) {
            Logs.debug(this,
              "Error reading: " + UrlUtil.getUrl(this.baseQueryUrl, this.queryParameters), e);
          }
          close();
        }
      }
    }
    throw new NoSuchElementException();
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  protected JsonParser newParser() {
    if (this.closed) {
      throw new NoSuchElementException();
    } else {
      this.currentRecordCount = 0;
      this.queryParameters.put("resultOffset", this.queryOffset + this.totalRecordCount);
      this.queryParameters.put("resultRecordCount", this.serverLimit);
      final Resource resource = Resource
        .getResource(UrlUtil.getUrl(this.baseQueryUrl, this.queryParameters));
      this.parser = new JsonParser(resource);
      if (!this.parser.skipToAttribute("features")) {
        throw new NoSuchElementException();
      }
      return this.parser;
    }
  }
}
