package com.revolsys.io.shp;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel.MapMode;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.data.io.DataObjectIterator;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.io.EndianInputStream;
import com.revolsys.gis.io.EndianMappedByteBuffer;
import com.revolsys.gis.io.LittleEndianRandomAccessFile;
import com.revolsys.io.EndianInput;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.shp.geometry.JtsGeometryConverter;
import com.revolsys.io.xbase.XbaseIterator;
import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.spring.SpringUtil;
import com.vividsolutions.jts.geom.Geometry;

public class ShapefileIterator extends AbstractIterator<DataObject> implements
  DataObjectIterator {

  private final DataObjectFactory dataObjectFactory;

  private JtsGeometryConverter geometryReader;

  private EndianInput in;

  private DataObjectMetaData metaData;

  private final String name;

  private final Resource resource;

  private XbaseIterator xbaseIterator;

  private int shapeType;

  private EndianMappedByteBuffer indexIn;

  private boolean closeFile = true;

  private boolean mappedFile;

  private int position;

  public ShapefileIterator(final Resource resource,
    final DataObjectFactory factory) throws IOException {
    this.dataObjectFactory = factory;
    final String baseName = FileUtil.getBaseName(resource.getFilename());
    name = baseName;
    this.resource = resource;
  }

  @Override
  protected void doClose() {
    if (closeFile) {
      forceClose();
    }
  }

  @Override
  protected synchronized void doInit() {
    if (in == null) {
      try {
        final Boolean memoryMapped = getProperty("memoryMapped");
        try {
          final File file = SpringUtil.getFile(resource);
          final File indexFile = new File(file.getParentFile(), name + ".shx");
          if (Boolean.TRUE == memoryMapped) {
            this.in = new EndianMappedByteBuffer(file, MapMode.READ_ONLY);
            this.indexIn = new EndianMappedByteBuffer(indexFile,
              MapMode.READ_ONLY);
            this.mappedFile = true;
          } else {
            this.in = new LittleEndianRandomAccessFile(file, "r");
          }
        } catch (final IllegalArgumentException e) {
          this.in = new EndianInputStream(resource.getInputStream());
        } catch (final FileNotFoundException e) {
          this.in = new EndianInputStream(resource.getInputStream());
        }

        final Resource xbaseResource = this.resource.createRelative(name
          + ".dbf");
        if (xbaseResource.exists()) {
          xbaseIterator = new XbaseIterator(xbaseResource,
            this.dataObjectFactory, new InvokeMethodRunnable(this,
              "updateMetaData"));
          xbaseIterator.setProperty("memoryMapped", memoryMapped);
          xbaseIterator.setCloseFile(closeFile);
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
        final Resource projResource = this.resource.createRelative(name
          + ".prj");
        if (projResource.exists()) {
          try {
            final CoordinateSystem coordinateSystem = EsriCoordinateSystems.getCoordinateSystem(projResource);
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
          metaData = new DataObjectMetaDataImpl(
            DataObjectUtil.GEOMETRY_META_DATA);
        }
        metaData.setGeometryFactory(geometryFactory);
      } catch (final IOException e) {
        throw new RuntimeException("Error initializing mappedFile " + resource,
          e);
      }
    }
  }

  public void forceClose() {
    FileUtil.closeSilent(in);
    FileUtil.closeSilent(indexIn);
    if (xbaseIterator != null) {
      xbaseIterator.forceClose();
    }
  }

  public DataObjectFactory getDataObjectFactory() {
    return dataObjectFactory;
  }

  @Override
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
            position++;
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

  public int getPosition() {
    return position;
  }

  public boolean isCloseFile() {
    return closeFile;
  }

  /**
   * Load the header record from the shape mappedFile.
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

  public void setCloseFile(final boolean closeFile) {
    this.closeFile = closeFile;
    if (xbaseIterator != null) {
      xbaseIterator.setCloseFile(closeFile);
    }
  }

  public void setPosition(final int position) {
    if (mappedFile) {
      final EndianMappedByteBuffer file = (EndianMappedByteBuffer)in;
      this.position = position;
      try {
        indexIn.seek(100 + 8 * position);
        final int offset = indexIn.readInt();
        file.seek(offset * 2);
        setLoadNext(true);
      } catch (final IOException e) {
        throw new RuntimeException("Unable to find record " + position, e);
      }
      if (xbaseIterator != null) {
        xbaseIterator.setPosition(position);
      }
    } else {
      throw new UnsupportedOperationException(
        "The position can only be set on files");
    }
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
