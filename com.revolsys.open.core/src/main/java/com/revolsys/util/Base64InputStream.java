package com.revolsys.util;

/**
 * A {@link Base64InputStream} will read data from another
 * <tt>java.io.InputStream</tt>, given in the constructor, and encode/decode
 * to/from Base64 notation on the fly.
 *
 * @see Base64
 * @since 1.3
 */
public class Base64InputStream extends java.io.FilterInputStream {

  private final boolean breakLines; // Break lines at less than 80 characters

  private final byte[] buffer; // Small buffer holding converted data

  private final int bufferLength; // Length of buffer (3 or 4)

  private final byte[] decodabet; // Local copies to avoid extra method calls

  private final boolean encode; // Encoding or decoding

  private int lineLength;

  private int numSigBytes; // Number of meaningful bytes in the buffer

  private final int options; // Record options used to create the stream.

  private int position; // Current position in the buffer

  /**
   * Constructs a {@link Base64InputStream} in DECODE mode.
   *
   * @param in the <tt>java.io.InputStream</tt> from which to read data.
   * @since 1.3
   */
  public Base64InputStream(final java.io.InputStream in) {
    this(in, Base64.DECODE);
  }

  /**
   * Constructs a {@link Base64InputStream} in either ENCODE or DECODE mode.
   * <p>
   * Valid options:
   *
   * <pre>
   *   ENCODE or DECODE: Encode or Decode as data is read.
   *   DONT_BREAK_LINES: don't break lines at 76 characters
   *     (only meaningful when encoding)
   *     &lt;i&gt;Note: Technically, this makes your encoding non-compliant.&lt;/i&gt;
   * </pre>
   * <p>
   * Example: <code>new Base64.InputStream( in, Base64.DECODE )</code>
   *
   * @param in the <tt>java.io.InputStream</tt> from which to read data.
   * @param options Specified options
   * @see Base64#ENCODE
   * @see Base64#DECODE
   * @see Base64#DONT_BREAK_LINES
   * @since 2.0
   */
  public Base64InputStream(final java.io.InputStream in, final int options) {
    super(in);
    this.breakLines = (options & Base64.DONT_BREAK_LINES) != Base64.DONT_BREAK_LINES;
    this.encode = (options & Base64.ENCODE) == Base64.ENCODE;
    this.bufferLength = this.encode ? 4 : 3;
    this.buffer = new byte[this.bufferLength];
    this.position = -1;
    this.lineLength = 0;
    this.options = options; // Record for later, mostly to determine which
    // alphabet to use
    this.decodabet = Base64.getDecodabet(options);
  }

  /**
   * Reads enough of the input stream to convert to/from Base64 and returns
   * the next byte.
   *
   * @return next byte
   * @since 1.3
   */
  @Override
  public int read() throws java.io.IOException {
    // Do we need to get data?
    if (this.position < 0) {
      if (this.encode) {
        final byte[] b3 = new byte[3];
        int numBinaryBytes = 0;
        for (int i = 0; i < 3; i++) {
          try {
            final int b = this.in.read();

            // If end of stream, b is -1.
            if (b >= 0) {
              b3[i] = (byte)b;
              numBinaryBytes++;
            } // end if: not end of stream

          } // end try: read
          catch (final java.io.IOException e) {
            // Only a problem if we got no data at all.
            if (i == 0) {
              throw e;
            }

          } // end catch
        } // end for: each needed input byte

        if (numBinaryBytes > 0) {
          Base64.encode3to4(b3, 0, numBinaryBytes, this.buffer, 0, this.options);
          this.position = 0;
          this.numSigBytes = 4;
        } // end if: got data
        else {
          return -1;
        } // end else
      } // end if: encoding

      // Else decoding
      else {
        final byte[] b4 = new byte[4];
        int i = 0;
        for (i = 0; i < 4; i++) {
          // Read four "meaningful" bytes:
          int b = 0;
          do {
            b = this.in.read();
          } while (b >= 0 && this.decodabet[b & 0x7f] <= Base64.WHITE_SPACE_ENC);

          if (b < 0) {
            break; // Reads a -1 if end of stream
          }

          b4[i] = (byte)b;
        } // end for: each needed input byte

        if (i == 4) {
          this.numSigBytes = Base64.decode4to3(b4, 0, this.buffer, 0, this.options);
          this.position = 0;
        } // end if: got four characters
        else if (i == 0) {
          return -1;
        } // end else if: also padded correctly
        else {
          // Must have broken out from above.
          throw new java.io.IOException("Improperly padded Base64 input.");
        } // end

      } // end else: decode
    } // end else: get data

    // Got data?
    if (this.position >= 0) {
      // End of relevant data?
      if ( /* !encode && */this.position >= this.numSigBytes) {
        return -1;
      }

      if (this.encode && this.breakLines && this.lineLength >= Base64.MAX_LINE_LENGTH) {
        this.lineLength = 0;
        return '\n';
      } // end if
      else {
        this.lineLength++; // This isn't important when decoding
        // but throwing an extra "if" seems
        // just as wasteful.

        final int b = this.buffer[this.position++];

        if (this.position >= this.bufferLength) {
          this.position = -1;
        }

        return b & 0xFF; // This is how you "cast" a byte that's
        // intended to be unsigned.
      } // end else
    } // end if: position >= 0

    // Else error
    else {
      // When JDK1.4 is more accepted, use an assertion here.
      throw new java.io.IOException("Error in Base64 code reading stream.");
    } // end else
  }

  /**
   * Calls {@link #read()} repeatedly until the end of stream is reached or
   * <var>len</var> bytes are read. Returns number of bytes read into array or
   * -1 if end of stream is encountered.
   *
   * @param dest array to hold values
   * @param off offset for array
   * @param len max number of bytes to read into array
   * @return bytes read into array or -1 if end of stream is encountered.
   * @since 1.3
   */
  @Override
  public int read(final byte[] dest, final int off, final int len) throws java.io.IOException {
    int i;
    int b;
    for (i = 0; i < len; i++) {
      b = read();

      // if( b < 0 && i == 0 )
      // return -1;

      if (b >= 0) {
        dest[off + i] = (byte)b;
      } else if (i == 0) {
        return -1;
      } else {
        break; // Out of 'for' loop
      }
    } // end for: each byte read
    return i;
  }

}
