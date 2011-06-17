package de.swagner.mgcube;

import java.io.Console;
import java.util.logging.ConsoleHandler;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

import de.swagner.gdx.obj.normalmap.helper.ObjLoaderTan;
import de.swagner.gdx.obj.normalmap.shader.NormalMapShader;
import de.swagner.gdx.obj.normalmap.shader.TnLShader;
import de.swagner.gdx.obj.normalmap.shader.TransShader;

public class GameScreen extends DefaultScreen implements InputProcessor {

	double startTime = 0;
	PerspectiveCamera cam;
	Mesh blockModel;
	Mesh playerModel;
	Mesh targetModel;
	Mesh worldModel;
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
	
	//GLES20 
	Matrix4 model = new Matrix4().idt();
	Matrix4 modelView = new Matrix4().idt();
	Matrix4 modelViewProjection = new Matrix4().idt();
	Matrix4 tmp = new Matrix4().idt();
	private ShaderProgram shader;
	private Vector3 light = new Vector3(-2f, 1f, 10f);
	
	float touchStartX = 0;
	float touchStartY = 0;

	public GameScreen(Game game) {
		super(game);
		Gdx.input.setInputProcessor(this);

		blockModel = ObjLoaderTan.loadObj(Gdx.files.internal("data/cube.obj"));
		blockModel.getVertexAttribute(Usage.Position).alias = "a_vertex";
		blockModel.getVertexAttribute(Usage.Normal).alias = "a_normal";
//		blockModel.getVertexAttribute(Usage.Color).alias = "a_color";
		blockModel.getVertexAttribute(10).alias = "a_tangent";
		blockModel.getVertexAttribute(11).alias = "a_binormal";
//		blockModel.getVertexAttribute(Usage.TextureCoordinates).alias = "a_texcoord0";
		
		playerModel = ObjLoaderTan.loadObj(Gdx.files.internal("data/sphere.obj"));
		playerModel.getVertexAttribute(Usage.Position).alias = "a_vertex";
		playerModel.getVertexAttribute(Usage.Normal).alias = "a_normal";
		playerModel.getVertexAttribute(10).alias = "a_tangent";
		playerModel.getVertexAttribute(11).alias = "a_binormal";
		
		targetModel = ObjLoaderTan.loadObj(Gdx.files.internal("data/cylinder.obj"));
		targetModel.getVertexAttribute(Usage.Position).alias = "a_vertex";
		targetModel.getVertexAttribute(Usage.Normal).alias = "a_normal";
		targetModel.getVertexAttribute(10).alias = "a_tangent";
		targetModel.getVertexAttribute(11).alias = "a_binormal";
		
		worldModel = ObjLoaderTan.loadObj(Gdx.files.internal("data/cube.obj"));
		worldModel.getVertexAttribute(Usage.Position).alias = "a_vertex";
		worldModel.getVertexAttribute(Usage.Normal).alias = "a_normal";
		worldModel.getVertexAttribute(10).alias = "a_tangent";
		worldModel.getVertexAttribute(11).alias = "a_binormal";
		

		cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0, 0, 20f);
		cam.direction.set(0, 0, -1);
		cam.up.set(0, 1, 0);
		cam.near = 1f;
		cam.far = 1000;

		// controller = new PerspectiveCamController(cam);
		// Gdx.input.setInputProcessor(controller);

		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		font = new BitmapFont();

