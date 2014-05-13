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

package com.revolsys.jts.math;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Point;

/**
 * Represents a vector in 3-dimensional Cartesian space.
 * 
 * @author mdavis
 *
 */
public class Vector3D {
	
	/**
	 * Computes the dot product of the 3D vectors AB and CD.
	 * 
	 * @param A
	 * @param B
	 * @param C
	 * @param D
	 * @return the dot product
	 */
	public static double dot(Point A, Point B, Point C, Point D)
	{
		double ABx = B.getX() - A.getX();
		double ABy = B.getY() - A.getY();
		double ABz = B.getZ() - A.getZ();
		double CDx = D.getX() - C.getX();
		double CDy = D.getY() - C.getY();
		double CDz = D.getZ() - C.getZ();
		return ABx*CDx + ABy*CDy + ABz*CDz;
	}

	/**
	 * Creates a new vector with given X and Y components.
	 * 
	 * @param x
	 *            the x component
	 * @param y
	 *            the y component
	 * @param z
	 *            the z component
	 * @return a new vector
	 */
	public static Vector3D create(double x, double y, double z) {
		return new Vector3D(x, y, z);
	}

	/**
	 * Creates a vector from a {@link Coordinates}.
	 * 
	 * @param coord
	 *            the Point to copy
	 * @return a new vector
	 */
	public static Vector3D create(Point coord) {
		return new Vector3D(coord);
	}

	public Vector3D(Point v) {
		x = v.getX();
		y = v.getY();
		z = v.getZ();
	}

	/**
	 * Computes the 3D dot-product of two {@link Coordinates}s.
	 * 
   * @param v1 the first vector
   * @param v2 the second vector
	 * @return the dot product of the vectors
	 */
	public static double dot(Point v1, Point v2) {
		return v1.getX() * v2.getX() + v1.getY() * v2.getY() + v1.getZ() * v2.getZ();
	}

	private double x;
	private double y;
	private double z;

	public Vector3D(Point from, Point to) {
		x = to.getX() - from.getX();
		y = to.getY() - from.getY();
		z = to.getZ() - from.getZ();
	}

	public Vector3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}


	/**
	 * Computes the dot-product of two vectors
	 * 
	 * @param v
	 *            a vector
	 * @return the dot product of the vectors
	 */
	public double dot(Vector3D v) {
		return x * v.x + y * v.y + z * v.z;
	}

	public double length() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	public static double length(Point v) {
		return Math.sqrt(v.getX() * v.getX() + v.getY() * v.getY() + v.getZ() * v.getZ());
	}

	public Vector3D normalize() {
		double length = length();
		if (length > 0.0)
			return divide(length());
		return create(0.0, 0.0, 0.0);
	}

	private Vector3D divide(double d) {
		return create(x / d, y / d, z / d);
	}

	public static Point normalize(Point v) {
		double len = length(v);
		return new Coordinate((double)v.getX() / len, v.getY() / len, v.getZ() / len);
	}
	  /**
	   * Gets a string representation of this vector
	   * 
	   * @return a string representing this vector
	   */
		public String toString() {
			return "[" + x + ", " + y + ", " + z + "]";
		}
		

}
