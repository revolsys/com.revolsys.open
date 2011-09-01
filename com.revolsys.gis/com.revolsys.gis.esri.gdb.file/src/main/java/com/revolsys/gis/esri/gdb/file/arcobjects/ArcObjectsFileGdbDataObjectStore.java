package com.revolsys.gis.esri.gdb.file.arcobjects;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.esri.arcgis.geodatabase.FeatureDataset;
import com.esri.arcgis.geodatabase.IDataset;
import com.esri.arcgis.geodatabase.IEnumFeatureClass;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.ITable;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.interop.AutomationException;
import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.AbstractDataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.io.IteratorReader;
import com.revolsys.gis.data.io.Query;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.esri.gdb.file.FileGdbDataObjectStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.EsriFileGdb;
import com.revolsys.gis.esri.gdb.xml.model.SpatialReference;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.parallel.process.InvokeMethodCallable;
import com.revolsys.parallel.process.InvokeStaticMethodCallable;
import com.revolsys.util.ExceptionUtil;
import com.vividsolutions.jts.geom.Geometry;

public class ArcObjectsFileGdbDataObjectStore extends AbstractDataObjectStore
  implements FileGdbDataObjectStore {

  private static ExecutorService executorService;

  private static final AtomicInteger INSTANCE_COUNT = new AtomicInteger();

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\?");

  public static SpatialReference getSpatialReference(
    final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      return null;
    } else {
      final String wkt = EsriFileGdb.getSpatialReferenceWkt(geometryFactory.getSRID());
      final SpatialReference spatialReference = SpatialReference.get(
        geometryFactory, wkt);
      return spatialReference;
    }
  }

  public static <T> T invoke(final Class<?> clazz, final String methodName,
    final Object... parameters) {
    final Callable<T> callable = new InvokeStaticMethodCallable<T>(clazz,
      methodName, parameters);
    try {
      final Future<T> future = executorService.submit(callable);
      return future.get();
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Error e) {
      throw e;
    } catch (final ExecutionException e) {
      ExceptionUtil.throwCauseException(e);
      return null;
    } catch (final Exception e) {
      throw new RuntimeException("Unable to invoke method " + callable, e);
    }
  }

  public static <T> T invoke(final Object object, final String methodName,
    final Object... parameters) {
    final Callable<T> callable = new InvokeMethodCallable<T>(object,
      methodName, parameters);
    try {
      final Future<T> future = executorService.submit(callable);
      return future.get();
    } catch (final Exception e) {
      throw new RuntimeException("Unable to invoke method " + callable, e);
    }
  }

  public static void release(final Object object) {
    invoke(ArcObjectsUtil.class, "release", object);
  }

  private String defaultSchema;

  private String fileName;

  private boolean createMissingGeodatabase = false;

  private boolean createMissingTables;

  private Resource template;

  private Workspace workspace;

  private Writer<DataObject> writer;

  public ArcObjectsFileGdbDataObjectStore() {
  }

  public ArcObjectsFileGdbDataObjectStore(final File file) {
    this();
    this.fileName = file.getAbsolutePath();
  }

  public ArcObjectsFileGdbDataObjectStore(final String fileName) {
    this();
    this.fileName = fileName;
  }

  private void addFeatureDatasetSchemas() {
    final List<String> datasetNames = invoke(ArcObjectsUtil.class,
      "getFeatureDatasetNames", workspace);
    for (final String datasetName : datasetNames) {
      final DataObjectStoreSchema schema = new DataObjectStoreSchema(this,
        datasetName);
      addSchema(schema);
    }
  }

  @Override
  @PreDestroy
  public void close() {
    if (writer != null) {
      writer.close();
    }
    if (workspace != null) {
      invoke(ArcObjectsUtil.class, "close", workspace);
    }
    synchronized (INSTANCE_COUNT) {
      INSTANCE_COUNT.decrementAndGet();
      if (INSTANCE_COUNT.get() == 0 && executorService != null) {
        invoke(ArcObjectsUtil.class, "releaseLicence");
        executorService.shutdownNow();
        executorService = null;
      }
    }
  }

  // public FileGdbReader createReader() {
  // return new FileGdbReader(this);
  // }

  private DataObjectMetaData createTable(final DataObjectMetaData objectMetaData) {
    // TODO final GeometryFactory geometryFactory =
    // objectMetaData.getGeometryFactory();
    // final SpatialReference spatialReference =
    // getSpatialReference(geometryFactory);
    //
    // final DETable deTable = EsriXmlDataObjectMetaDataUtil.getDETable(
    // objectMetaData, spatialReference);
    // return createTable(deTable);
    return null;
  }

  public Writer<DataObject> createWriter() {
    return getWriter();
  }

  @Override
  public void delete(final DataObject object) {
    if (object.getState() == DataObjectState.Persisted
      || object.getState() == DataObjectState.Modified) {
      object.setState(DataObjectState.Deleted);
      final Writer<DataObject> writer = getWriter();
      writer.write(object);
    }
  }

  public void deleteGeodatabase() {
    close();
    if (new File(fileName).exists()) {
      invoke(ArcObjectsUtil.class, "delete", workspace);
    }
  }

  public String getDefaultSchema() {
    return defaultSchema;
  }

  public String getFileName() {
    return fileName;
  }

  protected ITable getIFeatureClass(final FeatureDataset featureDataset,
    final String tableName) throws IOException, AutomationException {
    final IEnumFeatureClass featureClasses = featureDataset.getClasses();
    for (IFeatureClass featureClass = featureClasses.next(); featureClass != null; featureClass = featureClasses.next()) {
      final String name = ((IDataset)featureClass).getName();
      if (name.equals(tableName)) {
        return (ITable)featureClass;
      }
    }
    return null;
  }

  @Override
  public DataObjectMetaData getMetaData(final DataObjectMetaData objectMetaData) {
    DataObjectMetaData metaData = super.getMetaData(objectMetaData);
    if (createMissingTables && metaData == null) {
      metaData = createTable(objectMetaData);
    }
    return metaData;
  }

  public Resource getTemplate() {
    return template;
  }

  Workspace getWorkspace() {
    return workspace;
  }

  public synchronized Writer<DataObject> getWriter() {

    if (writer == null) {
      writer = new FileGdbWriter(this);
    }
    return writer;
  }

  @PostConstruct
  public void initialize() {
    INSTANCE_COUNT.incrementAndGet();
    synchronized (INSTANCE_COUNT) {
      if (executorService == null) {
        executorService = Executors.newSingleThreadExecutor();
        invoke(ArcObjectsUtil.class, "initLicence");
      }
    }
    try {
      final File file = new File(fileName);
      if (file.exists() && new File(fileName, "gdb").exists()) {
        if (file.isDirectory()) {
          workspace = invoke(ArcObjectsUtil.class, "openWorkspace", fileName);
        } else {
          throw new IllegalArgumentException(
            "ESRI File Geodatabase must be a directory");
        }
      } else if (createMissingGeodatabase) {
        if (template == null) {
          workspace = invoke(ArcObjectsUtil.class, "createWorkspace", fileName);
        } else if (template.exists()) {
          if (template instanceof FileSystemResource) {
            final FileSystemResource fileResource = (FileSystemResource)template;
            final File templateFile = fileResource.getFile();
            if (templateFile.isDirectory()) {
              try {
                FileUtil.copy(templateFile, file);
              } catch (final IOException e) {
                throw new IllegalArgumentException(
                  "Unable to copy template ESRI geodatabase " + template, e);
              }
              workspace = invoke(ArcObjectsUtil.class, "openWorkspace",
                fileName);
            }
          }
          if (workspace == null) {
            workspace = invoke(ArcObjectsUtil.class, "createFromXmlTemplate",
              fileName, template);
          }
        } else {
          throw new IllegalArgumentException("Template does not exist "
            + template);
        }
      } else {
        throw new IllegalArgumentException("ESRI file geodatbase not found "
          + fileName);
      }
      final List<CodeTable> codeTables = invoke(ArcObjectsUtil.class,
        "getCodeTables", workspace);
      for (final CodeTable codeTable : codeTables) {
        addCodeTable(codeTable);

      }
    } catch (final RuntimeException e) {
      close();
      throw e;
    } catch (final Error e) {
      close();
      throw e;
    }
  }

  @Override
  public void insert(final DataObject object) {
    final Writer<DataObject> writer = getWriter();
    writer.write(object);
  }

  public boolean isCreateMissingGeodatabase() {
    return createMissingGeodatabase;
  }

  public boolean isCreateMissingTables() {
    return createMissingTables;
  }

  @Override
  public DataObject load(final QName typeName, final Object id) {
    final DataObjectMetaData metaData = getMetaData(typeName);
    if (metaData == null) {
      throw new IllegalArgumentException("Unknown type " + typeName);
    } else {
      final FileGdbQueryIterator iterator = new FileGdbQueryIterator(this,
        typeName, metaData.getIdAttributeName() + " = " + id);
      try {
        if (iterator.hasNext()) {
          return iterator.next();
        } else {
          return null;
        }
      } finally {
        iterator.close();
      }
    }
  }

  @Override
  protected void loadSchemaDataObjectMetaData(
    final DataObjectStoreSchema schema,
    final Map<QName, DataObjectMetaData> metaDataMap) {
    final List<DataObjectMetaData> metaDataList = invoke(ArcObjectsUtil.class,
      "getSchemaDataObjectMetaData", workspace, schema, defaultSchema);
    for (final DataObjectMetaData metaData : metaDataList) {
      addMetaDataProperties((DataObjectMetaDataImpl)metaData);
    }
  }

  @Override
  protected void loadSchemas(final Map<String, DataObjectStoreSchema> schemaMap) {
    if (defaultSchema != null) {
      addSchema(new DataObjectStoreSchema(this, defaultSchema));
    } else {
      addSchema(new DataObjectStoreSchema(this, ""));
    }
    addFeatureDatasetSchemas();
  }

  public Reader<DataObject> query(final QName typeName) {
    final FileGdbQueryIterator iterator = new FileGdbQueryIterator(this,
      typeName);
    final IteratorReader<DataObject> reader = new IteratorReader<DataObject>(
      iterator);
    return reader;
  }

  public Reader<DataObject> query(final QName typeName,
    final BoundingBox boundingBox) {
    final Iterator<DataObject> iterator = new FileGdbFeatureClassQueryIterator(
      this, typeName, boundingBox);
    final IteratorReader<DataObject> reader = new IteratorReader<DataObject>(
      iterator);
    return reader;
  }

  public Reader<DataObject> query(final QName typeName, final Geometry geometry) {
    final BoundingBox boundingBox = new BoundingBox(geometry);
    return query(typeName, boundingBox);
  }

  @Override
  protected AbstractIterator<DataObject> createIterator(Query query,
    Map<String, Object> properties) {
    QName typeName = query.getTypeName();
    DataObjectMetaData metaData = query.getMetaData();
    if (metaData == null) {
      typeName = query.getTypeName();
      metaData = getMetaData(typeName);
      if (metaData == null) {
        throw new IllegalArgumentException("Type name does not exist "
          + typeName);
      }
    } else {
      typeName = metaData.getName();
    }
    final String where = query.getWhereClause();
    final List<Object> parameters = query.getParameters();
    final StringBuffer whereClause = new StringBuffer();
    if (parameters.isEmpty()) {
      if (where.indexOf('?') > -1) {
        throw new IllegalArgumentException(
          "No arguments specified for a where clause with placeholders: "
            + where);
      } else {
        whereClause.append(where);
      }
    } else {
      final Matcher matcher = PLACEHOLDER_PATTERN.matcher(where);
      int i = 0;
      while (matcher.find()) {
        if (i >= parameters.size()) {
          throw new IllegalArgumentException(
            "Not enough arguments for where clause with placeholders: " + where);
        }
        final Object argument = parameters.get(i);
        matcher.appendReplacement(whereClause, "");
        if (argument instanceof Number) {
          whereClause.append(argument);
        } else {
          whereClause.append("'");
          whereClause.append(argument);
          whereClause.append("'");
        }
        i++;
      }
      matcher.appendTail(whereClause);
    }

    final FileGdbQueryIterator iterator = new FileGdbQueryIterator(this,
      typeName, whereClause.toString());
    return iterator;
  }

  public void setCreateMissingGeodatabase(final boolean createMissingGeodatabase) {
    this.createMissingGeodatabase = createMissingGeodatabase;
  }

  public void setCreateMissingTables(final boolean createMissingTables) {
    this.createMissingTables = createMissingTables;
  }

  public void setDefaultSchema(final String defaultSchema) {
    this.defaultSchema = defaultSchema;
  }

  public void setFileName(final String fileName) {
    this.fileName = fileName;
  }

  public void setTemplate(final Resource template) {
    this.template = template;
  }

  @Override
  public void update(final DataObject object) {
    getWriter().write(object);
  }
}
