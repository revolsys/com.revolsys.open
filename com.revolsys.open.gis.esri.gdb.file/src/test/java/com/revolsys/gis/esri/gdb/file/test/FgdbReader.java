package com.revolsys.gis.esri.gdb.file.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.revolsys.data.record.ArrayRecord;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.esri.gdb.file.test.field.BinaryField;
import com.revolsys.gis.esri.gdb.file.test.field.DoubleField;
import com.revolsys.gis.esri.gdb.file.test.field.FgdbField;
import com.revolsys.gis.esri.gdb.file.test.field.FloatField;
import com.revolsys.gis.esri.gdb.file.test.field.GeometryField;
import com.revolsys.gis.esri.gdb.file.test.field.IntField;
import com.revolsys.gis.esri.gdb.file.test.field.ObjectIdField;
import com.revolsys.gis.esri.gdb.file.test.field.ShortField;
import com.revolsys.gis.esri.gdb.file.test.field.StringField;
import com.revolsys.gis.esri.gdb.file.test.field.XmlField;
import com.revolsys.gis.io.EndianInputStream;
import com.revolsys.io.EndianInput;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;

public class FgdbReader {

  public static void main(final String[] args) {
    new FgdbReader().run();
  }

  public static long readVarInt(final EndianInput in) throws IOException {
    long value = 0;
    int shift = 0;
    int b;
    int next;
    do {
      b = in.read();
      if (shift == 0) {
        value += (b & 0x3f) << shift;
        shift = 6;
      } else {
        value += (b & 0x7f) << shift;
        shift += 7;
      }
      next = b & 0x80;
    } while (next == 0x80);
    return value;

  }

  public static long readVarUInt(final EndianInput in) throws IOException {
    long value = 0;
    int shift = 0;
    int b;
    int next;
    do {
      b = in.read();
      value += (b & 0x7f) << shift;
      shift += 7;
      next = b & 0x80;
    } while (next == 0x80);
    return value;

  }

  private EndianInputStream in;

  private int fieldDescriptionOffset;

  private DataType geometryType;

  private final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl();

  private int numValidRows;

  private int optionalFieldCount;

