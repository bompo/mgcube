package de.swagner.mgcube;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class MainMenuScreen extends DefaultScreen implements InputProcessor {

	float startTime = 0;
	PerspectiveCamera cam;
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
	Array<String> menuItems = new Array<String>();
	int selectedMenuItem = -1;
	SpriteBatch fadeBatch;
	Sprite blackFade;
	Sprite title;
	float fade = 1.0f;
	boolean finished = false;
	
	Player player = new Player();
	Target target = new Target();
	Array<Block> blocks = new Array<Block>();
	Array<Portal> portals = new Array<Portal>();
	Array<MovableBlock> movableBlocks = new Array<MovableBlock>();
	Array<Renderable> renderObjects = new Array<Renderable>();
	boolean animateWorld = false;
	boolean animatePlayer = false;	

	float angleXBack = 0;
	float angleYBack = 0;
	
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
	FrameBuffer frameBuffer;
	FrameBuffer frameBufferVert;

	Vector3 position = new Vector3();
	
	public MainMenuScreen(Game game) {
		super(game);
		Gdx.input.setInputProcessor(this);
		
		title = new Sprite(new Texture(Gdx.files.internal("data/title.png")));
		blackFade = new Sprite(new Texture(Gdx.files.internal("data/blackfade.png")));

		blockModel = Resources.getInstance().blockModel;
		playerModel = Resources.getInstance().playerModel;
		targetModel = Resources.getInstance().targetModel;
		quadModel = Resources.getInstance().quadModel;
		wireCubeModel = Resources.getInstance().wireCubeModel;
		sphereModel = Resources.getInstance().sphereModel;

		cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(5.0f, 0, 16f);
		cam.direction.set(0, 0, -1);
		cam.up.set(0, 1, 0);
		cam.near = 1f;
		cam.far = 1000;

		// controller = new PerspectiveCamController(cam);
		// Gdx.input.setInputProcessor(controller);

		batch = new SpriteBatch();
		bat = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		font = Resources.getInstance().font;	
		font.scale(0.5f);
		selectedFont = Resources.getInstance().selectedFont;
		selectedFont.scale(0.1f);

		fadeBatch = new SpriteBatch();
		fadeBatch.getProjectionMatrix().setToOrtho2D(0, 0, 2, 2);

		transShader = Resources.getInstance().transShader;
		bloomShader = Resources.getInstance().bloomShader;
		
		menuItems.add("start game");
		menuItems.add("select level");
		menuItems.add("time attack");
		menuItems.add("options");
		
		initRender();
		
		initLevel(0);
		angleY = -70;
		angleX = -10;
	}
	
	public void initRender() {
		Gdx.graphics.getGL20().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

//		//antiAliasing for Desktop - no support in Android
//		Gdx.graphics.getGL20().glEnable (GL10.GL_LINE_SMOOTH);
//		Gdx.graphics.getGL20().glEnable (GL10.GL_BLEND);
//		Gdx.graphics.getGL20().glBlendFunc (GL10.GL_SRC_ALPHA,GL10. GL_ONE_MINUS_SRC_ALPHA);
//		Gdx.graphics.getGL20().glHint (GL10.GL_LINE_SMOOTH_HINT, GL10.GL_FASTEST);
//		Gdx.graphics.getGL20().glLineWidth (1.5f);		
		
		frameBuffer = new FrameBuffer(Format.RGB565, Resources.getInstance().m_i32TexSize, Resources.getInstance().m_i32TexSize, false);		
		frameBufferVert = new FrameBuffer(Format.RGB565, Resources.getInstance().m_i32TexSize, Resources.getInstance().m_i32TexSize, false);
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
		int[][][] level;
		switch (levelnumber) {
		case 1:
			level = Resources.getInstance().level7;
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

		// more levels

		default:
			level = Resources.getInstance().opening;
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
				}
			}
		}
		
