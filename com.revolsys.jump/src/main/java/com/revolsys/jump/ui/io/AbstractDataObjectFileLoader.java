package com.revolsys.jump.ui.io;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openjump.core.model.OpenJumpTaskProperties;
import org.openjump.core.ui.io.file.AbstractFileLayerLoader;
import org.openjump.core.ui.util.TaskUtil;
import org.openjump.util.UriUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import com.revolsys.gis.cs.GeometryFactory;
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
  public AbstractDataObjectFileLoader(
    final WorkbenchContext workbenchContext,
    final String description,
    final String... extensions) {
    super(description, Arrays.asList(extensions));
    this.workbenchContext = workbenchContext;
  }

  public boolean open(
    final TaskMonitor monitor,
    final URI uri,
    final Map<String, Object> options) {
    LayerManager layerManager = workbenchContext.getLayerManager();
    Task task = layerManager.getTask();
    GeometryFactory geometryFactory = task.getProperty(OpenJumpTaskProperties.GEOMETRY_FACTORY);
    layerManager.setFiringEvents(false);

    try {
      FeatureCollection features = readFeatureCollection(geometryFactory,
        new UrlResource(uri), options);
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

  private Layer createLayer(
    final LayerManager layerManager,
    final String name,
    final FeatureCollection features) {
    Layer layer = new Layer(name, layerManager.generateLayerFillColor(),
      features, layerManager);
    layer.setEditable(true);
    return layer;
  }

  protected FeatureCollection readFeatureCollection(
    GeometryFactory geometryFactory,
    final Resource resource,
    final Map<String, Object> options)
    throws Exception {

    Reader<DataObject> reader = createReader(resource, options);
    try {

      Iterator<DataObject> iterator = reader.iterator();
      if (iterator.hasNext()) {
        DataObjectFeature feature = (DataObjectFeature)iterator.next();
        FeatureSchema schema = feature.getSchema();
        FeatureCollection features = new FeatureDataset(schema);
        geometryFactory = addFeature(geometryFactory, feature, features);
        while (iterator.hasNext()) {
          feature = (DataObjectFeature)iterator.next();
          geometryFactory = addFeature(geometryFactory, feature, features);

        }
        return features;
      } else {
        return null;
      }
    } finally {
      reader.close();
    }
  }

  private GeometryFactory addFeature(
    GeometryFactory geometryFactory,
    final DataObjectFeature feature,
    final FeatureCollection features) {
    Geometry geometry = feature.getGeometry();
    if (geometryFactory == null) {
      geometryFactory = GeometryFactory.getFactory(geometry);
      if (geometryFactory != null) {
        LayerManager layerManager = workbenchContext.getLayerManager();
        Task task = layerManager.getTask();
        task.setProperty(OpenJumpTaskProperties.GEOMETRY_FACTORY,
          geometryFactory);
      }
    }
    geometry =  GeometryProjectionUtil.perform(geometry, geometryFactory);
    feature.setGeometry(geometry);
    features.add(feature);
    return geometryFactory;
  }

  protected abstract Reader<DataObject> createReader(
    final Resource resource,
    final Map<String, Object> options);
}
