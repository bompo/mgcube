package de.swagner.mgcube;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;

public class Player {
	public Vector3 position = new Vector3();
	public Vector3 direction = new Vector3(0,0,-1);
	
	public boolean isMoving = false;
	
	public void move() {
		this.setDirection();
		Resources.getInstance().move.play();
		this.isMoving = true;
	}
	
	public void stop() {
		this.isMoving = false;
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
}