//		renderObjects.add(player);
//		renderObjects.add(target);
		renderObjects.addAll(blocks);		
		renderObjects.addAll(portals);
		renderObjects.addAll(movableBlocks);
	}
	
	@Override
	public void show() {
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		delta = Math.min(0.02f, delta);
		
		startTime += delta;
		
		angleXBack += MathUtils.sin(startTime)/10f;
		angleYBack += MathUtils.cos(startTime)/5f;
		
		angleX += MathUtils.sin(startTime)/10f;
		angleY += MathUtils.cos(startTime)/5f;

		cam.update();
		
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
		
		frameBuffer.begin();
		renderScene();
		renderMenu();
		frameBuffer.end();

		//PostProcessing
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDisable(GL20.GL_BLEND);
		
		frameBuffer.getColorBufferTexture().bind(0);

		bloomShader.begin();
		bloomShader.setUniformi("sTexture", 0);
		bloomShader.setUniformf("bloomFactor", Helper.map((MathUtils.sin(startTime * 5f) * 0.5f) + 0.5f,0,1,0.5f,0.62f));
		
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
		renderMenu();
			
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDisable(GL20.GL_BLEND);
				
		batch.enableBlending();
		batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
		batch.begin();
		batch.draw(frameBuffer.getColorBufferTexture(), 0, 0,800,480,0,0,frameBuffer.getWidth(),frameBuffer.getHeight(),false,true);
		batch.end();
		
		bat.begin();
		float y = 365;
		for(String s : menuItems) {
			if(selectedMenuItem > -1 && s.equals(menuItems.get(selectedMenuItem)))
				selectedFont.draw(bat, s, 500, y);
			else
				font.draw(bat, s, 500, y);
			y -= 80;
		}
		bat.end();
		
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
				game.setScreen(new GameScreen(game));
			}
		}

	}
	
	private void renderMenu() {

		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
				
		transShader.begin();
			
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		{
			// render Button 1
			tmp.idt();
			model.idt();

			tmp.setToScaling(3.5f, 0.6f, 0.5f);
			model.mul(tmp);

			tmp.setToRotation(xAxis, (angleXBack/40.f));
			model.mul(tmp);
			tmp.setToRotation(yAxis, (angleYBack/100.f)-2.f);
			model.mul(tmp);

			tmp.setToTranslation(3.3f,4.5f, 12);
			model.mul(tmp);

			modelViewProjection.idt();
			modelViewProjection.set(cam.combined);
			modelViewProjection = tmp.mul(model);

			transShader.setUniformMatrix("MVPMatrix", modelViewProjection);

			transShader.setUniformf("a_color", 1.0f, 0.1f, 0.1f, 0.4f);
			wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);

			transShader.setUniformf("a_color", 1.0f, 0.1f, 0.1f,  0.08f);
			blockModel.render(transShader, GL20.GL_TRIANGLES);
		}
		
		{
			// render Button 2
			tmp.idt();
			model.idt();

			tmp.setToScaling(3.5f, 0.6f, 0.5f);
			model.mul(tmp);

			tmp.setToRotation(xAxis, (angleXBack/40.f));
			model.mul(tmp);
			tmp.setToRotation(yAxis, (angleYBack/100.f)-2.f);
			model.mul(tmp);

			tmp.setToTranslation(3.3f,1.3f, 12);
			model.mul(tmp);

			modelViewProjection.idt();
			modelViewProjection.set(cam.combined);
			modelViewProjection = tmp.mul(model);

			transShader.setUniformMatrix("MVPMatrix", modelViewProjection);

			transShader.setUniformf("a_color", 1.0f, 0.1f, 0.1f, 0.4f);
			wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);

			transShader.setUniformf("a_color", 1.0f, 0.1f, 0.1f,  0.08f);
			blockModel.render(transShader, GL20.GL_TRIANGLES);
		}
		
		{
			// render Button 3
			tmp.idt();
			model.idt();

			tmp.setToScaling(3.5f, 0.6f, 0.5f);
			model.mul(tmp);

			tmp.setToRotation(xAxis, (angleXBack/40.f));
			model.mul(tmp);
			tmp.setToRotation(yAxis, (angleYBack/100.f)-2.f);
			model.mul(tmp);

			tmp.setToTranslation(3.3f,-2.0f, 12);
			model.mul(tmp);

			modelViewProjection.idt();
			modelViewProjection.set(cam.combined);
			modelViewProjection = tmp.mul(model);

			transShader.setUniformMatrix("MVPMatrix", modelViewProjection);

			transShader.setUniformf("a_color", 1.0f, 0.1f, 0.1f, 0.4f);
			wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);

			transShader.setUniformf("a_color", 1.0f, 0.1f, 0.1f,  0.08f);
			blockModel.render(transShader, GL20.GL_TRIANGLES);
		}
		
		{
			// render Button 4
			tmp.idt();
			model.idt();

			tmp.setToScaling(3.5f, 0.6f, 0.5f);
			model.mul(tmp);

			tmp.setToRotation(xAxis, (angleXBack/40.f));
			model.mul(tmp);
			tmp.setToRotation(yAxis, (angleYBack/100.f)-2.f);
			model.mul(tmp);

			tmp.setToTranslation(3.3f,-5.0f, 12);
			model.mul(tmp);

			modelViewProjection.idt();
			modelViewProjection.set(cam.combined);
			modelViewProjection = tmp.mul(model);

			transShader.setUniformMatrix("MVPMatrix", modelViewProjection);

			transShader.setUniformf("a_color", 1.0f, 0.1f, 0.1f, 0.4f);
			wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);

			transShader.setUniformf("a_color", 1.0f, 0.1f, 0.1f,  0.08f);
			blockModel.render(transShader, GL20.GL_TRIANGLES);
		}
		
		transShader.end();
	}
	
	private void renderScene() {

		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
				
		transShader.begin();
		
		// render all objects
		for (Renderable renderable : renderObjects) {
			
			if(renderable instanceof Block) {
				tmp.idt();
				model.idt();

				model.set(renderable.model);
	
				modelViewProjection.idt();
				modelViewProjection.set(cam.combined);
				modelViewProjection = tmp.mul(model);
	
				transShader.setUniformMatrix("MVPMatrix", modelViewProjection);
	
				transShader.setUniformf("a_color", 1.0f, 0.1f, 0.1f, 0.8f);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", 1.0f, 0.1f, 0.1f, 0.2f);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
			}
			
			// render movableblocks
			if(renderable instanceof MovableBlock) {
				tmp.idt();
				model.idt();

				model.set(renderable.model);
	
				modelViewProjection.idt();
				modelViewProjection.set(cam.combined);
				modelViewProjection = tmp.mul(model);
	
				transShader.setUniformMatrix("MVPMatrix", modelViewProjection);
	
				transShader.setUniformf("a_color", 1.0f, 0.8f, 0.1f, 0.8f);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", 1.0f, 0.8f, 0.1f, 0.2f);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
			}
			
			// render Player
			if(renderable instanceof Player) {
				tmp.idt();
				model.idt();

				model.set(renderable.model);	
				
				tmp.setToRotation(xAxis, angleXBack);
				model.mul(tmp);
				tmp.setToRotation(yAxis, angleYBack);
				model.mul(tmp);

				tmp.setToScaling(0.5f, 0.5f, 0.5f);
				model.mul(tmp);

				modelViewProjection.idt();
				modelViewProjection.set(cam.combined);
				modelViewProjection = tmp.mul(model);
				
				transShader.setUniformMatrix("MVPMatrix", modelViewProjection);
				transShader.setUniformf("a_color", 1.0f, 1.0f, 0.0f, 0.4f);
				playerModel.render(transShader, GL20.GL_TRIANGLES);
				
				tmp.setToScaling(2.0f, 2.0f, 2.0f);
				model.mul(tmp);

				modelViewProjection.idt();
				modelViewProjection.set(cam.combined);
				modelViewProjection = tmp.mul(model);
				
				//render hull			
				transShader.setUniformMatrix("MVPMatrix", modelViewProjection);
				transShader.setUniformf("a_color", 1.0f, 1.0f, 0.0f, 0.4f);
				playerModel.render(transShader, GL20.GL_LINE_STRIP);
			}
			
			// render Portals
			if(renderable instanceof Portal) {
				if(renderable.position.x != -11) {
					// render Portal
					tmp.idt();
					model.idt();

					model.set(renderable.model);
		
					modelViewProjection.idt();
					modelViewProjection.set(cam.combined);
					modelViewProjection = tmp.mul(model);
		
					transShader.setUniformMatrix("MVPMatrix", modelViewProjection);
					
					transShader.setUniformf("a_color", 0.0f, 0.03f * ( Math.abs(((Portal)renderable).id)*5.0f), 1.0f, 0.5f);
					blockModel.render(transShader, GL20.GL_TRIANGLES);
					
					//render hull			
					transShader.setUniformf("a_color", 0.0f,0.03f * ( Math.abs(((Portal)renderable).id)*5.0f), 1.0f, 0.4f);
					wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
				}
			}
				
			// render Target
			if(renderable instanceof Target) {
				tmp.idt();
				model.idt();

				model.set(renderable.model);

				modelViewProjection.idt();
				modelViewProjection.set(cam.combined);
				modelViewProjection = tmp.mul(model);

				transShader.setUniformMatrix("MVPMatrix", modelViewProjection);

				transShader.setUniformf("a_color", 0.0f, 1.1f, 0.1f,0.5f);
				targetModel.render(transShader, GL20.GL_TRIANGLES);
				
				//render hull			
				transShader.setUniformf("a_color", 0.0f, 1.1f, 0.1f, 0.4f);
				targetModel.render(transShader, GL20.GL_LINE_STRIP);
			}
				
		}
			
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
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

			modelViewProjection.idt();
			modelViewProjection.set(cam.combined);
			modelViewProjection = tmp.mul(model);

			transShader.setUniformMatrix("MVPMatrix", modelViewProjection);

			transShader.setUniformf("a_color", 1.0f, 0.1f, 0.1f, 0.4f);
			wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);

			transShader.setUniformf("a_color", 1.0f, 0.1f, 0.1f,  0.08f);
			blockModel.render(transShader, GL20.GL_TRIANGLES);
		}
		
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

			modelViewProjection.idt();
			modelViewProjection.set(cam.combined);
			modelViewProjection = tmp.mul(model);

			transShader.setUniformMatrix("MVPMatrix", modelViewProjection);

			transShader.setUniformf("a_color", 1.0f, 0.8f, 0.8f, 0.2f);
			sphereModel.render(transShader, GL20.GL_LINE_STRIP);
		}

		transShader.end();
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
		
		if (keycode == Input.Keys.ENTER) {
			finished = true;
		}
		
		if (keycode == Input.Keys.DOWN) {
			selectedMenuItem++;
			selectedMenuItem %= 4;
		}
		
		if (keycode == Input.Keys.UP) {
			if(selectedMenuItem > 0)
				selectedMenuItem--;
			else
				selectedMenuItem = 3;
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
//		x = (int) (x / (float) Gdx.graphics.getWidth() * 800);
//		y = (int) (y / (float) Gdx.graphics.getHeight() * 480);

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
		return false;
	}

}
