package org.bactosim.haldane;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;

@SuppressWarnings({"rawtypes", "unchecked"})
public class EnginePhy {
	private Class clazz = null;
	
	public EnginePhy() {
		clazz = (new VEColi()).getClass(); 
	}
	
	@ScheduledMethod(start = 1, interval = 10, shuffle=true)
	public void step() {
		Context context = (Context) ContextUtils.getContext(this);
		IndexedIterable<VEColi> agents = context.getObjects(clazz);
		
		for (VEColi agent : agents) {
			System.out.println("heading= " + agent.getHeading() + " length= " + agent.getElongation());
		}
	}

}
