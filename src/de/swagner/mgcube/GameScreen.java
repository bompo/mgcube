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
import de.swagner.gdx.obj.normalmap.shader.FastBloomShader;
import de.swagner.gdx.obj.normalmap.shader.Quad2Shader;
import de.swagner.gdx.obj.normalmap.shader.TransShader;

public class GameScreen extends DefaultScreen implements InputProcessor {

	float startTime = 0;
	PerspectiveCamera cam;
	Mesh blockModel;
	Mesh playerModel;
	Mesh targetModel;
	Mesh quadModel;
	Mesh wireCubeModel;
	float angleX = 0;
	float angleY = 0;

	float angleXBack = 0;
	float angleYBack = 0;
	SpriteBatch batch;
	BitmapFont font;
	Player player = new Player();
	Target target = new Target();
	Array<Block> blocks = new Array<Block>();
	boolean animateWorld = false;
	boolean animatePlayer = false;
	
	//fade
	SpriteBatch fadeBatch;
	Sprite blackFade;
	Sprite title;
	float fade = 1.0f;
	boolean finished = false;

	float touchDistance = 0;

	Vector3 xAxis = new Vector3(1, 0, 0);
	Vector3 yAxis = new Vector3(0, 1, 0);
	Vector3 zAxis = new Vector3(0, 0, 1);

	// GLES20
	Matrix4 model = new Matrix4().idt();
	Matrix4 modelView = new Matrix4().idt();
	Matrix4 modelViewProjection = new Matrix4().idt();
	Matrix4 tmp = new Matrix4().idt();
	private ShaderProgram transShader;
	private ShaderProgram bloomShader;
	private Vector3 light = new Vector3(-2f, 1f, 10f);
	FrameBuffer frameBuffer;
	FrameBuffer frameBufferVert;
	FrameBuffer frameBufferHori;
	private int m_i32TexSize;
	private float m_fTexelOffset;
	
	float touchStartX = 0;
	float touchStartY = 0;

	public GameScreen(Game game) {
		super(game);
		Gdx.input.setInputProcessor(this);

		blockModel = Resources.getInstance().blockModel;
		playerModel = Resources.getInstance().playerModel;
		targetModel = Resources.getInstance().targetModel;
		quadModel = Resources.getInstance().quadModel;
		wireCubeModel = Resources.getInstance().wireCubeModel;

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

		blackFade = new Sprite(new Texture(Gdx.files.internal("data/blackfade.png")));
		fadeBatch = new SpriteBatch();
		fadeBatch.getProjectionMatrix().setToOrtho2D(0, 0, 2, 2);

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

		//BLOOOOOOMMMM from powervr examples
		// Blur render target size (power-of-two)
		m_i32TexSize = 128;

		// Texel offset for blur filter kernle
		m_fTexelOffset = 1.0f / (float)m_i32TexSize;
		
		// Altered weights for the faster filter kernel 
		float w1 = 0.0555555f;
		float w2 = 0.2777777f;
		float intraTexelOffset = (w2 / (w1 + w2)) * m_fTexelOffset;
		m_fTexelOffset += intraTexelOffset;
		
		bloomShader = new ShaderProgram(FastBloomShader.mVertexShader, FastBloomShader.mFragmentShader);
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
		if(Resources.getInstance().lives < 1)
		{
			game.setScreen(new MainMenuScreen(game));
			Resources.getInstance().lives = 3;
			Resources.getInstance().currentlevel = 1;
			initLevel(Resources.getInstance().currentlevel);
			Resources.getInstance().time = 0;
		}
	}

