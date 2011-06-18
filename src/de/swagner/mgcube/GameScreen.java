package de.swagner.mgcube;


import java.awt.geom.CubicCurve2D;
import java.util.logging.FileHandler;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.loaders.obj.ObjLoader;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

import de.swagner.gdx.obj.normalmap.helper.ObjLoaderTan;
import de.swagner.gdx.obj.normalmap.shader.BloomShader;
import de.swagner.gdx.obj.normalmap.shader.Quad2Shader;
import de.swagner.gdx.obj.normalmap.shader.TransShader;

public class GameScreen extends DefaultScreen implements InputProcessor {

	float startTime = 0;
	PerspectiveCamera cam;
	Mesh blockModel;
	Mesh playerModel;
	Mesh targetModel;
	Mesh worldModel;
	Mesh quadModel;
	Mesh wireCubeModel;
	float angleX = 0;
	float angleY = 0;
	SpriteBatch batch;
	BitmapFont font;
	Player player = new Player();
	Target target = new Target();
	Array<Block> blocks = new Array<Block>();
	boolean animateWorld = false;
	boolean animatePlayer = false;

	float touchDistance = 0;

	Vector3 xAxis = new Vector3(1, 0, 0);
	Vector3 yAxis = new Vector3(0, 1, 0);
	Vector3 zAxis = new Vector3(0, 0, 1);

	Vector3 light_position0 = new Vector3(10.0f, 10.0f, 20.75f);
	Vector3 light_position1 = new Vector3(-10.0f, -10.0f, -20.75f);

	// GLES20
	Matrix4 model = new Matrix4().idt();
	Matrix4 modelView = new Matrix4().idt();
	Matrix4 modelViewProjection = new Matrix4().idt();
	Matrix4 tmp = new Matrix4().idt();
	private ShaderProgram transShader;
	private ShaderProgram bloomShader;
	private Vector3 light = new Vector3(-2f, 1f, 10f);
	FrameBuffer frameBuffer;
	FrameBuffer frameBuffer1;
	Texture fbTexture;
	Texture texture;
	
	float touchStartX = 0;
	float touchStartY = 0;

	public GameScreen(Game game) {
		super(game);
		Gdx.input.setInputProcessor(this);

		blockModel = ObjLoader.loadObj(Gdx.files.internal("data/cube.obj").read());
		blockModel.getVertexAttribute(Usage.Position).alias = "a_vertex";

		playerModel = ObjLoader.loadObj(Gdx.files.internal("data/sphere.obj").read());
		playerModel.getVertexAttribute(Usage.Position).alias = "a_vertex";

		targetModel = ObjLoader.loadObj(Gdx.files.internal("data/cylinder.obj").read());
		targetModel.getVertexAttribute(Usage.Position).alias = "a_vertex";

		quadModel = new Mesh(true, 4, 6, new VertexAttribute(Usage.Position, 4, "a_position"), new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord"));
		float[] vertices = { -1.0f, 1.0f, 0.0f, 1.0f, // Position 0
				0.0f, 0.0f, // TexCoord 0
				-1.0f, -1.0f, 0.0f, 1.0f, // Position 1
				0.0f, 1.0f, // TexCoord 1
				1.0f, -1.0f, 0.0f, 1.0f, // Position 2
				1.0f, 1.0f, // TexCoord 2
				1.0f, 1.0f, 0.0f, 1.0f, // Position 3
				1.0f, 0.0f // TexCoord 3
		};
		short[] indices = { 0, 1, 2, 0, 2, 3 };
		quadModel.setVertices(vertices);
		quadModel.setIndices(indices);

		wireCubeModel = new Mesh(true, 20, 20, new VertexAttribute(Usage.Position, 4, "a_vertex"));
		float[] vertices2 = {
				// front face
				-1.0f, 1.0f, 1.0f, 1.0f, // 0
				1.0f, 1.0f, 1.0f, 1.0f, // 1
				1.0f, -1.0f, 1.0f, 1.0f, // 2
				-1.0f, -1.0f, 1.0f, 1.0f, // 3

				// left face
				-1.0f, 1.0f, 1.0f, 1.0f, // 0
				-1.0f, 1.0f, -1.0f, 1.0f, // 4
				-1.0f, -1.0f, -1.0f, 1.0f, // 7
				-1.0f, -1.0f, 1.0f, 1.0f, // 3

				// bottom face
				-1.0f, -1.0f, 1.0f, 1.0f, // 3
				1.0f, -1.0f, 1.0f, 1.0f, // 2
				1.0f, -1.0f, -1.0f, 1.0f, // 6
				-1.0f, -1.0f, -1.0f, 1.0f, // 7

				// back face
				-1.0f, -1.0f, -1.0f, 1.0f, // 7
				-1.0f, 1.0f, -1.0f, 1.0f, // 4
				1.0f, 1.0f, -1.0f, 1.0f, // 5
				1.0f, -1.0f, -1.0f, 1.0f, // 6

				// right face
				1.0f, -1.0f, -1.0f, 1.0f, // 6
				1.0f, -1.0f, 1.0f, 1.0f, // 2
				1.0f, 1.0f, 1.0f, 1.0f, // 1
				1.0f, 1.0f, -1.0f, 1.0f, // 5
		};
		short[] indices2 = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };
		wireCubeModel.setVertices(vertices2);
		wireCubeModel.setIndices(indices2);

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
		font = new BitmapFont(Gdx.files.internal("data/scorefont.fnt"), false);
		font.setColor(1, 1, 1, 0.8f);
		initShader();
		initLevel(1);
		initRender();
	}

