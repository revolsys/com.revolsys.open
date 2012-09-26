package com.revolsys.gis.model.geometry.operation.geomgraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;

/**
 * An EdgeEndBuilder creates EdgeEnds for all the "split edges"
 * created by the
 * intersections determined for an Edge.
 *
 * @version 1.7
 */

/**
 * Computes the {@link EdgeEnd}s which arise from a noded {@link Edge}.
 * 
 * @version 1.7
 */
public class EdgeEndBuilder {

  public EdgeEndBuilder() {
  }

  public List computeEdgeEnds(Iterator edges) {
    List l = new ArrayList();
    for (Iterator i = edges; i.hasNext();) {
      Edge e = (Edge)i.next();
      computeEdgeEnds(e, l);
    }
    return l;
  }

  /**
   * Creates stub edges for all the intersections in this Edge (if any) and
   * inserts them into the graph.
   */
  public void computeEdgeEnds(Edge edge, List l) {
    EdgeIntersectionList eiList = edge.getEdgeIntersectionList();
    // Debug.print(eiList);
    // ensure that the list has entries for the first and last point of the edge
    eiList.addEndpoints();

    Iterator it = eiList.iterator();
    EdgeIntersection eiPrev = null;
    EdgeIntersection eiCurr = null;
    // no intersections, so there is nothing to do
    if (!it.hasNext())
      return;
    EdgeIntersection eiNext = (EdgeIntersection)it.next();
    do {
      eiPrev = eiCurr;
      eiCurr = eiNext;
      eiNext = null;
      if (it.hasNext())
        eiNext = (EdgeIntersection)it.next();

      if (eiCurr != null) {
        createEdgeEndForPrev(edge, l, eiCurr, eiPrev);
        createEdgeEndForNext(edge, l, eiCurr, eiNext);
      }

    } while (eiCurr != null);

  }

  /**
   * Create a EdgeStub for the edge before the intersection eiCurr. The previous
   * intersection is provided in case it is the endpoint for the stub edge.
   * Otherwise, the previous point from the parent edge will be the endpoint. <br>
   * eiCurr will always be an EdgeIntersection, but eiPrev may be null.
   */
  void createEdgeEndForPrev(Edge edge, List l, EdgeIntersection eiCurr,
    EdgeIntersection eiPrev) {

    int iPrev = eiCurr.segmentIndex;
    if (eiCurr.dist == 0.0) {
      // if at the start of the edge there is no previous edge
      if (iPrev == 0)
        return;
      iPrev--;
    }
    Coordinates pPrev = edge.getCoordinate(iPrev);
    // if prev intersection is past the previous vertex, use it instead
    if (eiPrev != null && eiPrev.segmentIndex >= iPrev)
      pPrev = eiPrev.coord;

    Label label = new Label(edge.getLabel());
    // since edgeStub is oriented opposite to it's parent edge, have to flip
    // sides for edge label
    label.flip();
    EdgeEnd e = new EdgeEnd(edge, eiCurr.coord, pPrev, label);
    // e.print(System.out); System.out.println();
    l.add(e);
  }

  /**
   * Create a StubEdge for the edge after the intersection eiCurr. The next
   * intersection is provided in case it is the endpoint for the stub edge.
   * Otherwise, the next point from the parent edge will be the endpoint. <br>
   * eiCurr will always be an EdgeIntersection, but eiNext may be null.
   */
  void createEdgeEndForNext(Edge edge, List l, EdgeIntersection eiCurr,
    EdgeIntersection eiNext) {

    int iNext = eiCurr.segmentIndex + 1;
    // if there is no next edge there is nothing to do
    if (iNext >= edge.getNumPoints() && eiNext == null)
      return;

    Coordinates pNext = edge.getCoordinate(iNext);

    // if the next intersection is in the same segment as the current, use it as
    // the endpoint
    if (eiNext != null && eiNext.segmentIndex == eiCurr.segmentIndex)
      pNext = eiNext.coord;

    EdgeEnd e = new EdgeEnd(edge, eiCurr.coord, pNext, new Label(
      edge.getLabel()));
    // Debug.println(e);
    l.add(e);
  }

}
