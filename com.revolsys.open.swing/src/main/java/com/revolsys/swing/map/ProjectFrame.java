package com.revolsys.swing.map;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.ResponseCache;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreePath;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.util.BoundingBoxUtil;
import com.revolsys.io.FileUtil;
import com.revolsys.io.file.FileConnectionManager;
import com.revolsys.io.file.Paths;
import com.revolsys.net.urlcache.FileResponseCache;
import com.revolsys.process.JavaProcess;
import com.revolsys.record.io.RecordStoreConnectionManager;
import com.revolsys.record.io.RecordStoreConnectionRegistry;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.component.BaseFrame;
import com.revolsys.swing.component.DnDTabbedPane;
import com.revolsys.swing.component.TabClosableTitle;
import com.revolsys.swing.logging.Log4jTabLabel;
import com.revolsys.swing.logging.Log4jTableModel;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.overlay.MeasureOverlay;
import com.revolsys.swing.map.print.SinglePage;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.parallel.SwingWorkerProgressBar;
import com.revolsys.swing.pdf.SaveAsPdf;
import com.revolsys.swing.preferences.PreferencesDialog;
import com.revolsys.swing.scripting.ScriptRunner;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.worker.SwingWorkerTableModel;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.ListTreeNode;
import com.revolsys.swing.tree.node.file.FileSystemsTreeNode;
import com.revolsys.swing.tree.node.file.FolderConnectionsTreeNode;
import com.revolsys.swing.tree.node.file.PathTreeNode;
import com.revolsys.swing.tree.node.layer.ProjectTreeNode;
import com.revolsys.swing.tree.node.record.RecordStoreConnectionsTreeNode;
import com.revolsys.util.Exceptions;
import com.revolsys.util.OS;
import com.revolsys.util.PreferencesUtil;
import com.revolsys.util.Property;

public class ProjectFrame extends BaseFrame {
  public static final String PROJECT_FRAME = "projectFrame";

  public static final String SAVE_CHANGES_KEY = "Save Changes";

  public static final String SAVE_PROJECT_KEY = "Save Project";

  private static final long serialVersionUID = 1L;

  static {
    ResponseCache.setDefault(new FileResponseCache());
  }

