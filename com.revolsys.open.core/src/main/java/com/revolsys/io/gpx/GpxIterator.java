package com.revolsys.io.gpx;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.DataObjectIterator;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.FileUtil;
import com.revolsys.io.xml.StaxUtils;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.util.DateUtil;

public class GpxIterator implements DataObjectIterator {
  private static final DateTimeFormatter XML_DATE_TIME_FORMAT = ISODateTimeFormat.dateTimeParser();

  private static final XMLInputFactory FACTORY = XMLInputFactory.newInstance();

  private static final Logger log = Logger.getLogger(GpxIterator.class);

  private DataObject currentDataObject;

  private DataObjectFactory dataObjectFactory;

  private File file;

  private final com.revolsys.jts.geom.GeometryFactory geometryFactory = GeometryFactory.getFactory(4326);

  private boolean hasNext = true;

  private final XMLStreamReader in;

  private boolean loadNextObject = true;

  private String schemaName = GpxConstants.GPX_NS_URI;

  private String typePath;

  private String baseName;

  private int index = 0;

  private final Queue<DataObject> objects = new LinkedList<DataObject>();

  public GpxIterator(final File file) throws IOException, XMLStreamException {
    this(new FileReader(file));
  }

  public GpxIterator(final Reader in) throws IOException, XMLStreamException {
    this(StaxUtils.createXmlReader(in));
  }

  public GpxIterator(final Reader in,
    final DataObjectFactory dataObjectFactory, final String path) {
    this(StaxUtils.createXmlReader(in));
    this.dataObjectFactory = dataObjectFactory;
    this.typePath = path;
  }

  public GpxIterator(final Resource resource,
    final DataObjectFactory dataObjectFactory, final String path)
    throws IOException {
    this(StaxUtils.createXmlReader(resource));
    this.dataObjectFactory = dataObjectFactory;
    this.typePath = path;
    this.baseName = FileUtil.getBaseName(resource.getFilename());
  }

