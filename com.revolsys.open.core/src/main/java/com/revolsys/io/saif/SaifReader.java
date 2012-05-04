/*
 * $URL:https://secure.revolsys.com/svn/open.revolsys.com/GIS/trunk/src/main/java/com/revolsys/gis/format/saif/io/SaifReader.java $
 * $Author:paul.austin@revolsys.com $
 * $Date:2007-06-09 09:28:28 -0700 (Sat, 09 Jun 2007) $
 * $Revision:265 $

 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.io.saif;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataFactory;
import com.revolsys.gis.io.DataObjectReader;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.FileUtil;
import com.revolsys.io.PathUtil;
import com.revolsys.io.saif.util.PathCache;

/**
 * <p>
 * The SaifReader.
 * </p>
 * 
 * @author Paul Austin
 * @see SaifWriter
 */
public class SaifReader extends AbstractObjectWithProperties implements
  DataObjectReader, DataObjectMetaDataFactory {
  /** The logging instance. */
  private static final Logger log = Logger.getLogger(SaifReader.class);

  /** The current data object that was read. */
  private DataObject currentDataObject;

  /** The schema definition declared in the SAIF archive. */
  private DataObjectMetaDataFactory declaredMetaDataFactory;

  /** List of type names to exclude from reading. */
  private final Set<String> excludeTypeNames = new LinkedHashSet<String>();

  /** The list of exported objects. */
  private DataObject exportedObjects;

  private DataObjectFactory factory = new ArrayDataObjectFactory();

  /** The SAIF archive file. */
  private File file;

  /** Mapping between file names and type names. */
  private final Map<String, String> fileNameTypeNameMap = new HashMap<String, String>();

  /** The global metatdata for the archive. */
  private DataObject globalMetadata;

  /** Flag indicating if the iterator has more objects. */
  private boolean hasNext;

  /** The list of imported objects. */
  private DataObject importedObjects;

  /** List of type names to include for reading. */
  private final Set<String> includeTypeNames = new LinkedHashSet<String>();

  /** The list of internally referenced objects. */
  private DataObject internallyReferencedObjects;

  /** Flag indicating if a new data object should be read. */
  private boolean loadNewObject = true;

  /** The schema definition that will be set on each data object. */
  private DataObjectMetaDataFactory metaDataFactory;

  /** The iterator for the current object set. */
  private OsnReader osnReader;

  /** The directory the SAIF archive is extracted to. */
  private File saifArchiveDirectory;

  private int srid = 26910;

  /** Mapping between type names and file names. */
  private final Map<String, String> typePathFileNameMap = new HashMap<String, String>();

  /** The iterator of object subsets for the archive. */
  private Iterator<String> typePathIterator;

  private List<String> typePaths;

  /** The zip file. */
  private ZipFile zipFile;

  public SaifReader() {
  }

  /**
   * Create a new SaifReader to read the SAIF archive from the specified file .
   * If the file is a directory, then in must contain an expanded SAIF archive,
   * otherwise the file must be a compressed SAIF archive (.zip or.saf).
   * 
   * @param file The SAIF archive file to read.
   */
  public SaifReader(final File file) {
    setFile(file);
  }

  /**
   * Create a new SaifReader to read the SAIF archive from the specified file
   * name. If the file is a directory, then in must contain an expanded SAIF
   * archive, otherwise the file must be a compressed SAIF archive (.zip
   * or.saf).
   * 
   * @param fileName The name of the SAIF archive file to read.
   */
  public SaifReader(final String fileName) {
    this(new File(fileName));
  }

  /**
   * Close the SAIF archive.
   */
  public void close() {
    if (log.isDebugEnabled()) {
      log.debug("Closing SAIF archive '" + file.getAbsolutePath() + "'");
    }
    closeCurrentReader();
    if (!file.isDirectory() && saifArchiveDirectory != null) {
      if (log.isDebugEnabled()) {
        log.debug("  Deleting temporary files");
      }
      FileUtil.deleteDirectory(saifArchiveDirectory);
    }
    if (log.isDebugEnabled()) {
      log.debug("  Finished closing file");
    }
  }

  private void closeCurrentReader() {
    if (osnReader != null) {
      osnReader.close();
      osnReader = null;
    }
  }

  /**
   * Get the schema definition declared in the SAIF archive.
   * 
   * @return The schema definition.
   */
  public DataObjectMetaDataFactory getDeclaredMetaDataFactory() {
    return declaredMetaDataFactory;
  }

  /**
   * Get the list of exported objects for the SAIF archive.
   * 
   * @return The exported objects.
   */
  public DataObject getExportedObjects() {
    return exportedObjects;
  }

  /**
   * @return the factory
   */
  public DataObjectFactory getFactory() {
    return factory;
  }

  public File getFile() {
    return file;
  }

  private String getFileName(final String typePath) {
    return typePathFileNameMap.get(typePath);
  }

  /**
   * Get the global metatdata for the SAIF archive.
   * 
   * @return The global metadata.
   */
  public DataObject getGlobalMetadata() {
    if (globalMetadata == null) {
      try {
        loadGlobalMetadata();
      } catch (final IOException e) {
        throw new RuntimeException("Unable to load globmeta.osn: "
          + e.getMessage());
      }
    }
    return globalMetadata;
  }

  /**
   * Get the list of imported objects for the SAIF archive.
   * 
   * @return The imported objects.
   */
  public DataObject getImportedObjects() {
    if (importedObjects == null) {
      try {
        loadImportedObjects();
      } catch (final IOException e) {
        throw new RuntimeException("Unable to load imports.dir: "
          + e.getMessage());
      }
    }
    return importedObjects;
  }

  private InputStream getInputStream(final String fileName) throws IOException {
    if (zipFile != null) {
      final ZipEntry entry = zipFile.getEntry(fileName);
      return zipFile.getInputStream(entry);
    } else {
      return new FileInputStream(new File(saifArchiveDirectory, fileName));
    }
  }

  /**
   * Get the list of internally referenced objects for the SAIF archive.
   * 
   * @return The internally referenced objects.
   */
  public DataObject getInternallyReferencedObjects() {
    if (internallyReferencedObjects == null) {
      try {
        loadInternallyReferencedObjects();
      } catch (final IOException e) {
        throw new RuntimeException("Unable to load internal.dir: "
          + e.getMessage());
      }
    }
    return internallyReferencedObjects;
  }

  public DataObjectMetaData getMetaData(final String typePath) {
    return metaDataFactory.getMetaData(typePath);
  }

  /**
   * Get the schema definition that will be set on each data object.
   * 
   * @return The schema definition.
   */
  public DataObjectMetaDataFactory getMetaDataFactory() {
    return metaDataFactory;
  }

  private <D extends DataObject> OsnReader getOsnReader(
    final DataObjectMetaDataFactory metaDataFactory,
    final DataObjectFactory factory,
    final String className) throws IOException {
    String fileName = typePathFileNameMap.get(className);
    if (fileName == null) {
      fileName = PathUtil.getName(className);
    }
    OsnReader reader;
    if (zipFile != null) {
      reader = new OsnReader(metaDataFactory, zipFile, fileName, srid);
    } else {
      reader = new OsnReader(metaDataFactory, saifArchiveDirectory, fileName,
        srid);
    }
    reader.setFactory(factory);
    return reader;
  }

  public <D extends DataObject> OsnReader getOsnReader(
    final String className,
    final DataObjectFactory factory) throws IOException {
    final DataObjectMetaDataFactory metaDataFactory = this.metaDataFactory;
    return getOsnReader(metaDataFactory, factory, className);

  }

  public int getSrid() {
    return srid;
  }

  private String getTypeName(final String fileName) {
    return fileNameTypeNameMap.get(fileName);
  }

  /**
   * @return the typePathObjectSetMap
   */
  public Map<String, String> getTypeNameFileNameMap() {
    return typePathFileNameMap;
  }

  public List<String> getTypeNames() {
    return typePaths;
  }

  private boolean hasData(final String typePath) {
    final String fileName = getFileName(typePath);
    if (fileName == null) {
      return false;
    } else if (zipFile != null) {
      return zipFile.getEntry(fileName) != null;
    } else {
      return new File(this.saifArchiveDirectory, fileName).exists();
    }
  }

  /**
   * Check to see if the reader has more data objects to be read.
   * 
   * @return True if the reader has more data objects to be read.
   */
  public boolean hasNext() {
    if (loadNewObject) {
      return loadNextDataObject();
    }
    return hasNext;
  }

  /**
   * Load the exported objects for the SAIF archive.
   * 
   * @throws IOException If there was an I/O error.
   */
  @SuppressWarnings("unchecked")
  private void loadExportedObjects() throws IOException {
    final boolean setNames = includeTypeNames.isEmpty();
    ClassPathResource resource = new ClassPathResource(
      "com/revolsys/io/saif/saifzip.csn");
    DataObjectMetaDataFactory schema = new SaifSchemaReader().loadSchema(resource);
    final OsnReader reader = getOsnReader(schema, factory, "/exports.dir");
    try {
      final Map<String, String> names = new TreeMap<String, String>();
      if (reader.hasNext()) {
        exportedObjects = reader.next();
        final Set<DataObject> handles = ((Set<DataObject>)exportedObjects.getValue("handles"));
        for (final DataObject exportedObject : handles) {
          final String fileName = (String)exportedObject.getValue("objectSubset");
          if (fileName != null && !fileName.equals("globmeta.osn")) {
            String typePath = getTypeName(fileName);
            if (typePath == null) {
              final String name = (String)exportedObject.getValue("type");
              typePath = PathCache.getName(name);
              if (!fileNameTypeNameMap.containsKey(fileName)) {
                fileNameTypeNameMap.put(fileName, typePath);
                typePathFileNameMap.put(typePath, fileName);
              }
            }

            if (setNames && !fileName.equals("metdat00.osn")
              && !fileName.equals("refsys00.osn")) {
              names.put(typePath.toString(), typePath);
            }
          }
        }
        if (setNames) {
          typePaths = new ArrayList<String>(names.values());
        } else {
          typePaths = new ArrayList<String>(includeTypeNames);
        }
        typePaths.removeAll(excludeTypeNames);
      }
    } finally {
      reader.close();
    }
  }

  /**
   * Load the global metatdata for the SAIF archive.
   * 
   * @throws IOException If there was an I/O error.
   */
  private void loadGlobalMetadata() throws IOException {
    final OsnReader reader = getOsnReader("/globmeta.osn", factory);
    try {
      if (reader.hasNext()) {
        globalMetadata = osnReader.next();
      }
    } finally {
      reader.close();
    }
  }

  /**
   * Load the imported objects for the SAIF archive.
   * 
   * @throws IOException If there was an I/O error.
   */
  private void loadImportedObjects() throws IOException {
    final OsnReader reader = getOsnReader("/imports.dir", factory);
    try {
      if (reader.hasNext()) {
        importedObjects = osnReader.next();
      }
    } finally {
      reader.close();
    }
  }

  /**
   * Load the internally referenced objects for the SAIF archive.
   * 
   * @throws IOException If there was an I/O error.
   */
  private void loadInternallyReferencedObjects() throws IOException {
    final OsnReader reader = getOsnReader("/internal.dir", factory);
    try {
      if (reader.hasNext()) {
        internallyReferencedObjects = osnReader.next();
      }
    } finally {
      reader.close();
    }
  }

  /**
   * Load the next data object from the archive. A new subset will be loaded if
   * required or if there was an error reading from one of the subsets.
   * 
   * @return True if an object was loaded.
   */
  private boolean loadNextDataObject() {
    boolean useCurrentFile = true;
    if (osnReader == null) {
      useCurrentFile = false;
    } else if (!osnReader.hasNext()) {
      useCurrentFile = false;
    }
    if (!useCurrentFile) {
      if (!openNextObjectSet()) {
        currentDataObject = null;
        hasNext = false;
        return false;
      }
    }
    do {
      try {
        currentDataObject = osnReader.next();
        hasNext = true;
        loadNewObject = false;
        return true;
      } catch (final Throwable e) {
        log.error(e.getMessage(), e);
      }
    } while (openNextObjectSet());
    currentDataObject = null;
    hasNext = false;
    return false;
  }

  /**
   * Load the schema from the SAIF archive.
   * 
   * @throws IOException If there was an I/O error.
   */
  private void loadSchema() throws IOException {
    final SaifSchemaReader parser = new SaifSchemaReader();

    final InputStream in = getInputStream("clasdefs.csn");
    try {
      declaredMetaDataFactory = parser.loadSchema("clasdefs.csn", in);
    } finally {
      FileUtil.closeSilent(in);
    }
    if (metaDataFactory == null) {
      setMetaDataFactory(declaredMetaDataFactory);
    }
  }

  private void loadSrid() throws IOException {
    final OsnReader reader = getOsnReader("/refsys00.osn", factory);
    try {
      if (reader.hasNext()) {
        final DataObject spatialReferencing = reader.next();
        final DataObject coordinateSystem = spatialReferencing.getValue("coordSystem");
        if (coordinateSystem.getMetaData().getPath().equals("/UTM")) {
          final Number srid = coordinateSystem.getValue("zone");
          setSrid(26900 + srid.intValue());
        }
      }
    } finally {
      reader.close();
    }
  }

  /**
   * Get the next data object read by this reader. .
   * 
   * @return The next DataObject.
   * @exception NoSuchElementException If the reader has no more data objects.
   */
  public DataObject next() {
    if (hasNext()) {
      loadNewObject = true;
      return currentDataObject;
    } else {
      throw new NoSuchElementException();
    }
  }

  /**
   * Open a SAIF archive, extracting compressed archives to a temporary
   * directory.
   */
  public void open() {
    try {
      if (log.isDebugEnabled()) {
        log.debug("Opening SAIF archive '" + file.getCanonicalPath() + "'");
      }
      if (file.isDirectory()) {
        saifArchiveDirectory = file;
      } else {
        zipFile = new ZipFile(file);
      }
      if (log.isDebugEnabled()) {
        log.debug("  Finished opening archive");
      }
      loadSchema();
      loadExportedObjects();
      loadSrid();
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Open the iterator for the next object set.
   * 
   * @return True if an object set iterator was loaded.
   */
  private boolean openNextObjectSet() {
    try {
      closeCurrentReader();
      if (typePathIterator == null) {
        typePathIterator = typePaths.iterator();
      }
      if (typePathIterator.hasNext()) {
        do {
          final String typePath = typePathIterator.next();
          if (hasData(typePath)) {
            osnReader = getOsnReader(typePath, factory);
            osnReader.setFactory(factory);
            if (osnReader.hasNext()) {
              return true;
            }
          }
        } while (typePathIterator.hasNext());
      }
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    osnReader = null;
    return false;
  }

  protected DataObject readObject(
    final String className,
    final DataObjectFactory factory) throws IOException {
    final OsnReader reader = getOsnReader(className, factory);
    try {
      final DataObject object = reader.next();
      return object;
    } finally {
      reader.close();
    }
  }

  /**
   * Removing SAIF objects is not supported.
   * 
   * @throws UnsupportedOperationException
   */
  public void remove() {
    throw new UnsupportedOperationException(
      "Removing SAIF objects is not supported");
  }

  /**
   * Set the schema definition declared in the SAIF archive.
   * 
   * @param declaredSchema The schema definition.
   */
  public void setDeclaredMetaDataFactory(
    final DataObjectMetaDataFactory declaredMetaDataFactory) {
    this.declaredMetaDataFactory = declaredMetaDataFactory;
  }

  /**
   * @param excludeTypeNames the excludeTypeNames to set
   */
  public void setExcludeTypeNames(final Collection<String> excludeTypeNames) {
    this.excludeTypeNames.clear();
    for (final String typePath : excludeTypeNames) {
      this.excludeTypeNames.add(String.valueOf(typePath));
    }
  }

  /**
   * @param factory the factory to set
   */
  public void setFactory(final DataObjectFactory factory) {
    this.factory = factory;
  }

  public void setFile(final File file) {
    this.file = file;
  }

  /**
   * @param includeTypeNames the includeTypeNames to set
   */
  public void setIncludeTypeNames(final Collection<String> includeTypeNames) {
    this.includeTypeNames.clear();
    for (final String typePath : includeTypeNames) {
      this.includeTypeNames.add(String.valueOf(typePath));
    }
  }

  /**
   * Set the schema definition that will be set on each data object.
   * 
   * @param schema The schema definition.
   */
  public void setMetaDataFactory(final DataObjectMetaDataFactory metaDataFactory) {
    if (metaDataFactory != null) {
      this.metaDataFactory = metaDataFactory;
    } else {
      this.metaDataFactory = declaredMetaDataFactory;
    }
  }

  public void setSrid(final int srid) {
    this.srid = srid;
  }

  /**
   * @param typePathObjectSetMap the typePathObjectSetMap to set
   */
  public void setTypeNameFileNameMap(
    final Map<String, String> typePathObjectSetMap) {
    typePathFileNameMap.clear();
    fileNameTypeNameMap.clear();
    for (final Entry<String, String> entry : typePathObjectSetMap.entrySet()) {
      final String key = entry.getKey();
      final String value = entry.getValue();
      typePathFileNameMap.put(key, value);
      fileNameTypeNameMap.put(value, key);
    }
  }

  @Override
  public String toString() {
    return file.getAbsolutePath();
  }

  public OsnReader getOsnReader(String className) throws IOException {
    return getOsnReader(className, factory);
  }

}
