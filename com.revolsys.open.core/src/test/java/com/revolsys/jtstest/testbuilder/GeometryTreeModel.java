package com.revolsys.jtstest.testbuilder;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;

/**
 * Coordinates is the only leaf node now, but could be 
 * refactored into a LeafNode class.
 * 
 * @author Martin Davis
 *
 */
class CoordinateNode extends GeometricObjectNode {
  private static DecimalFormat fmt = new DecimalFormat("0.#################",
    new DecimalFormatSymbols());

  public static CoordinateNode create(final Coordinates p) {
    return new CoordinateNode(p);
  }

  public static CoordinateNode create(final Coordinates p, final int i,
    final double distPrev) {
    return new CoordinateNode(p, i, distPrev);
  }

  private static String label(final Coordinates coord, final int i,
    final double distPrev) {
    String lbl = fmt.format(coord.getX()) + "   " + fmt.format(coord.getY());
    if (!Double.isNaN(distPrev)) {
      lbl += "  --  dist: " + distPrev;
    }
    return lbl;
  }

  Coordinates coord;

  public CoordinateNode(final Coordinates coord) {
    this(coord, 0, Double.NaN);
  }

  public CoordinateNode(final Coordinates coord, final int i,
    final double distPrev) {
    super(label(coord, i, distPrev));
    this.coord = coord;
    this.index = i;
  }

  @Override
  public GeometricObjectNode getChildAt(final int index) {
    throw new IllegalStateException("should not be here");
  }

  @Override
  public int getChildCount() {
    return 0;
  }

  @Override
  public Geometry getGeometry() {
    final GeometryFactory geomFact = GeometryFactory.getFactory();
    return geomFact.point(coord);
  }

  @Override
  public int getIndexOfChild(final GeometricObjectNode child) {
    throw new IllegalStateException("should not be here");
  }

  @Override
  public boolean isLeaf() {
    return true;
  }
}

abstract class GeometricObjectNode {
  protected static String indexString(final int index) {
    return "[" + index + "]";
  }

  protected static String sizeString(final int size) {
    return "(" + size + ")";
  }

  protected int index = -1;

  protected String text = "";;

  public GeometricObjectNode(final String text) {
    this.text = text;
  }

  public abstract GeometricObjectNode getChildAt(int index);

  public abstract int getChildCount();

  public abstract Geometry getGeometry();

  public abstract int getIndexOfChild(GeometricObjectNode child);

  public String getText() {
    if (index >= 0) {
      return indexString(index) + " : " + text;
    }
    return text;
  }

  public abstract boolean isLeaf();

  public void setIndex(final int index) {
    this.index = index;
  }

}

class GeometryCollectionNode extends GeometryNode {
  GeometryCollection coll;

  GeometryCollectionNode(final GeometryCollection coll) {
    super(coll, coll.getNumGeometries(), null);
    this.coll = coll;
  }

  @Override
  protected void fillChildren() {
    for (int i = 0; i < coll.getNumGeometries(); i++) {
      final GeometryNode node = create(coll.getGeometry(i));
      node.setIndex(i);
      children.add(node);
    }
  }

  @Override
  public Geometry getGeometry() {
    return coll;
  }
}

abstract class GeometryNode extends GeometricObjectNode {
  public static GeometryNode create(final Geometry geom) {
    if (geom instanceof GeometryCollection) {
      return new GeometryCollectionNode((GeometryCollection)geom);
    }
    if (geom instanceof Polygon) {
      return new PolygonNode((Polygon)geom);
    }
    if (geom instanceof LineString) {
      return new LineStringNode((LineString)geom);
    }
    if (geom instanceof LinearRing) {
      return new LinearRingNode((LinearRing)geom);
    }
    if (geom instanceof Point) {
      return new PointNode((Point)geom);
    }
    return null;
  }

  private static String geometryText(final Geometry geom, final int size,
    final String tag) {
    final StringBuilder buf = new StringBuilder();
    if (tag != null && tag.length() > 0) {
      buf.append(tag + " : ");
    }
    buf.append(geom.getGeometryType());
    if (geom.isEmpty()) {
      buf.append(" EMPTY");
    } else {
      if (size > 0) {
        buf.append(" " + sizeString(size));
      }
    }

    buf.append(" --     Len: " + geom.getLength());
    if (geom.getDimension() >= 2) {
      buf.append("      Area: " + geom.getArea());
    }

    return buf.toString();
  }

  private boolean isLeaf;

  protected List<GeometricObjectNode> children = null;

  public GeometryNode(final Geometry geom) {
    this(geom, 0, null);
  }

  public GeometryNode(final Geometry geom, final int size, final String tag) {
    super(geometryText(geom, size, tag));
    if (geom.isEmpty()) {
      isLeaf = true;
    }
  }

  protected abstract void fillChildren();

