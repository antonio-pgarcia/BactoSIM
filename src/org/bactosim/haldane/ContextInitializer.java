package org.bactosim.haldane;

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
				new GridBuilderParameters<VEColi>(new repast.simphony.space.grid.WrapAroundBorders(),
						new RandomGridAdder<VEColi>(), true, XMAX, YMAX));

		ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null)
		.createContinuousSpace("continuous-space", context, new AdderCFU<VEColi>(N,4),
				new repast.simphony.space.continuous.WrapAroundBorders(), XMAX, YMAX, 1);
		
		GridValueLayer vl = new GridValueLayer("substrate", true, 
				new repast.simphony.space.grid.WrapAroundBorders(),XMAX,YMAX);
		
		
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
		EnginePhy cellEngine = new EnginePhy();
		context.add(cellEngine);  
		
		return context;
	}
	
}
