package de.swagner.mgcube;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
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
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.actions.Delay;
import com.badlogic.gdx.utils.Array;

public class GameScreen extends DefaultScreen implements InputProcessor {

	float startTime = 0;
	PerspectiveCamera cam;
	Mesh blockModel;
	Mesh playerModel;
	Mesh targetModel;
	Mesh quadModel;
	Mesh wireCubeModel;
	Mesh sphereModel;
	float angleX = 0;
	float angleY = 0;

	float angleXBack = 0;
	float angleYBack = 0;
	SpriteBatch batch;
	SpriteBatch fontbatch;
	BitmapFont font;
	Player player = new Player();
	Target target = new Target();

	Array<Block> blocks = new Array<Block>();
	Array<Portal> portals = new Array<Portal>();
	Array<MovableBlock> movableBlocks = new Array<MovableBlock>();
	Array<Renderable> renderObjects = new Array<Renderable>();
	Array<Switch> switches = new Array<Switch>();
	Array<SwitchableBlock> switchblocks = new Array<SwitchableBlock>();
	
	boolean animateWorld = false;
	boolean warplock = false;
	boolean movwarplock = false;
	
	//fade
	SpriteBatch fadeBatch;
	Sprite blackFade;
	Sprite title;
	float fade = 1.0f;
	boolean finished = false;

	float delta;
	
	float touchDistance = 0;
	float touchTime = 0;

	Vector3 xAxis = new Vector3(1, 0, 0);
	Vector3 yAxis = new Vector3(0, 1, 0);
	Vector3 zAxis = new Vector3(0, 0, 1);

	// GLES20
	Matrix4 model = new Matrix4().idt();
	Matrix4 tmp = new Matrix4().idt();
	private ShaderProgram transShader;
	private ShaderProgram bloomShader;
	FrameBuffer frameBuffer;
	FrameBuffer frameBufferVert;
	
	//garbage collector
	int seconds;
	int minutes;
	Ray pRay = new Ray(new Vector3(), new Vector3());
	Ray mRay = new Ray(new Vector3(), new Vector3());
	Vector3 intersection = new Vector3();
	Vector3 portalIntersection = new Vector3();
	BoundingBox box = new BoundingBox(new Vector3(-10f, -10f, -10f), new Vector3(10f, 10f, 10f));
	Vector3 exit = new Vector3();
	int port = 0;
	Vector3 position = new Vector3();
	
	protected int lastTouchX;
	protected int lastTouchY;
	private float changeLevelEffect;
	
	float touchStartX = 0;
	float touchStartY = 0;
	private boolean changeLevel;

	public GameScreen(Game game) {
		super(game);
		Gdx.input.setInputProcessor(this);

		blockModel = Resources.getInstance().blockModel;
		playerModel = Resources.getInstance().playerModel;
		targetModel = Resources.getInstance().targetModel;
		quadModel = Resources.getInstance().quadModel;
		wireCubeModel = Resources.getInstance().wireCubeModel;
		sphereModel = Resources.getInstance().sphereModel;

		cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0, 0, 16f);
		cam.direction.set(0, 0, -1);
		cam.up.set(0, 1, 0);
		cam.near = 1f;
		cam.far = 1000;

		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		fontbatch = new SpriteBatch();

		blackFade = new Sprite(new Texture(Gdx.files.internal("data/blackfade.png")));
		fadeBatch = new SpriteBatch();
		fadeBatch.getProjectionMatrix().setToOrtho2D(0, 0, 2, 2);

		font = Resources.getInstance().font;
		font.setScale(1);

		transShader = Resources.getInstance().transShader;
		bloomShader = Resources.getInstance().bloomShader;
		
		initRender();
		
