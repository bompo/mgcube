package de.swagner.mgcube;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.jogl.JoglApplication;

public class DesktopStarter extends Game {
	
	public static void main(String[] args) {
		new JoglApplication(new DesktopStarter(),
				"MG Cube", 800, 480,false);
	}
	
	@Override 
	public void create () {
		setScreen(new GameScreen(this));
		
		//commit
	}

}
