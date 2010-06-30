package com.revolsys.jump.gpx.io;

import org.openjump.core.ui.io.file.FileLayerLoader;

import com.revolsys.gis.gpx.io.GpxConstants;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.datasource.SaveFileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.registry.Registry;

public class GpxExtension extends Extension {

  public void configure(
    final PlugInContext context)
    throws Exception {
    WorkbenchContext workbenchContext = context.getWorkbenchContext();
    Registry registry = workbenchContext.getRegistry();
    addSaveChooser(workbenchContext);
    GpxFileLoader loader = new GpxFileLoader(workbenchContext);
    registry.createEntry(FileLayerLoader.KEY, loader);

  }

  public void addSaveChooser(
    final WorkbenchContext workbenchContext) {
    Blackboard blackboard = workbenchContext.getBlackboard();
    DataSourceQueryChooserManager manager = DataSourceQueryChooserManager.get(blackboard);
    String[] extensions = new String[] {
      GpxConstants.FILE_EXTENSION
    };

    SaveFileDataSourceQueryChooser chooser = new SaveFileDataSourceQueryChooser(
      GpxReaderWriterDataSource.class, "GPS Exchange Format", extensions,
      workbenchContext);
    manager.addSaveDataSourceQueryChooser(chooser);
  }
}
