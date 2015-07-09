package com.revolsys.gis.esri.gdb.file.test;

import java.io.File;

import com.revolsys.gis.io.LittleEndianRandomAccessFile;

public class FgdbUtil {

  public void createFileGdb(final File directory) {

  }

  public LittleEndianRandomAccessFile createGdbTable(final File file) {
    final LittleEndianRandomAccessFile out = new LittleEndianRandomAccessFile(file, "rw");
    // Magic
    out.write(0x03);
    out.write(0);
    out.write(0);
    out.write(0);

    out.writeLEInt(0); // Number of valid rows

    // 4 bytes: varying values - unknown role (TBC : this value does have
    // something to do with row size. A value larger than the size of the
    // largest row seems to be ok)

    out.write(0x03);
    out.write(0);
    out.write(0);
    out.write(0);

    out.writeLEInt(0); // Unknown
    out.writeLEInt(0); // Unknown
    out.writeLEInt(40); // file size in bytes
    out.writeLEInt(0); // Unknown
    out.writeLEInt(40); // offset in bytes at which the field description
                        // section begins
    out.writeLEInt(0); // Unknown
    return out;
  }
}
