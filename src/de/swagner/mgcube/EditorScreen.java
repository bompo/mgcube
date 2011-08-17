package de.swagner.mgcube;

import java.util.HashMap;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
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

public class EditorScreen extends DefaultScreen implements InputProcessor {

	static float EPSILON = 0.0001f;
	
	float startTime = 0;
	PerspectiveCamera cam;
	OrthographicCamera camMenu;
	Mesh blockModel;
	Mesh playerModel;
	Mesh coneModel;
	Mesh targetModel;
	Mesh quadModel;
	Mesh wireCubeModel;
	Mesh sphereModel;
	float angleX = 0;
	float angleY = 0;	

	int selectedMenuItem = -1;
	
	BoundingBox button1 = new BoundingBox();
	BoundingBox button2 = new BoundingBox();
	BoundingBox button3 = new BoundingBox();
	BoundingBox button4 = new BoundingBox();
	BoundingBox button5 = new BoundingBox();
	BoundingBox button6 = new BoundingBox();

	float angleXBack = 0;
	float angleYBack = 0;
	float angleXFront = 0;
	float angleYFront = 0;
	SpriteBatch batch;
	SpriteBatch fontbatch;
	BitmapFont font;
	BitmapFont timeAttackFont; //used only for drawing the +45 notification
	Player player;
	Target target;

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
	
	float showError = 0;
	int errorReason = 0;

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
	Portal port = new Portal();
	Vector3 position = new Vector3();
	
	protected int lastTouchX;
	protected int lastTouchY;
	private float changeLevelEffect;
	
	float touchStartX = 0;
	float touchStartY = 0;
	
	//pinchToZoom
	HashMap<Integer, Vector2> pointers  = new HashMap<Integer,Vector2>();;
	Vector2 v1 = new Vector2();
	Vector2 v2 = new Vector2();
	int finger_one_pointer = -1;
	int finger_two_pointer = -1;
	float initialDistance = 0f;
	float distance = 0f;
	
	EditorBlock editorBlock = new EditorBlock(new Vector3());
	
	String levelCode = "";
	boolean canSave = false;
	
	//0 = edit
	//1 = play
	int mode = 0;

