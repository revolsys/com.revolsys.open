package com.revolsys.swing.map.layer.record.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Collection;
import java.util.Collections;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import org.jdesktop.swingx.VerticalLayout;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.collection.CollectionUtil;
import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.graph.RecordGraph;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.logging.Logs;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.swing.Panels;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.table.model.MergedRecordsTableModel;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.record.model.RecordListTableModel;
import com.revolsys.swing.undo.CreateRecordUndo;
import com.revolsys.swing.undo.DeleteLayerRecordUndo;
import com.revolsys.swing.undo.MultipleUndo;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.Property;

public class MergeRecordsDialog extends JDialog implements WindowListener {

  private static final long serialVersionUID = 1L;

  public static void showDialog(final AbstractRecordLayer layer) {
    final UndoManager undoManager = layer.getMapPanel().getUndoManager();
    final MergeRecordsDialog dialog = new MergeRecordsDialog(undoManager, layer);
    dialog.showDialog();
  }

  private final AbstractRecordLayer layer;

  private final Map<Record, LayerRecord> mergeableToOiginalRecordMap = new HashMap<>();

  private JPanel mergedRecordsPanel;

  private Map<Record, Set<LayerRecord>> mergedRecords = Collections.emptyMap();

  private JButton okButton;

  private final Set<LayerRecord> replacedOriginalRecords = new LinkedHashSet<>();

  private final UndoManager undoManager;

  public MergeRecordsDialog(final UndoManager undoManager, final AbstractRecordLayer layer) {
    super(SwingUtil.getActiveWindow(), "Merge " + layer.getName(), ModalityType.APPLICATION_MODAL);
    this.undoManager = undoManager;
    this.layer = layer;
    initDialog();
  }

  public void cancel() {
    if (isVisible()) {
      SwingUtil.dispose(this);
    }
  }

  public void finish() {
    final MultipleUndo multipleUndo = new MultipleUndo();
    for (final Record mergedRecord : this.mergedRecords.keySet()) {
      final CreateRecordUndo createRecordUndo = new CreateRecordUndo(this.layer, mergedRecord);
      multipleUndo.addEdit(createRecordUndo);
    }
    for (final LayerRecord record : this.replacedOriginalRecords) {
      final DeleteLayerRecordUndo deleteRecordUndo = new DeleteLayerRecordUndo(record);
      multipleUndo.addEdit(deleteRecordUndo);
    }
    if (this.undoManager == null) {
      multipleUndo.redo();
    } else {
      this.undoManager.addEdit(multipleUndo);
    }
    setVisible(false);
  }

  protected void initDialog() {
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    setMinimumSize(new Dimension(600, 100));
    addWindowListener(this);

    final BasePanel panel = new BasePanel(new BorderLayout());
    add(new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));

    this.mergedRecordsPanel = new JPanel(new VerticalLayout());

    panel.add(this.mergedRecordsPanel, BorderLayout.CENTER);

    final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    add(buttonsPanel, BorderLayout.SOUTH);

    final JButton cancelButton = RunnableAction.newButton("Cancel", this::cancel);
    buttonsPanel.add(cancelButton);

    this.okButton = RunnableAction.newButton("OK", this::finish);
    this.okButton.setEnabled(false);
    buttonsPanel.add(this.okButton);

