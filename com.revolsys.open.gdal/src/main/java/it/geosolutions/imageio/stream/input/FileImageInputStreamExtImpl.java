/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.stream.input;

import it.geosolutions.imageio.stream.eraf.EnhancedRandomAccessFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteOrder;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

/**
 * An implementation of {@link ImageInputStream} that gets its input from a
 * {@link File}. The eraf contents are assumed to be stable during the lifetime
 * of the object.
 * 
 * @author Simone Giannecchini, GeoSolutions
 */
public class FileImageInputStreamExtImpl extends ImageInputStreamImpl {

  /** the associated {@link File}*/
  private final File file;

  private final EnhancedRandomAccessFile eraf;

  private boolean isClosed;

  /**
   * Constructs a {@link FileImageInputStreamExtImpl} that will read from a
   * given {@link File}.
   * 
   * <p>
   * The eraf contents must not change between the time this object is
   * constructed and the time of the last call to a read method.
   * 
   * @param f
   *                a {@link File} to read from.
   * 
   * @exception NullPointerException
   *                    if <code>f</code> is <code>null</code>.
   * @exception SecurityException
   *                    if a security manager exists and does not allow read
   *                    access to the eraf.
   * @exception FileNotFoundException
   *                    if <code>f</code> is a directory or cannot be opened
   *                    for reading for any other reason.
   * @exception IOException
   *                    if an I/O error occurs.
   */
  public FileImageInputStreamExtImpl(final File f)
    throws FileNotFoundException, IOException {
    this(f, -1);
  }

  /**
   * Constructs a {@link FileImageInputStreamExtImpl} that will read from a
   * given {@link File}.
   * 
   * <p>
   * The eraf contents must not change between the time this object is
   * constructed and the time of the last call to a read method.
   * 
   * @param f
   *                a {@link File} to read from.
   * @param bufferSize
   *                size of the underlying buffer.
   * 
   * @exception NullPointerException
   *                    if <code>f</code> is <code>null</code>.
   * @exception SecurityException
   *                    if a security manager exists and does not allow read
   *                    access to the eraf.
   * @exception FileNotFoundException
   *                    if <code>f</code> is a directory or cannot be opened
   *                    for reading for any other reason.
   * @exception IOException
   *                    if an I/O error occurs.
   */
  public FileImageInputStreamExtImpl(final File f, final int bufferSize)
    throws IOException {
    // //
    //
    // Check that the input file is a valid file
    //
    // //
    if (f == null) {
      throw new NullPointerException("f == null!");
    }
    final StringBuilder buff = new StringBuilder("Invalid input file provided");
    if (!f.exists() || f.isDirectory()) {
      buff.append("exists: ").append(f.exists()).append("\n");
      buff.append("isDirectory: ").append(f.isDirectory()).append("\n");
      throw new FileNotFoundException(buff.toString());
    }
    if (!f.exists() || f.isDirectory() || !f.canRead()) {
      buff.append("canRead: ").append(f.canRead()).append("\n");
      throw new IOException(buff.toString());
    }
    this.file = f;
    this.eraf = bufferSize <= 0 ? new EnhancedRandomAccessFile(f, "r")
      : new EnhancedRandomAccessFile(f, "r", bufferSize);
    // NOTE: this must be done accordingly to what ImageInputStreamImpl
    // does, otherwise some ImageReader subclasses might not work.
    this.eraf.setByteOrder(ByteOrder.BIG_ENDIAN);
  }

  /**
   * Closes the underlying {@link EnhancedRandomAccessFile}.
   * 
   * @throws IOException
   *                 in case something bad happens.
   */
  @Override
  public void close() throws IOException {
    try {
      if (!isClosed) {
        super.close();
        eraf.close();
      }
    } finally {
      isClosed = true;
    }
  }

  /**
   * Disposes this {@link FileImageInputStreamExtImpl} by closing its
   * underlying {@link EnhancedRandomAccessFile}.
   * 
   */
  public void dispose() {
    try {
      close();
    } catch (final IOException e) {

    }
  }

  public Class<File> getBinding() {
    return File.class;
  }

