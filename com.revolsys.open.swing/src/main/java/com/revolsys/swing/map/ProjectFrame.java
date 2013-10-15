package com.revolsys.swing.map;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.net.ResponseCache;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
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
import com.revolsys.famfamfam.silk.SilkIconLoader;
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
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.component.BaseFrame;
import com.revolsys.swing.component.ProgressMonitor;
import com.revolsys.swing.listener.InvokeMethodPropertyChangeListener;
import com.revolsys.swing.logging.Log4jTableModel;
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
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.parallel.SwingWorkerProgressBar;
import com.revolsys.swing.preferences.PreferencesDialog;
import com.revolsys.swing.table.worker.SwingWorkerTableModel;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.swing.tree.ObjectTreePanel;
import com.revolsys.swing.tree.datastore.DataObjectStoreConnectionManagerModel;
import com.revolsys.swing.tree.file.FileSystemConnectionManagerModel;
import com.revolsys.swing.tree.file.FolderConnectionManagerModel;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.ListObjectTreeNodeModel;
import com.revolsys.util.OS;

public class ProjectFrame extends BaseFrame {
  private static final long serialVersionUID = 1L;

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

  private ObjectTreePanel catalogPanel;

  private boolean exitOnClose = true;

  public ProjectFrame(final String title) {
    this(title, new Project());
  }

  public ProjectFrame(final String title, final File projectDirectory) {
    this(title);
    ProgressMonitor.background(this, title, "Load project", false, this,
      "loadProject", projectDirectory);
  }

  public ProjectFrame(final String title, final Project project) {
    super(title);
    this.project = project;
    SwingUtil.setSizeAndMaximize(this, 100, 100);

    this.dockControl.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
    final CEclipseTheme theme = (CEclipseTheme)this.dockControl.getController()
      .getTheme();
    theme.intern().setMovingImageFactory(
      new ScreencaptureMovingImageFactory(new Dimension(2000, 2000)));

    final CContentArea dockContentArea = this.dockControl.getContentArea();
    add(dockContentArea, BorderLayout.CENTER);
    DockingFramesUtil.setFlapSizes(this.dockControl);

    initUi();
  }

  protected void addCatalogPanel() {

    final PropertyChangeArrayList<Object> connectionManagers = new PropertyChangeArrayList<Object>();

    connectionManagers.add(DataObjectStoreConnectionManager.get());
    connectionManagers.add(FileSystemConnectionManager.get());
    connectionManagers.add(FolderConnectionManager.get());

    final ListObjectTreeNodeModel listModel = new ListObjectTreeNodeModel(
      new DataObjectStoreConnectionManagerModel(),
      new FileSystemConnectionManagerModel(),
      new FolderConnectionManagerModel());
    catalogPanel = new ObjectTreePanel(connectionManagers, listModel);
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
      "registries",
      new InvokeMethodPropertyChangeListener(true, this,
        "expandConnectionManagers", PropertyChangeEvent.class));
    final LayerGroup project = getProject();
    DockingFramesUtil.addDockable(project, MapPanel.MAP_CONTROLS_WORKING_AREA,
      "catalog", "Catalog", catalogPanel);

