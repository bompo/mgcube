package de.swagner.mgcube;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class Renderable implements Comparable {
	
	public Vector3 position = new Vector3();
	public float sortPosition;
	
	public Matrix4 model = new Matrix4();
	
	@Override
	public int compareTo(Object o) {
		if(!(o instanceof Renderable)) return -1;
		if(((Renderable)o).sortPosition<this.sortPosition) return -1;
		return 1;
	}

}
