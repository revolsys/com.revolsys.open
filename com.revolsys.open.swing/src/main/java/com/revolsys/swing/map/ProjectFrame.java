package com.revolsys.swing.map;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.ResponseCache;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.tree.TreePath;

import org.springframework.core.io.FileSystemResource;

import bibliothek.gui.dock.common.CContentArea;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.location.TreeLocationRoot;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.gui.dock.common.theme.CEclipseTheme;
import bibliothek.gui.dock.common.theme.ThemeMap;
import bibliothek.gui.dock.dockable.ScreencaptureMovingImageFactory;

import com.revolsys.collection.PropertyChangeArrayList;
import com.revolsys.io.FileUtil;
import com.revolsys.io.connection.ConnectionRegistry;
import com.revolsys.io.connection.ConnectionRegistryManager;
import com.revolsys.io.datastore.DataObjectStoreConnectionManager;
import com.revolsys.io.datastore.DataObjectStoreConnectionRegistry;
import com.revolsys.io.file.FileSystemConnectionManager;
import com.revolsys.io.file.FolderConnectionManager;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.net.urlcache.FileResponseCache;
import com.revolsys.swing.DockingFramesUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.WindowManager;
import com.revolsys.swing.action.file.Exit;
import com.revolsys.swing.component.BaseFrame;
import com.revolsys.swing.log4j.Log4jTableModel;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.arcgisrest.ArcGisServerRestLayerFactory;
import com.revolsys.swing.map.layer.bing.BingLayerFactory;
import com.revolsys.swing.map.layer.dataobject.DataObjectFileLayer;
import com.revolsys.swing.map.layer.dataobject.DataObjectStoreLayerFactory;
import com.revolsys.swing.map.layer.geonames.GeoNamesBoundingBoxLayerWorker;
import com.revolsys.swing.map.layer.grid.GridLayer;
import com.revolsys.swing.map.layer.openstreetmap.OpenStreetMapLayerFactory;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageLayer;
import com.revolsys.swing.map.layer.wikipedia.WikipediaBoundingBoxLayerWorker;
import com.revolsys.swing.map.tree.ProjectTreeNodeModel;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.table.worker.SwingWorkerTableModel;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.swing.tree.ObjectTreePanel;
import com.revolsys.swing.tree.datastore.DataObjectStoreConnectionManagerModel;
import com.revolsys.swing.tree.file.FileSystemConnectionManagerModel;
import com.revolsys.swing.tree.file.FolderConnectionManagerModel;
import com.revolsys.swing.tree.model.node.ListObjectTreeNodeModel;

@SuppressWarnings("serial")
public class ProjectFrame extends BaseFrame {
  static {
    ResponseCache.setDefault(new FileResponseCache());

    // TODO move to a file config
    MapObjectFactoryRegistry.addFactory(LayerGroup.FACTORY);
    MapObjectFactoryRegistry.addFactory(new DataObjectStoreLayerFactory());
    MapObjectFactoryRegistry.addFactory(new ArcGisServerRestLayerFactory());
    MapObjectFactoryRegistry.addFactory(new BingLayerFactory());
    MapObjectFactoryRegistry.addFactory(new OpenStreetMapLayerFactory());
    MapObjectFactoryRegistry.addFactory(DataObjectFileLayer.FACTORY);
    MapObjectFactoryRegistry.addFactory(GridLayer.FACTORY);
    MapObjectFactoryRegistry.addFactory(WikipediaBoundingBoxLayerWorker.FACTORY);
    MapObjectFactoryRegistry.addFactory(GeoNamesBoundingBoxLayerWorker.FACTORY);
    MapObjectFactoryRegistry.addFactory(GeoReferencedImageLayer.FACTORY);
  }

  private ObjectTreePanel tocPanel;

  private Project project;

  private CControl dockControl = new CControl(this);

  private MapPanel mapPanel;

  public ProjectFrame(final String title) {
    this(title, new Project());
  }

