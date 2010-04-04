package com.revolsys.jump.ui.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.namespace.QName;

import org.openjump.core.ccordsys.epsg.EpsgConstants;
import org.openjump.core.model.OpenJumpTaskProperties;
import org.openjump.core.ui.io.file.AbstractFileLayerLoader;
import org.openjump.core.ui.util.TaskUtil;
import org.openjump.util.UriUtil;

import com.revolsys.gis.cs.projection.GeometryOperation;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.jump.model.DataObjectFeature;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Task;

public abstract class AbstractDataObjectFileLoader extends
  AbstractFileLayerLoader {

  /** The workbench context. */
  private WorkbenchContext workbenchContext;

  private Map<Integer, GeometryOperation> transformations = new HashMap<Integer, GeometryOperation>();

  /**
   * Construct a new DataSourceFileLayerLoader.
   * 
   * @param workbenchContext The workbench context.
   * @param dataSourceClass The {@link DataSource} class.
   * @param description The file format name.
   * @param extensions The list of supported extensions.
   */
  public AbstractDataObjectFileLoader(final WorkbenchContext workbenchContext,
    final String description, final String... extensions) {
    super(description, Arrays.asList(extensions));
    this.workbenchContext = workbenchContext;
  }

  public boolean open(final TaskMonitor monitor, final URI uri,
    final Map<String, Object> options) {
    LayerManager layerManager = workbenchContext.getLayerManager();
    Task task = layerManager.getTask();
    QName srid = task.getProperty(OpenJumpTaskProperties.SRID);
    layerManager.setFiringEvents(false);

    try {
      InputStream in;
      if (uri.getScheme().equals("zip")) {
        File file = UriUtil.getZipFile(uri);
        String compressedFile = UriUtil.getZipEntryName(uri);
        ZipFile zipFile = new ZipFile(file);
        ZipEntry entry = zipFile.getEntry(compressedFile);
        if (entry == null) {
          // TODO file does not exist message
          return false;
        }
        in = zipFile.getInputStream(entry);
      } else {
        File file = new File(uri);
        in = new FileInputStream(file);
      }
      FeatureCollection features = readFeatureCollection(srid, in, options);
      if (features == null) {
        return false;
      }
      Layer layer = createLayer(layerManager,
        UriUtil.getFileNameWithoutExtension(uri), features);
      layerManager.setFiringEvents(true);
      Category category = TaskUtil.getSelectedCategoryName(workbenchContext);
      layerManager.addLayer(category.getName(), layer);
      return true;
    } catch (Exception e) {
      monitor.report(e);
      return false;
    } finally {
      layerManager.setFiringEvents(true);
    }
  }

  private Layer createLayer(final LayerManager layerManager, final String name,
    final FeatureCollection features) {
    Layer layer = new Layer(name, layerManager.generateLayerFillColor(),
      features, layerManager);
    layer.setEditable(true);
    return layer;
  }

  protected FeatureCollection readFeatureCollection(final QName srid,
    final InputStream in, final Map<String, Object> options) throws Exception {
    try {

      Reader<DataObject> reader = createReader(in, options);

      Iterator<DataObject> iterator = reader.iterator();
      if (iterator.hasNext()) {
        DataObjectFeature feature = (DataObjectFeature)iterator.next();
        FeatureSchema schema = feature.getSchema();
        FeatureCollection features = new FeatureDataset(schema);
        addFeature(srid, feature, features);
        while (iterator.hasNext()) {
          feature = (DataObjectFeature)iterator.next();
          addFeature(srid, feature, features);

        }
        return features;
      } else {
        return null;
      }
    } finally {
      in.close();
    }
  }

  private GeometryOperation getProjection(final int sourceSrid,
    final int targetSrid) {
    GeometryOperation transformation = transformations.get(sourceSrid);
    if (transformation == null) {
      transformation = GeometryProjectionUtil.getGeometryOperation(sourceSrid,
        targetSrid);
      transformations.put(sourceSrid, transformation);
    }
    return transformation;
  }

  private void addFeature(final QName srid, final DataObjectFeature feature,
    final FeatureCollection features) {
    Geometry geometry = feature.getGeometry();
    if (srid != null && geometry.getSRID() != EpsgConstants.getSrid(srid)) {
      GeometryOperation transform = getProjection(feature.getGeometry()
        .getSRID(), EpsgConstants.getSrid(srid));
      feature.setGeometry(transform.perform(geometry));
    }
    features.add(feature);
  }

  protected abstract Reader<DataObject> createReader(
    final InputStream in, final Map<String, Object> options);
}
