package de.swagner.mgcube;

import com.badlogic.gdx.math.collision.BoundingBox;

public class LevelButton {

	public BoundingBox box = new BoundingBox();
	public int[][][] level;
	public int levelnumber = 0;
	public boolean selected = false;
	
	public LevelButton(int[][][] lvl, int levelnumber) {
		level = lvl;
		this.levelnumber = levelnumber;
	}
	
}
