package com.revolsys.geometry.test.old.operation;

import java.util.Collection;

import com.revolsys.geometry.algorithm.match.AreaSimilarityMeasure;
import com.revolsys.geometry.algorithm.match.HausdorffSimilarityMeasure;
import com.revolsys.geometry.algorithm.match.SimilarityMeasureCombiner;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.operation.union.CascadedPolygonUnion;

/**
 * Compares the results of CascadedPolygonUnion to Geometry.union()
 * using shape similarity measures.
 *
 * @author mbdavis
 *
 */
public class CascadedPolygonUnionTester {

  public static final double MIN_SIMILARITY_MEAURE = 0.999999;

  GeometryFactory geometryFactoryFloating = GeometryFactory.DEFAULT_3D;

  public CascadedPolygonUnionTester() {
  }

  public boolean test(final Collection<? extends Polygon> polygons, final double minimumMeasure) {
    final Geometry union1 = unionIterated(polygons);
    final Geometry union2 = unionCascaded(polygons);

    final double areaMeasure = new AreaSimilarityMeasure().measure(union1, union2);
    final double hausMeasure = new HausdorffSimilarityMeasure().measure(union1, union2);
    final double overallMeasure = SimilarityMeasureCombiner.combine(areaMeasure, hausMeasure);

    return overallMeasure > minimumMeasure;
  }

  public Geometry unionCascaded(final Collection<? extends Polygon> polygons) {
    return CascadedPolygonUnion.union(polygons);
  }

  public Geometry unionIterated(final Collection<? extends Polygon> polygons) {
    Geometry unionAll = null;
    int count = 0;
    for (final Polygon polygon : polygons) {
      if (unionAll == null) {
        unionAll = polygon.clone();
      } else {
        unionAll = unionAll.union(polygon);
      }

      count++;
      if (count % 100 == 0) {
        System.out.print(".");
      }
    }
    return unionAll;
  }

}
