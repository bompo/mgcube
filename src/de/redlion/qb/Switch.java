package de.redlion.qb;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Switch extends Renderable{

	public boolean isSwitched = false;
	public float switchAnimation = 0.0f;
	
	int id = 0;
	Array<SwitchableBlock> sBlocks = new Array<SwitchableBlock>();
	
	public Switch(Vector3 position) {
		this.position = position;
	}
	
}
