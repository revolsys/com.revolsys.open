package com.revolsys.gis.graph.process;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.graph.DataObjectGraph;
import com.revolsys.gis.graph.Edge;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.util.ObjectProcessor;

public class GraphProcessor extends BaseInOutProcess<DataObject, DataObject> {
  private static final Logger LOG = LoggerFactory.getLogger(GraphProcessor.class);

  private DataObjectGraph graph;

  private GeometryFactory precisionModel;

  private List<ObjectProcessor<DataObjectGraph>> processors = new ArrayList<ObjectProcessor<DataObjectGraph>>();

  public GeometryFactory getPrecisionModel() {
    return precisionModel;
  }

  public List<ObjectProcessor<DataObjectGraph>> getProcessors() {
    return processors;
  }

  @Override
  protected void init() {
    super.init();
    graph = new DataObjectGraph();
    if (precisionModel != null) {
      graph.setPrecisionModel(precisionModel);
    }
  }

  @Override
  protected void postRun(final Channel<DataObject> in,
    final Channel<DataObject> out) {
    if (out != null) {
      processGraph();
      for (final Edge<DataObject> edge : graph.getEdges()) {
        final DataObject object = edge.getObject();
        out.write(object);
      }
    }
  }

  @Override
  protected void process(final Channel<DataObject> in,
    final Channel<DataObject> out, final DataObject object) {
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
      for (final ObjectProcessor<DataObjectGraph> processor : processors) {
        LOG.info(processor.getClass().getName());
        processor.process(graph);
      }
    }
  }

  public void setPrecisionModel(final GeometryFactory precisionModel) {
    this.precisionModel = precisionModel;
  }

  public void setProcessors(
    final List<ObjectProcessor<DataObjectGraph>> processors) {
    this.processors = processors;
  }
}
