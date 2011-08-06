package de.swagner.mgcube;

import java.util.HashMap;

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
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

public class GameScreen extends DefaultScreen implements InputProcessor {

	float startTime = 0;
	PerspectiveCamera cam;
	Mesh blockModel;
	Mesh playerModel;
	Mesh coneModel;
	Mesh targetModel;
	Mesh quadModel;
	Mesh wireCubeModel;
	Mesh sphereModel;
	Mesh sphereSliceModel;
	float angleX = 0;
	float angleY = 0;

	float angleXBack = 0;
	float angleYBack = 0;
	float angleXFront = 0;
	float angleYFront = 0;
	SpriteBatch batch;
	SpriteBatch fontbatch;
	BitmapFont font;
	BitmapFont timeAttackFont; //used only for drawing the +45 notification
	Player player = new Player();
	PlayerShadow playerShadow = new PlayerShadow();
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
	Portal port = new Portal();
	Vector3 position = new Vector3();
	
	protected int lastTouchX;
	protected int lastTouchY;
	private float changeLevelEffect;
	
	float touchStartX = 0;
	float touchStartY = 0;
	private boolean changeLevel;
	
	//pinchToZoom
	HashMap<Integer, Vector2> pointers  = new HashMap<Integer,Vector2>();;
	Vector2 v1 = new Vector2();
	Vector2 v2 = new Vector2();
	int finger_one_pointer = -1;
	int finger_two_pointer = -1;
	float initialDistance = 0f;
	float distance = 0f;
	
	//0 = puzzle
	//1 = time attack
	int mode = 0;

	public GameScreen(Game game, int level, int mode) {
		super(game);
		Gdx.input.setCatchBackKey( true );
		Gdx.input.setInputProcessor(this);

		this.mode = mode;
		Resources.getInstance().time = 0;
		Resources.getInstance().timeAttackTime = 120;
		
		Resources.getInstance().currentlevel = level;
		
		blockModel = Resources.getInstance().blockModel;
		playerModel = Resources.getInstance().playerModel;
		coneModel = Resources.getInstance().coneModel;
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
		
		timeAttackFont = Resources.getInstance().timeAttackFont;
		timeAttackFont.setScale(1);

		transShader = Resources.getInstance().transShader;
		bloomShader = Resources.getInstance().bloomShader;
		
		initRender();
		angleY = 160;
		angleX = 0;
		
		initLevel(level);
		
		updatePlayerShadow();
	}
	