    ((DefaultSingleCDockable)getDockControl().getSingleDockable("toc")).toFront();

  }

  protected void addControlWorkingArea() {
    final CLocation location = CLocation.base().normalWest(getControlWidth());
    DockingFramesUtil.createCWorkingArea(this.dockControl, this.project,
      MapPanel.MAP_CONTROLS_WORKING_AREA, location);
  }

  protected void addLogPanel() {
    final JPanel panel = Log4jTableModel.createPanel();
    DockingFramesUtil.addDockable(this.project,
      MapPanel.MAP_TABLE_WORKING_AREA, "log4j", "Logging", panel);
  }

  protected MapPanel addMapPanel() {
    this.mapPanel = new MapPanel(this.project);

    final DefaultSingleCDockable dockable = new DefaultSingleCDockable("map",
      "Map", this.mapPanel);
    dockable.setStackable(false);
    dockable.setCloseable(false);
    dockable.setDefaultLocation(ExtendedMode.MINIMIZED, CLocation.base()
      .minimalWest());

    this.dockControl.addDockable(dockable);
    dockable.setVisible(true);
    return this.mapPanel;
  }

  protected void addMenu(final JMenuBar menuBar, final MenuFactory menu) {
    final JMenu fileMenu = menu.createComponent();
    menuBar.add(fileMenu);
  }

  protected DefaultSingleCDockable addTableOfContents() {
    final JPanel panel = new JPanel(new BorderLayout());

    final ToolBar toolBar = new ToolBar();
    panel.add(toolBar, BorderLayout.NORTH);

    final ProjectTreeNodeModel model = new ProjectTreeNodeModel();
    this.tocPanel = new ObjectTreePanel(this.project, model);
    final ObjectTree tree = this.tocPanel.getTree();
    tree.setRootVisible(true);

    this.project.getPropertyChangeSupport().addPropertyChangeListener(
      "layers",
      new InvokeMethodPropertyChangeListener(true, this, "expandLayers",
        PropertyChangeEvent.class));
    panel.add(this.tocPanel, BorderLayout.CENTER);
    final DefaultSingleCDockable tableOfContents = DockingFramesUtil.addDockable(
      this.project, MapPanel.MAP_CONTROLS_WORKING_AREA, "toc", "TOC", panel);
    tableOfContents.toFront();
    return tableOfContents;
  }

  protected void addTableWorkingArea() {
    final TreeLocationRoot location = CLocation.base().normalSouth(0.25);
    DockingFramesUtil.createCWorkingArea(this.dockControl, this.project,
      MapPanel.MAP_TABLE_WORKING_AREA, location);
  }

  protected void addTasksPanel() {
    final JPanel panel = SwingWorkerTableModel.createPanel();
    final DefaultSingleCDockable dockable = DockingFramesUtil.addDockable(
      this.project, MapPanel.MAP_TABLE_WORKING_AREA, "tasks",
      "Background Tasks", panel);
    final SwingWorkerProgressBar progressBar = mapPanel.getProgressBar();
    final JButton viewTasksAction = InvokeMethodAction.createButton(null,
      "View Running Tasks", SilkIconLoader.getIcon("time_go"), dockable,
      "toFront");
    viewTasksAction.setBorderPainted(false);
    progressBar.add(viewTasksAction, BorderLayout.EAST);
  }

  protected void addWorkingAreas() {
    addControlWorkingArea();
    addTableWorkingArea();
  }

  protected JMenuBar createMenuBar() {
    final JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);

    addMenu(menuBar, createMenuFile());

    if (OS.isWindows()) {
      final MenuFactory tools = new MenuFactory("Tools");
      tools.addMenuItem("options", "Options...", "Options...", null, null,
        PreferencesDialog.get(), "showPanel");
      addMenu(menuBar, tools);
    }
    WindowManager.addMenu(menuBar);
    return menuBar;
  }

  protected MenuFactory createMenuFile() {
    final MenuFactory file = new MenuFactory("File");

    file.addMenuItem("project", "Save Project", "Save Project",
      SilkIconLoader.getIcon("layout_save"), this.project, "saveProject");
    file.addMenuItemTitleIcon("exit", "Exit", null, this, "exit");
    return file;
  }

  @Override
  public void dispose() {
    super.dispose();
    if (this.project != null) {
      final DataObjectStoreConnectionRegistry dataStores = this.project.getDataStores();
      DataObjectStoreConnectionManager.get().removeConnectionRegistry(
        dataStores);
      this.tocPanel = null;
      this.project = null;
      this.dockControl = null;
      this.mapPanel = null;
    }
  }

  public void exit() {
    final Project project = getProject();
    if (project != null && project.saveChangesWithPrompt()) {
      final Window[] windows = Window.getOwnerlessWindows();
      for (final Window window : windows) {
        window.dispose();
      }
      System.exit(0);
    }
  }

  public void expandConnectionManagers(final PropertyChangeEvent event) {
    final Object newValue = event.getNewValue();
    if (newValue instanceof ConnectionRegistry) {
      final ConnectionRegistry<?> registry = (ConnectionRegistry<?>)newValue;
      final ConnectionRegistryManager<?> connectionManager = registry.getConnectionManager();
      if (connectionManager != null) {
        final List<?> connectionRegistries = connectionManager.getConnectionRegistries();
        if (connectionRegistries != null) {
          final ObjectTree tree = catalogPanel.getTree();
          tree.expandPath(connectionRegistries, connectionManager, registry);
        }
      }
    }
  }

  public void expandLayers(final Layer layer) {
    if (SwingUtilities.isEventDispatchThread()) {
      final List<Layer> pathList = layer.getPathList();
      final ObjectTree tree = tocPanel.getTree();
      final TreePath treePath = ObjectTree.createTreePath(pathList);
      if (layer instanceof LayerGroup) {
        final LayerGroup layerGroup = (LayerGroup)layer;
        tree.expandPath(treePath);
        for (final Layer childLayer : layerGroup) {
          expandLayers(childLayer);
        }
      } else {
        final ObjectTreeModel model = tree.getModel();
        model.fireTreeNodesChanged(treePath);
      }
    } else {
      Invoke.later(this, "expandLayers", layer);
    }
  }

  public void expandLayers(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source instanceof LayerGroup) {
      final Object newValue = event.getNewValue();
      if (newValue instanceof LayerGroup) {
        expandLayers((LayerGroup)newValue);
      }
    }
  }

  public double getControlWidth() {
    return 0.20;
  }

  public CControl getDockControl() {
    return this.dockControl;
  }

  public MapPanel getMapPanel() {
    return this.mapPanel;
  }

  public Project getProject() {
    return this.project;
  }

  public ObjectTreePanel getTocPanel() {
    return this.tocPanel;
  }

  protected void initUi() {
    addMapPanel();

    addWorkingAreas();

    final DefaultSingleCDockable toc = addTableOfContents();
    addCatalogPanel();
    toc.toFront();

    addTasksPanel();
    addLogPanel();

    createMenuBar();
  }

  public void loadProject(final File projectFile) {
    final boolean eventsEnabled = project.setEventsEnabled(false);
    try {
      final FileSystemResource resource = new FileSystemResource(projectFile);
      this.project.readProject(resource);

      final DataObjectStoreConnectionManager dataStoreConnectionManager = DataObjectStoreConnectionManager.get();
      dataStoreConnectionManager.removeConnectionRegistry("Project");
      dataStoreConnectionManager.addConnectionRegistry(project.getDataStores());

      final FolderConnectionManager folderConnectionManager = FolderConnectionManager.get();
      folderConnectionManager.removeConnectionRegistry("Project");
      folderConnectionManager.addConnectionRegistry(project.getFolderConnections());
    } finally {
      project.setEventsEnabled(eventsEnabled);
    }
  }

  public void loadProject(final ProgressMonitor progressMonitor,
    final File projectFile) {
    try {
      loadProject(projectFile);
    } finally {
      progressMonitor.close();
      toFront();
    }
  }

  public void setExitOnClose(final boolean exitOnClose) {
    this.exitOnClose = exitOnClose;
  }

  @Override
  public void windowClosing(final WindowEvent e) {
    if (exitOnClose) {
      exit();
    } else {
      dispose();
    }
  }

}
