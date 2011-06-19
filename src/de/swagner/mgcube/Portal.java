package de.swagner.mgcube;

import com.badlogic.gdx.math.Vector3;


public class Portal {
	
	public static int idCnt = 0;

	public int id= 0;
	
	public Vector3 enterPosition = new Vector3(-1,-1,-1);
	public Vector3 exitPosition = new Vector3(-1,-1,-1);
	
	public Portal() {
		id = idCnt;
		idCnt++;
	}
}