  @Override
  public ByteOrder getByteOrder() {

    return eraf.getByteOrder();
  }

  /**
   * Retrieves the {@link File} we are connected to.
   */
  public File getFile() {
    return file;
  }

  @Override
  public long getStreamPosition() throws IOException {
    return eraf.getFilePointer();
  }

  public File getTarget() {
    return file;
  }

  @Override
  public boolean isCached() {
    return eraf.isCached();
  }

  /**
   * Returns the length of the underlying eraf, or <code>-1</code> if it is
   * unknown.
   * 
   * @return the eraf length as a <code>long</code>, or <code>-1</code>.
   */
  @Override
  public long length() {
    try {
      checkClosed();
      return eraf.length();
    } catch (final IOException e) {
      return -1L;
    }
  }

  /**
   * Reads an int from the underlying {@link EnhancedRandomAccessFile}.
   */
  @Override
  public int read() throws IOException {
    checkClosed();
    bitOffset = 0;
    final int val = eraf.read();
    if (val != -1) {
      ++streamPos;
    }
    return val;
  }

  @Override
  public int read(final byte[] b) throws IOException {
    return eraf.read(b);
  }

  /**
   * Read up to <code>len</code> bytes into an array, at a specified offset.
   * This will block until at least one byte has been read.
   * 
   * @param b
   *                the byte array to receive the bytes.
   * @param off
   *                the offset in the array where copying will start.
   * @param len
   *                the number of bytes to copy.
   * @return the actual number of bytes read, or -1 if there is not more data
   *         due to the end of the eraf being reached.
   */
  @Override
  public int read(final byte[] b, final int off, final int len)
    throws IOException {
    checkClosed();
    bitOffset = 0;
    final int nbytes = eraf.read(b, off, len);
    if (nbytes != -1) {
      streamPos += nbytes;
    }
    return nbytes;
  }

  @Override
  public byte readByte() throws IOException {

    return eraf.readByte();
  }

  @Override
  public char readChar() throws IOException {

    return eraf.readChar();
  }

  @Override
  public double readDouble() throws IOException {

    return eraf.readDouble();
  }

  @Override
  public float readFloat() throws IOException {

    return eraf.readFloat();
  }

  @Override
  public void readFully(final byte[] b) throws IOException {

    eraf.readFully(b);
  }

  @Override
  public void readFully(final byte[] b, final int off, final int len)
    throws IOException {

    eraf.readFully(b, off, len);
  }

  @Override
  public int readInt() throws IOException {

    return eraf.readInt();
  }

  @Override
  public String readLine() throws IOException {

    return eraf.readLine();
  }

  @Override
  public long readLong() throws IOException {

    return eraf.readLong();
  }

  @Override
  public short readShort() throws IOException {

    return eraf.readShort();
  }

  @Override
  public int readUnsignedByte() throws IOException {

    return eraf.readUnsignedByte();
  }

  @Override
  public long readUnsignedInt() throws IOException {

    return eraf.readUnsignedInt();
  }

  @Override
  public int readUnsignedShort() throws IOException {

    return eraf.readUnsignedShort();
  }

  @Override
  public String readUTF() throws IOException {

    return eraf.readUTF();
  }

  /**
   * Seeks the current position to pos.
   */
  @Override
  public void seek(final long pos) throws IOException {
    checkClosed();
    if (pos < flushedPos) {
      throw new IllegalArgumentException("pos < flushedPos!");
    }
    bitOffset = 0;
    eraf.seek(pos);
    streamPos = eraf.getFilePointer();
  }

  @Override
  public void setByteOrder(final ByteOrder byteOrder) {

    eraf.setByteOrder(byteOrder);
  }

  @Override
  public int skipBytes(final int n) throws IOException {

    return eraf.skipBytes(n);
  }

  @Override
  public long skipBytes(final long n) throws IOException {

    return eraf.skipBytes(n);
  }

  /**
   * Provides a simple description for this {@link ImageInputStream}.
   * 
   * @return a simple description for this {@link ImageInputStream}.
   */
  @Override
  public String toString() {
    return "FileImageInputStreamExtImpl which points to "
      + this.file.toString();
  }
}
