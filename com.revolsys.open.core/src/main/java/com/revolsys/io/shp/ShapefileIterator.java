package com.revolsys.io.shp;

import java.io.EOFException;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.WktCsParser;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.data.io.DataObjectIterator;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.io.EndianInputStream;
import com.revolsys.io.EndianInput;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.shp.geometry.JtsGeometryConverter;
import com.revolsys.io.xbase.XbaseIterator;
import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.vividsolutions.jts.geom.Geometry;

public class ShapefileIterator extends AbstractIterator<DataObject> implements
  DataObjectIterator {

  private final DataObjectFactory dataObjectFactory;

  private JtsGeometryConverter geometryReader;

  private final EndianInput in;

  private DataObjectMetaData metaData;

  private final String name;

  private final Resource resource;

  private XbaseIterator xbaseIterator;

  private int shapeType;

  public ShapefileIterator(final Resource resource,
    final DataObjectFactory factory) throws IOException {
    this.dataObjectFactory = factory;
    final String baseName = FileUtil.getBaseName(resource.getFilename());
    name = baseName;
    this.in = new EndianInputStream(resource.getInputStream());
    this.resource = resource;
  }

  @Override
  protected void doClose() {
    FileUtil.closeSilent(in);
    if (xbaseIterator != null) {
      xbaseIterator.close();
    }
  }

  @Override
  protected synchronized void doInit() {
    try {
      final Resource xbaseResource = this.resource.createRelative(name + ".dbf");
      if (xbaseResource.exists()) {
        xbaseIterator = new XbaseIterator(xbaseResource,
          this.dataObjectFactory, new InvokeMethodRunnable(this,
            "updateMetaData"));
      }
      loadHeader();
      int numAxis = 3;
      int srid = 0;
      if (shapeType < 10) {
        numAxis = 2;
      } else if (shapeType < 20) {
        numAxis = 3;
      } else if (shapeType < 30) {
        numAxis = 4;
      } else {
        numAxis = 4;
      }
      GeometryFactory geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
      final Resource projResource = this.resource.createRelative(name + ".prj");
      if (projResource.exists()) {
        try {
          CoordinateSystem coordinateSystem = new WktCsParser(
            projResource.getInputStream()).parse();
          coordinateSystem = EsriCoordinateSystems.getCoordinateSystem(coordinateSystem);
          srid = EsriCoordinateSystems.getCrsId(coordinateSystem);
          setProperty(IoConstants.GEOMETRY_FACTORY, geometryFactory);
        } catch (final Exception e) {
          e.printStackTrace();
        }
      }
      if (geometryFactory == null) {
        if (srid < 1) {
          srid = 4326;
        }
        geometryFactory = GeometryFactory.getFactory(srid, numAxis);
      }
      geometryReader = new JtsGeometryConverter(geometryFactory);

      if (xbaseIterator != null) {
        xbaseIterator.hasNext();
      }
      if (metaData == null) {
        metaData = new DataObjectMetaDataImpl(DataObjectUtil.GEOMETRY_META_DATA);
      }
      metaData.setGeometryFactory(geometryFactory);
    } catch (final IOException e) {
      throw new RuntimeException("Error initializing file " + resource, e);
    }
  }

  public DataObjectFactory getDataObjectFactory() {
    return dataObjectFactory;
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  @Override
  protected DataObject getNext() {
    DataObject object;
    try {
      if (xbaseIterator != null) {
        if (xbaseIterator.hasNext()) {
          object = xbaseIterator.next();
          for (int i = 0; i < xbaseIterator.getDeletedCount(); i++) {
            geometryReader.readGeometry(in);
          }
        } else {
          throw new NoSuchElementException();
        }
      } else {
        object = dataObjectFactory.createDataObject(metaData);
      }

      final Geometry geometry = geometryReader.readGeometry(in);
      object.setGeometryValue(geometry);
    } catch (final EOFException e) {
      throw new NoSuchElementException();
    } catch (final IOException e) {
      throw new RuntimeException("Error reading geometry " + resource, e);
    }
    return object;
  }

  /**
   * Load the header record from the shape file.
   * 
   * @throws IOException If an I/O error occurs.
   */
  private void loadHeader() throws IOException {
    in.readInt();
    in.skipBytes(20);
    final int fileLength = in.readInt();
    final int version = in.readLEInt();
    shapeType = in.readLEInt();
    final double minX = in.readLEDouble();
    final double minY = in.readLEDouble();
    final double maxX = in.readLEDouble();
    final double maxY = in.readLEDouble();
    final double minZ = in.readLEDouble();
    final double maxZ = in.readLEDouble();
    final double minM = in.readLEDouble();
    final double maxM = in.readLEDouble();
  }

  @Override
  public String toString() {
    return ShapefileConstants.DESCRIPTION + " " + resource;
  }

  public void updateMetaData() {
    assert this.metaData == null : "Cannot override metaData when set";
    if (xbaseIterator != null) {
      final DataObjectMetaDataImpl metaData = xbaseIterator.getMetaData();
      this.metaData = metaData;
      if (metaData.getGeometryAttributeIndex() == -1) {
        metaData.addAttribute("geometry", DataTypes.GEOMETRY, true);
      }
    }
  }

}
