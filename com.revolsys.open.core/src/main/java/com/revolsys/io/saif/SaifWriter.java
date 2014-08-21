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
package com.revolsys.io.saif;

import java.io.File;
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
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionFactory;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Path;
import com.revolsys.io.ZipUtil;
import com.revolsys.io.saif.util.ObjectSetUtil;
import com.revolsys.io.saif.util.OsnConverterRegistry;
import com.revolsys.io.saif.util.OsnSerializer;
import com.revolsys.util.Property;

/**
 * <p>
 * The SaifWriter.
 * </p>
 *
 * @author Paul Austin
 * @see SaifReader
 */
public class SaifWriter extends AbstractRecordWriter {
  private static final String GLOBAL_METADATA = "/GlobalMetadata";

  private static final Logger log = Logger.getLogger(SaifWriter.class);

  private RecordDefinition annotatedSpatialDataSetType;

  private final Map<String, String> compositeTypeNames = new HashMap<String, String>();

  protected OsnConverterRegistry converters = new OsnConverterRegistry();

  private RecordDefinitionFactory recordDefinitionFactory;

  private final Set<String> exportedTypes = new LinkedHashSet<String>();

  private final Map<String, Map<String, Object>> exports = new TreeMap<String, Map<String, Object>>();

  private File file;

  private boolean indentEnabled = false;

  private boolean initialized;

  private int maxSubsetSize = Integer.MAX_VALUE;

  private final Map<String, String> objectIdentifiers = new HashMap<String, String>();

  private final Map<String, String> objectSetNames = new HashMap<String, String>();

  private List<Resource> schemaFileNames;

  private String schemaResource;

  private final Map<String, OsnSerializer> serializers = new HashMap<String, OsnSerializer>();

  private RecordDefinition spatialDataSetType;

  private File tempDirectory;

  public SaifWriter() {
  }

  public SaifWriter(final File file) throws IOException {
    setFile(file);
  }

  public SaifWriter(final File file,
    final RecordDefinitionFactory recordDefinitionFactory) throws IOException {
    this(file);
    setRecordDefinitionFactory(recordDefinitionFactory);
  }

  public SaifWriter(final String fileName) throws IOException {
    this(new File(fileName));
  }

  public void addCompositeTypeName(final String typePath,
    final String compositeTypeName) {
    this.compositeTypeNames.put(String.valueOf(typePath), compositeTypeName);
  }

  protected void addExport(final String typePath, final String compositeType,
    final String objectSubset) {
    if (!this.exports.containsKey(typePath)) {
      final Map<String, Object> export = new HashMap<String, Object>();
      this.exports.put(typePath, export);
      final String referenceId = getObjectIdentifier(typePath);
      export.put("referenceId", referenceId);
      export.put("compositeType", compositeType);
      export.put("objectSubset", objectSubset);
    }
  }

  @Override
  public synchronized void close() {
    if (this.tempDirectory != null) {
      try {
        if (log.isInfoEnabled()) {
          log.info("Closing SAIF archive '" + this.file.getCanonicalPath()
            + "'");
        }
        createExports();
        createMissingDirObject("InternallyReferencedObjects", "internal.dir");
        createMissingDirObject("ImportedObjects", "imports.dir");
        createMissingGlobalMetadata();
        if (log.isInfoEnabled()) {
          log.info("  Closing serializers");
        }
        for (final OsnSerializer serializer : this.serializers.values()) {
          try {
            serializer.close();
          } catch (final Throwable e) {
            log.error(e.getMessage(), e);
          }
        }
        if (log.isDebugEnabled()) {
          log.debug("  Compressing SAIF archive");
        }
        if (!this.file.isDirectory()) {
          ZipUtil.zipDirectory(this.file, this.tempDirectory);
        }
      } catch (final RuntimeException e) {
        this.file.delete();
        throw e;
      } catch (final Error e) {
        this.file.delete();
        throw e;
      } catch (final IOException e) {
        log.error("  Unable to compress SAIF archive: " + e.getMessage(), e);
        e.printStackTrace();
      } finally {
        if (log.isDebugEnabled()) {
          log.debug("  Deleting temporary files");
        }
        if (!this.file.isDirectory()) {
          FileUtil.deleteDirectory(this.tempDirectory);
          if (log.isDebugEnabled()) {
            log.debug("  Finished closing file");
          }
        }
        this.tempDirectory = null;
      }
    }
  }

