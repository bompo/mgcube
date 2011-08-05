package de.swagner.mgcube;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;

public class IntroScreen extends DefaultScreen implements InputProcessor {

	float startTime = 0;
	PerspectiveCamera cam;
	Mesh quadModel;
	float angleX = 0;
	float angleY = 0;
	SpriteBatch batch;
	BitmapFont font;
	SpriteBatch fadeBatch;
	Sprite blackFade;
	Sprite title;
	float fade = 1.0f;
	boolean finished = false;
	
	// GLES20
	Matrix4 model = new Matrix4().idt();
	Matrix4 modelView = new Matrix4().idt();
	Matrix4 modelViewProjection = new Matrix4().idt();
	Matrix4 tmp = new Matrix4().idt();
	private ShaderProgram bloomShader;
	FrameBuffer frameBuffer;
	FrameBuffer frameBufferVert;

	public IntroScreen(Game game) {
		super(game);
		Gdx.input.setInputProcessor(this);
		
		Gdx.app.log("", Gdx.graphics.getType().toString());
		
		if(Resources.getInstance().fullscreenOnOff)
			Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode().width, Gdx.graphics.getDesktopDisplayMode().height, true);
		
		title = new Sprite(new Texture(Gdx.files.internal("data/logo.png")));
		blackFade = new Sprite(new Texture(Gdx.files.internal("data/blackfade.png")));

		quadModel = Resources.getInstance().quadModel;

		cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0, 0, 16f);
		cam.direction.set(0, 0, -1);
		cam.up.set(0, 1, 0);
		cam.near = 1f;
		cam.far = 1000;
		
		// controller = new PerspectiveCamController(cam);
		// Gdx.input.setInputProcessor(controller);

		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		font = new BitmapFont();
		
		fadeBatch = new SpriteBatch();
		fadeBatch.getProjectionMatrix().setToOrtho2D(0, 0, 2, 2);

		bloomShader = Resources.getInstance().bloomShader;
		
		initRender();
	}
	
	public void initRender() {
		Gdx.graphics.getGL20().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		frameBuffer = new FrameBuffer(Format.RGB565, Resources.getInstance().m_i32TexSize, Resources.getInstance().m_i32TexSize, false);		
		frameBufferVert = new FrameBuffer(Format.RGB565, Resources.getInstance().m_i32TexSize, Resources.getInstance().m_i32TexSize, false);
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		initRender();
	}
	
	@Override
	public void show() {
	}

	@Override
	public void render(float delta) {
		delta = Math.min(0.06f, delta);
		
		startTime += delta;

		if(startTime>2.5f) finished = true;
		
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		frameBuffer.begin();
		Gdx.graphics.getGL20().glViewport(0, 0, frameBuffer.getWidth(), frameBuffer.getHeight());

		Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
		Gdx.graphics.getGL20().glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		cam.update();
		
		batch.begin();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		batch.draw(title, 0, 0);
		batch.end();
		
		frameBuffer.end();
		
		//PostProcessing
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDisable(GL20.GL_BLEND);
		
		frameBuffer.getColorBufferTexture().bind(0);

		frameBufferVert.begin();
		bloomShader.begin();
		bloomShader.setUniformi("sTexture", 0);
		bloomShader.setUniformf("bloomFactor", (MathUtils.sin(startTime * 1f) * 0.1f) + 0.5f);
		bloomShader.setUniformf("TexelOffsetX", Resources.getInstance().m_fTexelOffset);
		bloomShader.setUniformf("TexelOffsetY", 0.0f);
		quadModel.render(bloomShader, GL20.GL_TRIANGLE_STRIP);
		bloomShader.end(); 
		frameBufferVert.end();
		
		
		frameBufferVert.getColorBufferTexture().bind(0);
		
		frameBuffer.begin();		
		bloomShader.begin();
		bloomShader.setUniformi("sTexture", 0);
		bloomShader.setUniformf("bloomFactor", (MathUtils.sin(startTime * 1f) * 0.1f) + 1f);
		bloomShader.setUniformf("TexelOffsetX", 0.0f);
		bloomShader.setUniformf("TexelOffsetY", Resources.getInstance().m_fTexelOffset);
		quadModel.render(bloomShader, GL20.GL_TRIANGLE_STRIP);
		bloomShader.end(); 
		frameBuffer.end();
		

		
		Gdx.graphics.getGL20().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		batch.begin();
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		batch.draw(title, 0, 0);	
		batch.draw(frameBuffer.getColorBufferTexture(), 0, 0,800,480,0,0,frameBuffer.getWidth(),frameBuffer.getHeight(),false,true);
		batch.end();
		
		if (!finished && fade > 0) {
			fade = Math.max(fade - delta / 2.f, 0);
			fadeBatch.begin();
			blackFade.setColor(blackFade.getColor().r, blackFade.getColor().g, blackFade.getColor().b, fade);
			blackFade.draw(fadeBatch);
			fadeBatch.end();
		}

		if (finished) {
			fade = Math.min(fade + delta / 2.f, 1);
			fadeBatch.begin();
			blackFade.setColor(blackFade.getColor().r, blackFade.getColor().g, blackFade.getColor().b, fade);
			blackFade.draw(fadeBatch);
			fadeBatch.end();
			if (fade >= 1) {
				game.setScreen(new MainMenuScreen(game));
			}
		}
		
	}

	@Override
	public void hide() {
	}
	
	@Override
	public void dispose() {
		frameBuffer.dispose();
		frameBufferVert.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		if (Gdx.input.isTouched())
			return false;

		if (keycode == Input.Keys.SPACE) {
			finished = true;
		}
		
		if (keycode == Input.Keys.F) {
			if(Gdx.app.getType() == ApplicationType.Desktop) {
				if(!org.lwjgl.opengl.Display.isFullscreen()) {
					Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode().width, Gdx.graphics.getDesktopDisplayMode().height, true);		
				} else {
					Gdx.graphics.setDisplayMode(800,480, false);		
				}
				Resources.getInstance().prefs.putBoolean("fullscreen", !Resources.getInstance().prefs.getBoolean("fullscreen"));
				Resources.getInstance().fullscreenOnOff = !Resources.getInstance().prefs.getBoolean("fullscreen");
				Resources.getInstance().prefs.flush();
			}
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		x = (int) (x / (float) Gdx.graphics.getWidth() * 800);
		y = (int) (y / (float) Gdx.graphics.getHeight() * 480);

		finished = true;
		
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		x = (int) (x / (float) Gdx.graphics.getWidth() * 800);
		y = (int) (y / (float) Gdx.graphics.getHeight() * 480);

		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		x = (int) (x / (float) Gdx.graphics.getWidth() * 800);
		y = (int) (y / (float) Gdx.graphics.getHeight() * 480);

		return false;
	}

	@Override
	public boolean touchMoved(int x, int y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
			cam.translate(0, 0, 1 * amount);
		if((cam.position.z < 2 && amount < -0) || (cam.position.z > 20 && amount > 0))
			cam.translate(0, 0, 1 * -amount);
		return false;
	}

}
