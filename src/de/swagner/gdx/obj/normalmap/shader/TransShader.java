package de.swagner.gdx.obj.normalmap.shader;

public class TransShader {

    public static final String mVertexShader =
    	"attribute vec4 a_vertex;\n" +
    	"uniform mat4 VPMatrix;\n"+
    	"uniform mat4 MMatrix;\n"+
    	"void main() {\n"+
    	"	gl_Position = VPMatrix * MMatrix * a_vertex;\n"+
    	"}\n";

    public static final String mFragmentShader =   
        "#ifdef GL_ES\n" +
        "precision mediump float;\n" +
        "#endif\n" +
        "uniform vec4 a_color;\n"+
    	"void main() {\n"+
    	"	gl_FragColor = a_color;\n"+
    	"}\n"; 
}