package com.revolsys.jump.ui;

import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;

import net.infonode.util.Direction;

import com.revolsys.jump.ui.builder.DateTimeUiBuilder;
import com.revolsys.jump.ui.builder.DateUIBuilder;
import com.revolsys.jump.ui.builder.FeatureUiBuilder;
import com.revolsys.jump.ui.builder.UiBuilderRegistry;
import com.revolsys.jump.ui.model.GridLayer;
import com.revolsys.jump.ui.model.GridRendererFactory;
import com.revolsys.jump.ui.plugin.file.newmenu.grid.NewBcgsGridPlugin;
import com.revolsys.jump.ui.plugin.file.newmenu.grid.NewNtsGridPlugin;
import com.revolsys.jump.ui.plugin.layer.ViewAttributesPlugIn;
import com.revolsys.jump.ui.plugin.layer.ZoomToGridLayerSheet;
import com.revolsys.jump.ui.plugin.window.ShowViewPlugIn;
import com.revolsys.jump.ui.style.FilterThemedStylesPlugin;
import com.revolsys.jump.ui.style.decoration.CentroidStyle;
import com.revolsys.jump.ui.swing.FieldComponentFactoryRegistry;
import com.revolsys.jump.ui.swing.FileFieldComponentFactory;
import com.revolsys.jump.ui.swing.view.InfoDetailViewFactory;
import com.revolsys.jump.ui.swing.view.InfoGeometryViewFactory;
import com.revolsys.jump.ui.swing.view.InfoTableViewFactory;
import com.revolsys.jump.ui.swing.view.LayerPanelComponentFactory;
import com.revolsys.jump.ui.swing.view.MapViewComponentFactory;
import com.revolsys.jump.ui.task.DockingTaskFrameFactory;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import com.vividsolutions.jump.workbench.ui.renderer.RenderingManager;

public class RevolutionSystemsExtension extends Extension {

  private WorkbenchContext workbenchContext;

  @SuppressWarnings("unchecked")
  public void configure(final PlugInContext context) throws Exception {
    workbenchContext = context.getWorkbenchContext();
    FeatureInstaller featureInstaller = context.getFeatureInstaller();

    new VisibilityPlugin(true).initialize(context);
    new VisibilityPlugin(false).initialize(context);
    featureInstaller.addMenuSeparator(MenuNames.VIEW);
    new AllVisiblePlugin(true).initialize(context);
    new AllVisiblePlugin(false).initialize(context);

    workbenchContext.getWorkbench().getFrame().addChoosableStyleClass(
      CentroidStyle.class);

    /***************************************************************************
     * UI Builders
     **************************************************************************/
    UiBuilderRegistry htmlrendererRepository = UiBuilderRegistry.getInstance(workbenchContext);
    htmlrendererRepository.addBuilder(Feature.class, new FeatureUiBuilder());
    htmlrendererRepository.addBuilder(Date.class, new DateTimeUiBuilder());
    htmlrendererRepository.addBuilder(java.sql.Date.class, new DateUIBuilder());

    /***************************************************************************
     * Info Frame Panels
     **************************************************************************/
    // Style
    FilterThemedStylesPlugin changeStylesPlugIn = new FilterThemedStylesPlugin();
    changeStylesPlugIn.initialize(context);

    InfoTableViewFactory infoTableViewFactory = new InfoTableViewFactory(
      workbenchContext);
    MapViewComponentFactory mapComponentFactory = new MapViewComponentFactory(
      workbenchContext);
    LayerPanelComponentFactory layersComponentFactory = new LayerPanelComponentFactory(
      workbenchContext);

    WorkbenchFrame workbenchFrame = context.getWorkbenchFrame();
    workbenchFrame.setExtendedState(workbenchFrame.getExtendedState()
      | JFrame.MAXIMIZED_BOTH);

    workbenchFrame.setTaskFrameFactory(new DockingTaskFrameFactory(
      workbenchContext, layersComponentFactory, mapComponentFactory,
      infoTableViewFactory));

    /***************************************************************************
     * File Menu
     */
    new NewNtsGridPlugin().initialize(context);
    new NewBcgsGridPlugin().initialize(context);
    /***************************************************************************
     * Window Menu
     */
    new ShowViewPlugIn(new InfoDetailViewFactory(workbenchContext),
      Direction.RIGHT).initialize(context);
    new ShowViewPlugIn(new InfoGeometryViewFactory(workbenchContext),
      Direction.RIGHT).initialize(context);
    new ShowViewPlugIn(infoTableViewFactory, Direction.DOWN).initialize(context);
//    new ShowViewPlugIn(new NewInfoTableViewFactory(workbenchContext),
//      Direction.DOWN).initialize(context);
    new ShowViewPlugIn(mapComponentFactory, Direction.RIGHT).initialize(context);
    new ShowViewPlugIn(layersComponentFactory, Direction.LEFT).initialize(context);

    new ViewAttributesPlugIn().initialize(context);
    /** readers */

    FieldComponentFactoryRegistry.setFactory(workbenchContext, "FileString",
      new FileFieldComponentFactory(workbenchContext));

    // ConnectionManagerToolboxPlugIn.instance(workbenchContext.getBlackboard()).initialize(context);
    RenderingManager.setRendererFactory(GridLayer.class,
      new GridRendererFactory());
    JPopupMenu gridLayerPopupMenu = new JPopupMenu();
    workbenchFrame.getNodeClassToPopupMenuMap().put(GridLayer.class,
      gridLayerPopupMenu);
    new ZoomToGridLayerSheet().initialize(context);
  }

}
