package com.revolsys.swing.parallel;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import javax.swing.Timer;

import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jeometry.common.awt.WebColors;

import com.revolsys.beans.PropertyChangeSupport;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.TabbedPane;
import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.table.AbstractTableModel;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.highlighter.ColorHighlighter;

public class BackgroundTaskTableModel extends AbstractTableModel implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private static final List<String> COLUMN_TITLES = Arrays.asList("Thread", "Title", "Message",
    "Time (s)", "Status");

  private static void addCountLabel(final JToolBar toolBar, final BackgroundTaskTableModel model,
    final String title, final String propertyName, final Color background, final Color foreground) {
    toolBar.add(new JLabel(title));
    final JTextField label = SwingUtil.newTextField(4);
    label.setEditable(false);
    label.setBackground(background);
    label.setForeground(foreground);
    label.setMaximumSize(new Dimension(50, 30));
    label.setHorizontalAlignment(SwingConstants.RIGHT);
    model.addPropertyChangeListener(propertyName, e -> {
      final Integer count = (Integer)e.getNewValue();
      label.setText(count.toString());
    });
    toolBar.add(label);
  }

  private static void addHighlighter(final BaseJTable table, final BackgroundTaskTableModel model,
    final StateValue state, final Color background, final Color foreground) {
    final HighlightPredicate predicate = (renderer, adapter) -> {
      final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
      final BackgroundTask task = model.tasks.get(rowIndex);
      return task.getTaskStatus() == state;
    };
    final ColorHighlighter highlighter = new ColorHighlighter(predicate, background, foreground,
      foreground, background);
    table.addHighlighter(highlighter);
  }

  public static int addNewTabPanel(final TabbedPane tabs) {
    final TablePanel panel = newPanel();
    final BackgroundTaskTableModel tableModel = panel.getTableModel();
    final BackgroundTaskTabLabel tabLabel = new BackgroundTaskTabLabel(tabs, tableModel);

    final int tabIndex = tabs.getTabCount();
    tabs.addTab(null, BackgroundTaskTabLabel.STATIC, panel);

    tabs.setTabComponentAt(tabIndex, tabLabel);
    tabs.setSelectedIndex(tabIndex);

    return tabIndex;
  }

  public static TablePanel newPanel() {
    final BaseJTable table = newTaskTable();
    final TablePanel panel = new TablePanel(table);
    final BackgroundTaskTableModel model = table.getTableModel();
    final JToolBar toolBar = panel.getToolBar();
    addCountLabel(toolBar, model, "Pending", "pendingCount", WebColors.Pink, WebColors.DarkRed);
    addCountLabel(toolBar, model, "Running", "runningCount", WebColors.Ivory, WebColors.DarkOrange);
    addCountLabel(toolBar, model, "Done", "doneCount", WebColors.HoneyDew, WebColors.Green);
    return panel;
  }

  private static BaseJTable newTaskTable() {
    final BackgroundTaskTableModel model = new BackgroundTaskTableModel();
    final BaseJTable table = new BaseJTable(model);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    table.setAutoCreateColumnsFromModel(false);

    for (int i = 0; i < model.getColumnCount(); i++) {
      final TableColumnExt column = table.getColumnExt(i);
      if (i == 1 || i == 2) {
        column.setMinWidth(200);
        column.setPreferredWidth(300);
      } else {
        column.setMinWidth(70);
        column.setPreferredWidth(70);
        column.setMaxWidth(70);
      }
    }
    addHighlighter(table, model, StateValue.PENDING, WebColors.Pink, WebColors.DarkRed);
    addHighlighter(table, model, StateValue.STARTED, WebColors.Ivory, WebColors.DarkOrange);
    addHighlighter(table, model, StateValue.DONE, WebColors.HoneyDew, WebColors.Green);

    return table;
  }

  private final Timer timer = new Timer(500, e -> {
    if (this.runningCount > 0) {
      final BaseJTable table = getTable();
      if (table != null) {
        table.repaint();
      }
    }
  });

  private transient List<BackgroundTask> tasks = new ArrayList<>();

  private final transient Map<SwingWorker<?, ?>, BackgroundTask> taskBySwingWorker = new LinkedHashMap<>();

  private final transient PropertyChangeListener taskListener;

  private final transient PropertyChangeListener taskStatusChangeListener;

  private int pendingCount;

  private int doneCount;

  private int runningCount;

  public BackgroundTaskTableModel() {
    final PropertyChangeSupport propertyChangeSupport = Invoke.getPropertyChangeSupport();
    propertyChangeSupport.addPropertyChangeListener(this);
    this.taskListener = BackgroundTaskManager.addTaskListener(task -> Invoke.later(() -> {
      if (!this.tasks.contains(task)) {
        this.tasks.add(task);
        updateCounts();
        fireTableDataChanged();
      }
    }));
    this.taskStatusChangeListener = BackgroundTaskManager
      .addTaskStatusChangedListener(() -> Invoke.later(() -> {
        updateCounts();
        fireTableDataChanged();
      }));
  }

  private void addNewWorkers(final List<SwingWorker<?, ?>> workers) {
    for (final SwingWorker<?, ?> worker : workers) {
      if (!worker.isDone() && !this.taskBySwingWorker.containsKey(worker)) {
        BackgroundTask task;
        if (worker instanceof BackgroundTask) {
          task = (BackgroundTask)worker;
        } else {
          task = new SwingWorkerBackgroundTask(worker);
        }
        this.taskBySwingWorker.put(worker, task);
        this.tasks.add(task);
      }
    }
  }

  public void clearDoneTasks() {
    for (final Iterator<BackgroundTask> iterator = this.tasks.iterator(); iterator.hasNext();) {
      final BackgroundTask task = iterator.next();
      if (task.getTaskStatus() == StateValue.DONE) {
        iterator.remove();
      }
    }
    updateCounts();
    fireTableDataChanged();
  }

  @Override
  public void dispose() {
    super.dispose();
    Invoke.getPropertyChangeSupport().removePropertyChangeListener(this);
    BackgroundTaskManager.removeListener(this.taskListener);
    BackgroundTaskManager.removeListener(this.taskStatusChangeListener);
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    if (columnIndex == 4) {
      return StateValue.class;
    } else if (columnIndex == 3) {
      return Long.class;
    } else {
      return String.class;
    }
  }

  @Override
  public int getColumnCount() {
    return COLUMN_TITLES.size();
  }

  @Override
  public String getColumnName(final int columnIndex) {
    return COLUMN_TITLES.get(columnIndex);
  }

  public int getDoneCount() {
    return this.doneCount;
  }

  @Override
  public BaseJPopupMenu getMenu(final int rowIndex, final int columnIndex) {
    if (rowIndex >= 0 && rowIndex < this.tasks.size()) {
      final BackgroundTask backgroundTask = this.tasks.get(rowIndex);
      if (backgroundTask != null) {
        final MenuFactory menu = backgroundTask.getMenu();
        if (menu != null) {
          return menu.newJPopupMenu();
        }
      }
    }
    return null;
  }

  public int getPendingCount() {
    return this.pendingCount;
  }

  @Override
  public int getRowCount() {
    return this.tasks.size();
  }

  public int getRunningCount() {
    return this.runningCount;
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    if (rowIndex < getRowCount()) {
      final BackgroundTask task = this.tasks.get(rowIndex);
      if (task != null) {
        if (columnIndex == 0) {
          if (task.isTaskClosed()) {
            updateCounts();
            this.tasks.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
          } else {
            return task.getTaskThreadName();
          }
        } else if (columnIndex == 1) {
          return task.getTaskTitle();
        } else if (columnIndex == 2) {
          return task.getTaskMessage();
        } else if (columnIndex == 3) {
          final long taskTime = task.getTaskTime();
          if (taskTime == -1) {
            return null;
          } else {
            return taskTime / 1000;
          }
        } else {
          return task.getTaskStatus();
        }
      }
    }
    return "-";
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    return false;
  }

  public boolean isHasDoneTasks() {
    return this.doneCount > 0;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    Invoke.later(() -> {
      final String propertyName = event.getPropertyName();
      if (propertyName.equals("workers")) {
        final List<SwingWorker<?, ?>> workers = (List<SwingWorker<?, ?>>)event.getNewValue();
        removeDoneWorkers(workers);
        addNewWorkers(workers);
        updateCounts();
        fireTableDataChanged();
      }
    });
  }

  private void removeDoneWorkers(final List<SwingWorker<?, ?>> workers) {
    for (final Iterator<Entry<SwingWorker<?, ?>, BackgroundTask>> iterator = this.taskBySwingWorker
      .entrySet()
      .iterator(); iterator.hasNext();) {
      final Entry<SwingWorker<?, ?>, BackgroundTask> entry = iterator.next();
      final SwingWorker<?, ?> worker = entry.getKey();
      if (worker.isDone() || !workers.contains(worker)) {
        final BackgroundTask task = entry.getValue();
        iterator.remove();
        this.tasks.remove(task);
      }
    }
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    throw new UnsupportedOperationException();
  }

  private void updateCounts() {
    this.pendingCount = 0;
    this.runningCount = 0;
    this.doneCount = 0;
    for (final BackgroundTask task : this.tasks) {
      final StateValue taskStatus = task.getTaskStatus();
      if (taskStatus == StateValue.PENDING) {
        this.pendingCount++;
      } else if (taskStatus == StateValue.STARTED) {
        this.runningCount++;
      } else if (!task.isTaskClosed()) {
        this.doneCount++;
      }
    }
    if (this.runningCount == 0) {
      this.timer.stop();
    } else {
      this.timer.start();
    }
    firePropertyChange("pendingCount", -1, this.pendingCount);
    firePropertyChange("runningCount", -1, this.runningCount);
    firePropertyChange("doneCount", -1, this.doneCount);

  }
}
