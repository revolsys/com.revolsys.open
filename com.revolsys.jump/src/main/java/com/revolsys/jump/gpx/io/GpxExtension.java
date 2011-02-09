package com.revolsys.jump.gpx.io;

import com.revolsys.gis.gpx.io.GpxConstants;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.datasource.SaveFileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class GpxExtension extends Extension {

  public void configure(final PlugInContext context) throws Exception {
    WorkbenchContext workbenchContext = context.getWorkbenchContext();
     addSaveChooser(workbenchContext);

  }

  public void addSaveChooser(final WorkbenchContext workbenchContext) {
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
