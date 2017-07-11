package com.revolsys.elevation.cloud.las.zip;

public interface ArithmeticConstants {

  static int AC_BUFFER_SIZE = 1024;

  static int AC_MIN_LENGTH = 0x01000000; // threshold for renormalization

  static int AC_MAX_LENGTH = 0xFFFFFFFF; // maximum AC interval length

  static int BM_LENGTH_SHIFT = 13; // length bits discarded before mult.

  static int BM_MAX_COUNT = 1 << BM_LENGTH_SHIFT; // for adaptive models

  static int DM_LENGTH_SHIFT = 15; // length bits discarded before mult.

  static int DM_MAX_COUNT = 1 << DM_LENGTH_SHIFT; // for adaptive models

}