  private void createExports() throws IOException {
    final File exportsFile = new File(this.tempDirectory, "exports.dir");
    final OsnSerializer exportsSerializer = createSerializer("/ExportedObject",
      exportsFile, Long.MAX_VALUE);
    exportsSerializer.startObject("/ExportedObjects");
    exportsSerializer.attributeName("handles");
    exportsSerializer.startCollection("Set");
    writeExport(exportsSerializer, "GlobalMetadata", "GlobalMetadata",
        "globmeta.osn");
    for (final Map<String, Object> export : this.exports.values()) {
      final String compositeType = (String)export.get("compositeType");
      final String referenceId = (String)export.get("referenceId");
      final String objectSubset = (String)export.get("objectSubset");
      String compositeTypeName = Path.getName(compositeType);
      final String compositeNamespace = Path.getPath(compositeType)
          .replaceAll("/", "");
      if (Property.hasValue(compositeNamespace)) {
        compositeTypeName += "::" + compositeNamespace;
      }
      writeExport(exportsSerializer, referenceId, compositeTypeName,
        objectSubset);
    }
    exportsSerializer.close();
  }

  private void createMissingDirObject(final String typePath,
    final String fileName) throws IOException {
    if (!this.serializers.containsKey(typePath)) {
      final File file = new File(this.tempDirectory, fileName);
      final PrintStream out = new PrintStream(new FileOutputStream(file));
      try {
        out.print(typePath);
        out.print("(handles:Set{})");
      } finally {
        out.close();
      }
    }
  }

