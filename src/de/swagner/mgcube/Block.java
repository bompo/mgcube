package de.swagner.mgcube;

import com.badlogic.gdx.math.Vector3;

public class Block implements Comparable {

	public Vector3 position;
	public float sortPosition;

	public Block(Vector3 position) {
		this.position = position;
	}

	@Override
	public int compareTo(Object o) {
		if(!(o instanceof Block)) return -1;
		if(((Block)o).sortPosition<this.sortPosition) return -1;
		return 1;
	}
}
