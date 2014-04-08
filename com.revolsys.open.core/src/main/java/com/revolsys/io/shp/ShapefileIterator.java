package com.revolsys.io.shp;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel.MapMode;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.data.io.DataObjectIterator;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.io.EndianInputStream;
import com.revolsys.gis.io.EndianMappedByteBuffer;
import com.revolsys.gis.io.LittleEndianRandomAccessFile;
import com.revolsys.io.EndianInput;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.xbase.XbaseIterator;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.spring.SpringUtil;

public class ShapefileIterator extends AbstractIterator<DataObject> implements
  DataObjectIterator {

  private boolean closeFile = true;

  private DataObjectFactory dataObjectFactory;

  private com.revolsys.jts.geom.GeometryFactory geometryFactory;

  private EndianInput in;

  private EndianMappedByteBuffer indexIn;

  private boolean mappedFile;

  private DataObjectMetaData metaData;

  private final String name;

  private int position;

  private Resource resource;

  private int shapeType;

  private XbaseIterator xbaseIterator;

  private String typeName;

  private DataObjectMetaData returnMetaData;

  public ShapefileIterator(final Resource resource,
    final DataObjectFactory factory) throws IOException {
    this.dataObjectFactory = factory;
    final String baseName = FileUtil.getBaseName(resource.getFilename());
    name = baseName;
    this.typeName = "/" + name;
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
          xbaseIterator.setTypeName(typeName);
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
        geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
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

        if (xbaseIterator != null) {
          xbaseIterator.hasNext();
        }
        if (metaData == null) {
          metaData = DataObjectUtil.createGeometryMetaData();
        }
        metaData.setGeometryFactory(geometryFactory);
      } catch (final IOException e) {
        throw new RuntimeException("Error initializing mappedFile " + resource,
          e);
      }
    }
  }

  public void forceClose() {
    FileUtil.closeSilent(in, indexIn);
    if (xbaseIterator != null) {
      xbaseIterator.forceClose();
    }
    dataObjectFactory = null;
    geometryFactory = null;
    in = null;
    indexIn = null;
    metaData = null;
    resource = null;
    xbaseIterator = null;
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
    DataObject record;
    try {
      if (xbaseIterator != null) {
        if (xbaseIterator.hasNext()) {
          record = xbaseIterator.next();
          for (int i = 0; i < xbaseIterator.getDeletedCount(); i++) {
            position++;
            readGeometry();
          }
        } else {
          throw new NoSuchElementException();
        }
      } else {
        record = dataObjectFactory.createDataObject(metaData);
      }

      final Geometry geometry = readGeometry();
      record.setGeometryValue(geometry);
    } catch (final EOFException e) {
      throw new NoSuchElementException();
    } catch (final IOException e) {
      throw new RuntimeException("Error reading geometry " + resource, e);
    }
    if (returnMetaData == null) {
      return record;
    } else {
      final DataObject copy = dataObjectFactory.createDataObject(returnMetaData);
      copy.setValues(record);
      return copy;
    }
  }

  public int getPosition() {
    return position;
  }

  public String getTypeName() {
    return this.typeName;
  }

  public boolean isCloseFile() {
    return closeFile;
  }

  /**
   * Load the header record from the shape mappedFile.
   * 
   * @throws IOException If an I/O error occurs.
   */
  @SuppressWarnings("unused")
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

  @SuppressWarnings("unused")
  private Geometry readGeometry() throws IOException {
    final int recordNumber = in.readInt();
    final int recordLength = in.readInt();
    final int shapeType = in.readLEInt();
    return ShapefileGeometryUtil.SHP_INSTANCE.read(geometryFactory, in,
      shapeType);
  }

  public void setCloseFile(final boolean closeFile) {
    this.closeFile = closeFile;
    if (xbaseIterator != null) {
      xbaseIterator.setCloseFile(closeFile);
    }
  }

  public void setMetaData(final DataObjectMetaData metaData) {
    this.returnMetaData = metaData;
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

  public void setTypeName(final String typeName) {
    if (StringUtils.hasText(typeName)) {
      this.typeName = typeName;
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
        DataType geometryType = DataTypes.GEOMETRY;
        switch (shapeType) {
          case ShapefileConstants.POINT_SHAPE:
          case ShapefileConstants.POINT_Z_SHAPE:
          case ShapefileConstants.POINT_M_SHAPE:
          case ShapefileConstants.POINT_ZM_SHAPE:
            geometryType = DataTypes.POINT;
          break;

          case ShapefileConstants.POLYLINE_SHAPE:
          case ShapefileConstants.POLYLINE_Z_SHAPE:
          case ShapefileConstants.POLYLINE_M_SHAPE:
          case ShapefileConstants.POLYLINE_ZM_SHAPE:
            geometryType = DataTypes.MULTI_LINE_STRING;
          break;

          case ShapefileConstants.POLYGON_SHAPE:
          case ShapefileConstants.POLYGON_Z_SHAPE:
          case ShapefileConstants.POLYGON_M_SHAPE:
          case ShapefileConstants.POLYGON_ZM_SHAPE:
            geometryType = DataTypes.MULTI_POLYGON;
          break;

          case ShapefileConstants.MULTI_POINT_SHAPE:
          case ShapefileConstants.MULTI_POINT_Z_SHAPE:
          case ShapefileConstants.MULTI_POINT_M_SHAPE:
          case ShapefileConstants.MULTI_POINT_ZM_SHAPE:
            geometryType = DataTypes.MULTI_POINT;
          break;

          default:
          break;
        }
        metaData.addAttribute("geometry", geometryType, true);
      }
    }
  }

}
