package de.swagner.mgcube;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class EditorBlock extends Renderable {
	
	public Vector3 direction = new Vector3(0,0,-1);
	public Vector3 up = new Vector3(0,1,0);
	
	public EditorBlock(Vector3 position) {
		this.position = position;
	}
	
	public void moveRight() {
		this.setDirection();
		direction.rot(new Matrix4().setToRotation(Vector3.Y, -90));
		position.add(direction.x*2, direction.y*2, direction.z*2);
	}	
	
	public void moveLeft() {
		this.setDirection();
		direction.rot(new Matrix4().setToRotation(Vector3.Y, 90));
		position.add(direction.x*2, direction.y*2, direction.z*2);
	}	
	
	public void moveUp() {
		this.setUp();
		up.rot(new Matrix4().setToRotation(Vector3.Y, -90));
		position.add(up.x*2, up.y*2, up.z*2);
	}	
	
	public void moveDown() {
		this.setUp();
		up.rot(new Matrix4().setToRotation(Vector3.Y, 90));
		position.add(up.x*2, up.y*2, up.z*2);
	}	
	
	public void setDirection() {
		if (Math.abs(this.direction.x) > Math.abs(this.direction.y) && Math.abs(this.direction.x) > Math.abs(this.direction.z)) {
			while (this.direction.x != -1 && this.direction.x != 1) {
				if (this.direction.x < 0)
					this.direction.x--;
				else
					this.direction.x++;
				if (this.direction.x < -1)
					this.direction.x = -1;
				if (this.direction.x > 1)
					this.direction.x = 1;
				this.direction.y = 0;
				this.direction.z = 0;
			}
		}
		if (Math.abs(this.direction.y) > Math.abs(this.direction.x) && Math.abs(this.direction.y) > Math.abs(this.direction.z)) {
			while (this.direction.y != -1 && this.direction.y != 1) {
				if (this.direction.y < 0)
					this.direction.y--;
				else
					this.direction.y++;
				if (this.direction.y < -1)
					this.direction.y = -1;
				if (this.direction.y > 1)
					this.direction.y = 1;
				this.direction.x = 0;
				this.direction.z = 0;
			}
		}
		if (Math.abs(this.direction.z) > Math.abs(this.direction.y) && Math.abs(this.direction.z) > Math.abs(this.direction.y)) {
			while (this.direction.z != -1 && this.direction.z != 1) {
				if (this.direction.z < 0)
					this.direction.z--;
				else
					this.direction.z++;
				if (this.direction.z < -1)
					this.direction.z = -1;
				if (this.direction.z > 1)
					this.direction.z = 1;
				this.direction.y = 0;
				this.direction.x = 0;
			}
		}
	}
	
	public void setUp() {
		if (Math.abs(this.up.x) > Math.abs(this.up.y) && Math.abs(this.up.x) > Math.abs(this.up.z)) {
			while (this.up.x != -1 && this.up.x != 1) {
				if (this.up.x < 0)
					this.up.x--;
				else
					this.up.x++;
				if (this.up.x < -1)
					this.up.x = -1;
				if (this.up.x > 1)
					this.up.x = 1;
				this.up.y = 0;
				this.up.z = 0;
			}
		}
		if (Math.abs(this.up.y) > Math.abs(this.up.x) && Math.abs(this.up.y) > Math.abs(this.up.z)) {
			while (this.up.y != -1 && this.up.y != 1) {
				if (this.up.y < 0)
					this.up.y--;
				else
					this.up.y++;
				if (this.up.y < -1)
					this.up.y = -1;
				if (this.up.y > 1)
					this.up.y = 1;
				this.up.x = 0;
				this.up.z = 0;
			}
		}
		if (Math.abs(this.up.z) > Math.abs(this.up.y) && Math.abs(this.up.z) > Math.abs(this.up.y)) {
			while (this.up.z != -1 && this.up.z != 1) {
				if (this.up.z < 0)
					this.up.z--;
				else
					this.up.z++;
				if (this.up.z < -1)
					this.up.z = -1;
				if (this.up.z > 1)
					this.up.z = 1;
				this.up.y = 0;
				this.up.x = 0;
			}
		}
	}
}
