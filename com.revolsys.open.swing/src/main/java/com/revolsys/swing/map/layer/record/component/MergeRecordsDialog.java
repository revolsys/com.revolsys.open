package com.revolsys.swing.map.layer.record.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.ScrollableSizeHint;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.ArrayRecord;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.graph.DataObjectGraph;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.layer.record.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.table.model.MergedRecordsTableModel;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.dataobject.model.DataObjectListTableModel;
import com.revolsys.swing.undo.CreateRecordUndo;
import com.revolsys.swing.undo.DeleteLayerRecordUndo;
import com.revolsys.swing.undo.MultipleUndo;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.CollectionUtil;

public class MergeRecordsDialog extends JDialog implements WindowListener {

  private static final long serialVersionUID = 1L;

  public static void showDialog(final AbstractDataObjectLayer layer) {
    final UndoManager undoManager = MapPanel.get(layer).getUndoManager();
    final MergeRecordsDialog dialog = new MergeRecordsDialog(undoManager, layer);
    dialog.showDialog();
  }

  private JButton okButton;

  private final AbstractDataObjectLayer layer;

  private final Map<DataObject, LayerRecord> mergeableToOiginalRecordMap = new HashMap<DataObject, LayerRecord>();

  private JPanel mergedObjectsPanel;

  private final Set<LayerRecord> replacedOriginalRecords = new LinkedHashSet<LayerRecord>();

  private HashMap<DataObject, Set<LayerRecord>> mergedRecords;

  private final UndoManager undoManager;

  public MergeRecordsDialog(final UndoManager undoManager,
    final AbstractDataObjectLayer layer) {
    super(SwingUtil.getActiveWindow(), "Merge " + layer.getName(),
      ModalityType.APPLICATION_MODAL);
    this.undoManager = undoManager;
    this.layer = layer;
    initDialog();
  }

  public void cancel() {
    if (isVisible()) {
      setVisible(false);
      dispose();
    }
  }

  public void finish() {
    final MultipleUndo multipleUndo = new MultipleUndo();
    for (final DataObject mergedRecord : mergedRecords.keySet()) {
      final CreateRecordUndo createRecordUndo = new CreateRecordUndo(layer,
        mergedRecord);
      multipleUndo.addEdit(createRecordUndo);
    }
    for (final LayerRecord record : replacedOriginalRecords) {
      final DeleteLayerRecordUndo deleteRecordUndo = new DeleteLayerRecordUndo(
        record);
      multipleUndo.addEdit(deleteRecordUndo);
    }
    if (undoManager == null) {
      multipleUndo.redo();
    } else {
      undoManager.addEdit(multipleUndo);
    }
    setVisible(false);
  }

