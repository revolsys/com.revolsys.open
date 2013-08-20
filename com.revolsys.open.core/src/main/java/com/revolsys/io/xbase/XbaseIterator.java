package com.revolsys.io.xbase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.channels.FileChannel.MapMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.data.io.DataObjectIterator;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.io.EndianInputStream;
import com.revolsys.gis.io.EndianMappedByteBuffer;
import com.revolsys.gis.io.LittleEndianRandomAccessFile;
import com.revolsys.io.EndianInput;
import com.revolsys.io.FileUtil;
import com.revolsys.spring.SpringUtil;

public class XbaseIterator extends AbstractIterator<DataObject> implements
  DataObjectIterator {
  public static final char CHARACTER_TYPE = 'C';

  private static final Map<Character, DataType> DATA_TYPES = new HashMap<Character, DataType>();

  public static final char DATE_TYPE = 'D';

  public static final char FLOAT_TYPE = 'F';

  public static final char LOGICAL_TYPE = 'L';

  public static final char MEMO_TYPE = 'M';

  public static final char NUMBER_TYPE = 'N';

  public static final char OBJECT_TYPE = 'o';

  private boolean closeFile = true;

  static {
    DATA_TYPES.put(CHARACTER_TYPE, DataTypes.STRING);
    DATA_TYPES.put(NUMBER_TYPE, DataTypes.DECIMAL);
    DATA_TYPES.put(LOGICAL_TYPE, DataTypes.BOOLEAN);
    DATA_TYPES.put(DATE_TYPE, DataTypes.DATE_TIME);
    DATA_TYPES.put(MEMO_TYPE, DataTypes.STRING);
    DATA_TYPES.put(FLOAT_TYPE, DataTypes.FLOAT);
    DATA_TYPES.put(OBJECT_TYPE, DataTypes.OBJECT);

  }

  private int currentDeletedCount = 0;

  private final DataObjectFactory dataObjectFactory;

  private final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

  private int deletedCount = 0;

  private EndianInput in;

  private DataObjectMetaDataImpl metaData;

  private final String name;

  private byte[] recordBuffer;

  private short recordSize;

  private Runnable initCallback;

  private int numRecords;

  private int position = 0;

  private long firstIndex;

  private boolean mappedFile;

  private final Resource resource;

  public XbaseIterator(final Resource resource,
    final DataObjectFactory dataObjectFactory) throws IOException {
    this.name = FileUtil.getBaseName(resource.getFilename());
    this.resource = resource;

    this.dataObjectFactory = dataObjectFactory;
  }

  public XbaseIterator(final Resource in,
    final DataObjectFactory dataObjectFactory, final Runnable initCallback)
    throws IOException {
    this(in, dataObjectFactory);
    this.initCallback = initCallback;
  }

  @Override
  protected void doClose() {
    if (closeFile) {
      forceClose();
    }
  }

  @Override
  protected void doInit() {
    if (in == null) {
      try {
        try {
          final File file = SpringUtil.getFile(resource);
          final Boolean memoryMapped = getProperty("memoryMapped");
          if (Boolean.TRUE == memoryMapped) {
            this.in = new EndianMappedByteBuffer(file, MapMode.READ_ONLY);
            this.mappedFile = true;
          } else {
            this.in = new LittleEndianRandomAccessFile(file, "r");
          }
        } catch (final IllegalArgumentException e) {
          this.in = new EndianInputStream(resource.getInputStream());
        } catch (final FileNotFoundException e) {
          this.in = new EndianInputStream(resource.getInputStream());
        }
        loadHeader();
        readMetaData();
        recordBuffer = new byte[recordSize];
        if (initCallback != null) {
          initCallback.run();
        }
      } catch (final IOException e) {
        throw new RuntimeException("Error initializing mappedFile ", e);
      }
    }
  }

  public void forceClose() {
    FileUtil.closeSilent(in);
  }

  private Boolean getBoolean(final int startIndex) {
    final char c = (char)recordBuffer[startIndex];
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
      try {
        return new java.sql.Date(dateFormat.parse(dateString).getTime());
      } catch (final ParseException e) {
        throw new IllegalStateException("'" + dateString
          + "' is not a date in 'YYYYMMDD' format");
      }
    }
  }

  public int getDeletedCount() {
    return deletedCount;
  }

  private Object getMemo(final int startIndex, final int len)
    throws IOException {
    return null;
    /*
     * String memoIndexString = new String(record, startIndex, len).trim(); if
     * (memoIndexString.length() != 0) { int memoIndex =
     * Integer.parseInt(memoIndexString.trim()); if (memoIn == null) { File
     * memoFile = new File(mappedFile.getParentFile(), typePath + ".dbt"); if
     * (memoFile.exists()) { if (log.isInfoEnabled()) { log.info("Opening memo
     * mappedFile:
     * " + memoFile); } memoIn = new RandomAccessFile(memoFile, "r"); } else {
     * return null; } } memoIn.seek(memoIndex 512); StringBuffer memo = new
     * StringBuffer(512); byte[] memoBuffer = new byte[512]; while
     * (memoIn.read(memoBuffer) != -1) { int i = 0; while (i <
     * memoBuffer.length) { if (memoBuffer[i] == 0x1A) { return memo.toString();
     * } memo.append((char)memoBuffer[i]); i++; } } return memo.toString(); }
     * return null;
     */
  }

  @Override
  public DataObjectMetaDataImpl getMetaData() {
    return metaData;
  }

  @Override
  protected DataObject getNext() {
    try {
      DataObject object = null;
      deletedCount = currentDeletedCount;
      currentDeletedCount = 0;
      int deleteFlag = ' ';
      do {
        deleteFlag = in.read();
        if (deleteFlag == -1) {
          throw new NoSuchElementException();
        } else if (deleteFlag == ' ') {
          object = loadDataObject();
        } else if (deleteFlag != 0x1A) {
          currentDeletedCount++;
          in.read(recordBuffer);
          position++;
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
    final String numberString = getString(startIndex, len);
    if (numberString.trim().length() != 0) {
      number = new BigDecimal(numberString.trim());
    }
    return number;
  }

  public int getNumRecords() {
    return numRecords;
  }

  public int getPosition() {
    return position;
  }

  private String getString(final int startIndex, final int len) {
    return new String(recordBuffer, startIndex, len).trim();
  }

  public boolean isCloseFile() {
    return closeFile;
  }

  protected DataObject loadDataObject() throws IOException {
    if (in.read(recordBuffer) != recordBuffer.length) {
      throw new IllegalStateException("Unexpected end of mappedFile");
    }
    final DataObject object = dataObjectFactory.createDataObject(metaData);
    int startIndex = 0;
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      int len = metaData.getAttributeLength(i);
      final DataType type = metaData.getAttributeType(i);
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

  /**
   * Load the header record from the shape mappedFile.
   * 
   * @throws IOException If an I/O error occurs.
   */
  @SuppressWarnings("unused")
  private void loadHeader() throws IOException {
    final int version = in.read();
    final int y = in.read();
    final int m = in.read();
    final int d = in.read();
    // properties.put(new QName("date"), new Date(y, m - 1, d));
    numRecords = in.readLEInt();
    final short headerSize = in.readLEShort();

    this.recordSize = (short)(in.readLEShort() - 1);
    in.skipBytes(20);
  }

  private void readMetaData() throws IOException {
    metaData = new DataObjectMetaDataImpl("/" + name);
    int b = in.read();
    while (b != 0x0D) {
      final StringBuffer fieldName = new StringBuffer();
      boolean endOfName = false;
      for (int i = 0; i < 11; i++) {
        if (!endOfName && b != 0) {
          fieldName.append((char)b);
        } else {

          endOfName = true;
        }
        if (i != 10) {
          b = in.read();
        }
      }
      final char fieldType = (char)in.read();
      in.skipBytes(4);
      int length = in.read();
      final int decimalCount = in.read();
      in.skipBytes(14);
      b = in.read();
      final DataType dataType = DATA_TYPES.get(fieldType);
      if (fieldType == MEMO_TYPE) {
        length = Integer.MAX_VALUE;
      }
      metaData.addAttribute(fieldName.toString(), dataType, length,
        decimalCount, false);
    }
    if (mappedFile) {
      final EndianMappedByteBuffer file = (EndianMappedByteBuffer)in;
      firstIndex = file.getFilePointer();
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
    if (mappedFile) {
      final EndianMappedByteBuffer file = (EndianMappedByteBuffer)in;
      this.position = position;
      try {
        final long offset = firstIndex + (long)(recordSize + 1) * position;
        file.seek(offset);
        setLoadNext(true);
      } catch (final IOException e) {
        throw new RuntimeException("Unable to seek to " + firstIndex, e);
      }

    } else {
      throw new UnsupportedOperationException(
        "The position can only be set on files");
    }
  }

}
