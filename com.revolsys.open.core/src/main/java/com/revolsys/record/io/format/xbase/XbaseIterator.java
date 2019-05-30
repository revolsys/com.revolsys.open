package com.revolsys.record.io.format.xbase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.io.EndianInput;
import com.revolsys.io.FileUtil;
import com.revolsys.io.endian.EndianInputStream;
import com.revolsys.io.endian.EndianMappedByteBuffer;
import com.revolsys.io.endian.LittleEndianRandomAccessFile;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Dates;

public class XbaseIterator extends AbstractIterator<Record> implements RecordReader {
  public static final char CHARACTER_TYPE = 'C';

  private static final Map<Character, DataType> DATA_TYPES = new HashMap<>();

  public static final char DATE_TYPE = 'D';

  public static final char FLOAT_TYPE = 'F';

  public static final char LOGICAL_TYPE = 'L';

  public static final char MEMO_TYPE = 'M';

  public static final char NUMBER_TYPE = 'N';

  public static final char OBJECT_TYPE = 'o';

  static {
    DATA_TYPES.put(CHARACTER_TYPE, DataTypes.STRING);
    DATA_TYPES.put(NUMBER_TYPE, DataTypes.DECIMAL);
    DATA_TYPES.put(LOGICAL_TYPE, DataTypes.BOOLEAN);
    DATA_TYPES.put(DATE_TYPE, DataTypes.DATE_TIME);
    DATA_TYPES.put(MEMO_TYPE, DataTypes.STRING);
    DATA_TYPES.put(FLOAT_TYPE, DataTypes.FLOAT);
    DATA_TYPES.put(OBJECT_TYPE, DataTypes.OBJECT);

  }

  private Charset charset = StandardCharsets.UTF_8;

  private boolean closeFile = true;

  private int currentDeletedCount = 0;

  private int deletedCount = 0;

  private long firstIndex;

  private EndianInput in;

  private Runnable initCallback;

  private boolean mappedFile;

  private int numRecords;

  private int position = 0;

  private byte[] recordBuffer;

  private RecordDefinitionImpl recordDefinition;

  private RecordFactory recordFactory;

  private short recordSize;

  private Resource resource;

  private PathName typeName;

  public XbaseIterator(final Resource resource, final RecordFactory recordFactory)
    throws IOException {
    this.resource = resource;
    final String baseName = resource.getBaseName();
    this.typeName = PathName.newPathName("/" + baseName);

    this.recordFactory = recordFactory;
    final Resource codePageResource = resource.newResourceChangeExtension("cpg");
    if (codePageResource != null && codePageResource.exists()) {
      final String charsetName = codePageResource.contentsAsString();
      try {
        this.charset = Charset.forName(charsetName);
      } catch (final Exception e) {
        Logs.debug(this, "Charset " + charsetName + " not supported for " + resource, e);
      }
    }
  }

  public XbaseIterator(final Resource in, final RecordFactory recordFactory,
    final Runnable initCallback) throws IOException {
    this(in, recordFactory);
    this.initCallback = initCallback;
  }

  @Override
  protected void closeDo() {
    if (this.closeFile) {
      forceClose();
    }
  }

  public void forceClose() {
    FileUtil.closeSilent(this.in);
    this.recordFactory = null;
    this.in = null;
    this.initCallback = null;
    this.recordDefinition = null;
    this.recordBuffer = null;
    this.resource = null;
  }

  private Boolean getBoolean(final int startIndex) {
    final char c = (char)this.recordBuffer[startIndex];
    switch (c) {
      case 't':
      case 'T':
      case 'y':
      case 'Y':
        return Boolean.TRUE;

      case 'f':
      case 'F':
      case 'n':
      case 'N':
        return Boolean.FALSE;
      default:
        return null;
    }
  }

  private Date getDate(final int startIndex, final int len) {
    final String dateString = getString(startIndex, len);
    if (dateString.trim().length() == 0 || dateString.equals("0")) {
      return null;
    } else {
      return new java.sql.Date(Dates.getDate("yyyyMMdd", dateString).getTime());
    }
  }

  public int getDeletedCount() {
    return this.deletedCount;
  }

  private Object getMemo(final int startIndex, final int len) throws IOException {
    return null;
    /*
     * String memoIndexString = new String(record, startIndex, len).trim(); if
     * (memoIndexString.length() != 0) { int memoIndex =
     * Integer.parseInt(memoIndexString.trim()); if (memoIn == null) { File
     * memoFile = new File(mappedFile.getParentFile(), typePath + ".dbt"); if
     * (memoFile.exists()) { if (log.isInfoEnabled()) { log.info("Opening memo
     * mappedFile: " + memoFile); } memoIn = new RandomAccessFile(memoFile, "
     * r"); } else { return null; } } memoIn.seek(memoIndex 512); StringBuilder
     * memo = new StringBuilder(512); byte[] memoBuffer = new byte[512]; while
     * (memoIn.read(memoBuffer) != -1) { int i = 0; while (i <
     * memoBuffer.length) { if (memoBuffer[i] == 0x1A) { return memo.toString();
     * } memo.append((char)memoBuffer[i]); i++; } } return memo.toString(); }
     * return null;
     */
  }

