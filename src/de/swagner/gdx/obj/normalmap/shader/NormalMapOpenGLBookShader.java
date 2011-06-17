package de.swagner.gdx.obj.normalmap.shader;

public class NormalMapOpenGLBookShader {

    public static final String mVertexShader =
    	"attribute vec4  inVertex;\n"+
    	"attribute vec3  inNormal;\n"+
    	"attribute vec2  inTexCoord;\n"+
    	"attribute vec3  inTangent;\n"+
    	"\n"+
    	"uniform mat4  MVPMatrix;// model view projection transformation\n"+
    	"uniform vec3  LightPosModel;// Light position (point light) in model space\n"+
    	"\n"+
    	"varying vec3  LightVec;\n"+
    	"varying vec2  TexCoord;\n"+
    	"\n"+
    	"void main()\n"+
    	"{\n"+
    	"// Transform position\n"+
    	"gl_Position = MVPMatrix * inVertex;\n"+
    	"\n"+
    	"// Calculate light direction from light position in model space\n"+
    	"// You can skip this step for directional lights\n"+
    	"vec3 lightDirection = normalize(LightPosModel);\n"+
    	"\n"+
    	"// transform light direction from model space to tangent space\n"+
    	"vec3 bitangent = cross(inNormal, inTangent);\n"+
    	"mat3 tangentSpaceXform = mat3(inTangent, inNormal, bitangent);\n"+
    	"LightVec = lightDirection * tangentSpaceXform;\n"+
    	"\n"+
    	"TexCoord = inTexCoord;\n"+
    	"}\n";

    public static final String mFragmentShader =   
    	"uniform sampler2D  sBaseTex;\n"+
    	"uniform sampler2D  sNormalMap;\n"+
    	"\n"+
    	"varying vec3  LightVec;\n"+
    	"varying vec2  TexCoord;\n"+
    	"\n"+
    	"void main()\n"+
    	"{\n"+
    	"// read the per-pixel normal from the normal map and expand to [-1, 1]\n"+
    	"vec3 normal = texture2D(sNormalMap, TexCoord).rgb * 2.0 - 1.0;\n"+
    	"\n"+
    	"// calculate diffuse lighting as the cosine of the angle between light\n"+
    	"// direction and surface normal (both in surface local/tangent space)\n"+
    	"// We don't have to clamp to 0 here because the framebuffer write will be clamped\n"+
    	"float lightIntensity = dot(LightVec, normal);\n"+
    	"\n"+
    	"// read base texture and modulate with light intensity\n"+
    	"vec3 texColor = texture2D(sBaseTex, TexCoord).rgb;\n"+
    	"gl_FragColor = vec4(texColor * lightIntensity, 1.0);\n"+
    	"}\n";
}
