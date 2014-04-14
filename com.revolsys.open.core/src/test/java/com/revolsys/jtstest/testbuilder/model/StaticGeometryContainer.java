package com.revolsys.jtstest.testbuilder.model;

import com.revolsys.jts.geom.Geometry;

public class StaticGeometryContainer implements GeometryContainer {

	private Geometry geometry;
	
	public StaticGeometryContainer(Geometry geometry)
	{
		this.geometry = geometry;
	}
	
	public Geometry getGeometry() {
		return geometry;
	}

}
