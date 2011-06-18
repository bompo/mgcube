package de.swagner.gdx.obj.normalmap.shader;

public class Quad2Shader {

    public static final String mVertexShader =
    	"uniform float u_offset;      \n" +                       
        "attribute vec4 a_position;   \n" +
        "attribute vec2 a_texCoord;   \n" +
        "varying vec2 v_texCoord;     \n" +
        "void main()                  \n" +
        "{                            \n" +
        "   gl_Position = a_position; \n" +
        "   gl_Position.x += u_offset;\n" +
        "   v_texCoord = a_texCoord;  \n" +
        "}                            \n";

    public static final String mFragmentShader =   
        "#ifdef GL_ES\n"
        + "precision mediump float;\n"
        + "#endif\n" +
"varying vec2 v_texCoord;                            \n" +
"uniform sampler2D s_texture;                        \n" +
"void main()                                         \n" +
"{                                                   \n" +
"  gl_FragColor = texture2D( s_texture, v_texCoord );\n" +
"}                                                   \n"; 
    

}