	private void initRender() {
		Gdx.graphics.getGL20().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		Gdx.graphics.getGL20().glClearColor(0.4f, 0.4f, 0.4f, 1.0f);

		Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
		Gdx.graphics.getGL20().glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		frameBuffer = new FrameBuffer(Format.RGB565, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
		
		frameBufferVert = new FrameBuffer(Format.RGB565, 128, 128, false);
		frameBufferHori = new FrameBuffer(Format.RGB565, 128, 128, false);	
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		initRender();
	}

	@Override
	public void show() {
	}

	protected int lastTouchX;
	protected int lastTouchY;

	@Override
	public void render(float delta) {
		startTime += delta;

		angleXBack += MathUtils.sin(startTime);
		angleYBack += MathUtils.cos(startTime);
		
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
			Resources.getInstance().lives--;
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
			transShader.setUniformf("a_color", 1.0f, 1.0f, 0.0f);
			transShader.setUniformf("alpha", 0.1f);
			playerModel.render(transShader, GL20.GL_TRIANGLES);
			
			
			//render hull			
			transShader.setUniformf("a_color", 1.0f, 1.0f, 0.0f);
			transShader.setUniformf("alpha", 0.4f);
			playerModel.render(transShader, GL20.GL_LINE_STRIP);
			
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
			
			//render hull			
			transShader.setUniformf("a_color", 0.0f, 1.1f, 0.1f);
			transShader.setUniformf("alpha", 0.4f);
			targetModel.render(transShader, GL20.GL_LINE_STRIP);

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
		
//		{
//			// render Background Wire
//			tmp.idt();
//			model.idt();
//			modelView.idt();
//
//			tmp.setToScaling(20.5f, 20.5f, 20.5f);
//			model.mul(tmp);
//
//			tmp.setToRotation(xAxis, angleX + angleXBack);
//			model.mul(tmp);
//			tmp.setToRotation(yAxis, angleY + angleYBack);
//			model.mul(tmp);
//
//			// modelView.set(cam.view);
//			// modelView.mul(model);
//			// tmp.setToRotation(angleY, modelView.getValues()[1],
//			// modelView.getValues()[5], modelView.getValues()[9]);
//			// model.mul(tmp);
//			//
//			// modelView.set(cam.view);
//			// modelView.mul(model);
//			// tmp.setToRotation(angleX, modelView.getValues()[0],
//			// modelView.getValues()[4], modelView.getValues()[8]);
//			// model.mul(tmp);
//			//
//			tmp.setToTranslation(0, 0, 0);
//			model.mul(tmp);
//
//			transShader.begin();
//
//			modelViewProjection.idt();
//			modelViewProjection.set(cam.combined);
//			modelViewProjection = tmp.mul(model);
//
//			transShader.setUniformMatrix("MVPMatrix", modelViewProjection);
//			// shader.setUniformf("LightDirection", light.x, light.y, light.z);
//
//			transShader.setUniformf("a_color", 1.0f, 1.0f, 0.1f);
//			transShader.setUniformf("alpha", 0.1f);
//			playerModel.render(transShader, GL20.GL_LINE_STRIP);
//
//			transShader.end();
//		}

		frameBuffer.end();

		//PostProcessing
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDisable(GL20.GL_BLEND);
		
		frameBuffer.getColorBufferTexture().bind(0);

		frameBufferVert.begin();
		bloomShader.begin();
		bloomShader.setUniformi("sTexture", 0);
		bloomShader.setUniformf("bloomFactor", Helper.map((MathUtils.sin(startTime * 5f) * 0.5f) + 0.5f,0,1,0.6f,0.9f));
		bloomShader.setUniformf("TexelOffsetX", m_fTexelOffset);
		bloomShader.setUniformf("TexelOffsetY", 0.0f);
		quadModel.render(bloomShader, GL20.GL_TRIANGLE_STRIP);
		bloomShader.end(); 
		frameBufferVert.end();
		
		
		frameBufferVert.getColorBufferTexture().bind(0);
		
		frameBufferHori.begin();		
		bloomShader.begin();
		bloomShader.setUniformi("sTexture", 0);
		bloomShader.setUniformf("TexelOffsetX", 0.0f);
		bloomShader.setUniformf("bloomFactor", Helper.map((MathUtils.sin(startTime * 5f) * 0.5f) + 0.5f,0,1,0.6f,0.9f));
		bloomShader.setUniformf("TexelOffsetY", m_fTexelOffset);
		quadModel.render(bloomShader, GL20.GL_TRIANGLE_STRIP);
		bloomShader.end(); 
		frameBufferHori.end();
		
	
		Gdx.graphics.getGL20().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		batch.begin();
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		batch.draw(frameBufferHori.getColorBufferTexture(), 0, 0,800,480,0,0,frameBufferHori.getWidth(),frameBufferHori.getHeight(),false,true);
		batch.draw(frameBuffer.getColorBufferTexture(), 0, 0,800,480,0,0,frameBuffer.getWidth(),frameBuffer.getHeight(),false,true);
		batch.end();

		batch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		batch.begin();
		font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond(), 620, 40);
		font.draw(batch, "lives: " + Resources.getInstance().lives, 620, 80);
		Resources.getInstance().time += delta;
		int seconds = (int) Resources.getInstance().time % 60;
		int minutes = (int)Resources.getInstance().time / 60;
		if(seconds > 9 && minutes > 9)
			font.draw(batch, "time: " + minutes + ":" + seconds, 620, 60);
		else if(seconds > 9 && minutes < 10)
			font.draw(batch, "time: 0" + minutes + ":" + seconds, 620, 60);
		else if(seconds < 10 && minutes > 9)
			font.draw(batch, "time: " + minutes + ":0" + seconds, 620, 60);
		else
			font.draw(batch, "time: 0" + minutes + ":0" + seconds, 620, 60);
		batch.end();
		
		if (!finished && fade > 0) {
			fade = Math.max(fade - Gdx.graphics.getDeltaTime() / 2.f, 0);
			fadeBatch.begin();
			blackFade.setColor(blackFade.getColor().r, blackFade.getColor().g, blackFade.getColor().b, fade);
			blackFade.draw(fadeBatch);
			fadeBatch.end();
		}

		if (finished) {
			fade = Math.min(fade + Gdx.graphics.getDeltaTime() / 2.f, 1);
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
	public boolean keyDown(int keycode) {
		if (Gdx.input.isTouched())
			return false;
		if (keycode == Input.Keys.ESCAPE) {
			System.exit(0);
		}

		if (keycode == Input.Keys.SPACE) {
			Resources.getInstance().move.play();
			animatePlayer = true;
		}

		if (keycode == Input.Keys.R) {
			reset();
			Resources.getInstance().time = 0;
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
		Resources.getInstance().time = 0;
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

		if (Math.abs(touchDistance) < 0.5f) {
			Resources.getInstance().move.play();
			animatePlayer = true;
		}
		
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
