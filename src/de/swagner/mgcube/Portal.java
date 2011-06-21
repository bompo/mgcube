package de.swagner.mgcube;

import com.badlogic.gdx.math.Vector3;


public class Portal {
	
	public static int idCnt = 0;

	public int id= 0;
	
	public Vector3 position = new Vector3(-11,-11,-11);
	
	public Portal(int id) {
		this.id = id;
		idCnt++;
	}
	
	public Portal() {
		//just a temp portal
	}
}
