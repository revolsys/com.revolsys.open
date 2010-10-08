/*
 * $URL:https://secure.revolsys.com/svn/open.revolsys.com/GIS/trunk/src/main/java/com/revolsys/gis/format/shape/io/ShapeFileReader.java $
 * $Author:paul.austin@revolsys.com $
 * $Date:2007-06-09 09:28:28 -0700 (Sat, 09 Jun 2007) $
 * $Revision:265 $

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
package com.revolsys.gis.format.shape.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.WktCsParser;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.format.shape.io.geometry.JtsGeometryConverter;
import com.revolsys.gis.format.xbase.io.XbaseFileReader;
import com.revolsys.gis.io.EndianInputStream;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.revolsys.io.EndianInput;
import com.revolsys.io.FileUtil;
import com.revolsys.util.MathUtil;

/**
 * @author Paul Austin
 */
public class ShapeFileReader extends XbaseFileReader {
  private static final Logger log = Logger.getLogger(ShapeFileReader.class);

  private JtsGeometryConverter geometryReader;

  private EndianInput in;

  private File shapeFile;

  private int srid;

  private GeometryFactory geometryFactory;

  private DataType dataType;

  public ShapeFileReader() {
  }

  /**
   * Construct a new ShapeFileReader for the shape file.
   * 
   * @param file The file to read.
   */
  public ShapeFileReader(
    final File file) {
    this(file, null);
  }

  /**
   * Construct a new ShapeFileReader for the shape file.
   * 
   * @param file The file to read.
   * @param typeName The name of the type to create for the data objects.
   */
  public ShapeFileReader(
    final File file,
    final String typeName) {
    setFile(file);
    setTypeName(typeName);
  }

  @Override
  public void close() {
    super.close();
    try {
      in.close();
    } catch (final IOException e) {
      log.error(e.getMessage(), e);
    }
  }

  public int getSrid() {
    return srid;
  }

  /**
   * Load the next record.
   * 
   * @return The next DataObject.
   * @throws IOException If an I/O error occurs
   */
  @Override
  public DataObject loadDataObject()
    throws IOException {
    final DataObject object = super.loadDataObject();
    object.setGeometryValue(geometryReader.readGeometry(in));
    return object;
  }

  /**
   * Load the header record from the shape file.
   * 
   * @throws IOException If an I/O error occurs.
   */
  private void loadHeader()
    throws IOException {
    final int fileCode = in.readInt();
    in.skipBytes(20);
    final int fileLength = in.readInt();
    final int version = in.readLEInt();
    final int shapeType = in.readLEInt();
    dataType = ShapefileConstants.DATA_TYPE_MAP.get(shapeType);
    if (dataType == null) {
      dataType = DataTypes.GEOMETRY;
    }
    in.readDouble();
    in.readDouble();
    in.readDouble();
    in.readDouble();
    in.readDouble();
    in.readDouble();
    in.readDouble();
    in.readDouble();
  }

  @Override
  protected DataObjectMetaData loadSchema(
    final EndianInput in)
    throws IOException {
    final DataObjectMetaDataImpl type = (DataObjectMetaDataImpl)super.loadSchema(in);
    type.setGeometryAttributeIndex(type.getAttributeCount());
    final Attribute attribute = type.addAttribute("geometry", dataType, true);
    attribute.setProperty(AttributeProperties.GEOMETRY_FACTORY, geometryFactory);
    return type;
  }

  @Override
  public void open() {

    final File file = getFile();
    File projFile = new File(file.getParentFile(),
      FileUtil.getFileNamePrefix(file) + ".prj");
    final CoordinatesPrecisionModel precisionModel = new SimpleCoordinatesPrecisionModel();
    geometryFactory = new GeometryFactory(precisionModel);
    if (projFile.exists()) {
      try {
        final CoordinateSystem coordinateSystem = new WktCsParser(
          new FileInputStream(projFile)).parse();
        final CoordinateSystem esriCoordinateSystem = EsriCoordinateSystems.getCoordinateSystem(coordinateSystem);
        srid = EsriCoordinateSystems.getCrsId(esriCoordinateSystem);
        geometryFactory = new GeometryFactory(esriCoordinateSystem,
          precisionModel);
      } catch (IOException e) {
        log.error("Unable to read projection file: " + projFile);
      }
    }

    geometryReader = new JtsGeometryConverter(geometryFactory);
    super.open();
    try {
      final FileInputStream fileIn = new FileInputStream(shapeFile);
      final BufferedInputStream bufferedIn = new BufferedInputStream(fileIn);
      in = new EndianInputStream(bufferedIn);
      loadHeader();
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public void setFile(
    final File file) {
    super.setFile(new File(file.getParentFile(),
      FileUtil.getFileNamePrefix(file) + ".dbf"));
    this.shapeFile = file;
  }

  public void setSrid(
    final int srid) {
    this.srid = srid;
  }

  @Override
  protected void skipDataObject()
    throws IOException {
    super.skipDataObject();
    in.readInt();
    final int recordLength = in.readInt();
    in.skipBytes(recordLength * MathUtil.BYTES_IN_SHORT);
  }
}
