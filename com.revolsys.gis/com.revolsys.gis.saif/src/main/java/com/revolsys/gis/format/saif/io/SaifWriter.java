/*
 * $URL:https://secure.revolsys.com/svn/open.revolsys.com/GIS/trunk/src/main/java/com/revolsys/gis/format/saif/io/SaifWriter.java $
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
package com.revolsys.gis.format.saif.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataFactory;
import com.revolsys.gis.format.saif.io.util.ObjectSetOutputStream;
import com.revolsys.gis.format.saif.io.util.ObjectSetUtil;
import com.revolsys.gis.format.saif.io.util.OsnSerializer;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.ZipUtil;

/**
 * <p>
 * The SaifWriter.
 * </p>
 * 
 * @author Paul Austin
 * @see SaifReader
 */
public class SaifWriter extends AbstractWriter<DataObject> {
  private static final Logger log = Logger.getLogger(SaifWriter.class);

  private DataObjectMetaData annotatedSpatialDataSetType;

  private final Map<QName, String> compositeTypeNames = new HashMap<QName, String>();

  private DataObjectMetaDataFactory dataObjectMetaDataFactory;

  private final Set<String> exportedTypes = new LinkedHashSet<String>();

  private OsnSerializer exportsSerializer;

  private File file;

  private boolean indentEnabled = false;

  private boolean initialized;

  private int maxSubsetSize = Integer.MAX_VALUE;

  private final Map<QName, String> objectIdentifiers = new HashMap<QName, String>();

  private final Map<QName, String> objectSetNames = new HashMap<QName, String>();

  private List<String> schemaFileNames;

  private String schemaResource;

  private final Map<QName, OsnSerializer> serializers = new HashMap<QName, OsnSerializer>();

  private DataObjectMetaData spatialDataSetType;

  // TODO read from file
  private final int srid = 26910;

  private File tempDirectory;

  public SaifWriter() {
  }

  public SaifWriter(
    final File file)
    throws IOException {
    setFile(file);
  }

  public SaifWriter(
    final File file,
    final DataObjectMetaDataFactory dataObjectMetaDataFactory)
    throws IOException {
    this(file);
    setDataObjectMetaDataFactory(dataObjectMetaDataFactory);
  }

  public SaifWriter(
    final String fileName)
    throws IOException {
    this(new File(fileName));
  }

  public void addCompositeTypeName(
    final String typeName,
    final String compositeTypeName) {
    compositeTypeNames.put(QName.valueOf(typeName), compositeTypeName);
  }

  public void flush() {
    // TODO Auto-generated method stub

  }

  protected void addExport(
    final QName typeName,
    final QName compositeType,
    final String objectSubset) {
    try {
      final String referenceId = getObjectIdentifier(typeName);
      if (!exportedTypes.contains(referenceId)) {
        String compositeTypeName = compositeType.getLocalPart();
        final String compositeNamespace = compositeType.getNamespaceURI();
        if (compositeNamespace != "") {
          compositeTypeName += "::" + compositeNamespace;
        }
        exportsSerializer.startObject("ExportedObjectHandle");
        exportsSerializer.attribute("referenceID", referenceId, true);
        exportsSerializer.attribute("type", compositeTypeName, true);
        exportsSerializer.attribute("objectSubset", objectSubset, true);
        exportsSerializer.attribute("offset", new BigDecimal("0"), true);
        exportsSerializer.attribute("sharable", Boolean.FALSE, true);
        exportsSerializer.endObject();
        exportedTypes.add(referenceId);
      }
    } catch (final IOException e) {
      log.error(e.getMessage(), e);
    }
  }

  public void close() {
    if (tempDirectory != null) {
      try {
        if (log.isInfoEnabled()) {
          log.info("Closing SAIF archive '" + file.getCanonicalPath() + "'");
        }
        createMissingDirObject("InternallyReferencedObjects", "internal.dir");
        createMissingDirObject("ImportedObjects", "imports.dir");
        createMissingGlobalMetadata();
        if (log.isInfoEnabled()) {
          log.info("  Closing serializers");
        }
        for (final OsnSerializer serializer : serializers.values()) {
          try {
            serializer.close();
          } catch (final Throwable e) {
            log.error(e.getMessage(), e);
          }
        }
        if (log.isDebugEnabled()) {
          log.debug("  Compressing SAIF archive");
        }
        if (!file.isDirectory()) {
          ZipUtil.zipDirectory(file, tempDirectory);
        }
      } catch (final RuntimeException e) {
        file.delete();
        throw e;
      } catch (final Error e) {
        file.delete();
        throw e;
      } catch (final IOException e) {
        log.error("  Unable to compress SAIF archive: " + e.getMessage(), e);
        e.printStackTrace();
      } finally {
        if (log.isDebugEnabled()) {
          log.debug("  Deleting temporary files");
        }
        if (!file.isDirectory()) {
          FileUtil.deleteDirectory(tempDirectory);
          if (log.isDebugEnabled()) {
            log.debug("  Finished closing file");
          }
        }
      }
    }
  }

