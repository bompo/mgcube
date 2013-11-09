package de.redlion.qb;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

import de.redlion.qb.shader.FastBloomShader;
import de.redlion.qb.shader.TransShader;

public class Resources {

	public final boolean debugMode = false;

	// 1=Block, 2=Player, 3=Target, 4-8 Portals (the corresponding portal is
	// marked by the respective negative number), 9 Movable Block, 10 - ?
	// Switchable Block ( the corresponding switch is marked by the respective
	// negative number

	public int[][][] locked;

	public int[][][] opening;

	// Leveleditor hash: falchfkbihffleecfbWhjYbaof
	public int[][][] tut1;

	// eckYShUhUhUhYhYhYhUfUfcffYfUhcifebhjYfUfYhUfYfUfYhYeYfUhUhYhfmhjjabXXblffllef
	public int[][][] tut2;

	public int[][][] tut3;

	// eajYTkoTjZbjefafbffjhi
	public int[][][] tut4;

	// eYkYTjoSiTiZbkffafaabhfidii
	public int[][][] tut5;

	// fajajWkkXcflTiZXlbfSfUffcfhi
	public int[][][] tut6;

	// eZjXThpYkZhZbbjfXfdfSfeflhdheodkdYcfmYdclf
	public int[][][] tut7;

	public int[][][] level1, level2, level3, level4, level5, level6, level7, level8, level9, level10, level11, level12, level13, level14, level15, level16, level17, level18, level19, level20, level21, level22, level23, level24, qbert;

	public int currentlevel = 0;
	
	public ArrayList<int[][][]> levels = new ArrayList<int[][][]>();

	public ArrayList<int[][][]> tutorials = new ArrayList<int[][][]>();

