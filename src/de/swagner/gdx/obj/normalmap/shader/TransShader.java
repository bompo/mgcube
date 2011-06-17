package de.swagner.gdx.obj.normalmap.shader;

public class TransShader {

    public static final String mVertexShader =
    	"attribute vec4 a_vertex;		\n" +
    	"uniform mat4 MVPMatrix;			\n"+
    	"void main() {						\n"+
    	"	gl_Position = MVPMatrix * a_vertex;\n"+
    	"}\n";

    public static final String mFragmentShader =   
        "#ifdef GL_ES\n" +
        "precision mediump float;\n" +
        "#endif\n" +
        "uniform vec3 a_color;		\n"+
        "uniform float alpha;		\n"+
    	"void main() {						\n"+
    	"gl_FragColor = vec4(a_color, alpha);	\n"+
    	"}\n"; 
}