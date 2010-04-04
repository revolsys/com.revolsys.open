package com.revolsys.gis.format.saif.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.ZipFile;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataFactory;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.format.saif.io.util.OsnConverter;
import com.revolsys.gis.format.saif.io.util.OsnConverterRegistry;
import com.revolsys.gis.format.saif.io.util.OsnIterator;
import com.revolsys.gis.io.DataObjectReader;

public class OsnReader implements DataObjectReader {
  private final OsnConverterRegistry converters;

  private File directory;

  private boolean endOfFile = false;

  private DataObjectFactory factory;

  private final String fileName;

  private final DataObjectMetaDataFactory metaDataFactory;

  private boolean nextChecked = false;

  private OsnIterator osnIterator;

  private ZipFile zipFile;

  public OsnReader(
    final DataObjectMetaDataFactory metaDataFactory,
    final File directory,
    final String fileName,
    final int srid)
    throws IOException {
    this.metaDataFactory = metaDataFactory;
    this.directory = directory;
    this.fileName = fileName;
    converters = new OsnConverterRegistry(srid);
    open();
  }

  public OsnReader(
    final DataObjectMetaDataFactory metaDataFactory,
    final ZipFile zipFile,
    final String fileName,
    final int srid)
    throws IOException {
    this.metaDataFactory = metaDataFactory;
    this.fileName = fileName;
    this.zipFile = zipFile;
    converters = new OsnConverterRegistry(srid);
    open();
  }

  /**
   * Get an attribute definition from the iterator.
   * 
   * @param dataObject
   * @param typeName TODO
   * @param iterator The OsnIterator.
   * @return The attribute definition.
   * @throws IOException If an I/O error occurs.
   */
  private void addAttribute(
    final DataObject dataObject) {
    if (osnIterator.getEventType() != OsnIterator.START_ATTRIBUTE) {
      if (osnIterator.next() != OsnIterator.START_ATTRIBUTE) {
        osnIterator.throwParseError("Excepecting an attribute name");
      }
    }
    final String name = osnIterator.getStringValue();
    final Object value = getExpression();
    dataObject.setValue(name, value);
  }

  public void close() {
    try {
      osnIterator.close();
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private Object getDataObject() {
    final QName typeName = osnIterator.getQNameValue();
    final OsnConverter converter = converters.getConverter(typeName);
    if (converter != null) {
      return converter.read(osnIterator);
    } else {
      final DataObjectMetaData type = metaDataFactory.getMetaData(typeName);
      final DataObject dataObject = factory.createDataObject(type);
      while (osnIterator.next() != OsnIterator.END_OBJECT) {
        addAttribute(dataObject);
      }
      return dataObject;
    }
  }

  /**
   * Get the value of an expression from the iterator.
   * 
   * @return The value of the expression.
   * @throws IOException If an I/O error occurs.
   */
  private Object getExpression() {
    final Object eventType = osnIterator.next();
    if (eventType == OsnIterator.START_DEFINITION) {
      return getDataObject();
    } else if (eventType == OsnIterator.START_SET) {
      final Set<Object> set = new LinkedHashSet<Object>();
      processCollection(set, OsnIterator.END_SET);
      return set;
    } else if (eventType == OsnIterator.START_LIST) {
      final List<Object> list = new ArrayList<Object>();
      processCollection(list, OsnIterator.END_LIST);
      return list;
    } else if (eventType == OsnIterator.TEXT_VALUE) {
      return osnIterator.getValue();
    } else if (eventType == OsnIterator.NUMERIC_VALUE) {
      return osnIterator.getValue();
    } else if (eventType == OsnIterator.BOOLEAN_VALUE) {
      return osnIterator.getValue();
    } else if (eventType == OsnIterator.ENUM_TAG) {
      return osnIterator.getValue();
    } else if (eventType == OsnIterator.UNKNOWN) {
      osnIterator.throwParseError("Expected an expression");
    }
    return null;
  }

  /**
   * @return the factory
   */
  public DataObjectFactory getFactory() {
    return factory;
  }

  /**
   * @return the fileName
   */
  public String getFileName() {
    return fileName;
  }

  public DataObjectMetaDataFactory getMetaDataFactory() {
    return metaDataFactory;
  }

  public String toString() {
    return fileName;
  }

  public boolean hasNext() {
    if (nextChecked) {
      return true;
    } else if (endOfFile) {
      return false;
    } else {
      if (osnIterator.hasNext()) {
        Object eventType = osnIterator.getEventType();
        if (eventType != OsnIterator.START_DEFINITION) {
          eventType = osnIterator.next();
        }
        if (eventType == OsnIterator.START_DEFINITION) {
          nextChecked = true;
          return true;
        } else if (eventType != OsnIterator.END_DOCUMENT
          && eventType != OsnIterator.END_SET) {
          osnIterator.throwParseError("Excepecting start of an object");
        }
      }
    }
    endOfFile = true;
    return false;
  }

   public DataObject next() {
    if (hasNext()) {
      nextChecked = false;
      return (DataObject)getDataObject();
    } else {
      throw new NoSuchElementException();
    }
  }

  public void open() {
    try {
      if (directory != null) {
        osnIterator = new OsnIterator(directory, fileName);
      } else {
        osnIterator = new OsnIterator(zipFile, fileName);
      }
      skipToFirstDataObject();
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Process a collection, loading all the values into the collection.
   * 
   * @param collection The collection to save the objects to.
   * @param endEventType The event type indicating the end of a collection.
   * @throws IOException If an I/O error occurs.
   */
  private void processCollection(
    final Collection<Object> collection,
    final Object endEventType) {
    while (osnIterator.getEventType() != endEventType) {
      final Object value = getExpression();
      if (value != null || osnIterator.getEventType() == OsnIterator.NULL_VALUE) {
        collection.add(value);
      }
    }
  }

  public void remove() {
    throw new UnsupportedOperationException(
      "Removing SAIF objects is not supported");
  }

  /**
   * @param factory the factory to set
   */
  public void setFactory(
    final DataObjectFactory factory) {
    this.factory = factory;
  }

  /**
   * Skip all objects and attributes until the first object in the collection.
   * 
   * @return True if an object was found.
   * @throws IOException If an I/O error occurs.
   */
  private boolean skipToFirstDataObject()
    throws IOException {
    if (osnIterator.next() == OsnIterator.START_DEFINITION) {
      final QName typeName = osnIterator.getQNameValue();
      final DataObjectMetaDataImpl type = (DataObjectMetaDataImpl)metaDataFactory.getMetaData(typeName);
      final DataObjectMetaData spatialDataSetType = metaDataFactory.getMetaData(new QName(
        "SpatialDataSet"));
      if (type != null && type.isInstanceOf(spatialDataSetType)) {
        final String oiName = osnIterator.nextAttributeName();

        if (oiName != null && oiName.equals("objectIdentifier")) {
          osnIterator.nextStringValue();
          final String attributeName = osnIterator.nextAttributeName();
          if (attributeName != null
            && (attributeName.equals("geoComponents") || attributeName.equals("annotationComponents"))) {
            if (osnIterator.next() == OsnIterator.START_SET) {
              return true;
            } else {
              osnIterator.throwParseError("Expecting a set of objects");
            }
          } else {
            osnIterator.throwParseError("Excepecting the 'geoComponents' attribute");
          }

        } else {
          osnIterator.throwParseError("Expecting the 'objectIdentifier' attribute");
        }
      } else {
        return true;
      }
    } else {
      osnIterator.throwParseError("Expecting a start of an object definition");
    }
    return false;
  }
}