  public GpxIterator(final XMLStreamReader in) {
    this.in = in;
    try {
      StaxUtils.skipToStartElement(in);
      skipMetaData();
    } catch (final XMLStreamException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public void close() {
    try {
      in.close();
    } catch (final XMLStreamException e) {
      log.error(e.getMessage(), e);
    }

  }

  @Override
  public DataObjectMetaData getMetaData() {
    return GpxConstants.GPX_TYPE;
  }

  public String getSchemaName() {
    return schemaName;
  }

  @Override
  public boolean hasNext() {
    if (!hasNext) {
      return false;
    } else if (loadNextObject) {
      return loadNextRecord();
    } else {
      return true;
    }
  }

  protected boolean loadNextRecord() {
    try {
      do {
        currentDataObject = parseDataObject();
      } while (currentDataObject != null && typePath != null
        && !currentDataObject.getMetaData().getPath().equals(typePath));
      loadNextObject = false;
      if (currentDataObject == null) {
        close();
        hasNext = false;
      }
      return hasNext;
    } catch (final XMLStreamException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public DataObject next() {
    if (hasNext()) {
      loadNextObject = true;
      return currentDataObject;
    } else {
      throw new NoSuchElementException();
    }
  }

  protected Object parseAttribute(final DataObject dataObject) {
    final String attributeName = in.getLocalName();
    final String stringValue = StaxUtils.getElementText(in);
    Object value;
    if (stringValue == null) {
      value = null;
    } else if (attributeName.equals("time")) {
      value = DateUtil.getDate("yyyy-MM-dd'T'HH:mm:ss'Z'", stringValue);
    } else {
      value = stringValue;
    }
    if (value != null) {
      dataObject.setValue(attributeName, value);
    }
    return value;
  }

  //
  // private SimpleAttribute processAttribute() throws XMLStreamException {
  // String propertySchemaName = in.getNamespaceURI();
  // String propertyName = in.getLocalName();
  // in.requireLocalPart(XMLStreamReader.START_ELEMENT, null, null);
  // if (in.getName().equals(GpxConstants.EXTENSION_ELEMENT)
  // || in.getName().equals(GpxConstants.TRACK_SEGMENT_ELEMENT)) {
  // StaxUtils.skipSubTree(in);
  // in.requireLocalPart(XMLStreamReader.END_ELEMENT, propertySchemaName,
  // propertyName);
  // return null;
  // }
  // Object value = null;
  // int eventType = StaxUtils.skipWhitespace(in);
  // switch (eventType) {
  // case XMLStreamReader.CHARACTERS:
  // value = in.getText();
  // StaxUtils.skipToEndElement(in);
  // break;
  // case XMLStreamReader.START_ELEMENT:
  // StaxUtils.skipSubTree(in);
  // return null;
  // case XMLStreamReader.END_ELEMENT:
  // value = null;
  // break;
  // default:
  // // assert false : in.getText();
  // break;
  // }
  // SimpleAttribute attribute = new SimpleAttribute(propertyName, value);
  // in.requireLocalPart(XMLStreamReader.END_ELEMENT, propertySchemaName,
  // propertyName);
  // return attribute;
  // }

  private DataObject parseDataObject() throws XMLStreamException {
    if (!objects.isEmpty()) {
      return objects.remove();
    } else {
      if (in.getEventType() != XMLStreamConstants.START_ELEMENT) {
        StaxUtils.skipToStartElement(in);
      }
      while (in.getEventType() == XMLStreamConstants.START_ELEMENT) {
        final QName name = in.getName();
        if (name.equals(GpxConstants.WAYPOINT_ELEMENT)) {
          return parseWaypoint();
        } else if (name.equals(GpxConstants.TRACK_ELEMENT)) {
          return parseTrack();
        } else if (name.equals(GpxConstants.ROUTE_ELEMENT)) {
          return parseRoute();
        } else {
          StaxUtils.skipSubTree(in);
          in.nextTag();
        }
      }
      return null;
    }
  }

  protected DataObject parsePoint(final String featureType, final double index)
    throws XMLStreamException {
    final DataObject dataObject = dataObjectFactory.createDataObject(GpxConstants.GPX_TYPE);
    dataObject.setValue("dataset_name", baseName);
    dataObject.setValue("index", index);
    dataObject.setValue("feature_type", featureType);
    final double lat = Double.parseDouble(in.getAttributeValue("", "lat"));
    final double lon = Double.parseDouble(in.getAttributeValue("", "lon"));
    double elevation = Double.NaN;

    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (in.getName().equals(GpxConstants.EXTENSION_ELEMENT)) {
        StaxUtils.skipSubTree(in);
      } else if (in.getName().equals(GpxConstants.ELEVATION_ELEMENT)) {
        elevation = Double.parseDouble(StaxUtils.getElementText(in));
      } else {
        parseAttribute(dataObject);
      }
    }

    Coordinates coord = null;
    if (Double.isNaN(elevation)) {
      coord = new DoubleCoordinates(lon, lat);
    } else {
      coord = new DoubleCoordinates(lon, lat, elevation);
    }

    final Point point = geometryFactory.point(coord);
    dataObject.setValue("location", point);
    return dataObject;
  }

  private DataObject parseRoute() throws XMLStreamException {
    index++;
    final DataObject dataObject = dataObjectFactory.createDataObject(GpxConstants.GPX_TYPE);
    dataObject.setValue("dataset_name", baseName);
    dataObject.setValue("index", index);
    dataObject.setValue("feature_type", "rte");
    final List<DataObject> pointObjects = new ArrayList<DataObject>();
    int axisCount = 2;
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (in.getName().equals(GpxConstants.EXTENSION_ELEMENT)) {
        StaxUtils.skipSubTree(in);
      } else if (in.getName().equals(GpxConstants.ROUTE_POINT_ELEMENT)) {
        final double pointIndex = index + (pointObjects.size() + 1.0) / 10000;
        final DataObject pointObject = parseRoutPoint(pointIndex);
        pointObjects.add(pointObject);
        final Point point = pointObject.getGeometryValue();
        final Coordinates coordinates = CoordinatesUtil.getInstance(point);
        axisCount = Math.max(axisCount, coordinates.getAxisCount());
      } else {
        parseAttribute(dataObject);
      }
    }
    final int vertexCount = pointObjects.size();
    final double[] coordinates = new double[vertexCount * axisCount];
    for (int i = 0; i < vertexCount; i++) {
      final DataObject pointObject = pointObjects.get(i);
      final Point point = pointObject.getGeometryValue();
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, i, point);
    }
    final LineString line;
    if (vertexCount > 1) {
      line = geometryFactory.lineString(axisCount, coordinates);
    } else {
      line = geometryFactory.lineString();
    }

    dataObject.setGeometryValue(line);
    objects.addAll(pointObjects);
    return dataObject;
  }

  private DataObject parseRoutPoint(final double index)
    throws XMLStreamException {
    final String featureType = "rtept";
    return parsePoint(featureType, index);
  }

  private DataObject parseTrack() throws XMLStreamException {
    index++;
    final DataObject dataObject = dataObjectFactory.createDataObject(GpxConstants.GPX_TYPE);
    dataObject.setValue("dataset_name", baseName);
    dataObject.setValue("index", index);
    dataObject.setValue("feature_type", "trk");
    final List<CoordinatesList> segments = new ArrayList<CoordinatesList>();
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (in.getName().equals(GpxConstants.EXTENSION_ELEMENT)) {
        StaxUtils.skipSubTree(in);
      } else if (in.getName().equals(GpxConstants.TRACK_SEGMENT_ELEMENT)) {
        final CoordinatesList points = parseTrackSegment();
        if (points.size() > 1) {
          segments.add(points);
        }
      } else {
        parseAttribute(dataObject);
      }
    }
    final MultiLineString lines = geometryFactory.multiLineString(segments);
    dataObject.setGeometryValue(lines);
    return dataObject;
  }

