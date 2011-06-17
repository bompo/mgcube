package de.swagner.gdx.obj.normalmap.shader;

public class NormalMapThreeJsShader {

    public static final String mVertexShader =
    	"attribute vec4 inVertex;\n"+
    	"attribute vec3 inNormal;\n"+
    	"attribute vec2 inTexCoord;\n"+
    	"attribute vec3 inTangent;\n"+
    	"uniform mat4 objectMatrix;\n"+
    	"uniform mat4 projectionMatrix;\n"+
    	"uniform mat4 modelViewMatrix;\n"+
    	"uniform vec3 cameraPosition;\n"+
		"uniform vec3 uPointLightPos;\n"+
		"varying vec3 vTangent;\n"+
		"varying vec3 vBinormal;\n"+
		"varying vec3 vNormal;\n"+
		"varying vec2 vUv;\n"+
		"varying vec3 vPointLightVector;\n"+
		"varying vec3 vViewPosition;\n"+
		"void main() {\n"+
			"vec4 mPosition = objectMatrix * inVertex;\n"+
			"vViewPosition = cameraPosition - mPosition.xyz;\n"+
			"vec4 mvPosition = modelViewMatrix * inVertex;\n"+
			"vNormal = inNormal;\n"+
			"vTangent = inTangent;\n"+
			"vBinormal = cross( vNormal, vTangent );\n"+
			"vBinormal = normalize( vBinormal );\n"+
			"vUv = inTexCoord;\n"+
			"vec4 lPosition = modelViewMatrix * vec4( uPointLightPos, 1.0 );\n"+
			"vPointLightVector = normalize( lPosition.xyz - mvPosition.xyz );\n"+
			"gl_Position = projectionMatrix * mvPosition;\n"+
		"}\n";

    public static final String mFragmentShader =   
        "#ifdef GL_ES\n" +
        "precision mediump float;\n" +
        "#endif\n" +
		"uniform vec3 uDirLightPos;\n"+
		"uniform vec3 uAmbientLightColor;\n"+
		"uniform vec3 uDirLightColor;\n"+
		"uniform vec3 uPointLightColor;\n"+
		"uniform vec3 uAmbientColor;\n"+
		"uniform vec3 uDiffuseColor;\n"+
		"uniform vec3 uSpecularColor;\n"+
		"uniform mat4 viewMatrix;\n"+
		"uniform float uShininess;\n"+
		"uniform sampler2D tDiffuse;\n"+
		"uniform sampler2D tNormal;\n"+
		"uniform float uNormalScale;\n"+
		"varying vec3 vTangent;\n"+
		"varying vec3 vBinormal;\n"+
		"varying vec3 vNormal;\n"+
		"varying vec2 vUv;\n"+
		"varying vec3 vPointLightVector;\n"+
		"varying vec3 vViewPosition;\n"+
		"void main() {\n"+
			"vec3 diffuseTex = vec3( 1.0, 1.0, 1.0 );\n"+
			"vec3 normalTex = texture2D( tNormal, vUv ).xyz * 2.0 - 1.0;\n"+
			"normalTex.xy *= uNormalScale;\n"+
			"normalTex = normalize( normalTex );\n"+
			"diffuseTex = texture2D( tDiffuse, vUv ).xyz;\n"+
			"mat3 tsb = mat3( vTangent, vBinormal, vNormal );\n"+
			"vec3 finalNormal = tsb * normalTex;\n"+
			"vec3 normal = normalize( finalNormal );\n"+
			"vec3 viewPosition = normalize( vViewPosition );\n"+
			"vec4 pointDiffuse  = vec4( 0.0, 0.0, 0.0, 0.0 );\n"+
			"vec4 pointSpecular = vec4( 0.0, 0.0, 0.0, 0.0 );\n"+
			"vec3 pointVector = normalize( vPointLightVector );\n"+
			"vec3 pointHalfVector = normalize( vPointLightVector + vViewPosition );\n"+
			"float pointDotNormalHalf = dot( normal, pointHalfVector );\n"+
			"float pointDiffuseWeight = max( dot( normal, pointVector ), 0.0 );\n"+
			"float pointSpecularWeight = 0.0;\n"+
			"if ( pointDotNormalHalf >= 0.0 )\n"+
				"pointSpecularWeight = pow( pointDotNormalHalf, uShininess );\n"+
			"pointDiffuse  += vec4( uDiffuseColor, 1.0 ) * pointDiffuseWeight;\n"+
			"pointSpecular += vec4( uSpecularColor, 1.0 ) * pointSpecularWeight;\n"+
			"vec4 dirDiffuse  = vec4( 0.0, 0.0, 0.0, 0.0 );\n"+
			"vec4 dirSpecular = vec4( 0.0, 0.0, 0.0, 0.0 );\n"+
			"vec4 lDirection = viewMatrix * vec4( uDirLightPos, 0.0 );\n"+
			"vec3 dirVector = normalize( lDirection.xyz );\n"+
			"vec3 dirHalfVector = normalize( lDirection.xyz + vViewPosition );\n"+
			"float dirDotNormalHalf = dot( normal, dirHalfVector );\n"+
			"float dirDiffuseWeight = max( dot( normal, dirVector ), 0.0 );\n"+
			"float dirSpecularWeight = 0.0;\n"+
			"if ( dirDotNormalHalf >= 0.0 )\n"+
				"dirSpecularWeight = pow( dirDotNormalHalf, uShininess );\n"+
			"dirDiffuse  += vec4( uDiffuseColor, 1.0 ) * dirDiffuseWeight;\n"+
			"dirSpecular += vec4( uSpecularColor, 1.0 ) * dirSpecularWeight;\n"+
			"vec4 totalLight = vec4( uAmbientLightColor * uAmbientColor, 1.0 );\n"+
			"totalLight += vec4( uDirLightColor, 1.0 ) * ( dirDiffuse + dirSpecular );\n"+
			"totalLight += vec4( uPointLightColor, 1.0 ) * ( pointDiffuse + pointSpecular );\n"+
			"gl_FragColor =  vec4( uPointLightColor, 1.0 );\n"+
		"}\n";
}
