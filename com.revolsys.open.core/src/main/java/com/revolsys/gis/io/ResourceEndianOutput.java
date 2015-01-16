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
    this.resourceOut = SpringUtil.getOutputStream(resource);
    this.file = SpringUtil.getFileOrCreateTempFile(resource);
    final OutputStream out = new FileOutputStream(this.file);
    final BufferedOutputStream bufferedOut = new BufferedOutputStream(out);
    this.out = new EndianOutputStream(bufferedOut);
  }

  @Override
  public void close() throws IOException {
    try {
      this.out.close();
    } finally {
      if (!(this.resource instanceof FileSystemResource)) {
        try {
          FileUtil.copy(this.file, this.resourceOut);
          this.resourceOut.flush();
        } finally {
          FileUtil.closeSilent(this.resourceOut);
          if (!(this.resource instanceof FileSystemResource)) {
            this.file.delete();
          }
        }
      }
    }
  }

  @Override
  public void flush() {
    this.out.flush();

  }

  @Override
  public long getFilePointer() throws IOException {
    return this.out.getFilePointer();
  }

  @Override
  public long length() throws IOException {
    return this.out.length();
  }

  public void seek(final long pos) throws IOException {
    final LittleEndianRandomAccessFile raOut;
    if (this.out instanceof LittleEndianRandomAccessFile) {
      raOut = (LittleEndianRandomAccessFile)this.out;
    } else {
      this.out.flush();
      this.out.close();
      raOut = new LittleEndianRandomAccessFile(this.file, "rw");
      this.out = raOut;
    }
    raOut.seek(pos);
  }

  @Override
  public void write(final byte[] bytes) throws IOException {
    this.out.write(bytes);
  }

  @Override
  public void write(final byte[] bytes, final int offset, final int length)
      throws IOException {
    this.out.write(bytes, offset, length);
  }

  @Override
  public void write(final int i) throws IOException {
    this.out.write(i);
  }

  @Override
  public void writeBytes(final String s) throws IOException {
    this.out.writeBytes(s);
  }

  @Override
  public void writeDouble(final double d) throws IOException {
    this.out.writeDouble(d);
  }

  @Override
  public void writeFloat(final float f) throws IOException {
    this.out.writeFloat(f);
  }

  @Override
  public void writeInt(final int i) throws IOException {
    this.out.writeInt(i);
  }

  @Override
  public void writeLEDouble(final double d) throws IOException {
    this.out.writeLEDouble(d);
  }

  @Override
  public void writeLEFloat(final float f) throws IOException {
    this.out.writeLEFloat(f);
  }

  @Override
  public void writeLEInt(final int i) throws IOException {
    this.out.writeLEInt(i);
  }

  @Override
  public void writeLELong(final long l) throws IOException {
    this.out.writeLELong(l);

  }

  @Override
  public void writeLEShort(final short s) throws IOException {
    this.out.writeLEShort(s);
  }

  @Override
  public void writeLong(final long l) throws IOException {
    this.out.writeLong(l);
  }

  @Override
  public void writeShort(final short s) throws IOException {
    this.out.writeShort(s);
  }

}
