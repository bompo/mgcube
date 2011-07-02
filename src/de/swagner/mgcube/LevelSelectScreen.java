package de.swagner.mgcube;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.sun.opengl.impl.packrect.Level;

import de.swagner.mgcube.Block;
import de.swagner.mgcube.DefaultScreen;
import de.swagner.mgcube.Helper;
import de.swagner.mgcube.LevelButton;
import de.swagner.mgcube.MovableBlock;
import de.swagner.mgcube.Player;
import de.swagner.mgcube.Portal;
import de.swagner.mgcube.Renderable;
import de.swagner.mgcube.Resources;
import de.swagner.mgcube.Switch;
import de.swagner.mgcube.SwitchableBlock;
import de.swagner.mgcube.Target;


public class LevelSelectScreen extends DefaultScreen implements InputProcessor{

	PerspectiveCamera cam;
	OrthographicCamera camMenu;
	Mesh quadModel;
	Mesh blockModel;
	Mesh playerModel;
	Mesh targetModel;
	Mesh worldModel;
	Mesh wireCubeModel;
	Mesh sphereModel;
	float angleX = 0;
	float angleY = 0;
	SpriteBatch batch;
	SpriteBatch bat;
	BitmapFont font;
	BitmapFont selectedFont;
	SpriteBatch fadeBatch;
	Sprite blackFade;
	Sprite title;
	float fade = 1.0f;
	boolean finished = false;
	
	int next = 0;
	
	// GLES20
	Matrix4 model = new Matrix4().idt();
	Matrix4 tmp = new Matrix4().idt();
	private ShaderProgram transShader;
	private ShaderProgram bloomShader;
	FrameBuffer frameBuffer;
	FrameBuffer frameBufferVert;
	
	float delta = 0;
	float angleXBack = 0;
	float angleYBack = 0;
	float startTime =0;
	
	Vector3 xAxis = new Vector3(1, 0, 0);
	Vector3 yAxis = new Vector3(0, 1, 0);
	Vector3 zAxis = new Vector3(0, 0, 1);
	
	Array<LevelButton> buttons = new Array<LevelButton>();
	
	BoundingBox collisionLevelBack = new BoundingBox();
	BoundingBox collisionLevelForward = new BoundingBox();
	BoundingBox collisionLevelStart = new BoundingBox();
	
	Player player = new Player();
	Target target = new Target();
	
	Array<Block> blocks = new Array<Block>();
	Array<Portal> portals = new Array<Portal>();
	Array<MovableBlock> movableBlocks = new Array<MovableBlock>();
	Array<Renderable> renderObjects = new Array<Renderable>();
	Array<Switch> switches = new Array<Switch>();
	Array<SwitchableBlock> switchblocks = new Array<SwitchableBlock>();

	Vector3 position = new Vector3();
	
	HighScore currentHighScore;
	
