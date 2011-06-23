package de.swagner.gdx.obj.normalmap.shader;

public class FastBloomShader {

    public static final String mVertexShader =                    
        "attribute vec4 a_vertex;   \n" +
        "attribute vec2 a_texCoord;   \n" +
        "uniform float TexelOffsetX;   \n" +
        "uniform float TexelOffsetY;   \n" +
        "varying vec2  TexCoord0;   \n" +
        "varying vec2  TexCoord1;   \n" +
        "varying vec2  TexCoord2;   \n" +
        "void main()   \n" +
        "{   \n" +
        "	// Pass through vertex   \n" +
        "	gl_Position = a_vertex;   \n" +
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
        "precision highp float;\n" +
        "#endif\n" +
        "uniform sampler2D  sTexture;\n" +
        "uniform float bloomFactor;\n" +
        "varying vec2  TexCoord0;\n" +
        "varying vec2  TexCoord1;\n" +
        "varying vec2  TexCoord2;\n" +
        "void main()\n" +
        "{\n" +
        "    vec3 color = texture2D(sTexture, TexCoord0).rgb * 0.333333;\n" +
        "    color = (texture2D(sTexture, TexCoord1).rgb * 0.333333) + color;\n" +
        "    color = (texture2D(sTexture, TexCoord2).rgb * 0.333333) + color;\n" + 
        "    gl_FragColor.rgb = color*bloomFactor;\n" +
        "}\n";
}