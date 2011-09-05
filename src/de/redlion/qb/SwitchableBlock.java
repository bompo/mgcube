package de.redlion.qb;

import com.badlogic.gdx.math.Vector3;

public class SwitchableBlock extends Renderable{

	public boolean isSwitched = false;
	public boolean isSwitchAnimation = false;
	public float switchAnimation = 0.0f;
	
	int id = 0;
	
	public SwitchableBlock(Vector3 position) {
		this.position = position;
	}
	
}
