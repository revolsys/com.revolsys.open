package com.revolsys.gis.model.geometry.operation.noding.snapround;

import java.util.Collection;

import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.model.geometry.impl.BoundingBox;
import com.revolsys.gis.model.geometry.operation.chain.MonotoneChain;
import com.revolsys.gis.model.geometry.operation.chain.MonotoneChainSelectAction;
import com.revolsys.gis.model.geometry.operation.chain.NodedSegmentString;
import com.revolsys.gis.model.geometry.operation.chain.SegmentString;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * "Snaps" all {@link SegmentString}s in a {@link SpatialIndex} containing
 * {@link MonotoneChain}s to a given {@link HotPixel}.
 *
 * @version 1.7
 */
public class MCIndexPointSnapper {
  public class HotPixelSnapAction extends MonotoneChainSelectAction {
    private final HotPixel hotPixel;

    private final SegmentString parentEdge;

    private final int vertexIndex;

    private boolean isNodeAdded = false;

    public HotPixelSnapAction(final HotPixel hotPixel,
      final SegmentString parentEdge, final int vertexIndex) {
      this.hotPixel = hotPixel;
      this.parentEdge = parentEdge;
      this.vertexIndex = vertexIndex;
    }

    public boolean isNodeAdded() {
      return isNodeAdded;
    }

    @Override
    public void select(final MonotoneChain mc, final int startIndex) {
      final NodedSegmentString ss = (NodedSegmentString)mc.getContext();
      // don't snap a vertex to itself
      if (parentEdge != null) {
        if (ss == parentEdge && startIndex == vertexIndex) {
          return;
        }
      }
      // isNodeAdded = SimpleSnapRounder.addSnappedNode(hotPixel, ss,
      // startIndex);
      isNodeAdded = hotPixel.addSnappedNode(ss, startIndex);
    }

  }

  public static int nSnaps = 0;

  private final Collection monoChains;

  private final STRtree index;

  public MCIndexPointSnapper(final Collection monoChains,
    final SpatialIndex index) {
    this.monoChains = monoChains;
    this.index = (STRtree)index;
  }

  public boolean snap(final HotPixel hotPixel) {
    return snap(hotPixel, null, -1);
  }

  /**
   * Snaps (nodes) all interacting segments to this hot pixel.
   * The hot pixel may represent a vertex of an edge,
   * in which case this routine uses the optimization
   * of not noding the vertex itself
   *
   * @param hotPixel the hot pixel to snap to
   * @param parentEdge the edge containing the vertex, if applicable, or <code>null</code>
   * @param vertexIndex the index of the vertex, if applicable, or -1
   * @return <code>true</code> if a node was added for this pixel
   */
  public boolean snap(final HotPixel hotPixel, final SegmentString parentEdge,
    final int vertexIndex) {
    final BoundingBox pixelEnv = hotPixel.getSafeBoundingBox();
    final HotPixelSnapAction hotPixelSnapAction = new HotPixelSnapAction(
      hotPixel, parentEdge, vertexIndex);

    index.query(JtsGeometryUtil.getEnvelope(pixelEnv), new ItemVisitor() {
      @Override
      public void visitItem(final Object item) {
        final MonotoneChain testChain = (MonotoneChain)item;
        testChain.select(pixelEnv, hotPixelSnapAction);
      }
    });
    return hotPixelSnapAction.isNodeAdded();
  }

}