  @Override
  protected Record getNext() {
    try {
      Record object = null;
      this.deletedCount = this.currentDeletedCount;
      this.currentDeletedCount = 0;
      int deleteFlag = ' ';
      do {
        deleteFlag = this.in.read();
        if (deleteFlag == -1) {
          throw new NoSuchElementException();
        } else if (deleteFlag == ' ') {
          object = loadRecord();
        } else if (deleteFlag != 0x1A) {
          this.currentDeletedCount++;
          this.in.read(this.recordBuffer);
          this.position++;
        }
      } while (deleteFlag == '*');
      if (object == null) {
        throw new NoSuchElementException();
      }
      return object;
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private BigDecimal getNumber(final int startIndex, final int len) {
    BigDecimal number = null;
    final String numberString = getString(startIndex, len).replaceAll("\\*", "");
    if (numberString.trim().length() != 0) {
      try {
        number = new BigDecimal(numberString.trim());
      } catch (final Throwable e) {
        Logs.error(this, "'" + numberString + " 'is not a valid number", e);
      }
    }
    return number;
  }

  public int getNumRecords() {
    return this.numRecords;
  }

  public int getPosition() {
    return this.position;
  }

  @Override
  public RecordDefinitionImpl getRecordDefinition() {
    open();
    return this.recordDefinition;
  }

  private String getString(final int startIndex, final int len) {
    final String text = new String(this.recordBuffer, startIndex, len, this.charset);
    return text.trim();
  }

  public PathName getTypeName() {
    return this.typeName;
  }

  @Override
  protected void initDo() {
    if (this.in == null) {
      try {
        try {
          final File file = this.resource.getFile();
          final Boolean memoryMapped = getProperty("memoryMapped");
          if (Boolean.TRUE == memoryMapped) {
            this.in = new EndianMappedByteBuffer(file, MapMode.READ_ONLY);
            this.mappedFile = true;
          } else {
            this.in = new LittleEndianRandomAccessFile(file, "r");
          }
        } catch (final IllegalArgumentException e) {
          this.in = new EndianInputStream(this.resource.getInputStream());
        } catch (final FileNotFoundException e) {
          this.in = new EndianInputStream(this.resource.getInputStream());
        }
        loadHeader();
        readRecordDefinition();
        this.recordBuffer = new byte[this.recordSize];
        if (this.initCallback != null) {
          this.initCallback.run();
        }
      } catch (final IOException e) {
        throw new RuntimeException("Error initializing mappedFile ", e);
      }
    }
  }

  public boolean isCloseFile() {
    return this.closeFile;
  }

  /**
   * Load the header record from the shape mappedFile.
   *
   * @throws IOException If an I/O error occurs.
   */
  @SuppressWarnings("unused")
  private void loadHeader() throws IOException {
    final int version = this.in.read();
    final int y = this.in.read();
    final int m = this.in.read();
    final int d = this.in.read();
    // properties.put(new QName("date"), new Date(y, m - 1, d));
    this.numRecords = this.in.readLEInt();
    final short headerSize = this.in.readLEShort();

    this.recordSize = (short)(this.in.readLEShort() - 1);
    this.in.skipBytes(20);
  }

  protected Record loadRecord() throws IOException {
    if (this.in.read(this.recordBuffer) != this.recordBuffer.length) {
      throw new IllegalStateException("Unexpected end of mappedFile");
    }
    final Record object = this.recordFactory.newRecord(this.recordDefinition);
    int startIndex = 0;
    for (int i = 0; i < this.recordDefinition.getFieldCount(); i++) {
      int len = this.recordDefinition.getFieldLength(i);
      final DataType type = this.recordDefinition.getFieldType(i);
      Object value = null;

      if (type == DataTypes.STRING) {
        if (len < 255) {
          value = getString(startIndex, len);
        } else {
          value = getMemo(startIndex, len);
          len = 10;
        }
      } else if (type == DataTypes.DECIMAL || type == DataTypes.FLOAT) {
        value = getNumber(startIndex, len);
      } else if (type == DataTypes.BOOLEAN) {
        value = getBoolean(startIndex);
      } else if (type == DataTypes.DATE_TIME) {
        value = getDate(startIndex, len);
      }
      startIndex += len;
      object.setValue(i, value);
    }
    return object;
  }

  private void readRecordDefinition() throws IOException {
    this.recordDefinition = new RecordDefinitionImpl(this.typeName);
    int b = this.in.read();
    while (b != 0x0D) {
      final StringBuilder fieldName = new StringBuilder();
      boolean endOfName = false;
      for (int i = 0; i < 11; i++) {
        if (!endOfName && b != 0) {
          fieldName.append((char)b);
        } else {

          endOfName = true;
        }
        if (i != 10) {
          b = this.in.read();
        }
      }
      final char fieldType = (char)this.in.read();
      this.in.skipBytes(4);
      int length = this.in.read();
      final int decimalCount = this.in.read();
      this.in.skipBytes(14);
      b = this.in.read();
      final DataType dataType = DATA_TYPES.get(fieldType);
      if (fieldType == MEMO_TYPE) {
        length = Integer.MAX_VALUE;
      }
      this.recordDefinition.addField(fieldName.toString(), dataType, length, decimalCount, false);
    }
    if (this.mappedFile) {
      final EndianMappedByteBuffer file = (EndianMappedByteBuffer)this.in;
      this.firstIndex = file.getFilePointer();
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  public void setCloseFile(final boolean closeFile) {
    this.closeFile = closeFile;
  }

  public void setPosition(final int position) {
    if (this.mappedFile) {
      final EndianMappedByteBuffer file = (EndianMappedByteBuffer)this.in;
      this.position = position;
      try {
        final long offset = this.firstIndex + (long)(this.recordSize + 1) * position;
        file.seek(offset);
        setLoadNext(true);
      } catch (final IOException e) {
        throw new RuntimeException("Unable to seek to " + this.firstIndex, e);
      }

    } else {
      throw new UnsupportedOperationException("The position can only be set on files");
    }
  }

  public void setTypeName(final PathName typeName) {
    this.typeName = typeName;
  }

  @Override
  public String toString() {
    if (this.resource == null) {
      return super.toString();
    } else {
      return this.resource.toString();
    }
  }

}