  protected void initDialog() {
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    setMinimumSize(new Dimension(600, 100));
    addWindowListener(this);

    final JXPanel panel = new JXPanel(new BorderLayout());
    panel.setScrollableWidthHint(ScrollableSizeHint.FIT);
    panel.setScrollableHeightHint(ScrollableSizeHint.VERTICAL_STRETCH);
    panel.setOpaque(false);
    add(new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

    mergedObjectsPanel = new JPanel(new VerticalLayout());
    mergedObjectsPanel.setOpaque(false);

    panel.add(mergedObjectsPanel, BorderLayout.CENTER);

    final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    add(buttonsPanel, BorderLayout.SOUTH);

    final JButton cancelButton = InvokeMethodAction.createButton("Cancel",
      this, "cancel");
    buttonsPanel.add(cancelButton);

    this.okButton = InvokeMethodAction.createButton("OK", this, "finish");
    this.okButton.setEnabled(false);
    buttonsPanel.add(this.okButton);

    pack();
    SwingUtil.autoAdjustPosition(this);
  }

  protected void replaceRecord(final DataObject mergedRecord,
    final DataObject record) {
    if (mergedRecord != record) {
      final LayerRecord originalRecord = mergeableToOiginalRecordMap.remove(record);
      if (originalRecord != null) {
        replacedOriginalRecords.add(originalRecord);
      }
    }
  }

  public void run() {
    try {
      final List<LayerRecord> originalRecords = this.layer.getMergeableSelectedRecords();

      String errorMessage = "";
      final DataType geometryType = this.layer.getGeometryType();
      mergedRecords = new HashMap<DataObject, Set<LayerRecord>>();
      if (originalRecords.size() < 2) {
        errorMessage = " at least two records must be selected to merge.";
      } else if (!DataTypes.LINE_STRING.equals(geometryType)) {
        errorMessage = "Merging " + geometryType + " not currently supported";
      } else {
        final DataObjectGraph graph = new DataObjectGraph();
        for (final LayerRecord originalRecord : originalRecords) {
          final DataObject mergeableRecord = new ArrayRecord(originalRecord);
          mergeableToOiginalRecordMap.put(mergeableRecord, originalRecord);
          graph.addEdge(mergeableRecord);
        }
        for (final Node<DataObject> node : graph.nodes()) {
          if (node != null) {
            final List<Edge<DataObject>> edges = node.getEdges();
            if (edges.size() == 2) {
              final Edge<DataObject> edge1 = edges.get(0);
              final DataObject record1 = edge1.getObject();
              final Edge<DataObject> edge2 = edges.get(1);
              final DataObject record2 = edge2.getObject();
              if (record1 != record2) {
                final DataObject mergedRecord = layer.getMergedRecord(node,
                  record1, record2);

                graph.addEdge(mergedRecord);
                edge1.remove();
                edge2.remove();

                final Set<LayerRecord> sourceRecords = new LinkedHashSet<LayerRecord>();
                // TODO verify orientation to ensure they are in the correct
                // order
                // and see if they are reversed
                CollectionUtil.addIfNotNull(sourceRecords,
                  mergeableToOiginalRecordMap.get(record1));
                CollectionUtil.addAllIfNotNull(sourceRecords,
                  mergedRecords.remove(record1));
                CollectionUtil.addIfNotNull(sourceRecords,
                  mergeableToOiginalRecordMap.get(record2));
                CollectionUtil.addAllIfNotNull(sourceRecords,
                  mergedRecords.remove(record2));
                mergedRecords.put(mergedRecord, sourceRecords);
                replaceRecord(mergedRecord, record1);
                replaceRecord(mergedRecord, record2);
              }
            }
          }
        }
      }
      Invoke.later(this, "setMergedRecords", errorMessage, mergedRecords);

    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error("Error " + this, e);
    } finally {
    }
  }

  public void setMergedRecord(final int i, final DataObject mergedObject,
    final Collection<LayerRecord> objects) {

    this.okButton.setEnabled(true);

    final TablePanel tablePanel = MergedRecordsTableModel.createPanel(layer,
      mergedObject, objects);

    final JPanel panel = new JPanel(new VerticalLayout());
    panel.add(tablePanel);
    SwingUtil.setTitledBorder(panel, "Merged " + objects.size()
      + " Source Records");
    mergedObjectsPanel.add(panel);

  }

  public void setMergedRecords(String errorMessage,
    final Map<DataObject, Set<LayerRecord>> mergedRecords) {
    final Set<DataObject> unMergeableRecords = new HashSet<DataObject>(
      mergeableToOiginalRecordMap.keySet());
    unMergeableRecords.removeAll(mergedRecords.keySet());
    if (!mergedRecords.isEmpty()) {
      int i = 0;

      for (final Entry<DataObject, Set<LayerRecord>> mergedEntry : mergedRecords.entrySet()) {
        final DataObject mergedObject = mergedEntry.getKey();
        final Set<LayerRecord> originalObjects = mergedEntry.getValue();
        setMergedRecord(i, mergedObject, originalObjects);
        i++;
      }
    }
    if (!unMergeableRecords.isEmpty()) {
      final Set<LayerRecord> records = new LinkedHashSet<LayerRecord>();
      for (final DataObject record : unMergeableRecords) {
        final LayerRecord originalRecord = mergeableToOiginalRecordMap.get(record);
        if (originalRecord != null) {
          records.add(originalRecord);
        }
      }
      final TablePanel tablePanel = DataObjectListTableModel.createPanel(layer,
        records);
      final DataObjectListTableModel tableModel = tablePanel.getTableModel();
      tableModel.setEditable(false);
      tablePanel.setPreferredSize(new Dimension(100,
        50 + unMergeableRecords.size() * 22));

      final JPanel panel = new JPanel(new BorderLayout());
      if (!StringUtils.hasText(errorMessage)) {
        errorMessage = "The following records could not be merged and will not be modified.";
      }
      final JLabel unMergeLabel = new JLabel("<html><p style=\"color:red\">"
        + errorMessage + "</p></html>");
      panel.add(unMergeLabel, BorderLayout.NORTH);
      panel.add(tablePanel, BorderLayout.SOUTH);
      SwingUtil.setTitledBorder(panel, unMergeableRecords.size()
        + " Un-Mergable Records");

      mergedObjectsPanel.add(panel);
    }
    pack();
    SwingUtil.autoAdjustPosition(this);
  }

  private void showDialog() {
    Invoke.background(toString(), this, "run");
    pack();
    setVisible(true);
  }

  @Override
  public String toString() {
    return getTitle();
  }

  @Override
  public void windowActivated(final WindowEvent e) {
  }

  @Override
  public void windowClosed(final WindowEvent e) {
  }

  @Override
  public void windowClosing(final WindowEvent e) {
    cancel();
  }

  @Override
  public void windowDeactivated(final WindowEvent e) {
  }

  @Override
  public void windowDeiconified(final WindowEvent e) {
  }

  @Override
  public void windowIconified(final WindowEvent e) {
  }

  @Override
  public void windowOpened(final WindowEvent e) {
  }

}
