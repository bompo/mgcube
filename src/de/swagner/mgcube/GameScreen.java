package de.swagner.mgcube;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.loaders.collada.ColladaLoader;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.test.utils.PerspectiveCamController;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

public class GameScreen extends DefaultScreen implements InputProcessor {

	double startTime = 0;
	PerspectiveCamera cam;
	StillModel blockModel;
	StillModel playerModel;
	StillModel targetModel;
	StillModel worldModel;
	float angleX = 0;
	float angleY = 0;
	SpriteBatch batch;
	BitmapFont font;
	Player player = new Player();
	Target target = new Target();
	Array<Block> blocks = new Array<Block>();
	boolean animateWorld = false;
	boolean animatePlayer = false;
	
	float touchTime = 0;

	Vector3 xAxis = new Vector3(1, 0, 0);
	Vector3 yAxis = new Vector3(0, 1, 0);
	Vector3 zAxis = new Vector3(0, 0, 1);

	Vector3 light_position0 = new Vector3(10.0f, 10.0f, 20.75f);
	Vector3 light_position1 = new Vector3(-10.0f, -10.0f, -20.75f);
	

	float touchStartX = 0;
	float touchStartY = 0;

	public GameScreen(Game game) {
		super(game);
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setInputProcessor(this);

		blockModel = ColladaLoader.loadStillModel(Gdx.files.internal("data/cube.dae"));
		blockModel.setMaterial(new Material("test", new ColorAttribute(Color.RED, "")));

		playerModel = ColladaLoader.loadStillModel(Gdx.files.internal("data/player.dae"));

		targetModel = ColladaLoader.loadStillModel(Gdx.files.internal("data/target.dae"));

		worldModel = ColladaLoader.loadStillModel(Gdx.files.internal("data/cubeWorld.dae"));

		cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0, 0, 15f);
		cam.direction.set(0, 0, -1);
		cam.up.set(0, 1, 0);
		cam.near = 1f;
		cam.far = 1000;

		// controller = new PerspectiveCamController(cam);
		// Gdx.input.setInputProcessor(controller);

		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		font = new BitmapFont();

