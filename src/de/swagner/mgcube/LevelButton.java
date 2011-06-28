package de.swagner.mgcube;

import com.badlogic.gdx.math.collision.BoundingBox;

public class LevelButton {

	public BoundingBox box = new BoundingBox();
	public int levelnumber = 0;
	public boolean selected = false;
	
	public LevelButton(int levelnumber) {
		this.levelnumber = levelnumber;
	}
	
}