  public static void addSaveActions(final JComponent component, final Project project) {
    final InputMap inputMap = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
      SAVE_PROJECT_KEY);
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_DOWN_MASK),
      SAVE_PROJECT_KEY);

    inputMap.put(
      KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
      SAVE_CHANGES_KEY);
    inputMap.put(
      KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
      SAVE_CHANGES_KEY);

    final ActionMap actionMap = component.getActionMap();
    actionMap.put(SAVE_PROJECT_KEY, new RunnableAction(SAVE_PROJECT_KEY, project::saveAllSettings));
    actionMap.put(SAVE_CHANGES_KEY, new RunnableAction(SAVE_CHANGES_KEY, project::saveChanges));
  }

  public static ProjectFrame get(final Layer layer) {
    if (layer == null) {
      return null;
    } else {
      final LayerGroup project = layer.getProject();
      if (project == null) {
        return null;
      } else {
        return project.getProperty(PROJECT_FRAME);
      }
    }
  }

  private DnDTabbedPane bottomTabs = new DnDTabbedPane();

  private BaseTree catalogTree;

  private boolean exitOnClose = true;

  private final String frameTitle;

  private JSplitPane leftRightSplit;

  private JTabbedPane leftTabs = new JTabbedPane();

  private MapPanel mapPanel;

  private final MenuFactory openRecentMenu = new MenuFactory("Open Recent Project");

  private Project project;

  private BaseTree tocTree;

  private JSplitPane topBottomSplit;

  public ProjectFrame(final String title) {
    this(title, new Project());
  }

  public ProjectFrame(final String title, final Path projectPath) {
    this(title);
    if (projectPath != null) {
      Invoke.background("Load project: " + projectPath, () -> loadProject(projectPath));
    }
  }

  public ProjectFrame(final String title, final Project project) {
    super(title, false);
    this.frameTitle = title;
    this.project = project;
    init();
  }

  private void actionNewProject() {
    if (this.project != null && this.project.saveWithPrompt()) {
      this.project.reset();
      super.setTitle("NEW - " + this.frameTitle);
    }
  }

  private void actionOpenProject() {
    if (this.project != null && this.project.saveWithPrompt()) {

      final JFileChooser fileChooser = SwingUtil.newFileChooser("Open Project",
        "com.revolsys.swing.map.project", "directory");

      final FileNameExtensionFilter filter = new FileNameExtensionFilter(
        "RevolutionGIS Project (*.rgmap)", "rgmap");
      fileChooser.setAcceptAllFileFilterUsed(false);
      fileChooser.addChoosableFileFilter(filter);
      fileChooser.setFileFilter(filter);
      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

      final int returnVal = fileChooser.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        final File projectDirectory = fileChooser.getSelectedFile();
        openProject(projectDirectory.toPath());
      }
    }
  }

  public void actionSaveProjectAs() {
    final Path path = this.project.saveAllSettingsAs();
    if (path != null) {
      addToRecentProjects(path);
      Invoke.later(() -> setTitle(this.project.getName() + " - " + this.frameTitle));
    }
  }

  @SuppressWarnings("unchecked")
  public <C extends Component> C addBottomTab(final ProjectFramePanel panel,
    final Map<String, Object> config) {
    final JTabbedPane tabs = getBottomTabs();

    final Object tableView = panel.getProperty("bottomTab");
    Component component = null;
    if (tableView instanceof Component) {
      component = (Component)tableView;
      if (component.getParent() != tabs) {
        component = null;
      }
    }
    if (component == null) {
      component = panel.newPanelComponent(config);

      if (component != null) {
        final Component panelComponent = component;
        panel.activatePanelComponent(panelComponent, config);
        final int tabIndex = tabs.getTabCount();
        final String name = panel.getName();
        tabs.addTab(name, panel.getIcon(), panelComponent);

        final TabClosableTitle tabTitle = new TabClosableTitle(tabs,
          () -> panel.deletePanelComponent(panelComponent));
        tabs.setTabComponentAt(tabIndex, tabTitle);

        panel.setPropertyWeak("bottomTab", panelComponent);
        tabs.setSelectedIndex(tabIndex);
      }
    } else {
      panel.activatePanelComponent(component, config);
      tabs.setSelectedComponent(component);
    }
    return (C)component;
  }

  protected void addCatalogPanel() {
    final RecordStoreConnectionsTreeNode recordStores = new RecordStoreConnectionsTreeNode();

    final FileSystemsTreeNode fileSystems = new FileSystemsTreeNode();

    final FolderConnectionsTreeNode folderConnections = new FolderConnectionsTreeNode();

    final ListTreeNode root = new ListTreeNode("/", recordStores, fileSystems, folderConnections);

    final BaseTree tree = new BaseTree(root);
    tree.setRootVisible(false);

    recordStores.expandChildren();
    fileSystems.expand();
    folderConnections.expandChildren();

    this.catalogTree = tree;

    final Icon icon = Icons.getIconWithBadge(PathTreeNode.ICON_FOLDER, "tree");
    addTab(this.leftTabs, icon, "Catalog", this.catalogTree, true);
  }

  protected void addLogPanel() {
    final TablePanel panel = Log4jTableModel.newPanel();

    final Log4jTableModel tableModel = panel.getTableModel();

    final int tabIndex = this.bottomTabs.getTabCount();
    this.bottomTabs.addTab(null, Icons.getIcon("error"), panel);

    final Log4jTabLabel tabLabel = new Log4jTabLabel(this.bottomTabs, tableModel);
    this.bottomTabs.setTabComponentAt(tabIndex, tabLabel);
    this.bottomTabs.setSelectedIndex(tabIndex);
  }

  protected MapPanel addMapPanel() {
    this.mapPanel = new MapPanel(this.project);
    if (OS.isMac()) {
      // Make border on right/bottom to match the JTabbedPane UI on a mac
      this.mapPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 9, 9));
    }
    return this.mapPanel;
  }

  protected void addMenu(final JMenuBar menuBar, final MenuFactory menuFactory) {
    if (menuFactory != null) {
      final JMenu menu = menuFactory.newComponent();
      menuBar.add(menu, menuBar.getMenuCount() - 1);
    }
  }

  public int addTab(final JTabbedPane tabs, final Icon icon, final String toolTipText,
    Component component, final boolean useScrollPane) {
    if (useScrollPane) {
      final JScrollPane scrollPane = new JScrollPane(component);
      scrollPane.setBorder(BorderFactory.createEmptyBorder());
      component = scrollPane;
    }

    tabs.addTab(null, icon, component);
    final int tabIndex = tabs.getTabCount() - 1;
    tabs.setToolTipTextAt(tabIndex, toolTipText);
    return tabIndex;
  }

  public int addTabIcon(final JTabbedPane tabs, final String iconName, final String toolTipText,
    final Component component, final boolean useScrollPane) {
    final ImageIcon icon = Icons.getIcon(iconName);

    return addTab(tabs, icon, toolTipText, component, useScrollPane);
  }

  protected void addTableOfContents() {
    final Project project = getProject();
    this.tocTree = ProjectTreeNode.newTree(project);

    // Property.addListener(this.project, "layers", (event) -> {
    // Invoke.later(() -> {
    // final boolean open = this.project.isOpen();
    // this.tocTree.collapseRow(0);
    // if (open) {
    // this.tocTree.expandRow(0);
    // }
    // });
    // });

    addTabIcon(this.leftTabs, "tree_layers", "TOC", this.tocTree, true);

  }

  protected void addTasksPanel() {
    final JPanel panel = SwingWorkerTableModel.newPanel();
    final int tabIndex = addTabIcon(this.bottomTabs, "time", "Background Tasks", panel, false);

    final SwingWorkerProgressBar progressBar = this.mapPanel.getProgressBar();
    final JButton viewTasksAction = RunnableAction.newButton(null, "View Running Tasks",
      Icons.getIcon("time_go"), () -> this.bottomTabs.setSelectedIndex(tabIndex));
    viewTasksAction.setBorderPainted(false);
    viewTasksAction.setBorder(null);
    progressBar.add(viewTasksAction, BorderLayout.EAST);
  }

  private void addToRecentProjects(final Path projectPath) {
    final List<String> recentProjects = getRecentProjectPaths();
    final String filePath = projectPath.toAbsolutePath().toString();
    recentProjects.remove(filePath);
    recentProjects.add(0, filePath);
    while (recentProjects.size() > 10) {
      recentProjects.remove(recentProjects.size() - 1);
    }
    OS.setPreference("com.revolsys.gis", "/com/revolsys/gis/project", "recentProjects",
      recentProjects);
    OS.setPreference("com.revolsys.gis", "/com/revolsys/gis/project", "recentProject", filePath);
    updateRecentMenu();
  }

  @Override
  public void dispose() {
    Property.removeAllListeners(this);
    setVisible(false);
    super.dispose();
    setRootPane(new JRootPane());
    removeAll();
    setMenuBar(null);
    if (this.project != null) {
      final RecordStoreConnectionRegistry recordStores = this.project.getRecordStores();
      RecordStoreConnectionManager.get().removeConnectionRegistry(recordStores);
      if (Project.get() == this.project) {
        Project.set(null);
      }
    }
    this.tocTree = null;
    this.project = null;
    this.leftTabs = null;
    this.leftRightSplit = null;
    this.bottomTabs = null;
    this.topBottomSplit = null;

    if (this.mapPanel != null) {
      this.mapPanel.destroy();
      this.mapPanel = null;
    }
    final ActionMap actionMap = getRootPane().getActionMap();
    actionMap.put(SAVE_PROJECT_KEY, null);
    actionMap.put(SAVE_CHANGES_KEY, null);
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
    if (layer != null) {
      Invoke.later(() -> {
        final LayerGroup group;
        if (layer instanceof LayerGroup) {
          group = (LayerGroup)layer;
        } else {
          group = layer.getLayerGroup();
        }
        if (group != null) {
          final List<Layer> layerPath = group.getPathList();
          this.tocTree.expandPath(layerPath);
        }
      });
    }
  }

  public JTabbedPane getBottomTabs() {
    return this.bottomTabs;
  }

  public double getControlWidth() {
    return 0.20;
  }

  protected BoundingBox getDefaultBoundingBox() {
    return BoundingBox.EMPTY;
  }

  public JTabbedPane getLeftTabs() {
    return this.leftTabs;
  }

  public File getLogDirectory() {
    return FileUtil.getDirectory("log");
  }

  public MapPanel getMapPanel() {
    return this.mapPanel;
  }

  public Project getProject() {
    return this.project;
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

  private List<String> getRecentProjectPaths() {
    final List<String> recentProjects = OS.getPreference("com.revolsys.gis",
      "/com/revolsys/gis/project", "recentProjects", new ArrayList<String>());
    for (int i = 0; i < recentProjects.size();) {
      final String filePath = recentProjects.get(i);
      final File file = FileUtil.getFile(filePath);
      if (file.exists()) {
        i++;
      } else {
        recentProjects.remove(i);
      }
    }
    OS.setPreference("com.revolsys.gis", "/com/revolsys/gis/project", "recentProjects",
      recentProjects);
    return recentProjects;
  }

  public BaseTreeNode getTreeNode(final Layer layer) {
    final List<Layer> layerPath = layer.getPathList();
    final TreePath treePath = this.tocTree.getTreePath(layerPath);
    if (treePath == null) {
      return null;
    } else {
      return (BaseTreeNode)treePath.getLastPathComponent();
    }
  }

  @Override
  protected void init() {
    setMinimumSize(new Dimension(600, 500));

    final JRootPane rootPane = getRootPane();

    addSaveActions(rootPane, this.project);

    final BoundingBox defaultBoundingBox = getDefaultBoundingBox();
    this.project.setViewBoundingBox(defaultBoundingBox);
    Project.set(this.project);
    this.project.setProperty(PROJECT_FRAME, this);

    addMapPanel();

    this.leftTabs.setMinimumSize(new Dimension(100, 300));
    this.leftTabs.setPreferredSize(new Dimension(300, 700));

    this.mapPanel.setMinimumSize(new Dimension(300, 300));
    this.mapPanel.setPreferredSize(new Dimension(700, 700));
    this.leftRightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.leftTabs, this.mapPanel);

    this.leftRightSplit.setBorder(BorderFactory.createEmptyBorder());
    this.bottomTabs.setBorder(BorderFactory.createEmptyBorder());
    this.bottomTabs.setPreferredSize(new Dimension(700, 200));
    final ContainerListener listener = new ContainerAdapter() {
      @Override
      public void componentRemoved(final ContainerEvent e) {
        final Component eventComponent = e.getChild();
        if (eventComponent instanceof ProjectFramePanel) {
          final ProjectFramePanel panel = (ProjectFramePanel)eventComponent;
          panel.setProperty("bottomTab", null);
        }
      }
    };
    this.bottomTabs.addContainerListener(listener);

    this.topBottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.leftRightSplit,
      this.bottomTabs);
    this.bottomTabs.setMinimumSize(new Dimension(600, 100));

    this.topBottomSplit.setResizeWeight(1);

    add(this.topBottomSplit, BorderLayout.CENTER);

    addTableOfContents();
    addCatalogPanel();

    addTasksPanel();
    addLogPanel();
    setBounds((Object)null, false);

    super.init();
  }

  public void loadProject(final Path projectPathe) {
    final PathResource resource = new PathResource(projectPathe);
    this.project.readProject(resource);
    Invoke.later(() -> setTitle(this.project.getName() + " - " + this.frameTitle));

    final Object frameBoundsObject = this.project.getProperty("frameBounds");
    setBounds(frameBoundsObject, true);
    setVisible(true);

    final RecordStoreConnectionManager recordStoreConnectionManager = RecordStoreConnectionManager
      .get();
    recordStoreConnectionManager.removeConnectionRegistry("Project");
    recordStoreConnectionManager.addConnectionRegistry(this.project.getRecordStores());

    final FileConnectionManager fileConnectionManager = FileConnectionManager.get();
    fileConnectionManager.removeConnectionRegistry("Project");
    fileConnectionManager.addConnectionRegistry(this.project.getFolderConnections());

    final MapPanel mapPanel = getMapPanel();
    final BoundingBox initialBoundingBox = this.project.getInitialBoundingBox();
    final Viewport2D viewport = mapPanel.getViewport();
    if (!BoundingBoxUtil.isEmpty(initialBoundingBox)) {
      final GeometryFactory geometryFactory = initialBoundingBox.getGeometryFactory();
      this.project.setGeometryFactory(geometryFactory);
      this.project.setViewBoundingBox(initialBoundingBox);
      viewport.setGeometryFactory(geometryFactory);
      viewport.setBoundingBox(initialBoundingBox);

    }
    viewport.setInitialized(true);
  }

  public JavaProcess newJavaProcess() {
    return new JavaProcess();
  }

  @Override
  protected JMenuBar newMenuBar() {
    final JMenuBar menuBar = super.newMenuBar();
    addMenu(menuBar, newMenuFile());

    final MenuFactory tools = newMenuTools();

    if (OS.isWindows()) {
      tools.addMenuItem("options", "Options...", "Options...", null,
        () -> PreferencesDialog.get().showPanel());
    }
    addMenu(menuBar, tools);
    return menuBar;
  }

  protected MenuFactory newMenuFile() {
    final MenuFactory file = new MenuFactory("File");

    file.addMenuItemTitleIcon("projectOpen", "New Project", "layout_add", () -> actionNewProject())
      .setAcceleratorControlKey(KeyEvent.VK_N);

    file.addMenuItemTitleIcon("projectOpen", "Open Project...", "layout_add",
      () -> actionOpenProject()).setAcceleratorControlKey(KeyEvent.VK_O);

    file.addComponentFactory("projectOpen", this.openRecentMenu);
    updateRecentMenu();

    file.addMenuItemTitleIcon("projectSave", "Save Project", "layout_save",
      () -> this.project.saveAllSettings()).setAcceleratorControlKey(KeyEvent.VK_S);

    file.addMenuItemTitleIcon("projectSave", "Save Project As...", "layout_save",
      () -> actionSaveProjectAs()).setAcceleratorShiftControlKey(KeyEvent.VK_S);

    file.addMenuItemTitleIcon("save", "Save as PDF", "save_pdf", () -> SaveAsPdf.save());

    file.addMenuItemTitleIcon("print", "Print", "printer", () -> SinglePage.print())
      .setAcceleratorControlKey(KeyEvent.VK_P);

    if (OS.isWindows()) {
      file.addMenuItemTitleIcon("exit", "Exit", null, () -> exit())
        .setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
    } else if (OS.isUnix()) {
      file.addMenuItemTitleIcon("exit", "Exit", null, () -> exit())
        .setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
    }

    return file;
  }

  protected MenuFactory newMenuTools() {
    final MenuFactory tools = new MenuFactory("Tools");
    final MapPanel map = getMapPanel();
    tools.addCheckboxMenuItem("map",
      new RunnableAction("Measure", Icons.getIcon("ruler"),
        () -> map.toggleMode(MeasureOverlay.MEASURE)),
      new ObjectPropertyEnableCheck(map, "overlayAction", MeasureOverlay.MEASURE));

    tools.addMenuItem("script", "Run Script...", "script_go", () -> {
      final File logDirectory = getLogDirectory();
      final JavaProcess javaProcess = newJavaProcess();
      ScriptRunner.runScriptProcess(this, logDirectory, javaProcess);
    });
    return tools;
  }

  public void openProject(final Path projectPath) {
    if (Files.exists(projectPath)) {
      try {
        addToRecentProjects(projectPath);

        PreferencesUtil.setUserString("com.revolsys.swing.map.project", "directory",
          projectPath.getParent().toString());
        this.project.reset();
        Invoke.background("Load project", () -> loadProject(projectPath));
      } catch (final Throwable e) {
        Exceptions.log(getClass(), "Unable to open project:" + projectPath, e);
      }
    }
  }

  public void removeBottomTab(final ProjectFramePanel panel) {
    final JTabbedPane tabs = getBottomTabs();
    final Component component = panel.getProperty("bottomTab");
    if (component != null) {
      if (tabs != null) {
        tabs.remove(component);
      }
      panel.setProperty("bottomTab", null);
    }
  }

  public void setBounds(final Object frameBoundsObject, final boolean visible) {
    Invoke.later(() -> {
      boolean sizeSet = false;
      if (frameBoundsObject instanceof List) {
        try {
          @SuppressWarnings("unchecked")
          final List<Number> frameBoundsList = (List<Number>)frameBoundsObject;
          if (frameBoundsList.size() == 4) {
            int x = frameBoundsList.get(0).intValue();
            int y = frameBoundsList.get(1).intValue();
            int width = frameBoundsList.get(2).intValue();
            int height = frameBoundsList.get(3).intValue();

            final Rectangle screenBounds = SwingUtil.getScreenBounds(x, y);

            width = Math.min(width, screenBounds.width);
            height = Math.min(height, screenBounds.height);
            setSize(width, height);

            if (x < screenBounds.x || x > screenBounds.x + screenBounds.width) {
              x = 0;
            } else {
              x = Math.min(x, screenBounds.x + screenBounds.width - width);
            }
            if (y < screenBounds.y || x > screenBounds.y + screenBounds.height) {
              y = 0;
            } else {
              y = Math.min(y, screenBounds.y + screenBounds.height - height);
            }
            setLocation(x, y);
            sizeSet = true;
          }
        } catch (final Throwable t) {
        }
      }
      if (!sizeSet) {
        final Rectangle screenBounds = SwingUtil.getScreenBounds();
        setLocation(screenBounds.x + 10, screenBounds.y + 10);
        setSize(screenBounds.width - 20, screenBounds.height - 20);
      }
      final int leftRightDividerLocation = (int)(getWidth() * 0.2);
      this.leftRightSplit.setDividerLocation(leftRightDividerLocation);

      final int topBottomDividerLocation = (int)(getHeight() * 0.75);
      this.topBottomSplit.setDividerLocation(topBottomDividerLocation);
      if (visible) {
        setVisible(true);
      }
    });
  }

  public void setExitOnClose(final boolean exitOnClose) {
    this.exitOnClose = exitOnClose;
  }

  public void setProject(final Project project) {
    if (this.project != project) {
      final Project oldProject = this.project;
      this.mapPanel.setProject(project);
      this.tocTree.setRoot(new ProjectTreeNode(project));
      if (oldProject != null) {
        oldProject.delete();
      }
      this.project = project;
      firePropertyChange("project", oldProject, project);
    }
  }

  public void updateRecentMenu() {
    final List<String> recentProjects = getRecentProjectPaths();

    this.openRecentMenu.clear();
    for (final String filePath : recentProjects) {
      final Path file = Paths.getPath(filePath);
      final String fileName = Paths.getFileName(file);
      final String path = file.getParent().toString();
      this.openRecentMenu.addMenuItem("default", fileName + " - " + path, "layout_add",
        () -> openProject(file));
    }
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
