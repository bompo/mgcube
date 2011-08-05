package de.swagner.mgcube;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

public abstract class DefaultScreen implements Screen {
	protected Game game;

	public DefaultScreen(Game game) {
		this.game = game;
	}

	@Override
	public void resize(int width, int height) {
		Resources.getInstance().initShader();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		Resources.getInstance().dispose();
	}
}
