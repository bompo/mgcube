package de.swagner.mgcube;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class Player extends Renderable {
	
	public Vector3 direction = new Vector3(0,0,-1);
	public Vector3 moveDirection = new Vector3(0,0,-1);

	public boolean isMoving = false;
	
	public void move() {
		this.setDirection();
		moveDirection.set(direction);
		if(Resources.getInstance().musicOnOff) {
			Resources.getInstance().move.play();
		}
		this.isMoving = true;
	}
	
	public void stop() {
		this.position.x = MathUtils.ceil(this.position.x-0.5f);
		this.position.y = MathUtils.ceil(this.position.y-0.5f);
		this.position.z = MathUtils.ceil(this.position.z-0.5f);
		if(this.position.x%2 !=0) this.position.x -=1;
		if(this.position.y%2 !=0) this.position.y -=1;
		if(this.position.z%2 !=0) this.position.z -=1;
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
