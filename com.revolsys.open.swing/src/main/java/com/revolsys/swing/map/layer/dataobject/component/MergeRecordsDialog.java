package com.revolsys.swing.map.layer.dataobject.component;

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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.graph.DataObjectGraph;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.layer.dataobject.table.model.MergedRecordsTableModel;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.dataobject.model.DataObjectListTableModel;
import com.revolsys.util.CollectionUtil;

public class MergeRecordsDialog extends JDialog implements WindowListener {

  private static final long serialVersionUID = 1L;

  public static void showDialog(final AbstractDataObjectLayer layer) {
    final MergeRecordsDialog dialog = new MergeRecordsDialog(layer);
    dialog.showDialog();
  }

  private JButton okButton;

  private final AbstractDataObjectLayer layer;

  private final Map<LayerDataObject, DataObject> originalObjectsToMergeableObjects = new HashMap<LayerDataObject, DataObject>();

  private final Map<DataObject, LayerDataObject> mergeableObjectsToOiginalObjects = new HashMap<DataObject, LayerDataObject>();

  private JPanel mergedObjectsPanel;

  public MergeRecordsDialog(final AbstractDataObjectLayer layer) {
    super(SwingUtil.getActiveWindow(), "Merge " + layer.getName(),
      ModalityType.APPLICATION_MODAL);
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
    // layer.deleteRecords(originalObjects);
    setVisible(false);
  }

  protected void initDialog() {
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    setMinimumSize(new Dimension(600, 400));
    addWindowListener(this);

    final JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(new JScrollPane(panel));

    mergedObjectsPanel = new JPanel(new VerticalLayout(10));
    mergedObjectsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
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

    SwingUtil.setSize(this, 10, 50);
  }

  public void run() {
    try {
      final List<LayerDataObject> originalObjects = this.layer.getMergeableSelectedRecords();

      String errorMessage = "";
      final DataType geometryType = this.layer.getGeometryType();
      final Map<DataObject, Set<LayerDataObject>> mergedObjects = new HashMap<DataObject, Set<LayerDataObject>>();
      if (originalObjects.size() < 2) {
        errorMessage = " at least two records must be selected to merge.";
      } else if (!DataTypes.LINE_STRING.equals(geometryType)) {
        errorMessage = "Merging " + geometryType + " not currently supported";
      } else {
        for (final LayerDataObject originalObject : originalObjects) {
          final DataObject mergeableObject = new ArrayDataObject(originalObject);
          originalObjectsToMergeableObjects.put(originalObject, mergeableObject);
          mergeableObjectsToOiginalObjects.put(mergeableObject, originalObject);
        }
        final DataObjectGraph graph = new DataObjectGraph(
          this.originalObjectsToMergeableObjects.values());
        for (final Node<DataObject> node : graph.nodes()) {
          final List<Edge<DataObject>> edges = node.getEdges();
          if (edges.size() == 2) {
            final Edge<DataObject> edge1 = edges.get(0);
            final DataObject object1 = edge1.getObject();
            final Edge<DataObject> edge2 = edges.get(1);
            final DataObject object2 = edge2.getObject();

            final Edge<DataObject> mergedEdge = graph.merge(node, edge1, edge2);
            final DataObject mergedObject = mergedEdge.getObject();
            final Set<LayerDataObject> sourceObjects = new LinkedHashSet<LayerDataObject>();
            // TODO verify orientation to ensure they are in the correct order
            // and see if they are reversed
            CollectionUtil.addIfNotNull(sourceObjects,
              mergeableObjectsToOiginalObjects.get(object1));
            CollectionUtil.addAllIfNotNull(sourceObjects,
              mergedObjects.remove(object1));
            CollectionUtil.addIfNotNull(sourceObjects,
              mergeableObjectsToOiginalObjects.get(object2));
            CollectionUtil.addAllIfNotNull(sourceObjects,
              mergedObjects.remove(object2));
            mergedObjects.put(mergedObject, sourceObjects);
          }
        }
      }
      Invoke.later(this, "setMergedRecords", errorMessage, mergedObjects);

    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error("Error " + this, e);
    } finally {
    }
  }

  public void setMergedRecord(final int i, final DataObject mergedObject,
    final Collection<LayerDataObject> objects) {
    final TablePanel tablePanel = MergedRecordsTableModel.createPanel(layer,
      mergedObject, objects);

    this.okButton.setEnabled(true);
    tablePanel.setPreferredSize(new Dimension(100, 75 + objects.size() * 22));

    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(tablePanel, BorderLayout.SOUTH);
    SwingUtil.setTitledBorder(panel, "Merged " + objects.size()
      + " Source Records");

    mergedObjectsPanel.add(panel);

  }

  public void setMergedRecords(String errorMessage,
    final Map<DataObject, Set<LayerDataObject>> mergedObjects) {
    final Set<LayerDataObject> unMergeableRecords = new HashSet<LayerDataObject>(
      originalObjectsToMergeableObjects.keySet());
    if (!mergedObjects.isEmpty()) {
      final JPanel instructions = new JPanel(new BorderLayout());
      SwingUtil.setTitledBorder(instructions, "Instructions");
      instructions.add(
        new JLabel(
          "<html><p>Clicking OK will replace the records below with the merged records highlighted in green, or click Cancel to abandon any changes.</p>"
            + "<p>Verify the values in the merged green record and edit them if required before clicking OK.</p>"
            + "<p>Values in the source records that differ from the merged records will be highlighted in red.</p>"
            + "<p>Values in the source record that were null but not null in the merged record will be highlighted in yellow.</p></html>"),
        BorderLayout.NORTH);
      int i = 0;
      mergedObjectsPanel.add(instructions);

      for (final Entry<DataObject, Set<LayerDataObject>> mergedEntry : mergedObjects.entrySet()) {
        final DataObject mergedObject = mergedEntry.getKey();
        final Set<LayerDataObject> originalObjects = mergedEntry.getValue();
        unMergeableRecords.removeAll(originalObjects);
        setMergedRecord(i, mergedObject, originalObjects);
        i++;
      }
    }
    if (!unMergeableRecords.isEmpty()) {
      final TablePanel tablePanel = DataObjectListTableModel.createPanel(layer,
        unMergeableRecords);
      final DataObjectListTableModel tableModel = tablePanel.getTableModel();
      tableModel.setEditable(false);
      tablePanel.setPreferredSize(new Dimension(100,
        50 + unMergeableRecords.size() * 22));

      final JPanel panel = new JPanel(new BorderLayout());
      if (!StringUtils.hasText(errorMessage)) {
        errorMessage = "The following records could not be merged and will not be modified by clicking OK.";
      }
      final JLabel unMergeLabel = new JLabel("<html><p style=\"color:red\">"
        + errorMessage + "</p></html>");
      panel.add(unMergeLabel, BorderLayout.NORTH);
      panel.add(tablePanel, BorderLayout.SOUTH);
      SwingUtil.setTitledBorder(panel, unMergeableRecords.size()
        + " Un-Mergable Records");

      mergedObjectsPanel.add(panel);
    }
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