	public EditorScreen(Game game, int level, int mode) {
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
		
		camMenu = new OrthographicCamera(800,480);

		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		fontbatch = new SpriteBatch();

		blackFade = new Sprite(new Texture(Gdx.files.internal("data/blackfade.png")));
		fadeBatch = new SpriteBatch();
		fadeBatch.getProjectionMatrix().setToOrtho2D(0, 0, 2, 2);

		font = Resources.getInstance().font;
		font.setScale(1);
		font.scale(0.5f);
		
		timeAttackFont = Resources.getInstance().selectedFont;
		timeAttackFont.setScale(1);
		timeAttackFont.scale(0.5f);

		transShader = Resources.getInstance().transShader;
		bloomShader = Resources.getInstance().bloomShader;
		
		initRender();
		angleY = 160;
		angleX = 0;
		
		button1.set(new Vector3(30, 450, 0), new Vector3(90, 390, 0));
		button2.set(new Vector3(100, 450, 0), new Vector3(160, 390, 0));
		button3.set(new Vector3(180, 450, 0), new Vector3(240, 390, 0));
		button4.set(new Vector3(100, 375, 0), new Vector3(160, 315, 0));
		button5.set(new Vector3(590, 450, 0), new Vector3(790, 370, 0));
		button6.set(new Vector3(30, 90, 0), new Vector3(190, 30, 0));
		
		initLevel(level);
		saveLevel();
		alterLevel();
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
	
	
	private void alterLevel() {
		renderObjects.clear();
		
		renderObjects.add(editorBlock);
		if(player!= null) {
			renderObjects.add(player);
		}
		if(target!= null) {
			renderObjects.add(target);
		}
		renderObjects.addAll(blocks);		
		renderObjects.addAll(portals);
		renderObjects.addAll(movableBlocks);
		renderObjects.addAll(switches);
		renderObjects.addAll(switchblocks);
	}
	
	private boolean saveLevel() {
		if(!canSaveCheck()) {
			if(startTime>1) {
				showError = 3;
			}
			return false;
		}
		
		int[][][] levelArray = { { { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } } };
			
		if(player!=null) {
			levelArray[((int) (Math.abs((Math.round(player.position.z +10)/2))))][ ((int) (Math.abs((Math.round(player.position.y +10)/2))))][ ((int) (Math.abs((Math.round(-player.position.x +10)/2))))] = 2;
		}
		if(target!=null) {
			levelArray[((int) (Math.abs((Math.round(target.position.z +10)/2))))][ ((int) (Math.abs((Math.round(target.position.y +10)/2))))][ ((int) (Math.abs((Math.round(-target.position.x +10)/2))))] = 3;
		}
		for(Block block:blocks) {
//			Gdx.app.log("", (int) ((-block.position.x +10)/2)+" " + (int) ((block.position.y +10)/2)+" "+(int) ((block.position.z +10)/2));
			levelArray[ ((int) (Math.abs((Math.round(block.position.z +10)/2))))][ ((int) (Math.abs((Math.round(block.position.y +10)/2))))][ ((int) (Math.abs((Math.round(-block.position.x +10)/2))))] = 1;
		}
		for(MovableBlock block:movableBlocks) {
			levelArray[ ((int) (Math.abs((Math.round(block.position.z +10)/2))))][ ((int) (Math.abs((Math.round(block.position.y +10)/2))))][ ((int) (Math.abs((Math.round(-block.position.x +10)/2))))] = 9;
		}
		for(Portal block:portals) {
			levelArray[ ((int) (Math.abs((Math.round(block.position.z +10)/2))))][ ((int) (Math.abs((Math.round(block.position.y +10)/2))))][ ((int) (Math.abs((Math.round(-block.position.x +10)/2))))] = block.id;
		}
		for(SwitchableBlock block:switchblocks) {
			levelArray[ ((int) (Math.abs((Math.round(block.position.z +10)/2))))][ ((int) (Math.abs((Math.round(block.position.y +10)/2))))][ ((int) (Math.abs((Math.round(-block.position.x +10)/2))))] = block.id;
		}
		for(Switch block:switches) {
			levelArray[ ((int) (Math.abs((Math.round(block.position.z +10)/2))))][ ((int) (Math.abs((Math.round(block.position.y +10)/2))))][ ((int) (Math.abs((Math.round(-block.position.x +10)/2))))] = block.id;
		}
		
		levelCode = Resources.getInstance().encode(levelArray);
		if( Resources.getInstance().customLevels.size<Resources.getInstance().currentlevel) {
			Resources.getInstance().customLevels.add(levelCode);
		} else {
			Resources.getInstance().customLevels.set(Resources.getInstance().currentlevel-1,levelCode);
		}
		int i = 1;
		Resources.getInstance().prefs.putInteger("customLevel_count",Resources.getInstance().customLevels.size);
		for(String s:Resources.getInstance().customLevels) {
			Resources.getInstance().prefs.putString("customLevel_"+i,s);
			++i;
		}		
		Resources.getInstance().prefs.flush();
		return true;
	}
	
	private boolean canSaveCheck() {
		if(player == null) {
			errorReason = 0;
			return false;
		}
		if(target == null) {
			errorReason = 1;
			return false;
		}
		
		for(Portal portal:portals) {
			boolean found = false;
			for(Portal portal2:portals) {
				if(portal.id==-portal2.id) {
					found = true;
				}
			}
			if(found ==false) {
				errorReason = 2;
				return false;
			}
		}
		
		return true;
	}
	
	private void loadLevel() {
		renderObjects.clear();
		blocks.clear();
		portals.clear();
		movableBlocks.clear();
		switchblocks.clear();
		switches.clear();
		
		loadLevel(Resources.getInstance().decode(levelCode));
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
		if(levelnumber <=  Resources.getInstance().customLevels.size) {
			level = Resources.getInstance().decode(Resources.getInstance().customLevels.get(levelnumber-1));
		} else {
			level = Resources.getInstance().decode(Resources.getInstance().blankLevel);
		}

		loadLevel(level);			
	}

	private void loadLevel(int[][][] level) {
		
		int MAX = level.length;
		
		int z = 0, y = 0, x = 0;
		for (z = 0; z < MAX; z++) {
			for (y = 0; y < MAX; y++) {
				for (x = 0; x < MAX; x++) {
					if (level[z][y][x] == 1) {
						blocks.add(new Block(new Vector3(10f - (x * 2), -10f + (y * 2), -10f + (z * 2))));
					}
					if (level[z][y][x] == 2) {
						player = new Player();
						player.position.x = 10f - (x * 2);
						player.position.y = -10f + (y * 2);
						player.position.z = -10f + (z * 2);
					}
					if (level[z][y][x] == 3) {
						target = new Target();
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
		
		renderObjects.add(editorBlock);
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

		for(int i = 0; i<portals.size; i++) {
			for(Portal q : portals) {
				if(portals.get(i).id == -q.id) {
					portals.get(i).correspondingPortal = q;
				}
			}
		}
	}

	private void reset() {
		player.collideAnimation = 1;
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
		port=new Portal();

		loadLevel();
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
		
		for(MovableBlock m : movableBlocks) {
			if(m.isMoving) {
				m.position.add(m.direction.x * delta * 10f, m.direction.y * delta * 10f, m.direction.z * delta * 10f);
			}
		}		
		collisionTest();
		
		sortScene();
		
		// render scene again
		renderScene();
		renderButtons();

		if(Resources.getInstance().bloomOnOff) {
			frameBuffer.begin();
			renderScene();
			renderButtons();
			frameBuffer.end();
	
			// PostProcessing
			Gdx.gl.glDisable(GL20.GL_CULL_FACE);
			Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
			Gdx.gl.glDisable(GL20.GL_BLEND);
	
			bloomShader.begin();
			
			frameBuffer.getColorBufferTexture().bind(0);
	
			bloomShader.setUniformi("sTexture", 0);
			bloomShader.setUniformf("bloomFactor", Helper.map((MathUtils.sin(startTime * 3f) * 0.5f) + 0.5f,0,1,0.50f,0.60f));
	
			frameBufferVert.begin();
			bloomShader.setUniformf("TexelOffsetX", Resources.getInstance().m_fTexelOffset);
			bloomShader.setUniformf("TexelOffsetY", Resources.getInstance().m_fTexelOffset);			
			quadModel.render(bloomShader, GL20.GL_TRIANGLE_STRIP);
			frameBufferVert.end();
	
//			frameBufferVert.getColorBufferTexture().bind(0);
//	
//			frameBuffer.begin();
//			bloomShader.setUniformf("TexelOffsetX", 0.0f);
//			bloomShader.setUniformf("TexelOffsetY", Resources.getInstance().m_fTexelOffset);
//			quadModel.render(bloomShader, GL20.GL_TRIANGLE_STRIP);			
//			frameBuffer.end();
//			
//			frameBuffer.getColorBufferTexture().bind(0);
//	
//			frameBufferVert.begin();
//			bloomShader.setUniformf("TexelOffsetX", 0.0f);
//			bloomShader.setUniformf("TexelOffsetY", 0.0f);			
//			quadModel.render(bloomShader, GL20.GL_TRIANGLE_STRIP);
//			frameBufferVert.end();
			
			bloomShader.end();
			
			batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
			batch.getProjectionMatrix().setToOrtho2D(0, 0, Resources.getInstance().m_i32TexSize, Resources.getInstance().m_i32TexSize);
			batch.begin();
			batch.draw(frameBufferVert.getColorBufferTexture(),0,0);
			batch.end();
			batch.getProjectionMatrix().setToOrtho2D(0, 0, 800,480);
			
			if(Gdx.graphics.getBufferFormat().coverageSampling) {
				Gdx.gl.glClear(GL20.GL_COVERAGE_BUFFER_BIT_NV);
				Gdx.graphics.getGL20().glColorMask(false, false, false, false);			
				renderScene();
				renderButtons();
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
		if(mode==1) {
		if(selectedMenuItem==0) {
			timeAttackFont.draw(fontbatch, "a", 40, 60);
		} else {
			font.draw(fontbatch, "a", 40, 60);
		}
		if(selectedMenuItem==1) {
			timeAttackFont.draw(fontbatch, "s", 115, 60);
		} else {
			font.draw(fontbatch, "s", 115, 60);
		}
		if(selectedMenuItem==2) {
			timeAttackFont.draw(fontbatch, "d", 190, 60);
		} else {
			font.draw(fontbatch, "d", 190, 60);
		}
		if(selectedMenuItem==3) {
			timeAttackFont.draw(fontbatch, "w", 115, 135);
		} else {
			font.draw(fontbatch, "w", 115, 135);
		}
		if(selectedMenuItem==4) {
			timeAttackFont.drawMultiLine(fontbatch, "change\nblock", 600, 85);
		} else {
			font.drawMultiLine(fontbatch, "change\nblock", 600, 85);
		}
		}
		if(selectedMenuItem==5) {
			if(mode==1) {
				timeAttackFont.draw(fontbatch, "play", 42, 420);
			} else {
				timeAttackFont.draw(fontbatch, "edit", 42, 420);
			}
		} else {
			if (mode == 1) {
				font.draw(fontbatch, "play", 42, 420);
			} else {
				font.draw(fontbatch, "edit", 42, 420);
			}
		}
		
		if (showError > 0) {
			showError = Math.max(0, showError-delta);
			if(errorReason==0) {
				font.drawMultiLine(fontbatch, "Could't save\nNo player", 150, 270);
			} else if(errorReason==1) {
				font.drawMultiLine(fontbatch, "Could't save\nNo exit", 150,250);
			} else if(errorReason==2) {
				font.drawMultiLine(fontbatch, "Could't save\nNo portal exit", 150, 270);
			}
		}

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

			if(!(renderable instanceof EditorBlock)) {
				tmp.setToScaling(0.95f, 0.95f, 0.95f);
				model.mul(tmp);
			}
			
			model.getTranslation(position);
			
			renderable.model.set(model);
			
			renderable.sortPosition = cam.position.dst(position);
		}
		renderObjects.sort();
	}
	
	private void renderButtons() {

		Gdx.gl.glEnable(GL20.GL_CULL_FACE);

		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		transShader.begin();
		transShader.setUniformMatrix("VPMatrix", camMenu.combined);

		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		if(mode==1) {
		{
			// render Button 1
			tmp.idt();
			model.idt();

			tmp.setToTranslation(-400.0f, -240.0f, 0.0f);
			model.mul(tmp);
			
			tmp.setToScaling(30f, 30, 1);
			model.mul(tmp);

			tmp.setToTranslation(2f, 2f, -1);
			model.mul(tmp);

			transShader.setUniformMatrix("MMatrix", model);

			if(selectedMenuItem==0) {
				transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0],Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2],Resources.getInstance().blockEdgeColor[3]+0.2f);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", Resources.getInstance().blockColor[0],Resources.getInstance().blockColor[1],Resources.getInstance().blockColor[2],Resources.getInstance().blockColor[3]+0.2f);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
			} else {
				transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0],Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2],Resources.getInstance().blockEdgeColor[3]-0.3f);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", Resources.getInstance().blockColor[0],Resources.getInstance().blockColor[1],Resources.getInstance().blockColor[2],Resources.getInstance().blockColor[3]-0.2f);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
			}
		}

		{
			// render Button 2
			tmp.idt();
			model.idt();

			tmp.setToTranslation(-400.0f, -240.0f, 0.0f);
			model.mul(tmp);
			
			tmp.setToScaling(30f, 30, 1);
			model.mul(tmp);

			tmp.setToTranslation(4.5f, 2f, -1);
			model.mul(tmp);
			
			transShader.setUniformMatrix("MMatrix", model);

			if(selectedMenuItem==1) {
				transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0],Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2],Resources.getInstance().blockEdgeColor[3]+0.2f);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", Resources.getInstance().blockColor[0],Resources.getInstance().blockColor[1],Resources.getInstance().blockColor[2],Resources.getInstance().blockColor[3]+0.2f);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
			} else {
				transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0],Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2],Resources.getInstance().blockEdgeColor[3]-0.3f);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", Resources.getInstance().blockColor[0],Resources.getInstance().blockColor[1],Resources.getInstance().blockColor[2],Resources.getInstance().blockColor[3]-0.2f);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
			}
		}

		{
			// render Button 3
			tmp.idt();
			model.idt();

			tmp.setToTranslation(-400.0f, -240.0f, 0.0f);
			model.mul(tmp);
			
			tmp.setToScaling(30f, 30, 1);
			model.mul(tmp);

			tmp.setToTranslation(7.0f, 2f, -1);
			model.mul(tmp);
			
			transShader.setUniformMatrix("MMatrix", model);

			if(selectedMenuItem==2) {
				transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0],Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2],Resources.getInstance().blockEdgeColor[3]+0.2f);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", Resources.getInstance().blockColor[0],Resources.getInstance().blockColor[1],Resources.getInstance().blockColor[2],Resources.getInstance().blockColor[3]+0.2f);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
			} else {
				transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0],Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2],Resources.getInstance().blockEdgeColor[3]-0.3f);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", Resources.getInstance().blockColor[0],Resources.getInstance().blockColor[1],Resources.getInstance().blockColor[2],Resources.getInstance().blockColor[3]-0.2f);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
			}
		}

		{
			// render Button 4
			tmp.idt();
			model.idt();

			tmp.setToTranslation(-400.0f, -240.0f, 0.0f);
			model.mul(tmp);
			
			tmp.setToScaling(30f, 30, 1);
			model.mul(tmp);

			tmp.setToTranslation(4.5f, 4.5f, -1);
			model.mul(tmp);

			transShader.setUniformMatrix("MMatrix", model);

			if(selectedMenuItem==3) {
				transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0],Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2],Resources.getInstance().blockEdgeColor[3]+0.2f);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", Resources.getInstance().blockColor[0],Resources.getInstance().blockColor[1],Resources.getInstance().blockColor[2],Resources.getInstance().blockColor[3]+0.2f);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
			} else {
				transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0],Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2],Resources.getInstance().blockEdgeColor[3]-0.3f);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", Resources.getInstance().blockColor[0],Resources.getInstance().blockColor[1],Resources.getInstance().blockColor[2],Resources.getInstance().blockColor[3]-0.2f);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
			}
		}
		
		{
			// render Button 5
			tmp.idt();
			model.idt();

			tmp.setToTranslation(-400.0f, -240.0f, 0.0f);
			model.mul(tmp);
			
			tmp.setToScaling(100f,40, 1);
			model.mul(tmp);

			tmp.setToTranslation(6.9f, 1.72f, -1);
			model.mul(tmp);

			transShader.setUniformMatrix("MMatrix", model);

			if(selectedMenuItem==4) {
				transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0],Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2],Resources.getInstance().blockEdgeColor[3]+0.2f);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", Resources.getInstance().blockColor[0],Resources.getInstance().blockColor[1],Resources.getInstance().blockColor[2],Resources.getInstance().blockColor[3]+0.2f);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
			} else {
				transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0],Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2],Resources.getInstance().blockEdgeColor[3]-0.3f);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", Resources.getInstance().blockColor[0],Resources.getInstance().blockColor[1],Resources.getInstance().blockColor[2],Resources.getInstance().blockColor[3]-0.2f);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
			}
		}
		}
		
		{
			// render Button 6
			tmp.idt();
			model.idt();

			tmp.setToTranslation(-400.0f, -240.0f, 0.0f);
			model.mul(tmp);
			
			tmp.setToScaling(80f, 30, 1);
			model.mul(tmp);

			tmp.setToTranslation(1.4f, 14f, -1);
			model.mul(tmp);

			transShader.setUniformMatrix("MMatrix", model);

			if(selectedMenuItem==5) {
				transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0],Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2],Resources.getInstance().blockEdgeColor[3]+0.2f);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", Resources.getInstance().blockColor[0],Resources.getInstance().blockColor[1],Resources.getInstance().blockColor[2],Resources.getInstance().blockColor[3]+0.2f);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
			} else {
				transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0],Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2],Resources.getInstance().blockEdgeColor[3]-0.3f);
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
	
				transShader.setUniformf("a_color", Resources.getInstance().blockColor[0],Resources.getInstance().blockColor[1],Resources.getInstance().blockColor[2],Resources.getInstance().blockColor[3]-0.2f);
				blockModel.render(transShader, GL20.GL_TRIANGLES);
			}
		}

		transShader.end();
	}

	private void renderScene() {		
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		
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

			tmp.setToScaling(20.5f, 20.5f, 20.5f);
			model.mul(tmp);

			tmp.setToRotation(xAxis, angleX + angleXBack);
			model.mul(tmp);
			tmp.setToRotation(yAxis, angleY + angleYBack);
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

			tmp.setToRotation(xAxis, angleX);
			model.mul(tmp);
			tmp.setToRotation(yAxis, angleY);
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
			
			//render editor position
			if(mode == 1 && renderable instanceof EditorBlock) {
				model.set(renderable.model);
	
				transShader.setUniformMatrix("MMatrix", model);
	
				transShader.setUniformf("a_color", Resources.getInstance().editorBlockColor[0], Resources.getInstance().editorBlockColor[1], Resources.getInstance().editorBlockColor[2], Resources.getInstance().editorBlockColor[3]+ renderable.collideAnimation );
				blockModel.render(transShader, GL20.GL_TRIANGLES);
				
				transShader.setUniformf("a_color",Resources.getInstance().editorBlockEdgeColor[0], Resources.getInstance().editorBlockEdgeColor[1],Resources.getInstance().editorBlockEdgeColor[2], Resources.getInstance().editorBlockEdgeColor[3] + renderable.collideAnimation );
				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
			}		
			
			
			//render impact
			if(renderable.isCollidedAnimation == true && renderable.collideAnimation == 0) {
				renderable.collideAnimation = 1.0f;
			}
			if(renderable instanceof EditorBlock && renderable.collideAnimation==0) {
				renderable.collideAnimation = 0.5f;
			}
			
			if(renderable.collideAnimation>0.0f) {
				if(renderable instanceof EditorBlock) {
					renderable.collideAnimation -= delta/5.f;
					renderable.collideAnimation = Math.max(0.0f, renderable.collideAnimation);
					if(renderable.collideAnimation == 0.0f) renderable.isCollidedAnimation = false;
				} else {
					renderable.collideAnimation -= delta*1.f;
					renderable.collideAnimation = Math.max(0.0f, renderable.collideAnimation);
					if(renderable.collideAnimation == 0.0f) renderable.isCollidedAnimation = false;
				}
			}
			
			//render switchblock fade out/in
			if( renderable instanceof SwitchableBlock) {
				if(((SwitchableBlock) renderable).isSwitchAnimation == true && ((SwitchableBlock) renderable).isSwitched == true && ((SwitchableBlock) renderable).switchAnimation == 0) {
					((SwitchableBlock) renderable).switchAnimation = 0.0f;
				}
				if(((SwitchableBlock) renderable).isSwitchAnimation == true && ((SwitchableBlock) renderable).isSwitched == false && ((SwitchableBlock) renderable).switchAnimation == 1) {
					((SwitchableBlock) renderable).switchAnimation = 1.0f;
				}
				if (((SwitchableBlock) renderable).isSwitchAnimation == true) {
					if (!((SwitchableBlock) renderable).isSwitched) {
						((SwitchableBlock) renderable).switchAnimation -= delta * 1.f;
						((SwitchableBlock) renderable).switchAnimation = Math.max(0.0f, ((SwitchableBlock) renderable).switchAnimation);
						if (((SwitchableBlock) renderable).switchAnimation == 0.0f)
							((SwitchableBlock) renderable).isSwitchAnimation = false;
					} else {
						((SwitchableBlock) renderable).switchAnimation += delta * 1.f;
						((SwitchableBlock) renderable).switchAnimation = Math.min(1.0f, ((SwitchableBlock) renderable).switchAnimation);
						if (((SwitchableBlock) renderable).switchAnimation == 1.0f)
							((SwitchableBlock) renderable).isSwitchAnimation = false;
					}
				}

			}			
			
			if(renderable instanceof Block) {
				model.set(renderable.model);
	
				transShader.setUniformMatrix("MMatrix", model);
	
//				transShader.setUniformf("a_color", Resources.getInstance().blockColor[0]- (Helper.map(renderable.sortPosition,10,25,0,0.4f)), Resources.getInstance().blockColor[1], Resources.getInstance().blockColor[2] + (Helper.map(renderable.sortPosition,10,25,0,0.15f)), Resources.getInstance().blockColor[3]+ renderable.collideAnimation + (Helper.map(renderable.sortPosition,10,25,0.15f,-0.25f)));
//				blockModel.render(transShader, GL20.GL_TRIANGLES);
//				
//				transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0] - (Helper.map(renderable.sortPosition,10,25,0,0.4f)), Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2] + (Helper.map(renderable.sortPosition,10,25,0,0.15f)), Resources.getInstance().blockEdgeColor[3] + renderable.collideAnimation + (Helper.map(renderable.sortPosition,10,25,0.15f,-0.25f)));
//				wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
				
				transShader.setUniformf("a_color", Resources.getInstance().blockColor[0], Resources.getInstance().blockColor[1], Resources.getInstance().blockColor[2], Resources.getInstance().blockColor[3]+ renderable.collideAnimation );
				blockModel.render(transShader, GL20.GL_TRIANGLES);
				
				transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0], Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2], Resources.getInstance().blockEdgeColor[3] + renderable.collideAnimation );
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
				if(!((SwitchableBlock) renderable).isSwitched || ((SwitchableBlock) renderable).isSwitchAnimation == true) {	
					model.set(renderable.model);
		
					SwitchableBlock tmpSwitchb = (SwitchableBlock) renderable;
					
					switch (Math.abs(tmpSwitchb.id)) {
					case 10:

						tmp.setToScaling(0.3f, 0.3f, 0.3f);
						model.mul(tmp);
		
						transShader.setUniformMatrix("MMatrix", model);
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2] , Resources.getInstance().switchBlockColor[3]+ renderable.collideAnimation  - ((SwitchableBlock) renderable).switchAnimation);
						playerModel.render(transShader, GL20.GL_TRIANGLES);
						
						tmp.setToScaling(3f, 3f, 3f);
						model.mul(tmp);
						
						transShader.setUniformMatrix("MMatrix", model);
						
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2], Resources.getInstance().switchBlockColor[3]+ renderable.collideAnimation  - ((SwitchableBlock) renderable).switchAnimation);
						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
			
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockEdgeColor[0] * (Math.abs(((SwitchableBlock)renderable).id)), Resources.getInstance().switchBlockEdgeColor[1]* (Math.abs(((SwitchableBlock)renderable).id)), Resources.getInstance().switchBlockEdgeColor[2] * (Math.abs(((SwitchableBlock)renderable).id)), Resources.getInstance().switchBlockEdgeColor[3] + renderable.collideAnimation - ((SwitchableBlock) renderable).switchAnimation);
						blockModel.render(transShader, GL20.GL_TRIANGLES);
						break;
					case 12:

						tmp.setToScaling(0.3f, 0.3f, 0.3f);
						model.mul(tmp);
		
						transShader.setUniformMatrix("MMatrix", model);
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2] , Resources.getInstance().switchBlockColor[3]+ renderable.collideAnimation  - ((SwitchableBlock) renderable).switchAnimation);
						blockModel.render(transShader, GL20.GL_TRIANGLES);
						
						tmp.setToScaling(3f, 3f, 3f);
						model.mul(tmp);
						
						transShader.setUniformMatrix("MMatrix", model);
						
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2], Resources.getInstance().switchBlockColor[3]+ renderable.collideAnimation  - ((SwitchableBlock) renderable).switchAnimation);
						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
			
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockEdgeColor[0] * (Math.abs(((SwitchableBlock)renderable).id)), Resources.getInstance().switchBlockEdgeColor[1]* (Math.abs(((SwitchableBlock)renderable).id)), Resources.getInstance().switchBlockEdgeColor[2] * (Math.abs(((SwitchableBlock)renderable).id)), Resources.getInstance().switchBlockEdgeColor[3] + renderable.collideAnimation - ((SwitchableBlock) renderable).switchAnimation);
						blockModel.render(transShader, GL20.GL_TRIANGLES);
						break;
					case 13:

						tmp.setToScaling(0.3f, 0.3f, 0.3f);
						model.mul(tmp);
		
						transShader.setUniformMatrix("MMatrix", model);
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2] , Resources.getInstance().switchBlockColor[3]+ renderable.collideAnimation  - ((SwitchableBlock) renderable).switchAnimation);
						coneModel.render(transShader, GL20.GL_TRIANGLES);
						
						tmp.setToScaling(3f, 3f, 3f);
						model.mul(tmp);
						
						transShader.setUniformMatrix("MMatrix", model);
						
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2], Resources.getInstance().switchBlockColor[3]+ renderable.collideAnimation  - ((SwitchableBlock) renderable).switchAnimation);
						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
			
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockEdgeColor[0] * (Math.abs(((SwitchableBlock)renderable).id)), Resources.getInstance().switchBlockEdgeColor[1]* (Math.abs(((SwitchableBlock)renderable).id)), Resources.getInstance().switchBlockEdgeColor[2] * (Math.abs(((SwitchableBlock)renderable).id)), Resources.getInstance().switchBlockEdgeColor[3] + renderable.collideAnimation - ((SwitchableBlock) renderable).switchAnimation);
						blockModel.render(transShader, GL20.GL_TRIANGLES);
						break;
					default:

						tmp.setToScaling(0.3f, 0.3f, 0.3f);
						model.mul(tmp);
		
						transShader.setUniformMatrix("MMatrix", model);
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2] , Resources.getInstance().switchBlockColor[3]+ renderable.collideAnimation  - ((SwitchableBlock) renderable).switchAnimation);
						playerModel.render(transShader, GL20.GL_TRIANGLES);
						
						tmp.setToScaling(3f, 3f, 3f);
						model.mul(tmp);
						
						transShader.setUniformMatrix("MMatrix", model);
						
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2], Resources.getInstance().switchBlockColor[3]+ renderable.collideAnimation  - ((SwitchableBlock) renderable).switchAnimation);
						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
			
						transShader.setUniformf("a_color", Resources.getInstance().switchBlockEdgeColor[0] * (Math.abs(((SwitchableBlock)renderable).id)), Resources.getInstance().switchBlockEdgeColor[1]* (Math.abs(((SwitchableBlock)renderable).id)), Resources.getInstance().switchBlockEdgeColor[2] * (Math.abs(((SwitchableBlock)renderable).id)), Resources.getInstance().switchBlockEdgeColor[3] + renderable.collideAnimation - ((SwitchableBlock) renderable).switchAnimation);
						blockModel.render(transShader, GL20.GL_TRIANGLES);
						break;
					}
				}
			}
			
			// render switches
			if(renderable instanceof Switch) {
				model.set(renderable.model);	
				
				Switch tmpSwitch = (Switch) renderable;
				
				switch (Math.abs(tmpSwitch.id)) {
				case 10:

					tmp.setToScaling(0.3f, 0.3f, 0.3f);
					model.mul(tmp);
	
					transShader.setUniformMatrix("MMatrix", model);
					transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2] , Resources.getInstance().switchBlockColor[3]+ renderable.collideAnimation);
					playerModel.render(transShader, GL20.GL_TRIANGLES);
					
					tmp.setToScaling(2.0f, 2.0f, 2.0f);
					model.mul(tmp);
	
					//render hull			
					transShader.setUniformMatrix("MMatrix", model);
					transShader.setUniformf("a_color", Resources.getInstance().switchBlockEdgeColor[0],Resources.getInstance().switchBlockEdgeColor[1], Resources.getInstance().switchBlockEdgeColor[2] , Resources.getInstance().switchBlockEdgeColor[3]+ renderable.collideAnimation);
					playerModel.render(transShader, GL20.GL_TRIANGLES);
					break;
				case 12:

					tmp.setToScaling(0.3f, 0.3f, 0.3f);
					model.mul(tmp);
	
					transShader.setUniformMatrix("MMatrix", model);
					transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0] ,Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2] , Resources.getInstance().switchBlockColor[3]+ renderable.collideAnimation);
					blockModel.render(transShader, GL20.GL_TRIANGLES);
					
					tmp.setToScaling(2.0f, 2.0f, 2.0f);
					model.mul(tmp);
	
					//render hull			
					transShader.setUniformMatrix("MMatrix", model);
					transShader.setUniformf("a_color", Resources.getInstance().switchBlockEdgeColor[0],Resources.getInstance().switchBlockEdgeColor[1], Resources.getInstance().switchBlockEdgeColor[2] , Resources.getInstance().switchBlockEdgeColor[3]+ renderable.collideAnimation);
					playerModel.render(transShader, GL20.GL_TRIANGLES);
					break;
				case 13:

					tmp.setToScaling(0.3f, 0.3f, 0.3f);
					model.mul(tmp);
	
					transShader.setUniformMatrix("MMatrix", model);
					transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2] , Resources.getInstance().switchBlockColor[3]+ renderable.collideAnimation);
					coneModel.render(transShader, GL20.GL_TRIANGLES);
					
					tmp.setToScaling(2.0f, 2.0f, 2.0f);
					model.mul(tmp);
	
					//render hull			
					transShader.setUniformMatrix("MMatrix", model);
					transShader.setUniformf("a_color", Resources.getInstance().switchBlockEdgeColor[0],Resources.getInstance().switchBlockEdgeColor[1], Resources.getInstance().switchBlockEdgeColor[2] , Resources.getInstance().switchBlockEdgeColor[3]+ renderable.collideAnimation);
					playerModel.render(transShader, GL20.GL_TRIANGLES);
					break;
				default:

					tmp.setToScaling(0.3f, 0.3f, 0.3f);
					model.mul(tmp);
		
					transShader.setUniformMatrix("MMatrix", model);
					transShader.setUniformf("a_color", Resources.getInstance().switchBlockColor[0],Resources.getInstance().switchBlockColor[1], Resources.getInstance().switchBlockColor[2], Resources.getInstance().switchBlockColor[3]+ renderable.collideAnimation);
					playerModel.render(transShader, GL20.GL_TRIANGLES);
						
					tmp.setToScaling(2.0f, 2.0f, 2.0f);
					model.mul(tmp);
		
					//render hull			
					transShader.setUniformMatrix("MMatrix", model);
					transShader.setUniformf("a_color", Resources.getInstance().switchBlockEdgeColor[0],Resources.getInstance().switchBlockEdgeColor[1], Resources.getInstance().switchBlockEdgeColor[2] , Resources.getInstance().switchBlockEdgeColor[3]+ renderable.collideAnimation);
					playerModel.render(transShader, GL20.GL_TRIANGLES);
					break;
				}
				
			}
			
			// render Player
			if(player!= null && renderable instanceof Player) {
				model.set(renderable.model);	
				
				tmp.setToRotation(xAxis, angleXBack);
				model.mul(tmp);
				tmp.setToRotation(yAxis, angleYBack);
				model.mul(tmp);

				tmp.setToScaling(0.5f, 0.5f, 0.5f);
				model.mul(tmp);

				transShader.setUniformMatrix("MMatrix", model);
				transShader.setUniformf("a_color",Resources.getInstance().playerColor[0], Resources.getInstance().playerColor[1], Resources.getInstance().playerColor[2], Resources.getInstance().playerColor[3]  + renderable.collideAnimation);
				playerModel.render(transShader, GL20.GL_TRIANGLES);
				
				tmp.setToScaling(2.0f - (renderable.collideAnimation), 2.0f - (renderable.collideAnimation), 2.0f  - (renderable.collideAnimation));
				model.mul(tmp);

				//render hull			
				transShader.setUniformMatrix("MMatrix", model);
				transShader.setUniformf("a_color",Resources.getInstance().playerEdgeColor[0], Resources.getInstance().playerEdgeColor[1], Resources.getInstance().playerEdgeColor[2], Resources.getInstance().playerEdgeColor[3]  + renderable.collideAnimation);
				
				playerModel.render(transShader, GL20.GL_LINE_STRIP);
				
				//TODO add animations
				playerModel.render(transShader, GL20.GL_LINE_STRIP, 0, (int) (playerModel.getNumVertices()-(renderable.collideAnimation*playerModel.getNumVertices())));
				
//				//render direction indicator
//				model.set(renderable.model);
//				((Player) renderable).setDirection();
//				tmp.setToTranslation(((Player) renderable).direction);
//				model.mul(tmp);
//				transShader.setUniformMatrix("MMatrix", model);
//				transShader.setUniformf("a_color",Resources.getInstance().playerEdgeColor[0], Resources.getInstance().playerEdgeColor[1], Resources.getInstance().playerEdgeColor[2], Resources.getInstance().playerEdgeColor[3]  + renderable.collideAnimation);
//				sphereSliceModel.render(transShader, GL20.GL_LINE_STRIP);
			}
			
			// render Portals
			if(renderable instanceof Portal) {
				if(renderable.position.x != -11) {
					// render Portal
					Portal tmpPortal = (Portal) renderable;
					
					switch (Math.abs(tmpPortal.id)) {
					case 4:
						model.set(renderable.model);

						tmp.setToScaling(0.4f, 0.4f, 0.4f);
						model.mul(tmp);	
						
						transShader.setUniformMatrix("MMatrix", model);
						
						transShader.setUniformf("a_color", Resources.getInstance().portalColor[0], Resources.getInstance().portalColor[1], Resources.getInstance().portalColor[2], Resources.getInstance().portalColor[3]  + renderable.collideAnimation);
						playerModel.render(transShader, GL20.GL_TRIANGLES);
						
						model.set(renderable.model);
//						//render hull		
//						transShader.setUniformMatrix("MMatrix", model);
//						transShader.setUniformf("a_color", Resources.getInstance().portalEdgeColor[0],Resources.getInstance().portalEdgeColor[1] , Resources.getInstance().portalEdgeColor[2], Resources.getInstance().portalEdgeColor[3]  + renderable.collideAnimation);
//						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						
						tmp.setToRotation(xAxis, angleXFront);
						model.mul(tmp);
						tmp.setToRotation(yAxis, angleYFront);
						model.mul(tmp);
						
						tmp.setToScaling(0.8f, 0.8f, 0.8f);
						model.mul(tmp);
						transShader.setUniformMatrix("MMatrix", model);
						transShader.setUniformf("a_color",Resources.getInstance().portalColor[0], Resources.getInstance().portalColor[1], Resources.getInstance().portalColor[2], Resources.getInstance().portalColor[3]  + renderable.collideAnimation);
						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						break;
						
					case 5:
						model.set(renderable.model);

						tmp.setToScaling(0.4f, 0.4f, 0.4f);
						model.mul(tmp);	
						
						transShader.setUniformMatrix("MMatrix", model);
						
						transShader.setUniformf("a_color", Resources.getInstance().portalColor2[0], Resources.getInstance().portalColor2[1], Resources.getInstance().portalColor2[2], Resources.getInstance().portalColor2[3]  + renderable.collideAnimation);
						playerModel.render(transShader, GL20.GL_TRIANGLES);
						
						model.set(renderable.model);
						//render hull		
//						transShader.setUniformMatrix("MMatrix", model);
//						transShader.setUniformf("a_color", Resources.getInstance().portalEdgeColor[0],Resources.getInstance().portalEdgeColor[1] , Resources.getInstance().portalEdgeColor[2], Resources.getInstance().portalEdgeColor[3]  + renderable.collideAnimation);
//						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						
						tmp.setToRotation(xAxis, angleXFront);
						model.mul(tmp);
						tmp.setToRotation(yAxis, angleYFront);
						model.mul(tmp);
						
						tmp.setToScaling(0.8f, 0.8f, 0.8f);
						model.mul(tmp);
						transShader.setUniformMatrix("MMatrix", model);
						transShader.setUniformf("a_color",Resources.getInstance().portalColor2[0], Resources.getInstance().portalColor2[1], Resources.getInstance().portalColor2[2], Resources.getInstance().portalColor2[3]  + renderable.collideAnimation);
						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						break;
						
					case 6:
						model.set(renderable.model);

						tmp.setToScaling(0.4f, 0.4f, 0.4f);
						model.mul(tmp);	
						
						transShader.setUniformMatrix("MMatrix", model);
						
						transShader.setUniformf("a_color", Resources.getInstance().portalColor3[0], Resources.getInstance().portalColor3[1], Resources.getInstance().portalColor3[2], Resources.getInstance().portalColor3[3]  + renderable.collideAnimation);
						playerModel.render(transShader, GL20.GL_TRIANGLES);
						
						model.set(renderable.model);
						//render hull		
//						transShader.setUniformMatrix("MMatrix", model);
//						transShader.setUniformf("a_color", Resources.getInstance().portalEdgeColor[0],Resources.getInstance().portalEdgeColor[1] , Resources.getInstance().portalEdgeColor[2], Resources.getInstance().portalEdgeColor[3]  + renderable.collideAnimation);
//						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						
						tmp.setToRotation(xAxis, angleXFront);
						model.mul(tmp);
						tmp.setToRotation(yAxis, angleYFront);
						model.mul(tmp);
						
						tmp.setToScaling(0.8f, 0.8f, 0.8f);
						model.mul(tmp);
						transShader.setUniformMatrix("MMatrix", model);
						transShader.setUniformf("a_color",Resources.getInstance().portalColor3[0], Resources.getInstance().portalColor3[1], Resources.getInstance().portalColor3[2], Resources.getInstance().portalColor3[3]  + renderable.collideAnimation);
						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						break;
						
					case 7:
						model.set(renderable.model);

						tmp.setToScaling(0.4f, 0.4f, 0.4f);
						model.mul(tmp);	
						
						transShader.setUniformMatrix("MMatrix", model);
						
						transShader.setUniformf("a_color", Resources.getInstance().portalColor4[0], Resources.getInstance().portalColor4[1], Resources.getInstance().portalColor4[2], Resources.getInstance().portalColor4[3]  + renderable.collideAnimation);
						playerModel.render(transShader, GL20.GL_TRIANGLES);
						
						model.set(renderable.model);
						//render hull		
//						transShader.setUniformMatrix("MMatrix", model);
//						transShader.setUniformf("a_color", Resources.getInstance().portalEdgeColor[0],Resources.getInstance().portalEdgeColor[1] , Resources.getInstance().portalEdgeColor[2], Resources.getInstance().portalEdgeColor[3]  + renderable.collideAnimation);
//						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						
						tmp.setToRotation(xAxis, angleXFront);
						model.mul(tmp);
						tmp.setToRotation(yAxis, angleYFront);
						model.mul(tmp);
						
						tmp.setToScaling(0.8f, 0.8f, 0.8f);
						model.mul(tmp);
						transShader.setUniformMatrix("MMatrix", model);
						transShader.setUniformf("a_color",Resources.getInstance().portalColor4[0], Resources.getInstance().portalColor4[1], Resources.getInstance().portalColor4[2], Resources.getInstance().portalColor4[3]  + renderable.collideAnimation);
						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						break;
						
					case 8:
						model.set(renderable.model);

						tmp.setToScaling(0.4f, 0.4f, 0.4f);
						model.mul(tmp);	
						
						transShader.setUniformMatrix("MMatrix", model);
						
						transShader.setUniformf("a_color", Resources.getInstance().portalColor5[0], Resources.getInstance().portalColor5[1], Resources.getInstance().portalColor5[2], Resources.getInstance().portalColor5[3]  + renderable.collideAnimation);
						playerModel.render(transShader, GL20.GL_TRIANGLES);
						
						model.set(renderable.model);
						//render hull		
//						transShader.setUniformMatrix("MMatrix", model);
//						transShader.setUniformf("a_color", Resources.getInstance().portalEdgeColor[0],Resources.getInstance().portalEdgeColor[1] , Resources.getInstance().portalEdgeColor[2], Resources.getInstance().portalEdgeColor[3]  + renderable.collideAnimation);
//						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						
						tmp.setToRotation(xAxis, angleXFront);
						model.mul(tmp);
						tmp.setToRotation(yAxis, angleYFront);
						model.mul(tmp);
						
						tmp.setToScaling(0.8f, 0.8f, 0.8f);
						model.mul(tmp);
						transShader.setUniformMatrix("MMatrix", model);
						transShader.setUniformf("a_color",Resources.getInstance().portalColor5[0], Resources.getInstance().portalColor5[1], Resources.getInstance().portalColor5[2], Resources.getInstance().portalColor5[3]  + renderable.collideAnimation);
						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						break;

					default:
						model.set(renderable.model);

						tmp.setToScaling(0.4f, 0.4f, 0.4f);
						model.mul(tmp);	
						
						transShader.setUniformMatrix("MMatrix", model);
						
						transShader.setUniformf("a_color", Resources.getInstance().portalColor[0], Resources.getInstance().portalColor[1], Resources.getInstance().portalColor[2], Resources.getInstance().portalColor[3]  + renderable.collideAnimation);
						playerModel.render(transShader, GL20.GL_TRIANGLES);
						
						model.set(renderable.model);
						//render hull		
//						transShader.setUniformMatrix("MMatrix", model);
//						transShader.setUniformf("a_color", Resources.getInstance().portalEdgeColor[0],Resources.getInstance().portalEdgeColor[1] , Resources.getInstance().portalEdgeColor[2], Resources.getInstance().portalEdgeColor[3]  + renderable.collideAnimation);
//						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						
						tmp.setToRotation(xAxis, angleXFront);
						model.mul(tmp);
						tmp.setToRotation(yAxis, angleYFront);
						model.mul(tmp);
						
						tmp.setToScaling(0.8f, 0.8f, 0.8f);
						model.mul(tmp);
						transShader.setUniformMatrix("MMatrix", model);
						transShader.setUniformf("a_color",Resources.getInstance().portalColor[0], Resources.getInstance().portalColor[1], Resources.getInstance().portalColor[2], Resources.getInstance().portalColor[3]  + renderable.collideAnimation);
						wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
						break;
					}
				}
			}
				
			// render Target
			if(renderable instanceof Target) {
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

	private void collisionTest() {
		// collision
		if (player!= null && player.isMoving) {
			
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
					m.move(player.moveDirection.cpy());
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
				if(mode==0) {
					//Resources.getInstance().lives--;
				}
				reset();
			}

			if (win) {
				player.stop();
				if(mode == 1) {
					if(saveLevel()) {
						mode = 0;
					}
				} else {
					loadLevel();										
					mode = 1;
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
								mm.move(m.direction.cpy());
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
			if(player!=null && s.position.equals(player.position)) {
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
			saveLevel();
			game.setScreen(new LevelSelectScreen(game,2));
		}

		if (keycode == Input.Keys.SPACE) {
			movePlayer();
		}

		if (keycode == Input.Keys.Y) {
			reset();
			Resources.getInstance().time = 0;
		}
		
		if(keycode == Input.Keys.BACK) {
			saveLevel();
			game.setScreen(new LevelSelectScreen(game,2));
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
		
		if (keycode == Input.Keys.W) {
			if(mode==1) {
				if(angleX>45) {
					editorBlock.direction.set(0, 0, 1);
					editorBlock.direction.rot(new Matrix4().setToRotation(Vector3.X, -angleX));
					editorBlock.direction.rot(new Matrix4().setToRotation(Vector3.Y, -angleY));
					editorBlock.moveRight();
				} else if(angleX<-45) {
					editorBlock.direction.set(0, 0, -1);
					editorBlock.direction.rot(new Matrix4().setToRotation(Vector3.X, -angleX));
					editorBlock.direction.rot(new Matrix4().setToRotation(Vector3.Y, -angleY));
					editorBlock.moveRight();
				} else {
					editorBlock.up.set(0, 1, 0);
					editorBlock.up.rot(new Matrix4().setToRotation(Vector3.X, -angleX));
					editorBlock.up.rot(new Matrix4().setToRotation(Vector3.Y, -angleY));
					editorBlock.moveUp();
				}
			}
		}

		if (keycode == Input.Keys.S) {
			if(mode==1) {
				if(angleX>45) {
					editorBlock.direction.set(0,0,-1);
					editorBlock.direction.rot(new Matrix4().setToRotation(Vector3.X, -angleX));
					editorBlock.direction.rot(new Matrix4().setToRotation(Vector3.Y, -angleY));
					editorBlock.moveLeft();
				} else if(angleX<-45) {
					editorBlock.direction.set(0,0,1);
					editorBlock.direction.rot(new Matrix4().setToRotation(Vector3.X, -angleX));
					editorBlock.direction.rot(new Matrix4().setToRotation(Vector3.Y, -angleY));
					editorBlock.moveLeft();
				} else {
					editorBlock.up.set(0, -1, 0);
					editorBlock.up.rot(new Matrix4().setToRotation(Vector3.X, -angleX));
					editorBlock.up.rot(new Matrix4().setToRotation(Vector3.Y, -angleY));
					editorBlock.moveDown();
				}
			}
		}
		
		if (keycode == Input.Keys.A) {
			if(mode==1) {	
				if(angleX>45) {
					editorBlock.up.set(0, 1, 0);
					editorBlock.up.rot(new Matrix4().setToRotation(Vector3.X, -angleX));
					editorBlock.up.rot(new Matrix4().setToRotation(Vector3.Y, -angleY));
					editorBlock.moveDown();
				} else if(angleX<-45) {
					editorBlock.up.set(0, -1, 0);
					editorBlock.up.rot(new Matrix4().setToRotation(Vector3.X, -angleX));
					editorBlock.up.rot(new Matrix4().setToRotation(Vector3.Y, -angleY));
					editorBlock.moveDown();
				} else {
					editorBlock.direction.set(0,0,-1);
					editorBlock.direction.rot(new Matrix4().setToRotation(Vector3.X, -angleX));
					editorBlock.direction.rot(new Matrix4().setToRotation(Vector3.Y, -angleY));
					editorBlock.moveLeft();
				}
			}
		}
		
		if (keycode == Input.Keys.D) {
			if(mode==1) {
				if(angleX>45) {
					editorBlock.up.set(0, 1, 0);
					editorBlock.up.rot(new Matrix4().setToRotation(Vector3.X, -angleX));
					editorBlock.up.rot(new Matrix4().setToRotation(Vector3.Y, -angleY));
					editorBlock.moveUp();
				} else if(angleX<-45) {
					editorBlock.up.set(0, -1, 0);
					editorBlock.up.rot(new Matrix4().setToRotation(Vector3.X, -angleX));
					editorBlock.up.rot(new Matrix4().setToRotation(Vector3.Y, -angleY));
					editorBlock.moveUp();
				} else {
					editorBlock.direction.set(0, 0, -1);
					editorBlock.direction.rot(new Matrix4().setToRotation(Vector3.X, -angleX));
					editorBlock.direction.rot(new Matrix4().setToRotation(Vector3.Y, -angleY));
					editorBlock.moveRight();
				}
			}
		}
		
		if(keycode == Input.Keys.Q) {
			if(mode==1) {
			editorBlock.position.z -= 2;
			}
		}
		
		if(keycode == Input.Keys.E) {
			if(mode==1) {
			editorBlock.position.z += 2;
			}
		}		
		
		if(keycode == Input.Keys.R) {
			if(mode==1) {
				changeCurrentBlock();
			}
		}
		
		
		return false;
	}

	private void changeCurrentBlock() {
		int castTo = 0;
		Renderable castObject = null;
		for(Renderable renderable:renderObjects) {
			if((Math.abs(editorBlock.position.x  - renderable.position.x) < EPSILON)
				&& (Math.abs(editorBlock.position.y  - renderable.position.y) < EPSILON)
				&& (Math.abs(editorBlock.position.z  - renderable.position.z) < EPSILON)
				&& !(renderable instanceof EditorBlock)) {
				castObject = renderable;
				if(renderable instanceof Player) castTo = 1;
				if(renderable instanceof Target) castTo = 2;
				if(renderable instanceof Block) castTo = 3;
				if(renderable instanceof MovableBlock) castTo = 4;
				if(renderable instanceof Portal) castTo = 5;
				if(renderable instanceof Switch) castTo = 6;
				if(renderable instanceof SwitchableBlock) castTo = 7;
				break;
			}
		}
		if(castTo == 0) {
			if(player == null) {
				player = new Player();
				player.position.set(editorBlock.position);
			} else if (target == null){
				target = new Target();
				target.position.set(editorBlock.position);
			} else {
				blocks.add(new Block(new Vector3(editorBlock.position)));
//				Gdx.app.log("", "add new block");
			}
		} else {
			castTo++;
			castTo = castTo%8;
			
			int deleteObject = castTo-1;
			if (castTo == 1) {
				if(player!=null) {
					player = new Player();
					player.position.set(editorBlock.position);
				} else {
					castTo = 2;
				}
			} 
			if (castTo == 2) {
				deleteObject(castObject, deleteObject);
				if(target!=null) {
					target = new Target();
					target.position.set(editorBlock.position);
				} else {
					castTo = 3;
				}
			} 
			if (castTo == 3) {
				deleteObject(castObject, deleteObject);
				blocks.add(new Block(new Vector3(editorBlock.position)));
			} 
			if (castTo == 4) {
				deleteObject(castObject, deleteObject);					
				movableBlocks.add(new MovableBlock(new Vector3(editorBlock.position)));
			} 				
			if (castTo == 5) {
				deleteObject(castObject, deleteObject);
				
				//other portals placed?
				int portalIDT = 0;
				Portal portalT = null;
				for(Portal portal:portals) {
					if(Math.abs(portal.id)>Math.abs(portalIDT)) {
						portalIDT = portal.id;
						portalT = portal;
					}
				}
				if(portalT==null) {
					Portal portal = new Portal(4);
					portal.position.set(editorBlock.position);
					portals.add(portal);
//					Gdx.app.log("", "new Portal(4)");
				} else if(portalT.correspondingPortal != null && portalT.correspondingPortal.id==-8) {
					//max reached skip this
					castTo = 6;
					deleteObject = 4;
				} else if(portalT.correspondingPortal == null || !portals.contains(portalT.correspondingPortal, true)) {
					portalT.correspondingPortal = new Portal(-portalT.id);
					portalT.correspondingPortal.position.set(editorBlock.position);
					portalT.correspondingPortal.correspondingPortal = portalT;
					portals.add(portalT.correspondingPortal);
//					Gdx.app.log("", "Portal in Portal(" + portalT.id + ")");
				} else {
					Portal portal = new Portal(portalIDT+1);
					portal.position.set(editorBlock.position);
					portals.add(portal);
//					Gdx.app.log("", "new Portal(" + portal.id + ")");
				}
			} 
			if (castTo == 6) {
				deleteObject(castObject, deleteObject);
				
				//other switches placed?
				int switchIDT = 0;
				Switch switchT = null;
				for(Switch switch_:switches) {
					if(Math.abs(switch_.id)>Math.abs(switchIDT)) {
						switchIDT = switch_.id;
						switchT = switch_;
					}
				}
				if(switchT==null) {
					Switch switchBlock = new Switch(new Vector3(editorBlock.position));
					switchBlock.id = 10;
					switches.add(switchBlock);
//					Gdx.app.log("", "new Switch(10)");
				} else if(switchT.id==13) {
					//max reached skip this
					castTo = 7;
					deleteObject = 5;
				} else {
					Switch switchBlock = new Switch(new Vector3(editorBlock.position));
					if(Math.abs(switchT.id) == 10) {
						switchBlock.id = switchIDT+2;
					} else {
						switchBlock.id = switchIDT+1;
					}
					switches.add(switchBlock);
//					Gdx.app.log("", "new Switch(" + switchBlock.id + ")");
				}
			}
			if (castTo == 7) {
				deleteObject(castObject, deleteObject);
				
				//cast to last used switch
				int switchIDT = 0;
				Switch switchT = null;
				for(Switch switch_:switches) {
					if(Math.abs(switch_.id)>Math.abs(switchIDT)) {
						switchIDT = switch_.id;
						switchT = switch_;
					}
				}			
				if(switchT!=null) {
					SwitchableBlock switchBlock = new SwitchableBlock(new Vector3(editorBlock.position));
					switchBlock.id = -switchT.id;
					switchblocks.add(switchBlock);
					switchT.sBlocks.add(switchBlock);
//					Gdx.app.log("", "new SwitchBlock(" + switchBlock.id + ") in Switch(" + switchT.id + ")");
				}
			} 
			if(castTo == 0) {
				deleteObject(castObject, deleteObject);
			}


		}
		alterLevel();
	}

	private void deleteObject(Renderable castObject, int deleteObject) {
		if (deleteObject == 1) {
			player = null;
		} else if (deleteObject == 2) {
			target = null;
		} else if (deleteObject == 3) {
			blocks.removeValue((Block) castObject, true);			
		} else if (deleteObject == 4) {
			movableBlocks.removeValue((MovableBlock) castObject, true);
		} else if (deleteObject == 5) {
			portals.removeValue((Portal) castObject, true);
		} else if (deleteObject == 6) {
			switches.removeValue((Switch) castObject, true);
		} else if (deleteObject == -1) {
			switchblocks.removeValue((SwitchableBlock) castObject, true);
		}
	}

	private void movePlayer() {
		if (!player.isMoving) {
			player.direction.set(0, 0, -1);
			player.direction.rot(new Matrix4().setToRotation(xAxis, -angleX));
			player.direction.rot(new Matrix4().setToRotation(yAxis, -angleY));
			player.move();
			
		}
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
		
		if (!finished) {
			if(mode==1) {
			if (button1.contains(new Vector3(x, y, 0))) {
				editorBlock.direction.set(0, 0, -1);
				editorBlock.direction.rot(new Matrix4().setToRotation(Vector3.X, -angleX));
				editorBlock.direction.rot(new Matrix4().setToRotation(Vector3.Y, -angleY));
				editorBlock.moveLeft();
				return true;
			} else if (button2.contains(new Vector3(x, y, 0))) {
				editorBlock.up.set(0, -1, 0);
				editorBlock.up.rot(new Matrix4().setToRotation(Vector3.X, -angleX));
				editorBlock.up.rot(new Matrix4().setToRotation(Vector3.Y, -angleY));
				editorBlock.moveDown();
				return true;
			} else if (button3.contains(new Vector3(x, y, 0))) {
				editorBlock.direction.set(0, 0, -1);
				editorBlock.direction.rot(new Matrix4().setToRotation(Vector3.X, -angleX));
				editorBlock.direction.rot(new Matrix4().setToRotation(Vector3.Y, -angleY));
				editorBlock.moveRight();
				return true;
			} else if (button4.contains(new Vector3(x, y, 0))) {
				editorBlock.up.set(0, 1, 0);
				editorBlock.up.rot(new Matrix4().setToRotation(Vector3.X, -angleX));
				editorBlock.up.rot(new Matrix4().setToRotation(Vector3.Y, -angleY));
				editorBlock.moveUp();
				return true;
			} else if (button5.contains(new Vector3(x, y, 0))) {
				changeCurrentBlock();
				return true;
			} 
			}
			if (button6.contains(new Vector3(x, y, 0))) {
				if(mode == 1) {
					if(saveLevel()) {
						mode = 0;
					}
				} else {
					loadLevel();										
					mode = 1;
				}
				return true;
			} else {
				selectedMenuItem = -1;
			}
		}	
		

		if (pointers.size() > 1) {
			if (pointer == finger_one_pointer) {
				finger_one_pointer = -1;
			} else if (pointer == finger_two_pointer) {
				finger_two_pointer = -1;
			}
			
		} else {
			if(mode==0) {
			if (Math.abs(touchDistance) < 1.0f && touchTime < 0.3f && startTime > 0.5) {
				movePlayer();
			}
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

		touchDistance += ((x - touchStartX) / 5.f) + ((y - touchStartY) / 5.f);

		touchStartX = x;
		touchStartY = y;
		}

		return false;
	}

	@Override
	public boolean touchMoved(int x, int y) {
		x = (int) (x / (float) Gdx.graphics.getWidth() * 800);
		y = (int) (y / (float) Gdx.graphics.getHeight() * 480);
		
		if (button1.contains(new Vector3(x, y, 0))) {
			selectedMenuItem = 0;
		} else if (button2.contains(new Vector3(x, y, 0))) {
			selectedMenuItem = 1;
		} else if (button3.contains(new Vector3(x, y, 0))) {
			selectedMenuItem = 2;
		} else if (button4.contains(new Vector3(x, y, 0))) {
			selectedMenuItem = 3;
		} else if (button5.contains(new Vector3(x, y, 0))) {
			selectedMenuItem = 4;
		} else if (button6.contains(new Vector3(x, y, 0))) {
			selectedMenuItem = 5;
		} else {
			selectedMenuItem = -1;
		}
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
