package com.revolsys.gis.esri.gdb.file.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.io.EndianInputStream;

public class FgdbReader {

  public static void main(final String[] args) {
    new FgdbReader().run();
  }

  private EndianInputStream in;

  private int fieldDescriptionOffset;

  private DataType geometryType;

  public FgdbReader() {
    try {
      in = new EndianInputStream(
        new FileInputStream(
          "/apps/gba/data/exports/GBA/ACRD/transport_line_acrd.gdb/a00000014.gdbtable"));
    } catch (final FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  protected void readFieldDescription() throws IOException {
    final String fieldName = readUtf16String();
    final String fieldAlias = readUtf16String();
    final int fieldType = in.read();
    DataType fieldDataType = null;
    int length = -1;
    switch (fieldType) {
      case 0:
        fieldDataType = DataTypes.SHORT;
        in.read(); // length
        readFlags();

      // ubyte: ldf = length of default value in byte if (flag&4) != 0
      // followed by ldf bytes
      break;
      case 1:
        fieldDataType = DataTypes.INT;
        in.read(); // length
        readFlags();

      // ubyte: ldf = length of default value in byte if (flag&4) != 0
      // followed by ldf bytes
      break;
      case 2:
        fieldDataType = DataTypes.FLOAT;
        in.read(); // length
        readFlags();
      // ubyte: ldf = length of default value in byte if (flag&4) != 0
      // followed by ldf bytes
      break;
      case 3:
        fieldDataType = DataTypes.DOUBLE;
        in.read(); // length
        readFlags();
      // ubyte: ldf = length of default value in byte if (flag&4) != 0
      // followed by ldf bytes
      break;
      case 4:
        fieldDataType = DataTypes.STRING;
        length = in.readLEInt();
        readFlags();

      break;
      case 5:
        fieldDataType = DataTypes.DATE_TIME;
        in.read(); // length
        readFlags();
      // ubyte: ldf = length of default value in byte if (flag&4) != 0
      // followed by ldf bytes
      break;
      case 6:
        // OBJECTID
        fieldDataType = DataTypes.INT;
        in.read();
        readFlags();
      break;
      case 7:
        fieldDataType = this.geometryType;
        final int geometryFlag1 = in.read();
        final int geometryFlag2 = in.read();
        final String wkt = readUtf8String();
        final CoordinateSystem coordinateSystem = EsriCoordinateSystems.getCoordinateSystem(wkt);
        final int geometryFlags = in.read();
        int numAxis = 2;
        if (geometryFlags == 5) {
          numAxis = 3;
        }
        if (geometryFlags == 7) {
          numAxis = 4;
        }
        final double xOrigin = in.readLEDouble();
        final double yOrigin = in.readLEDouble();
        final double xyScale = in.readLEDouble();
        double zScale;
        double mScale;
        if (numAxis == 4) {
          final double mOrigin = in.readLEDouble();
          mScale = in.readLEDouble();
        } else {
          mScale = 0;
        }
        if (numAxis >= 3) {
          final double zOrigin = in.readLEDouble();
          zScale = in.readLEDouble();
        } else {
          zScale = 0;
        }
        final double xyTolerance = in.readLEDouble();
        if (numAxis == 4) {
          final double mTolerance = in.readLEDouble();

        }
        if (numAxis >= 3) {
          final double zTolerance = in.readLEDouble();
        }
        final double minX = in.readLEDouble();
        final double minY = in.readLEDouble();
        final double maxX = in.readLEDouble();
        final double maxY = in.readLEDouble();
        final GeometryFactory geometryFactory = GeometryFactory.getFactory(
          coordinateSystem, numAxis, xyScale, zScale);
        System.out.println(new BoundingBox(geometryFactory, minX, minY, maxX,
          maxY));
        boolean run = true;
        while (run) {
          final int v1 = in.read();
          final int v2 = in.read();
          final int v3 = in.read();
          final int v4 = in.read();
          final int v5 = in.read();
          if (v1 == 0 && v2 > 0 && v2 < 3 && v3 == 0 && v4 == 0 && v5 == 0) {
            for (int i = 0; i < v2; i++) {
              in.readLEDouble();
            }
            run = false;
          } else {
            in.read();
            in.read();
            in.read();
          }
        }
      // Read 5 bytes
      // If those bytes are 0x00 XXX 0x00 0x00 0x00 where XXX=0x01, 0x02 or
      // 0x03, then read XXX * float64 values and go to 6.
      // Otherwise, rewind those 5 bytes
      // Read a float64 value
      // Goto 1
      // End
      break;
      case 8:
        fieldDataType = DataTypes.BLOB;
        in.read();
        readFlags();
      break;
      case 9:
        // Raster
        fieldDataType = DataTypes.BLOB;
      break;
      case 10:
        // UUID
        fieldDataType = DataTypes.STRING;
        in.read();
        readFlags();
      break;
      case 11:// UUID
        fieldDataType = DataTypes.STRING;
        in.read();
        readFlags();
      break;
      case 12:
        // XML
        fieldDataType = DataTypes.STRING;
      break;
      default:
        LoggerFactory.getLogger(getClass()).error(
          "Unknown field type " + fieldName + " " + fieldType);
      break;
    }
    System.out.println(new Attribute(fieldName, fieldDataType, length, false));
  }

  private void readFieldDescriptions() throws IOException {
    final int headerSize = in.readLEInt();
    final int version = in.readLEInt();
    final int geometryType = in.read();
    switch (geometryType) {
      case 0:
      break;
      case 1:
        this.geometryType = DataTypes.POINT;
      break;
      case 2:
        this.geometryType = DataTypes.MULTI_POINT;
      break;
      case 3:
        this.geometryType = DataTypes.MULTI_LINE_STRING;
      break;
      case 4:
        this.geometryType = DataTypes.MULTI_POLYGON;
      break;

      default:
        LoggerFactory.getLogger(getClass()).error(
          "Unknown geometry type " + geometryType);
      break;
    }
    final int unknown1 = in.read();
    final int unknown2 = in.read();
    final int unknown3 = in.read();
    final short numFields = in.readLEShort();
    for (int i = 0; i < numFields; i++) {
      readFieldDescription();
    }
    System.out.println();

  }

  protected void readFlags() throws IOException {
    final int flag = in.read();
    if ((flag & 4) != 0) {
      final int defaultBytes = in.read();
      for (int i = 0; i < defaultBytes; i++) {
        in.read();
      }
    }
  }

  private void readHeader() throws IOException {
    final int signature = in.readLEInt();
    final int numValidRows = in.readLEInt();
    final int unknown1 = in.readLEInt();
    final int unknown2 = in.readLEInt();
    final int unknown3 = in.readLEInt();
    final int unknown4 = in.readLEInt();
    final int fileSize = in.readLEInt();
    final int unknown6 = in.readLEInt();
    fieldDescriptionOffset = in.readLEInt();
    final int unknown8 = in.readLEInt();

  }

  private String readUtf16String() throws IOException {
    final int numWords = in.read();
    final char[] characters = new char[numWords];
    for (int i = 0; i < numWords; i++) {
      final short c = in.readLEShort();
      characters[i] = (char)c;
    }
    return new String(characters);
  }

  private String readUtf8String() throws IOException {
    final short numBytes = in.readLEShort();
    final int numWords = numBytes / 2;
    final char[] characters = new char[numWords];
    for (int i = 0; i < numWords; i++) {
      final short c = in.readLEShort();
      characters[i] = (char)c;
    }
    return new String(characters);
  }

  private void run() {
    try {
      readHeader();
      readFieldDescriptions();
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
