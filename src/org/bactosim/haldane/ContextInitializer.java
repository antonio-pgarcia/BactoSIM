package org.bactosim.haldane;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.RandomGridAdder;
import repast.simphony.valueLayer.GridValueLayer;

@SuppressWarnings("rawtypes")
public class ContextInitializer implements ContextBuilder {
	private final static int XMAX = 100;
	private final static int YMAX = 100;
	private final static int GENERATIONS = 10;
	
	@SuppressWarnings({"unchecked" })
	public Context build(Context context) {
		int N = (int) ((XMAX * YMAX) * 0.05);
		
		RandomHelper.createNormal(0, 1);
		GridFactoryFinder.createGridFactory(null).createGrid("grid-space", context,
				//new GridBuilderParameters<VEColi>(new repast.simphony.space.grid.WrapAroundBorders(),
				new GridBuilderParameters<VEColi>(new repast.simphony.space.grid.InfiniteBorders(),
						new RandomGridAdder<VEColi>(), true, XMAX, YMAX));

		ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null)
		.createContinuousSpace("continuous-space", context, new AdderCFU<VEColi>(N,2),
				new repast.simphony.space.continuous.InfiniteBorders(), XMAX, YMAX, 1);
		
		GridValueLayer vl = new GridValueLayer("substrate", true, 
				new repast.simphony.space.grid.InfiniteBorders(),XMAX,YMAX);
		
		
		// Acquire the instance parameters
		Parameters p = RunEnvironment.getInstance().getParameters();
		
		int doublingTime = (Integer) p.getValue("doublingTime");
		double donorDensity = (Double) p.getValue("donorDensity");
		
		// Create the initial population of R
		for (int i = 0; i < N * (1-donorDensity); i++) {
			VEColi vecoli = new VEColi();          
			context.add(vecoli);                  
		}
		
		// Create the initial population f D
		for (int i = 0; i < N * donorDensity; i++) {
			VEColi vecoli = new VEColi(true);          
			context.add(vecoli);                  
		}
		
		// Initialize the substrate for supporting N generations 
		for (int x=0; x< XMAX; x++){
			for (int y=0; y< YMAX; y++){
				vl.set(doublingTime * GENERATIONS,x,y);
			}
		}
		context.addValueLayer(vl);
		
		// Initialize the simple diffusion engine
		EngineDiffusion diffusion = new EngineDiffusion(vl);
		context.add(diffusion);  
		
		// Instantiate the CellEngine implementation
		EnginePhy cellEngine = new EnginePhy(XMAX/2, YMAX/2, XMAX, YMAX);
		context.add(cellEngine);  
		
		double heading = 45;
		double h1 = ((450-heading)%360);
		double xx1 = Math.cos(Math.toRadians(h1));
		double yy1 = Math.sin(Math.toRadians(h1));
				
		double alpha = Math.toDegrees(Math.atan2(yy1 , xx1));
		double h2 = ((450-alpha)%360);
		System.out.println("*** h: " + h2);
		double x0 = 2;
		double y0 = 1;
		double x1 = 3;
		double y1 = 2;
		Vector3D v3d1 = new Vector3D(new double[] {x0,y0,0});
		Vector3D v3d2 = new Vector3D(new double[] {x1,y1,0});
		Vector3D v3d3 = v3d2.subtract(v3d1);
		Vector3D v3d = new Vector3D(new double[] {x1-x0,y1-y0,0});
		System.out.println("*** vec: " + v3d3.normalize().toString());
		//Math.atan2(v3d.normalize().getY(),v3d.normalize().getY())
		return context;
	}
	
}




