package com.revolsys.jump.saif;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.xml.namespace.QName;

import org.openjump.core.ccordsys.epsg.EpsgConstants;
import org.openjump.core.model.OpenJumpTaskProperties;
import org.openjump.core.ui.io.file.FileLayerLoader;
import org.openjump.core.ui.io.file.Option;

import com.revolsys.gis.cs.projection.GeometryOperation;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.format.saif.io.OsnReader;
import com.revolsys.gis.format.saif.io.SaifReader;
import com.revolsys.jump.model.DataObjectFeature;
import com.revolsys.jump.model.DataObjectMetaDataFeatureSchema;
import com.revolsys.jump.model.FeatureDataObjectFactory;
import com.revolsys.jump.saif.style.AnnotatedPointStyle;
import com.revolsys.jump.saif.style.StyleFileFactory;
import com.revolsys.jump.ui.model.ThemedLayer;
import com.revolsys.jump.ui.style.FilterTheme;
import com.revolsys.jump.ui.style.FilterThemingStyle;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

/**
 * The SaifImportPlugin loads the layers from a SAIF (.saf) file, adding a
 * category with the name of the SAIF file with one layer for each feature type
 * that contains data.
 * 
 * @author Paul Austin
 */
public class SaifFileLoader implements FileLayerLoader {

  private PlugInContext context;

  private WorkbenchContext workbenchContext;

  private int numRead;

  public SaifFileLoader(
    final PlugInContext context) {
    this.context = context;
    workbenchContext = context.getWorkbenchContext();
  }

  public boolean open(
    final TaskMonitor monitor,
    final URI uri,
    final Map<String, Object> options) {
    Task primaryTask = context.getWorkbenchContext().getTask();
    QName taskSrid = primaryTask.getProperty(OpenJumpTaskProperties.SRID);
    try {
      File file = new File(uri);
      String categoryName = com.revolsys.io.FileUtil.getFileNamePrefix(file);
      monitor.report("Opening File " + file.getName());
      SaifReader saifReader = new SaifReader(file);
      FeatureDataObjectFactory factory = new FeatureDataObjectFactory();
      saifReader.setFactory(factory);
      saifReader.open();
      int saifSrid = saifReader.getSrid();
      GeometryOperation transform = null;
      if (taskSrid != null) {
        int taskSridNum = EpsgConstants.getSrid(taskSrid);
        if (saifSrid != 0 && taskSridNum != saifSrid) {
          transform = GeometryProjectionUtil.getGeometryOperation(saifSrid,
            taskSridNum);
        }
      } else {
        primaryTask.setProperty(OpenJumpTaskProperties.SRID,
          EpsgConstants.getSrid(saifSrid));
      }
      LayerManager layerManager = primaryTask.getLayerManager();
      layerManager.addCategory(categoryName);

      StyleFileFactory styleFactory = new StyleFileFactory(getClass().getResourceAsStream("/trimStyle.csv"));
      try {
        List<QName> typeNames = new ArrayList<QName>(saifReader.getTypeNames());
        Collections.reverse(typeNames);
        Iterator<QName> classNames = typeNames.iterator();
        while (!monitor.isCancelRequested() && classNames.hasNext()) {
          QName className = classNames.next();
          OsnReader reader = saifReader.getOsnReader(className, factory);
          reader.setFactory(factory);
          try {
            addLayer(monitor, transform, categoryName, layerManager, reader,
              context.getWorkbenchFrame(), styleFactory);
          } finally {
            reader.close();
          }
        }
      } catch (IOException e) {
        context.getErrorHandler().handleThrowable(e);
      } finally {
        saifReader.close();
      }

    } finally {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          try {
            LayerViewPanel layerViewPanel = workbenchContext.getLayerViewPanel();
            Viewport viewport = layerViewPanel.getViewport();
            viewport.zoomToFullExtent();
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      });
    }
    return true;
  }

  private void addLayer(
    final TaskMonitor monitor,
    final GeometryOperation geometryTransform,
    final String categoryName,
    final LayerManager layerManager,
    final OsnReader reader,
    final WorkbenchFrame workbenchFrame,
    final StyleFileFactory styleFactory) {

    if (reader.hasNext()) {
      DataObjectFeature feature = (DataObjectFeature)reader.next();
      DataObjectMetaData type = feature.getMetaData();
      String layerName = type.getName().getLocalPart();
      monitor.report("Loading " + layerName);
      int count = 0;

      DataObjectMetaDataFeatureSchema featureSchema = (DataObjectMetaDataFeatureSchema)feature.getSchema();
      final FeatureCollection features = new FeatureDataset(featureSchema);
      layerManager.setFiringEvents(false);
      try {
        Layer layer = new ThemedLayer(layerName,
          layerManager.generateLayerFillColor(), features, layerManager);
        layerManager.addLayerable(categoryName, layer);
        setLayerStyle(layer, layerName, featureSchema, styleFactory);

        addFeature(features, feature, geometryTransform);
        while (reader.hasNext() && !monitor.isCancelRequested()) {
          monitor.report("Loading " + layerName + " " + ++count);
          feature = (DataObjectFeature)reader.next();
          addFeature(features, feature, geometryTransform);
          numRead++;
        }
        layer.setName(layerName);
      } finally {
        layerManager.setFiringEvents(true);
      }
    }
  }

  private void addFeature(
    final FeatureCollection features,
    final DataObjectFeature feature,
    final GeometryOperation geometryTransform) {
    Geometry geometry = feature.getGeometry();
    geometry = GeometryProjectionUtil.perform(geometryTransform, geometry);
    feature.setGeometry(geometry);
    features.add(feature);
  }

  private void setLayerStyle(
    final Layer layer,
    final String layerName,
    final DataObjectMetaDataFeatureSchema featureSchema,
    final StyleFileFactory styleFactory) {
    DataObjectMetaData featureType = featureSchema.getMetaData();
    QName typeName = featureType.getName();

    if (typeName.equals(new QName("TRIM", "OtherText"))
      || typeName.equals(new QName("TRIM", "Toponymy"))) {
      layer.addStyle(new AnnotatedPointStyle());
    }
    BasicStyle defaultStyle = styleFactory.getDefaultStyle(layerName);

    if (defaultStyle != null) {
      layer.removeStyle(layer.getBasicStyle());
      layer.addStyle(defaultStyle);
    }
    List<FilterTheme> attributeStyles = styleFactory.getFilterThemes(layerName);
    if (attributeStyles != null && !attributeStyles.isEmpty()) {
      Style filterStyle = new FilterThemingStyle(attributeStyles, layer);
      layer.addStyle(filterStyle);
      layer.getBasicStyle().setEnabled(false);
    }
    layer.setVisible(styleFactory.getLayerVisible(layerName));
  }

  public static MultiEnableCheck createEnableCheck(
    final WorkbenchContext workbenchContext) {
    EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
    return new MultiEnableCheck().add(checkFactory.createWindowWithAssociatedTaskFrameMustBeActiveCheck());
  }

  public String getDescription() {
    return "Spatial Archive and Interchange Format";
  }

  public Collection<String> getFileExtensions() {
    return Collections.singletonList("saf");
  }

  public List<Option> getOptionMetadata() {
    return Collections.emptyList();
  }
}
