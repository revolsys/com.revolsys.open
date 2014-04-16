package com.revolsys.jtstest.testbuilder.geom;

import com.revolsys.jts.geom.BoundingBox;

public class EnvelopeUtil 
{
	public static double minExtent(BoundingBox env)
	{
		double w = env.getWidth();
		double h = env.getHeight();
		if (w < h) return w;
		return h;
	}
	public static double maxExtent(BoundingBox env)
	{
		double w = env.getWidth();
		double h = env.getHeight();
		if (w > h) return w;
		return h;
	}
}