	private void initShader() {
		transShader = new ShaderProgram(TransShader.mVertexShader, TransShader.mFragmentShader);
		if (transShader.isCompiled() == false) {
			Gdx.app.log("ShaderTest", transShader.getLog());
			System.exit(0);
		}

		bloomShader = new ShaderProgram(BloomShader.mVertexShader, BloomShader.mFragmentShader);
		if (bloomShader.isCompiled() == false) {
			Gdx.app.log("ShaderTest", bloomShader.getLog());
			System.exit(0);
		}
	}

	private void initLevel(int levelnumber) {
		blocks.clear();
		int[][][] level;
		switch (levelnumber) {
		case 1:
			level = Resources.getInstance().level1;
			break;
		case 2:
			level = Resources.getInstance().level2;
			break;
		case 3:
			level = Resources.getInstance().level3;
			break;

		// more levels

		default:
			level = Resources.getInstance().level1;
			break;
		}

		// finde player pos
		int z = 0, y = 0, x = 0;
		for (z = 0; z < 10; z++) {
			for (y = 0; y < 10; y++) {
				for (x = 0; x < 10; x++) {
					if (level[z][y][x] == 1) {
						blocks.add(new Block(new Vector3(-10f + (x * 2), -10f + (y * 2), -10f + (z * 2))));
					}
					if (level[z][y][x] == 2) {
						player.position.x = -10f + (x * 2);
						player.position.y = -10f + (y * 2);
						player.position.z = -10f + (z * 2);
					}
					if (level[z][y][x] == 3) {
						target.position.x = -10f + (x * 2);
						target.position.y = -10f + (y * 2);
						target.position.z = -10f + (z * 2);
					}
				}
			}
		}
	}

	private void reset() {
		animateWorld = false;
		animatePlayer = false;
		initLevel(Resources.getInstance().currentlevel);
	}

	private void initRender() {
		Gdx.graphics.getGL20().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		Gdx.graphics.getGL20().glClearColor(0.4f, 0.4f, 0.4f, 1.0f);

		Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
		Gdx.graphics.getGL20().glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		frameBuffer = new FrameBuffer(Format.RGB565, 800, 480, false);
		frameBuffer1 = new FrameBuffer(Format.RGB565, 800, 480, false);
	}

	@Override
	public void show() {
	}

	protected int lastTouchX;
	protected int lastTouchY;

