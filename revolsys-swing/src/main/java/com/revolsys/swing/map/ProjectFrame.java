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
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.set.Sets;
import com.revolsys.connection.file.FileConnectionManager;
import com.revolsys.connection.file.FolderConnectionRegistry;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.util.RectangleUtil;
import com.revolsys.io.FileUtil;
import com.revolsys.io.file.Paths;
import com.revolsys.io.filter.FileNameExtensionFilter;
import com.revolsys.process.JavaProcess;
import com.revolsys.raster.GeoreferencedImageWriterFactory;
import com.revolsys.record.io.RecordStoreConnectionManager;
import com.revolsys.record.io.RecordStoreConnectionRegistry;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.swing.Dialogs;
import com.revolsys.swing.EventQueue;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.TabbedPane;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.component.BaseFrame;
import com.revolsys.swing.component.DnDTabbedPane;
import com.revolsys.swing.component.TabClosableTitle;
import com.revolsys.swing.io.SwingIo;
import com.revolsys.swing.logging.LoggingTableModel;
import com.revolsys.swing.map.layer.AbstractLayer.PanelComponentHolder;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.raster.SaveAsGeoreferencedImage;
import com.revolsys.swing.map.overlay.MeasureOverlay;
import com.revolsys.swing.map.print.SinglePage;
import com.revolsys.swing.map.view.pdf.SaveAsPdf;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.BackgroundTaskTableModel;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.parallel.SwingWorkerProgressBar;
import com.revolsys.swing.preferences.PreferencesDialog;
import com.revolsys.swing.scripting.ScriptRunner;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.node.ListTreeNode;
import com.revolsys.swing.tree.node.WebServiceConnectionTrees;
import com.revolsys.swing.tree.node.file.FolderConnectionsTrees;
import com.revolsys.swing.tree.node.file.PathTreeNode;
import com.revolsys.swing.tree.node.layer.ProjectTreeNode;
import com.revolsys.swing.tree.node.record.RecordStoreConnectionTrees;
import com.revolsys.util.OS;
import com.revolsys.util.PreferenceKey;
import com.revolsys.util.Preferences;
import com.revolsys.util.PreferencesUtil;
import com.revolsys.util.Property;
import com.revolsys.webservice.WebServiceConnectionManager;
import com.revolsys.webservice.WebServiceConnectionRegistry;

public class ProjectFrame extends BaseFrame {
  private static final String PREFERENCE_PROJECT = "/com/revolsys/gis/project";

  private static final PreferenceKey PREFERENCE_PROJECT_DIRECTORY = new PreferenceKey(
    PREFERENCE_PROJECT, "directory");

  private static final PreferenceKey PREFERENCE_RECENT_PROJECT = new PreferenceKey(
    PREFERENCE_PROJECT, "recentProject");

  private static final PreferenceKey PREFERENCE_RECENT_PROJECTS = new PreferenceKey(
    PREFERENCE_PROJECT, "recentProjects", DataTypes.LIST, new ArrayList<String>());

  private static final String BOTTOM_TAB = "INTERNAL_bottomTab";

  private static final String BOTTOM_TAB_LISTENER = "INTERNAL_bottomTabListener";

  public static final String PROJECT_FRAME = "INTERNAL_projectFrame";

  public static final String SAVE_CHANGES_KEY = "Save Changes";

  public static final String SAVE_PROJECT_KEY = "Save Project";

  private static final long serialVersionUID = 1L;

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

  public static void init() {
  }

  private Set<String> bottomTabLayerPaths = new LinkedHashSet<>();

  private DnDTabbedPane bottomTabs = new DnDTabbedPane();

  private BaseTree catalogTree;

  private boolean exitOnClose = true;

  private final String frameTitle;

  private JSplitPane leftRightSplit;

  private final ToolBar leftToolBar = new ToolBar();

  private TabbedPane leftTabs = new TabbedPane();

  private MapPanel mapPanel;

  private final JMenu openRecentMenu = new JMenu("Open Recent Project");

  private Project project = newEmptyProject();

  private BaseTree tocTree;

  private JSplitPane topBottomSplit;

  private Path projectPath;

