package org.bactosim.haldane;

import java.awt.Color;
import java.awt.geom.RoundRectangle2D;
//import javax.vecmath.Point3f;
//import saf.v3d.scene.VShape;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;

import saf.v3d.scene.VSpatial;

public class Style2DVEColi extends DefaultStyleOGL2D {
	private Color DONOR = new Color(224, 0, 78);
	//private Color TRANS = new Color(84, 106, 150);
	private Color TRANS = new Color(255, 255, 153, 0x33);
	private Color RECIP = new Color(0, 255, 51, 0x33);
	
	@Override
	public Color getColor(Object o){
		
		if (o instanceof VEColi) {
			if(((VEColi) o).isR())
				return RECIP;
			if(((VEColi) o).isD())
				return DONOR;
			if(((VEColi) o).isT())
				return TRANS;
		} 
		return null;
	}
	
	@Override
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
		int length = 8 + (int) Math.round(12 * ((VEColi) agent).getElongation());
		RoundRectangle2D.Double rect= new RoundRectangle2D.Double(0,0,5,length,6,6);
		return shapeFactory.createShape(rect);
		//return shapeFactory.createRectangle(5, length);
		//if (spatial == null) return shapeFactory.createRectangle(5, length);
		//return super.getVSpatial(agent, spatial);
	}
	
	@Override
	public float getRotation(Object agent) {
		return (float) ((VEColi) agent).getHeading();
	}
	
	@Override
	public float getScale(Object o) {
		if (o instanceof VEColi)
			return 1f;
		return 0.5f;
	}
}