	@Override
	public void render(float delta) {
		startTime += delta;

		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		frameBuffer.begin();
		Gdx.graphics.getGL20().glViewport(0, 0, frameBuffer.getWidth(), frameBuffer.getHeight());

		Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
		Gdx.graphics.getGL20().glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		cam.update();

		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

		// collision
		Ray pRay = new Ray(player.position, player.direction);

		for (Block block : blocks) {
			Vector3 intersection = new Vector3();
			boolean intersect = Intersector.intersectRaySphere(pRay, block.position, 1f, intersection);
			float dst = intersection.dst(player.position);
			if (dst < 1.2f && intersect) {
				animatePlayer = false;
				break;
			}
		}
		Vector3 Targetintersection = new Vector3();
		boolean Targetintersect = Intersector.intersectRaySphere(pRay, target.position, 1f, Targetintersection);
		float targetdst = Targetintersection.dst(player.position);
		boolean win = false;
		if (targetdst < 1.2f) {
			win = true;
		}

		// player out of bound?
		BoundingBox box = new BoundingBox(new Vector3(-10f, -10f, -10f), new Vector3(10f, 10f, 10f));
		if (!box.contains(player.position)) {
			animatePlayer = false;
			reset();
		}

		if (animatePlayer) {

			player.position.add(player.direction.x * delta * 10f, player.direction.y * delta * 10f, player.direction.z * delta * 10f);

			if (win) {
				animatePlayer = false;
				nextLevel();
				reset();
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
			tmp.idt();
			model.idt();
			modelView.idt();

			tmp.setToScaling(0.5f, 0.5f, 0.5f);
			model.mul(tmp);

			tmp.setToRotation(xAxis, angleX);
			model.mul(tmp);
			tmp.setToRotation(yAxis, angleY);
			model.mul(tmp);

			// modelView.set(cam.view);
			// modelView.mul(model);
			// tmp.setToRotation(angleY, modelView.getValues()[1],
			// modelView.getValues()[5], modelView.getValues()[9]);
			// model.mul(tmp);
			//
			// modelView.set(cam.view);
			// modelView.mul(model);
			// tmp.setToRotation(angleX, modelView.getValues()[0],
			// modelView.getValues()[4], modelView.getValues()[8]);
			// model.mul(tmp);
			//
			tmp.setToTranslation(block.position.x, block.position.y, block.position.z);
			model.mul(tmp);

			tmp.setToScaling(0.95f, 0.95f, 0.95f);
			model.mul(tmp);

			transShader.begin();

			modelViewProjection.idt();
			modelViewProjection.set(cam.combined);
			modelViewProjection = tmp.mul(model);

			transShader.setUniformMatrix("MVPMatrix", modelViewProjection);

			transShader.setUniformf("a_color", 1.0f, 0.1f, 0.1f);
			transShader.setUniformf("alpha", 0.8f);
			wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);

			transShader.setUniformf("a_color", 1.0f, 0.1f, 0.1f);
			transShader.setUniformf("alpha", 0.2f);
			blockModel.render(transShader, GL20.GL_TRIANGLES);

			transShader.end();
		}

		{
			// render Player
			tmp.idt();
			model.idt();
			modelView.idt();

			tmp.setToScaling(0.5f, 0.5f, 0.5f);
			model.mul(tmp);

			tmp.setToRotation(xAxis, angleX);
			model.mul(tmp);
			tmp.setToRotation(yAxis, angleY);
			model.mul(tmp);

			// modelView.set(cam.view);
			// modelView.mul(model);
			// tmp.setToRotation(angleY, modelView.getValues()[1],
			// modelView.getValues()[5], modelView.getValues()[9]);
			// model.mul(tmp);
			//
			// modelView.set(cam.view);
			// modelView.mul(model);
			// tmp.setToRotation(angleX, modelView.getValues()[0],
			// modelView.getValues()[4], modelView.getValues()[8]);
			// model.mul(tmp);
			//
			tmp.setToTranslation(player.position.x, player.position.y, player.position.z);
			model.mul(tmp);

			transShader.begin();

			modelViewProjection.idt();
			modelViewProjection.set(cam.combined);
			modelViewProjection = tmp.mul(model);

			transShader.setUniformMatrix("MVPMatrix", modelViewProjection);
			// shader.setUniformf("LightDirection", light.x, light.y, light.z);

			transShader.setUniformf("a_color", 1.0f, 1.0f, 0.0f);
			transShader.setUniformf("alpha", 0.8f);
			playerModel.render(transShader, GL20.GL_TRIANGLES);
			transShader.end();
		}

		{
			// render Target
			tmp.idt();
			model.idt();
			modelView.idt();

			tmp.setToScaling(0.5f, 0.5f, 0.5f);
			model.mul(tmp);

			tmp.setToRotation(xAxis, angleX);
			model.mul(tmp);
			tmp.setToRotation(yAxis, angleY);
			model.mul(tmp);

			// modelView.set(cam.view);
			// modelView.mul(model);
			// tmp.setToRotation(angleY, modelView.getValues()[1],
			// modelView.getValues()[5], modelView.getValues()[9]);
			// model.mul(tmp);
			//
			// modelView.set(cam.view);
			// modelView.mul(model);
			// tmp.setToRotation(angleX, modelView.getValues()[0],
			// modelView.getValues()[4], modelView.getValues()[8]);
			// model.mul(tmp);
			//
			tmp.setToTranslation(target.position.x, target.position.y, target.position.z);
			model.mul(tmp);

			transShader.begin();

			modelViewProjection.idt();
			modelViewProjection.set(cam.combined);
			modelViewProjection = tmp.mul(model);

			transShader.setUniformMatrix("MVPMatrix", modelViewProjection);
			// shader.setUniformf("LightDirection", light.x, light.y, light.z);

			transShader.setUniformf("a_color", 0.0f, 1.1f, 0.1f);
			transShader.setUniformf("alpha", 0.5f);
			targetModel.render(transShader, GL20.GL_TRIANGLES);

			transShader.end();
		}

		{
			// render Wire
			tmp.idt();
			model.idt();
			modelView.idt();

			tmp.setToScaling(5.5f, 5.5f, 5.5f);
			model.mul(tmp);

			tmp.setToRotation(xAxis, angleX);
			model.mul(tmp);
			tmp.setToRotation(yAxis, angleY);
			model.mul(tmp);

			// modelView.set(cam.view);
			// modelView.mul(model);
			// tmp.setToRotation(angleY, modelView.getValues()[1],
			// modelView.getValues()[5], modelView.getValues()[9]);
			// model.mul(tmp);
			//
			// modelView.set(cam.view);
			// modelView.mul(model);
			// tmp.setToRotation(angleX, modelView.getValues()[0],
			// modelView.getValues()[4], modelView.getValues()[8]);
			// model.mul(tmp);
			//
			tmp.setToTranslation(0, 0, 0);
			model.mul(tmp);

			transShader.begin();

			modelViewProjection.idt();
			modelViewProjection.set(cam.combined);
			modelViewProjection = tmp.mul(model);

			transShader.setUniformMatrix("MVPMatrix", modelViewProjection);
			// shader.setUniformf("LightDirection", light.x, light.y, light.z);

			transShader.setUniformf("a_color", 1.0f, 0.1f, 0.1f);
			transShader.setUniformf("alpha", 0.2f);
			wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);

			transShader.setUniformf("a_color", 1.0f, 0.1f, 0.1f);
			transShader.setUniformf("alpha", 0.09f);
			blockModel.render(transShader, GL20.GL_TRIANGLES);

			transShader.end();
		}

