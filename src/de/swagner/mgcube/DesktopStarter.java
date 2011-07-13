package de.swagner.mgcube;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopStarter extends Game {

	public static void main(String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "qb";

//		config.width = 800;
//		config.height = 480;
		config.setFromDisplayMode(LwjglApplicationConfiguration.getDesktopDisplayMode());
		

		config.fullscreen = true;
		config.samples = 4;
		config.useGL20 = true;
		config.r = 5;
		config.g = 6;
		config.b = 5;
		config.a = 0;
		new LwjglApplication(new DesktopStarter(), config);
	}

	@Override
	public void create() {
		setScreen(new IntroScreen(this));
	}

}
