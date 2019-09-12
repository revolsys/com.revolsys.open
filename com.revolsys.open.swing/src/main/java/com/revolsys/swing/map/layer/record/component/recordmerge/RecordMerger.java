package com.revolsys.swing.map.layer.record.component.recordmerge;

import java.util.ArrayList;
import java.util.List;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.graph.RecordGraph;
import com.revolsys.geometry.model.Direction;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.LineString;
import com.revolsys.record.Record;
import com.revolsys.record.property.DirectionalFields;
import com.revolsys.swing.component.ProgressMonitor;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;

class RecordMerger {

  private final AbstractRecordLayer layer;

  String errorMessage = "";

  final List<MergeableRecord> records = new ArrayList<>();

  final DirectionalFields directionalFields;

  MergeRecordsDialog dialog;

  RecordMerger(final AbstractRecordLayer layer) {
    this.layer = layer;
    this.directionalFields = DirectionalFields.getProperty(layer);
  }

  public AbstractRecordLayer getLayer() {
    return this.layer;
  }

  public void run(final ProgressMonitor progress) {
    try {
      final List<LayerRecord> originalRecords = this.layer.getMergeableSelectedRecords();
      this.records.clear();
      final DataType geometryType = this.layer.getGeometryType();
      if (originalRecords.size() < 2) {
        this.errorMessage = " at least two records must be selected to merge.";
      } else if (!GeometryDataTypes.LINE_STRING.equals(geometryType)
        && !GeometryDataTypes.MULTI_LINE_STRING.equals(geometryType)) {
        this.errorMessage = "Merging " + geometryType + " not currently supported";
      } else {
        final RecordGraph graph = new RecordGraph();
        for (final LayerRecord originalRecord : originalRecords) {
          Geometry geometry = originalRecord.getGeometry();
          if (geometry != null && !geometry.isEmpty()) {
            geometry = this.layer.getGeometryFactory().geometry(LineString.class, geometry);
            if (geometry instanceof LineString) {
              final MergeableRecord mergeableRecord = new MergeableRecord(this.layer,
                originalRecord);
              this.records.add(mergeableRecord);
              graph.addEdge(mergeableRecord);
            }
          }
        }
        for (final Node<Record> node : graph.nodes()) {
          if (node != null) {
            final List<Edge<Record>> edges = node.getEdges();
            if (edges.size() == 2) {
              final Edge<Record> edge1 = edges.get(0);
              final MergeableRecord record1 = (MergeableRecord)edge1.getObject();
              final Edge<Record> edge2 = edges.get(1);
              final MergeableRecord record2 = (MergeableRecord)edge2.getObject();
              if (record1 != record2 && !edge1.isLoop() && !edge2.isLoop()) {
                final LineString line1 = record1.getGeometry();
                final Record mergedRecord = this.layer.getMergedRecord(node, record1, record2);
                final LineString line = mergedRecord.getGeometry();

                final Direction direction1 = edge1.getDirection(node);
                final Direction direction2 = edge2.getDirection(node);

                final MergeableRecord record;
                if (direction1.isBackwards()) {
                  if (direction2.isBackwards()) {
                    // --> * <--
                    if (line.equalsVertex(2, 0, line1, 0)) {
                      // Reverse line2
                      record = new MergeableRecord(this.layer, mergedRecord, record1, true, record2,
                        false);
                    } else {
                      // Reverse line1
                      record = new MergeableRecord(this.layer, mergedRecord, record2, true, record1,
                        false);
                    }
                  } else {
                    // --> * -->
                    record = new MergeableRecord(this.layer, mergedRecord, record1, true, record2,
                      true);
                  }
                } else {
                  if (direction2.isBackwards()) {
                    // <-- * <--
                    record = new MergeableRecord(this.layer, mergedRecord, record2, true, record1,
                      true);
                  } else {
                    // <-- * -->
                    if (line.equalsVertex(2, 0, line1, line1.getLastVertexIndex())) {
                      // Reverse line1
                      record = new MergeableRecord(this.layer, mergedRecord, record1, false,
                        record2, true);
                    } else {
                      // Reverse line2
                      record = new MergeableRecord(this.layer, mergedRecord, record2, false,
                        record1, true);
                    }
                  }
                }
                this.records.add(record);
                this.records.remove(record1);
                this.records.remove(record2);

                graph.addEdge(record);
                edge1.remove();
                edge2.remove();

              }
            }
          }
        }
      }
    } catch (final Throwable e) {
      Logs.error(this, "Error " + this, e);
    }
  }
}
