package de.swagner.mgcube;

import com.badlogic.gdx.math.Vector3;

public class SwitchableBlock extends Renderable{

	boolean isSwitched = false;
	int id = 0;
	
	public SwitchableBlock(Vector3 position) {
		this.position = position;
	}
	
}
