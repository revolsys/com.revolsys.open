package com.revolsys.elevation.gridded.img;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.revolsys.collection.map.MapEx;
import com.revolsys.util.Debug;

public class HfaField {
  private static final int EPT_MIN = 0;

  private static final int EPT_u1 = 0;

  private static final int EPT_u2 = 1;

  private static final int EPT_u4 = 2;

  private static final int EPT_u8 = 3;

  private static final int EPT_s8 = 4;

  private static final int EPT_u16 = 5;

  private static final int EPT_s16 = 6;

  private static final int EPT_u32 = 7;

  private static final int EPT_s32 = 8;

  private static final int EPT_f32 = 9;

  private static final int EPT_f64 = 10;

  private static final int EPT_c64 = 11;

  private static final int EPT_c128 = 12;

  private static final int EPT_MAX = EPT_c128;

  public static HfaField newObject(final int itemCount, final String fieldName,
    final String fieldTypeName) {
    return new HfaField(itemCount, ' ', 'o', fieldName, null, fieldTypeName,
      Collections.emptyList());
  }

  public static HfaField newPointer(final int itemCount, final char pointerType,
    final String fieldName, final String fieldTypeName) {
    return new HfaField(itemCount, pointerType, 'o', fieldName, null, fieldTypeName,
      Collections.emptyList());
  }

  int byteCount;

  private int itemCount;

  private char pointerType = '\0';

  private char itemType;

  private final List<String> enumValues;

  private String name;

  private HfaType fieldType;

  private String fieldTypeName;

  public HfaField() {
    this.enumValues = Collections.emptyList();
  }

  public HfaField(final int itemCount, final char pointerType, final char itemType,
    final String fieldName) {
    this(itemCount, pointerType, itemType, fieldName, null, null, Collections.emptyList());
  }

  public HfaField(final int itemCount, final char pointerType, final char itemType,
    final String fieldName, final HfaType fieldType, final String fieldTypeName,
    final List<String> enumValues) {
    this.itemCount = itemCount;
    this.itemType = itemType;
    this.pointerType = pointerType;
    this.name = fieldName;
    this.fieldType = fieldType;
    this.fieldTypeName = fieldTypeName;
    this.name = fieldName;
    this.enumValues = enumValues;
  }

  public HfaField(final int itemCount, final char itemType, final String fieldName) {
    this(itemCount, itemType, fieldName, Collections.emptyList());
  }

  public HfaField(final int itemCount, final char itemType, final String fieldName,
    final List<String> enumNames) {
    this.itemCount = itemCount;
    this.itemType = itemType;
    this.name = fieldName;
    this.enumValues = enumNames;
  }

  public HfaField(final int itemCount, final char itemType, final String fieldName,
    final String... enumValues) {
    this.itemCount = itemCount;
    this.itemType = itemType;
    this.name = fieldName;
    this.enumValues = Arrays.asList(enumValues);
  }

  boolean completeDefn(final HfaDictionary dictionary) {
    if (this.fieldTypeName != null) {
      this.fieldType = dictionary.findType(this.fieldTypeName);
    }

    // Figure out the size.
    if (this.pointerType == 'p') {
      this.byteCount = -1; // We can't know the instance size.
    } else if (this.fieldType != null) {
      if (!this.fieldType.completeDefn(dictionary)) {
        return false;
      }
      if (this.fieldType.byteCount == -1) {
        this.byteCount = -1;
      } else if (this.fieldType.byteCount != 0
        && this.itemCount > Integer.MAX_VALUE / this.fieldType.byteCount) {
        this.byteCount = -1;
      } else {
        this.byteCount = this.fieldType.byteCount * this.itemCount;
      }

      // TODO(schwehr): What does the 8 represent?
      if (this.pointerType == '*' && this.byteCount != -1) {
        if (this.byteCount > Integer.MAX_VALUE - 8) {
          this.byteCount = -1;
        } else {
          this.byteCount += 8;
        } // Count, and offset.
      }
    } else {
      final int itemSize = dictionary.getItemSize(this.itemType);
      if (itemSize != 0 && this.itemCount > Integer.MAX_VALUE / itemSize) {
        this.byteCount = -1;
      } else {
        this.byteCount = itemSize * this.itemCount;
      }
    }
    return true;
  }

  int getInstCount(final ImgGriddedElevationReader reader, final int dataSize) {
    if (this.pointerType == '\0') {
      return this.itemCount;
    }

    if (this.itemType == 'b') {
      if (dataSize < 20) {
        return 0;
      }
      // why skip?
      final int skip1 = reader.readInt();
      final int skip2 = reader.readInt();
      final int rowCount = reader.readInt();
      final int columnCount = reader.readInt();

      if (rowCount < 0 || columnCount < 0) {
        return 0;
      }
      if (columnCount != 0 && rowCount > Integer.MAX_VALUE / columnCount) {
        return 0;
      }

      return rowCount * columnCount;
    }

    if (dataSize < 4) {
      return 0;
    }

    return reader.readInt();
  }

  public String getName() {
    return this.name;
  }

