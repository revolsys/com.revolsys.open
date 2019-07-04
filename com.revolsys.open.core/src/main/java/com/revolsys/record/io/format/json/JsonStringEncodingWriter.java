package com.revolsys.record.io.format.json;

import java.io.IOException;
import java.io.Writer;

import com.revolsys.io.FileUtil;

public class JsonStringEncodingWriter extends Writer {

  private Writer out;

  public JsonStringEncodingWriter(final Writer out) {
    this.out = out;
  }

  @Override
  public void close() {
    FileUtil.closeSilent(this.out);
    this.out = null;
  }

  @Override
  public void flush() {
    try {
      final Writer out = this.out;
      if (out != null) {
        out.flush();
      }
    } catch (final IOException e) {
    }
  }

  @Override
  public void write(final char[] chars) throws IOException {
    final int length = chars.length;
    write(chars, 0, length);
  }

  @Override
  public void write(final char[] chars, int startIndex, final int length) throws IOException {
    final Writer out = this.out;
    int count = 0;
    final int endIndex = startIndex + length;
    for (int i = 0; i < endIndex; i++) {
      final char c = chars[i];
      switch (c) {
        case '\b':
          out.write(chars, startIndex, count);
          out.write('\\');
          out.write('b');
          startIndex = i + 1;
          count = 0;
        break;
        case '\t':
          out.write(chars, startIndex, count);
          out.write('\\');
          out.write('t');
          startIndex = i + 1;
          count = 0;
        break;
        case '\n':
          out.write(chars, startIndex, count);
          out.write('\\');
          out.write('n');
          startIndex = i + 1;
          count = 0;
        break;
        case '\f':
          out.write(chars, startIndex, count);
          out.write('\\');
          out.write('f');
          startIndex = i + 1;
          count = 0;
        break;
        case '\r':
          out.write(chars, startIndex, count);
          out.write('\\');
          out.write('r');
          startIndex = i + 1;
          count = 0;
        break;
        case '"':
          out.write(chars, startIndex, count);
          out.write('\\');
          out.write('"');
          startIndex = i + 1;
          count = 0;
        break;
        case '\\':
          out.write(chars, startIndex, count);
          out.write('\\');
          out.write('\\');
          startIndex = i + 1;
          count = 0;
        break;
        default:
          if (count == 1024) {
            out.write(chars, startIndex, count);
            startIndex = i;
            count = 0;
          }
          count++;
        break;
      }
    }
    out.write(chars, startIndex, count);
  }

  @Override
  public void write(final int c) throws IOException {
    final Writer out = this.out;
    switch (c) {
      case '\b':
        out.write('\\');
        out.write('b');
      break;
      case '\t':
        out.write('\\');
        out.write('t');
      break;
      case '\n':
        out.write('\\');
        out.write('n');
      break;
      case '\f':
        out.write('\\');
        out.write('f');
      break;
      case '\r':
        out.write('\\');
        out.write('r');
      break;
      case '"':
        out.write('\\');
        out.write('"');
      break;
      case '\\':
        out.write('\\');
        out.write('\\');
      break;
      default:
        out.write(c);
      break;
    }
  }

  @Override
  public void write(final String string) throws IOException {
    final int length = string.length();
    write(string, 0, length);
  }

  @Override
  public void write(final String string, int startIndex, final int length) throws IOException {
    final Writer out = this.out;
    int count = 0;
    final int endIndex = startIndex + length;
    for (int i = 0; i < endIndex; i++) {
      final char c = string.charAt(i);
      switch (c) {
        case '\b':
          out.write(string, startIndex, count);
          out.write('\\');
          out.write('b');
          startIndex = i + 1;
          count = 0;
        break;
        case '\t':
          out.write(string, startIndex, count);
          out.write('\\');
          out.write('t');
          startIndex = i + 1;
          count = 0;
        break;
        case '\n':
          out.write(string, startIndex, count);
          out.write('\\');
          out.write('n');
          startIndex = i + 1;
          count = 0;
        break;
        case '\f':
          out.write(string, startIndex, count);
          out.write('\\');
          out.write('f');
          startIndex = i + 1;
          count = 0;
        break;
        case '\r':
          out.write(string, startIndex, count);
          out.write('\\');
          out.write('r');
          startIndex = i + 1;
          count = 0;
        break;
        case '"':
          out.write(string, startIndex, count);
          out.write('\\');
          out.write('"');
          startIndex = i + 1;
          count = 0;
        break;
        case '\\':
          out.write(string, startIndex, count);
          out.write('\\');
          out.write('\\');
          startIndex = i + 1;
          count = 0;
        break;
        default:
          if (count == 1024) {
            out.write(string, startIndex, count);
            startIndex = i;
            count = 0;
          }
          count++;
        break;
      }
    }
    out.write(string, startIndex, count);
  }
}
