package de.swagner.mgcube;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

public abstract class DefaultScreen implements Screen {
	protected Game game;

	public DefaultScreen(Game game) {
		Resources.getInstance().reInit();
		this.game = game;
	}

	@Override
	public void resize(int width, int height) {
		Resources.getInstance().reInit();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
		Resources.getInstance().reInit();
	}

	@Override
	public void dispose() {
		Resources.getInstance().dispose();
	}
}