	public LevelSelectScreen(Game game) {
		super(game);
		Gdx.input.setCatchBackKey( true );
		Gdx.input.setInputProcessor(this);
		
		blackFade = new Sprite(new Texture(Gdx.files.internal("data/blackfade.png")));

		blockModel = Resources.getInstance().blockModel;
		playerModel = Resources.getInstance().playerModel;
		targetModel = Resources.getInstance().targetModel;
		quadModel = Resources.getInstance().quadModel;
		wireCubeModel = Resources.getInstance().wireCubeModel;
		sphereModel = Resources.getInstance().sphereModel;
		
		cam = new PerspectiveCamera(40, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(12.0f, -3.5f, 35f);
		cam.direction.set(0, 0, -1);
		cam.up.set(0, 1, 0);
		cam.near = 1f;
		cam.far = 1000;
		
		camMenu = new OrthographicCamera(800,480);
		
		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		font = Resources.getInstance().font;
		font.setScale(1);
		font.scale(0.5f);
		selectedFont = Resources.getInstance().selectedFont;
		selectedFont.setScale(1);
		selectedFont.scale(0.5f);
		
		fadeBatch = new SpriteBatch();
		fadeBatch.getProjectionMatrix().setToOrtho2D(0, 0, 2, 2);

		transShader = Resources.getInstance().transShader;
		bloomShader = Resources.getInstance().bloomShader;
		
		int distX = 100;
		int distY = 100;
		buttons.clear();
		int y = 0;
		int x = 0;

		for (int i = 0; i < Resources.getInstance().levelcount; i++) {
			LevelButton temp = new LevelButton(i+1);
			buttons.add(temp);
			temp.box = new BoundingBox(new Vector3(350+ (distX * x), 350 - (distY * y) ,0), new Vector3(450 + (distX * x),450  - (distY * y) ,0));
			++x;
			if(x%4 == 0) {
				++y;
				x=0;
				if(y%3 == 0)
					y=0;
			}			
		}
		
		collisionLevelBack.set(new Vector3(370 , 25, 0),new Vector3(430, 85, 0));
		collisionLevelForward.set(new Vector3(470 , 25, 0),new Vector3(530, 85, 0));
		collisionLevelStart.set(new Vector3(570 , 25, 0),new Vector3(730, 85, 0));
		
		initRender();
		initLevel(1);
		angleY = -60;
		angleX = 0;
	}
	
	public void initRender() {
		Gdx.graphics.getGL20().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		// //antiAliasing for Desktop - no support in Android
		// Gdx.graphics.getGL20().glEnable (GL10.GL_LINE_SMOOTH);
		// Gdx.graphics.getGL20().glEnable (GL10.GL_BLEND);
		// Gdx.graphics.getGL20().glBlendFunc (GL10.GL_SRC_ALPHA,GL10.
		// GL_ONE_MINUS_SRC_ALPHA);
		// Gdx.graphics.getGL20().glHint (GL10.GL_LINE_SMOOTH_HINT,
		// GL10.GL_FASTEST);
		// Gdx.graphics.getGL20().glLineWidth (1.5f);

		frameBuffer = new FrameBuffer(Format.RGB565, Resources.getInstance().m_i32TexSize, Resources.getInstance().m_i32TexSize, false);
		frameBufferVert = new FrameBuffer(Format.RGB565, Resources.getInstance().m_i32TexSize, Resources.getInstance().m_i32TexSize, false);
	}
	
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
		int[][][] level = Resources.getInstance().locked;
//		try {
			if(levelnumber ==1 || HighScoreManager.getInstance().getHighScore(levelnumber-1).first != 0)
				level = Resources.getInstance().levels[levelnumber-1];
//		} catch(ArrayIndexOutOfBoundsException e) {
			
//		}
		
		Resources.getInstance().currentlevel = levelnumber;

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
		
		currentHighScore = HighScoreManager.getInstance().getHighScore( Resources.getInstance().currentlevel);
	}

