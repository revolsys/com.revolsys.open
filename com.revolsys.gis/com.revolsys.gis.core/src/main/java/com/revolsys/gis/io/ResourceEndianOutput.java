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
  private Resource resource;

  private OutputStream resourceOut;

  private File file;

  private EndianOutput out;

  public ResourceEndianOutput(
    Resource resource)
    throws IOException {
    this.resource = resource;
    resourceOut = SpringUtil.getOutputStream(resource);
    file = SpringUtil.getFileOrCreateTempFile(resource);
    final OutputStream out = new FileOutputStream(file);
    final BufferedOutputStream bufferedOut = new BufferedOutputStream(out);
    this.out = new EndianOutputStream(bufferedOut);
  }

  public long length() throws IOException {
    return out.length();
  }

  public void close()
    throws IOException {
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

  public void seek(
    long pos)
    throws IOException {
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

  public void flush() {
    out.flush();

  }

  public long getFilePointer()
    throws IOException {
    return out.getFilePointer();
  }

  public void write(
    int i)
    throws IOException {
    out.write(i);
  }

  public void writeBytes(
    String s)
    throws IOException {
    out.writeBytes(s);
  }

  public void writeDouble(
    double d)
    throws IOException {
    out.writeDouble(d);
  }

  public void writeFloat(
    float f)
    throws IOException {
    out.writeFloat(f);
  }

  public void writeInt(
    int i)
    throws IOException {
    out.writeInt(i);
  }

  public void writeLEDouble(
    double d)
    throws IOException {
    out.writeLEDouble(d);
  }

  public void writeLEFloat(
    float f)
    throws IOException {
    out.writeLEFloat(f);
  }

  public void writeLEInt(
    int i)
    throws IOException {
    out.writeLEInt(i);
  }

  public void writeLELong(
    long l)
    throws IOException {
    out.writeLELong(l);

  }

  public void writeLEShort(
    short s)
    throws IOException {
    out.writeLEShort(s);
  }

  public void writeLong(
    long l)
    throws IOException {
    out.writeLong(l);
  }

  public void writeShort(
    short s)
    throws IOException {
    out.writeShort(s);
  }

}
