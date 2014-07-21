package com.revolsys.gis.graph.process;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.data.record.Record;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.RecordGraph;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.util.ObjectProcessor;

public class GraphProcessor extends BaseInOutProcess<Record, Record> {
  private static final Logger LOG = LoggerFactory.getLogger(GraphProcessor.class);

  private RecordGraph graph;

  private GeometryFactory precisionModel;

  private List<ObjectProcessor<RecordGraph>> processors = new ArrayList<ObjectProcessor<RecordGraph>>();

  public GeometryFactory getPrecisionModel() {
    return precisionModel;
  }

  public List<ObjectProcessor<RecordGraph>> getProcessors() {
    return processors;
  }

  @Override
  protected void init() {
    super.init();
    graph = new RecordGraph();
    if (precisionModel != null) {
      graph.setPrecisionModel(precisionModel);
    }
  }

  @Override
  protected void postRun(final Channel<Record> in,
    final Channel<Record> out) {
    if (out != null) {
      processGraph();
      for (final Edge<Record> edge : graph.getEdges()) {
        final Record object = edge.getObject();
        out.write(object);
      }
    }
  }

  @Override
  protected void process(final Channel<Record> in,
    final Channel<Record> out, final Record object) {
    final Geometry geometry = object.getGeometryValue();
    if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      graph.addEdge(object, line);
    } else {
      if (out != null) {
        out.write(object);
      }
    }
  }

  private void processGraph() {
    if (graph != null) {
      for (final ObjectProcessor<RecordGraph> processor : processors) {
        LOG.info(processor.getClass().getName());
        processor.process(graph);
      }
    }
  }

  public void setPrecisionModel(final GeometryFactory precisionModel) {
    this.precisionModel = precisionModel;
  }

  public void setProcessors(
    final List<ObjectProcessor<RecordGraph>> processors) {
    this.processors = processors;
  }
}