  private String applicationId = "com.revolsys.gis";

  private Preferences preferences = new Preferences(this.applicationId);

  private final List<File> initialFiles = new ArrayList<>();

  public ProjectFrame(final String title, final Path projectPath) {
    this(title, projectPath, true);
  }

  public ProjectFrame(final String title, final Path projectPath, final boolean initialize) {
    super(title, false);
    this.frameTitle = title;
    this.projectPath = projectPath;
    this.preferences = new Preferences(this.applicationId);
    if (initialize) {
      initUi();
      loadProject();
    }
  }

  public ProjectFrame(final String applicationId, final String title) {
    this(applicationId, title, Collections.emptyList());
  }

  public ProjectFrame(final String applicationId, final String title,
    final Collection<File> initialFiles) {
    super(title, false);
    this.frameTitle = title;
    this.applicationId = applicationId;
    this.preferences.setApplicationId(applicationId);

    File initialProjectFile = null;
    if (initialFiles.size() == 1) {
      final File initialFile = initialFiles.iterator().next();
      if (FileUtil.getFileNameExtension(initialFile).equals("rgmap")) {
        if (initialFile.exists()) {
          initialProjectFile = initialFile;
        }
      }
    }
    this.initialFiles.addAll(initialFiles);

    if (initialProjectFile == null) {
      final String recentProjectPath = this.preferences.getValue(PREFERENCE_RECENT_PROJECT);
      this.projectPath = Paths.getPath(recentProjectPath);
    } else {
      this.projectPath = initialProjectFile.toPath();
      this.preferences.setValue(PREFERENCE_RECENT_PROJECT, this.projectPath);
    }
    initUi();
    loadProject();
  }

  private void actionProjectExport() {
    final String baseName = this.project.getName();
    SwingIo.exportToFile("Export Project", "com.revolsys.swing.map.project.export",
      GeoreferencedImageWriterFactory.class, "pdf", baseName, file -> {
        if ("pdf".equals(FileUtil.getFileNameExtension(file))) {
          SaveAsPdf.save(file, this.project);
        } else {
          SaveAsGeoreferencedImage.save(file, this.project);
        }
      });
  }

  private void actionProjectNew() {
    if (this.project != null && this.project.saveWithPrompt()) {
      this.project.reset();
      super.setTitle("NEW - " + this.frameTitle);
    }
  }

  private void actionProjectNewFromTemplate() {
    if (this.project != null) {
      this.project.actionImportProject("New Project from Template", true);
    }

  }

