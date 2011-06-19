package de.swagner.gdx.obj.normalmap.shader;

public class PreFastBloomShader {

    public static final String mVertexShader =                    
        "attribute vec4 a_position;   \n" +
        "attribute vec2 a_texCoord;   \n" +
        "uniform float  TexelOffsetX;   \n" +
        "uniform float  TexelOffsetY;   \n" +
        "varying vec2  TexCoord0;   \n" +
        "varying vec2  TexCoord1;   \n" +
        "varying vec2  TexCoord2;   \n" +
        "void main()   \n" +
        "{   \n" +
        "	// Pass through vertex   \n" +
        "	gl_Position = a_position;   \n" +
        "	   \n" +
        "	// Calculate texture offsets and pass through	   \n" +
        "	vec2 offset = vec2(TexelOffsetX, TexelOffsetY);   \n" +
        "     \n" +
        "    TexCoord0 = a_texCoord - offset;   \n" +
        "    TexCoord1 = a_texCoord;   \n" +
        "    TexCoord2 = a_texCoord + offset;       \n" +
        "}   \n";
    
    public static final String mFragmentShader =   
        "#ifdef GL_ES\n" +
        "precision mediump float;\n" +
        "#endif\n" +
        "uniform mediump float fBloomIntensity;\n" +
        "uniform sampler2D  sTexture;\n" +
        "varying vec2  TexCoord;\n" +
        "void main()\n" +
        "{\n" +
        "    gl_FragColor = texture2D(sTexture, TexCoord) * fBloomIntensity;\n" +
        "}\n";
}