		initLevel();
		initRender();
	}

	private void initLevel() {
		// finde player pos
		int z = 0, y = 0, x = 0;
		for (z = 0; z < 10; z++) {
			for (y = 0; y < 10; y++) {
				for (x = 0; x < 10; x++) {
					if (Resources.getInstance().level1[z][y][x] == 1) {
						blocks.add(new Block(new Vector3(-10f + (x*2), -10f + (y*2), -10f + (z*2))));
					}
					if (Resources.getInstance().level1[z][y][x] == 2) {
						player.position.x = -10f + (x*2);
						player.position.y = -10f + (y*2);
						player.position.z = -10f + (z*2);
					}
					if (Resources.getInstance().level1[z][y][x] == 3) {
						target.x = -10f + (x*2);
						target.y = -10f + (y*2);
						target.z = -10f + (z*2);
					}
				}
			}
		}
	}

	private void reset() {
		animateWorld = false;
		animatePlayer = false;
		initLevel();
	}

	private void initRender() {
		Gdx.graphics.getGL10().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		Gdx.graphics.getGL10().glClearColor(0.4f, 0.4f, 0.4f, 1.0f);

		float[] light_ambient = new float[] { 0.2f, 0.2f, 0.2f, 1.0f };
		float[] light_diffuse1 = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
		float[] light_diffuse2 = new float[] { 0.3f, 0.3f, 0.3f, 1.0f };

		Gdx.graphics.getGL10().glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, light_ambient, 0);
		Gdx.graphics.getGL10().glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, light_diffuse1, 0);
		Gdx.graphics.getGL10().glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, new float[] { light_position0.x, light_position0.y, light_position0.z, 1f }, 0);

		Gdx.graphics.getGL10().glLightfv(GL10.GL_LIGHT1, GL10.GL_DIFFUSE, light_diffuse2, 0);
		Gdx.graphics.getGL10().glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, new float[] { light_position1.x, light_position1.y, light_position1.z, 1f }, 0);

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

	protected int lastTouchX;
	protected int lastTouchY;

	@Override
	public void render(float delta) {
		GL10 gl = Gdx.graphics.getGL10();
		Gdx.gl.glClearColor(0.9f, 0.9f, 0.9f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		cam.update();
		cam.apply(Gdx.gl10);

		// refresh lights
		Gdx.graphics.getGL10().glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, new float[] { light_position0.x, light_position0.y, light_position0.z,1f }, 0);
		Gdx.graphics.getGL10().glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, new float[] { light_position1.x, light_position1.y, light_position1.z, 1f }, 0);

		Gdx.gl.glEnable(GL10.GL_CULL_FACE);
		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);

		if (animatePlayer) {
			player.position.add(player.direction.x * delta * 10f, player.direction.y * delta * 10f, player.direction.z * delta * 10f);

			for (Block block : blocks) {
				// TODO only check blocks in moving direction
				// block.position.dst(player.position);

				// distance < 2?
				float dst = block.position.dst(player.position);
				if (dst < 2f) {
					animatePlayer = false;
					player.position.sub(player.direction.x * delta * 10f, player.direction.y * delta * 10f, player.direction.z * delta * 10f);
					break;
				}
				if (dst > 50f) {
					reset();
					break;
				}
			}

		}

		if (!Gdx.input.isTouched()) {
			if (Math.abs(player.direction.x) > Math.abs(player.direction.y) && Math.abs(player.direction.x) > Math.abs(player.direction.z)) {
				while (player.direction.x != -1 && player.direction.x != 1) {
					if (player.direction.x < 0)
						player.direction.x--;
					else
						player.direction.x++;
					if (player.direction.x < -1)
						player.direction.x = -1;
					if (player.direction.x > 1)
						player.direction.x = 1;
					player.direction.y = 0;
					player.direction.z = 0;
				}
			}
			if (Math.abs(player.direction.y) > Math.abs(player.direction.x) && Math.abs(player.direction.y) > Math.abs(player.direction.z)) {
				while (player.direction.y != -1 && player.direction.y != 1) {
					if (player.direction.y < 0)
						player.direction.y--;
					else
						player.direction.y++;
					if (player.direction.y < -1)
						player.direction.y = -1;
					if (player.direction.y > 1)
						player.direction.y = 1;
					player.direction.x = 0;
					player.direction.z = 0;
				}
			}
			if (Math.abs(player.direction.z) > Math.abs(player.direction.y) && Math.abs(player.direction.z) > Math.abs(player.direction.y)) {
				while (player.direction.z != -1 && player.direction.z != 1) {
					if (player.direction.z < 0)
						player.direction.z--;
					else
						player.direction.z++;
					if (player.direction.z < -1)
						player.direction.z = -1;
					if (player.direction.z > 1)
						player.direction.z = 1;
					player.direction.y = 0;
					player.direction.x = 0;
				}
			}
		}

		// render Blocks
		for (Block block : blocks) {
			Gdx.gl11.glPushMatrix();

			Gdx.gl11.glScalef(0.5f, 0.5f, 0.5f);

			float[] currentModelViewMatrix = new float[16];
			Gdx.graphics.getGL11().glGetFloatv(GL11.GL_MODELVIEW_MATRIX, currentModelViewMatrix, 0);
			Gdx.graphics.getGL11().glRotatef(angleY, currentModelViewMatrix[1], currentModelViewMatrix[5], currentModelViewMatrix[9]);
			Gdx.graphics.getGL11().glGetFloatv(GL11.GL_MODELVIEW_MATRIX, currentModelViewMatrix, 0);
			Gdx.graphics.getGL11().glRotatef(angleX, currentModelViewMatrix[0], currentModelViewMatrix[4], currentModelViewMatrix[8]);

			Gdx.gl11.glTranslatef(block.position.x, block.position.y, block.position.z);
			Gdx.gl10.glColor4f(1f, 0, 0, 1);

			Gdx.gl11.glScalef(0.95f, 0.95f, 0.95f);
			blockModel.render();
			Gdx.gl11.glPopMatrix();
		}

		{
			// render Player
			Gdx.gl11.glPushMatrix();
			Gdx.gl11.glScalef(0.5f, 0.5f, 0.5f);

			float[] currentModelViewMatrix = new float[16];
			Gdx.graphics.getGL11().glGetFloatv(GL11.GL_MODELVIEW_MATRIX, currentModelViewMatrix, 0);
			Gdx.graphics.getGL11().glRotatef(angleY, currentModelViewMatrix[1], currentModelViewMatrix[5], currentModelViewMatrix[9]);
			Gdx.graphics.getGL11().glGetFloatv(GL11.GL_MODELVIEW_MATRIX, currentModelViewMatrix, 0);
			Gdx.graphics.getGL11().glRotatef(angleX, currentModelViewMatrix[0], currentModelViewMatrix[4], currentModelViewMatrix[8]);

			Gdx.gl11.glTranslatef(player.position.x, player.position.y, player.position.z);
			playerModel.render();
			Gdx.gl11.glPopMatrix();
		}

		{
			// render Target
			Gdx.gl11.glPushMatrix();
			Gdx.gl11.glScalef(0.5f, 0.5f, 0.5f);

			float[] currentModelViewMatrix = new float[16];
			Gdx.graphics.getGL11().glGetFloatv(GL11.GL_MODELVIEW_MATRIX, currentModelViewMatrix, 0);
			Gdx.graphics.getGL11().glRotatef(angleY, currentModelViewMatrix[1], currentModelViewMatrix[5], currentModelViewMatrix[9]);
			Gdx.graphics.getGL11().glGetFloatv(GL11.GL_MODELVIEW_MATRIX, currentModelViewMatrix, 0);
			Gdx.graphics.getGL11().glRotatef(angleX, currentModelViewMatrix[0], currentModelViewMatrix[4], currentModelViewMatrix[8]);

			Gdx.gl11.glTranslatef(target.x, target.y, target.z);
			targetModel.render();
			Gdx.gl11.glPopMatrix();
		}

		{
			// render Wire
			Gdx.gl11.glPushMatrix();
			Gdx.gl11.glScalef(5.5f, 5.5f, 5.5f);
			float[] currentModelViewMatrix = new float[16];
			Gdx.graphics.getGL11().glGetFloatv(GL11.GL_MODELVIEW_MATRIX, currentModelViewMatrix, 0);
			Gdx.graphics.getGL11().glRotatef(angleY, currentModelViewMatrix[1], currentModelViewMatrix[5], currentModelViewMatrix[9]);
			Gdx.graphics.getGL11().glGetFloatv(GL11.GL_MODELVIEW_MATRIX, currentModelViewMatrix, 0);
			Gdx.graphics.getGL11().glRotatef(angleX, currentModelViewMatrix[0], currentModelViewMatrix[4], currentModelViewMatrix[8]);

			Gdx.gl11.glTranslatef(0, 0, 0);
			worldModel.render(GL11.GL_LINE_STRIP);
			Gdx.gl11.glPopMatrix();
		}

		Gdx.gl.glDisable(GL10.GL_CULL_FACE);
		Gdx.gl.glDisable(GL10.GL_DEPTH_TEST);
		batch.begin();
		font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
		batch.end();

	}

	@Override
	public void hide() {
	}

	@Override
	public boolean keyDown(int keycode) {
		if (Gdx.input.isTouched())
			return false;
		if (keycode == Input.Keys.ESCAPE) {
			System.exit(0);
		}

		if (keycode == Input.Keys.SPACE) {
			animatePlayer = true;
		}

		if (keycode == Input.Keys.R) {
			reset();
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
		touchTime = 0;
		x = (int) (x / (float) Gdx.graphics.getWidth() * 800);
		y = (int) (y / (float) Gdx.graphics.getHeight() * 480);

		touchStartX = x;
		touchStartY = y;

		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		x = (int) (x / (float) Gdx.graphics.getWidth() * 800);
		y = (int) (y / (float) Gdx.graphics.getHeight() * 480);

		if(touchTime<0.1) animatePlayer = true;
		
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		touchTime += Gdx.graphics.getDeltaTime();
		x = (int) (x / (float) Gdx.graphics.getWidth() * 800);
		y = (int) (y / (float) Gdx.graphics.getHeight() * 480);

		angleY += ((x - touchStartX) / 5.f);
		angleX += ((y - touchStartY) / 5.f);

		if(!animatePlayer) {
			player.direction.set(0, 0, -1);
			player.direction.rot(new Matrix4().setToRotation(xAxis, -angleX));
			player.direction.rot(new Matrix4().setToRotation(yAxis, -angleY));
		}
		
		light_position0.set(10.0f, 10.0f, 20.75f);
		light_position0.rot(new Matrix4().setToRotation(xAxis, angleX));
		light_position0.rot(new Matrix4().setToRotation(yAxis, angleY));
		
		light_position1.set(-10.0f, -10.0f, -20.75f);
		light_position1.rot(new Matrix4().setToRotation(xAxis, angleX));
		light_position1.rot(new Matrix4().setToRotation(yAxis, angleY));

		touchStartX = x;
		touchStartY = y;
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
