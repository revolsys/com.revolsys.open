package com.revolsys.format.tcx;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import com.revolsys.data.io.RecordIterator;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.format.gpx.GpxConstants;
import com.revolsys.format.xml.StaxUtils;
import com.revolsys.io.FileUtil;

public class TcxIterator implements RecordIterator {

  private static final Logger LOG = Logger.getLogger(TcxIterator.class);

  private Record currentRecord;

  private File file;

  private boolean hasNext = true;

  private final XMLStreamReader in;

  private boolean loadNextObject = true;

  private final String schemaName = TcxConstants._NS_URI;

  private String typePath;

  private String baseName;

  private final Queue<Record> records = new LinkedList<Record>();

  public TcxIterator(final File file) throws IOException, XMLStreamException {
    this(new FileReader(file));
  }

  public TcxIterator(final Reader in) throws IOException, XMLStreamException {
    this(StaxUtils.createXmlReader(in));
  }

  public TcxIterator(final Reader in, final RecordFactory recordFactory,
    final String path) {
    this(StaxUtils.createXmlReader(in));
    this.typePath = path;
  }

  public TcxIterator(final Resource resource,
    final RecordFactory recordFactory, final String path) throws IOException {
    this(StaxUtils.createXmlReader(resource));
    this.typePath = path;
    this.baseName = FileUtil.getBaseName(resource.getFilename());
  }

  public TcxIterator(final XMLStreamReader in) {
    this.in = in;
    // try {
    // StaxUtils.skipToStartElement(in);
    // // skipMetaData();
    // } catch (final XMLStreamException e) {
    // throw new RuntimeException(e.getMessage(), e);
    // }
  }

  public void close() {
    try {
      this.in.close();
    } catch (final XMLStreamException e) {
      LOG.error(e.getMessage(), e);
    }

  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return GpxConstants.GPX_TYPE;
  }

  public String getSchemaName() {
    return this.schemaName;
  }

  @Override
  public boolean hasNext() {
    if (!this.hasNext) {
      return false;
    } else if (this.loadNextObject) {
      return loadNextRecord();
    } else {
      return true;
    }
  }

  protected boolean loadNextRecord() {
    // try {
    do {
      // this.currentRecord = parseRecord();
    } while (this.currentRecord != null
        && this.typePath != null
        && !this.currentRecord.getRecordDefinition()
        .getPath()
        .equals(this.typePath));
    this.loadNextObject = false;
    if (this.currentRecord == null) {
      close();
      this.hasNext = false;
    }
    return this.hasNext;
    // } catch (final XMLStreamException e) {
    // throw new RuntimeException(e.getMessage(), e);
    // }
  }

  @Override
  public Record next() {
    if (hasNext()) {
      this.loadNextObject = true;
      return this.currentRecord;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return this.file.getAbsolutePath();
  }

}