	@Override
	public void render(float deltaTime) {
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		delta = Math.min(0.02f, deltaTime);
		
		startTime += delta;

		angleXBack += MathUtils.sin(startTime) / 10f;
		angleYBack += MathUtils.cos(startTime) / 5f;

		angleX += MathUtils.sin(startTime) / 10f;
		angleY += MathUtils.cos(startTime) / 5f;
		
		cam.update();		
		
		sortScene();

		if(Resources.getInstance().bloomOnOff) {
		frameBuffer.begin();
		renderScene();
		renderLevelSelect();
		frameBuffer.end();

		// PostProcessing
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDisable(GL20.GL_BLEND);

		frameBuffer.getColorBufferTexture().bind(0);

		bloomShader.begin();
		bloomShader.setUniformi("sTexture", 0);
		bloomShader.setUniformf("bloomFactor", Helper.map((MathUtils.sin(startTime * 3f) * 0.5f) + 0.5f,0,1,0.50f,0.70f));

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
		}

		// render scene again
		renderScene();
		renderLevelSelect();

		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDisable(GL20.GL_BLEND);

		if(Resources.getInstance().bloomOnOff) {
		batch.enableBlending();
		batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
		batch.begin();
		batch.draw(frameBuffer.getColorBufferTexture(), 0, 0, 800, 480, 0, 0, frameBuffer.getWidth(), frameBuffer.getHeight(), false, true);
		batch.end();
		}

		batch.begin();
		// render highscore
		selectedFont.draw(batch, "Highscore", 50, 150);
		if (currentHighScore.first == 0) {
			font.draw(batch, "1. -", 50, 110);
		} else {
			font.draw(batch, "1. " + currentHighScore.first, 50, 110);
		}
		if (currentHighScore.second == 0) {
			font.draw(batch, "2. -", 50, 80);
		} else {
			font.draw(batch, "2. " + currentHighScore.second, 50, 80);
		}
		if (currentHighScore.third == 0) {
			font.draw(batch, "3. -", 50, 50);
		} else {
			font.draw(batch, "3. " + currentHighScore.third, 50, 50);
		}

		//render level description
		for(LevelButton button : buttons) {
			if(button.levelnumber <=12 && next==0) {
				if(button.levelnumber == Resources.getInstance().currentlevel)
					selectedFont.draw(batch, button.levelnumber + "", button.box.getCenter().x-22, button.box.getCenter().y);
				else
					font.draw(batch, button.levelnumber + "", button.box.getCenter().x-22, button.box.getCenter().y);
			}
			else if(button.levelnumber > 12*next && next > 0 && button.levelnumber <= 12*(next+1)){
				if(button.levelnumber == Resources.getInstance().currentlevel)
					selectedFont.draw(batch, button.levelnumber + "", button.box.getCenter().x-22, button.box.getCenter().y);
				else
					font.draw(batch, button.levelnumber + "", button.box.getCenter().x-22, button.box.getCenter().y);
			}
		}
		if(next>0)
			font.draw(batch, "<", 377, 55);
		if((next == 0 && Resources.getInstance().levelcount > 12) || (Resources.getInstance().levelcount / (12*next) > 1))
			font.draw(batch, ">", 480, 55);
		if(Resources.getInstance().currentlevel ==1 || HighScoreManager.getInstance().getHighScore(Resources.getInstance().currentlevel-1).first != 0)
			font.draw(batch, "Start", 578, 55);
		else
			font.draw(batch, "Locked", 578, 55);
		batch.end();
		
		
		if (!finished && fade > 0) {
			fade = Math.max(fade - (delta), 0);
			fadeBatch.begin();
			blackFade.setColor(blackFade.getColor().r, blackFade.getColor().g, blackFade.getColor().b, fade);
			blackFade.draw(fadeBatch);
			fadeBatch.end();
		}

		if (finished) {
			fade = Math.min(fade + (delta), 1);
			fadeBatch.begin();
			blackFade.setColor(blackFade.getColor().r, blackFade.getColor().g, blackFade.getColor().b, fade);
			blackFade.draw(fadeBatch);
			fadeBatch.end();
			if (fade >= 1) {
				game.setScreen(new GameScreen(game,Resources.getInstance().currentlevel));
			}
		}
		
	}
	