  public ProjectFrame(final String title, final Project project) {
    super(title);
    this.project = project;
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    SwingUtil.setSizeAndMaximize(this, 100, 100);

    dockControl.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
    final CEclipseTheme theme = (CEclipseTheme)dockControl.getController()
      .getTheme();
    theme.intern().setMovingImageFactory(
      new ScreencaptureMovingImageFactory(new Dimension(2000, 2000)));

    final CContentArea dockContentArea = dockControl.getContentArea();
    add(dockContentArea, BorderLayout.CENTER);
    DockingFramesUtil.setFlapSizes(dockControl);

    initUi();
  }

  protected void addCatalogPanel() {

    final PropertyChangeArrayList<Object> connectionManagers = new PropertyChangeArrayList<Object>();

    connectionManagers.add(DataObjectStoreConnectionManager.get());
    connectionManagers.add(FileSystemConnectionManager.get());
    connectionManagers.add(FolderConnectionManager.get());
    /* connectionManagers.add(WmsConnectionManager.get()); */

    final ListObjectTreeNodeModel listModel = new ListObjectTreeNodeModel(
      new DataObjectStoreConnectionManagerModel(),
      new FileSystemConnectionManagerModel(),
      new FolderConnectionManagerModel());
    final ObjectTreePanel catalogPanel = new ObjectTreePanel(
      connectionManagers, listModel);
    final ObjectTree tree = catalogPanel.getTree();
    tree.setDragEnabled(false);

    for (final Object connectionManager : connectionManagers) {
      tree.expandPath(connectionManagers, connectionManager);
      if (connectionManager instanceof ConnectionRegistryManager) {
        final ConnectionRegistryManager<?> manager = (ConnectionRegistryManager<?>)connectionManager;
        for (final Object registry : manager.getVisibleConnectionRegistries()) {
          tree.expandPath(connectionManagers, connectionManager, registry);
        }
      }
    }
    connectionManagers.getPropertyChangeSupport().addPropertyChangeListener(
      "registries", new PropertyChangeListener() {
        @Override
        public void propertyChange(final PropertyChangeEvent event) {
          final Object newValue = event.getNewValue();
          if (newValue instanceof ConnectionRegistry) {
            final ConnectionRegistry<?> registry = (ConnectionRegistry<?>)newValue;
            final ConnectionRegistryManager<?> connectionManager = registry.getConnectionManager();
            tree.expandPath(connectionManagers, connectionManager, registry);
          }
        }
      });
    final LayerGroup project = getProject();
    DockingFramesUtil.addDockable(project, MapPanel.MAP_CONTROLS_WORKING_AREA,
      "catalog", "Catalog", catalogPanel);

    ((DefaultSingleCDockable)getDockControl().getSingleDockable("toc")).toFront();

  }

  protected void addControlWorkingArea() {
    final CLocation location = CLocation.base().normalWest(0.20);
    DockingFramesUtil.createCWorkingArea(dockControl, project,
      MapPanel.MAP_CONTROLS_WORKING_AREA, location);
  }

  protected void addLayers() {
    final File userHomeDirectory = FileUtil.getUserHomeDirectory();
    final File projectFile = new File(userHomeDirectory,
      "Documents/default.rgmap");
    loadProject(projectFile);
  }

  protected void addLogPanel() {
    final JPanel panel = Log4jTableModel.createPanel();
    DockingFramesUtil.addDockable(project, MapPanel.MAP_TABLE_WORKING_AREA,
      "log4j", "Logging", panel);
  }

  protected MapPanel addMapPanel() {
    mapPanel = new MapPanel(this.project);

    final DefaultSingleCDockable dockable = new DefaultSingleCDockable("map",
      "Map", mapPanel);
    dockable.setStackable(false);
    dockable.setCloseable(false);
    dockable.setDefaultLocation(ExtendedMode.MINIMIZED, CLocation.base()
      .minimalWest());

    dockControl.addDockable(dockable);
    dockable.setVisible(true);
    return mapPanel;
  }

