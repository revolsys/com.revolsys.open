package com.revolsys.jump.ecsv;

import java.util.Set;

import org.openjump.core.ui.io.file.FileLayerLoader;

import com.revolsys.gis.data.io.DataObjectReaderFactory;
import com.revolsys.gis.ecsv.io.EcsvConstants;
import com.revolsys.gis.ecsv.io.EcsvDataObjectReaderFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.jump.model.FeatureDataObjectFactory;
import com.revolsys.jump.ui.io.InputStreamReaderFileLoader;
import com.vividsolutions.jump.datastore.DataStoreDriver;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.datasource.SaveFileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.registry.Registry;

public class EcsvExtension extends Extension {
  public void configure(
    final PlugInContext context)
    throws Exception {
    WorkbenchContext workbenchContext = context.getWorkbenchContext();
    Registry registry = workbenchContext.getRegistry();

    addSaveChooser(workbenchContext);

    FeatureDataObjectFactory dataObjectFactory = new FeatureDataObjectFactory();

    final Set<DataObjectReaderFactory> factories = IoFactoryRegistry.INSTANCE.getFactories(DataObjectReaderFactory.class);
    for (DataObjectReaderFactory factory : factories) {
      final String name = factory.getName();
      final Set<String> fileExtensions = factory.getFileExtensions();
      InputStreamReaderFileLoader loader = new InputStreamReaderFileLoader(
        workbenchContext, factory, dataObjectFactory, name,
        fileExtensions.toArray(new String[0]));

      registry.createEntry(FileLayerLoader.KEY, loader);

    }

    EcsvDataStoreDriver driver = new EcsvDataStoreDriver();
    if (driver.isAvailable()) {
      registry.createEntry(DataStoreDriver.REGISTRY_CLASSIFICATION, driver);
    }
  }

  public void addSaveChooser(
    final WorkbenchContext workbenchContext) {
    Blackboard blackboard = workbenchContext.getBlackboard();
    DataSourceQueryChooserManager manager = DataSourceQueryChooserManager.get(blackboard);
    String[] extensions = new String[] {
      EcsvConstants.FILE_EXTENSION
    };

    SaveFileDataSourceQueryChooser chooser = new SaveFileDataSourceQueryChooser(
      EcsvReaderWriterDataSource.class, EcsvConstants.DESCRIPTION, extensions,
      workbenchContext);
    manager.addSaveDataSourceQueryChooser(chooser);
  }
}