  public FgdbReader() {
    try {
      in = new EndianInputStream(new FileInputStream(
        "/Users/paustin/Downloads/KSRD_20140306.gdb/a0000000d.gdbtable"));
    } catch (final FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private boolean isNull(final byte[] nullFields, final int fieldIndex) {
    final int flagByte = fieldIndex / 8;
    if (flagByte < nullFields.length) {
      final byte flags = nullFields[flagByte];
      final int bit = fieldIndex % 8;

      return (flags & (1 << bit)) != 0;
    } else {
      return false;
    }
  }

  private void readData() throws IOException {
    int objectId = 1;
    final int x = in.readLEInt();
    if (x != -272716322) {
      return;
    }
    for (int i = 0; i < numValidRows; i++) {
      final int recordSize = in.readLEInt();
      final double opt = Math.ceil(optionalFieldCount / 8.0);
      final byte[] nullFields = new byte[(int)opt];
      in.read(nullFields);
      final Record record = new ArrayRecord(recordDefinition);
      record.setIdValue(objectId++);
      int fieldIndex = 0;
      int optionalFieldIndex = 0;
      final int idIndex = recordDefinition.getIdAttributeIndex();
      for (final Attribute field : recordDefinition.getAttributes()) {
        if (fieldIndex != idIndex) {
          if (field.isRequired() || !isNull(nullFields, optionalFieldIndex++)) {
            final FgdbField fgdbField = (FgdbField)field;
            fgdbField.setValue(record, in);
          }
        }
        fieldIndex++;
      }
    }
    // final int rowLength = in.readLEInt();
    // final byte[] b = new byte[rowLength];
    // final int read = in.read(b);
    // System.out.println(Arrays.toString(b));

  }

  protected FgdbField readFieldDescription() throws IOException {
    final String fieldName = readUtf16String();
    final String fieldAlias = readUtf16String();
    final int fieldType = in.read();
    int length = -1;
    FgdbField field;
    boolean required = false;
    switch (fieldType) {
      case 0:
        in.read(); // length
        required = readFlags();
        // ubyte: ldf = length of default value in byte if (flag&4) != 0
        // followed by ldf bytes
        field = new ShortField(fieldName, required);
      break;
      case 1:
        in.read(); // length
        required = readFlags();

        // ubyte: ldf = length of default value in byte if (flag&4) != 0
        // followed by ldf bytes
        field = new IntField(fieldName, required);
      break;
      case 2:
        in.read(); // length
        required = readFlags();
        // ubyte: ldf = length of default value in byte if (flag&4) != 0
        // followed by ldf bytes
        field = new FloatField(fieldName, required);
      break;
      case 3:
        in.read(); // length
        required = readFlags();
        // ubyte: ldf = length of default value in byte if (flag&4) != 0
        // followed by ldf bytes
        field = new DoubleField(fieldName, required);
      break;
      case 4:
        length = in.readLEInt();
        required = readFlags();
        field = new StringField(fieldName, length, required);
      break;
      case 5:
        in.read(); // length
        required = readFlags();
        // ubyte: ldf = length of default value in byte if (flag&4) != 0
        // followed by ldf bytes
        field = new FgdbField(fieldName, DataTypes.DATE_TIME, required);
      break;
      case 6:
        // OBJECTID
        in.read();
        required = readFlags();
        field = new ObjectIdField(fieldName, true);
      break;
      case 7:
        final int geometryFlag1 = in.read();
        final int geometryFlag2 = in.read();
        final String wkt = readUtf8String();
        final CoordinateSystem coordinateSystem = EsriCoordinateSystems.getCoordinateSystem(wkt);
        final int geometryFlags = in.read();
        int axisCount = 2;
        if (geometryFlags == 5) {
          axisCount = 3;
        }
        if (geometryFlags == 7) {
          axisCount = 4;
        }
        final double xOrigin = in.readLEDouble();
        final double yOrigin = in.readLEDouble();
        final double xyScale = in.readLEDouble();
        double zScale;
        double mScale;
        if (axisCount == 4) {
          final double mOrigin = in.readLEDouble();
          mScale = in.readLEDouble();
        } else {
          mScale = 0;
        }
        if (axisCount >= 3) {
          final double zOrigin = in.readLEDouble();
          zScale = in.readLEDouble();
        } else {
          zScale = 0;
        }
        final double xyTolerance = in.readLEDouble();
        if (axisCount == 4) {
          final double mTolerance = in.readLEDouble();

        }
        if (axisCount >= 3) {
          final double zTolerance = in.readLEDouble();
        }
        final double minX = in.readLEDouble();
        final double minY = in.readLEDouble();
        final double maxX = in.readLEDouble();
        final double maxY = in.readLEDouble();
        final GeometryFactory geometryFactory = GeometryFactory.fixed(
          coordinateSystem, axisCount, xyScale, zScale);
        final BoundingBox boundingBox = new BoundingBoxDoubleGf(geometryFactory, 2,
          minX, minY, maxX, maxY);
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
            run = required;
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
        field = new GeometryField(fieldName, geometryType, required,
          geometryFactory);
      break;
      case 8:
        in.read();
        required = readFlags();
        field = new BinaryField(fieldName, length, required);
      break;
      case 9:
        // Raster
        field = new FgdbField(fieldName, DataTypes.BLOB, required);
      break;
      case 10:
        // UUID
        in.read();
        required = readFlags();
        field = new FgdbField(fieldName, DataTypes.STRING, required);
      break;
      case 11:// UUID
        in.read();
        required = readFlags();
        field = new FgdbField(fieldName, DataTypes.STRING, required);
      break;
      case 12:
        // XML
        field = new XmlField(fieldName, length, required);
      break;
      default:
        System.out.println("Unknown field type " + fieldName + " " + fieldType);
        return null;
    }
    field.setProperty("ALIAS", fieldAlias);
    return field;
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
        System.out.println("Unknown geometry type " + geometryType);
      break;
    }
    final int unknown1 = in.read();
    final int unknown2 = in.read();
    final int unknown3 = in.read();
    final short numFields = in.readLEShort();
    optionalFieldCount = 0;
    for (int i = 0; i < numFields; i++) {
      final FgdbField field = readFieldDescription();
      if (field != null) {
        recordDefinition.addAttribute(field);
        if (field instanceof ObjectIdField) {
          final String fieldName = field.getName();
          recordDefinition.setIdAttributeName(fieldName);
        }
        if (!field.isRequired()) {
          optionalFieldCount++;
        }
      }
    }
    recordDefinition.setProperty("optionalFieldCount", optionalFieldCount);
    System.out.println(MapObjectFactoryRegistry.toString(recordDefinition));
  }

  protected boolean readFlags() throws IOException {
    final int flag = in.read();
    if ((flag & 4) != 0) {
      final int defaultBytes = in.read();
      for (int i = 0; i < defaultBytes; i++) {
        in.read();
      }
    }
    final int i = flag & 1;
    return i == 0;
  }

  private void readHeader() throws IOException {
    final int signature = in.readLEInt();
    numValidRows = in.readLEInt();
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
      readData();
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
