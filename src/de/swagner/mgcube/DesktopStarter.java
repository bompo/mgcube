package de.swagner.mgcube;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.jogl.JoglApplication;
import com.scoreloop.client.android.core.controller.ScoresController;
import com.scoreloop.client.android.core.ui.ScoreloopCustomDialog;

public class DesktopStarter extends Game {
	
	public static void main(String[] args) {
		new JoglApplication(new DesktopStarter(),
				"MG Cube", 800, 480,true);
	}
	
	@Override 
	public void create () {
		setScreen(new IntroScreen(this));
			}

}
