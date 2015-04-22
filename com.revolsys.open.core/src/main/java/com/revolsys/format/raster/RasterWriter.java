package com.revolsys.format.raster;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.revolsys.gis.io.EndianOutputStream;
import com.revolsys.util.Property;
import com.revolsys.util.WrappedException;

public class RasterWriter implements AutoCloseable {

  private static final byte PROPERTY_BOOLEAN = 1;

  private static final byte PROPERTY_BYTE = 2;

  private static final byte PROPERTY_SHORT = 3;

  private static final byte PROPERTY_INT = 4;

  private static final byte PROPERTY_LONG = 5;

  private static final byte PROPERTY_FLOAT = 6;

  private static final byte PROPERTY_DOUBLE = 7;

  private static final byte PROPERTY_STRING = 8;

  private static final int ELEVATIONS = 9;

  private final EndianOutputStream out;

  public RasterWriter(final OutputStream out) {
    this.out = new EndianOutputStream(new BufferedOutputStream(out));
    property("TYPE", "RASTER");
    property("VERSION", (byte)1);
  }

  @Override
  public void close() {
    try {
      this.out.flush();
      this.out.close();
    } catch (final IOException e) {
    }
  }

  public void property(final String name, final boolean value) {
    writeByte(PROPERTY_BOOLEAN);
    if (value) {
      writeByte(1);
    } else {
      writeByte(0);
    }
  }

  public void property(final String name, final byte value) {
    writeByte(PROPERTY_BYTE);
    writeByte(value);
  }

  public void property(final String name, final double value) {
    try {
      writeByte(PROPERTY_DOUBLE);
      this.out.writeDouble(value);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }

  public void property(final String name, final float value) {
    try {
      writeByte(PROPERTY_FLOAT);
      this.out.writeFloat(value);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }

  public void property(final String name, final int value) {
    try {
      writeByte(PROPERTY_INT);
      this.out.writeInt(value);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }

  public void property(final String name, final long value) {
    try {
      writeByte(PROPERTY_LONG);
      this.out.writeLong(value);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }

  public void property(final String name, final Object value) {
    if (Property.hasValue(name)) {
      if (value == null) {
      } else if (value instanceof Boolean) {
        property(name, value);
      } else if (value instanceof Byte) {
        property(name, value);
      } else if (value instanceof Short) {
        property(name, value);
      } else if (value instanceof Integer) {
        property(name, value);
      } else if (value instanceof Long) {
        property(name, value);
      } else if (value instanceof Float) {
        property(name, value);
      } else if (value instanceof Double) {
        property(name, value);
      } else if (value instanceof String) {
        property(name, (String)value);
      } else {
        property(name, value.toString());
      }
    }
  }

  public void property(final String name, final short value) {
    try {
      writeByte(PROPERTY_SHORT);
      this.out.writeShort(value);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }

  public void property(final String name, final String value) {
    try {
      writeByte(PROPERTY_STRING);
      this.out.writeBytes(value);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }

  public void values(final double offset, final double scale, final double... values) {
    writeByte(ELEVATIONS);
    int previousValue = 0;
    for (final double value : values) {
      final int scaledValue = (int)Math.round((value - offset) * scale);
      final int delta = scaledValue - previousValue;
      varSInt(delta);
      previousValue = scaledValue;
    }
  }

  public void varInt(int value) {
    while (true) {
      if ((value & ~0x7F) == 0) {
        writeByte(value);
        return;
      } else {
        writeByte(value & 0x7F | 0x80);
        value >>>= 7;
      }
    }
  }

  public void varInt(long value) {
    while (true) {
      while (true) {
        if ((value & ~0x7FL) == 0) {
          writeByte((int)value);
          return;
        } else {
          writeByte((int)value & 0x7F | 0x80);
          value >>>= 7;
        }
      }
    }
  }

  public void varSInt(int value) {
    value = value << 1 ^ value >> 31;
    varInt(value);
  }

  public void varSInt(long value) {
    value = value << 1 ^ value >> 63;
    varInt(value);
  }

  private void writeByte(final int value) {
    try {
      this.out.write(value);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }
}
