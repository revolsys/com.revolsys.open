package com.revolsys.gis.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.io.FileUtil;
import com.revolsys.spring.SpringUtil;

public class ResourceEndianOutput implements EndianOutput {
  private final Resource resource;

  private final OutputStream resourceOut;

  private final File file;

  private EndianOutput out;

  public ResourceEndianOutput(final Resource resource) throws IOException {
    this.resource = resource;
    resourceOut = SpringUtil.getOutputStream(resource);
    file = SpringUtil.getFileOrCreateTempFile(resource);
    final OutputStream out = new FileOutputStream(file);
    final BufferedOutputStream bufferedOut = new BufferedOutputStream(out);
    this.out = new EndianOutputStream(bufferedOut);
  }

  @Override
  public void close() throws IOException {
    try {
      out.close();
    } finally {
      if (!(resource instanceof FileSystemResource)) {
        try {
          FileUtil.copy(file, resourceOut);
          resourceOut.flush();
        } finally {
          FileUtil.closeSilent(resourceOut);
          if (!(resource instanceof FileSystemResource)) {
            file.delete();
          }
        }
      }
    }
  }

  @Override
  public void flush() {
    out.flush();

  }

  @Override
  public long getFilePointer() throws IOException {
    return out.getFilePointer();
  }

  @Override
  public long length() throws IOException {
    return out.length();
  }

  public void seek(final long pos) throws IOException {
    final LittleEndianRandomAccessFile raOut;
    if (out instanceof LittleEndianRandomAccessFile) {
      raOut = (LittleEndianRandomAccessFile)out;
    } else {
      out.flush();
      out.close();
      raOut = new LittleEndianRandomAccessFile(file, "rw");
      out = raOut;
    }
    raOut.seek(pos);
  }

  @Override
  public void write(final byte[] bytes) throws IOException {
    out.write(bytes);
  }

  @Override
  public void write(final byte[] bytes, final int offset, final int length)
    throws IOException {
    out.write(bytes, offset, length);
  }

  @Override
  public void write(final int i) throws IOException {
    out.write(i);
  }

  @Override
  public void writeBytes(final String s) throws IOException {
    out.writeBytes(s);
  }

  @Override
  public void writeDouble(final double d) throws IOException {
    out.writeDouble(d);
  }

  @Override
  public void writeFloat(final float f) throws IOException {
    out.writeFloat(f);
  }

  @Override
  public void writeInt(final int i) throws IOException {
    out.writeInt(i);
  }

  @Override
  public void writeLEDouble(final double d) throws IOException {
    out.writeLEDouble(d);
  }

  @Override
  public void writeLEFloat(final float f) throws IOException {
    out.writeLEFloat(f);
  }

  @Override
  public void writeLEInt(final int i) throws IOException {
    out.writeLEInt(i);
  }

  @Override
  public void writeLELong(final long l) throws IOException {
    out.writeLELong(l);

  }

  @Override
  public void writeLEShort(final short s) throws IOException {
    out.writeLEShort(s);
  }

  @Override
  public void writeLong(final long l) throws IOException {
    out.writeLong(l);
  }

  @Override
  public void writeShort(final short s) throws IOException {
    out.writeShort(s);
  }

}
