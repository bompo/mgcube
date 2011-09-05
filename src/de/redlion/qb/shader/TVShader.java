package de.redlion.qb.shader;

public class TVShader {

    public static final String mVertexShader =                    
    	  "attribute vec4 aPosition;\n"
		+ "void main() {\n" 
		+ "  gl_Position = vec4(aPosition);\n"
		+ "}\n";

    public static final String mFragmentShader =   
        "#ifdef GL_ES\n" +
        "precision mediump float;\n" +
        "#endif\n" +
"uniform int rotation;\n" +
"uniform sampler2D sampler0;\n" +
"uniform vec2 resolution;\n" +
"uniform float time;\n" +
"void main ()\n" +
"{\n" +
"  vec3 color;\n" +
"  vec2 position;\n" +
"  vec2 tmpvar_1;\n" +
"  tmpvar_1 = (gl_FragCoord.xy / resolution);\n" +
"  position = tmpvar_1;\n" +
"  position.y = (tmpvar_1.y * -1.0);\n" +
"  if ((rotation == 0)) {\n" +
"    vec2 tmpvar_2;\n" +
"    tmpvar_2.x = (tmpvar_1.x + 0.002);\n" +
"    tmpvar_2.y = -(position.y);\n" +
"    color.x = texture2D (sampler0, tmpvar_2).x;\n" +
"    vec2 tmpvar_3;\n" +
"    tmpvar_3.x = tmpvar_1.x;\n" +
"    tmpvar_3.y = -(position.y);\n" +
"    color.y = texture2D (sampler0, tmpvar_3).y;\n" +
"    vec2 tmpvar_4;\n" +
"    tmpvar_4.x = (tmpvar_1.x - 0.002);\n" +
"    tmpvar_4.y = -(position.y);\n" +
"    color.z = texture2D (sampler0, tmpvar_4).z;\n" +
"  } else {\n" +
"    vec2 tmpvar_5;\n" +
"    tmpvar_5.x = (-(position.y) + 0.002);\n" +
"    tmpvar_5.y = -(tmpvar_1.x);\n" +
"    color.x = texture2D (sampler0, tmpvar_5).x;\n" +
"    vec2 tmpvar_6;\n" +
"    tmpvar_6.x = -(position.y);\n" +
"    tmpvar_6.y = -(tmpvar_1.x);\n" +
"    color.y = texture2D (sampler0, tmpvar_6).y;\n" +
"    vec2 tmpvar_7;\n" +
"    tmpvar_7.x = (-(position.y) - 0.002);\n" +
"    tmpvar_7.y = -(tmpvar_1.x);\n" +
"    color.z = texture2D (sampler0, tmpvar_7).z;\n" +
"  };\n" +
"  vec3 tmpvar_8;\n" +
"  tmpvar_8 = ((((clamp (((color * 0.5) + (((0.5 * 1.2) * color) * color)), 0.0, 1.0) * (0.5 + ((((8.0 * tmpvar_1.x) * position.y) * (1.0 - tmpvar_1.x)) * (-1.0 - position.y)))) * vec3(0.95, 0.85, 1.0)) * (0.9 + (0.1 * sin (((10.0 * time) + (position.y * 1000.0)))))) * (0.97 + (0.03 * sin ((110.0 * time)))));\n" +
"  color = tmpvar_8;\n" +
"  vec4 tmpvar_9;\n" +
"  tmpvar_9.w = 1.0;\n" +
"  tmpvar_9.xyz = tmpvar_8;\n" +
"  gl_FragColor = tmpvar_9;\n" +
"}\n";

}