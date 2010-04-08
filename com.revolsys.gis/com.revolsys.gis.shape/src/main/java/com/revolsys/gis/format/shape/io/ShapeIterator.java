package com.revolsys.gis.format.shape.io;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.namespace.QName;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.WktCsParser;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.format.shape.io.geometry.JtsGeometryConverter;
import com.revolsys.gis.format.xbase.io.XbaseIterator;
import com.revolsys.gis.io.EndianInputStream;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.FileUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

public class ShapeIterator extends AbstractObjectWithProperties implements
  Iterator<DataObject> {

  private DataObject currentObject;

  private DataObjectFactory factory;

  private JtsGeometryConverter geometryReader;

  private EndianInputStream in;

  private DataObjectMetaDataImpl metaData;

  private QName name;

  private XbaseIterator xbaseIterator;

  public ShapeIterator(
    final File file,
    final DataObjectFactory factory)
    throws IOException {
    this.factory = factory;
    final File parentFile = file.getParentFile();
    final String baseName = FileUtil.getFileNamePrefix(file);
    name = new QName(baseName);
    final FileInputStream in = new FileInputStream(file);
    this.in = new EndianInputStream(in);

    final File xbaseFile = new File(parentFile, baseName + ".dbf");
    if (xbaseFile.exists()) {
      xbaseIterator = new XbaseIterator(xbaseFile, factory);
    }
    final File projFile = new File(parentFile, baseName + ".prj");
    if (projFile.exists()) {
      try {
        CoordinateSystem coordinateSystem = new WktCsParser(
          new FileInputStream(projFile)).parse();
        coordinateSystem = EsriCoordinateSystems.getCoordinateSystem(coordinateSystem);
        final int crsId = EsriCoordinateSystems.getCrsId(coordinateSystem);
        if (crsId != 0) {
          coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(crsId);
        }
        setProperty("coordinateSystem", coordinateSystem);
        final PrecisionModel precisionModel = new PrecisionModel();
        geometryReader = new JtsGeometryConverter(new GeometryFactory(
          precisionModel, crsId));
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }
    loadHeader();
    readNextRecord();

  }

  public ShapeIterator(
    final QName name,
    final InputStream in,
    final XbaseIterator xbaseIterator,
    final DataObjectFactory factory) {
    init(name, in, xbaseIterator);
  }

  public void close() {
    try {
      in.close();
    } catch (final IOException e) {
    }
    if (xbaseIterator != null) {
      xbaseIterator.close();
    }
  }

  public DataObjectMetaDataImpl getMetaData() {
    return metaData;
  }

  public boolean hasNext() {
    return currentObject != null;
  }

  private void init(
    final QName name,
    final InputStream in,
    final XbaseIterator xbaseIterator) {
    this.name = name;
    final PrecisionModel precisionModel = new PrecisionModel();
    geometryReader = new JtsGeometryConverter(new GeometryFactory(
      precisionModel, 4326));
    try {
      this.in = new EndianInputStream(in);
      this.xbaseIterator = xbaseIterator;
      loadHeader();
      readNextRecord();
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Load the header record from the shape file.
   * 
   * @throws IOException If an I/O error occurs.
   */
  private void loadHeader()
    throws IOException {
    in.readInt();
    in.skipBytes(20);
    in.readInt();
    in.readLEInt();
    in.readLEInt();
    in.readDouble();
    in.readDouble();
    in.readDouble();
    in.readDouble();
    in.readDouble();
    in.readDouble();
    in.readDouble();
    in.readDouble();
    if (xbaseIterator != null) {
      metaData = xbaseIterator.getMetaData();
    } else {
      metaData = new DataObjectMetaDataImpl(name);
    }
    metaData.addAttribute("geometry", DataTypes.GEOMETRY, true);
  }

  public DataObject next() {
    if (hasNext()) {
      final DataObject object = currentObject;
      readNextRecord();
      return object;
    } else {
      return null;
    }
  }

  private void readNextRecord() {
    if (xbaseIterator != null) {
      if (xbaseIterator.hasNext()) {
        currentObject = xbaseIterator.next();
      } else {
        currentObject = null;
        close();
        return;
      }
    } else {
      currentObject = factory.createDataObject(metaData);
    }

    try {
      if (xbaseIterator != null) {
        for (int i = 0; i < xbaseIterator.getDeletedCount(); i++) {
          geometryReader.readGeometry(in);
        }
      }
      final Geometry geometry = geometryReader.readGeometry(in);
      currentObject.setGeometryValue(geometry);
    } catch (final EOFException e) {
      currentObject = null;
      return;
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

}
