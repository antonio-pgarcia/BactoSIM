package org.bactosim.haldane;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.valueLayer.GridValueLayer;

@SuppressWarnings("rawtypes")
public class VEColi {
	private Context context = null;
	private GridValueLayer vl;
	private Grid grid;
	private ContinuousSpace space;
	
	// Model parameters & constants 
	Parameters p = RunEnvironment.getInstance().getParameters();
	private int doublingTime = (Integer) p.getValue("doublingTime");
	
	private double P = ((double) p.getValue("P"));
	private int Cost = ((Integer) p.getValue("Cost"))/100;

	private double cov = 0.10;
	private double cyclePoint = 0.70;
	private double minlen = 0.8;		// Min capsule length
	private double maxlen = 2.0;		// Max capsule length
	
	
	// Vegetative state variables
	private double heading;    			// The Agent heading in degrees
	private double mass;       			// The Agent mass
	private double length;				// The Agent length 
	private double width = 0.5;			// The Agent width (0.5 micrometers)
	private double divisionMass;		// The Agent mass at division
	
	// Epidemic state variables
	private boolean infected0  = false; // The initial 
	private boolean infected  = false;  // 
	private boolean pgamma = false;		// The outcome of probability P(gamma0) for this Agent 				
	
	
	public VEColi() {
		Defaults();
	}
	
	public VEColi(boolean v) {
		Defaults();
		infected0 = infected = v;
		if(infected) divisionMass= addCost(divisionMass, Cost); 
	}
	
	public VEColi(double h, double m, boolean i0, boolean i1) {
		heading= h;
		mass= m;
		divisionMass = Math.round( (2 * mass) * cov * RandomHelper.getNormal().nextDouble() + (2 * mass)); 
		length = minlen + (mass/divisionMass * (maxlen - minlen)); 
		infected0 = i0;
		infected = i1;		
	}

	private void Defaults() {
		heading= RandomHelper.getUniform().nextDouble() * 360;
		mass= Math.round(doublingTime * cov * RandomHelper.getNormal().nextDouble() + doublingTime);
		divisionMass = 2 * Math.round(doublingTime * cov * RandomHelper.getNormal().nextDouble() + doublingTime);
		length = minlen + (mass/divisionMass * (maxlen - minlen)); 
	}
	
	@SuppressWarnings("unchecked")
	private void Context() {
		if(null != context) return;
		
		context = (Context) ContextUtils.getContext(this);
		vl = (GridValueLayer) context.getValueLayer("substrate");
		grid = (Grid) context.getProjection("grid-space");
		space = (ContinuousSpace) context.getProjection("continuous-space");
		
		// Put agent into appropriate grid position
		NdPoint point = space.getLocation(this);
		grid.moveTo(this, (int) point.getX(), (int) point.getY());
		
		if(RandomHelper.getUniform().nextDouble() < P) pgamma = true;
	}
	
	/**
	 * Adds a cost
	 * 
	 * @param v The reference value
	 * @param c The cost
	 * @return The value increased by the cost c
	 */
	public double addCost(double v, double c) {
		return v + v * c;
	}
	
	public double getHeading() {
		return heading;
	}
	
	public double getMass() {
		return mass;
	}
	
	public double getLength() {
		return length;
	}
	
	public double getElongation() {
		return length / maxlen;
	}
	
	public double getWidth() {
		return width;
	}
	
	/**
	 * This Agent is a Recipient (R)
	 * 
	 * @return true if the current Agent is a plasmid free recipient
	 */
	public boolean isR() {
		return !infected;  
	}
	
	/**
	 * This Agent is a Donor (D)
	 * 
	 * @return true if the current Agent is a plasmid donor
	 */
	public boolean isD() {
		return !isR();
	}
	
	/**
	 * This Agent is a Transconjugant (T)
	 * 
	 * @return true if the current Agent is a plasmid Transconjugant
	 */
	public boolean isT() {
		return isD() & infected0;
	}
	
	/**
	 * Infects the current Agent.
	 */
	public void receiveP() {
		infected= true;
		divisionMass= addCost(divisionMass, Cost); 
	}
	
	/**
	 * Add additive noise to some value v
	 * 
	 * @param v The value 
	 * @param m The coefficient of variation
	 * @return The value v with random noise
	 */
	public double addNoise(double v, double m) {
		double noise = v * Math.random() * m;
		if(0.5 < Math.random()) 
			return v + noise;
		else
			return v - noise;
	}
	
	/**
	 * Uptake the required amount of nutrients from the underlying Repast ValueLayer.
	 * 
	 * @param r Amount of nutrients
	 * @return The effective amount of nutrients comsumed
	 */
	public double Uptake(double r) {
		double v= 0;
		NdPoint point = space.getLocation(this); 
		double c= vl.get(point.getX(),point.getY());

		if(c > 0) {
			v=(c >= r ? r : c);
			vl.set(c-v, (int) point.getX(), (int) point.getY());
		} 
		return v;
	}
	
	/**
	 * Nutrient uptake and growth sub-model.
	 * 
	 * @return The nutrient particle consumed, if any.
	 */
	public double Growth() {
		double v = Uptake(1);
		mass= mass + v;
		length = minlen + (mass/divisionMass * (maxlen - minlen)); 
		return v;
	}
	
	/**
	 * Bacterial division sub-model
	 */
	@SuppressWarnings("unchecked")
	public void Division() {
		if( divisionMass < mass) {
			VEColi vecoli = new VEColi(heading + (-45 + 90 * Math.random()), mass - addNoise(mass/2,cov), infected0, infected);
			mass = mass - vecoli.getMass();
			if(context.add(vecoli)) {
				NdPoint point = space.getLocation(this);
				space.moveTo(vecoli,point.getX() + 1 * Math.sin(Math.toRadians(heading)), point.getY() + 1 * Math.cos(Math.toRadians(heading)),point.getZ());
			}
		}
	}
	
	/**
	 * Simplified conjugation sub-model.
	 */
	public void Conjugation() {
		if(!isD() & !isT()) return;
		if(!pgamma) return;
		if(mass < divisionMass * cyclePoint) return;
		GridPoint p = grid.getLocation(this);
		VEColi vecoli = null;
		
		for (Object o: grid.getObjectsAt(p.getX(),p.getY())){
			if (o instanceof VEColi) 
				if(((VEColi) o).isR()) {
					vecoli = (VEColi) o;
					break;
				}
		}
		
		if(null != vecoli) {
			vecoli.receiveP();
		}
	}
	
	/**
	 * Schedule the execution of Agent logic.
	 */
	@ScheduledMethod(start = 1, interval = 1, shuffle=true)
	public void step() {
		Context();
		if(Growth() > 0) {
			Conjugation();
			Division();
		}
	}
}
