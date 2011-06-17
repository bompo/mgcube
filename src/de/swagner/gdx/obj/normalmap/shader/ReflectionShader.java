package de.swagner.gdx.obj.normalmap.shader;

public class ReflectionShader {

    public static final String mVertexShader =
    	"attribute vec3 inVertex;\n"+
    	"attribute vec3  inNormal;\n"+
    	"uniform mat4  MVPMatrix;\n"+
    	"uniform mat3  ModelWorld;\n"+
    	"uniform vec3  EyePosModel;\n"+
    	"varying vec3  ReflectDir;\n"+
    	"void main() {\n"+
    	"	// Transform position\n"+
    	"	gl_Position = MVPMatrix * vec4(inVertex, 1.0);\n"+
    	"	// Calculate eye direction in model space\n"+
    	"	vec3 eyeDir = normalize(inVertex - EyePosModel);\n"+
    	"	// reflect eye direction over normal and transform to world space\n"+
    	"	ReflectDir = ModelWorld * reflect(eyeDir, inNormal);\n"+
    	"}";

    public static final String mFragmentShader =   
        "#ifdef GL_ES\n" +
        "precision mediump float;\n" +
        "#endif\n" +
    	"uniform sampler2D s2DMap;\n"+
    	"varying vec3  ReflectDir;\n"+
    	"void main() {\n"+
    	"	gl_FragColor = texture2D(s2DMap, ReflectDir.xy * 0.5 + 0.5);\n"+
    	"}";
}
