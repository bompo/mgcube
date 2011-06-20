package de.swagner.gdx.obj.normalmap.shader;

public class QuadShader {

    public static final String mVertexShader =
    	"attribute vec4 a_vertex;\n" //
        + "attribute vec4 a_color;\n" //
        + "attribute vec2 a_texCoords;\n" //
        + "uniform mat4 u_projectionViewMatrix;\n" //
        + "varying vec4 v_color;\n" //
        + "varying vec2 v_texCoords;\n" //
        + "\n" //
        + "void main()\n" //
        + "{\n" //
        + "   v_color = a_color;\n" //
        + "   v_texCoords = a_texCoords;\n" //
        + "   gl_Position =  u_projectionViewMatrix * a_vertex;\n" //
        + "}\n";

    public static final String mFragmentShader =   
    	"#ifdef GL_ES\n" //
        + "precision mediump float;\n" //
        + "#endif\n" //
        + "varying vec4 v_color;\n" //
        + "varying vec2 v_texCoords;\n" //
        + "uniform sampler2D u_texture;\n" //
        + "void main()\n"//
        + "{\n" //
        + "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" //
        + "}";
    
}