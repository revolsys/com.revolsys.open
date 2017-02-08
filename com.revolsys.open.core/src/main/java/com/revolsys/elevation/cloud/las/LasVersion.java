package com.revolsys.elevation.cloud.las;

import java.io.IOException;

import com.revolsys.io.channels.ChannelReader;

public class LasVersion implements Comparable<LasVersion> {
  public static final LasVersion VERSION_1_0 = new LasVersion(1, 0);

  public static final LasVersion VERSION_1_1 = new LasVersion(1, 1);

  public static final LasVersion VERSION_1_2 = new LasVersion(1, 2);

  public static final LasVersion VERSION_1_3 = new LasVersion(1, 3);

  public static final LasVersion VERSION_1_4 = new LasVersion(1, 4);

  public static LasVersion getVersion14() {
    return VERSION_1_4;
  }

  private final short major;

  private final short minor;

  public LasVersion(final ChannelReader reader) throws IOException {
    this.major = reader.getUnsignedByte();
    this.minor = reader.getUnsignedByte();
  }

  public LasVersion(final int major, final int minor) {
    this.major = (short)major;
    this.minor = (short)minor;
  }

  public boolean atLeast(final LasVersion version) {
    return compareTo(version) >= 0;
  }

  @Override
  public int compareTo(final LasVersion version) {
    int compare = Integer.compare(this.major, version.major);
    if (compare == 0) {
      compare = Integer.compare(this.minor, version.minor);
    }
    return compare;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    } else if (other instanceof LasVersion) {
      final LasVersion version = (LasVersion)other;
      if (this.major == version.major) {
        if (this.minor == version.minor) {
          return true;
        }
      }
    }
    return false;
  }

  public short getMajor() {
    return this.major;
  }

  public short getMinor() {
    return this.minor;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.major;
    result = prime * result + this.minor;
    return result;
  }

  @Override
  public String toString() {
    return this.major + "." + this.minor;
  }
}
