package de.swagner.mgcube;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
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
	private ShaderProgram transShader;
	private ShaderProgram bloomShader;
	FrameBuffer frameBuffer;
	FrameBuffer frameBufferVert;
	FrameBuffer frameBufferHori;
	private int m_i32TexSize;
	private float m_fTexelOffset;

	public IntroScreen(Game game) {
		super(game);
		Gdx.input.setInputProcessor(this);
		
		title = new Sprite(new Texture(Gdx.files.internal("data/logo.png")));
		blackFade = new Sprite(new Texture(Gdx.files.internal("data/blackfade.png")));

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

		initShader();
		initRender();
		
		Preferences prefs = Gdx.app.getPreferences("cubism3000");
		if(prefs.getBoolean("music") != true) { 
			if(Resources.getInstance().music == null) Resources.getInstance().reInit();
			Resources.getInstance().music.play();
			Resources.getInstance().music.setLooping(true);
		} else {
			Resources.getInstance().music.stop();	
		}
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
		bloomShader.setUniformf("TexelOffsetX", m_fTexelOffset);
		bloomShader.setUniformf("TexelOffsetY", 0.0f);
		quadModel.render(bloomShader, GL20.GL_TRIANGLE_STRIP);
		bloomShader.end(); 
		frameBufferVert.end();
		
		
		frameBufferVert.getColorBufferTexture().bind(0);
		
		frameBufferHori.begin();		
		bloomShader.begin();
		bloomShader.setUniformi("sTexture", 0);
		bloomShader.setUniformf("bloomFactor", (MathUtils.sin(startTime * 1f) * 0.1f) + 0.5f);
		bloomShader.setUniformf("TexelOffsetX", 0.0f);
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
		frameBuffer.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		if (Gdx.input.isTouched())
			return false;

		if (keycode == Input.Keys.SPACE) {
			finished = true;
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
