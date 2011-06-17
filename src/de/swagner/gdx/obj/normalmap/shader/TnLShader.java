package de.swagner.gdx.obj.normalmap.shader;

public class TnLShader {

    public static final String mVertexShader =
    	"attribute vec4 a_vertex;		\n" +
    	"attribute vec3 a_normal;		\n"+
    	"uniform mat4 MVPMatrix;			\n"+
    	"uniform vec3 LightDirection;		\n"+
    	"varying vec3 DiffuseLight;	\n"+
    	"varying vec3 SpecularLight;	\n"+
    	"void main() {						\n"+
    	"	gl_Position = MVPMatrix * a_vertex;\n"+
    	"	DiffuseLight = vec3(max(dot(a_normal, LightDirection), 0.0));\n"+
    	"	SpecularLight = vec3(max(DiffuseLight.x, 0.0));\n"+
    	"}\n";

    public static final String mFragmentShader =   
        "#ifdef GL_ES\n" +
        "precision mediump float;\n" +
        "#endif\n" +
        "uniform vec3 a_color;		\n"+
    	"varying vec3 DiffuseLight;	\n"+
    	"varying vec3 SpecularLight;	\n"+
    	"void main() {						\n"+
    	"vec3 color = (a_color*DiffuseLight) + SpecularLight;\n"+
    	"gl_FragColor = vec4(color, 0.5f);	\n"+
    	"}\n"; 
}