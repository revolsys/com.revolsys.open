/*
* The JTS Topology Suite is a collection of Java classes that
* implement the fundamental operations required to validate a given
* geo-spatial data set to a known topological specification.
*
* Copyright (C) 2001 Vivid Solutions
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
* For more information, contact:
*
*     Vivid Solutions
*     Suite #1A
*     2328 Government Street
*     Victoria BC  V8T 5G5
*     Canada
*
*     (250)385-6040
*     www.vividsolutions.com
*/

package com.revolsys.jts.algorithm;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.math.Vector3D;

/**
 * Basic computational geometry algorithms 
 * for geometry and coordinates defined in 3-dimensional Cartesian space.
 * 
 * @author mdavis
 *
 */
public class CGAlgorithms3D 
{
	public static double distance(Coordinates p0, Coordinates p1)
	{
		// default to 2D distance if either Z is not set
		if (Double.isNaN(p0.getZ()) || Double.isNaN(p1.getZ()))
			return p0.distance(p1);
		
	    double dx = p0.getX() - p1.getX();
	    double dy = p0.getY() - p1.getY();
	    double dz = p0.getZ() - p1.getZ();
	    return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	public static double distancePointSegment(Coordinates p,
			Coordinates A, Coordinates B) {
	    // if start = end, then just compute distance to one of the endpoints
	    if (A.equals3d(B))
	      return distance(p, A);

	    // otherwise use comp.graphics.algorithms Frequently Asked Questions method
	    /*
	     * (1) r = AC dot AB 
	     *         --------- 
	     *         ||AB||^2 
	     *         
	     * r has the following meaning: 
	     *   r=0 P = A 
	     *   r=1 P = B 
	     *   r<0 P is on the backward extension of AB 
	     *   r>1 P is on the forward extension of AB 
	     *   0<r<1 P is interior to AB
	     */

	    double len2 = (B.getX() - A.getX()) * (B.getX() - A.getX()) + (B.getY() - A.getY()) * (B.getY() - A.getY()) + (B.getZ() - A.getZ()) * (B.getZ() - A.getZ());
	    if (Double.isNaN(len2))
	    	throw new IllegalArgumentException("Ordinates must not be NaN");
	    double r = ((p.getX() - A.getX()) * (B.getX() - A.getX()) + (p.getY() - A.getY()) * (B.getY() - A.getY()) + (p.getZ() - A.getZ()) * (B.getZ() - A.getZ()))
	        / len2;

	    if (r <= 0.0)
	      return distance(p, A);
	    if (r >= 1.0)
	      return distance(p, B);

	    // compute closest point q on line segment
	    double qx = A.getX() + r * (B.getX() - A.getX());
	    double qy = A.getY() + r * (B.getY() - A.getY());
	    double qz = A.getZ() + r * (B.getZ() - A.getZ());
	    // result is distance from p to q
	    double dx = p.getX() - qx;
	    double dy = p.getY() - qy;
	    double dz = p.getZ() - qz;
	    return Math.sqrt(dx*dx + dy*dy + dz*dz);
	}
	

	/**
	 * Computes the distance between two 3D segments.
	 * 
	 * @param A the start point of the first segment
	 * @param B the end point of the first segment
	 * @param C the start point of the second segment
	 * @param D the end point of the second segment
	 * @return the distance between the segments
	 */
	public static double distanceSegmentSegment(
			Coordinates A, Coordinates B, Coordinates C, Coordinates D) 
	{
		/**
		 * This calculation is susceptible to roundoff errors when 
		 * passed large ordinate values.
		 * It may be possible to improve this by using {@link DD} arithmetic.
		 */
	    if (A.equals3d(B))
		      return distancePointSegment(A, C, D);
	    if (C.equals3d(B))
		      return distancePointSegment(C, A, B);
	    
	    /**
	     * Algorithm derived from http://softsurfer.com/Archive/algorithm_0106/algorithm_0106.htm
	     */
		double a = Vector3D.dot(A, B, A, B);
		double b = Vector3D.dot(A, B, C, D);
		double c = Vector3D.dot(C, D, C, D);
		double d = Vector3D.dot(A, B, C, A);
		double e = Vector3D.dot(C, D, C, A);
		
		double denom = a*c - b*b;
	    if (Double.isNaN(denom))
	    	throw new IllegalArgumentException("Ordinates must not be NaN");
		
		double s;
		double t;
		if (denom <= 0.0) {
			/**
			 * The lines are parallel. 
			 * In this case solve for the parameters s and t by assuming s is 0.
			 */
			s = 0;
			// choose largest denominator for optimal numeric conditioning
			if (b > c)
				t = d/b;
			else 
				t = e/c;
		}
		else {
			s = (b*e - c*d) / denom;
			t = (a*e - b*d) / denom;
		}
		if (s < 0) 
			return distancePointSegment(A, C, D);
		else if (s > 1)
			return distancePointSegment(B, C, D);
		else if (t < 0)	
			return distancePointSegment(C, A, B);
		else if(t > 1) {
			return distancePointSegment(D, A, B);
		}
		/**
		 * The closest points are in interiors of segments,
		 * so compute them directly
		 */
		double x1 = A.getX() + s * (B.getX() - A.getX());
		double y1 = A.getY() + s * (B.getY() - A.getY());
		double z1 = A.getZ() + s * (B.getZ() - A.getZ());

		double x2 = C.getX() + t * (D.getX() - C.getX());
		double y2 = C.getY() + t * (D.getY() - C.getY());
		double z2 = C.getZ() + t * (D.getZ() - C.getZ());
		
		// length (p1-p2)
		return distance(new Coordinate((double)x1, y1, z1), new Coordinate((double)x2, y2, z2));
	}

	
}
