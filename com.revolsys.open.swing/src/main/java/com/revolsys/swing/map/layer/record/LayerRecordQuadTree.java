package com.revolsys.swing.map.layer.record;

import com.revolsys.geometry.index.RecordSpatialIndex;
import com.revolsys.geometry.index.SpatialIndex;
import com.revolsys.geometry.index.quadtree.QuadTree;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.swing.map.layer.Layer;

public class LayerRecordQuadTree extends QuadTree<LayerRecord> {
  private static final long serialVersionUID = 1L;

  public static RecordSpatialIndex<LayerRecord> newIndex(final GeometryFactory geometryFactory) {
    final SpatialIndex<LayerRecord> spatialIndex = new LayerRecordQuadTree(geometryFactory);
    return new RecordSpatialIndex<>(spatialIndex);
  }

  public static RecordSpatialIndex<LayerRecord> newIndex(final Layer layer) {
    final GeometryFactory geometryFactory = layer.getGeometryFactory();
    return newIndex(geometryFactory);
  }

  public LayerRecordQuadTree(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  @Override
  protected boolean equalsItem(final LayerRecord record1, final LayerRecord record2) {
    if (record1 == record2) {
      return true;
    } else if (record1 == null || record2 == null) {
      return false;
    } else {
      return record1.isSame(record2);
    }
  }
}
