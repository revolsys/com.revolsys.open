package com.revolsys.jump.moep;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openjump.core.model.OpenJumpTaskProperties;
import org.openjump.core.ui.io.file.FileLayerLoader;
import org.openjump.core.ui.io.file.Option;
import org.openjump.core.ui.util.TaskUtil;
import org.openjump.util.UriUtil;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.moep.io.MoepBinaryReader;
import com.revolsys.gis.moep.io.MoepConstants;
import com.revolsys.jump.model.DataObjectFeature;
import com.revolsys.jump.model.DataObjectMetaDataFeatureSchema;
import com.revolsys.jump.model.FeatureDataObjectFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class MoepFileLayerLoader implements FileLayerLoader {

  private WorkbenchContext workbenchContext;

  public MoepFileLayerLoader(
    final PlugInContext context) {
    workbenchContext = context.getWorkbenchContext();
  }

  public String getDescription() {
    return "BC Government MOEP Binary";
  }

  public Collection<String> getFileExtensions() {
    return Collections.singleton("bin");
  }

  public List<Option> getOptionMetadata() {
    return Collections.emptyList();
  }

  public boolean open(
    final TaskMonitor monitor,
    final URI uri,
    final Map<String, Object> options) {
    try {

      GeometryFactory globalGeometryFactory = workbenchContext.getTask()
        .getProperty(OpenJumpTaskProperties.GEOMETRY_FACTORY);
      FeatureDataObjectFactory factory = new FeatureDataObjectFactory();
      Reader<DataObject> reader = new MoepBinaryReader(uri.toURL(), factory);
      String name = UriUtil.getFileNameWithoutExtension(uri);

      DataObjectMetaDataFeatureSchema featureSchema = factory.getFeatureSchema(
        MoepConstants.META_DATA, "geometry");
      final FeatureCollection features = new FeatureDataset(featureSchema);

      for (DataObject object : reader) {
        final Geometry geometry = object.getGeometryValue();
        Geometry newGeometry =  GeometryProjectionUtil.perform(geometry,
          globalGeometryFactory);
        object.setGeometryValue(newGeometry);
        DataObjectFeature feature = (DataObjectFeature)object;
        features.add(feature);
      }
      if (features.size() > 0) {
        LayerManager layerManager = workbenchContext.getLayerManager();
        Layer layer = new Layer(name, layerManager.generateLayerFillColor(),
          features, layerManager);
        Category category = TaskUtil.getSelectedCategoryName(workbenchContext);
        layerManager.addLayerable(category.getName(), layer);
        layer.setName(name);
        return true;
      } else {
        return false;
      }
    } catch (IOException e) {
      return false;
    }
  }
}