		initShader();
		initLevel(1);
		initRender();
	}

	private void initShader() {
		shader = new ShaderProgram(TransShader.mVertexShader, TransShader.mFragmentShader);
		if (shader.isCompiled() == false) {
            Gdx.app.log("ShaderTest", shader.getLog());
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
			
			//more levels

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
						blocks.add(new Block(new Vector3(-10f + (x*2), -10f + (y*2), -10f + (z*2))));
					}
					if (level[z][y][x] == 2) {
						player.position.x = -10f + (x*2);
						player.position.y = -10f + (y*2);
						player.position.z = -10f + (z*2);
					}
					if (level[z][y][x] == 3) {
						target.position.x = -10f + (x*2);
						target.position.y = -10f + (y*2);
						target.position.z = -10f + (z*2);
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
	}

	@Override
	public void show() {
	}

	protected int lastTouchX;
	protected int lastTouchY;

	@Override
	public void render(float delta) {
		Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
		Gdx.graphics.getGL20().glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		GL20 gl = Gdx.graphics.getGL20();
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		cam.update();

		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

		
		//collision
		Ray pRay = new Ray(player.position, player.direction);
		
		for(Block block : blocks)
		{
			Vector3 intersection = new Vector3();
			boolean intersect = Intersector.intersectRaySphere(pRay, block.position, 1f, intersection);
			float dst = intersection.dst(player.position);
			if(dst < 1.2f && intersect)
			{
				animatePlayer = false;
				break;
			}
		}
		Vector3 Targetintersection = new Vector3();
		boolean Targetintersect = Intersector.intersectRaySphere(pRay, target.position, 1f, Targetintersection);
		float targetdst = Targetintersection.dst(player.position);
		boolean resetter = false;
		if (targetdst < 1.2f) {
			resetter = true;
		}
		
		
		if (animatePlayer) {
			
			player.position.add(player.direction.x * delta * 10f, player.direction.y * delta * 10f, player.direction.z * delta * 10f);
			
			if(resetter)
			{
				animatePlayer = false;
				reset();
			}
			
			// Win?
//			float targetdst = target.position.dst(player.position);
//			if (targetdst < 2f) {
//				animatePlayer = false;
//				reset();
//			}
//			
//			for (Block block : blocks) {
//				// TODO only check blocks in moving direction
//				// block.position.dst(player.position);
//				
//				// distance < 2?
//				float dst = block.position.dst(player.position);
//				if (dst < 2.1f) {
//					animatePlayer = false;
//					player.position.sub(player.direction.x * delta * 8f, player.direction.y * delta * 8f, player.direction.z * delta * 8f);
//					break;
//				}
//				if (dst > 50f) {
//					reset();
//					break;
//				}
//			}

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
			
//			modelView.set(cam.view);
//			modelView.mul(model);			
//			tmp.setToRotation(angleY, modelView.getValues()[1], modelView.getValues()[5], modelView.getValues()[9]);
//			model.mul(tmp);
//			
//			modelView.set(cam.view);
//			modelView.mul(model);
//			tmp.setToRotation(angleX, modelView.getValues()[0], modelView.getValues()[4], modelView.getValues()[8]);
//			model.mul(tmp);
//			
			tmp.setToTranslation(block.position.x, block.position.y, block.position.z);
			model.mul(tmp);

			tmp.setToScaling(0.95f, 0.95f, 0.95f);
			model.mul(tmp);
			
			shader.begin();		

			modelViewProjection.idt();
			modelViewProjection.set(cam.combined);
			modelViewProjection = tmp.mul(model);
			
			shader.setUniformMatrix("MVPMatrix",modelViewProjection);
			
			shader.setUniformf("a_color", 1.0f, 0.1f, 0.1f);
			shader.setUniformf("alpha", 0.8f);
			blockModel.render(shader, GL20.GL_LINE_STRIP);
			
			shader.setUniformf("a_color", 1.0f, 0.1f, 0.1f);
			shader.setUniformf("alpha", 0.5f);
			blockModel.render(shader, GL20.GL_LINES);
			

			shader.end();		
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
			
//			modelView.set(cam.view);
//			modelView.mul(model);			
//			tmp.setToRotation(angleY, modelView.getValues()[1], modelView.getValues()[5], modelView.getValues()[9]);
//			model.mul(tmp);
//			
//			modelView.set(cam.view);
//			modelView.mul(model);
//			tmp.setToRotation(angleX, modelView.getValues()[0], modelView.getValues()[4], modelView.getValues()[8]);
//			model.mul(tmp);
//			
			tmp.setToTranslation(player.position.x, player.position.y, player.position.z);
			model.mul(tmp);
			
			shader.begin();		

			modelViewProjection.idt();
			modelViewProjection.set(cam.combined);
			modelViewProjection = tmp.mul(model);
			
			shader.setUniformMatrix("MVPMatrix",modelViewProjection);
//			shader.setUniformf("LightDirection", light.x, light.y, light.z);
						
			shader.setUniformf("a_color", 1.0f, 1.0f, 0.0f);
			shader.setUniformf("alpha", 0.5f);
			playerModel.render(shader, GL20.GL_TRIANGLES);
			shader.end();
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
			
//			modelView.set(cam.view);
//			modelView.mul(model);			
//			tmp.setToRotation(angleY, modelView.getValues()[1], modelView.getValues()[5], modelView.getValues()[9]);
//			model.mul(tmp);
//			
//			modelView.set(cam.view);
//			modelView.mul(model);
//			tmp.setToRotation(angleX, modelView.getValues()[0], modelView.getValues()[4], modelView.getValues()[8]);
//			model.mul(tmp);
//			
			tmp.setToTranslation(target.position.x, target.position.y, target.position.z);
			model.mul(tmp);
			
			shader.begin();		

			modelViewProjection.idt();
			modelViewProjection.set(cam.combined);
			modelViewProjection = tmp.mul(model);
			
			shader.setUniformMatrix("MVPMatrix",modelViewProjection);
//			shader.setUniformf("LightDirection", light.x, light.y, light.z);
			
			shader.setUniformf("a_color", 0.0f, 1.1f, 0.1f);
			shader.setUniformf("alpha", 0.8f);
			targetModel.render(shader, GL20.GL_LINE_STRIP);
			
			shader.setUniformf("a_color", 0.0f, 1.1f, 0.1f);
			shader.setUniformf("alpha", 0.5f);
			targetModel.render(shader, GL20.GL_TRIANGLES);
			
			shader.end();
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
			
//			modelView.set(cam.view);
//			modelView.mul(model);			
//			tmp.setToRotation(angleY, modelView.getValues()[1], modelView.getValues()[5], modelView.getValues()[9]);
//			model.mul(tmp);
//			
//			modelView.set(cam.view);
//			modelView.mul(model);
//			tmp.setToRotation(angleX, modelView.getValues()[0], modelView.getValues()[4], modelView.getValues()[8]);
//			model.mul(tmp);
//			
			tmp.setToTranslation(0,0,0);
			model.mul(tmp);
			
			shader.begin();		

			modelViewProjection.idt();
			modelViewProjection.set(cam.combined);
			modelViewProjection = tmp.mul(model);
			
			shader.setUniformMatrix("MVPMatrix",modelViewProjection);
//			shader.setUniformf("LightDirection", light.x, light.y, light.z);
			
			shader.setUniformf("a_color", 1.0f, 0.1f, 0.1f);
			shader.setUniformf("alpha", 0.8f);
			worldModel.render(shader, GL20.GL_LINE_STRIP);
			
			shader.end();
		}

		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
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
		
		if (keycode == Input.Keys.RIGHT) {
			Resources.getInstance().currentlevel++;
			initLevel(Resources.getInstance().currentlevel);
		}
		
		if (keycode == Input.Keys.LEFT) {
			Resources.getInstance().currentlevel--;
			initLevel(Resources.getInstance().currentlevel);
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

		if(touchTime<0.15) animatePlayer = true;
		
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
