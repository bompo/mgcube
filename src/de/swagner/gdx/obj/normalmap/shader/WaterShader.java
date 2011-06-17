package de.swagner.gdx.obj.normalmap.shader;

public class WaterShader {

    public static final String mVertexShader =
    	"attribute vec3  a_vertex;\n" +
    	"\n" +
    	"uniform mat4  u_matModelView;\n" +
    	"uniform mat4  u_matModelViewProjection;\n" +
    	"uniform vec3  u_eyePosition;// Eye (aka Camera) positon in model-space\n" +
    	"uniform vec2 BumpTranslation0;\n" +
    	"uniform vec2 BumpScale0;\n" +
    	"uniform vec2 BumpTranslation1;\n" +
    	"uniform vec2 BumpScale1;\n" +
    	"\n" +
    	"uniform float PerturbScale;\n" +
    	" \n" +
    	"varying vec2 BumpCoord0;\n" +
    	"varying vec2 BumpCoord1;\n" +
    	"varying vec3 WaterToEye;\n" +
    	"varying float WaterToEyeLength;\n" +
    	"\n" +
    	"void main()\n" +
    	"{\n" +
    	"// Convert each vertex into projection-space and output the value\n" +
    	"vec4 va_vertex = vec4(a_vertex, 1.0);\n" +
    	"gl_Position = u_matModelViewProjection * va_vertex;\n" +
    	"\n" +
    	"// The texture coordinate is calculated this way to reduce the number of attributes needed\n" +
    	"vec2 vTexCoord = a_vertex.xz;\n" +
    	"\n" +
    	"// Scale and translate texture coordinates used to sample the normal map - section 2.2 of white paper\n" +
    	"BumpCoord0 = vTexCoord.xy * BumpScale0;\n" +
    	"BumpCoord0 += BumpTranslation0;\n" +
    	"\n" +
    	"BumpCoord1 = vTexCoord.xy * BumpScale1;\n" +
    	"BumpCoord1 += BumpTranslation1;\n" +
    	"\n" +
    	"/* \n" +
    	"The water to eye vector is used to calculate the Fresnel term\n" +
    	"and to fade out perturbations based on distance from the viewer\n" +
    	"*/\n" +
    	"WaterToEye = u_eyePosition - a_vertex;\n" +
    	"WaterToEyeLength = length(WaterToEye);\n" +
    	"}\n";

    public static final String mFragmentShader =   
    	"uniform s_normalTex;\n" +
    	"uniform s_reflectionTex;\n" +
    	"#ifdef ENABLE_REFRACTION\n" +
    	"uniform s_RefractionTex;\n" +
    	"#endif\n" +
    	"uniform samplerCube NormalisationCubeMap;\n" +
    	"\n" +
    	"uniform mat4 u_matModelView;\n" +
    	"uniform vec4 WaterColour;\n" +
    	"#ifdef ENABLE_DISTORTION\n" +
    	"uniform floatWaveDistortion;\n" +
    	"#endif\n" +
    	"uniform vec2 RcpWindowSize;\n" +
    	"\n" +
    	"varying vec2 BumpCoord0;\n" +
    	"varying vec2 BumpCoord1;\n" +
    	"varying vec3 WaterToEye;\n" +
    	"varying float WaterToEyeLength;\n" +
    	"\n" +
    	"void main()\n" +
    	"{\n" +
    	"// Calculate the tex coords of the fragment (using it's position on the screen)\n" +
    	"vec3 vAccumulatedNormal = vec3(0.0,1.0,0.0);\n" +
    	"vec2 vTexCoord = gl_FragCoord.xy * RcpWindowSize;\n" +
    	"\n" +
    	"#ifdef ENABLE_DISTORTION\n" +
    	"// When distortion is enabled, use the normal map to calculate perturbation\n" +
    	"vAccumulatedNormal = texture2D(NormalTex, BumpCoord0).rgb;\n" +
    	"vAccumulatedNormal += texture2D(NormalTex, BumpCoord1).rgb;\n" +
    	"vAccumulatedNormal -= 1.0; // Same as * 2.0 - 2.0\n" +
    	"\n" +
    	"vec2 vTmp = vAccumulatedNormal.xz;\n" +
    	"/* \n" +
    	"Divide by WaterToEyeLength to scale down the distortion\n" +
    	" of fragments based on their distance from the camera \n" +
    	"*/\n" +
    	"vTexCoord.xy -= vTmp * (WaveDistortion / length(WaterToEye));\n" +
    	"#endif\n" +
    	"\n" +
    	"#ifdef ENABLE_REFRACTION\n" +
    	"lowp vec4 vReflectionColour = texture2D(ReflectionTex, vTexCoord);\n" +
    	"lowp vec4 vRefractionColour = texture2D(RefractionTex, vTexCoord);\n" +
    	"\n" +
    	"#ifdef ENABLE_FRESNEL\n" +
    	"// Calculate the Fresnel term to determine amount of reflection for each fragment\n" +
    	"\n" +
    	"// Use normalisation cube map instead of normalize() - See section 3.3.1 of white paper for more info\n" +
    	"vec3 vWaterToEyeCube = textureCube(NormalisationCubeMap,WaterToEye).rgb * 2.0 - 1.0;\n" +
    	"\n" +
    	"float fEyeToNormalAngle = clamp(dot(vWaterToEyeCube, vAccumulatedNormal),0.0,1.0);\n" +
    	"\n" +
    	"float fAirWaterFresnel = 1.0 - fEyeToNormalAngle;\n" +
    	"fAirWaterFresnel = pow(fAirWaterFresnel, 5.0);\n" +
    	"fAirWaterFresnel = (0.98 * fAirWaterFresnel) + 0.02;// R(0)-1 = ~0.98 , R(0)= ~0.02\n" +
    	"float fTemp = fAirWaterFresnel;\n" +
    	"\n" +
    	"// Blend reflection and refraction\n" +
    	"gl_FragColor = mix(vRefractionColour, vReflectionColour, fTemp);\n" +
    	"#else\n" +
    	"gl_FragColor = mix(vRefractionColour, vReflectionColour, 0.4);// Constant mix\n" +
    	"#endif\n" +
    	"#else\n" +
    	"gl_FragColor = texture2D(ReflectionTex, vTexCoord);// Reflection only\n" +
    	"#endif\n" +
    	"}\n";
}