    pack();
    SwingUtil.autoAdjustPosition(this);
  }

  protected void replaceRecord(final Record mergedRecord, final Record record) {
    if (mergedRecord != record) {
      final LayerRecord originalRecord = this.mergeableToOiginalRecordMap.remove(record);
      if (originalRecord != null) {
        this.replacedOriginalRecords.add(originalRecord);
      }
    }
  }

  public void run() {
    try {
      final List<LayerRecord> originalRecords = this.layer.getMergeableSelectedRecords();

      String errorMessage = "";
      final DataType geometryType = this.layer.getGeometryType();
      this.mergedRecords = new HashMap<>();
      if (originalRecords.size() < 2) {
        errorMessage = " at least two records must be selected to merge.";
      } else if (!DataTypes.LINE_STRING.equals(geometryType)
        && !DataTypes.MULTI_LINE_STRING.equals(geometryType)) {
        errorMessage = "Merging " + geometryType + " not currently supported";
      } else {
        final RecordGraph graph = new RecordGraph();
        for (final LayerRecord originalRecord : originalRecords) {
          Geometry geometry = originalRecord.getGeometry();
          if (geometry != null && !geometry.isEmpty()) {
            geometry = this.layer.getGeometryFactory().geometry(LineString.class, geometry);
            if (geometry instanceof LineString) {
              final Record mergeableRecord = new ArrayRecord(originalRecord);
              mergeableRecord.setGeometryValue(geometry);
              this.mergeableToOiginalRecordMap.put(mergeableRecord, originalRecord);
              graph.addEdge(mergeableRecord);
            }
          }
        }
        for (final Node<Record> node : graph.nodes()) {
          if (node != null) {
            final List<Edge<Record>> edges = node.getEdges();
            if (edges.size() == 2) {
              final Edge<Record> edge1 = edges.get(0);
              final Record record1 = edge1.getObject();
              final Edge<Record> edge2 = edges.get(1);
              final Record record2 = edge2.getObject();
              if (record1 != record2) {
                final Record mergedRecord = this.layer.getMergedRecord(node, record1, record2);

                graph.addEdge(mergedRecord);
                edge1.remove();
                edge2.remove();

                final Set<LayerRecord> sourceRecords = new LinkedHashSet<>();
                // TODO verify orientation to ensure they are in the correct
                // order
                // and see if they are reversed
                CollectionUtil.addIfNotNull(sourceRecords,
                  this.mergeableToOiginalRecordMap.get(record1));
                CollectionUtil.addAllIfNotNull(sourceRecords, this.mergedRecords.remove(record1));
                CollectionUtil.addIfNotNull(sourceRecords,
                  this.mergeableToOiginalRecordMap.get(record2));
                CollectionUtil.addAllIfNotNull(sourceRecords, this.mergedRecords.remove(record2));
                this.mergedRecords.put(mergedRecord, sourceRecords);
                replaceRecord(mergedRecord, record1);
                replaceRecord(mergedRecord, record2);
              }
            }
          }
        }
      }
      final String message = errorMessage;
      Invoke.later(() -> setMergedRecords(message, this.mergedRecords));

    } catch (final Throwable e) {
      Logs.error(this, "Error " + this, e);
    }
  }

  public void setMergedRecord(final int i, final Record mergedObject,
    final Collection<LayerRecord> objects) {

    this.okButton.setEnabled(true);

    final TablePanel tablePanel = MergedRecordsTableModel.newPanel(this.layer, mergedObject,
      objects);

    final JPanel panel = Panels
      .titledTransparentVerticalLayout("Merged " + objects.size() + " Records");
    panel.add(tablePanel);
    this.mergedRecordsPanel.add(panel);

  }

  public void setMergedRecords(String errorMessage,
    final Map<Record, Set<LayerRecord>> mergedRecords) {
    final Set<Record> unMergeableRecords = new HashSet<>(this.mergeableToOiginalRecordMap.keySet());
    unMergeableRecords.removeAll(mergedRecords.keySet());
    if (!mergedRecords.isEmpty()) {
      int i = 0;

      for (final Entry<Record, Set<LayerRecord>> mergedEntry : mergedRecords.entrySet()) {
        final Record mergedObject = mergedEntry.getKey();
        final Set<LayerRecord> originalObjects = mergedEntry.getValue();
        setMergedRecord(i, mergedObject, originalObjects);
        i++;
      }
    }
    if (!unMergeableRecords.isEmpty() || Property.hasValue(errorMessage)) {
      final Set<LayerRecord> records = new LinkedHashSet<>();
      for (final Record record : unMergeableRecords) {
        final LayerRecord originalRecord = this.mergeableToOiginalRecordMap.get(record);
        if (originalRecord != null) {
          records.add(originalRecord);
        }
      }
      final TablePanel tablePanel = RecordListTableModel.newPanel(this.layer, records);
      final RecordListTableModel tableModel = tablePanel.getTableModel();
      tableModel.setEditable(false);
      tablePanel.setPreferredSize(new Dimension(100, 50 + unMergeableRecords.size() * 22));

      final JPanel panel = Panels
        .titledTransparentBorderLayout(unMergeableRecords.size() + " Un-Mergeable Records");
      if (!Property.hasValue(errorMessage)) {
        errorMessage = "The following records could not be merged and will not be modified.";
      }
      final JLabel unMergeLabel = new JLabel(
        "<html><p style=\"color:red\">" + errorMessage + "</p></html>");
      panel.add(unMergeLabel, BorderLayout.NORTH);
      panel.add(tablePanel, BorderLayout.SOUTH);

      this.mergedRecordsPanel.add(panel);
    }
    SwingUtil.autoAdjustPosition(this);
    setVisible(true);
  }

  private void showDialog() {
    Invoke.background(toString(), this::run);
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
