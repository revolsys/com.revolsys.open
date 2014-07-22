package com.revolsys.swing.map;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.net.ResponseCache;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreePath;

import org.springframework.core.io.FileSystemResource;

import bibliothek.gui.dock.common.CContentArea;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.MultipleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.location.TreeLocationRoot;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.gui.dock.common.theme.CEclipseTheme;
import bibliothek.gui.dock.common.theme.ThemeMap;
import bibliothek.gui.dock.dockable.ScreencaptureMovingImageFactory;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.io.datastore.RecordStoreConnectionManager;
import com.revolsys.io.datastore.RecordStoreConnectionRegistry;
import com.revolsys.io.file.FolderConnectionManager;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.jts.util.BoundingBoxUtil;
import com.revolsys.net.urlcache.FileResponseCache;
import com.revolsys.swing.DockingFramesUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.WindowManager;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.component.BaseFrame;
import com.revolsys.swing.listener.InvokeMethodPropertyChangeListener;
import com.revolsys.swing.logging.Log4jTableModel;
import com.revolsys.swing.map.component.layerchooser.LayerChooserPanel;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.arcgisrest.ArcGisServerRestLayer;
import com.revolsys.swing.map.layer.bing.BingLayer;
import com.revolsys.swing.map.layer.geonames.GeoNamesBoundingBoxLayerWorker;
import com.revolsys.swing.map.layer.grid.GridLayer;
import com.revolsys.swing.map.layer.openstreetmap.OpenStreetMapApiLayer;
import com.revolsys.swing.map.layer.openstreetmap.OpenStreetMapLayer;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageLayer;
import com.revolsys.swing.map.layer.record.FileRecordLayer;
import com.revolsys.swing.map.layer.record.RecordStoreLayer;
import com.revolsys.swing.map.layer.wikipedia.WikipediaBoundingBoxLayerWorker;
import com.revolsys.swing.map.tree.ProjectTreeNodeModel;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.parallel.SwingWorkerProgressBar;
import com.revolsys.swing.preferences.PreferencesDialog;
import com.revolsys.swing.table.worker.SwingWorkerTableModel;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.swing.tree.ObjectTreePanel;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.OS;
import com.revolsys.util.PreferencesUtil;
import com.revolsys.util.Property;

public class ProjectFrame extends BaseFrame {
  public static void addSaveActions(final JComponent component,
    final Project project) {
    final InputMap inputMap = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    inputMap.put(
      KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
      SAVE_PROJECT_KEY);
    inputMap.put(
      KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_DOWN_MASK),
      SAVE_PROJECT_KEY);

