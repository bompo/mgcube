package de.swagner.gdx.obj.normalmap.shader;

public class NormalMapShader {

    public static final String mVertexShader =
    "uniform mat4 u_matViewProjection;\n" +
    "uniform vec3 u_lightPosition;\n" +
    "uniform vec3 u_eyePosition;\n" +

    "varying vec2 v_texcoord;\n" +
    "varying vec3 v_viewDirection;\n" +
    "varying vec3 v_lightDirection;\n" +

    "attribute vec4 a_vertex;\n" +
    "attribute vec2 a_texcoord0;\n" +
    "attribute vec3 a_normal;\n" +
    "attribute vec3 a_binormal;\n" +
    "attribute vec3 a_tangent;\n" +
       
    "void main( void ) {\n" +
       "// Transform eye vector into world space\n" +
      "vec3 eyePositionWorld = u_eyePosition;\n" +
       
      "// Compute world-space direction vector\n" +
       "vec3 viewDirectionWorld = eyePositionWorld - a_vertex.xyz;\n" +
          
       "// Transform light position into world space\n" +
       "vec3 lightPositionWorld = u_lightPosition; \n" +  
       
       "// Compute world-space light dirction vector\n" +
       "vec3 lightDirectionWorld = lightPositionWorld - a_vertex.xyz;\n" +
       
       "// Create the tangent matrix\n" +
       "mat3 tangentMat = mat3( a_tangent, a_binormal, a_normal );\n" +   
       
       "// Transform the view and light vectors into tangent space\n" +
       "v_viewDirection = viewDirectionWorld * tangentMat;\n" +
       "v_lightDirection = lightDirectionWorld * tangentMat;\n" +
          
       "// Transform output position\n" +
       "gl_Position = u_matViewProjection * a_vertex;\n" +
      
       "// Pass through texture coordinate\n" +
       "v_texcoord = a_texcoord0.xy;\n" +
    "}\n";

    public static final String mFragmentShader =  

    "uniform vec4 u_ambient;\n" +
    "uniform vec4 u_specular;\n" +
    "uniform vec4 u_diffuse;\n" +
    "uniform float u_specularPower;\n" +

    "uniform sampler2D s_baseMap;\n" +
    "uniform sampler2D s_bumpMap;\n" +

    "varying vec2 v_texcoord;\n" +
    "varying vec3 v_viewDirection;\n" +
    "varying vec3 v_lightDirection;\n" +

    "void main( void ) {\n" +
       "// Fetch basemap color\n" +
       "vec4 baseColor = texture2D( s_baseMap, v_texcoord );\n" +
       
       "// Fetch the tangent space normal from normal map\n" +
       "vec3 normal = texture2D( s_bumpMap, v_texcoord ).xyz;\n" +
       
       "// Scale and bias from [0, 1] to [-1, 1] and normalize\n" +
       "normal = normalize( normal * 2.0 - 1.0 );\n" +
       
       "// Normalize the light direction and view direction\n" +
       "vec3 lightDirection = normalize( v_lightDirection );\n" +
       "vec3 viewDirection = normalize( v_viewDirection );\n" +
       
       "// Compute N.L\n" +
       "float nDotL = dot( normal, lightDirection );\n" +
       
       "// Compute reflection vector\n" +
       "vec3 reflection = ( 2.0 * normal * nDotL ) - lightDirection;\n" +
       
       "// Compute R.V\n" +
       "float rDotV = max( 0.0, dot( reflection, viewDirection ) );\n" +
       
       "// Compute Ambient term\n" +
       "vec4 ambient = u_ambient * baseColor;\n" +
       
       "// Compute Diffuse term\n" +
       "vec4 diffuse = u_diffuse * nDotL * baseColor;\n" +
       
       "// Compute Specular term\n" +
       "vec4 specular = u_specular * pow( rDotV, u_specularPower );\n" +
       
       "// Output final color\n" +
       "gl_FragColor =  clamp( ambient + diffuse + specular, 0.0, 1.0 );\n" +
    "}\n";
}