  private void createMissingGlobalMetadata() {
    if (!this.exports.containsKey(GLOBAL_METADATA)) {
      try {
        addExport(GLOBAL_METADATA, GLOBAL_METADATA, "globmeta.osn");
        final File metaFile = new File(this.tempDirectory, "globmeta.osn");
        final OsnSerializer serializer = createSerializer(GLOBAL_METADATA,
          metaFile, Long.MAX_VALUE);
        serializer.startObject("/GlobalMetadata");
        serializer.attribute("objectIdentifier", "GlobalMetadata", true);

        serializer.attributeName("creationTime");
        serializer.startObject("/TimeStamp");
        final Date creationTimestamp = new Date(System.currentTimeMillis());
        serializer.attribute("year", new BigDecimal(
          creationTimestamp.getYear() + 1900), true);
        serializer.attribute("month",
          new BigDecimal(creationTimestamp.getMonth() + 1), true);
        serializer.attribute("day",
          new BigDecimal(creationTimestamp.getDate()), true);
        serializer.attribute("hour",
          new BigDecimal(creationTimestamp.getHours()), true);
        serializer.attribute("minute",
          new BigDecimal(creationTimestamp.getMinutes()), true);
        serializer.attribute("second",
          new BigDecimal(creationTimestamp.getSeconds()), true);
        serializer.endObject();

        serializer.attributeName("saifProfile");
        serializer.startObject("/Profile");
        serializer.attribute("authority", "Government of British Columbia",
          true);
        serializer.attribute("idName", "SAIFLite", true);
        serializer.attribute("version", "Release 1.1", true);
        serializer.endObject();

        serializer.attribute("saifRelease", "SAIF 3.2", true);
        serializer.attribute("toolkitVersion",
          "SAIF Toolkit Version 1.4.0 (May 05, 1997)", true);

        serializer.attributeName("userProfile");
        serializer.startObject("/UserProfile");

        serializer.attributeName("coordDefs");
        serializer.startObject("/LocationalDefinitions");
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

  protected OsnSerializer createSerializer(final String typePath,
    final File file, final long maxSize) throws IOException {
    final OsnSerializer serializer = new OsnSerializer(typePath, file, maxSize,
      this.converters);
    serializer.setIndentEnabled(this.indentEnabled);
    return serializer;
  }

  @Override
  public void flush() {
  }

  private RecordDefinition getCompositeType(final String typePath) {
    String compositeTypeName = this.compositeTypeNames.get(typePath);
    if (compositeTypeName == null) {
      compositeTypeName = typePath + "Composite";
    }
    final RecordDefinition compisteType = this.recordDefinitionFactory.getRecordDefinition(String.valueOf(compositeTypeName));
    return compisteType;
  }

  public File getFile() {
    return this.file;
  }

  public int getMaxSubsetSize() {
    return this.maxSubsetSize;
  }

  public String getObjectIdentifier(final String typePath) {
    String objectIdentifier = this.objectIdentifiers.get(typePath);
    if (objectIdentifier == null) {
      objectIdentifier = Path.getName(typePath);
      this.objectIdentifiers.put(typePath, objectIdentifier);
    }
    return objectIdentifier;
  }

  /**
   * @return the objectIdentifiers
   */
  public Map<String, String> getObjectIdentifiers() {
    return this.objectIdentifiers;
  }

  /**
   * Get the object set name (file name) within a SAIF archive file name for the
   * specified type name. The null value will be returned if a object set name
   * has not been set for that type name.
   *
   * @param typePath The type name.
   * @return The object set name for the type name.
   */
  public String getObjectSetName(final String typePath) {
    return this.objectSetNames.get(typePath);
  }

  public Map<String, String> getObjectSetNames() {
    return this.objectSetNames;
  }

  private String getObjectSubsetName(final String typePath) {
    String objectSubsetName = getObjectSetName(typePath);
    if (objectSubsetName == null) {
      objectSubsetName = Path.getName(typePath);
      if (objectSubsetName.length() > 6) {
        objectSubsetName = objectSubsetName.substring(0, 6);
      }
      objectSubsetName += "00.osn";
      this.objectSetNames.put(typePath, objectSubsetName);
    }
    return objectSubsetName;
  }

  public String getSchemaResource() {
    return this.schemaResource;
  }

  private OsnSerializer getSerializer(final String typePath) throws IOException {
    OsnSerializer serializer = this.serializers.get(typePath);
    if (serializer == null) {
      initialize();
      try {
        final RecordDefinition compositeType = getCompositeType(typePath);
        if (compositeType != null) {
          final String objectSubsetName = getObjectSubsetName(typePath);
          if (this.maxSubsetSize != Long.MAX_VALUE) {
            FileUtil.deleteFiles(this.tempDirectory,
              ObjectSetUtil.getObjectSubsetPrefix(objectSubsetName) + "...osn");
            serializer = createSerializer(typePath, new File(
              this.tempDirectory, objectSubsetName), this.maxSubsetSize);
          } else {
            serializer = createSerializer(typePath, new File(
              this.tempDirectory, objectSubsetName), Long.MAX_VALUE);
          }
          if (compositeType.isInstanceOf(this.annotatedSpatialDataSetType)) {
            serializer.startObject(compositeType.getPath());
            serializer.attributeName("objectIdentifier");
            final String objectIdentifier = getObjectIdentifier(typePath);
            serializer.attributeValue(objectIdentifier);
            serializer.endLine();
            serializer.serializeIndent();
            serializer.attributeName("annotationComponents");
            serializer.startCollection("Set");
          } else if (compositeType.isInstanceOf(this.spatialDataSetType)) {
            serializer.startObject(compositeType.getPath());
            serializer.attributeName("objectIdentifier");
            final String objectIdentifier = getObjectIdentifier(typePath);
            serializer.attributeValue(objectIdentifier);
            serializer.endLine();
            serializer.serializeIndent();
            serializer.attributeName("geoComponents");
            serializer.startCollection("Set");
          }
          addExport(typePath, compositeType.getPath(), objectSubsetName);
          this.serializers.put(typePath, serializer);
        } else if (typePath.equals("/ImportedObjects")) {
          serializer = createSerializer("/ImportedObject", new File(
            this.tempDirectory, "imports.dir"), Long.MAX_VALUE);
          this.serializers.put(typePath, serializer);
        } else if (Path.getName(typePath).endsWith(
            "InternallyReferencedObjects")) {
          serializer = createSerializer("/InternallyReferencedObject",
            new File(this.tempDirectory, "internal.dir"), Long.MAX_VALUE);
          this.serializers.put(typePath, serializer);
        } else if (Path.getName(typePath).endsWith("GlobalMetadata")) {
          serializer = createSerializer(GLOBAL_METADATA, new File(
            this.tempDirectory, "globmeta.osn"), Long.MAX_VALUE);
          addExport(typePath, typePath, "globmeta.osn");
          this.serializers.put(typePath, serializer);
        }
      } catch (final IOException e) {
        log.error("Unable to create serializer: " + e.getMessage(), e);
      }
    }
    return serializer;
  }

  public File getTempDirectory() {
    return this.tempDirectory;
  }

  private void initialize() throws IOException {
    if (!this.initialized) {
      this.initialized = true;
      if (this.schemaResource != null) {
        final InputStream in = getClass().getResourceAsStream(
          this.schemaResource);
        if (in != null) {
          FileUtil.copy(in, new File(this.tempDirectory, "clasdefs.csn"));
        }
      }
      if (this.schemaFileNames != null) {
        try {
          final OutputStream out = new FileOutputStream(new File(
            this.tempDirectory, "clasdefs.csn"));
          try {
            for (final Resource resource : this.schemaFileNames) {
              final InputStream in = resource.getInputStream();
              final SaifSchemaReader reader = new SaifSchemaReader();
              setRecordDefinitionFactory(reader.loadSchemas(this.schemaFileNames));
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
    }
  }

  public boolean isIndentEnabled() {
    return this.indentEnabled;
  }

  public void setCompositeTypeNames(final Map<String, String> compositeTypeNames) {
    for (final Entry<String, String> entry : compositeTypeNames.entrySet()) {
      final String key = entry.getKey();
      final String value = entry.getValue();
      addCompositeTypeName(key, value);
    }
  }

  public void setFile(final File file) throws IOException {
    if (!file.isDirectory()) {
      final File parentDir = file.getParentFile();
      if (!parentDir.exists()) {
        parentDir.mkdirs();
      }
      String fileName = FileUtil.getFileName(file);
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
      this.tempDirectory = FileUtil.createTempDirectory(filePrefix, ".saf");
      FileUtil.deleteFileOnExit(this.tempDirectory);
    } else {
      this.file = file;
      this.tempDirectory = file;
    }
    initialize();
  }

  public void setIndentEnabled(final boolean indentEnabled) {
    this.indentEnabled = indentEnabled;
  }

  public void setMaxSubsetSize(final int maxSubsetSize) {
    this.maxSubsetSize = maxSubsetSize;
  }

  /**
   * @param objectIdentifiers the objectIdentifiers to set
   */
  public void setObjectIdentifiers(final Map<String, String> objectIdentifiers) {
    for (final Entry<String, String> entry : objectIdentifiers.entrySet()) {
      final String key = entry.getKey();
      final String value = entry.getValue();
      final String qName = String.valueOf(key);
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
   * @param typePath The type name
   * @param subSetName The sub set name for the type name.
   */
  public void setObjectSetName(final String typePath, final String subSetName) {
    this.objectSetNames.put(typePath, subSetName);
  }

  public void setObjectSetNames(final Map<String, String> objectSetNames) {
    for (final Entry<String, String> entry : objectSetNames.entrySet()) {
      final String key = entry.getKey();
      final String value = entry.getValue();
      setObjectSetName(key, value);
    }
  }

  public void setRecordDefinitionFactory(final RecordDefinitionFactory schema) {
    this.recordDefinitionFactory = schema;
    if (schema != null) {
      this.spatialDataSetType = schema.getRecordDefinition("/SpatialDataSet");
      this.annotatedSpatialDataSetType = schema.getRecordDefinition("/AnnotatedSpatialDataSet");
    }
  }

  public void setSchemaFileNames(final List<Resource> schemaFileNames) {
    this.schemaFileNames = schemaFileNames;

  }

  public void setSchemaResource(final String schemaResource) throws IOException {
    this.schemaResource = schemaResource;

  }

  @Override
  public String toString() {
    return this.file.getAbsolutePath();
  }

  @Override
  public void write(final Record object) {
    try {
      final RecordDefinition type = object.getRecordDefinition();
      final OsnSerializer serializer = getSerializer(type.getPath());
      if (serializer != null) {
        serializer.serializeRecord(object);
        if (this.indentEnabled) {
          serializer.endLine();
        }
      } else {
        log.error("No serializer for type '" + type.getPath() + "'");
      }
    } catch (final IOException e) {
      log.error(e.getMessage(), e);
    }
  }

  public void writeExport(final OsnSerializer exportsSerializer,
    final String referenceId, final String compositeTypeName,
    final String objectSubset) throws IOException {
    exportsSerializer.startObject("ExportedObjectHandle");
    exportsSerializer.attribute("referenceID", referenceId, true);
    exportsSerializer.attribute("type", compositeTypeName, true);
    exportsSerializer.attribute("objectSubset", objectSubset, true);
    exportsSerializer.attribute("offset", new BigDecimal("0"), true);
    exportsSerializer.attribute("sharable", Boolean.FALSE, true);
    exportsSerializer.endObject();
  }

}