    inputMap.put(
      KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK
        | InputEvent.ALT_DOWN_MASK), SAVE_CHANGES_KEY);
    inputMap.put(
      KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_DOWN_MASK
        | InputEvent.ALT_DOWN_MASK), SAVE_CHANGES_KEY);

    final ActionMap actionMap = component.getActionMap();
    actionMap.put(SAVE_PROJECT_KEY, new InvokeMethodAction(SAVE_PROJECT_KEY,
      project, "saveAllSettings"));
    actionMap.put(SAVE_CHANGES_KEY, new InvokeMethodAction(SAVE_CHANGES_KEY,
      project, "saveChanges"));
  }

  public static final String SAVE_PROJECT_KEY = "Save Project";

  public static final String SAVE_CHANGES_KEY = "Save Changes";

  private static final long serialVersionUID = 1L;

  static {
    ResponseCache.setDefault(new FileResponseCache());

    // TODO move to a file config
    MapObjectFactoryRegistry.addFactory(LayerGroup.FACTORY);
    MapObjectFactoryRegistry.addFactory(RecordStoreLayer.FACTORY);
    MapObjectFactoryRegistry.addFactory(ArcGisServerRestLayer.FACTORY);
    MapObjectFactoryRegistry.addFactory(BingLayer.FACTORY);
    MapObjectFactoryRegistry.addFactory(OpenStreetMapLayer.FACTORY);
    MapObjectFactoryRegistry.addFactory(OpenStreetMapApiLayer.FACTORY);
    MapObjectFactoryRegistry.addFactory(FileRecordLayer.FACTORY);
    MapObjectFactoryRegistry.addFactory(GridLayer.FACTORY);
    MapObjectFactoryRegistry.addFactory(WikipediaBoundingBoxLayerWorker.FACTORY);
    MapObjectFactoryRegistry.addFactory(GeoNamesBoundingBoxLayerWorker.FACTORY);
    MapObjectFactoryRegistry.addFactory(GeoReferencedImageLayer.FACTORY);

  }

  private ObjectTreePanel tocPanel;

  private Project project;

  private CControl dockControl = new CControl(this);

  private MapPanel mapPanel;

  private BaseTree catalogTree;

  private boolean exitOnClose = true;

  public ProjectFrame(final String title) {
    this(title, new Project());
  }

  public ProjectFrame(final String title, final File projectDirectory) {
    this(title);
    Invoke.background("Load project", this, "loadProject", projectDirectory);
  }

  public ProjectFrame(final String title, final Project project) {
    super(title);
    final JRootPane rootPane = getRootPane();

    addSaveActions(rootPane, project);

    this.project = project;
    final BoundingBox defaultBoundingBox = getDefaultBoundingBox();
    this.project.setViewBoundingBox(defaultBoundingBox);
    Project.set(project);
    SwingUtil.setSizeAndMaximize(this, 100, 100);
    setLocationByPlatform(true);

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
    this.catalogTree = LayerChooserPanel.createTree();
    final LayerGroup project = getProject();

    DockingFramesUtil.addDockable(project, MapPanel.MAP_CONTROLS_WORKING_AREA,
      "catalog", "Catalog", new JScrollPane(this.catalogTree));

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

    Property.addListener(this.project, "layers",
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
    final SwingWorkerProgressBar progressBar = this.mapPanel.getProgressBar();
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

    final MenuFactory tools = createMenuTools();

    if (OS.isWindows()) {
      tools.addMenuItem("options", "Options...", "Options...", null, null,
        PreferencesDialog.get(), "showPanel");
    }
    addMenu(menuBar, tools);
    WindowManager.addMenu(menuBar);
    return menuBar;
  }

  protected MenuFactory createMenuFile() {
    final MenuFactory file = new MenuFactory("File");

    file.addMenuItem("project", "Save Project", "Save Project",
      SilkIconLoader.getIcon("layout_save"), this.project, "saveAllSettings");
    file.addMenuItemTitleIcon("exit", "Exit", null, this, "exit");
    return file;
  }

  protected MenuFactory createMenuTools() {
    final MenuFactory tools = new MenuFactory("Tools");

    tools.addMenuItem("script", "Run Script...", "Run Script",
      SilkIconLoader.getIcon("script_go"), this, "runScript");
    return tools;
  }

  @Override
  public void dispose() {
    if (SwingUtilities.isEventDispatchThread()) {
      setVisible(false);
      super.dispose();
      Property.removeAllListeners(this);
      if (this.project != null) {
        final RecordStoreConnectionRegistry recordStores = this.project.getRecordStores();
        RecordStoreConnectionManager.get().removeConnectionRegistry(
          recordStores);
        if (Project.get() == this.project) {
          Project.set(null);
        }
      }
      if (this.tocPanel != null) {
        this.tocPanel.destroy();
      }

      this.tocPanel = null;
      this.project = null;
      if (this.dockControl != null) {
        int i = 0;
        while (i < this.dockControl.getCDockableCount()) {
          final CDockable dockable = this.dockControl.getCDockable(i);
          if (dockable instanceof MultipleCDockable) {
            final MultipleCDockable multiple = (MultipleCDockable)dockable;
            this.dockControl.remove(multiple);

          } else if (dockable instanceof SingleCDockable) {
            final SingleCDockable multiple = (SingleCDockable)dockable;
            this.dockControl.remove(multiple);

          } else {
            i++;
          }
        }
        this.dockControl.destroy();
        this.dockControl.setRootWindow(null);
        this.dockControl = null;
      }
      if (this.mapPanel != null) {
        this.mapPanel.destroy();
        this.mapPanel = null;
      }
      setMenuBar(null);
      final ActionMap actionMap = getRootPane().getActionMap();
      actionMap.put(SAVE_PROJECT_KEY, null);
      actionMap.put(SAVE_CHANGES_KEY, null);

      setRootPane(new JRootPane());
      removeAll();
    } else {
      Invoke.later(this, "dispose");
    }
  }

  public void exit() {
    final Project project = getProject();
    if (project != null && project.saveWithPrompt()) {
      final Window[] windows = Window.getOwnerlessWindows();
      for (final Window window : windows) {
        window.dispose();
      }
      System.exit(0);
    }
  }

  public void expandLayers(final Layer layer) {
    if (SwingUtilities.isEventDispatchThread()) {
      final List<Layer> pathList = layer.getPathList();
      final ObjectTree tree = this.tocPanel.getTree();
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

  // public void expandConnectionManagers(final PropertyChangeEvent event) {
  // final Object newValue = event.getNewValue();
  // if (newValue instanceof ConnectionRegistry) {
  // final ConnectionRegistry<?> registry = (ConnectionRegistry<?>)newValue;
  // final ConnectionRegistryManager<?> connectionManager =
  // registry.getConnectionManager();
  // if (connectionManager != null) {
  // final List<?> connectionRegistries =
  // connectionManager.getConnectionRegistries();
  // if (connectionRegistries != null) {
  // final ObjectTree tree = catalogPanel.getTree();
  // tree.expandPath(connectionRegistries, connectionManager, registry);
  // }
  // }
  // }
  // }

  public double getControlWidth() {
    return 0.20;
  }

  protected BoundingBox getDefaultBoundingBox() {
    return new BoundingBoxDoubleGf();
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

    addTasksPanel();
    addLogPanel();

    createMenuBar();
    toc.toFront();
  }

  public void loadProject(final File projectDirectory) {
    final FileSystemResource resource = new FileSystemResource(projectDirectory);
    this.project.readProject(resource);

    final RecordStoreConnectionManager recordStoreConnectionManager = RecordStoreConnectionManager.get();
    recordStoreConnectionManager.removeConnectionRegistry("Project");
    recordStoreConnectionManager.addConnectionRegistry(this.project.getRecordStores());

    final FolderConnectionManager folderConnectionManager = FolderConnectionManager.get();
    folderConnectionManager.removeConnectionRegistry("Project");
    folderConnectionManager.addConnectionRegistry(this.project.getFolderConnections());

    final BoundingBox initialBoundingBox = this.project.getInitialBoundingBox();
    if (!BoundingBoxUtil.isEmpty(initialBoundingBox)) {
      this.project.setGeometryFactory(initialBoundingBox.getGeometryFactory());
      getMapPanel().setGeometryFactory(initialBoundingBox.getGeometryFactory());
      this.project.setViewBoundingBox(initialBoundingBox);
    }
    getMapPanel().getViewport().setInitialized(true);
  }

  public void runScript() {
    final JFileChooser fileChooser = SwingUtil.createFileChooser(
      "Select Script", "com.revolsys.swing.tools.script", "directory");
    final FileNameExtensionFilter groovyFilter = new FileNameExtensionFilter(
      "Groovy Script", "groovy");
    fileChooser.addChoosableFileFilter(groovyFilter);
    fileChooser.setMultiSelectionEnabled(false);
    final int returnVal = fileChooser.showOpenDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {

      final Binding binding = new Binding();
      final GroovyShell shell = new GroovyShell(binding);
      final File scriptFile = fileChooser.getSelectedFile();
      final String[] args = new String[0];
      try {
        PreferencesUtil.setUserString("com.revolsys.swing.tools.script",
          "directory", scriptFile.getParent());
        shell.run(scriptFile, args);
      } catch (final Throwable e) {
        ExceptionUtil.log(getClass(), "Unable to run script:" + scriptFile, e);
      }
    }
  }

  public void setExitOnClose(final boolean exitOnClose) {
    this.exitOnClose = exitOnClose;
  }

  @Override
  public void windowClosing(final WindowEvent e) {
    if (this.exitOnClose) {
      exit();
    } else {
      dispose();
    }
  }

}
