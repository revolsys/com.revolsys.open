package com.revolsys.gis.format.shape.io;

import java.io.EOFException;
import java.io.IOException;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

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
import com.revolsys.gis.format.shape.io.geometry.JtsGeometryConverter;
import com.revolsys.gis.format.xbase.io.XbaseIterator;
import com.revolsys.gis.io.EndianInputStream;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.revolsys.io.EndianInput;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.vividsolutions.jts.geom.Geometry;

public class ShapefileIterator extends AbstractIterator<DataObject> implements
  DataObjectIterator {

  private final DataObjectFactory dataObjectFactory;

  private JtsGeometryConverter geometryReader;

  private final EndianInput in;

  private DataObjectMetaData metaData;

  private final QName name;

  private final Resource resource;

  private XbaseIterator xbaseIterator;

  public ShapefileIterator(final Resource resource,
    final DataObjectFactory factory) throws IOException {
    this.dataObjectFactory = factory;
    final String baseName = FileUtil.getBaseName(resource.getFilename());
    name = QName.valueOf(baseName);
    this.in = new EndianInputStream(resource.getInputStream());
    this.resource = resource;
  }

  public DataObjectFactory getDataObjectFactory() {
    return dataObjectFactory;
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
      final Resource xbaseResource = this.resource.createRelative(name.getLocalPart()
        + ".dbf");
      if (xbaseResource.exists()) {
        xbaseIterator = new XbaseIterator(xbaseResource,
          this.dataObjectFactory, new InvokeMethodRunnable(this,
            "updateMetaData"));
      }
      GeometryFactory geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
      final Resource projResource = this.resource.createRelative(name.getLocalPart()
        + ".prj");
      if (projResource.exists()) {
        try {
          CoordinateSystem coordinateSystem = new WktCsParser(
            projResource.getInputStream()).parse();
          coordinateSystem = EsriCoordinateSystems.getCoordinateSystem(coordinateSystem);
          final int crsId = EsriCoordinateSystems.getCrsId(coordinateSystem);
          if (crsId != 0) {
            coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(crsId);
          }
          setProperty(IoConstants.GEOMETRY_FACTORY, geometryFactory);
          final CoordinatesPrecisionModel precisionModel = new SimpleCoordinatesPrecisionModel();
          geometryFactory = new GeometryFactory(coordinateSystem,
            precisionModel);
        } catch (final Exception e) {
          e.printStackTrace();
        }
      }
      if (geometryFactory == null) {
        geometryFactory = GeometryFactory.getFactory(4326);
      }
      geometryReader = new JtsGeometryConverter(geometryFactory);

      loadHeader();
      if (xbaseIterator != null) {
        xbaseIterator.hasNext();
      }
      if (metaData == null) {
        metaData = DataObjectUtil.GEOMETRY_META_DATA;
      }
    } catch (final IOException e) {
      throw new RuntimeException("Error initializing file " + resource, e);
    }
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
    int fileLength = in.readInt();
    int version = in.readLEInt();
    int shapeType = in.readLEInt();
    double minX = in.readLEDouble();
    double minY = in.readLEDouble();
    double maxX = in.readLEDouble();
    double maxY = in.readLEDouble();
    double minZ = in.readLEDouble();
    double maxZ = in.readLEDouble();
    double minM = in.readLEDouble();
    double maxM = in.readLEDouble();
  }

  public void updateMetaData() {
    assert this.metaData == null : "Cannot override metaData when set";
    if (xbaseIterator != null) {
      DataObjectMetaDataImpl metaData = xbaseIterator.getMetaData();
      this.metaData = metaData;
      if (metaData.getGeometryAttributeIndex() == -1) {
        metaData.addAttribute("geometry", DataTypes.GEOMETRY, true);
      }
    }
  }

  @Override
  public String toString() {
    return ShapefileConstants.DESCRIPTION + " " + resource;
  }

}
