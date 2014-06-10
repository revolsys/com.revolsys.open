/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2008, GeoSolutions
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
package it.geosolutions.imageio.plugins.ecw;

import it.geosolutions.imageio.stream.input.URIImageInputStream;
import it.geosolutions.imageio.stream.input.URIImageInputStreamImpl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteOrder;

import javax.imageio.stream.IIOByteBuffer;

/**
 * A simple class which allow to handle ECWP protocol on GDAL.
 * Actually, this shouldn't be used as a real ImageInputStream.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 */
public class ECWPImageInputStream implements URIImageInputStream {

  private final static String ECWP_PREFIX = "ecwp://";

  private final URIImageInputStreamImpl uriInputStream;

  public ECWPImageInputStream(final String ecwpUrl) {
    // Can improve checks
    if (ecwpUrl == null) {
      throw new NullPointerException("Specified argument is null");
    } else if (!ecwpUrl.startsWith(ECWP_PREFIX)) {
      throw new IllegalArgumentException("Specified ECWP is not valid");
    }
    URI uri;
    try {
      uri = new URI(ecwpUrl);
    } catch (final URISyntaxException e) {
      throw new IllegalArgumentException(
        "Unable to create a proper stream for the provided input");
    }
    uriInputStream = new URIImageInputStreamImpl(uri);
  }

  public ECWPImageInputStream(final URI uri) {
    uriInputStream = new URIImageInputStreamImpl(uri);
  }

  @Override
  public void close() throws IOException {
    uriInputStream.close();
  }

  @Override
  public void flush() throws IOException {
    uriInputStream.flush();
  }

  @Override
  public void flushBefore(final long pos) throws IOException {
    uriInputStream.flushBefore(pos);
  }

  @Override
  public Class<URI> getBinding() {
    return URI.class;
  }

  @Override
  public int getBitOffset() throws IOException {
    return uriInputStream.getBitOffset();
  }

  @Override
  public ByteOrder getByteOrder() {
    return uriInputStream.getByteOrder();
  }

  public String getECWPLink() {
    final URI uri = getUri();
    String ecwp = null;
    if (uri != null) {
      ecwp = uri.toString();
    }
    return ecwp;
  }

  @Override
  public long getFlushedPosition() {
    return uriInputStream.getFlushedPosition();
  }

  @Override
  public long getStreamPosition() throws IOException {
    return uriInputStream.getStreamPosition();
  }

  @Override
  public URI getTarget() {
    return uriInputStream.getTarget();
  }

  @Override
  public URI getUri() {
    return uriInputStream.getUri();
  }

  @Override
  public boolean isCached() {
    return uriInputStream.isCached();
  }

  @Override
  public boolean isCachedFile() {
    return uriInputStream.isCachedFile();
  }

  @Override
  public boolean isCachedMemory() {
    return uriInputStream.isCachedMemory();
  }

  @Override
  public long length() throws IOException {
    return uriInputStream.length();
  }

  @Override
  public void mark() {
    uriInputStream.mark();
  }

  @Override
  public int read() throws IOException {
    return uriInputStream.read();
  }

  @Override
  public int read(final byte[] b) throws IOException {
    return uriInputStream.read(b);
  }

  @Override
  public int read(final byte[] b, final int off, final int len)
    throws IOException {
    return uriInputStream.read(b, off, len);
  }

  @Override
  public int readBit() throws IOException {
    return uriInputStream.readBit();
  }

  @Override
  public long readBits(final int numBits) throws IOException {
    return uriInputStream.readBits(numBits);
  }

  @Override
  public boolean readBoolean() throws IOException {
    return uriInputStream.readBoolean();
  }

  @Override
  public byte readByte() throws IOException {
    return uriInputStream.readByte();
  }

  @Override
  public void readBytes(final IIOByteBuffer buf, final int len)
    throws IOException {
    // uriInputStream.readBytes(buf, len);
  }

  @Override
  public char readChar() throws IOException {
    return uriInputStream.readChar();
  }

  @Override
  public double readDouble() throws IOException {
    return uriInputStream.readDouble();
  }

  @Override
  public float readFloat() throws IOException {
    return uriInputStream.readFloat();
  }

  @Override
  public void readFully(final byte[] b) throws IOException {
  }

  @Override
  public void readFully(final byte[] b, final int off, final int len)
    throws IOException {
  }

  @Override
  public void readFully(final char[] c, final int off, final int len)
    throws IOException {
  }

  @Override
  public void readFully(final double[] d, final int off, final int len)
    throws IOException {
  }

  @Override
  public void readFully(final float[] f, final int off, final int len)
    throws IOException {
  }

  @Override
  public void readFully(final int[] i, final int off, final int len)
    throws IOException {
  }

  @Override
  public void readFully(final long[] l, final int off, final int len)
    throws IOException {
  }

  @Override
  public void readFully(final short[] s, final int off, final int len)
    throws IOException {
  }

  @Override
  public int readInt() throws IOException {
    return uriInputStream.readInt();
  }

  @Override
  public String readLine() throws IOException {
    return uriInputStream.readLine();
  }

  @Override
  public long readLong() throws IOException {
    return uriInputStream.readLong();
  }

  @Override
  public short readShort() throws IOException {
    return uriInputStream.readShort();
  }

  @Override
  public int readUnsignedByte() throws IOException {
    return uriInputStream.readUnsignedByte();
  }

  @Override
  public long readUnsignedInt() throws IOException {
    return uriInputStream.readUnsignedInt();
  }

  @Override
  public int readUnsignedShort() throws IOException {
    return uriInputStream.readUnsignedShort();
  }

  @Override
  public String readUTF() throws IOException {
    return uriInputStream.readUTF();
  }

  @Override
  public void reset() throws IOException {
  }

  @Override
  public void seek(final long pos) throws IOException {
  }

  @Override
  public void setBitOffset(final int bitOffset) throws IOException {
  }

  @Override
  public void setByteOrder(final ByteOrder byteOrder) {
  }

  @Override
  public int skipBytes(final int n) throws IOException {
    return uriInputStream.skipBytes(n);
  }

  @Override
  public long skipBytes(final long n) throws IOException {
    return uriInputStream.skipBytes(n);
  }
}
