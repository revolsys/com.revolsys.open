package com.revolsys.swing.map.layer.dataobject.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.LoggerFactory;

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
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.dataobject.row.DataObjectListTableModel;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;

public class MergeObjectsDialog extends JDialog implements WindowListener {

  private static final long serialVersionUID = 1L;

  public static void showDialog(final DataObjectLayer layer) {
    final MergeObjectsDialog dialog = new MergeObjectsDialog(layer);
    dialog.showDialog();
  }

  private JButton okButton;

  private final JLabel statusLabel = new JLabel();

  private final DataObjectLayer layer;

  private TablePanel tablePanel;

  private LayerDataObject mergedObject;

  private List<LayerDataObject> originalObjects;

  public MergeObjectsDialog(final DataObjectLayer layer) {
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

    this.tablePanel = DataObjectListTableModel.createPanel(this.layer);

    tabs.addTab("Merged Object", this.tablePanel);

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
      this.originalObjects = this.layer.getMergeableSelectedRecords();
      final DataType geometryType = this.layer.getGeometryType();
      if (this.originalObjects.size() < 2) {
        setStatus("<p style=\"color:red;font-weight: bold\">At least 2 active records must be selected to merge.</p></body></html>");
      } else if (!DataTypes.LINE_STRING.equals(geometryType)) {
        setStatus("<p style=\"color:red;font-weight: bold\">Merging "
          + geometryType + " not currently supported</p></body></html>");
      } else {
        final DataObjectGraph graph = new DataObjectGraph(this.originalObjects);
        for (final Node<DataObject> node : graph.nodes()) {
          final List<Edge<DataObject>> edges = node.getEdges();
          if (edges.size() == 2) {
            final Edge<DataObject> edge1 = edges.get(0);
            final Edge<DataObject> edge2 = edges.get(1);
            graph.merge(node, edge1, edge2);
          }
        }
        if (graph.getEdgeCount() == 1) {
          final List<Edge<DataObject>> edges = graph.getEdges();
          final Edge<DataObject> edge = edges.get(0);
          final LayerDataObject object = (LayerDataObject)edge.getObject();
          this.mergedObject = object;
        }
      }
      if (this.mergedObject != null) {
        setMergedObject(this.mergedObject, this.originalObjects);
      }

    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error("Error " + this, e);
    } finally {
    }
  }

  public void setMergedObject(final LayerDataObject mergedObject,
    final List<LayerDataObject> objects) {
    if (SwingUtilities.isEventDispatchThread()) {
      setStatus("<p><b>Merged "
        + objects.size()
        + " "
        + this.layer.getName()
        + " records.</p>"
        + "<p>Verify the values shown in the merged record below (highlighted in green).</p>"
        + "<p style=\"color:green;font-weight: bold\">Click OK to save the merged record or click Cancel to abondon edits.</p>");
      if (mergedObject != null) {
        objects.add(0, mergedObject);
      }

      final DataObjectRowTable table = this.tablePanel.getTable();
      final DataObjectListTableModel model = (DataObjectListTableModel)table.getModel();
      model.setObjects(objects);
      MergedObjectPredicate.add(table, mergedObject);
      MergedValuePredicate.add(table, mergedObject);
      this.okButton.setEnabled(true);
    } else {
      Invoke.later(this, "setMergedObject", mergedObject, objects);
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
