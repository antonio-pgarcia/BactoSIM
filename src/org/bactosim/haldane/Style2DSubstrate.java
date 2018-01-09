package org.bactosim.haldane;

import java.awt.Color;

import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.visualizationOGL2D.ValueLayerStyleOGL;

public class Style2DSubstrate implements ValueLayerStyleOGL {
	protected ValueLayer layer;
	//private Color tan = new Color(205, 133, 63);
	private Color tan0 = new Color(0, 71, 89);
	private Color tan1 = new Color(0, 71, 89, 0x33);

	public void init(ValueLayer layer) {
		this.layer = layer;
	}

	public float getCellSize() {
		return 15.0f;
	}

	/**
	 * Return the color based on the value at given coordinates.
	 */
	public Color getColor(double... coordinates) {
		double v = layer.get(coordinates);
		
		if (v > 0)
			return tan0;
		else
			//return Color.GRAY;
			return tan1;
			
	}
}