		initLevel(1);
	}
	
	public void initRender() {
		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

//		//antiAliasing for Desktop - no support in Android
//		Gdx.gl20.glEnable (GL10.GL_LINE_SMOOTH);
//		Gdx.gl20.glEnable (GL10.GL_BLEND);
//		Gdx.gl20.glBlendFunc (GL10.GL_SRC_ALPHA,GL10. GL_ONE_MINUS_SRC_ALPHA);
//		Gdx.gl20.glHint (GL10.GL_LINE_SMOOTH_HINT, GL10.GL_FASTEST);
//		Gdx.gl20.glLineWidth (1.5f);		
		
		frameBuffer = new FrameBuffer(Format.RGB565, Resources.getInstance().m_i32TexSize, Resources.getInstance().m_i32TexSize, false);		
		frameBufferVert = new FrameBuffer(Format.RGB565, Resources.getInstance().m_i32TexSize, Resources.getInstance().m_i32TexSize, false);
		
		Gdx.gl20.glDepthMask(false);
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		initRender();
	}
	
	private void initLevel(int levelnumber) {
		renderObjects.clear();
		blocks.clear();
		portals.clear();
		movableBlocks.clear();
		switchblocks.clear();
		switches.clear();
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
		case 4:
			level = Resources.getInstance().level4;
			break;
		case 5:
			level = Resources.getInstance().level5;
			break;
		case 6:
			level = Resources.getInstance().level6;
			break;
		case 7:
			level = Resources.getInstance().level7;
			break;
		case 8:
			level = Resources.getInstance().level8;
			break;
		case 9:
			level = Resources.getInstance().level9;
			break;
		case 10:
			level = Resources.getInstance().level10;
			break;
		case 11:
			level = Resources.getInstance().level11;
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
					if (level[z][y][x] >=4 && level[z][y][x] <=8) {
						Portal temp = new Portal(level[z][y][x]);
						temp.position.x = -10f + (x * 2);
						temp.position.y = -10f + (y * 2);
						temp.position.z = -10f + (z * 2);
						portals.add(temp);
						}
					if (level[z][y][x] >=-8 && level[z][y][x] <=-4){
						Portal temp = new Portal(level[z][y][x]);
						temp.position.x = -10f + (x * 2);
						temp.position.y = -10f + (y * 2);
						temp.position.z = -10f + (z * 2);
						portals.add(temp);
						}
					if (level[z][y][x] == 9){
						MovableBlock temp = new MovableBlock(new Vector3(-10f + (x * 2),-10f + (y * 2),-10f + (z * 2)));
						movableBlocks.add(temp);
						}
					if (level[z][y][x] <= -10){
						Switch temp = new Switch(new Vector3(-10f + (x * 2),-10f + (y * 2),-10f + (z * 2)));
						temp.id = level[z][y][x];
						switches.add(temp);
						}
					if (level[z][y][x] >= 10){
						SwitchableBlock temp = new SwitchableBlock(new Vector3(-10f + (x * 2),-10f + (y * 2),-10f + (z * 2)));
						temp.id = level[z][y][x];
						switchblocks.add(temp);
						}
				}
			}
		}
		
		renderObjects.add(player);
		renderObjects.add(target);
		renderObjects.addAll(blocks);		
		renderObjects.addAll(portals);
		renderObjects.addAll(movableBlocks);
		renderObjects.addAll(switches);
		renderObjects.addAll(switchblocks);
		
		for(Switch s : switches) {
			Array<SwitchableBlock> tmp = getCorrespondingSwitchableBlock(s.id);
			if(tmp != null) {
				s.sBlocks = tmp;
			}
		}
		
	}

	private void reset() {
		animateWorld = false;
		player.stop();
		for(MovableBlock m : movableBlocks) {
			m.stop();
		}
		
		for(Switch s : switches) {
			s.isSwitched = false;
		}
		for(SwitchableBlock s : switchblocks) {
			s.isSwitched = false;
		}
		warplock = false;
		movwarplock = false;
		port=0;
		
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

	@Override
	public void show() {
	}

	@Override
	public void render(float deltaTime) {
		delta = Math.min(0.02f, deltaTime);
		
		startTime += delta;
		
		angleXBack += MathUtils.sin(startTime)/10f;;
		angleYBack += MathUtils.cos(startTime)/5f;;
		
		cam.update();
		
		if(player.isMoving) {
			player.position.add(player.direction.x * delta * 10f, player.direction.y * delta * 10f, player.direction.z * delta * 10f);
		}
		
		for(MovableBlock m : movableBlocks) {
			if(m.isMoving) {
				m.position.add(m.direction.x * delta * 10f, m.direction.y * delta * 10f, m.direction.z * delta * 10f);
			}
		}		
		collisionTest();
		
		sortScene();
		
		frameBuffer.begin();
		renderScene();
		frameBuffer.end();

		//PostProcessing
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDisable(GL20.GL_BLEND);
		
		frameBuffer.getColorBufferTexture().bind(0);
		
		bloomShader.begin();
		bloomShader.setUniformi("sTexture", 0);
		bloomShader.setUniformf("bloomFactor", Helper.map((MathUtils.sin(startTime * 3f) * 0.5f) + 0.5f,0,1,0.50f,0.70f)+changeLevelEffect);
		
		frameBufferVert.begin();
		bloomShader.setUniformf("TexelOffsetX", Resources.getInstance().m_fTexelOffset);
		bloomShader.setUniformf("TexelOffsetY", 0.0f);
		quadModel.render(bloomShader, GL20.GL_TRIANGLE_STRIP);
		frameBufferVert.end();
		
		frameBufferVert.getColorBufferTexture().bind(0);
		
		frameBuffer.begin();		
		bloomShader.setUniformf("TexelOffsetX", 0.0f);
		bloomShader.setUniformf("TexelOffsetY", Resources.getInstance().m_fTexelOffset);
		quadModel.render(bloomShader, GL20.GL_TRIANGLE_STRIP);
		frameBuffer.end();

		bloomShader.end(); 
		
		//render scene again
		renderScene();
			
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDisable(GL20.GL_BLEND);
				
		batch.enableBlending();
		batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
		batch.begin();
		batch.draw(frameBuffer.getColorBufferTexture(), 0, 0,800,480,0,0,frameBuffer.getWidth(),frameBuffer.getHeight(),false,true);
		batch.end();

		
		//GUI
		//fontbatch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		fontbatch.begin();
		font.draw(fontbatch, "level: " + Resources.getInstance().currentlevel, 620, 100);
		font.draw(fontbatch, "fps: " + Gdx.graphics.getFramesPerSecond(), 620, 40);
		font.draw(fontbatch, "lives: " + Resources.getInstance().lives, 620, 80);
		Resources.getInstance().time += delta;
		seconds = (int) Resources.getInstance().time % 60;
		minutes = (int)Resources.getInstance().time / 60;
		if(seconds > 9 && minutes > 9)
			font.draw(fontbatch, "time: " + minutes + ":" + seconds, 620, 60);
		else if(seconds > 9 && minutes < 10)
			font.draw(fontbatch, "time: 0" + minutes + ":" + seconds, 620, 60);
		else if(seconds < 10 && minutes > 9)
			font.draw(fontbatch, "time: " + minutes + ":0" + seconds, 620, 60);
		else
			font.draw(fontbatch, "time: 0" + minutes + ":0" + seconds, 620, 60);
		fontbatch.end();
		
		
		//FadeInOut
		if (!finished && fade > 0) {
			fade = Math.max(fade - (delta / 2.f), 0);
			fadeBatch.begin();
			blackFade.setColor(blackFade.getColor().r, blackFade.getColor().g, blackFade.getColor().b, fade);
			blackFade.draw(fadeBatch);
			fadeBatch.end();
		}

		if (finished) {
			fade = Math.min(fade + (delta / 2.f), 1);
			fadeBatch.begin();
			blackFade.setColor(blackFade.getColor().r, blackFade.getColor().g, blackFade.getColor().b, fade);
			blackFade.draw(fadeBatch);
			fadeBatch.end();
			if (fade >= 1) {
				game.setScreen(new MainMenuScreen(game));
			}
		}
		
		//LevelChangeEffect
		if (!changeLevel && changeLevelEffect > 0) {
			changeLevelEffect = Math.max(changeLevelEffect - (delta * 15.f), 0);
		}

		if (changeLevel) {
			changeLevelEffect = Math.min(changeLevelEffect + (delta * 15.f), 5);
			if (changeLevelEffect >= 5) {				
				nextLevel();
			}
		}

	}

	private void sortScene() {
		//sort blocks because of transparency
		for (Renderable renderable : renderObjects) {
			tmp.idt();
			model.idt();
			
			tmp.setToScaling(0.5f, 0.5f, 0.5f);
			model.mul(tmp);

			tmp.setToRotation(xAxis, angleX);
			model.mul(tmp);
			tmp.setToRotation(yAxis, angleY);
			model.mul(tmp);

			tmp.setToTranslation(renderable.position.x, renderable.position.y, renderable.position.z);
			model.mul(tmp);

			tmp.setToScaling(0.95f, 0.95f, 0.95f);
			model.mul(tmp);
			
			model.getTranslation(position);
			
			renderable.model.set(model);
			
			renderable.sortPosition = cam.position.dst(position);
		}
		renderObjects.sort();
	}

	private void renderScene() {
		
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
				
		transShader.begin();
		transShader.setUniformMatrix("VPMatrix", cam.combined);
				
		{
			// render Background Wire
			tmp.idt();
			model.idt();

			tmp.setToScaling(20.5f, 20.5f, 20.5f);
			model.mul(tmp);

			tmp.setToRotation(xAxis, angleX + angleXBack);
			model.mul(tmp);
			tmp.setToRotation(yAxis, angleY + angleYBack);
			model.mul(tmp);

			tmp.setToTranslation(0, 0, 0);
			model.mul(tmp);

			transShader.setUniformMatrix("MMatrix", model);

			transShader.setUniformf("a_color", 1.0f, 0.8f, 0.8f, 0.07f);
			sphereModel.render(transShader, GL20.GL_LINE_STRIP);
		}
		
		{
			// render Wire
			tmp.idt();
			model.idt();

			tmp.setToScaling(5.5f, 5.5f, 5.5f);
			model.mul(tmp);

			tmp.setToRotation(xAxis, angleX);
			model.mul(tmp);
			tmp.setToRotation(yAxis, angleY);
			model.mul(tmp);

			tmp.setToTranslation(0, 0, 0);
			model.mul(tmp);

			transShader.setUniformMatrix("MMatrix", model);

			transShader.setUniformf("a_color", 1.0f, 0.1f, 0.1f, 0.5f);
			wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);

			transShader.setUniformf("a_color", 1.0f, 0.1f, 0.1f,  0.04f);
			blockModel.render(transShader, GL20.GL_TRIANGLES);
		}
				
		// render all objects
		for (Renderable renderable : renderObjects) {
			
			//render impact
			if(renderable.isCollidedAnimation == true && renderable.collideAnimation == 0) {
				renderable.collideAnimation = 1.0f;
			}
			if(renderable.collideAnimation>0.0f) {
				renderable.collideAnimation -= delta*1.f;
				renderable.collideAnimation = Math.max(0.0f, renderable.collideAnimation);
				if(renderable.collideAnimation == 0.0f) renderable.isCollidedAnimation = false;
			}
			
			
			if(renderable instanceof Block) {
				model.set(renderable.model);
	
				transShader.setUniformMatrix("MMatrix", model);
	
				transShader.setUniformf("a_color", 1.0f, 0.1f, 0.1f, 0.8f + renderable.collideAnimation);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", 1.0f, 0.1f, 0.1f, 0.2f + renderable.collideAnimation);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
			}
			
			// render movableblocks
			if(renderable instanceof MovableBlock) {
				model.set(renderable.model);
	
				transShader.setUniformMatrix("MMatrix", model);
	
				transShader.setUniformf("a_color", 1.0f, 0.8f, 0.1f, 0.8f + renderable.collideAnimation);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", 1.0f, 0.8f, 0.1f, 0.2f + renderable.collideAnimation);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
			}
			
			// render switchableblocks
			if(renderable instanceof SwitchableBlock) {
				if(!((SwitchableBlock) renderable).isSwitched) {	
					model.set(renderable.model);
		
					transShader.setUniformMatrix("MMatrix", model);
		
					transShader.setUniformf("a_color", 1.0f, 1f, 1f, 0.8f + renderable.collideAnimation);
					wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
		
					transShader.setUniformf("a_color", 1.0f, 1f, 1f, 0.2f + renderable.collideAnimation);
					blockModel.render(transShader, GL20.GL_TRIANGLES);
				}
			}
			
			// render switches
			if(renderable instanceof Switch) {
				model.set(renderable.model);	

				tmp.setToScaling(0.3f, 0.3f, 0.3f);
				model.mul(tmp);

				transShader.setUniformMatrix("MMatrix", model);
				transShader.setUniformf("a_color", 1.0f, 1.0f, 1.0f, 0.8f);
				playerModel.render(transShader, GL20.GL_TRIANGLES);
				
				tmp.setToScaling(2.0f, 2.0f, 2.0f);
				model.mul(tmp);

				//render hull			
				transShader.setUniformMatrix("MMatrix", model);
				transShader.setUniformf("a_color", 1.0f, 1.0f, 1.0f, 0.2f);
				playerModel.render(transShader, GL20.GL_LINE_STRIP);
			}
			
			// render Player
			if(renderable instanceof Player) {
				model.set(renderable.model);	
				
				tmp.setToRotation(xAxis, angleXBack);
				model.mul(tmp);
				tmp.setToRotation(yAxis, angleYBack);
				model.mul(tmp);

				tmp.setToScaling(0.5f, 0.5f, 0.5f);
				model.mul(tmp);

				transShader.setUniformMatrix("MMatrix", model);
				transShader.setUniformf("a_color", 1.0f, 1.0f, 0.0f, 0.4f);
				playerModel.render(transShader, GL20.GL_TRIANGLES);
				
				tmp.setToScaling(2.0f, 2.0f, 2.0f);
				model.mul(tmp);

				//render hull			
				transShader.setUniformMatrix("MMatrix", model);
				transShader.setUniformf("a_color", 1.0f, 1.0f, 0.0f, 0.4f);
				playerModel.render(transShader, GL20.GL_LINE_STRIP);
			}
			
			// render Portals
			if(renderable instanceof Portal) {
				if(renderable.position.x != -11) {
					// render Portal
					model.set(renderable.model);
		
					transShader.setUniformMatrix("MMatrix", model);
					
					transShader.setUniformf("a_color", 0.0f, 0.03f * ( Math.abs(((Portal)renderable).id)*5.0f), 1.0f, 0.5f + renderable.collideAnimation);
					blockModel.render(transShader, GL20.GL_TRIANGLES);
					
					//render hull			
					transShader.setUniformf("a_color", 0.0f,0.03f * ( Math.abs(((Portal)renderable).id)*5.0f), 1.0f, 0.4f + renderable.collideAnimation);
					wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
				}
			}
				
			// render Target
			if(renderable instanceof Target) {
				model.set(renderable.model);
				
				tmp.setToRotation(yAxis, angleY + angleYBack);
				model.mul(tmp);

				transShader.setUniformMatrix("MMatrix", model);

				transShader.setUniformf("a_color", 0.0f, 1.1f, 0.1f,0.5f + renderable.collideAnimation);
				targetModel.render(transShader, GL20.GL_TRIANGLES);
				
				//render hull			
				transShader.setUniformf("a_color", 0.0f, 1.1f, 0.1f, 0.4f + renderable.collideAnimation);
				targetModel.render(transShader, GL20.GL_LINE_STRIP);
			}
				
		}
		
		transShader.end();
	}

	private void collisionTest() {
		// collision
		if (player.isMoving) {
			
			pRay.set(player.position, player.direction);

			for (Block block : blocks) {
				boolean intersect = Intersector.intersectRaySphere(pRay, block.position, 1f, intersection);
				float dst = intersection.dst(player.position);
				if (dst < 1.0f && intersect) {
					player.stop();
					block.isCollidedAnimation = true;
					break;
				}
			}
			
			for (MovableBlock m : movableBlocks) {
				boolean intersect = Intersector.intersectRaySphere(pRay, m.position, 1f, intersection);
				float movdst = intersection.dst(player.position);
				if (movdst < 1.0f && box.contains(m.position) && intersect) {
					player.stop();
					m.move(player.direction.cpy());
					m.isCollidedAnimation = true;
				}
				else if(movdst <1.0f && !box.contains(m.position) && intersect) {
					player.stop();
					m.isCollidedAnimation = true;
				}
				
				//recursiveCollisionCheck(m);
			}
			
			for (SwitchableBlock s : switchblocks) {
				boolean swintersect = Intersector.intersectRaySphere(pRay, s.position, 1f, intersection);
				float swdst = intersection.dst(player.position);
				if (swdst < 1.0f && swintersect && !s.isSwitched) {
					player.stop();
					s.isCollidedAnimation = true;
					break;
				}
			}

			boolean targetIntersect = Intersector.intersectRaySphere(pRay, target.position, 1f, intersection);
			float targetdst = intersection.dst(player.position);
			boolean win = false;
			if (targetdst < 0.2f && targetIntersect) {
				win = true;
				target.isCollidedAnimation = true;
			}
			
			for(Switch s : switches) {
//				if (s.position.equals(player.position) && !s.isSwitched) {
//					s.isSwitched = true;
//					s.isLocked = true;
//					s.isCollidedAnimation = true;
//					setCorrespondingSwitchBlocks(s);
//				}
//				if(!s.position.equals(player.position) && s.isSwitched) {
//					s.isSwitched = false;
//					s.isLocked = false;
//				}
				s.isSwitched = false;
				setCorrespondingSwitchBlocks(s);
			}
			
			portalIntersection.set(0, 0, 0);
			boolean warp = false;

			if (!warplock) {
				for (Portal portal : portals) {
					
					boolean portalintersect = Intersector.intersectRaySphere(pRay, portal.position, 1f, portalIntersection);
					float portaldst = portalIntersection.dst(player.position);
					
					if (portaldst < 0.2f && portalintersect) {
						warp = true;
						warplock = false;
						port = portal.id;
						portal.isCollidedAnimation = true;
						break;
					}
				}
			} else {
				//end warplock
				for(Portal p : portals) {
					if (p.id == -port) {
						boolean portalintersect = Intersector.intersectRaySphere(pRay, p.position, 1f, portalIntersection);
						if (!portalintersect) {
							warplock = false;
						}
					}
				}
			}

			// player out of bound?
			if (!box.contains(player.position)) {
				player.stop();
				Resources.getInstance().lives--;
				reset();
			}

			if (win) {
				player.stop();
				changeLevel = true;
			}

			if (warp) {
				Portal tmp = getCorrespondingPortal(port);
				if(tmp != null) {
					player.position = tmp.position.cpy();
					warplock = true;
					tmp.isCollidedAnimation = true;
				}
			}
		}
		else
			warplock = false;
		
		// collisiontest for movable blocks
		for(MovableBlock m : movableBlocks) {
			mRay.set(m.position, m.direction);
			if(m.isMoving) {
				player.stop();
				
				for (Block block : blocks) {
					boolean intersect = Intersector.intersectRaySphere(mRay, block.position, 1f, intersection);
					float dst = intersection.dst(m.position);
					if (dst < 1.0f && intersect) {
						m.stop();
						block.isCollidedAnimation = true;
						break;
					}
				}
				
				//NOTE: THIS SHOULD NOT HAPPEN
				boolean targetIntersect = Intersector.intersectRaySphere(mRay, target.position, 1f, intersection);
				float targetdst = intersection.dst(m.position);
				if (targetdst < 1.0f && targetIntersect) {
					m.stop();
				}
				
				//NOTE: THIS REALLY SHOULD NOT HAPPEN
				boolean playerIntersect = Intersector.intersectRaySphere(mRay, player.position, 1f, intersection);
				float playerdst = intersection.dst(m.position);
				if (playerdst < 1.0f && playerIntersect) {
					m.stop();
				}
				
				for (SwitchableBlock s : switchblocks) {
					boolean swintersect = Intersector.intersectRaySphere(mRay, s.position, 1f, intersection);
					float swdst = intersection.dst(m.position);
					if (swdst < 1.0f && swintersect && !s.isSwitched) {
						m.stop();
						s.isCollidedAnimation = true;
					}
				}
				
				for(Switch s : switches) {
//					if (s.position.equals(m.position) && !s.isSwitched) {
//						s.isSwitched = !s.isSwitched;
//						s.isCollidedAnimation = true;
//						setCorrespondingSwitchBlocks(s);
//					}
//					if(!m.position.equals(s.position) && s.isSwitched)
//						s.isLocked = false;
//						s.isSwitched  = false;
					s.isSwitched = false;
					setCorrespondingSwitchBlocks(s);
				}
				
				boolean warp = false;
				portalIntersection.set(0, 0, 0);
				if (!movwarplock) {
					for (Portal portal : portals) {
						
						boolean portalintersect = Intersector.intersectRaySphere(mRay, portal.position, 1f, portalIntersection);
						float portaldst = portalIntersection.dst(m.position);
						
						if (portaldst < 0.2f && portalintersect) {
							warp = true;
							movwarplock = false;
							port = portal.id;
							portal.isCollidedAnimation = true;
							break;
						}
					}
				} else {
					//end warplock
					for(Portal p : portals) {
						if (p.id == -port) {
							boolean portalintersect = Intersector.intersectRaySphere(mRay, p.position, 1f, portalIntersection);
							if (!portalintersect) {
								movwarplock = false;
							}
						}
					}
				}
				
				if (warp) {
					Portal tmp = getCorrespondingPortal(port);
					if(tmp != null) {
						m.position = tmp.position.cpy();
						movwarplock = true;
						tmp.isCollidedAnimation = true;
					}
				}
				
				for(MovableBlock mm : movableBlocks) {
					
					boolean intersect = Intersector.intersectRaySphere(mRay, mm.position, 1f, intersection);
					float dst = intersection.dst(m.position);
					if (dst < 1.0f && intersect) {
						m.stop();
						if(box.contains(mm.position)) 
							mm.move(m.direction);
						else
							player.stop();
						mm.isCollidedAnimation = true;
						break;
					}
				}
				
//				if(recursiveCollisionCheck(m)) {
//					m.stop();
//					//player.stop();
//				}
				
			}
				
			//movblock out of bound?
			if (!box.contains(m.position)) {
				m.stop();
				movwarplock = false;
			}
		}
		
		for(Switch s : switches) {
			s.isSwitched = false;
			for(MovableBlock m : movableBlocks) {
				if(m.position.equals(s.position)) {
					s.isSwitched = true;
				}
			}
			if(s.position.equals(player.position)) {
				s.isSwitched = true;
			}
			setCorrespondingSwitchBlocks(s);
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
		if (keycode == Input.Keys.ESCAPE) {
			System.exit(0);
		}

		if (keycode == Input.Keys.SPACE) {
			movePlayer();
		}

		if (keycode == Input.Keys.R) {
			reset();
			Resources.getInstance().time = 0;
		}

		if (keycode == Input.Keys.RIGHT) {
			changeLevel = true;
		}

		if (keycode == Input.Keys.LEFT) {
			prevLevel();
		}
		return false;
	}

	private void movePlayer() {
		if (!player.isMoving) {
			player.direction.set(0, 0, -1);
			player.direction.rot(new Matrix4().setToRotation(xAxis, -angleX));
			player.direction.rot(new Matrix4().setToRotation(yAxis, -angleY));
			player.move();
			
		}
	}

	private void nextLevel() {
		Resources.getInstance().currentlevel++;
		Resources.getInstance().time = 0;
		initLevel(Resources.getInstance().currentlevel);
		changeLevel = false;
	}

	private void prevLevel() {
		Resources.getInstance().currentlevel--;
		Resources.getInstance().time = 0;
		initLevel(Resources.getInstance().currentlevel);
		changeLevel = false;
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

		if (Math.abs(touchDistance) < 1.0f && touchTime < 0.3f) {
			movePlayer();
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
		touchTime += Gdx.graphics.getDeltaTime();

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
	
	public Portal getCorrespondingPortal(int ids)
	{
		for(Portal p : portals)
		{
			if(p.id == -ids)
				return p;
		}
		return null;
	}
	
	public void setCorrespondingSwitchBlocks(Switch s) {
		for(SwitchableBlock sw : s.sBlocks) {
			sw.isSwitched = s.isSwitched;
		}
	}
	
	public Array<SwitchableBlock> getCorrespondingSwitchableBlock(int ids) {
		Array<SwitchableBlock> temp = new Array<SwitchableBlock>();
		for(SwitchableBlock sw : switchblocks) {
			if(sw.id == -ids) {
				temp.add(sw);
			}
		}
		return temp;
	}
	
	//for movable objects in a row
	public boolean recursiveCollisionCheck(MovableBlock mov)
	{
		if(!box.contains(mov.position)) {
			return true;
		}
		mRay.set(mov.position, mov.direction);
		MovableBlock next = null;
		for(MovableBlock m :movableBlocks) {
			if(m.position != mov.position && !m.isMoving) {
				boolean intersect = Intersector.intersectRaySphere(mRay, m.position, 1f, intersection);
				float dst = intersection.dst(mov.position);
				if(dst < 1.2f && intersect) {
					next = m;
					next.isCollidedAnimation = true;
					break;
				}
			}
		}
		if(next != null) {
			recursiveCollisionCheck(next);
		}
		return false;
	}

}