	public Array<String> customLevels = new Array<String>();
	public String questionLevel = new String(
			"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,1,1,0,0,0,0,0,3,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
	public String blankLevel = new String(
			"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");

	// public int lives = 3;
	public float time = 0;
	public float timeAttackTime = 120;

	public Mesh blockModel;
	public Mesh playerModel;
	public Mesh coneModel;
	public Mesh targetModel;
	public Mesh quadModel;
	public Mesh wireCubeModel;
	public Mesh sphereModel;
	public Mesh bigMesh;
	public Mesh combinedModel;

	public Array<Integer> bigMeshIndicesCntSubMesh = new Array<Integer>();
	public Array<Integer> bigMeshVerticesCntSubMesh = new Array<Integer>();

	public Music music = Gdx.audio.newMusic(Gdx.files
			.internal("data/bitbof_amboned.mp3"));
	public Sound moveSFX = Gdx.audio.newSound(Gdx.files
			.internal("data/move.wav"));
	public Sound warpSFX = Gdx.audio.newSound(Gdx.files
			.internal("data/warp.wav"));
	public Sound changeLevelSFX = Gdx.audio.newSound(Gdx.files
			.internal("data/changeLevel.wav"));
	public Sound collideSFX = Gdx.audio.newSound(Gdx.files
			.internal("data/collide.wav"));
	public Sound switchSFX = Gdx.audio.newSound(Gdx.files
			.internal("data/switch.wav"));
	public Sound loseSFX = Gdx.audio.newSound(Gdx.files
			.internal("data/lose.wav"));

	public ShaderProgram transShader;
	public ShaderProgram bloomShader;
	public int m_i32TexSize = 128;
	public float m_fTexelOffset;

	public BitmapFont font;
	public BitmapFont selectedFont;
	public BitmapFont timeAttackFont;

	public float[] clearColor = { 0.0f, 0.0f, 0.0f, 1.0f };
	public float[] backgroundWireColor = { 1.0f, 0.8f, 0.8f, 0.07f };
	public float[] wireCubeColor = { 1.0f, 0.1f, 0.1f, 0.04f };
	public float[] wireCubeEdgeColor = { 1.0f, 0.1f, 0.1f, 0.5f };
	public float[] blockColor = { 1.0f, 0.1f, 0.1f, 0.2f };
	public float[] blockEdgeColor = { 1.0f, 0.1f, 0.1f, 0.8f };
	public float[] movableBlockColor = { 1.0f, 0.8f, 0.1f, 0.8f };
	public float[] movableBlockEdgeColor = { 1.0f, 0.8f, 0.1f, 0.2f };
	public float[] switchBlockColor = { 1f, 1f, 1f, 0.8f };
	public float[] switchBlockEdgeColor = { 1f, 1f, 1f, 0.3f };
	public float[] playerColor = { 1.0f, 1.0f, 0.0f, 0.4f };
	public float[] playerEdgeColor = { 1.0f, 1.0f, 0.0f, 0.4f };
	public float[] portalColor = { 1f, 1f, 0f, 0.5f };
	public float[] portalColor2 = { 0.03f, 0.3f, 0.73f, 0.5f };
	public float[] portalColor3 = { 0f, 1f, 1f, 0.5f };
	public float[] portalColor4 = { 0f, 0.8f, 0.4f, 0.5f };
	public float[] portalColor5 = { 0.89f, 0.21f, 0.15f, 0.5f };
	public float[] portalEdgeColor = { 1.0f, 1.0f, 1.0f, 0.7f };
	public float[] targetColor = { 0.0f, 1.0f, 0.1f, 0.5f };
	public float[] targetEdgeColor = { 0.0f, 1.0f, 0.1f, 0.4f };
	public float[] editorBlockColor = { 0.7f, 0.7f, 0.5f, 0.2f };
	public float[] editorBlockEdgeColor = { 0.7f, 0.7f, 0.5f, 0.4f };
	public float[] playerShadowColor = { 1.0f, 0.1f, 0.1f, 0.01f };

	public int colorTheme = 0;

	public Preferences prefs = Gdx.app.getPreferences("qb");
	public boolean bloomOnOff = true;
	public boolean musicOnOff = true;
	public boolean fullscreenOnOff = true;

	public String[] tutorialText1 = {
			"Welcome to the Qb tutorial!",
			"See this yellow sphere?\nThat's you, the player!",
			"Now your goal is to\nreach the green cylinder",
			"You can rotate the level\nby touching the screen",
			"Pinch your fingers\nto zoom.\nTry it now!",
			"If you tap the screen\nthe player will fly away\nfrom the camera",
			"Now try to reach that block\nright ahead of you",
			"Just don't fly off the screen,\nokay? Then you'd have to\nstart all over again!",
			"Go ahead and try to\nreach the exit" };
	public String[] tutorialText1PC = {
			"Welcome to the Qb tutorial!",
			"See this yellow sphere?\nThat's you, the player!",
			"Now your goal is to\nreach the green cylinder",
			"You can rotate the level\nby touching the screen",
			"You can zoom by\nscrolling your mousewheel.\nTry it now!",
			"If you click the mouse or\nhit space, the player will\nfly into the screen",
			"Now try to reach that block\nright ahead of you",
			"Just don't fly off the screen,\nokay? Then you'd have to\nstart all over again!",
			"Go ahead and try to\nreach the exit" };
	public String[] tutorialText2 = {
			"Welcome to the second tutorial!",
			"Today: Portals",
			"See these rotating cubes?\nIf you fly in one of these\nyou're warped to the other one",
			"You exit a Portal in the\ndirection you entered it",
			"You can enter a Portal from any\ndirection, so be sure to\ntry out different combinations",
			"Now go ahead and try to\nsolve this level" };
	public String[] tutorialText3 = { "Welcome to the third tutorial!",
			"There are levels that feature\nmultiple Portals",
			"But how do you know which Portal\nleads where?",
			"Easy. Portals that belong\ntogether share the same color",
			"Now try to solve this one!", "Hint:\nUse blue, then light blue" };
	public String[] tutorialText4 = {
			"Welcome to the fourth tutorial!",
			"See this yellow block?\nIt's a Movable Block",
			"If you push it, it will fly off",
			"Use these blocks to manipulate\nthe level",
			"Go ahead and push the\nMovable Block",
			"Notice how, unlike you,\nthe Movable Block just stops\nat the edge of the level" };
	public String[] tutorialText5 = {
			"Welcome to the fifth tutorial!",
			"Today you will solve a level with\nmultiple Movable Blocks",
			"If two or more Movable Blocks\nare situated in a row,\nyou can cause a chain reaction",
			"Now try to reach the exit" };
	public String[] tutorialText6 = { "Welcome to the sixth tutorial!",
			"This is a level with\nMovable Blocks AND Portals",
			"Just like you, Movable Blocks can\ntravel through Portals",
			"Try and push this Movable Block\nthrough the Portal",
			"Beautiful.\nNow reach the exit" };
	public String[] tutorialText7 = {
			"Welcome to the seventh tutorial!",
			"This tutorial will introduce\nthe last gameplay mechanic:\nSwitches",
			"You can activate a switch\nby standing on it",
			"Go ahead and fly towards\nthe switch",
			"Notice how the white block\ndisappears",
			"Now try and reach the exit!",
			"Whoops.\nThat didn't seem to work.",
			"Maybe you should try and push\nthis Movable Block onto\nthe switch",
			"Perfect.\nNotice this little shape\ninside the switch?",
			"That's how you can distinguish\nbetween different switches\n",
			"A switch will always disable\nblocks marked with it's symbol",
			"Now all that's left to do\nis reach the exit!" };

	public static Resources instance;

	public static Resources getInstance() {
		if (instance == null) {
			instance = new Resources();
		}
		return instance;
	}

	public Resources() {
		reInit();
	}

	public void reInit() {

//		System.out.println("\"level1\": \"" + encode(level1) + "\"");
//		System.out.println("\"level2\": \"" + encode(level2) + "\"");
//		System.out.println("\"level3\": \"" + encode(level3) + "\"");
//		System.out.println("\"level4\": \"" + encode(level4) + "\"");
//		System.out.println("\"level5\": \"" + encode(level5) + "\"");
//		System.out.println("\"level6\": \"" + encode(level6) + "\"");
//		System.out.println("\"level7\": \"" + encode(level7) + "\"");
//		System.out.println("\"level8\": \"" + encode(level8) + "\"");
//		System.out.println("\"level9\": \"" + encode(level9) + "\"");
//		System.out.println("\"level10\": \"" + encode(level10) + "\"");
//		System.out.println("\"level11\": \"" + encode(level11) + "\"");
//		System.out.println("\"level12\": \"" + encode(level12) + "\"");
//		System.out.println("\"level13\": \"" + encode(level13) + "\"");
//		System.out.println("\"level14\": \"" + encode(level14) + "\"");
//		System.out.println("\"level15\": \"" + encode(level15) + "\"");
//		System.out.println("\"level16\": \"" + encode(level16) + "\"");
//		System.out.println("\"level17\": \"" + encode(level17) + "\"");
//		System.out.println("\"level18\": \"" + encode(level18) + "\"");
//		System.out.println("\"level19\": \"" + encode(level19) + "\"");
//		System.out.println("\"level20\": \"" + encode(level20) + "\"");
//		System.out.println("\"level21\": \"" + encode(level21) + "\"");
//		System.out.println("\"level22\": \"" + encode(level22) + "\"");
//		System.out.println("\"level23\": \"" + encode(level23) + "\"");
//		System.out.println("\"level24\": \"" + encode(level24) + "\"");
//		System.out.println("\"qbert\": \"" + encode(qbert) + "\"");

		ObjectMap<String, ?> map = new Json().fromJson(ObjectMap.class,
				Gdx.files.internal("data/levels.json"));
		locked = decode((String) map.get("locked"));
		opening = decode((String) map.get("opening"));
		tut1 = decode((String) map.get("tut1"));
		tut2 = decode((String) map.get("tut2"));
		tut3 = decode((String) map.get("tut3"));
		tut4 = decode((String) map.get("tut4"));
		tut5 = decode((String) map.get("tut5"));
		tut6 = decode((String) map.get("tut6"));
		tut7 = decode((String) map.get("tut7"));
		tutorials.clear();
		tutorials.add(tut1);
		tutorials.add(tut2);
		tutorials.add(tut3);
		tutorials.add(tut4);
		tutorials.add(tut5);
		tutorials.add(tut6);
		tutorials.add(tut7);
		
		level1 = decode((String) map.get("level1"));
		level2 = decode((String) map.get("level2"));
		level3 = decode((String) map.get("level3"));
		level4 = decode((String) map.get("level4"));
		level5 = decode((String) map.get("level5"));
		level6 = decode((String) map.get("level6"));
		level7 = decode((String) map.get("level7"));
		level8 = decode((String) map.get("level8"));
		level9 = decode((String) map.get("level9"));
		level10 = decode((String) map.get("level10"));
		level11 = decode((String) map.get("level11"));
		level12 = decode((String) map.get("level12"));
		level13 = decode((String) map.get("level13"));
		level14 = decode((String) map.get("level14"));
		level15 = decode((String) map.get("level15"));
		level16 = decode((String) map.get("level16"));
		level17 = decode((String) map.get("level17"));
		level18 = decode((String) map.get("level18"));
		level19 = decode((String) map.get("level19"));
		level20 = decode((String) map.get("level20"));
		level21 = decode((String) map.get("level21"));
		level22 = decode((String) map.get("level22"));
		level23 = decode((String) map.get("level23"));
		level24 = decode((String) map.get("level24"));
		qbert = decode((String) map.get("qbert"));
		levels.clear();
		levels.add(level1);
		levels.add(level2);
		levels.add(level3);
		levels.add(level4);
		levels.add(level5);
		levels.add(level6);
		levels.add(level7);
		levels.add(level8);
		levels.add(level9);
		levels.add(level10);
		levels.add(level11);
		levels.add(level12);
		levels.add(level13);		
		levels.add(level14);
		levels.add(level15);
		levels.add(level16);
		levels.add(level17);
		levels.add(level18);
		levels.add(level19);
		levels.add(level20);
		levels.add(level21);
		levels.add(level22);
		levels.add(level23);
		levels.add(level24);
		
		ObjLoader obLoader = new ObjLoader();
		blockModel = obLoader.loadModel(Gdx.files.internal("data/cube.obj")).meshes.get(0);
		playerModel = obLoader.loadModel(Gdx.files.internal("data/sphere_small.obj")).meshes.get(0);
		sphereModel = obLoader.loadModel(Gdx.files.internal("data/sphere.obj")).meshes.get(0);
		targetModel = obLoader.loadModel(Gdx.files.internal("data/cylinder.obj")).meshes.get(0);
		combinedModel = obLoader.loadModel(Gdx.files.internal("data/models.obj")).meshes.get(0);
		coneModel = obLoader.loadModel(Gdx.files.internal("data/cone.obj")).meshes.get(0);

		quadModel = new Mesh(true, 4, 6, new VertexAttribute(Usage.Position, 4,
				"a_position"), new VertexAttribute(Usage.TextureCoordinates, 2,
				"a_texCoord"));
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

		wireCubeModel = new Mesh(true, 20, 20, new VertexAttribute(
				Usage.Position, 3, "a_position"));
		float[] vertices2 = {
				// front face
				-1.0f, 1.0f, 1.0f, // 0
				1.0f, 1.0f, 1.0f, // 1
				1.0f, -1.0f, 1.0f, // 2
				-1.0f, -1.0f, 1.0f, // 3

				// left face
				-1.0f, 1.0f, 1.0f, // 0
				-1.0f, 1.0f, -1.0f, // 4
				-1.0f, -1.0f, -1.0f, // 7
				-1.0f, -1.0f, 1.0f, // 3

				// bottom face
				-1.0f, -1.0f, 1.0f, // 3
				1.0f, -1.0f, 1.0f, // 2
				1.0f, -1.0f, -1.0f, // 6
				-1.0f, -1.0f, -1.0f, // 7

				// back face
				-1.0f, -1.0f, -1.0f, // 7
				-1.0f, 1.0f, -1.0f, // 4
				1.0f, 1.0f, -1.0f, // 5
				1.0f, -1.0f, -1.0f, // 6

				// right face
				1.0f, -1.0f, -1.0f, // 6
				1.0f, -1.0f, 1.0f, // 2
				1.0f, 1.0f, 1.0f, // 1
				1.0f, 1.0f, -1.0f, // 5
		};
		short[] indices2 = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
				15, 16, 17, 18, 19 };
		wireCubeModel.setVertices(vertices2);
		wireCubeModel.setIndices(indices2);

		// wireCubeModel =
		// ObjLoader.loadObj(Gdx.files.internal("data/wirecube.obj").read());

		// copy all Meshes into a combined Mesh for performance reasons...
		int bigMeshVerticesCnt = blockModel.getNumVertices()
				* blockModel.getVertexSize() / 4;// +playerModel.getNumVertices()+targetModel.getNumVertices();

		Array<Float> bigMeshVertices = new Array<Float>(bigMeshVerticesCnt);

		float[] bigMeshCopyVertices = new float[blockModel.getNumVertices()
				* blockModel.getVertexSize() / 4];
		blockModel.getVertices(bigMeshCopyVertices);
		for (Float f : bigMeshCopyVertices) {
			bigMeshVertices.add(f);
		}
		bigMeshVerticesCntSubMesh.add(bigMeshVertices.size);

		// {
		// float[] bigMeshCopyVertices = new float[playerModel.getNumVertices()
		// * playerModel.getVertexSize() / 4];
		// playerModel.getVertices(bigMeshCopyVertices);
		// for(Float f:bigMeshCopyVertices) {
		// bigMeshVertices.add(f);
		// }
		// bigMeshVerticesCntSubMesh.add(bigMeshVertices.size);
		// }
		// {
		// float[] bigMeshCopyVertices = new float[targetModel.getNumVertices()
		// * targetModel.getVertexSize() / 4];
		// targetModel.getVertices(bigMeshCopyVertices);
		// for(Float f:bigMeshCopyVertices) {
		// bigMeshVertices.add(f);
		// }
		// bigMeshVerticesCntSubMesh.add(bigMeshVertices.size);
		// }

		float[] bigMeshVerticesFloatArray = new float[bigMeshVertices.size];
		for (int i = 0; i < bigMeshVertices.size; i++) {
			Float f = bigMeshVertices.get(i);
			bigMeshVerticesFloatArray[i] = (f != null ? f : 0); // Or whatever
																// default you
																// want.
		}

		short[] bigMeshIndicesShortArray = new short[bigMeshVertices.size];
		for (short i = 0; i < bigMeshVertices.size; i++) {
			Short f = i;
			bigMeshIndicesShortArray[i] = (f != null ? f : 0); // Or whatever
																// default you
																// want.
		}

		bigMesh = new Mesh(true, bigMeshVertices.size, 0, new VertexAttribute(
				Usage.Position, 3, "a_position"));
		bigMesh.setVertices(bigMeshCopyVertices);
		// bigMesh.setIndices(bigMeshIndicesShortArray);
		//
		// for(Integer inte:bigMeshVerticesCntSubMesh) {
		// Gdx.app.log("", inte.toString());
		// }
		// Gdx.app.log("", "---");
		// int n = 0;
		// for(Float inte:bigMeshVerticesFloatArray) {
		// Gdx.app.log("", n + ": " + inte.toString());
		// ++n;
		// }

		try {
			if (music != null)
				music.stop();
			if (moveSFX != null)
				moveSFX.stop();
		} catch (Exception e) {
			// TODO: handle exception
		}

		music = Gdx.audio.newMusic(Gdx.files
				.internal("data/bitbof_amboned.mp3"));
		moveSFX = Gdx.audio.newSound(Gdx.files.internal("data/move.wav"));
		warpSFX = Gdx.audio.newSound(Gdx.files.internal("data/warp.wav"));
		changeLevelSFX = Gdx.audio.newSound(Gdx.files
				.internal("data/changeLevel.wav"));
		collideSFX = Gdx.audio.newSound(Gdx.files.internal("data/collide.wav"));
		switchSFX = Gdx.audio.newSound(Gdx.files.internal("data/switch.wav"));
		loseSFX = Gdx.audio.newSound(Gdx.files.internal("data/lose.wav"));

		initShader();

		font = new BitmapFont(Gdx.files.internal("data/scorefont.fnt"), false);
		timeAttackFont = new BitmapFont(
				Gdx.files.internal("data/scorefont.fnt"), false);
		// font.setColor(1, 1, 1, 0.8f);
		selectedFont = new BitmapFont(Gdx.files.internal("data/selected.fnt"),
				false);

		bloomOnOff = !prefs.getBoolean("bloom");
		musicOnOff = !prefs.getBoolean("music");
		fullscreenOnOff = !prefs.getBoolean("fullscreen");

		if (!debugMode) {
			if (musicOnOff) {
				music.stop();
				music.play();
				music.setLooping(true);
			}
		}

		// load custom levels
		int customLevelCount = prefs.getInteger("customLevel_count", 0);
		customLevels.clear();
		for (int i = 1; i <= customLevelCount; i++) {
			customLevels.add(prefs.getString("customLevel_" + i));
		}
	}