		frameBuffer.end();

		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

		frameBuffer.getColorBufferTexture().bind(0);

		// Gdx.graphics.getGL20().glViewport(0, 0, Gdx.graphics.getWidth(),
		// Gdx.graphics.getHeight());

		frameBuffer1.begin();
		Gdx.graphics.getGL20().glViewport(0, 0, frameBuffer1.getWidth(), frameBuffer1.getHeight());
		Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);
		Gdx.graphics.getGL20().glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		bloomShader.begin();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, frameBuffer1.getWidth(), frameBuffer1.getHeight());
		bloomShader.setUniformi("s_texture", 0);
		bloomShader.setUniformf("bloomfactor", (MathUtils.sin(startTime * 5f) * 0.1f) + 1.0f);
		quadModel.render(bloomShader, GL20.GL_TRIANGLE_FAN);
		bloomShader.end();
		frameBuffer1.end();

		Gdx.graphics.getGL20().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.begin();

		batch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		batch.draw(frameBuffer1.getColorBufferTexture(), 0, 0);
		
		font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond(), 620, 40);
		font.draw(batch, "lives: 3", 620, 80);
		font.draw(batch, "time: 00:30", 620, 60);
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

		if (keycode == Input.Keys.RIGHT) {
			nextLevel();
		}

		if (keycode == Input.Keys.LEFT) {
			prevLevel();
		}
		return false;
	}

	private void nextLevel() {
		Resources.getInstance().currentlevel++;
		initLevel(Resources.getInstance().currentlevel);
	}

	private void prevLevel() {
		Resources.getInstance().currentlevel--;
		initLevel(Resources.getInstance().currentlevel);
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
		touchDistance = 0;
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

		if (Math.abs(touchDistance) < 0.5f)
			animatePlayer = true;

		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		x = (int) (x / (float) Gdx.graphics.getWidth() * 800);
		y = (int) (y / (float) Gdx.graphics.getHeight() * 480);

		angleY += ((x - touchStartX) / 5.f);
		angleX += ((y - touchStartY) / 5.f);

		touchDistance += ((x - touchStartX) / 5.f) + ((y - touchStartY) / 5.f);

		if (!animatePlayer) {
			player.direction.set(0, 0, -1);
			player.direction.rot(new Matrix4().setToRotation(xAxis, -angleX));
			player.direction.rot(new Matrix4().setToRotation(yAxis, -angleY));
		}

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
			cam.translate(0, 0, 1 * amount);
		if((cam.position.z < 2 && amount < -0) || (cam.position.z > 20 && amount > 0))
			cam.translate(0, 0, 1 * -amount);
		return false;
	}

}
