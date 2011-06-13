package de.swagner.mgcube;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.loaders.collada.ColladaLoader;
import com.badlogic.gdx.graphics.g3d.loaders.wavefront.ObjLoader;
import com.badlogic.gdx.graphics.g3d.model.Model;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.test.utils.PerspectiveCamController;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Interpolator;

public class GameScreen extends DefaultScreen implements InputProcessor {

	double startTime = 0;
	PerspectiveCamera cam;
	PerspectiveCamController controller;
	StillModel model;
	Texture[] textures = null;
	boolean hasNormals = false;
	BoundingBox bounds = new BoundingBox();
	ImmediateModeRenderer renderer;
	float angleX = 0;
	float angleY = 0;
	float angleXTarget = 0;
	float angleYTarget = 0;
	String fileName;
	String[] textureFileNames;
	FPSLogger fps = new FPSLogger();
	SpriteBatch batch;
	BitmapFont font;
	Texture diffuse;
	Texture lightMaps;
	boolean animate = false;

	public GameScreen(Game game) {
		super(game);
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setInputProcessor(this);

		model = ColladaLoader.loadStillModel(Gdx.files.internal("data/cube.dae"));
		lightMaps = new Texture(Gdx.files.internal("data/qbob/world_blobbie_lm_01.jpg"), true);

		diffuse = new Texture(Gdx.files.internal("data/qbob/world_blobbie_blocks.png"), true);

		cam = new PerspectiveCamera(45, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(00, 00, 4f);
		cam.direction.set(0, 0, -1);
		cam.up.set(0, 1, 0);
		cam.near = 1f;
		cam.far = 1000;

//		controller = new PerspectiveCamController(cam);
//		Gdx.input.setInputProcessor(controller);

		batch = new SpriteBatch();
		font = new BitmapFont();
		
		initRender();

	}
	
	private void initRender() {
		Gdx.graphics.getGL10().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		Gdx.graphics.getGL10().glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		float[] light_ambient = new float[] { 0.2f, 0.2f, 0.2f, 1.0f };
		float[] light_diffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		float[] light_specular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		/* light_position is NOT default value */
		float[] light_position0 = new float[] { 1.0f, 10.0f, 1.0f, 0.0f };
		float[] light_position1 = new float[] { -1.0f, -10.0f, -1.0f, 0.0f };

		Gdx.graphics.getGL10().glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, light_ambient, 0);
		Gdx.graphics.getGL10().glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, light_diffuse, 0);
		Gdx.graphics.getGL10().glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, light_specular, 0);
		Gdx.graphics.getGL10().glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, light_position0, 0);

		Gdx.graphics.getGL10().glLightfv(GL10.GL_LIGHT1, GL10.GL_AMBIENT, light_ambient, 0);
		Gdx.graphics.getGL10().glLightfv(GL10.GL_LIGHT1, GL10.GL_DIFFUSE, light_diffuse, 0);
		Gdx.graphics.getGL10().glLightfv(GL10.GL_LIGHT1, GL10.GL_SPECULAR, light_specular, 0);
		Gdx.graphics.getGL10().glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, light_position1, 0);

		Gdx.graphics.getGL10().glEnable(GL10.GL_LIGHTING);
		Gdx.graphics.getGL10().glEnable(GL10.GL_LIGHT0);
		Gdx.graphics.getGL10().glEnable(GL10.GL_LIGHT1);

		Gdx.graphics.getGL10().glShadeModel(GL10.GL_SMOOTH);
		Gdx.graphics.getGL10().glEnable(GL10.GL_DEPTH_TEST);
		Gdx.graphics.getGL10().glDepthFunc(GL10.GL_LESS);

		Gdx.graphics.getGL10().glClearColor(0.7f, 0.7f, 0.7f, 0f);
	}

	@Override
	public void show() {
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.8f, 0.8f, 0.8f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		cam.update();
		cam.apply(Gdx.gl10);

		Gdx.gl.glEnable(GL10.GL_CULL_FACE);
		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);

		
		if(animate) {
			if(angleXTarget < angleX) {
				angleX -= delta * 150f;
				if(angleX < angleXTarget) {
					angleX = angleXTarget;
					animate = false;
				}
			}
			if(angleXTarget > angleX) {
				angleX += delta * 150f;
				if(angleX > angleXTarget) {
					angleX = angleXTarget;
					animate = false;
				}
			}
			
			if(angleYTarget < angleY) {
				angleY -= delta * 150f;
				if(angleY < angleYTarget) {
					angleY = angleYTarget;
					animate = false;
				}
			}
			if(angleYTarget > angleY) {
				angleY += delta * 150f;
				if(angleY > angleYTarget) {
					angleY = angleYTarget;
					animate = false;
				}
			}
		}
		
		Gdx.gl11.glPushMatrix();
		Gdx.gl11.glRotatef(angleX, 0,1,0);
		Gdx.gl11.glRotatef(angleY, 0,0,1);
		model.render();
		Gdx.gl11.glPopMatrix();

		Gdx.gl.glDisable(GL10.GL_DEPTH_TEST);
		batch.begin();
		font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
		batch.end();

		fps.log();

	}

	private void setCombiners() {
		Gdx.gl11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_COMBINE);
		Gdx.gl11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_COMBINE_RGB, GL11.GL_ADD_SIGNED);
		Gdx.gl11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_SRC0_RGB, GL11.GL_PREVIOUS);
		Gdx.gl11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_SRC1_RGB, GL11.GL_TEXTURE);
	}

	@Override
	public void hide() {

	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Input.Keys.ESCAPE) {
			System.exit(0);
		}
		if (keycode == Input.Keys.LEFT && animate == false) {
			angleXTarget = angleX + 90;
			animate = true;
		}
		if (keycode == Input.Keys.RIGHT && animate == false) {
			angleXTarget = angleX - 90;
			animate = true;
		}
		if (keycode == Input.Keys.UP && animate == false) {
			angleYTarget = angleY + 90;
			animate = true;
		}
		if (keycode == Input.Keys.DOWN && animate == false) {
			angleYTarget = angleY - 90;
			animate = true;
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
		y = 480 - y;

		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		x = (int) (x / (float) Gdx.graphics.getWidth() * 800);
		y = (int) (y / (float) Gdx.graphics.getHeight() * 480);
		y = 480 - y;

		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		return false;
	}

	@Override
	public boolean touchMoved(int x, int y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
