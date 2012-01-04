package com.revolsys.io.page;

public class ByteArrayPage extends AbstractPage {
  private final byte[] content;

  private int offset = 0;

  public ByteArrayPage(final PageManager pageManager, final int index,
    final int size) {
    super(pageManager, index);
    content = new byte[size];
  }

  public byte[] getContent() {
    return content;
  }

  public int getOffset() {
    return offset;
  }

  public int getSize() {
    return content.length;
  }

  @Override
  protected int readNextByte() {
    final byte b = content[offset];
    offset++;
    return b & 0xff;
  }

  public void setContent(final Page page) {
    final byte[] copyContent = page.getContent();
    System.arraycopy(copyContent, 0, content, 0, copyContent.length);
  }

  public void setOffset(final int offset) {
    if (offset > getSize()) {
      throw new IllegalArgumentException("Cannot set offset past end of file ");
    } else {
      this.offset = offset;
    }
  }

  @Override
  protected void writeByte(final int b) {
    content[offset++] = (byte)b;
  }
}
