package de.swagner.gdx.obj.normalmap.shader;

public class BloomShader {

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
        "#ifdef GL_ES\n" +
        "precision mediump float;\n" +
        "#endif\n" +
        "uniform sampler2D s_texture;\n" +
        "uniform float bloomfactor;\n" +
        "varying vec2 v_texCoord; \n" +
        "void main()\n" +
        "{\n" +
        "   vec4 sum = vec4(0);\n" +
        "   int j;\n" +
        "   int i;\n" +
        "   for( i= -5 ;i < 5; i++)\n" +
        "   {\n" +
        "        for (j = -4; j < 4; j++)\n" +
        "        {\n" +
        "            sum += texture2D(s_texture, v_texCoord + vec2(j, i)*0.004) * 0.25 * bloomfactor;\n" +
        "        }\n" +
        "   }\n" +
        "       if (texture2D(s_texture, v_texCoord).r < 0.3)\n" +
        "    {\n" +
        "      gl_FragColor = sum*sum*0.012 + texture2D(s_texture, v_texCoord);\n" +
        "    }\n" +
        "    else\n" +
        "    {\n" +
        "        if (texture2D(s_texture, v_texCoord).r < 0.5)\n" +
        "        {\n" +
        "            gl_FragColor = sum*sum*0.009 + texture2D(s_texture, v_texCoord);\n" +
        "        }\n" +
        "        else\n" +
        "        {\n" +
        "             gl_FragColor = sum*sum*0.0075 + texture2D(s_texture, v_texCoord);\n" +
        "        }\n" +
        "    }\n" +
        "}\n";

}