  protected DefaultSingleCDockable addTableOfContents() {
    final JPanel panel = new JPanel(new BorderLayout());

    final ToolBar toolBar = new ToolBar();
    panel.add(toolBar, BorderLayout.NORTH);

    final ProjectTreeNodeModel model = new ProjectTreeNodeModel();
    tocPanel = new ObjectTreePanel(project, model);
    final ObjectTree tree = tocPanel.getTree();
    tree.setRootVisible(true);
    project.addPropertyChangeListener("layers", new PropertyChangeListener() {

      @Override
      public void propertyChange(final PropertyChangeEvent event) {
        final Object source = event.getSource();
        if (source instanceof LayerGroup) {
          LayerGroup layerGroup = (LayerGroup)source;
          final Object newValue = event.getNewValue();
          if (newValue instanceof LayerGroup) {
            layerGroup = (LayerGroup)newValue;
          }
          final List<Layer> pathList = layerGroup.getPathList();
          final TreePath treePath = ObjectTree.createTreePath(pathList);
          tree.expandPath(treePath);
        }

      }
    });
    panel.add(tocPanel, BorderLayout.CENTER);
    final DefaultSingleCDockable tableOfContents = DockingFramesUtil.addDockable(
      project, MapPanel.MAP_CONTROLS_WORKING_AREA, "toc", "TOC", panel);
    tableOfContents.toFront();
    return tableOfContents;
  }

  protected void addTableWorkingArea() {
    final TreeLocationRoot location = CLocation.base().normalSouth(0.25);
    DockingFramesUtil.createCWorkingArea(dockControl, project,
      MapPanel.MAP_TABLE_WORKING_AREA, location);
  }

  protected void addTasksPanel() {
    final JPanel panel = SwingWorkerTableModel.createPanel();
    DockingFramesUtil.addDockable(project, MapPanel.MAP_TABLE_WORKING_AREA,
      "tasks", "Background Tasks", panel);
  }

  protected void addWorkingAreas() {
    addControlWorkingArea();
    addTableWorkingArea();
  }

  protected JMenu createFileMenu(final JMenuBar menuBar) {
    final MenuFactory file = new MenuFactory("File");

    file.addMenuItemTitleIcon("project", "Save Project", "picture_save",
      project, "saveProject");
    file.addMenuItem("exit", new Exit());
    // final JMenu fileMenu = new JMenu(new I18nAction(ProjectFrame.class,
    // "File"));
    final JMenu fileMenu = file.createComponent();
    menuBar.add(fileMenu);

    return fileMenu;
  }

  protected JMenuBar createMenuBar() {
    final JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);

    createFileMenu(menuBar);
    WindowManager.addMenu(menuBar);
    return menuBar;
  }

  @Override
  public void dispose() {
    super.dispose();
    if (project != null) {
      final DataObjectStoreConnectionRegistry dataStores = project.getDataStores();
      DataObjectStoreConnectionManager.get().removeConnectionRegistry(
        dataStores);
      tocPanel = null;
      project = null;
      dockControl = null;
      mapPanel = null;
    }
  }

  public CControl getDockControl() {
    return dockControl;
  }

  public MapPanel getMapPanel() {
    return mapPanel;
  }

  public Project getProject() {
    return project;
  }

  public ObjectTreePanel getTocPanel() {
    return tocPanel;
  }

  protected void initUi() {
    addMapPanel();

    addWorkingAreas();

    final DefaultSingleCDockable toc = addTableOfContents();
    addCatalogPanel();
    toc.toFront();

    addTasksPanel();
    addLogPanel();

    addLayers();

    createMenuBar();
  }

  public void loadProject(final File projectFile) {
    final FileSystemResource resource = new FileSystemResource(projectFile);
    if (resource.exists()) {
      project.readProject(resource);
    }
  }

}
