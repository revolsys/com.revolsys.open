package com.revolsys.jtstest.testbuilder.topostretch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateFilter;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.util.LinearComponentExtracter;

/**
 * Stretches the vertices and segments of a @link Geometry}
 * to make the topology more visible.
 * 
 * @author Martin Davis
 *
 */
public class TopologyStretcher {
  private static class VertexInMaskCountCoordinateFilter implements
    CoordinateFilter {
    private final Envelope mask;

    private int count = 0;

    public VertexInMaskCountCoordinateFilter(final Envelope mask) {
      this.mask = mask;
    }

    @Override
    public void filter(final Coordinates coord) {
      if (mask.contains(coord)) {
        count++;
      }
    }

    public int getCount() {
      return count;
    }
  }

  private double stretchDistance = 0.1;

  private final Geometry[] inputGeoms;

  private List linestrings;

  private List[] modifiedCoords;

  public TopologyStretcher(final Geometry g) {
    inputGeoms = new Geometry[1];
    inputGeoms[0] = g;
  }

  public TopologyStretcher(final Geometry g1, final Geometry g2) {
    inputGeoms = new Geometry[2];
    inputGeoms[0] = g1;
    inputGeoms[1] = g2;
  }

  private List extractLineStrings(final Geometry[] geom, final Envelope mask) {
    final List lines = new ArrayList();
    final LinearComponentExtracter lineExtracter = new LinearComponentExtracter(
      lines);
    for (int i = 0; i < geom.length; i++) {
      if (geom[i] == null) {
        continue;
      }

      if (mask != null && !mask.intersects(geom[i].getEnvelopeInternal())) {
        continue;
      }

      geom[i].apply(lineExtracter);
    }
    if (mask != null) {
      final List masked = new ArrayList();
      for (final Iterator i = lines.iterator(); i.hasNext();) {
        final LineString line = (LineString)i.next();
        if (mask.intersects(line.getEnvelopeInternal())) {
          masked.add(line);
        }
      }
      return masked;
    }
    return lines;
  }

  private Map getCoordinateMoves(final List nearVerts) {
    final Map moves = new TreeMap();
    for (final Iterator i = nearVerts.iterator(); i.hasNext();) {
      final StretchedVertex nv = (StretchedVertex)i.next();
      // TODO: check if move would invalidate topology. If yes, don't move
      final Coordinates src = nv.getVertexCoordinate();
      final Coordinates moved = nv.getStretchedVertex(stretchDistance);
      if (!moved.equals2d(src)) {
        moves.put(src, moved);
      }
    }
    return moves;
  }

  /**
   * Gets the {@link Coordinate}s in each stretched geometry which were modified  (if any).
   * 
   * @return lists of Coordinates, one for each input geometry
   */
  public List[] getModifiedCoordinates() {
    return modifiedCoords;
  }

  public int numVerticesInMask(final Envelope mask) {
    final VertexInMaskCountCoordinateFilter filter = new VertexInMaskCountCoordinateFilter(
      mask);
    if (inputGeoms[0] != null) {
      inputGeoms[0].apply(filter);
    }
    if (inputGeoms[1] != null) {
      inputGeoms[1].apply(filter);
    }
    return filter.getCount();
  }

  public Geometry[] stretch(final double nearnessTol,
    final double stretchDistance) {
    return stretch(nearnessTol, stretchDistance, null);
  }

  public Geometry[] stretch(final double nearnessTol,
    final double stretchDistance, final Envelope mask) {
    this.stretchDistance = stretchDistance;
    linestrings = extractLineStrings(inputGeoms, mask);

    final List nearVerts = StretchedVertexFinder.findNear(linestrings,
      nearnessTol, mask);

    final Map coordinateMoves = getCoordinateMoves(nearVerts);

    final Geometry[] strGeoms = new Geometry[inputGeoms.length];
    modifiedCoords = new List[inputGeoms.length];

    for (int i = 0; i < inputGeoms.length; i++) {
      final Geometry geom = inputGeoms[i];
      if (geom != null) {
        final GeometryVerticesMover mover = new GeometryVerticesMover(geom,
          coordinateMoves);
        final Geometry stretchedGeom = mover.move();
        strGeoms[i] = stretchedGeom;
        modifiedCoords[i] = mover.getModifiedCoordinates();
      }
    }
    return strGeoms;
  }
}