  @Override
  public GeometricObjectNode getChildAt(final int index) {
    if (isLeaf) {
      return null;
    }
    populateChildren();
    return children.get(index);
  }

  @Override
  public int getChildCount() {
    if (isLeaf) {
      return 0;
    }
    populateChildren();
    return children.size();
  }

  @Override
  public int getIndexOfChild(final GeometricObjectNode child) {
    if (isLeaf) {
      return -1;
    }
    populateChildren();
    return children.indexOf(child);
  }

  @Override
  public boolean isLeaf() {
    return isLeaf;
  }

  /**
   * Lazily creates child nodes
   */
  private void populateChildren() {
    // already initialized
    if (children != null) {
      return;
    }

    children = new ArrayList<GeometricObjectNode>();
    fillChildren();
  }
}

public class GeometryTreeModel implements TreeModel {
  private final Vector<TreeModelListener> treeModelListeners = new Vector<TreeModelListener>();

  private final GeometricObjectNode rootGeom;

  public GeometryTreeModel(final Geometry geom) {
    rootGeom = GeometryNode.create(geom);
  }

  // ////////////// TreeModel interface implementation ///////////////////////

  /**
   * Adds a listener for the TreeModelEvent posted after the tree changes.
   */
  @Override
  public void addTreeModelListener(final TreeModelListener l) {
    treeModelListeners.addElement(l);
  }

  /**
   * Returns the child of parent at index index in the parent's child array.
   */
  @Override
  public Object getChild(final Object parent, final int index) {
    final GeometricObjectNode gn = (GeometricObjectNode)parent;
    return gn.getChildAt(index);
  }

  /**
   * Returns the number of children of parent.
   */
  @Override
  public int getChildCount(final Object parent) {
    final GeometricObjectNode gn = (GeometricObjectNode)parent;
    return gn.getChildCount();
  }

  /**
   * Returns the index of child in parent.
   */
  @Override
  public int getIndexOfChild(final Object parent, final Object child) {
    final GeometricObjectNode gn = (GeometricObjectNode)parent;
    return gn.getIndexOfChild((GeometricObjectNode)child);
  }

  /**
   * Returns the root of the tree.
   */
  @Override
  public Object getRoot() {
    return rootGeom;
  }

  /**
   * Returns true if node is a leaf.
   */
  @Override
  public boolean isLeaf(final Object node) {
    final GeometricObjectNode gn = (GeometricObjectNode)node;
    return gn.isLeaf();
  }

  /**
   * Removes a listener previously added with addTreeModelListener().
   */
  @Override
  public void removeTreeModelListener(final TreeModelListener l) {
    treeModelListeners.removeElement(l);
  }

  /**
   * Messaged when the user has altered the value for the item identified by
   * path to newValue. Not used by this model.
   */
  @Override
  public void valueForPathChanged(final TreePath path, final Object newValue) {
    System.out.println("*** valueForPathChanged : " + path + " --> " + newValue);
  }
}

class LinearRingNode extends LineStringNode {
  public LinearRingNode(final LinearRing ring) {
    super(ring);
  }

  public LinearRingNode(final LinearRing ring, final String tag) {
    super(ring, tag);
  }
}

class LineStringNode extends GeometryNode {
  private final LineString line;

  public LineStringNode(final LineString line) {
    this(line, null);
  }

  public LineStringNode(final LineString line, final String tag) {
    super(line, line.getVertexCount(), tag);
    this.line = line;
  }

  @Override
  protected void fillChildren() {
    populateChildren(line.getCoordinateArray());
  }

  @Override
  public Geometry getGeometry() {
    return line;
  }

  private void populateChildren(final Coordinates[] pt) {
    final Envelope env = line.getEnvelopeInternal();

    for (int i = 0; i < pt.length; i++) {
      double dist = Double.NaN;
      if (i < pt.length - 1) {
        dist = pt[i].distance(pt[i + 1]);
      }
      final GeometricObjectNode node = CoordinateNode.create(pt[i], i, dist);
      children.add(node);
    }
  }
}

class PointNode extends GeometryNode {
  Point pt;

  public PointNode(final Point p) {
    super(p);
    pt = p;
  }

  @Override
  protected void fillChildren() {
    children.add(CoordinateNode.create(pt.getCoordinate()));
  }

  @Override
  public Geometry getGeometry() {
    return pt;
  }
}

class PolygonNode extends GeometryNode {
  Polygon poly;

  PolygonNode(final Polygon poly) {
    super(poly, poly.getVertexCount(), null);
    this.poly = poly;
  }

  @Override
  protected void fillChildren() {
    children.add(new LinearRingNode(poly.getExteriorRing(), "Shell"));
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      children.add(new LinearRingNode(poly.getInteriorRingN(i), "Hole " + i));
    }
  }

  @Override
  public Geometry getGeometry() {
    return poly;
  }

}