  private void createExportsSerializer()
    throws IOException {
    final File exportsFile = new File(tempDirectory, "exports.dir");
    exportsSerializer = createSerializer(new QName("ExportedObject"),
      new FileOutputStream(exportsFile));
    exportsSerializer.startObject("ExportedObjects");
    exportsSerializer.attributeName("handles");
    exportsSerializer.startCollection("Set");
    serializers.put(new QName("ExportedObject"), exportsSerializer);
  }

  private void createMissingDirObject(
    final String typeName,
    final String fileName)
    throws IOException {
    if (!serializers.containsKey(typeName)) {
      final File file = new File(tempDirectory, fileName);
      final PrintStream out = new PrintStream(new FileOutputStream(file));
      try {
        out.print(typeName);
      } finally {
        out.close();
      }
    }
  }

  private void createMissingGlobalMetadata() {
    if (!exportedTypes.contains("GlobalMetadata")) {
      try {
        addExport(new QName("GlobalMetadata"), new QName("GlobalMetadata"),
          "globmeta.osn");
        final File metaFile = new File(tempDirectory, "globmeta.osn");
        final OsnSerializer serializer = createSerializer(new QName(
          "GlobalMetadata"), new FileOutputStream(metaFile));
        serializer.startObject("GlobalMetadata");
        serializer.attribute("objectIdentifier", "GlobalMetadata", true);

        serializer.attributeName("creationTime");
        serializer.startObject("TimeStamp");
        final Date creationTimestamp = new Date(System.currentTimeMillis());
        serializer.attribute("year", new BigDecimal(
          creationTimestamp.getYear() + 1900), true);
        serializer.attribute("month", new BigDecimal(
          creationTimestamp.getMonth() + 1), true);
        serializer.attribute("day",
          new BigDecimal(creationTimestamp.getDate()), true);
        serializer.attribute("hour", new BigDecimal(
          creationTimestamp.getHours()), true);
        serializer.attribute("minute", new BigDecimal(
          creationTimestamp.getMinutes()), true);
        serializer.attribute("second", new BigDecimal(
          creationTimestamp.getSeconds()), true);
        serializer.endObject();

        serializer.attributeName("saifProfile");
        serializer.startObject("Profile");
        serializer.attribute("authority", "Government of British Columbia",
          true);
        serializer.attribute("idName", "SAIFLite", true);
        serializer.attribute("version", "Release 1.1", true);
        serializer.endObject();

        serializer.attribute("saifRelease", "SAIF 3.2", true);
        serializer.attribute("toolkitVersion",
          "SAIF Toolkit Version 1.4.0 (May 05, 1997)", true);

        serializer.attributeName("userProfile");
        serializer.startObject("UserProfile");

        serializer.attributeName("coordDefs");
        serializer.startObject("LocationalDefinitions");
        serializer.attributeEnum("c1", "real32", true);
        serializer.attributeEnum("c2", "real32", true);
        serializer.attributeEnum("c3", "real32", true);
        serializer.endObject();

        serializer.attribute("organization", new BigDecimal("4"), true);
        serializer.endObject();

        serializer.endObject();
        serializer.close();
      } catch (final IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
  }

  protected OsnSerializer createSerializer(
    final QName typeName,
    final OutputStream out) {
    final OsnSerializer serializer = new OsnSerializer(typeName, out, srid);
    serializer.setIndentEnabled(indentEnabled);
    return serializer;
  }

  private DataObjectMetaData getCompositeType(
    final QName typeName) {
    String compositeTypeName = compositeTypeNames.get(typeName);
    if (compositeTypeName == null) {
      compositeTypeName = typeName + "Composite";
    }
    final DataObjectMetaData compisteType = dataObjectMetaDataFactory.getMetaData(QName.valueOf(compositeTypeName));
    return compisteType;
  }

  public File getFile() {
    return file;
  }

  public int getMaxSubsetSize() {
    return maxSubsetSize;
  }

  public String getObjectIdentifier(
    final QName typeName) {
    String objectIdentifier = objectIdentifiers.get(typeName);
    if (objectIdentifier == null) {
      objectIdentifier = typeName.getLocalPart();
      objectIdentifiers.put(typeName, objectIdentifier);
    }
    return objectIdentifier;
  }

  /**
   * @return the objectIdentifiers
   */
  public Map<QName, String> getObjectIdentifiers() {
    return objectIdentifiers;
  }

  /**
   * Get the object set name (file name) within a SAIF archive file name for the
   * specified type name. The null value will be returned if a object set name
   * has not been set for that type name.
   * 
   * @param typeName The type name.
   * @return The object set name for the type name.
   */
  public String getObjectSetName(
    final QName typeName) {
    return objectSetNames.get(typeName);
  }

  public Map<QName, String> getObjectSetNames() {
    return objectSetNames;
  }

  private String getObjectSubsetName(
    final QName typeName) {
    String objectSubsetName = getObjectSetName(typeName);
    if (objectSubsetName == null) {
      objectSubsetName = typeName.getLocalPart();
      if (objectSubsetName.length() > 6) {
        objectSubsetName = objectSubsetName.substring(0, 6);
      }
      objectSubsetName += "00.osn";
      objectSetNames.put(typeName, objectSubsetName);
    }
    return objectSubsetName;
  }

  public String getSchemaResource() {
    return schemaResource;
  }

  private OsnSerializer getSerializer(
    final QName typeName)
    throws IOException {
    OsnSerializer serializer = serializers.get(typeName);
    if (serializer == null) {
      initialize();
      try {
        final DataObjectMetaData compositeType = getCompositeType(typeName);
        if (compositeType != null) {
          final String objectSubsetName = getObjectSubsetName(typeName);
          if (maxSubsetSize != Long.MAX_VALUE) {
            FileUtil.deleteFiles(tempDirectory,
              ObjectSetUtil.getObjectSubsetPrefix(objectSubsetName) + "...osn");
            serializer = createSerializer(typeName, new ObjectSetOutputStream(
              new File(tempDirectory, objectSubsetName), maxSubsetSize));
          } else {
            serializer = createSerializer(typeName, new FileOutputStream(
              new File(tempDirectory, objectSubsetName)));
          }
          if (compositeType.isInstanceOf(annotatedSpatialDataSetType)) {
            serializer.startObject(compositeType.getName());
            serializer.attributeName("objectIdentifier");
            final String objectIdentifier = getObjectIdentifier(typeName);
            serializer.attributeValue(objectIdentifier);
            serializer.endLine();
            serializer.serializeIndent();
            serializer.attributeName("annotationComponents");
            serializer.startCollection("Set");
          } else if (compositeType.isInstanceOf(spatialDataSetType)) {
            serializer.startObject(compositeType.getName());
            serializer.attributeName("objectIdentifier");
            final String objectIdentifier = getObjectIdentifier(typeName);
            serializer.attributeValue(objectIdentifier);
            serializer.endLine();
            serializer.serializeIndent();
            serializer.attributeName("geoComponents");
            serializer.startCollection("Set");
          }
          addExport(typeName, compositeType.getName(), objectSubsetName);
          serializers.put(typeName, serializer);
        } else if (typeName.equals("ImportedObjects")) {
          serializer = createSerializer(new QName("ImportedObject"),
            new FileOutputStream(new File(tempDirectory, "imports.dir")));
          serializers.put(typeName, serializer);
        } else if (typeName.getLocalPart().endsWith(
          "InternallyReferencedObjects")) {
          serializer = createSerializer(
            new QName("InternallyReferencedObject"), new FileOutputStream(
              new File(tempDirectory, "internal.dir")));
          serializers.put(typeName, serializer);
        } else if (typeName.getLocalPart().endsWith("GlobalMetadata")) {
          serializer = createSerializer(new QName("GlobalMetadata"),
            new FileOutputStream(new File(tempDirectory, "globmeta.osn")));
          addExport(typeName, typeName, "globmeta.osn");
          serializers.put(typeName, serializer);
        }
      } catch (final IOException e) {
        log.error("Unable to create serializer: " + e.getMessage(), e);
      }
    }
    return serializer;
  }

  public String toString() {
    return file.getAbsolutePath();
  }

  public File getTempDirectory() {
    return tempDirectory;
  }

  private void initialize()
    throws IOException {
    if (!initialized) {
      initialized = true;
      if (schemaResource != null) {
        final InputStream in = getClass().getResourceAsStream(schemaResource);
        if (in != null) {
          FileUtil.copy(in, new File(tempDirectory, "clasdefs.csn"));
        }
      }
      if (schemaFileNames != null) {
        try {
          final OutputStream out = new FileOutputStream(new File(tempDirectory,
            "clasdefs.csn"));
          try {
            for (final String fileName : schemaFileNames) {
              final File file = new File(fileName);
              InputStream in;
              if (file.exists()) {
                in = new FileInputStream(file);
              } else {
                in = getClass().getResourceAsStream("/" + fileName);
                if (in == null) {
                  throw new IllegalArgumentException("Schema file '"
                    + file.getPath() + "' could not be found");
                }
              }
              final SaifSchemaReader reader = new SaifSchemaReader();
              setDataObjectMetaDataFactory(reader.loadSchemas(schemaFileNames));
              try {
                FileUtil.copy(in, out);
              } finally {
                in.close();
              }
            }
          } finally {
            out.close();
          }
        } catch (final IOException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      }
      createExportsSerializer();
    }
  }

  public boolean isIndentEnabled() {
    return indentEnabled;
  }

  public void setCompositeTypeNames(
    final Map<String, String> compositeTypeNames) {
    for (final Entry<String, String> entry : compositeTypeNames.entrySet()) {
      final String key = entry.getKey();
      final String value = entry.getValue();
      addCompositeTypeName(key, value);
    }
  }

  public void setDataObjectMetaDataFactory(
    final DataObjectMetaDataFactory schema) {
    this.dataObjectMetaDataFactory = schema;
    if (schema != null) {
      spatialDataSetType = schema.getMetaData(new QName("SpatialDataSet"));
      annotatedSpatialDataSetType = schema.getMetaData(new QName(
        "AnnotatedSpatialDataSet"));
    }
  }

  public void setFile(
    final File file)
    throws IOException {
    if (!file.isDirectory()) {
      final File parentDir = file.getParentFile();
      if (!parentDir.exists()) {
        parentDir.mkdirs();
      }
      String fileName = file.getName();
      String filePrefix = fileName;
      final int extensionIndex = fileName.lastIndexOf('.');
      if (extensionIndex != -1) {
        filePrefix = fileName.substring(0, extensionIndex);
        final String extension = fileName.substring(extensionIndex + 1);
        if (!extension.equals(".saf") && !extension.equals(".zip")) {
          fileName = filePrefix + ".saf";
        }
      } else {
        fileName = filePrefix + ".saf";
      }
      this.file = new File(file.getCanonicalFile().getParentFile(), fileName);
      if (log.isInfoEnabled()) {
        log.info("Creating SAIF archive '" + file.getAbsolutePath() + "'");
      }
      tempDirectory = FileUtil.createTempDirectory(filePrefix, ".saf");
      FileUtil.deleteFileOnExit(tempDirectory);
    } else {
      this.file = file;
      tempDirectory = file;
    }
    initialize();
  }

  public void setIndentEnabled(
    final boolean indentEnabled) {
    this.indentEnabled = indentEnabled;
  }

  public void setMaxSubsetSize(
    final int maxSubsetSize) {
    this.maxSubsetSize = maxSubsetSize;
  }

  /**
   * @param objectIdentifiers the objectIdentifiers to set
   */
  public void setObjectIdentifiers(
    final Map<String, String> objectIdentifiers) {
    for (final Entry<String, String> entry : objectIdentifiers.entrySet()) {
      final String key = entry.getKey();
      final String value = entry.getValue();
      final QName qName = QName.valueOf(key);
      this.objectIdentifiers.put(qName, value);
    }
  }

  /**
   * Set the full object set name (file name) within a SAIF archive file name
   * for the specified type name. The name must include the .osn (or other)
   * extension (e.g. globmeta.osn). If the file is to be split into multiple
   * object sub sets (for large files) include {$partNum} before the file
   * extension (e.g. roads{$segment}.osn) and the file names will include the
   * object sub set number. If a value is not set the file name will be the
   * first 6 characters of the type name, followed by a object subset number
   * starting at 00 with the .osn suffix (e.g. BreakLines would be
   * breakl00.osn).
   * 
   * @param typeName The type name
   * @param subSetName The sub set name for the type name.
   */
  public void setObjectSetName(
    final String typeName,
    final String subSetName) {
    objectSetNames.put(QName.valueOf(typeName), subSetName);
  }

  public void setObjectSetNames(
    final Map<String, String> objectSetNames) {
    for (final Entry<String, String> entry : objectSetNames.entrySet()) {
      final String key = entry.getKey();
      final String value = entry.getValue();
      setObjectSetName(key, value);
    }
  }

  public void setSchemaFileNames(
    final List<String> schemaFileNames) {
    this.schemaFileNames = schemaFileNames;

  }

  public void setSchemaResource(
    final String schemaResource)
    throws IOException {
    this.schemaResource = schemaResource;

  }

  /*
   * (non-Javadoc)
   * @see
   * com.revolsys.gis.format.saif.io.Writer<DataObject>#write(com.revolsys.gis
   * .model.data.DataObject)
   */
  public void write(
    final DataObject object) {
    try {
      final DataObjectMetaData type = object.getMetaData();
      final OsnSerializer serializer = getSerializer(type.getName());
      if (serializer != null) {
        serializer.serializeDataObject(object);
        if (indentEnabled) {
          serializer.endLine();
        }
      } else {
        log.error("No serializer for type '" + type.getName() + "'");
      }
    } catch (final IOException e) {
      log.error(e.getMessage(), e);
    }
  }

}