	public void initShader() {
		transShader = new ShaderProgram(TransShader.mVertexShader,
				TransShader.mFragmentShader);
		if (transShader.isCompiled() == false) {
			Gdx.app.log("ShaderTest", transShader.getLog());
			System.exit(0);
		}

		// BLOOOOOOMMMM from powervr examples
		// Blur render target size (power-of-two)
		float blurSize = 1.0f;
		if (Gdx.graphics.getWidth() <= 1000) {
			blurSize = 1.0f;
			// m_i32TexSize = 256;
		} else {
			// m_i32TexSize = 128;
			blurSize = 1.0f;
		}

		// Texel offset for blur filter kernle
		m_fTexelOffset = 1.0f / m_i32TexSize / blurSize;

		// Altered weights for the faster filter kernel
		float w1 = 0.0555555f / blurSize;
		float w2 = 0.2777777f / blurSize;
		float intraTexelOffset = (w2 / (w1 + w2)) * m_fTexelOffset;
		m_fTexelOffset += intraTexelOffset;

		bloomShader = new ShaderProgram(FastBloomShader.mVertexShader,
				FastBloomShader.mFragmentShader);
		if (bloomShader.isCompiled() == false) {
			Gdx.app.log("ShaderTest", bloomShader.getLog());
			System.exit(0);
		}
	}