  private int parseTrackPoint(final List<Double> points)
    throws XMLStreamException {
    final String lonText = in.getAttributeValue("", "lon");
    final double lon = Double.parseDouble(lonText);
    points.add(lon);

    final String latText = in.getAttributeValue("", "lat");
    final double lat = Double.parseDouble(latText);
    points.add(lat);

    int axisCount = 2;

    double z = Double.NaN;
    double m = Double.NaN;
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (in.getName().equals(GpxConstants.EXTENSION_ELEMENT)
        || in.getName().equals(GpxConstants.TRACK_SEGMENT_ELEMENT)) {
        StaxUtils.skipSubTree(in);
      } else {
        if (in.getName().equals(GpxConstants.ELEVATION_ELEMENT)) {
          final String elevationText = StaxUtils.getElementText(in);
          final double elevation = Double.parseDouble(elevationText);
          z = elevation;
          if (axisCount < 3) {
            axisCount = 3;
          }
        } else if (in.getName().equals(GpxConstants.TIME_ELEMENT)) {
          final String dateText = StaxUtils.getElementText(in);
          final DateTime date = XML_DATE_TIME_FORMAT.parseDateTime(dateText);
          final long time = date.getMillis();
          m = time;
          if (axisCount < 4) {
            axisCount = 4;
          }
        } else {
          // TODO decide if we want to handle the metadata on a track point
          StaxUtils.skipSubTree(in);
        }
      }
    }
    points.add(z);
    points.add(m);
    return axisCount;
  }

  private CoordinatesList parseTrackSegment() throws XMLStreamException {
    final List<Double> coordinates = new ArrayList<Double>();
    int axisCount = 2;
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final int pointAxisCount = parseTrackPoint(coordinates);
      axisCount = Math.max(axisCount, pointAxisCount);
    }
    return new DoubleCoordinatesList(axisCount, coordinates);
  }

  private DataObject parseWaypoint() throws XMLStreamException {
    index++;
    final String featureType = "wpt";
    return parsePoint(featureType, index);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  public void setSchemaName(final String schemaName) {
    this.schemaName = schemaName;
  }

  public void skipMetaData() throws XMLStreamException {
    StaxUtils.requireLocalPart(in, GpxConstants.GPX_ELEMENT);
    StaxUtils.skipToStartElement(in);
    if (in.getName().equals(GpxConstants.METADATA_ELEMENT)) {
      StaxUtils.skipSubTree(in);
      StaxUtils.skipToStartElement(in);
    }
  }

  @Override
  public String toString() {
    return file.getAbsolutePath();
  }

}
