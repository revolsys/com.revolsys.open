/*
 * $URL: https://secure.revolsys.com/svn/open.revolsys.com/GIS/trunk/src/java/com/revolsys/gis/format/xbase/io/XbaseFileReader.java $
 * $Author: paul.austin@revolsys.com $
 * $Date: 2006-01-31 15:41:41 -0800 (Tue, 31 Jan 2006) $
 * $Revision: 76 $

 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.io.gpx;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.DataObjectIterator;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleListCoordinatesList;
import com.revolsys.io.FileUtil;
import com.revolsys.io.xml.StaxUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

public class GpxIterator implements DataObjectIterator {
  private static final DateTimeFormatter XML_DATE_TIME_FORMAT = ISODateTimeFormat.dateTimeNoMillis();

  private static final XMLInputFactory FACTORY = XMLInputFactory.newInstance();

  private static final Logger log = Logger.getLogger(GpxIterator.class);

  private static XMLStreamReader createXmlReader(final Reader in) {
    try {
      return FACTORY.createXMLStreamReader(in);
    } catch (final XMLStreamException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }

  private DataObject currentDataObject;

  private DataObjectFactory dataObjectFactory;

  private File file;

  private final GeometryFactory geometryFactory = GeometryFactory.getFactory(4326);

  private boolean hasNext = true;

  private final XMLStreamReader in;

  private boolean loadNextObject = true;

  private String schemaName = GpxConstants.GPX_NS_URI;

  private QName typeName;

  private String baseName;

  private int index = 0;

  private final Queue<DataObject> objects = new LinkedList<DataObject>();

  public GpxIterator(final File file) throws IOException, XMLStreamException {
    this(new FileReader(file));
  }

  public GpxIterator(final Reader in) throws IOException, XMLStreamException {
    this(createXmlReader(in));
  }

  public GpxIterator(final Reader in,
    final DataObjectFactory dataObjectFactory, final QName typeName) {
    this(createXmlReader(in));
    this.dataObjectFactory = dataObjectFactory;
    this.typeName = typeName;
  }

  public GpxIterator(final Resource resource,
    final DataObjectFactory dataObjectFactory, final QName typeName)
    throws IOException {
    this(createXmlReader(new InputStreamReader(resource.getInputStream())));
    this.dataObjectFactory = dataObjectFactory;
    this.typeName = typeName;
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

  public DataObjectMetaData getMetaData() {
    return GpxConstants.GPX_TYPE;
  }

  public String getSchemaName() {
    return schemaName;
  }

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
      } while (currentDataObject != null && typeName != null
        && !currentDataObject.getMetaData().getName().equals(typeName));
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

  public DataObject next() {
    if (hasNext()) {
      loadNextObject = true;
      return currentDataObject;
    } else {
      throw new NoSuchElementException();
    }
  }

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

  //
  // private SimpleAttribute processAttribute() throws XMLStreamException {
  // String propertySchemaName = in.getNamespaceURI();
  // String propertyName = in.getLocalName();
  // in.require(XMLStreamReader.START_ELEMENT, null, null);
  // if (in.getName().equals(GpxConstants.EXTENSION_ELEMENT)
  // || in.getName().equals(GpxConstants.TRACK_SEGMENT_ELEMENT)) {
  // StaxUtils.skipSubTree(in);
  // in.require(XMLStreamReader.END_ELEMENT, propertySchemaName, propertyName);
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
  // in.require(XMLStreamReader.END_ELEMENT, propertySchemaName, propertyName);
  // return attribute;
  // }

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
        final String attributeName = in.getLocalName();
        final String value = StaxUtils.getElementText(in);
        if (value != null) {
          dataObject.setValue(attributeName, value);
        }
      }
    }

    Coordinate coord = null;
    if (Double.isNaN(elevation)) {
      coord = new Coordinate(lon, lat);
    } else {
      coord = new Coordinate(lon, lat, elevation);
    }

    final Point point = geometryFactory.createPoint(coord);
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
    int numAxis = 2;
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (in.getName().equals(GpxConstants.EXTENSION_ELEMENT)) {
        StaxUtils.skipSubTree(in);
      } else if (in.getName().equals(GpxConstants.ROUTE_POINT_ELEMENT)) {
        final double pointIndex = index + (pointObjects.size() + 1.0) / 10000;
        final DataObject pointObject = parseRoutPoint(pointIndex);
        pointObjects.add(pointObject);
        final Point point = pointObject.getGeometryValue();
        final Coordinates coordinates = CoordinatesUtil.get(point);
        numAxis = Math.max(numAxis, coordinates.getNumAxis());
      } else {
        final String attributeName = in.getLocalName();
        final String value = StaxUtils.getElementText(in);
        if (value != null) {
          dataObject.setValue(attributeName, value);
        }
      }
    }
    final CoordinatesList points = new DoubleCoordinatesList(
      pointObjects.size(), numAxis);
    for (int i = 0; i < points.size(); i++) {
      final DataObject pointObject = pointObjects.get(i);
      final Point point = pointObject.getGeometryValue();
      final Coordinates coordinates = CoordinatesUtil.get(point);
      for (int j = 0; j < numAxis; j++) {
        final double value = coordinates.getValue(j);
        points.setValue(i, j, value);
      }
    }
    final LineString line = geometryFactory.createLineString(points);
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
        segments.add(points);
      } else {
        final String attributeName = in.getLocalName();
        final String value = StaxUtils.getElementText(in);
        if (value != null) {
          dataObject.setValue(attributeName, value);
        }
      }
    }
    final MultiLineString lines = geometryFactory.createMultiLineString(segments);
    dataObject.setGeometryValue(lines);
    return dataObject;
  }

  private int parseTrackPoint(final CoordinatesList points)
    throws XMLStreamException {
    final int index = points.size();

    final String lonText = in.getAttributeValue("", "lon");
    final double lon = Double.parseDouble(lonText);
    points.setX(index, lon);

    final String latText = in.getAttributeValue("", "lat");
    final double lat = Double.parseDouble(latText);
    points.setY(index, lat);

    int numAxis = 2;

    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (in.getName().equals(GpxConstants.EXTENSION_ELEMENT)
        || in.getName().equals(GpxConstants.TRACK_SEGMENT_ELEMENT)) {
        StaxUtils.skipSubTree(in);
      } else {
        if (in.getName().equals(GpxConstants.ELEVATION_ELEMENT)) {
          final String elevationText = StaxUtils.getElementText(in);
          final double elevation = Double.parseDouble(elevationText);
          points.setZ(index, elevation);
          if (numAxis < 3) {
            numAxis = 3;
          }
        } else if (in.getName().equals(GpxConstants.TIME_ELEMENT)) {
          final String dateText = StaxUtils.getElementText(in);
          final DateTime date = XML_DATE_TIME_FORMAT.parseDateTime(dateText);
          final long time = date.getMillis();
          points.setTime(index, time);
          if (numAxis < 4) {
            numAxis = 4;
          }
        } else {
          // TODO decide if we want to handle the metadata on a track point
          StaxUtils.skipSubTree(in);
        }
      }
    }

    return numAxis;
  }

  private CoordinatesList parseTrackSegment() throws XMLStreamException {
    final CoordinatesList points = new DoubleListCoordinatesList(4);
    int numAxis = 2;
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final int pointNumAxis = parseTrackPoint(points);
      numAxis = Math.max(numAxis, pointNumAxis);
    }
    return new DoubleCoordinatesList(numAxis, points);
  }

  private DataObject parseWaypoint() throws XMLStreamException {
    index++;
    final String featureType = "wpt";
    return parsePoint(featureType, index);
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  public void setSchemaName(final String schemaName) {
    this.schemaName = schemaName;
  }

  public void skipMetaData() throws XMLStreamException {
    StaxUtils.require(in, GpxConstants.GPX_ELEMENT);
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