	private void sortScene() {
		// sort blocks because of transparency
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
	
	private void renderLevelSelect() {


		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		transShader.begin();
		transShader.setUniformMatrix("VPMatrix", camMenu.combined);

		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		//render level buttons
		{
			
			for(LevelButton button : buttons) {
				if(button.levelnumber <=12 && next==0 || (button.levelnumber > 12*next && next > 0 && button.levelnumber <= 12*(next+1))) { 
					tmp.idt();
					model.idt();
	
					tmp.setToTranslation(-400.0f, -240.0f, 0.0f);
					model.mul(tmp);
					
					tmp.setToTranslation(button.box.getCenter().x , button.box.getCenter().y, 0);
					model.mul(tmp);				
					
					tmp.setToScaling(30.0f, 30.0f, 10.0f);
					model.mul(tmp);
	
					transShader.setUniformMatrix("MMatrix", model);
	
					transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0],Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2],Resources.getInstance().blockEdgeColor[3]+0.2f);
					wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
		
					transShader.setUniformf("a_color", Resources.getInstance().blockColor[0],Resources.getInstance().blockColor[1],Resources.getInstance().blockColor[2],Resources.getInstance().blockColor[3]+0.2f);
					blockModel.render(transShader, GL20.GL_TRIANGLES);
				}
					
			}
		}
		//render back button
		{
			if(next > 0) {
				tmp.idt();
				model.idt();

				tmp.setToTranslation(-400.0f, -240.0f, 0.0f);
				model.mul(tmp);
				
				tmp.setToTranslation(400 , 55, 0);
				model.mul(tmp);
				
				tmp.setToScaling(30.0f, 30.0f, 10.0f);
				model.mul(tmp);

				transShader.setUniformMatrix("MMatrix", model);

				transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0],Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2],Resources.getInstance().blockEdgeColor[3]+0.2f);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", Resources.getInstance().blockColor[0],Resources.getInstance().blockColor[1],Resources.getInstance().blockColor[2],Resources.getInstance().blockColor[3]+0.2f);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
			}
	
		}
		//render forward button
		{ if((next == 0 && Resources.getInstance().levelcount > 12) || (Resources.getInstance().levelcount / (12*next) > 1)) {
				tmp.idt();
				model.idt();

				tmp.setToTranslation(-400.0f, -240.0f, 0.0f);
				model.mul(tmp);
				
				tmp.setToTranslation(500 , 55, 0);
				model.mul(tmp);
				
				tmp.setToScaling(30.0f, 30.0f, 10.0f);
				model.mul(tmp);

				transShader.setUniformMatrix("MMatrix", model);

				transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0],Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2],Resources.getInstance().blockEdgeColor[3]+0.2f);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", Resources.getInstance().blockColor[0],Resources.getInstance().blockColor[1],Resources.getInstance().blockColor[2],Resources.getInstance().blockColor[3]+0.2f);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
		}
	
		}
		
		//render start button
		{
				tmp.idt();
				model.idt();

				tmp.setToTranslation(-400.0f, -240.0f, 0.0f);
				model.mul(tmp);
				
				tmp.setToTranslation(650 , 55, 0);
				model.mul(tmp);
				
				tmp.setToScaling(80.0f, 30.0f, 10.0f);
				model.mul(tmp);

				transShader.setUniformMatrix("MMatrix", model);

				transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0],Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2],Resources.getInstance().blockEdgeColor[3]+0.2f);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", Resources.getInstance().blockColor[0],Resources.getInstance().blockColor[1],Resources.getInstance().blockColor[2],Resources.getInstance().blockColor[3]+0.2f);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
	
		}
		transShader.end();		
	}
	
	private void renderScene() {		
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClearColor(Resources.getInstance().clearColor[0],Resources.getInstance().clearColor[1],Resources.getInstance().clearColor[2],Resources.getInstance().clearColor[3]);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
				
		transShader.begin();
		transShader.setUniformMatrix("VPMatrix", cam.combined);
		{
			// render Background Wire
			tmp.idt();
			model.idt();

			tmp.setToScaling(40.5f, 40.5f, 40.5f);
			model.mul(tmp);

			tmp.setToRotation(xAxis, angleX + angleXBack);
			model.mul(tmp);
			tmp.setToRotation(yAxis, angleY + angleYBack);
			model.mul(tmp);

			tmp.setToTranslation(0, 0, 0);
			model.mul(tmp);

			transShader.setUniformMatrix("MMatrix", model);

			transShader.setUniformf("a_color", Resources.getInstance().backgroundWireColor[0],Resources.getInstance().backgroundWireColor[1],Resources.getInstance().backgroundWireColor[2],Resources.getInstance().backgroundWireColor[3]);
			sphereModel.render(transShader, GL20.GL_LINE_STRIP);
		}
		{
			// render Wire
			tmp.idt();
			model.idt();

			tmp.setToRotation(xAxis, angleX);
			model.mul(tmp);
			tmp.setToRotation(yAxis, angleY);
			model.mul(tmp);
			
			tmp.setToScaling(5.5f, 5.5f, 5.5f);
			model.mul(tmp);

			tmp.setToTranslation(0, 0, 0);
			model.mul(tmp);

			transShader.setUniformMatrix("MMatrix", model);

			transShader.setUniformf("a_color", Resources.getInstance().clearColor[0],Resources.getInstance().clearColor[1],Resources.getInstance().clearColor[2],Resources.getInstance().clearColor[3]);
			blockModel.render(transShader, GL20.GL_TRIANGLES);
			
			transShader.setUniformf("a_color", Resources.getInstance().wireCubeEdgeColor[0],Resources.getInstance().wireCubeEdgeColor[1],Resources.getInstance().wireCubeEdgeColor[2],Resources.getInstance().wireCubeEdgeColor[3]);
			wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
			
			transShader.setUniformf("a_color", Resources.getInstance().wireCubeColor[0],Resources.getInstance().wireCubeColor[1],Resources.getInstance().wireCubeColor[2],Resources.getInstance().wireCubeColor[3]);
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
	
				transShader.setUniformf("a_color", Resources.getInstance().blockColor[0], Resources.getInstance().blockColor[1], Resources.getInstance().blockColor[2], Resources.getInstance().blockColor[3]+ renderable.collideAnimation);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
	
				transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0], Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2], Resources.getInstance().blockEdgeColor[3] + renderable.collideAnimation);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
			}
			
			// render movableblocks
			if(renderable instanceof MovableBlock) {
				model.set(renderable.model);
	
				transShader.setUniformMatrix("MMatrix", model);
	
				transShader.setUniformf("a_color", Resources.getInstance().movableBlockColor[0], Resources.getInstance().movableBlockColor[1], Resources.getInstance().movableBlockColor[2], Resources.getInstance().movableBlockColor[3] + renderable.collideAnimation);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", Resources.getInstance().movableBlockEdgeColor[0], Resources.getInstance().movableBlockEdgeColor[1],Resources.getInstance().movableBlockEdgeColor[2], Resources.getInstance().movableBlockEdgeColor[3] + renderable.collideAnimation);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
			}
			
			// render switchableblocks
			if(renderable instanceof SwitchableBlock) {
				if(!((SwitchableBlock) renderable).isSwitched) {	
					model.set(renderable.model);
		
					transShader.setUniformMatrix("MMatrix", model);
		
					transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0] * (Math.abs(((SwitchableBlock)renderable).id)),Resources.getInstance().switchBlockColor[1]* (Math.abs(((SwitchableBlock)renderable).id)), Resources.getInstance().switchBlockColor[2] * (Math.abs(((SwitchableBlock)renderable).id)), Resources.getInstance().switchBlockColor[3]+ renderable.collideAnimation);
					wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
		
					transShader.setUniformf("a_color", Resources.getInstance().switchBlockEdgeColor[0] * (Math.abs(((SwitchableBlock)renderable).id)), Resources.getInstance().switchBlockEdgeColor[1]* (Math.abs(((SwitchableBlock)renderable).id)), Resources.getInstance().switchBlockEdgeColor[2] * (Math.abs(((SwitchableBlock)renderable).id)), Resources.getInstance().switchBlockEdgeColor[3] + renderable.collideAnimation);
					blockModel.render(transShader, GL20.GL_TRIANGLES);
				}
			}
			
			// render switches
			if(renderable instanceof Switch) {
				model.set(renderable.model);	

				tmp.setToScaling(0.3f, 0.3f, 0.3f);
				model.mul(tmp);

				transShader.setUniformMatrix("MMatrix", model);
				transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0] * (Math.abs(((Switch)renderable).id)),Resources.getInstance().switchBlockColor[1]* (Math.abs(((Switch)renderable).id)), Resources.getInstance().switchBlockColor[2] * (Math.abs(((Switch)renderable).id)), Resources.getInstance().switchBlockColor[3]+ renderable.collideAnimation);
				playerModel.render(transShader, GL20.GL_TRIANGLES);
				
				tmp.setToScaling(2.0f, 2.0f, 2.0f);
				model.mul(tmp);

				//render hull			
				transShader.setUniformMatrix("MMatrix", model);
				transShader.setUniformf("a_color", Resources.getInstance().switchBlockEdgeColor[0] * (Math.abs(((Switch)renderable).id)), Resources.getInstance().switchBlockEdgeColor[1]* (Math.abs(((Switch)renderable).id)), Resources.getInstance().switchBlockEdgeColor[2] * (Math.abs(((Switch)renderable).id)), Resources.getInstance().switchBlockEdgeColor[3] + renderable.collideAnimation);
				playerModel.render(transShader, GL20.GL_LINE_STRIP);
			}
			
			// render Player
			if(renderable instanceof Player && HighScoreManager.getInstance().getHighScore(Resources.getInstance().currentlevel).first != 0) {
				model.set(renderable.model);	
				
				tmp.setToRotation(xAxis, angleXBack);
				model.mul(tmp);
				tmp.setToRotation(yAxis, angleYBack);
				model.mul(tmp);

				tmp.setToScaling(0.5f, 0.5f, 0.5f);
				model.mul(tmp);

				transShader.setUniformMatrix("MMatrix", model);
				transShader.setUniformf("a_color",Resources.getInstance().playerColor[0], Resources.getInstance().playerColor[1], Resources.getInstance().playerColor[2], Resources.getInstance().playerColor[3]);
				playerModel.render(transShader, GL20.GL_TRIANGLES);
				
				tmp.setToScaling(2.0f, 2.0f, 2.0f);
				model.mul(tmp);

				//render hull			
				transShader.setUniformMatrix("MMatrix", model);
				transShader.setUniformf("a_color",Resources.getInstance().playerEdgeColor[0], Resources.getInstance().playerEdgeColor[1], Resources.getInstance().playerEdgeColor[2], Resources.getInstance().playerEdgeColor[3]);
				playerModel.render(transShader, GL20.GL_LINE_STRIP);
			}
			
			// render Portals
			if(renderable instanceof Portal) {
				if(renderable.position.x != -11) {
					// render Portal
					model.set(renderable.model);
		
					transShader.setUniformMatrix("MMatrix", model);
					
					transShader.setUniformf("a_color", Resources.getInstance().portalColor[0], Resources.getInstance().portalColor[1] * ((Math.abs(((Portal)renderable).id)*4f)), Resources.getInstance().portalColor[2], Resources.getInstance().portalColor[3] * (Math.abs(((Portal)renderable).id)) + renderable.collideAnimation);
					blockModel.render(transShader, GL20.GL_TRIANGLES);
					
					//render hull			
					transShader.setUniformf("a_color", Resources.getInstance().portalEdgeColor[0],Resources.getInstance().portalEdgeColor[1] * ((Math.abs(((Portal)renderable).id)*4f)), Resources.getInstance().portalEdgeColor[2], Resources.getInstance().portalEdgeColor[3] * (Math.abs(((Portal)renderable).id)) + renderable.collideAnimation);
					wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
				}
			}
				
			// render Target
			if(renderable instanceof Target && HighScoreManager.getInstance().getHighScore(Resources.getInstance().currentlevel).first != 0) {
				model.set(renderable.model);
				
				tmp.setToRotation(yAxis, angleY + angleYBack);
				model.mul(tmp);

				transShader.setUniformMatrix("MMatrix", model);

				transShader.setUniformf("a_color", Resources.getInstance().targetColor[0],  Resources.getInstance().targetColor[1],  Resources.getInstance().targetColor[2], Resources.getInstance().targetColor[3] + renderable.collideAnimation);
				targetModel.render(transShader, GL20.GL_TRIANGLES);
				
				//render hull			
				transShader.setUniformf("a_color",  Resources.getInstance().targetEdgeColor[0], Resources.getInstance().targetEdgeColor[1], Resources.getInstance().targetEdgeColor[2], Resources.getInstance().targetEdgeColor[3] + renderable.collideAnimation);
				targetModel.render(transShader, GL20.GL_LINE_STRIP);
			}
				
		}
		
		transShader.end();
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub 
		
	}
	
	public void dispose() {
		frameBuffer.dispose();
		frameBufferVert.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		if(keycode == Input.Keys.BACK) {
			game.setScreen(new MainMenuScreen(game));
		}
		if (keycode == Input.Keys.ESCAPE) {
			game.setScreen(new MainMenuScreen(game));
		}
		if (keycode == Input.Keys.ENTER && Resources.getInstance().currentlevel != 0) {
			game.setScreen(new GameScreen(game, Resources.getInstance().currentlevel));
		}
		if(keycode == Input.Keys.LEFT) {
			int lvl = Resources.getInstance().currentlevel;
			lvl--;
			if(lvl >0 && next==0)
				initLevel(lvl);
			else if(lvl > 12*next)
				initLevel(lvl);
		}
		
		if(keycode == Input.Keys.RIGHT) {
			int lvl = Resources.getInstance().currentlevel;
			lvl++;
			if(lvl <= 12*(next+1) && lvl <= Resources.getInstance().levelcount)
				initLevel(lvl);
		}
		
		if(keycode==Input.Keys.DOWN) {
			int lvl = Resources.getInstance().currentlevel;
			lvl+=4;
			if(lvl <= 12*(next+1) && lvl <= Resources.getInstance().levelcount)
				initLevel(lvl);
		}
		
		if(keycode==Input.Keys.UP) {
			int lvl = Resources.getInstance().currentlevel;
			lvl-=4;
			if(lvl >0 && next==0)
				initLevel(lvl);
			else if(lvl > 12*next)
				initLevel(lvl);
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {

		return false;
	}

	@Override
	public boolean keyTyped(char character) {

		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		x = (int) (x / (float) Gdx.graphics.getWidth() * 800);
		y = (int) (y / (float) Gdx.graphics.getHeight() * 480);
		
		y = 480 -y;
		for(LevelButton b : buttons) {
			if(b.levelnumber <= 12 && next==0) {
				if(b.box.contains(new Vector3(x,y,0))) {
					initLevel(b.levelnumber);
				}
			}
			else if(b.levelnumber > 12*next && next > 0 && b.levelnumber <= 12*(next+1)) {
				if(b.box.contains(new Vector3(x,y,0))) {
					initLevel(b.levelnumber);
				}
			}
		}
		if(collisionLevelStart.contains(new Vector3(x,y,0)) && (HighScoreManager.getInstance().getHighScore(Resources.getInstance().currentlevel).first != 0
			|| Resources.getInstance().currentlevel==1)) {
			game.setScreen(new GameScreen(game,Resources.getInstance().currentlevel));
		}
		
		if(collisionLevelForward.contains(new Vector3(x,y,0)) && ((next == 0 && Resources.getInstance().levelcount > 12) || (Resources.getInstance().levelcount / (12*next) > 1))) {
			next++;
			initLevel(12*next +1);
		}
		
		if(collisionLevelBack.contains(new Vector3(x,y,0)) && next > 0) {
			next--;
			initLevel(12*next +1);
		}
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {

		return false;
	}

	@Override
	public boolean touchMoved(int x, int y) {
		x = (int) (x / (float) Gdx.graphics.getWidth() * 800);
		y = (int) (y / (float) Gdx.graphics.getHeight() * 480);
		
		return false;
	}

	@Override
	public boolean scrolled(int amount) {

		return false;
	}

}
