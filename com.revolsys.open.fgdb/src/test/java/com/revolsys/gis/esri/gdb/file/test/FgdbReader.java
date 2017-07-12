package com.revolsys.gis.esri.gdb.file.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.esri.EsriCoordinateSystems;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
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
import com.revolsys.identifier.Identifier;
import com.revolsys.io.endian.EndianInput;
import com.revolsys.io.endian.EndianInputStream;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;

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

  private int fieldDescriptionOffset;

  private DataType geometryType;

  private EndianInputStream in;

  private int numValidRows;

  private int optionalFieldCount;

  private final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl();

  public FgdbReader() {
    try {
      this.in = new EndianInputStream(
        new FileInputStream("/Users/paustin/Downloads/KSRD_20140306.gdb/a0000000d.gdbtable"));
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

      return (flags & 1 << bit) != 0;
    } else {
      return false;
    }
  }

  private void readData() throws IOException {
    int objectId = 1;
    final int x = this.in.readLEInt();
    if (x != -272716322) {
      return;
    }
    for (int i = 0; i < this.numValidRows; i++) {
      final int recordSize = this.in.readLEInt();
      final double opt = Math.ceil(this.optionalFieldCount / 8.0);
      final byte[] nullFields = new byte[(int)opt];
      this.in.read(nullFields);
      final Record record = new ArrayRecord(this.recordDefinition);
      record.setIdentifier(Identifier.newIdentifier(objectId++));
      int fieldIndex = 0;
      int optionalFieldIndex = 0;
      final int idIndex = this.recordDefinition.getIdFieldIndex();
      for (final FieldDefinition field : this.recordDefinition.getFields()) {
        if (fieldIndex != idIndex) {
          if (field.isRequired() || !isNull(nullFields, optionalFieldIndex++)) {
            final FgdbField fgdbField = (FgdbField)field;
            fgdbField.setValue(record, this.in);
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
    final int fieldType = this.in.read();
    int length = -1;
    FgdbField field;
    boolean required = false;
    switch (fieldType) {
      case 0:
        this.in.read(); // length
        required = readFlags();
        // ubyte: ldf = length of default value in byte if (flag&4) != 0
        // followed by ldf bytes
        field = new ShortField(fieldName, required);
      break;
      case 1:
        this.in.read(); // length
        required = readFlags();

        // ubyte: ldf = length of default value in byte if (flag&4) != 0
        // followed by ldf bytes
        field = new IntField(fieldName, required);
      break;
      case 2:
        this.in.read(); // length
        required = readFlags();
        // ubyte: ldf = length of default value in byte if (flag&4) != 0
        // followed by ldf bytes
        field = new FloatField(fieldName, required);
      break;
      case 3:
        this.in.read(); // length
        required = readFlags();
        // ubyte: ldf = length of default value in byte if (flag&4) != 0
        // followed by ldf bytes
        field = new DoubleField(fieldName, required);
      break;
      case 4:
        length = this.in.readLEInt();
        required = readFlags();
        field = new StringField(fieldName, length, required);
      break;
      case 5:
        this.in.read(); // length
        required = readFlags();
        // ubyte: ldf = length of default value in byte if (flag&4) != 0
        // followed by ldf bytes
        field = new FgdbField(fieldName, DataTypes.DATE_TIME, required);
      break;
      case 6:
        // OBJECTID
        this.in.read();
        required = readFlags();
        field = new ObjectIdField(fieldName, true);
      break;
      case 7:
        final int geometryFlag1 = this.in.read();
        final int geometryFlag2 = this.in.read();
        final String wkt = readUtf8String();
        final CoordinateSystem coordinateSystem = EsriCoordinateSystems.getCoordinateSystem(wkt);
        final int geometryFlags = this.in.read();
        int axisCount = 2;
        if (geometryFlags == 5) {
          axisCount = 3;
        }
        if (geometryFlags == 7) {
          axisCount = 4;
        }
        final double xOrigin = this.in.readLEDouble();
        final double yOrigin = this.in.readLEDouble();
        final double xyScale = this.in.readLEDouble();
        double zScale;
        double mScale;
        if (axisCount == 4) {
          final double mOrigin = this.in.readLEDouble();
          mScale = this.in.readLEDouble();
        } else {
          mScale = 0;
        }
        if (axisCount >= 3) {
          final double zOrigin = this.in.readLEDouble();
          zScale = this.in.readLEDouble();
        } else {
          zScale = 0;
        }
        final double xyTolerance = this.in.readLEDouble();
        if (axisCount == 4) {
          final double mTolerance = this.in.readLEDouble();

        }
        if (axisCount >= 3) {
          final double zTolerance = this.in.readLEDouble();
        }
        final double minX = this.in.readLEDouble();
        final double minY = this.in.readLEDouble();
        final double maxX = this.in.readLEDouble();
        final double maxY = this.in.readLEDouble();
        final GeometryFactory geometryFactory = GeometryFactory.fixed(coordinateSystem, axisCount,
          xyScale, xyScale, zScale);
        final BoundingBox boundingBox = new BoundingBoxDoubleGf(geometryFactory, 2, minX, minY,
          maxX, maxY);
        boolean run = true;
        while (run) {
          final int v1 = this.in.read();
          final int v2 = this.in.read();
          final int v3 = this.in.read();
          final int v4 = this.in.read();
          final int v5 = this.in.read();
          if (v1 == 0 && v2 > 0 && v2 < 3 && v3 == 0 && v4 == 0 && v5 == 0) {
            for (int i = 0; i < v2; i++) {
              this.in.readLEDouble();
            }
            run = required;
          } else {
            this.in.read();
            this.in.read();
            this.in.read();
          }
        }
        // Read 5 bytes
        // If those bytes are 0x00 XXX 0x00 0x00 0x00 where XXX=0x01, 0x02 or
        // 0x03, then read XXX * float64 values and go to 6.
        // Otherwise, rewind those 5 bytes
        // Read a float64 value
        // Goto 1
        // End
        field = new GeometryField(fieldName, this.geometryType, required, geometryFactory);
      break;
      case 8:
        this.in.read();
        required = readFlags();
        field = new BinaryField(fieldName, length, required);
      break;
      case 9:
        // Raster
        field = new FgdbField(fieldName, DataTypes.BLOB, required);
      break;
      case 10:
        // UUID
        this.in.read();
        required = readFlags();
        field = new FgdbField(fieldName, DataTypes.STRING, required);
      break;
      case 11:// UUID
        this.in.read();
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
    final int headerSize = this.in.readLEInt();
    final int version = this.in.readLEInt();
    final int geometryType = this.in.read();
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
    final int unknown1 = this.in.read();
    final int unknown2 = this.in.read();
    final int unknown3 = this.in.read();
    final short numFields = this.in.readLEShort();
    this.optionalFieldCount = 0;
    for (int i = 0; i < numFields; i++) {
      final FgdbField field = readFieldDescription();
      if (field != null) {
        this.recordDefinition.addField(field);
        if (field instanceof ObjectIdField) {
          final String fieldName = field.getName();
          this.recordDefinition.setIdFieldName(fieldName);
        }
        if (!field.isRequired()) {
          this.optionalFieldCount++;
        }
      }
    }
    this.recordDefinition.setProperty("optionalFieldCount", this.optionalFieldCount);
    System.out.println(MapObjectFactory.toString(this.recordDefinition));
  }

  protected boolean readFlags() throws IOException {
    final int flag = this.in.read();
    if ((flag & 4) != 0) {
      final int defaultBytes = this.in.read();
      for (int i = 0; i < defaultBytes; i++) {
        this.in.read();
      }
    }
    final int i = flag & 1;
    return i == 0;
  }

  private void readHeader() throws IOException {
    final int signature = this.in.readLEInt();
    this.numValidRows = this.in.readLEInt();
    final int unknown1 = this.in.readLEInt();
    final int unknown2 = this.in.readLEInt();
    final int unknown3 = this.in.readLEInt();
    final int unknown4 = this.in.readLEInt();
    final int fileSize = this.in.readLEInt();
    final int unknown6 = this.in.readLEInt();
    this.fieldDescriptionOffset = this.in.readLEInt();
    final int unknown8 = this.in.readLEInt();

  }

  private String readUtf16String() throws IOException {
    final int numCharacters = this.in.read();
    final char[] characters = new char[numCharacters];
    for (int i = 0; i < numCharacters; i++) {
      final short c = this.in.readLEShort();
      characters[i] = (char)c;
    }
    return new String(characters);
  }

  private String readUtf8String() throws IOException {
    final short numBytes = this.in.readLEShort();
    final int numWords = numBytes / 2;
    final char[] characters = new char[numWords];
    for (int i = 0; i < numWords; i++) {
      final short c = this.in.readLEShort();
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