  private void actionProjectOpen() {
    if (this.project != null && this.project.saveWithPrompt()) {

      final JFileChooser fileChooser = SwingUtil.newFileChooser("Open Project", this.preferences,
        PREFERENCE_PROJECT_DIRECTORY);

      final FileNameExtensionFilter filter = new FileNameExtensionFilter("Project (*.rgmap)",
        "rgmap");
      fileChooser.setAcceptAllFileFilterUsed(true);
      fileChooser.addChoosableFileFilter(filter);
      fileChooser.setFileFilter(filter);
      if (OS.isMac()) {
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      } else {
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      }
      final int returnVal = Dialogs.showOpenDialog(fileChooser);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        final File projectDirectory = fileChooser.getSelectedFile();
        final File parentDirectory = projectDirectory.getParentFile();
        this.preferences.setValue(PREFERENCE_PROJECT_DIRECTORY, parentDirectory);
        openProject(projectDirectory.toPath());
      }
    }
  }

  private void actionProjectSave() {
    if (this.project.isSaved()) {
      this.project.saveAllSettings();
    } else {
      actionProjectSaveAs();
    }
  }

  private void actionProjectSaveAs() {
    final Path path = this.project.saveAllSettingsAs();
    if (path != null) {
      addToRecentProjects(path);
      Invoke.later(() -> {
        final Project project = getProject();
        setTitle(project.getName() + " - " + getFrameTitle());
      });
    }
  }

  private void actionRunScript() {
    final File logDirectory = getLogDirectory();
    final JavaProcess javaProcess = newJavaProcess();
    ScriptRunner.runScriptProcess(this, logDirectory, javaProcess);
  }

  public void addBottomTab(final ProjectFramePanel panel, final MapEx config) {
    if (SwingUtilities.isEventDispatchThread()) {
      final TabbedPane tabs = getBottomTabs();
      final boolean selectTab = config.getBoolean("selectTab", true);
      final Object tableView = panel.getProperty(BOTTOM_TAB);
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

          panel.setPropertyWeak(BOTTOM_TAB, panelComponent);

          final String layerPath = panel.getPath();
          final Runnable closeAction = () -> removeBottomTab(panel);
          synchronized (this.bottomTabLayerPaths) {
            this.bottomTabLayerPaths.add(layerPath);
          }
          final String name = panel.getName();
          final Icon icon = panel.getIcon();
          final TabClosableTitle tab = tabs.addClosableTab(name, icon, panelComponent, closeAction);
          tab.setMenu(panel);

          if (selectTab) {
            tabs.setSelectedIndex(tabIndex);
          }

          panel.setPropertyWeak(BOTTOM_TAB_LISTENER, Arrays.asList(//
            EventQueue.addPropertyChange(panel, "name", () -> {
              final int index = tabs.indexOfComponent(panelComponent);
              if (index != -1) {
                final String newName = panel.getName();
                tabs.setTitleAt(index, newName);
              }
            }), //
            EventQueue.addPropertyChange(panel, "icon", () -> {
              final int index = tabs.indexOfComponent(panelComponent);
              if (index != -1) {
                final Icon newName = panel.getIcon();
                tabs.setIconAt(index, newName);
              }
            })//
          ));
        }
      } else {
        Component panelComponent = component;
        if (component instanceof PanelComponentHolder) {
          final PanelComponentHolder holder = (PanelComponentHolder)component;
          panelComponent = holder.getPanel();
        }
        panel.activatePanelComponent(panelComponent, config);
        if (selectTab) {
          tabs.setSelectedComponent(component);
        }
      }
    } else {
      Invoke.later(() -> addBottomTab(panel, config));
    }
  }

  protected void addBottomTabs(final DnDTabbedPane bottomTabs) {
    addBottomTabsTasks();
    LoggingTableModel.addNewTabPane(bottomTabs);
  }

  protected void addBottomTabsTasks() {
    final int tabIndex = BackgroundTaskTableModel.addNewTabPanel(this.bottomTabs);

    final SwingWorkerProgressBar progressBar = this.mapPanel.getProgressBar();
    final JButton viewTasksAction = RunnableAction.newButton(null, "View Running Tasks",
      Icons.getIcon("time_go"), () -> this.bottomTabs.setSelectedIndex(tabIndex));
    viewTasksAction.setBorderPainted(false);
    viewTasksAction.setBorder(null);
    progressBar.add(viewTasksAction, BorderLayout.EAST);
  }

  protected void addMenu(final JMenuBar menuBar, final MenuFactory menuFactory) {
    if (menuFactory != null) {
      final JMenu menu = menuFactory.newComponent();
      menuBar.add(menu, menuBar.getMenuCount() - 1);
    }
  }

  protected void addMenuItemAndToolBarButton(final MenuFactory menu, final ToolBar toolBar,
    final String groupName, final String name, final String iconName, final Runnable runnable) {
    toolBar.addButton(groupName, name, iconName, runnable);

    menu.addMenuItemTitleIcon(groupName, name, iconName, runnable);
  }

  protected void addMenuItemAndToolBarButton(final MenuFactory menu, final ToolBar toolBar,
    final String groupName, final String name, final String iconName, final Runnable runnable,
    final int acceleratorControlKey) {
    toolBar.addButton(groupName, name, iconName, runnable);

    final RunnableAction menuItem = menu.addMenuItemTitleIcon(groupName, name, iconName, runnable);
    menuItem.setAcceleratorControlKey(acceleratorControlKey);
  }

  private void addToRecentProjects(final Path projectPath) {
    final List<String> recentProjects = getRecentProjectPaths();
    final String filePath = projectPath.toAbsolutePath().toString();
    recentProjects.remove(filePath);
    recentProjects.add(0, filePath);
    while (recentProjects.size() > 10) {
      recentProjects.remove(recentProjects.size() - 1);
    }
    this.preferences.setValue(PREFERENCE_RECENT_PROJECTS, recentProjects);
    this.preferences.setValue(PREFERENCE_RECENT_PROJECT, filePath);
    updateRecentMenu();
  }

  @Override
  protected void close() {
    Property.removeAllListeners(this);
    setVisible(false);
    super.close();
    setRootPane(new JRootPane());
    removeAll();
    setMenuBar(null);
    if (this.project != null) {
      this.project.setProperty(PROJECT_FRAME, null);
      Project.clearProject(this.project);
    }
    if (this.bottomTabs != null) {
      for (final ContainerListener listener : this.bottomTabs.getContainerListeners()) {
        this.bottomTabs.removeContainerListener(listener);
      }
    }
    if (this.catalogTree != null) {
      this.catalogTree.setRoot(null);
    }
    if (this.mapPanel != null) {
      this.mapPanel.destroy();
    }
    if (this.project != null) {
      this.project.getRecordStores().remove();
      this.project.getFolderConnections().remove();
      this.project.getWebServices().remove();
      if (Project.get() == this.project) {
        Project.set(null);
      }
      this.project.delete();
    }
    if (this.tocTree != null) {
      this.tocTree.setRoot(null);
    }
    this.bottomTabs = null;
    this.catalogTree = null;
    this.leftRightSplit = null;
    this.leftTabs = null;
    this.mapPanel = null;
    this.project = null;
    this.tocTree = null;
    this.topBottomSplit = null;

    final ActionMap actionMap = getRootPane().getActionMap();
    actionMap.put(SAVE_PROJECT_KEY, null);
    actionMap.put(SAVE_CHANGES_KEY, null);
  }

  public void exit() {
    final Project project = getProject();
    if (project != null && project.saveWithPrompt()) {
      final Window[] windows = Window.getOwnerlessWindows();
      for (final Window window : windows) {
        SwingUtil.dispose(window);

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

  public TabbedPane getBottomTabs() {
    return this.bottomTabs;
  }

  public double getControlWidth() {
    return 0.20;
  }

  protected BoundingBox getDefaultBoundingBox() {
    return BoundingBox.empty();
  }

  public String getFrameTitle() {
    return this.frameTitle;
  }

  public TabbedPane getLeftTabs() {
    return this.leftTabs;
  }

  public File getLogDirectory() {
    return FileUtil.getDirectory("log");
  }

  public MapPanel getMapPanel() {
    return this.mapPanel;
  }

  public Preferences getPreferences() {
    return this.preferences;
  }

  public Project getProject() {
    return this.project;
  }

  public Path getProjectPath() {
    return this.projectPath;
  }

  private List<String> getRecentProjectPaths() {
    final List<String> recentProjects = this.preferences.getValue(PREFERENCE_RECENT_PROJECTS);
    for (int i = 0; i < recentProjects.size();) {
      final String filePath = recentProjects.get(i);
      final File file = FileUtil.getFile(filePath);
      if (file.exists()) {
        i++;
      } else {
        recentProjects.remove(i);
      }
    }
    this.preferences.setValue(PREFERENCE_RECENT_PROJECTS, recentProjects);
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

  protected void initLeftToolbar(final ToolBar toolBar) {

  }

  @Override
  protected void initUi() {
    setMinimumSize(new Dimension(600, 500));

    final JRootPane rootPane = getRootPane();

    addSaveActions(rootPane, this.project);

    final BoundingBox defaultBoundingBox = getDefaultBoundingBox();
    this.project.setViewBoundingBoxAndGeometryFactory(defaultBoundingBox);
    Project.set(this.project);
    this.project.setPropertyWeak(PROJECT_FRAME, this);
    setConnectionRegistries();

    this.mapPanel = newMapPanel();

    this.leftTabs.setMinimumSize(new Dimension(100, 300));
    this.leftTabs.setPreferredSize(new Dimension(300, 700));

    final JPanel leftPanel = new JPanel(new BorderLayout());
    leftPanel.add(this.leftToolBar, BorderLayout.NORTH);
    leftPanel.add(this.leftTabs, BorderLayout.CENTER);

    this.mapPanel.setMinimumSize(new Dimension(300, 300));
    this.mapPanel.setPreferredSize(new Dimension(700, 700));
    this.leftRightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, this.mapPanel);

    this.leftRightSplit.setBorder(BorderFactory.createEmptyBorder());
    this.bottomTabs.setBorder(BorderFactory.createEmptyBorder());
    this.bottomTabs.setPreferredSize(new Dimension(700, 200));
    final ContainerListener listener = new ContainerAdapter() {
      @Override
      public void componentRemoved(final ContainerEvent e) {
        final Component eventComponent = e.getChild();
        if (eventComponent instanceof ProjectFramePanel) {
          final ProjectFramePanel panel = (ProjectFramePanel)eventComponent;
          panel.setProperty(BOTTOM_TAB, null);
        }
      }
    };
    this.bottomTabs.addContainerListener(listener);

    this.topBottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.leftRightSplit,
      this.bottomTabs);
    this.bottomTabs.setMinimumSize(new Dimension(600, 100));

    this.topBottomSplit.setResizeWeight(1);

    add(this.topBottomSplit, BorderLayout.CENTER);

    initLeftToolbar(this.leftToolBar);
    newTabLeftTableOfContents();
    newTabLeftCatalogPanel();

    addBottomTabs(this.bottomTabs);
    setBounds((Object)null, false);

    super.initUi();
  }

  protected final void loadProject() {
    Property.addListener(this.project, "name", e -> {
      final Object source = e.getSource();
      if (source instanceof Layer) {
        final Layer layer = (Layer)source;
        final String oldPath = (String)e.getOldValue();
        synchronized (this.bottomTabLayerPaths) {
          if (this.bottomTabLayerPaths.remove(oldPath)) {
            final String newPath = layer.getPath();
            this.bottomTabLayerPaths.add(newPath);
          }
        }
      }
    });
    final Path projectPath = getProjectPath();
    if (projectPath == null) {
      final Viewport2D viewport = this.mapPanel.getViewport();
      final GeometryFactory geometryFactory = GeometryFactory.worldMercator();
      final BoundingBox initialBoundingBox = geometryFactory.getAreaBoundingBox();
      this.project.setViewBoundingBoxAndGeometryFactory(initialBoundingBox);
      viewport.setBoundingBoxAndGeometryFactory(initialBoundingBox);
      viewport.setInitialized(true);
      getMapPanel().setInitializing(false);
    } else {
      Invoke.background("Load Project: " + projectPath, () -> {
        loadProject(projectPath);
        this.project.openFiles(this.initialFiles);
        getMapPanel().setInitializing(false);
        loadProjectAfter();
      });
    }
  }

  protected void loadProject(final Path projectPath) {
    final PathResource resource = new PathResource(projectPath);
    this.project.readProject(this.project, resource);
    Invoke.later(() -> setTitle(this.project.getName() + " - " + this.frameTitle));

    final Object frameBoundsObject = this.project.getProperty("frameBounds");
    setBounds(frameBoundsObject, true);
    setVisible(true);

    final MapPanel mapPanel = getMapPanel();
    final BoundingBox initialBoundingBox = this.project.getInitialBoundingBox();
    final Viewport2D viewport = mapPanel.getViewport();
    if (!RectangleUtil.isEmpty(initialBoundingBox)) {
      this.project.setViewBoundingBoxAndGeometryFactory(initialBoundingBox);
      viewport.setBoundingBoxAndGeometryFactory(initialBoundingBox);
    }
    viewport.setInitialized(true);
  }

  protected void loadProjectAfter() {
    this.bottomTabLayerPaths = Sets
      .newLinkedHash(this.project.<Collection<String>> getProperty("bottomTabLayerPaths"));
    // TODO cleanup on save
    this.project.setProperty("bottomTabLayerPaths", this.bottomTabLayerPaths);
    for (final Iterator<String> iterator = this.bottomTabLayerPaths.iterator(); iterator
      .hasNext();) {
      final String layerPath = iterator.next();
      final Layer layer = this.project.getLayerByPath(layerPath);
      if (layer == null) {
        iterator.remove();
      } else {
        Invoke.later(layer::showTableView);
      }
    }
  }

  protected Project newEmptyProject() {
    return new Project();
  }

  public JavaProcess newJavaProcess() {
    return new JavaProcess();
  }

  protected MapPanel newMapPanel() {
    final MapPanel mapPanel = new MapPanel(this, this.preferences, this.project);
    if (OS.isMac()) {
      // Make border on right/bottom to match the JTabbedPane UI on a mac
      mapPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 9, 9));
    }
    return mapPanel;
  }

  @Override
  protected JMenuBar newMenuBar() {
    final JMenuBar menuBar = super.newMenuBar();
    addMenu(menuBar, newMenuFile());

    final MenuFactory tools = newMenuTools();

    if (OS.isWindows()) {
      tools.addMenuItem("options", "Options...", "Options...", (String)null, () -> {
        new PreferencesDialog().showPanel();
      });
    }
    addMenu(menuBar, tools);
    return menuBar;
  }

  protected MenuFactory newMenuFile() {
    final MenuFactory file = new MenuFactory("File");

    file.addMenuItemTitleIcon("projectOpen", "New Project", "layout:add", this::actionProjectNew)
      .setAcceleratorControlKey(KeyEvent.VK_N);

    file
      .addMenuItemTitleIcon("projectOpen", "New Project...", "layout:add",
        this::actionProjectNewFromTemplate)
      .setAcceleratorShiftControlKey(KeyEvent.VK_N);

    file
      .addMenuItemTitleIcon("projectOpen", "Open Project...", "layout:add", this::actionProjectOpen)
      .setAcceleratorControlKey(KeyEvent.VK_O);

    file.addComponent("projectOpen", this.openRecentMenu);
    updateRecentMenu();

    addMenuItemAndToolBarButton(file, this.leftToolBar, "projectSave", "Save Project",
      "layout:save", this::actionProjectSave, KeyEvent.VK_S);

    file
      .addMenuItemTitleIcon("projectSave", "Save Project As...", "layout:save",
        this::actionProjectSaveAs)
      .setAcceleratorShiftControlKey(KeyEvent.VK_S);

    addMenuItemAndToolBarButton(file, this.leftToolBar, "projectSave", "Export Map...", "map:save",
      this::actionProjectExport);

    addMenuItemAndToolBarButton(file, this.leftToolBar, "print", "Print", "printer",
      SinglePage::print, KeyEvent.VK_P);

    if (OS.isWindows()) {
      file.addMenuItemTitleIcon("exit", "Exit", null, this::exit)
        .setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
    } else if (OS.isUnix()) {
      file.addMenuItemTitleIcon("exit", "Exit", null, this::exit)
        .setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
    }

    return file;
  }

  protected MenuFactory newMenuTools() {
    final MenuFactory tools = new MenuFactory("Tools");
    final MapPanel map = getMapPanel();

    final MeasureOverlay measureOverlay = map.getMapOverlay(MeasureOverlay.class);
    measureOverlay.initMenuTools(tools);

    tools.addMenuItem("script", "Run Script...", "script_go", this::actionRunScript);
    return tools;
  }

  protected void newTabLeftCatalogPanel() {
    final BaseTreeNode recordStores = RecordStoreConnectionTrees
      .newRecordStoreConnectionsTreeNode();

    final BaseTreeNode fileSystems = PathTreeNode.newFileSystemsTreeNode();

    final BaseTreeNode folderConnections = FolderConnectionsTrees.newFolderConnectionsTreeNode();

    final BaseTreeNode webServices = WebServiceConnectionTrees.newWebServiceConnectionsTreeNode();

    final ListTreeNode root = new ListTreeNode("/", recordStores, fileSystems, folderConnections,
      webServices);

    final BaseTree tree = new BaseTree(root);
    tree.setRootVisible(false);

    recordStores.expandChildren();
    fileSystems.expand();
    folderConnections.expandChildren();
    webServices.expandChildren();

    this.catalogTree = tree;

    final Icon icon = Icons.getIconWithBadge("folder", "tree");
    final TabbedPane tabs = this.leftTabs;
    final Component component = this.catalogTree;
    tabs.addTab(icon, "Catalog", component, true);
  }

  protected void newTabLeftTableOfContents() {
    final Project project = getProject();
    this.tocTree = ProjectTreeNode.newTree(project);
    this.leftTabs.addTabIcon("tree_layers", "TOC", this.tocTree, true);
  }

  public void openProject(final Path projectPath) {
    if (Files.exists(projectPath)) {
      this.projectPath = projectPath;
      try {
        addToRecentProjects(projectPath);

        PreferencesUtil.setUserString("com.revolsys.swing.map.project", "directory",
          projectPath.getParent().toString());
        this.project.reset();
        final Runnable task = this::loadProject;
        Invoke.background("Load project", task);
      } catch (final Throwable e) {
        Logs.error(this, "Unable to open project:" + projectPath, e);
      }
    }
  }

  public void removeBottomTab(final ProjectFramePanel panel) {
    Invoke.later(() -> {
      final String layerPath = panel.getPath();
      synchronized (this.bottomTabLayerPaths) {
        this.bottomTabLayerPaths.remove(layerPath);
      }
      final List<PropertyChangeListener> listeners = panel.getProperty(BOTTOM_TAB_LISTENER);
      if (listeners != null) {
        for (final PropertyChangeListener listener : listeners) {
          Property.removeListener(panel, listener);
        }
      }

      final Component component = panel.getProperty(BOTTOM_TAB);
      if (component != null) {
        final JTabbedPane tabs = getBottomTabs();
        if (tabs != null) {
          tabs.remove(component);
        }
        panel.deletePanelComponent(component);
      }
      panel.setProperty(BOTTOM_TAB, null);
      panel.setProperty(BOTTOM_TAB_LISTENER, null);
    });
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

            final Rectangle screenBounds = SwingUtil.getScreenBounds(x, y, width, height);

            width = Math.min(width, screenBounds.width);
            height = Math.min(height, screenBounds.height);
            setSize(width, height);

            if (x < screenBounds.x || x > screenBounds.x + screenBounds.width) {
              x = screenBounds.x;
            } else {
              x = Math.min(x, screenBounds.x + screenBounds.width - width);
            }
            if (y < screenBounds.y || x > screenBounds.y + screenBounds.height) {
              y = screenBounds.y;
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

  private void setConnectionRegistries() {
    final String connectionRegistryName = this.project.getConnectionRegistryName();
    final RecordStoreConnectionManager recordStoreConnectionManager = RecordStoreConnectionManager
      .get();
    recordStoreConnectionManager.removeConnectionRegistry(connectionRegistryName);
    final RecordStoreConnectionRegistry recordStores = this.project.getRecordStores();
    recordStoreConnectionManager.addConnectionRegistry(recordStores);

    final FileConnectionManager fileConnectionManager = FileConnectionManager.get();
    fileConnectionManager.removeConnectionRegistry(connectionRegistryName);
    final FolderConnectionRegistry folderConnections = this.project.getFolderConnections();
    fileConnectionManager.addConnectionRegistry(folderConnections);

    final WebServiceConnectionManager webServiceConnectionManager = WebServiceConnectionManager
      .get();
    webServiceConnectionManager.removeConnectionRegistry(connectionRegistryName);
    final WebServiceConnectionRegistry webServices = this.project.getWebServices();
    webServiceConnectionManager.addConnectionRegistry(webServices);
  }

  public void setExitOnClose(final boolean exitOnClose) {
    this.exitOnClose = exitOnClose;
  }

  protected void setProjectPath(final Path projectPath) {
    this.projectPath = projectPath;
  }

  public void updateRecentMenu() {
    final List<String> recentProjects = getRecentProjectPaths();

    this.openRecentMenu.removeAll();
    for (final String filePath : recentProjects) {
      final Path file = Paths.getPath(filePath);
      if (Paths.exists(file)) {
        final String fileName = Paths.getFileName(file);
        final String path = file.getParent().toString();
        final JMenuItem menuItem = MenuFactory
          .newMenuItem(fileName, path, Icons.getIcon("layout:add"), null, () -> openProject(file))
          .newMenuItem();
        this.openRecentMenu.add(menuItem);
      }
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