  public Object readValue(final ImgGriddedElevationReader reader) {
    int pointerSize = 0;
    int p2 = 1;
    if (this.pointerType != '\0') {
      pointerSize = reader.readInt(); // why
      p2 = reader.readInt(); // skip why?
    }

    switch (this.itemType) {
      case 'c':
        if (pointerSize == 0) {
          return null;
        } else {
          return reader.readString0(pointerSize);
        }
      case 'C':
        return reader.readChar();

      case 'e':
        final int enumIndex = reader.readUnsignedShort();
        if (enumIndex >= 0 && enumIndex < this.enumValues.size()) {
          return this.enumValues.get(enumIndex);
        } else {
          return null;
        }
      case 's':
        return reader.readUnsignedShort();

      case 'S':
        return reader.readShort();

      case 't':
      case 'l':
        return reader.readUnsignedInt();

      case 'L':
        return reader.readInt();

      case 'f':
        return reader.readFloat();

      case 'd':
        return reader.readDouble();

      case 'b': {
        final int height = reader.readInt();
        final int width = reader.readInt();
        final int cellCount = width * height;
        final short baseItemType = reader.readShort();
        reader.readShort(); // We ignore the 2 byte objecttype value.
        if (baseItemType == EPT_u1) {
          Debug.noOp();
          // TODO(schwehr): What are these constants like 8 and 0x7?
          // if( nIndexValue * 8 >= dataSize )
          // {
          // throw new IllegalArgumentException( "Buffer too small");
          // return false;
          // }
          //
          // if( pabyData[nIndexValue >> 3] & 1 << (nIndexValue & 0x7) )
          // {
          // dfDoubleRet = 1;
          // nIntRet = 1;
          // }
          // else
          // {
          // dfDoubleRet = 0.0;
          // nIntRet = 0;
          // }
        } else if (baseItemType == EPT_u2) {
          Debug.noOp();
          // const final int nBitOffset = nIndexValue & 0x3;
          // const final int nByteOffset = nIndexValue >> 2;
          //
          // if( nByteOffset >= dataSize )
          // {
          // throw new IllegalArgumentException( "Buffer too small");
          // return false;
          // }
          //
          // const final int nMask = 0x3;
          // nIntRet = pabyData[nByteOffset] >> nBitOffset & nMask;
          // dfDoubleRet = nIntRet;
        } else if (baseItemType == EPT_u4) {
          Debug.noOp();
          // const final int nBitOffset = nIndexValue & 0x7;
          // const final int nByteOffset = nIndexValue >> 3;
          //
          // if( nByteOffset >= dataSize )
          // {
          // throw new IllegalArgumentException( "Buffer too small");
          // return false;
          // }
          //
          // const final int nMask = 0x7;
          // nIntRet = pabyData[nByteOffset] >> nBitOffset & nMask;
          // dfDoubleRet = nIntRet;
        } else if (baseItemType == EPT_u8) {
          final short[] cells = new short[cellCount];
          for (int i = 0; i < cellCount; i++) {
            cells[i] = reader.readUnsignedByte();
          }
          return new HfaBinaryData(width, height, baseItemType, cells);
        } else if (baseItemType == EPT_s8) {
          final byte[] cells = new byte[cellCount];
          for (int i = 0; i < cellCount; i++) {
            cells[i] = reader.readByte();
          }
          return new HfaBinaryData(width, height, baseItemType, cells);

        } else if (baseItemType == EPT_s16) {
          final short[] cells = new short[cellCount];
          for (int i = 0; i < cellCount; i++) {
            cells[i] = reader.readShort();
          }
          return new HfaBinaryData(width, height, baseItemType, cells);
        } else if (baseItemType == EPT_u16) {
          final int[] cells = new int[cellCount];
          for (int i = 0; i < cellCount; i++) {
            cells[i] = reader.readUnsignedShort();
          }
          return new HfaBinaryData(width, height, baseItemType, cells);
        } else if (baseItemType == EPT_s32) {
          final int[] cells = new int[cellCount];
          for (int i = 0; i < cellCount; i++) {
            cells[i] = reader.readInt();
          }
          return new HfaBinaryData(width, height, baseItemType, cells);
        } else if (baseItemType == EPT_u32) {
          final long[] cells = new long[cellCount];
          for (int i = 0; i < cellCount; i++) {
            cells[i] = reader.readUnsignedInt();
          }
          return new HfaBinaryData(width, height, baseItemType, cells);
        } else if (baseItemType == EPT_f32) {
          final float[] cells = new float[cellCount];
          for (int i = 0; i < cellCount; i++) {
            cells[i] = reader.readFloat();
          }
          return new HfaBinaryData(width, height, baseItemType, cells);
        } else if (baseItemType == EPT_f64) {
          final double[] cells = new double[cellCount];
          for (int i = 0; i < cellCount; i++) {
            cells[i] = reader.readDouble();
          }
          return new HfaBinaryData(width, height, baseItemType, cells);
        } else {
          throw new IllegalArgumentException("Unknown base item type: " + baseItemType);
        }
      }
      break;

      case 'o':
        if (this.fieldType != null) {
          if ('*' == this.pointerType) {
            return this.fieldType.readFieldValues(reader);
          } else {
            final List<MapEx> values = new ArrayList<>();
            for (int i = 0; i < pointerSize; i++) {
              final MapEx value = this.fieldType.readFieldValues(reader);
              values.add(value);
            }
            return values;
          }
        } else {
          return MapEx.EMPTY;
        }

      default:
        return null;
    }
    return null;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