	public void initRender() {
		Gdx.graphics.getGL20().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		frameBuffer = new FrameBuffer(Format.RGB565, Resources.getInstance().m_i32TexSize, Resources.getInstance().m_i32TexSize, false);
		frameBufferVert = new FrameBuffer(Format.RGB565, Resources.getInstance().m_i32TexSize, Resources.getInstance().m_i32TexSize, false);

		Gdx.gl.glClearColor(Resources.getInstance().clearColor[0],Resources.getInstance().clearColor[1],Resources.getInstance().clearColor[2],Resources.getInstance().clearColor[3]);
		Gdx.graphics.getGL20().glDepthMask(true);
		Gdx.graphics.getGL20().glColorMask(true, true, true, true);
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0, 0, 16f);
		cam.direction.set(0, 0, -1);
		cam.up.set(0, 1, 0);
		cam.near = 1f;
		cam.far = 1000;
		initRender();
	}
	
	private void initLevel(int levelnumber) {
		renderObjects.clear();
		blocks.clear();
		portals.clear();
		movableBlocks.clear();
		switchblocks.clear();
		switches.clear();
		timeAttackFont.setColor(1,1,1,1);
		int[][][] level = Resources.getInstance().level1;
		try {
		level = Resources.getInstance().levels[levelnumber-1];
		} catch(ArrayIndexOutOfBoundsException e) {
			
		}

		// finde player pos
		int z = 0, y = 0, x = 0;
		for (z = 0; z < 10; z++) {
			for (y = 0; y < 10; y++) {
				for (x = 0; x < 10; x++) {
					if (level[z][y][x] == 1) {
						blocks.add(new Block(new Vector3(10f - (x * 2), -10f + (y * 2), -10f + (z * 2))));
					}
					if (level[z][y][x] == 2) {
						player.position.x = 10f - (x * 2);
						player.position.y = -10f + (y * 2);
						player.position.z = -10f + (z * 2);
					}
					if (level[z][y][x] == 3) {
						target.position.x = 10f - (x * 2);
						target.position.y = -10f + (y * 2);
						target.position.z = -10f + (z * 2);
					}
					if (level[z][y][x] >=4 && level[z][y][x] <=8) {
						Portal temp = new Portal(level[z][y][x]);
						temp.position.x = 10f - (x * 2);
						temp.position.y = -10f + (y * 2);
						temp.position.z = -10f + (z * 2);
						portals.add(temp);
						}
					if (level[z][y][x] >=-8 && level[z][y][x] <=-4){
						Portal temp = new Portal(level[z][y][x]);
						temp.position.x = 10f - (x * 2);
						temp.position.y = -10f + (y * 2);
						temp.position.z = -10f + (z * 2);
						portals.add(temp);
						}
					if (level[z][y][x] == 9){
						MovableBlock temp = new MovableBlock(new Vector3(10f - (x * 2),-10f + (y * 2),-10f + (z * 2)));
						movableBlocks.add(temp);
						}
					if (level[z][y][x] <= -10){
						Switch temp = new Switch(new Vector3(10f - (x * 2),-10f + (y * 2),-10f + (z * 2)));
						temp.id = level[z][y][x];
						switches.add(temp);
						}
					if (level[z][y][x] >= 10){
						SwitchableBlock temp = new SwitchableBlock(new Vector3(10f - (x * 2),-10f + (y * 2),-10f + (z * 2)));
						temp.id = level[z][y][x];
						switchblocks.add(temp);
						}
				}
			}
		}
		
		renderObjects.add(player);
		
		playerShadow.position.set(player.position);
		renderObjects.add(playerShadow);
		
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

		for(int i = 0; i<portals.size; i++) {
			for(Portal q : portals) {
				if(portals.get(i).id == -q.id) {
					portals.get(i).correspondingPortal = q;
				}
			}
		}	
	
		
	}

	private void reset() {
		if(Resources.getInstance().currentlevel-1 >= Resources.getInstance().levelcount) {
			finished = true;
			return;
		}
		
		player.collideAnimation = 1;
		animateWorld = false;
		player.stop();
		updatePlayerShadow();
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
		port=new Portal();
		
		initLevel(Resources.getInstance().currentlevel);
//		if(Resources.getInstance().lives < 1)
//		{
//			Resources.getInstance().lives = 3;
//			Resources.getInstance().currentlevel = 1;
//			initLevel(Resources.getInstance().currentlevel);
//			Resources.getInstance().time = 0;
//			game.setScreen(new MainMenuScreen(game));			
//		}
	}

	@Override
	public void show() {
	}

	@Override
	public void render(float deltaTime) {
		delta = Math.min(0.02f, deltaTime);
	
		startTime += delta;
		touchTime += Gdx.graphics.getDeltaTime();
		
		angleXBack += MathUtils.sin(startTime) * delta * 10f;
		angleYBack += MathUtils.cos(startTime) * delta * 5f;

		angleXFront += MathUtils.sin(startTime) * delta * 10f;
		angleYFront += MathUtils.cos(startTime) * delta * 5f;

		cam.update();
		
		if(player!= null && player.isMoving) {
			player.position.add(player.direction.x * delta * 10f, player.direction.y * delta * 10f, player.direction.z * delta * 10f);
		}
				
		if(playerShadow!= null && playerShadow.isMoving) {
			playerShadow.position.add(playerShadow.direction.x * delta, playerShadow.direction.y * delta, playerShadow.direction.z * delta);
		}
		playerShadow.distance = player.position.dst(playerShadow.position);
		if(playerShadow.distance>3.f) playerShadow.position.set(player.position);
		
		for(MovableBlock m : movableBlocks) {
			if(m.isMoving) {
				m.position.add(m.direction.x * delta * 10f, m.direction.y * delta * 10f, m.direction.z * delta * 10f);
			}
		}	
		
		collisionTest();
		
		sortScene();
		
		// render scene again
		renderScene();

		if(Resources.getInstance().bloomOnOff) {
			frameBuffer.begin();
			renderScene();
			frameBuffer.end();
	
			// PostProcessing
			Gdx.gl.glDisable(GL20.GL_CULL_FACE);
			Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
			Gdx.gl.glDisable(GL20.GL_BLEND);
	
			frameBuffer.getColorBufferTexture().bind(0);
	
			bloomShader.begin();
			bloomShader.setUniformi("sTexture", 0);
			bloomShader.setUniformf("bloomFactor", Helper.map((MathUtils.sin(startTime * 3f) * 0.5f) + 0.5f + changeLevelEffect,0,1,0.50f,0.70f));
	
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
			batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_DST_ALPHA);
			batch.begin();
			batch.draw(frameBuffer.getColorBufferTexture(), 0, 0, 800, 480, 0, 0, frameBuffer.getWidth(), frameBuffer.getHeight(), false, true);
			batch.end();
			
			if(Gdx.graphics.getBufferFormat().coverageSampling) {
				Gdx.gl.glClear(GL20.GL_COVERAGE_BUFFER_BIT_NV);
				Gdx.graphics.getGL20().glColorMask(false, false, false, false);			
				renderScene();
				Gdx.graphics.getGL20().glColorMask(true, true, true, true);
				
				Gdx.gl.glDisable(GL20.GL_CULL_FACE);
				Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
				Gdx.gl.glDisable(GL20.GL_BLEND);
			}

		} else {
			Gdx.gl.glDisable(GL20.GL_CULL_FACE);
			Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
			Gdx.gl.glDisable(GL20.GL_BLEND);
		}	
		
		//GUI
		fontbatch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		fontbatch.begin();
		
		font.draw(fontbatch, "level: " + Resources.getInstance().currentlevel, 620, 80);	
		
		Resources.getInstance().time += delta;
		Resources.getInstance().timeAttackTime -= delta;
		
		if(mode == 0) {			
			seconds = (int) Resources.getInstance().time % 60;
			minutes = (int)Resources.getInstance().time / 60;
		} else if ( mode == 1) {			
			seconds = (int) Resources.getInstance().timeAttackTime % 60;
			minutes = (int)Resources.getInstance().timeAttackTime / 60;
		}

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
			fade = Math.max(fade - (delta*2.f), 0);
			fadeBatch.begin();
			blackFade.setColor(blackFade.getColor().r, blackFade.getColor().g, blackFade.getColor().b, fade);
			blackFade.draw(fadeBatch);
			fadeBatch.end();
		}

		if (finished) {
			fade = Math.min(fade + (delta*2.f), 1);
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
			if(mode == 1) {
				fontbatch.begin();
				timeAttackFont.draw(fontbatch, "+45", 740, 40 - changeLevelEffect * 4);
				timeAttackFont.setColor(1, 1, 1, 1 / changeLevelEffect);
				fontbatch.end();
			}
			if (changeLevelEffect >= 5) {				
				nextLevel();
			}
		}
		
		
		if(Resources.getInstance().timeAttackTime <= 0.5 && mode ==1) {
			finished = true;
			HighScoreManager.getInstance().newTimeAttackHighScore(0, Resources.getInstance().levelcount);
		}

	}

	private void sortScene() {
		//sort blocks because of transparency
		for (Renderable renderable : renderObjects) {
			tmp.idt();
			model.idt();
			
			tmp.setToScaling(0.5f, 0.5f, 0.5f);
			model.mul(tmp);

			tmp.setToRotation(Vector3.X, angleX);
			model.mul(tmp);
			tmp.setToRotation(Vector3.Y, angleY);
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
		
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		transShader.begin();
		transShader.setUniformMatrix("VPMatrix", cam.combined);
		{
			// render Background Wire
			tmp.idt();
			model.idt();

			tmp.setToScaling(20.5f, 20.5f, 20.5f);
			model.mul(tmp);

			tmp.setToRotation(Vector3.X, angleX + angleXBack);
			model.mul(tmp);
			tmp.setToRotation(Vector3.Y, angleY + angleYBack);
			model.mul(tmp);

			tmp.setToTranslation(0, 0, 0);
			model.mul(tmp);

			transShader.setUniformMatrix("MMatrix", model);

			transShader.setUniformf("a_color", Resources.getInstance().backgroundWireColor[0],Resources.getInstance().backgroundWireColor[1],Resources.getInstance().backgroundWireColor[2],Resources.getInstance().backgroundWireColor[3]);
			playerModel.render(transShader, GL20.GL_LINE_STRIP);
		}
		{
			// render Wire
			tmp.idt();
			model.idt();

			tmp.setToScaling(5.5f, 5.5f, 5.5f);
			model.mul(tmp);

			tmp.setToRotation(Vector3.X, angleX);
			model.mul(tmp);
			tmp.setToRotation(Vector3.Y, angleY);
			model.mul(tmp);

			tmp.setToTranslation(0, 0, 0);
			model.mul(tmp);

			transShader.setUniformMatrix("MMatrix", model);

			transShader.setUniformf("a_color", Resources.getInstance().clearColor[0],Resources.getInstance().clearColor[1],Resources.getInstance().clearColor[2],Resources.getInstance().clearColor[3]);
			blockModel.render(transShader, GL20.GL_TRIANGLES);
			
			transShader.setUniformf("a_color", Resources.getInstance().wireCubeEdgeColor[0],Resources.getInstance().wireCubeEdgeColor[1],Resources.getInstance().wireCubeEdgeColor[2],Resources.getInstance().wireCubeEdgeColor[3]);
			wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
			
//			transShader.setUniformf("a_color", Resources.getInstance().wireCubeColor[0],Resources.getInstance().wireCubeColor[1],Resources.getInstance().wireCubeColor[2],Resources.getInstance().wireCubeColor[3]);
//			blockModel.render(transShader, GL20.GL_TRIANGLES);
		}
				
		// render all objects
		for (int i = 0; i<renderObjects.size;++i) {
			
			//render impact
			if(renderObjects.get(i).isCollidedAnimation == true && renderObjects.get(i).collideAnimation == 0) {
				renderObjects.get(i).collideAnimation = 1.0f;
			}
			if(renderObjects.get(i).collideAnimation>0.0f) {
				renderObjects.get(i).collideAnimation -= delta*1.f;
				renderObjects.get(i).collideAnimation = Math.max(0.0f, renderObjects.get(i).collideAnimation);
				if(renderObjects.get(i).collideAnimation == 0.0f) renderObjects.get(i).isCollidedAnimation = false;
			}
			
			//render switchblock fade out/in
			if( renderObjects.get(i) instanceof SwitchableBlock) {
				if(((SwitchableBlock) renderObjects.get(i)).isSwitchAnimation == true && ((SwitchableBlock) renderObjects.get(i)).isSwitched == true && ((SwitchableBlock) renderObjects.get(i)).switchAnimation == 0) {
					((SwitchableBlock) renderObjects.get(i)).switchAnimation = 0.0f;
				}
				if(((SwitchableBlock) renderObjects.get(i)).isSwitchAnimation == true && ((SwitchableBlock) renderObjects.get(i)).isSwitched == false && ((SwitchableBlock) renderObjects.get(i)).switchAnimation == 1) {
					((SwitchableBlock) renderObjects.get(i)).switchAnimation = 1.0f;
				}
				if (((SwitchableBlock) renderObjects.get(i)).isSwitchAnimation == true) {
					if (!((SwitchableBlock) renderObjects.get(i)).isSwitched) {
						((SwitchableBlock) renderObjects.get(i)).switchAnimation -= delta * 1.f;
						((SwitchableBlock) renderObjects.get(i)).switchAnimation = Math.max(0.0f, ((SwitchableBlock) renderObjects.get(i)).switchAnimation);
						if (((SwitchableBlock) renderObjects.get(i)).switchAnimation == 0.0f)
							((SwitchableBlock) renderObjects.get(i)).isSwitchAnimation = false;
					} else {
						((SwitchableBlock) renderObjects.get(i)).switchAnimation += delta * 1.f;
						((SwitchableBlock) renderObjects.get(i)).switchAnimation = Math.min(1.0f, ((SwitchableBlock) renderObjects.get(i)).switchAnimation);
						if (((SwitchableBlock) renderObjects.get(i)).switchAnimation == 1.0f)
							((SwitchableBlock) renderObjects.get(i)).isSwitchAnimation = false;
					}
				}

			}
			
			
			if(renderObjects.get(i) instanceof Block) {
				model.set(renderObjects.get(i).model);
	
				transShader.setUniformMatrix("MMatrix", model);
	
//				transShader.setUniformf("a_color", Resources.getInstance().blockColor[0]- (Helper.map(renderObjects.get(i).sortPosition,10,25,0,0.4f)), Resources.getInstance().blockColor[1], Resources.getInstance().blockColor[2] + (Helper.map(renderObjects.get(i).sortPosition,10,25,0,0.15f)), Resources.getInstance().blockColor[3]+ renderObjects.get(i).collideAnimation + (Helper.map(renderObjects.get(i).sortPosition,10,25,0.15f,-0.25f)));
//				blockModel.render(transShader, GL20.GL_TRIANGLES);
//				
//				transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0] - (Helper.map(renderObjects.get(i).sortPosition,10,25,0,0.4f)), Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2] + (Helper.map(renderObjects.get(i).sortPosition,10,25,0,0.15f)), Resources.getInstance().blockEdgeColor[3] + renderObjects.get(i).collideAnimation + (Helper.map(renderObjects.get(i).sortPosition,10,25,0.15f,-0.25f)));
//				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
				
				transShader.setUniformf("a_color", Resources.getInstance().blockColor[0], Resources.getInstance().blockColor[1], Resources.getInstance().blockColor[2], Resources.getInstance().blockColor[3]+ renderObjects.get(i).collideAnimation );
				blockModel.render(transShader, GL20.GL_TRIANGLES);
				
				transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0], Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2], Resources.getInstance().blockEdgeColor[3] + renderObjects.get(i).collideAnimation );
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
			}
			
			// render movableblocks
			if(renderObjects.get(i) instanceof MovableBlock) {
				model.set(renderObjects.get(i).model);
	
				transShader.setUniformMatrix("MMatrix", model);
	
				transShader.setUniformf("a_color", Resources.getInstance().movableBlockColor[0], Resources.getInstance().movableBlockColor[1], Resources.getInstance().movableBlockColor[2], Resources.getInstance().movableBlockColor[3] + renderObjects.get(i).collideAnimation);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", Resources.getInstance().movableBlockEdgeColor[0], Resources.getInstance().movableBlockEdgeColor[1],Resources.getInstance().movableBlockEdgeColor[2], Resources.getInstance().movableBlockEdgeColor[3] + renderObjects.get(i).collideAnimation);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
			}
			
			// render switchableblocks
			if(renderObjects.get(i) instanceof SwitchableBlock) {
				if(!((SwitchableBlock) renderObjects.get(i)).isSwitched || ((SwitchableBlock) renderObjects.get(i)).isSwitchAnimation == true) {	
					model.set(renderObjects.get(i).model);
		
					SwitchableBlock tmpSwitchb = (SwitchableBlock) renderObjects.get(i);
					
					switch (Math.abs(tmpSwitchb.id)) {
					case 10:

						tmp.setToScaling(0.3f, 0.3f, 0.3f);
						model.mul(tmp);
		
						transShader.setUniformMatrix("MMatrix", model);
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2] , Resources.getInstance().switchBlockColor[3]+ renderObjects.get(i).collideAnimation  - ((SwitchableBlock) renderObjects.get(i)).switchAnimation);
						playerModel.render(transShader, GL20.GL_TRIANGLES);
						
						tmp.setToScaling(3f, 3f, 3f);
						model.mul(tmp);
						
						transShader.setUniformMatrix("MMatrix", model);
						
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2], Resources.getInstance().switchBlockColor[3]+ renderObjects.get(i).collideAnimation  - ((SwitchableBlock) renderObjects.get(i)).switchAnimation);
						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
			
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockEdgeColor[0] * (Math.abs(((SwitchableBlock)renderObjects.get(i)).id)), Resources.getInstance().switchBlockEdgeColor[1]* (Math.abs(((SwitchableBlock)renderObjects.get(i)).id)), Resources.getInstance().switchBlockEdgeColor[2] * (Math.abs(((SwitchableBlock)renderObjects.get(i)).id)), Resources.getInstance().switchBlockEdgeColor[3] + renderObjects.get(i).collideAnimation - ((SwitchableBlock) renderObjects.get(i)).switchAnimation);
						blockModel.render(transShader, GL20.GL_TRIANGLES);
						break;
					case 12:

						tmp.setToScaling(0.3f, 0.3f, 0.3f);
						model.mul(tmp);
		
						transShader.setUniformMatrix("MMatrix", model);
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2] , Resources.getInstance().switchBlockColor[3]+ renderObjects.get(i).collideAnimation  - ((SwitchableBlock) renderObjects.get(i)).switchAnimation);
						blockModel.render(transShader, GL20.GL_TRIANGLES);
						
						tmp.setToScaling(3f, 3f, 3f);
						model.mul(tmp);
						
						transShader.setUniformMatrix("MMatrix", model);
						
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2], Resources.getInstance().switchBlockColor[3]+ renderObjects.get(i).collideAnimation  - ((SwitchableBlock) renderObjects.get(i)).switchAnimation);
						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
			
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockEdgeColor[0] * (Math.abs(((SwitchableBlock)renderObjects.get(i)).id)), Resources.getInstance().switchBlockEdgeColor[1]* (Math.abs(((SwitchableBlock)renderObjects.get(i)).id)), Resources.getInstance().switchBlockEdgeColor[2] * (Math.abs(((SwitchableBlock)renderObjects.get(i)).id)), Resources.getInstance().switchBlockEdgeColor[3] + renderObjects.get(i).collideAnimation - ((SwitchableBlock) renderObjects.get(i)).switchAnimation);
						blockModel.render(transShader, GL20.GL_TRIANGLES);
						break;
					case 13:

						tmp.setToScaling(0.3f, 0.3f, 0.3f);
						model.mul(tmp);
		
						transShader.setUniformMatrix("MMatrix", model);
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2] , Resources.getInstance().switchBlockColor[3]+ renderObjects.get(i).collideAnimation  - ((SwitchableBlock) renderObjects.get(i)).switchAnimation);
						coneModel.render(transShader, GL20.GL_TRIANGLES);
						
						tmp.setToScaling(3f, 3f, 3f);
						model.mul(tmp);
						
						transShader.setUniformMatrix("MMatrix", model);
						
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2], Resources.getInstance().switchBlockColor[3]+ renderObjects.get(i).collideAnimation  - ((SwitchableBlock) renderObjects.get(i)).switchAnimation);
						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
			
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockEdgeColor[0] * (Math.abs(((SwitchableBlock)renderObjects.get(i)).id)), Resources.getInstance().switchBlockEdgeColor[1]* (Math.abs(((SwitchableBlock)renderObjects.get(i)).id)), Resources.getInstance().switchBlockEdgeColor[2] * (Math.abs(((SwitchableBlock)renderObjects.get(i)).id)), Resources.getInstance().switchBlockEdgeColor[3] + renderObjects.get(i).collideAnimation - ((SwitchableBlock) renderObjects.get(i)).switchAnimation);
						blockModel.render(transShader, GL20.GL_TRIANGLES);
						break;
					default:

						tmp.setToScaling(0.3f, 0.3f, 0.3f);
						model.mul(tmp);
		
						transShader.setUniformMatrix("MMatrix", model);
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2] , Resources.getInstance().switchBlockColor[3]+ renderObjects.get(i).collideAnimation  - ((SwitchableBlock) renderObjects.get(i)).switchAnimation);
						playerModel.render(transShader, GL20.GL_TRIANGLES);
						
						tmp.setToScaling(3f, 3f, 3f);
						model.mul(tmp);
						
						transShader.setUniformMatrix("MMatrix", model);
						
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2], Resources.getInstance().switchBlockColor[3]+ renderObjects.get(i).collideAnimation  - ((SwitchableBlock) renderObjects.get(i)).switchAnimation);
						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
			
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockEdgeColor[0] * (Math.abs(((SwitchableBlock)renderObjects.get(i)).id)), Resources.getInstance().switchBlockEdgeColor[1]* (Math.abs(((SwitchableBlock)renderObjects.get(i)).id)), Resources.getInstance().switchBlockEdgeColor[2] * (Math.abs(((SwitchableBlock)renderObjects.get(i)).id)), Resources.getInstance().switchBlockEdgeColor[3] + renderObjects.get(i).collideAnimation - ((SwitchableBlock) renderObjects.get(i)).switchAnimation);
						blockModel.render(transShader, GL20.GL_TRIANGLES);
						break;
					}
				}
			}
			
			// render switches
			if(renderObjects.get(i) instanceof Switch) {
				model.set(renderObjects.get(i).model);	
				
				Switch tmpSwitch = (Switch) renderObjects.get(i);
				
				switch (Math.abs(tmpSwitch.id)) {
				case 10:

					tmp.setToScaling(0.3f, 0.3f, 0.3f);
					model.mul(tmp);
	
					transShader.setUniformMatrix("MMatrix", model);
					transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2] , Resources.getInstance().switchBlockColor[3]+ renderObjects.get(i).collideAnimation);
					playerModel.render(transShader, GL20.GL_TRIANGLES);
					
					tmp.setToScaling(2.0f, 2.0f, 2.0f);
					model.mul(tmp);
	
					//render hull			
					transShader.setUniformMatrix("MMatrix", model);
					transShader.setUniformf("a_color", Resources.getInstance().switchBlockEdgeColor[0],Resources.getInstance().switchBlockEdgeColor[1], Resources.getInstance().switchBlockEdgeColor[2] , Resources.getInstance().switchBlockEdgeColor[3]+ renderObjects.get(i).collideAnimation);
					playerModel.render(transShader, GL20.GL_TRIANGLES);
					break;
				case 12:

					tmp.setToScaling(0.3f, 0.3f, 0.3f);
					model.mul(tmp);
	
					transShader.setUniformMatrix("MMatrix", model);
					transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0] ,Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2] , Resources.getInstance().switchBlockColor[3]+ renderObjects.get(i).collideAnimation);
					blockModel.render(transShader, GL20.GL_TRIANGLES);
					
					tmp.setToScaling(2.0f, 2.0f, 2.0f);
					model.mul(tmp);
	
					//render hull			
					transShader.setUniformMatrix("MMatrix", model);
					transShader.setUniformf("a_color", Resources.getInstance().switchBlockEdgeColor[0],Resources.getInstance().switchBlockEdgeColor[1], Resources.getInstance().switchBlockEdgeColor[2] , Resources.getInstance().switchBlockEdgeColor[3]+ renderObjects.get(i).collideAnimation);
					playerModel.render(transShader, GL20.GL_TRIANGLES);
					break;
				case 13:

					tmp.setToScaling(0.3f, 0.3f, 0.3f);
					model.mul(tmp);
	
					transShader.setUniformMatrix("MMatrix", model);
					transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2] , Resources.getInstance().switchBlockColor[3]+ renderObjects.get(i).collideAnimation);
					coneModel.render(transShader, GL20.GL_TRIANGLES);
					
					tmp.setToScaling(2.0f, 2.0f, 2.0f);
					model.mul(tmp);
	
					//render hull			
					transShader.setUniformMatrix("MMatrix", model);
					transShader.setUniformf("a_color", Resources.getInstance().switchBlockEdgeColor[0],Resources.getInstance().switchBlockEdgeColor[1], Resources.getInstance().switchBlockEdgeColor[2] , Resources.getInstance().switchBlockEdgeColor[3]+ renderObjects.get(i).collideAnimation);
					playerModel.render(transShader, GL20.GL_TRIANGLES);
					break;
				default:

					tmp.setToScaling(0.3f, 0.3f, 0.3f);
					model.mul(tmp);
		
					transShader.setUniformMatrix("MMatrix", model);
					transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2], Resources.getInstance().switchBlockColor[3]+ renderObjects.get(i).collideAnimation);
					playerModel.render(transShader, GL20.GL_TRIANGLES);
						
					tmp.setToScaling(2.0f, 2.0f, 2.0f);
					model.mul(tmp);
		
					//render hull			
					transShader.setUniformMatrix("MMatrix", model);
					transShader.setUniformf("a_color", Resources.getInstance().switchBlockEdgeColor[0],Resources.getInstance().switchBlockEdgeColor[1], Resources.getInstance().switchBlockEdgeColor[2] , Resources.getInstance().switchBlockEdgeColor[3]+ renderObjects.get(i).collideAnimation);
					playerModel.render(transShader, GL20.GL_TRIANGLES);
					break;
				}
				
			}
			
			// render Player
			if(renderObjects.get(i) instanceof Player) {
				model.set(renderObjects.get(i).model);	
				
				tmp.setToRotation(Vector3.X, angleXBack);
				model.mul(tmp);
				tmp.setToRotation(Vector3.Y, angleYBack);
				model.mul(tmp);

				tmp.setToScaling(0.5f, 0.5f, 0.5f);
				model.mul(tmp);

				transShader.setUniformMatrix("MMatrix", model);
				transShader.setUniformf("a_color",Resources.getInstance().playerColor[0], Resources.getInstance().playerColor[1], Resources.getInstance().playerColor[2], Resources.getInstance().playerColor[3]  + renderObjects.get(i).collideAnimation);
				playerModel.render(transShader, GL20.GL_TRIANGLES);
				
				tmp.setToScaling(2.0f - (renderObjects.get(i).collideAnimation), 2.0f - (renderObjects.get(i).collideAnimation), 2.0f  - (renderObjects.get(i).collideAnimation));
				model.mul(tmp);

				//render hull			
				transShader.setUniformMatrix("MMatrix", model);
				transShader.setUniformf("a_color",Resources.getInstance().playerEdgeColor[0], Resources.getInstance().playerEdgeColor[1], Resources.getInstance().playerEdgeColor[2], Resources.getInstance().playerEdgeColor[3]  + renderObjects.get(i).collideAnimation);
				
				playerModel.render(transShader, GL20.GL_LINE_STRIP);
				
				//TODO add animations
				playerModel.render(transShader, GL20.GL_LINE_STRIP, 0, (int) (playerModel.getNumVertices()-(renderObjects.get(i).collideAnimation*playerModel.getNumVertices())));
				
//				//render direction indicator
//				model.set(renderObjects.get(i).model);
//				((Player) renderObjects.get(i)).setDirection();
//				tmp.setToTranslation(((Player) renderObjects.get(i)).direction);
//				model.mul(tmp);
//				transShader.setUniformMatrix("MMatrix", model);
//				transShader.setUniformf("a_color",Resources.getInstance().playerEdgeColor[0], Resources.getInstance().playerEdgeColor[1], Resources.getInstance().playerEdgeColor[2], Resources.getInstance().playerEdgeColor[3]  + renderObjects.get(i).collideAnimation);
//				sphereSliceModel.render(transShader, GL20.GL_LINE_STRIP);
			}
			
			// render player shadow
			if(renderObjects.get(i) instanceof PlayerShadow && !player.isMoving && player.collideAnimation==0 && playerShadow.isMoving) {
				model.set(renderObjects.get(i).model);				
				
				tmp.setToRotation(Vector3.X, angleXBack);
				model.mul(tmp);
				tmp.setToRotation(Vector3.Y, angleYBack);
				model.mul(tmp);

				tmp.setToScaling(0.5f, 0.5f, 0.5f);
				model.mul(tmp);

				transShader.setUniformMatrix("MMatrix", model);
				transShader.setUniformf("a_color",Resources.getInstance().playerColor[0], Resources.getInstance().playerColor[1], Resources.getInstance().playerColor[2], Resources.getInstance().playerColor[3] - (playerShadow.distance/4.f));
				playerModel.render(transShader, GL20.GL_TRIANGLES);
				
				tmp.setToScaling(2.0f - (player.collideAnimation), 2.0f - (player.collideAnimation), 2.0f  - (player.collideAnimation));
				model.mul(tmp);

				//render hull			
				transShader.setUniformMatrix("MMatrix", model);
				transShader.setUniformf("a_color",Resources.getInstance().playerEdgeColor[0], Resources.getInstance().playerEdgeColor[1], Resources.getInstance().playerEdgeColor[2], Resources.getInstance().playerEdgeColor[3]  - (playerShadow.distance/4.f));
				
				playerModel.render(transShader, GL20.GL_LINE_STRIP);
				
				//TODO add animations
				playerModel.render(transShader, GL20.GL_LINE_STRIP, 0, (int) (playerModel.getNumVertices()-(renderObjects.get(i).collideAnimation*playerModel.getNumVertices())));
			}
			
			// render Portals
			if(renderObjects.get(i) instanceof Portal) {
				if(renderObjects.get(i).position.x != -11) {
					// render Portal
					Portal tmpPortal = (Portal) renderObjects.get(i);
					
					switch (Math.abs(tmpPortal.id)) {
					case 4:
						model.set(renderObjects.get(i).model);

						tmp.setToScaling(0.4f, 0.4f, 0.4f);
						model.mul(tmp);	
						
						transShader.setUniformMatrix("MMatrix", model);
						
						transShader.setUniformf("a_color", Resources.getInstance().portalColor[0], Resources.getInstance().portalColor[1], Resources.getInstance().portalColor[2], Resources.getInstance().portalColor[3]  + renderObjects.get(i).collideAnimation);
						playerModel.render(transShader, GL20.GL_TRIANGLES);
						
						model.set(renderObjects.get(i).model);
//						//render hull		
//						transShader.setUniformMatrix("MMatrix", model);
//						transShader.setUniformf("a_color", Resources.getInstance().portalEdgeColor[0],Resources.getInstance().portalEdgeColor[1] , Resources.getInstance().portalEdgeColor[2], Resources.getInstance().portalEdgeColor[3]  + renderObjects.get(i).collideAnimation);
//						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						
						tmp.setToRotation(Vector3.X, angleXFront);
						model.mul(tmp);
						tmp.setToRotation(Vector3.Y, angleYFront);
						model.mul(tmp);
						
						tmp.setToScaling(0.8f, 0.8f, 0.8f);
						model.mul(tmp);
						transShader.setUniformMatrix("MMatrix", model);
						transShader.setUniformf("a_color",Resources.getInstance().portalColor[0], Resources.getInstance().portalColor[1], Resources.getInstance().portalColor[2], Resources.getInstance().portalColor[3]  + renderObjects.get(i).collideAnimation);
						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						break;
						
					case 5:
						model.set(renderObjects.get(i).model);

						tmp.setToScaling(0.4f, 0.4f, 0.4f);
						model.mul(tmp);	
						
						transShader.setUniformMatrix("MMatrix", model);
						
						transShader.setUniformf("a_color", Resources.getInstance().portalColor2[0], Resources.getInstance().portalColor2[1], Resources.getInstance().portalColor2[2], Resources.getInstance().portalColor2[3]  + renderObjects.get(i).collideAnimation);
						playerModel.render(transShader, GL20.GL_TRIANGLES);
						
						model.set(renderObjects.get(i).model);
						//render hull		
//						transShader.setUniformMatrix("MMatrix", model);
//						transShader.setUniformf("a_color", Resources.getInstance().portalEdgeColor[0],Resources.getInstance().portalEdgeColor[1] , Resources.getInstance().portalEdgeColor[2], Resources.getInstance().portalEdgeColor[3]  + renderObjects.get(i).collideAnimation);
//						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						
						tmp.setToRotation(Vector3.X, angleXFront);
						model.mul(tmp);
						tmp.setToRotation(Vector3.Y, angleYFront);
						model.mul(tmp);
						
						tmp.setToScaling(0.8f, 0.8f, 0.8f);
						model.mul(tmp);
						transShader.setUniformMatrix("MMatrix", model);
						transShader.setUniformf("a_color",Resources.getInstance().portalColor2[0], Resources.getInstance().portalColor2[1], Resources.getInstance().portalColor2[2], Resources.getInstance().portalColor2[3]  + renderObjects.get(i).collideAnimation);
						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						break;
						
					case 6:
						model.set(renderObjects.get(i).model);

						tmp.setToScaling(0.4f, 0.4f, 0.4f);
						model.mul(tmp);	
						
						transShader.setUniformMatrix("MMatrix", model);
						
						transShader.setUniformf("a_color", Resources.getInstance().portalColor3[0], Resources.getInstance().portalColor3[1], Resources.getInstance().portalColor3[2], Resources.getInstance().portalColor3[3]  + renderObjects.get(i).collideAnimation);
						playerModel.render(transShader, GL20.GL_TRIANGLES);
						
						model.set(renderObjects.get(i).model);
						//render hull		
//						transShader.setUniformMatrix("MMatrix", model);
//						transShader.setUniformf("a_color", Resources.getInstance().portalEdgeColor[0],Resources.getInstance().portalEdgeColor[1] , Resources.getInstance().portalEdgeColor[2], Resources.getInstance().portalEdgeColor[3]  + renderObjects.get(i).collideAnimation);
//						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						
						tmp.setToRotation(Vector3.X, angleXFront);
						model.mul(tmp);
						tmp.setToRotation(Vector3.Y, angleYFront);
						model.mul(tmp);
						
						tmp.setToScaling(0.8f, 0.8f, 0.8f);
						model.mul(tmp);
						transShader.setUniformMatrix("MMatrix", model);
						transShader.setUniformf("a_color",Resources.getInstance().portalColor3[0], Resources.getInstance().portalColor3[1], Resources.getInstance().portalColor3[2], Resources.getInstance().portalColor3[3]  + renderObjects.get(i).collideAnimation);
						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						break;
						
					case 7:
						model.set(renderObjects.get(i).model);

						tmp.setToScaling(0.4f, 0.4f, 0.4f);
						model.mul(tmp);	
						
						transShader.setUniformMatrix("MMatrix", model);
						
						transShader.setUniformf("a_color", Resources.getInstance().portalColor4[0], Resources.getInstance().portalColor4[1], Resources.getInstance().portalColor4[2], Resources.getInstance().portalColor4[3]  + renderObjects.get(i).collideAnimation);
						playerModel.render(transShader, GL20.GL_TRIANGLES);
						
						model.set(renderObjects.get(i).model);
						//render hull		
//						transShader.setUniformMatrix("MMatrix", model);
//						transShader.setUniformf("a_color", Resources.getInstance().portalEdgeColor[0],Resources.getInstance().portalEdgeColor[1] , Resources.getInstance().portalEdgeColor[2], Resources.getInstance().portalEdgeColor[3]  + renderObjects.get(i).collideAnimation);
//						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						
						tmp.setToRotation(Vector3.X, angleXFront);
						model.mul(tmp);
						tmp.setToRotation(Vector3.Y, angleYFront);
						model.mul(tmp);
						
						tmp.setToScaling(0.8f, 0.8f, 0.8f);
						model.mul(tmp);
						transShader.setUniformMatrix("MMatrix", model);
						transShader.setUniformf("a_color",Resources.getInstance().portalColor4[0], Resources.getInstance().portalColor4[1], Resources.getInstance().portalColor4[2], Resources.getInstance().portalColor4[3]  + renderObjects.get(i).collideAnimation);
						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						break;
						
					case 8:
						model.set(renderObjects.get(i).model);

						tmp.setToScaling(0.4f, 0.4f, 0.4f);
						model.mul(tmp);	
						
						transShader.setUniformMatrix("MMatrix", model);
						
						transShader.setUniformf("a_color", Resources.getInstance().portalColor5[0], Resources.getInstance().portalColor5[1], Resources.getInstance().portalColor5[2], Resources.getInstance().portalColor5[3]  + renderObjects.get(i).collideAnimation);
						playerModel.render(transShader, GL20.GL_TRIANGLES);
						
						model.set(renderObjects.get(i).model);
						//render hull		
//						transShader.setUniformMatrix("MMatrix", model);
//						transShader.setUniformf("a_color", Resources.getInstance().portalEdgeColor[0],Resources.getInstance().portalEdgeColor[1] , Resources.getInstance().portalEdgeColor[2], Resources.getInstance().portalEdgeColor[3]  + renderObjects.get(i).collideAnimation);
//						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						
						tmp.setToRotation(Vector3.X, angleXFront);
						model.mul(tmp);
						tmp.setToRotation(Vector3.Y, angleYFront);
						model.mul(tmp);
						
						tmp.setToScaling(0.8f, 0.8f, 0.8f);
						model.mul(tmp);
						transShader.setUniformMatrix("MMatrix", model);
						transShader.setUniformf("a_color",Resources.getInstance().portalColor5[0], Resources.getInstance().portalColor5[1], Resources.getInstance().portalColor5[2], Resources.getInstance().portalColor5[3]  + renderObjects.get(i).collideAnimation);
						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						break;

					default:
						model.set(renderObjects.get(i).model);

						tmp.setToScaling(0.4f, 0.4f, 0.4f);
						model.mul(tmp);	
						
						transShader.setUniformMatrix("MMatrix", model);
						
						transShader.setUniformf("a_color", Resources.getInstance().portalColor[0], Resources.getInstance().portalColor[1], Resources.getInstance().portalColor[2], Resources.getInstance().portalColor[3]  + renderObjects.get(i).collideAnimation);
						playerModel.render(transShader, GL20.GL_TRIANGLES);
						
						model.set(renderObjects.get(i).model);
						//render hull		
//						transShader.setUniformMatrix("MMatrix", model);
//						transShader.setUniformf("a_color", Resources.getInstance().portalEdgeColor[0],Resources.getInstance().portalEdgeColor[1] , Resources.getInstance().portalEdgeColor[2], Resources.getInstance().portalEdgeColor[3]  + renderObjects.get(i).collideAnimation);
//						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						
						tmp.setToRotation(Vector3.X, angleXFront);
						model.mul(tmp);
						tmp.setToRotation(Vector3.Y, angleYFront);
						model.mul(tmp);
						
						tmp.setToScaling(0.8f, 0.8f, 0.8f);
						model.mul(tmp);
						transShader.setUniformMatrix("MMatrix", model);
						transShader.setUniformf("a_color",Resources.getInstance().portalColor[0], Resources.getInstance().portalColor[1], Resources.getInstance().portalColor[2], Resources.getInstance().portalColor[3]  + renderObjects.get(i).collideAnimation);
						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						break;
					}
				}
			}
				
			// render Target
			if(renderObjects.get(i) instanceof Target) {
				model.set(renderObjects.get(i).model);
				
				tmp.setToRotation(Vector3.Y, angleY + angleYBack);
				model.mul(tmp);

				transShader.setUniformMatrix("MMatrix", model);

				transShader.setUniformf("a_color", Resources.getInstance().targetColor[0],  Resources.getInstance().targetColor[1],  Resources.getInstance().targetColor[2], Resources.getInstance().targetColor[3] + renderObjects.get(i).collideAnimation);
				targetModel.render(transShader, GL20.GL_TRIANGLES);
				
				//render hull			
				transShader.setUniformf("a_color",  Resources.getInstance().targetEdgeColor[0], Resources.getInstance().targetEdgeColor[1], Resources.getInstance().targetEdgeColor[2], Resources.getInstance().targetEdgeColor[3] + renderObjects.get(i).collideAnimation);
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
					updatePlayerShadow();
					block.isCollidedAnimation = true;
					break;
				}
			}
			
			for (MovableBlock m : movableBlocks) {
				boolean intersect = Intersector.intersectRaySphere(pRay, m.position, 1f, intersection);
				float movdst = intersection.dst(player.position);
				if (movdst < 1.0f && box.contains(m.position) && intersect) {
					player.stop();
					updatePlayerShadow();
					m.move(player.direction.cpy());
					m.isCollidedAnimation = true;
				}
				else if(movdst <1.0f && !box.contains(m.position) && intersect) {
					player.stop();
					updatePlayerShadow();
					m.isCollidedAnimation = true;
				}
				
				//recursiveCollisionCheck(m);
			}
			
			for (SwitchableBlock s : switchblocks) {
				boolean swintersect = Intersector.intersectRaySphere(pRay, s.position, 1f, intersection);
				float swdst = intersection.dst(player.position);
				if (swdst < 1.0f && swintersect && !s.isSwitched) {
					player.stop();
					updatePlayerShadow();
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
						port = portal;
						portal.isCollidedAnimation = true;
						player.isCollidedAnimation = true;
						break;
					}
				}
			} else {
				//end warplock
					boolean portalintersect = Intersector.intersectRaySphere(pRay, port.correspondingPortal.position, 1f, portalIntersection);
					if (!portalintersect) {
						warplock = false;
					}
				}

			// player out of bound?
			if (!box.contains(player.position)) {
				player.stop();
				updatePlayerShadow();
				reset();
			}

			if (win) {
				HighScoreManager.getInstance().newHighScore( (int) Resources.getInstance().time,Resources.getInstance().currentlevel);
				player.stop();

				Resources.getInstance().time = 0;
				Resources.getInstance().timeAttackTime += 45;
				if(Resources.getInstance().currentlevel<Resources.getInstance().levelcount) {
					changeLevel = true;
				} else {
					//game completed
					if(mode==1) {
						HighScoreManager.getInstance().newTimeAttackHighScore((int) Resources.getInstance().timeAttackTime, Resources.getInstance().levelcount);
					}
				}
			}

			if (warp) {
				player.position = port.correspondingPortal.position.cpy();
				warplock = true;
				port.correspondingPortal.isCollidedAnimation = true;
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
							port = portal;
							portal.isCollidedAnimation = true;
							m.isCollidedAnimation = true;
							break;
						}
					}
				} else {
					//end warplock
					boolean portalintersect = Intersector.intersectRaySphere(mRay, port.correspondingPortal.position, 1f, portalIntersection);
					if (!portalintersect) {
						movwarplock = false;
					}
				}
				
				if (warp) {
						m.position = port.correspondingPortal.position.cpy();
						movwarplock = true;
						port.correspondingPortal.isCollidedAnimation = true;
				}
				
				for(MovableBlock mm : movableBlocks) {
					if(m.id!=mm.id) {
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
			game.setScreen(new MainMenuScreen(game));
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
		
		if (keycode == Input.Keys.UP) {
			Resources.getInstance().colorTheme++;
			Resources.getInstance().switchColorTheme();
		}
		
		if (keycode == Input.Keys.DOWN) {
			Resources.getInstance().colorTheme--;
			Resources.getInstance().switchColorTheme();
		}
		
		if(keycode == Input.Keys.BACK) {
			game.setScreen(new MainMenuScreen(game));
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

	private void movePlayer() {
		if (!player.isMoving) {
			player.direction.set(0, 0, -1);
			player.direction.rot(new Matrix4().setToRotation(Vector3.X, -angleX));
			player.direction.rot(new Matrix4().setToRotation(Vector3.Y, -angleY));
			player.move();
			playerShadow.isMoving = false;
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
		
	     if(pointers.size() == 0) {
	         // no fingers down so assign v1
	         finger_one_pointer = pointer;
	         v1 = new Vector2(x,y);
	         pointers.put(pointer, v1);
	      } else if (pointers.size() == 1) {
	         // figure out which finger is down
	         if (finger_one_pointer == -1) {
	            //finger two is still down
	            finger_one_pointer = pointer;
	            v1 = new Vector2(x,y);
	            pointers.put(pointer,v1);
	            initialDistance = v1.dst(pointers.get(finger_two_pointer));
	       
	         } else {
	            //finger one is still down
	            finger_two_pointer = pointer;
	            v2 = new Vector2(x,y);
	            pointers.put(pointer, v2);
	            initialDistance = v2.dst(pointers.get(finger_one_pointer));
	         }
	      }

		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		x = (int) (x / (float) Gdx.graphics.getWidth() * 800);
		y = (int) (y / (float) Gdx.graphics.getHeight() * 480);

		if (pointers.size() > 1) {
			if (pointer == finger_one_pointer) {
				finger_one_pointer = -1;
			} else if (pointer == finger_two_pointer) {
				finger_two_pointer = -1;
			}
			
		} else {
			if (Math.abs(touchDistance) < 1.0f && touchTime < 0.3f && startTime > 0.5) {
				movePlayer();
			}
			
		}
		pointers.remove(pointer);
		
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		x = (int) (x / (float) Gdx.graphics.getWidth() * 800);
		y = (int) (y / (float) Gdx.graphics.getHeight() * 480);
		
		
		if(pointers.size() == 2) {
         // two finger pinch (zoom)
         // now fingers are being dragged so measure the distance and apply zoom
         if (pointer == finger_one_pointer) {
            v1 = new Vector2(x,y);
            v2 = pointers.get(finger_two_pointer);
            pointers.put(pointer,v1);
         } else if (pointer == finger_one_pointer) {
            v2 = new Vector2(x,y);
            v1 = pointers.get(finger_one_pointer);
            pointers.put(pointer,v2);
         }
         distance = v2.dst(v1);
         cam.position.z = ((int) Helper.map((initialDistance - distance),-200,200,2,20));
         if(cam.position.z < 2) {
        	 cam.position.z = 2;
         } else if (cam.position.z > 20) {
        	 cam.position.z = 20;
         }
		} else {

		angleY += ((x - touchStartX) / 5.f);
		angleX += ((y - touchStartY) / 5.f);

		//update Player Shadow
		updatePlayerShadow();

		
		touchDistance += ((x - touchStartX) / 5.f) + ((y - touchStartY) / 5.f);

		touchStartX = x;
		touchStartY = y;
		}

		return false;
	}

	private void updatePlayerShadow() {
		if(!player.isMoving) {
			player.direction.set(0, 0, -1);
			player.direction.rot(new Matrix4().setToRotation(Vector3.X, -angleX));
			player.direction.rot(new Matrix4().setToRotation(Vector3.Y, -angleY));
			player.setDirection();
			if(!player.direction.equals(playerShadow.direction)) {
				playerShadow.position.set(player.position);
				playerShadow.direction.set(0, 0, -1);
				playerShadow.direction.rot(new Matrix4().setToRotation(Vector3.X, -angleX));
				playerShadow.direction.rot(new Matrix4().setToRotation(Vector3.Y, -angleY));
				playerShadow.setDirection();
				playerShadow.isMoving = true;
				playerShadow.position.add(playerShadow.direction.x, playerShadow.direction.y, playerShadow.direction.z);
				for(Renderable renderable:renderObjects) {				
					if(!(renderable instanceof Player) && !(renderable instanceof PlayerShadow) && renderable.position.dst(playerShadow.position)<2.0f) {
						playerShadow.isMoving = false;
						break;
					}
				}
				playerShadow.position.set(player.position);
			}
		}
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
	
	public void setCorrespondingSwitchBlocks(Switch s) {
		for(SwitchableBlock sw : s.sBlocks) {
			if(s.isSwitched != sw.isSwitched) {
				sw.isSwitchAnimation = true;
			}
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
