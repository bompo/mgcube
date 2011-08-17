package de.swagner.gdx.obj.normalmap.shader;

public class FastBloomShader {

    public static final String mVertexShader =                    
		"varying vec2 TexCoord2; \n" +
		"varying vec2 TexCoord1; \n" + 
		"varying vec2 TexCoord0; \n" +
		"uniform float TexelOffsetY; \n" +
		"uniform float TexelOffsetX; \n" +
		"attribute vec2 a_texCoord; \n" +
		"attribute vec4 a_position; \n" +
		"void main () \n" +
		"{ \n" +
		"  gl_Position = a_position; \n" +
		"  vec2 tmpvar_1; \n" +
		"  tmpvar_1.x = TexelOffsetX; \n" +
		"  tmpvar_1.y = TexelOffsetY; \n" +
		"  TexCoord0 = (a_texCoord - tmpvar_1); \n" +
		"  TexCoord1 = a_texCoord; \n" +
		"  TexCoord2 = (a_texCoord + tmpvar_1); \n" +
		"} \n";
    
    public static final String mFragmentShader =   
        "#ifdef GL_ES\n" +
        "precision mediump float;\n" +
        "#endif\n" +
		"varying vec2 TexCoord2;\n" +
		"varying vec2 TexCoord1;\n" +
		"varying vec2 TexCoord0;\n" +
		"uniform float bloomFactor;\n" +
		"uniform sampler2D sTexture;\n" +
		"void main ()\n" +
		"{\n" +
		"  gl_FragColor.xyz = (((texture2D (sTexture, TexCoord2).xyz * 0.333333) + ((texture2D (sTexture, TexCoord1).xyz * 0.333333) + (texture2D (sTexture, TexCoord0).xyz * 0.333333))) * bloomFactor);\n" +
		"} \n";
		}

/** Precision qualifiers gain one frame on milestone but break desktop support an look a bit uglier
public static final String mVertexShader =                    
"attribute highp vec4 a_vertex;   \n" +
"attribute mediump vec2 a_texCoord;   \n" +
"uniform mediump float TexelOffsetX;   \n" +
"uniform mediump float TexelOffsetY;   \n" +
"varying mediump vec2  TexCoord0;   \n" +
"varying mediump vec2  TexCoord1;   \n" +
"varying mediump vec2  TexCoord2;   \n" +
"void main()   \n" +
"{   \n" +
"	// Pass through vertex   \n" +
"	gl_Position = a_vertex;   \n" +
"	   \n" +
"	// Calculate texture offsets and pass through	   \n" +
"	mediump vec2 offset = vec2(TexelOffsetX, TexelOffsetY);   \n" +
"     \n" +
"    TexCoord0 = a_texCoord - offset;   \n" +
"    TexCoord1 = a_texCoord;   \n" +
"    TexCoord2 = a_texCoord + offset;       \n" +
"}   \n";

public static final String mFragmentShader =   
"#ifdef GL_ES\n" +
"precision highp float;\n" +
"#endif\n" +
"uniform lowp sampler2D  sTexture;\n" +
"uniform lowp float bloomFactor;\n" +
"varying mediump vec2  TexCoord0;\n" +
"varying mediump vec2  TexCoord1;\n" +
"varying mediump vec2  TexCoord2;\n" +
"void main()\n" +
"{\n" +
"    lowp vec3 color = texture2D(sTexture, TexCoord0).rgb * 0.333333;\n" +
"    color = (texture2D(sTexture, TexCoord1).rgb * 0.333333) + color;\n" +
"    color = (texture2D(sTexture, TexCoord2).rgb * 0.333333) + color;\n" + 
"    gl_FragColor.rgb = color*bloomFactor;\n" +
"}\n";
**/