	public void dispose() {
		font.dispose();
		selectedFont.dispose();

		blockModel.dispose();
		playerModel.dispose();
		targetModel.dispose();
		quadModel.dispose();
		wireCubeModel.dispose();

		music.stop();
		moveSFX.stop();

		transShader.dispose();
		bloomShader.dispose();
	}

	public void switchColorTheme() {
		if (colorTheme == 0) {
			clearColor = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
			backgroundWireColor = new float[] { 1.0f, 0.8f, 0.8f, 0.07f };
			wireCubeColor = new float[] { 1.0f, 0.1f, 0.1f, 0.04f };
			wireCubeEdgeColor = new float[] { 1.0f, 0.1f, 0.1f, 0.5f };
			blockColor = new float[] { 1.0f, 0.1f, 0.1f, 0.2f };
			blockEdgeColor = new float[] { 1.0f, 0.1f, 0.1f, 0.8f };
			movableBlockColor = new float[] { 1.0f, 0.8f, 0.1f, 0.8f };
			movableBlockEdgeColor = new float[] { 1.0f, 0.8f, 0.1f, 0.2f };
			switchBlockColor = new float[] { 0.2f, 0.2f, 0.2f, 0.8f };
			switchBlockEdgeColor = new float[] { 0.2f, 0.2f, 0.2f, 0.2f };
			playerColor = new float[] { 1.0f, 1.0f, 0.0f, 0.4f };
			playerEdgeColor = new float[] { 1.0f, 1.0f, 0.0f, 0.4f };
			portalColor = new float[] { 0.0f, 0.05f, 1.0f, 0.05f };
			portalEdgeColor = new float[] { 0.0f, 0.05f, 1.0f, 0.02f };
			targetColor = new float[] { 0.0f, 1.0f, 0.1f, 0.5f };
			targetEdgeColor = new float[] { 0.0f, 1.0f, 0.1f, 0.4f };
		} else if (colorTheme == 1) {
			clearColor = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
			backgroundWireColor = new float[] { 0.5f, 0.5f, 0.5f, 0.8f };
			wireCubeColor = new float[] { 0.5f, 0.5f, 0.5f, 0.5f };
			wireCubeEdgeColor = new float[] { 0.1f, 0.1f, 0.1f, 1.0f };
			blockColor = new float[] { 0.2f, 0.2f, 0.2f, 0.4f };
			blockEdgeColor = new float[] { 0.2f, 0.2f, 0.2f, 1.0f };
			movableBlockColor = new float[] { 1.0f, 0.8f, 0.1f, 0.8f };
			movableBlockEdgeColor = new float[] { 1.0f, 0.8f, 0.1f, 0.2f };
			switchBlockColor = new float[] { 0.2f, 0.2f, 0.2f, 0.8f };
			switchBlockEdgeColor = new float[] { 0.2f, 0.2f, 0.2f, 0.2f };
			playerColor = new float[] { 1.0f, 0.1f, 0.1f, 0.2f };
			playerEdgeColor = new float[] { 1.0f, 0.1f, 0.1f, 0.2f };
			portalColor = new float[] { 0.0f, 0.05f, 1.0f, 0.05f };
			portalEdgeColor = new float[] { 0.0f, 0.05f, 1.0f, 0.02f };
			targetColor = new float[] { 0.0f, 1.0f, 0.1f, 0.5f };
			targetEdgeColor = new float[] { 0.0f, 1.0f, 0.1f, 0.4f };
		}
	}

	public String encode(int[][][] level) {
		StringBuilder string = new StringBuilder();
		for (int x = 0; x < 11; x++) {
			for (int y = 0; y < 11; y++) {
				for (int z = 0; z < 11; z++) {
					if (x == 10 && y == 10 && z == 10) {
						string.append(level[x][y][z]);
					} else {
						string.append(level[x][y][z] + ",");
					}
				}
			}
		}
		return string.toString();
	}

	public int[][][] decode(String level) {
		int[][][] levelArray = {
				{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
				{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
				{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
				{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
				{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
				{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
				{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
				{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
				{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
				{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } },
				{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } } };
		String[] splitString = level.split(",");

		int z = 0;
		int y = 0;
		int x = 0;
		for (String s : splitString) {
			levelArray[x][y][z] = Integer.parseInt(s);
			z++;
			if (z == 11) {
				z = 0;
				y++;
			}
			if (y == 11) {
				y = 0;
				x++;
			}
		}
		return levelArray;
	}

}
