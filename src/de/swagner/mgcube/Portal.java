package de.swagner.mgcube;

import com.badlogic.gdx.math.Vector3;


public class Portal extends Renderable {
	
	public static int idCnt = 0;

	public int id= 0;
	
	public Portal(int id) { 
		position = new Vector3(-11,-11,-11);
		this.id = id;
		idCnt++;
	}
	
	public Portal() {
		position = new Vector3(-11,-11,-11);
	}
}
