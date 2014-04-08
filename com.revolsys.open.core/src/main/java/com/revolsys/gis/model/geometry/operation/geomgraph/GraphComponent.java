package com.revolsys.gis.model.geometry.operation.geomgraph;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.jts.geom.IntersectionMatrix;
import com.revolsys.jts.util.Assert;

/**
 * A GraphComponent is the parent class for the objects' that form a graph. Each
 * GraphComponent can carry a Label.
 * 
 * @version 1.7
 */
abstract public class GraphComponent {

  protected Label label;

  /**
   * isInResult indicates if this component has already been included in the
   * result
   */
  private boolean isInResult = false;

  private boolean isCovered = false;

  private boolean isCoveredSet = false;

  private boolean isVisited = false;

  public GraphComponent() {
  }

  public GraphComponent(final Label label) {
    this.label = label;
  }

  /**
   * compute the contribution to an IM for this component
   */
  abstract protected void computeIM(IntersectionMatrix im);

  /**
   * @return a coordinate in this component (or null, if there are none)
   */
  abstract public Coordinates getCoordinate();

  public Label getLabel() {
    return label;
  }

  public boolean isCovered() {
    return isCovered;
  }

  public boolean isCoveredSet() {
    return isCoveredSet;
  }

  public boolean isInResult() {
    return isInResult;
  }

  /**
   * An isolated component is one that does not intersect or touch any other
   * component. This is the case if the label has valid locations for only a
   * single Geometry.
   * 
   * @return true if this component is isolated
   */
  abstract public boolean isIsolated();

  public boolean isVisited() {
    return isVisited;
  }

  public void setCovered(final boolean isCovered) {
    this.isCovered = isCovered;
    this.isCoveredSet = true;
  }

  public void setInResult(final boolean isInResult) {
    this.isInResult = isInResult;
  }

  public void setLabel(final Label label) {
    this.label = label;
  }

  public void setVisited(final boolean isVisited) {
    this.isVisited = isVisited;
  }

  /**
   * Update the IM with the contribution for this component. A component only
   * contributes if it has a labelling for both parent geometries
   */
  public void updateIM(final IntersectionMatrix im) {
    Assert.isTrue(label.getGeometryCount() >= 2, "found partial label");
    computeIM(im);
  }

}
