package com.revolsys.io.shp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDirectoryReader;
import com.revolsys.gis.data.io.DataObjectDirectoryReader;
import com.revolsys.gis.data.io.DataObjectIteratorReader;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.Reader;
import com.revolsys.spring.SpringUtil;

/**
 * <p>
 * The ShapeDirectoryReader is a that can read .shp
 * data files contained in a single directory. The reader will iterate through
 * the .shp files in alpabetical order returning all features.
 * </p>
 * <p>
 * See the {@link AbstractDirectoryReader} class for examples on how to use
 * dataset readers.
 * </p>
 * 
 * @author Paul Austin
 * @see AbstractDirectoryReader
 */
public class ShapeDirectoryReader extends DataObjectDirectoryReader {
  private final Map<String, String> fileNameTypeMap = new HashMap<String, String>();

  private Map<String, DataObjectMetaData> typeNameMetaDataMap = new HashMap<String, DataObjectMetaData>();

  public ShapeDirectoryReader() {
    setFileExtensions(ShapefileConstants.FILE_EXTENSION);
  }

  /**
   * Construct a new ShapeDirectoryReader.
   * 
   * @param directory The containing the .shp files.
   */
  public ShapeDirectoryReader(final File directory) {
    this();
    setDirectory(directory);
  }

  @Override
  protected Reader<DataObject> createReader(final Resource resource) {
    try {
      final ArrayDataObjectFactory factory = new ArrayDataObjectFactory();
      final ShapefileIterator iterator = new ShapefileIterator(resource,
        factory);
      final String baseName = SpringUtil.getBaseName(resource).toUpperCase();
      iterator.setTypeName(fileNameTypeMap.get(baseName));
      iterator.setMetaData(typeNameMetaDataMap.get(iterator.getTypeName()));
      return new DataObjectIteratorReader(iterator);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }

  public Map<String, String> getFileNameTypeMap() {
    return fileNameTypeMap;
  }

  public Map<String, DataObjectMetaData> getTypeNameMetaDataMap() {
    return typeNameMetaDataMap;
  }

  public void setFileNameTypeMap(final Map<String, String> fileNameTypeMap) {
    this.fileNameTypeMap.clear();
    for (final Entry<String, String> entry : fileNameTypeMap.entrySet()) {
      final String fileName = entry.getKey();
      final String typeName = entry.getValue();
      this.fileNameTypeMap.put(fileName.toUpperCase(), typeName);
    }
    setBaseFileNames(fileNameTypeMap.keySet());
  }

  public void setTypeNameMetaDataMap(
    final Map<String, DataObjectMetaData> typeNameMetaDataMap) {
    this.typeNameMetaDataMap = typeNameMetaDataMap;
  }

}
