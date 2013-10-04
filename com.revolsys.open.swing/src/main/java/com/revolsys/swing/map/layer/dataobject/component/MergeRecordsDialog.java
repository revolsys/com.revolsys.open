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
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.graph.DataObjectGraph;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.logging.Log4jTableModel;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.BaseJxTable;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.dataobject.row.DataObjectListTableModel;
import com.revolsys.util.CollectionUtil;

public class MergeRecordsDialog extends JDialog implements WindowListener {

  private static final long serialVersionUID = 1L;

  public static void showDialog(final DataObjectLayer layer) {
    final MergeRecordsDialog dialog = new MergeRecordsDialog(layer);
    dialog.showDialog();
  }

  private JButton okButton;

  private final JLabel statusLabel = new JLabel();

  private final DataObjectLayer layer;

  private final Map<LayerDataObject, DataObject> originalObjectsToMergeableObjects = new HashMap<LayerDataObject, DataObject>();

  private final Map<DataObject, LayerDataObject> mergeableObjectsToOiginalObjects = new HashMap<DataObject, LayerDataObject>();

  private JPanel mergedObjectsPanel;

  public MergeRecordsDialog(final DataObjectLayer layer) {
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
    addWindowListener(this);
    setLayout(new BorderLayout());

    final JPanel statusPanel = new JPanel(new VerticalLayout());
    statusPanel.add(statusLabel);
    SwingUtil.setTitledBorder(statusPanel, "Status");
    add(statusPanel, BorderLayout.NORTH);
    setStatus("Creating merged record for " + this.layer.getName());

    final JTabbedPane tabs = new JTabbedPane();
    add(tabs, BorderLayout.CENTER);

    mergedObjectsPanel = new JPanel(new VerticalLayout(10));
    mergedObjectsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    mergedObjectsPanel.setOpaque(false);

    tabs.addTab("Merged Object", new JScrollPane(mergedObjectsPanel));

    tabs.addTab("Errors", Log4jTableModel.createPanel());

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

      final DataType geometryType = this.layer.getGeometryType();
      if (originalObjects.size() < 2) {
        setStatus("<p style=\"color:red;font-weight: bold\">At least 2 active records must be selected to merge.</p></body></html>");
      } else if (!DataTypes.LINE_STRING.equals(geometryType)) {
        setStatus("<p style=\"color:red;font-weight: bold\">Merging "
          + geometryType + " not currently supported</p></body></html>");
      } else {
        for (final LayerDataObject originalObject : originalObjects) {
          final DataObject mergeableObject = new ArrayDataObject(originalObject);
          originalObjectsToMergeableObjects.put(originalObject, mergeableObject);
          mergeableObjectsToOiginalObjects.put(mergeableObject, originalObject);
        }
        final Map<DataObject, Set<LayerDataObject>> mergedObjects = new HashMap<DataObject, Set<LayerDataObject>>();
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
        Invoke.later(this, "setMergedObjects", mergedObjects);
      }

    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error("Error " + this, e);
    } finally {
    }
  }

  public void setMergedObject(final int i, final DataObject mergedObject,
    final Collection<LayerDataObject> objects) {
    final TablePanel tablePanel = MergedRecordsTableModel.createPanel(layer);

    final BaseJxTable table = tablePanel.getTable();
    final MergedRecordsTableModel model = (MergedRecordsTableModel)table.getModel();
    model.setObjects(mergedObject, objects);
    this.okButton.setEnabled(true);
    tablePanel.setPreferredSize(new Dimension(100, 45 + objects.size() * 20));

    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(tablePanel, BorderLayout.SOUTH);
    SwingUtil.setTitledBorder(panel, "Merged records #" + i);

    mergedObjectsPanel.add(panel);

  }

  public void setMergedObjects(
    final Map<DataObject, Set<LayerDataObject>> mergedObjects) {
    final Set<LayerDataObject> unMergeableRecords = new HashSet<LayerDataObject>(
      originalObjectsToMergeableObjects.keySet());
    if (!mergedObjects.isEmpty()) {
      setStatus("<p><b>Merged "
        + mergedObjects.size()
        + " "
        + this.layer.getName()
        + " records.</p>"
        + "<p>Verify the values shown in the merged record below (highlighted in green).</p>"
        + "<p style=\"color:green;font-weight: bold\">Click OK to save the merged record or click Cancel to abondon edits.</p>");

      int i = 0;
      for (final Entry<DataObject, Set<LayerDataObject>> mergedEntry : mergedObjects.entrySet()) {
        final DataObject mergedObject = mergedEntry.getKey();
        final Set<LayerDataObject> originalObjects = mergedEntry.getValue();
        unMergeableRecords.removeAll(originalObjects);
        setMergedObject(i, mergedObject, originalObjects);
        i++;
      }
    }
    if (!unMergeableRecords.isEmpty()) {
      final TablePanel tablePanel = DataObjectListTableModel.createPanel(layer,
        unMergeableRecords);
      final DataObjectListTableModel tableModel = tablePanel.getTableModel();
      tableModel.setEditable(false);
      tablePanel.setPreferredSize(new Dimension(100,
        25 + unMergeableRecords.size() * 20));

      final JPanel panel = new JPanel(new BorderLayout());
      panel.add(tablePanel, BorderLayout.SOUTH);
      SwingUtil.setTitledBorder(panel, "Records that could not be merged");

      mergedObjectsPanel.add(panel);
    }
  }

  public void setStatus(final String message) {
    this.statusLabel.setText("<html><body>" + message + "</body></html>");
  }

  private void showDialog() {
    Invoke.background(toString(), this, "run");
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
