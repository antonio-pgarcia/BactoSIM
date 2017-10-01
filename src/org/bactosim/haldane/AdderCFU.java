package org.bactosim.haldane;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.Dimensions;
import repast.simphony.space.continuous.ContinuousAdder;
import repast.simphony.space.continuous.ContinuousSpace;

public class AdderCFU <T> implements ContinuousAdder<T> {
	private double radius;
	
	public AdderCFU(double N0, double S){
		radius= Math.sqrt((N0 * S)/Math.PI);
	}
	
	public void add(ContinuousSpace<T> space, T obj) {
		Dimensions dims = space.getDimensions();
		double[] location = new double[dims.size()];
		findLocation(location, dims);
		while (!space.moveTo(obj, location)) {
			findLocation(location, dims);
		}
	}

	private void findLocation(double[] location, Dimensions limits) {
		double alpha= 2 * Math.PI * RandomHelper.getUniform().nextDouble();
		double U= Math.sqrt(RandomHelper.getUniform().nextDouble());
		double x= radius * U * Math.cos(alpha) + limits.getDimension(0)/2;
		double y= radius * U * Math.sin(alpha) + limits.getDimension(1)/2;
		location[0] = x;
		location[1] = y;
		location[2] = 1;
	}
}
