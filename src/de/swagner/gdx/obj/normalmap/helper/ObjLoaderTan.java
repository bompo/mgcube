package de.swagner.gdx.obj.normalmap.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class ObjLoaderTan {
	static public Mesh loadObj(FileHandle file) {

	      return loadObjFromStringTanBiTan(file);
	      
	   }
	   
	   public static Mesh loadObjFromStringTanBiTan (FileHandle file) {
		   String line = "";
		   int maxVert = 60000;
	        float[] vertices = new float[60000];
	        float[] normals = new float[60000];
	        float[] uv = new float[60000];
	        float[] tangents = new float[60000];
	        float[] bitangents = new float[60000];

	        int numVertices = 0;
	        int numNormals = 0;
	        int numUV = 0;
	        int numTangents = 0;
	        int numBitangents = 0;
	        int numFaces = 0;

	        int[] facesVerts = new int[60000];
	        int[] facesNormals = new int[60000];
	        int[] facesUV = new int[60000];
	        int[] facesTangent = new int[60000];
	        int[] facesBitangent = new int[60000];
	        int vertexIndex = 0;
	        int normalIndex = 0;
	        int uvIndex = 0;
	        int tangentIndex = 0;
	        int bitangentIndex = 0;
	        int faceIndex = 0;

	        BufferedReader reader = new BufferedReader(new InputStreamReader(
					file.read()), 4096);
			try {
				while ((line = reader.readLine()) != null) {
	                if (line.startsWith("v ")) {
	                        String[] tokens = line.split("[ ]+");
	                        vertices[vertexIndex] = Float.parseFloat(tokens[1]);
	                        vertices[vertexIndex + 1] = Float.parseFloat(tokens[2]);
	                        vertices[vertexIndex + 2] = Float.parseFloat(tokens[3]);
	                        vertexIndex += 3;
	                        numVertices++;
	                        continue;
	                }

	                if (line.startsWith("vn ")) {
	                        String[] tokens = line.split("[ ]+");
	                        normals[normalIndex] = Float.parseFloat(tokens[1]);
	                        normals[normalIndex + 1] = Float.parseFloat(tokens[2]);
	                        normals[normalIndex + 2] = Float.parseFloat(tokens[3]);
	                        
	                        normalIndex += 3;
	                        numNormals++;
	                        continue;
	                }

	                if (line.startsWith("vt")) {
	                        String[] tokens = line.split("[ ]+");
	                        uv[uvIndex] = Float.parseFloat(tokens[1]);
	                        uv[uvIndex + 1] = Float.parseFloat(tokens[2]);
	                        uvIndex += 2;
	                        numUV++;
	                        continue;
	                }

	                if (line.startsWith("f ")) {
	                        String[] tokens = line.split("[ ]+");

	                        String[] parts = tokens[1].split("/");
	                        facesVerts[faceIndex] = getIndex(parts[0], numVertices);
	                        
	                        if (parts.length > 2) {
	                           facesNormals[faceIndex] = getIndex(parts[2], numNormals);
	                        }
	                        if (parts.length > 1) {
	                           facesUV[faceIndex] = getIndex(parts[1], numUV);
	                        }
	                        faceIndex++;

	                        parts = tokens[2].split("/");
	                        facesVerts[faceIndex] = getIndex(parts[0], numVertices);
	                        
	                        if (parts.length > 2) {
	                           facesNormals[faceIndex] = getIndex(parts[2], numNormals);
	                        }
	                        if (parts.length > 1) {
	                           facesUV[faceIndex] = getIndex(parts[1], numUV);
	                        }
	                        faceIndex++;

	                        parts = tokens[3].split("/");
	                        facesVerts[faceIndex] = getIndex(parts[0], numVertices);
	                        
	                        if (parts.length > 2) {
	                           facesNormals[faceIndex] = getIndex(parts[2], numNormals);
	                        }
	                        if (parts.length > 1) {
	                           facesUV[faceIndex] = getIndex(parts[1], numUV);
	                        }
	                        faceIndex++;
	                        numFaces++;
	                        continue;
	                }
	        }
	        
	        Vector3 triVertex1 = new Vector3();
	        Vector3 triVertex2 = new Vector3();
	        Vector3 triVertex3 = new Vector3();
	        
	        Vector3 triNormal1 = new Vector3();
	        Vector3 triNormal2 = new Vector3();
	        Vector3 triNormal3 = new Vector3();
	        
	        Vector2 triUv1 = new Vector2();
	        Vector2 triUv2 = new Vector2();
	        Vector2 triUv3 = new Vector2();
	        
	        Vector3 edge1 = new Vector3();
	        Vector3 edge2 = new Vector3();
	        
	        Vector2 texEdge1 = new Vector2();
	        Vector2 texEdge2 = new Vector2();
	        
	        Vector3 tempTangent = new Vector3();
	        Vector3 tempBitangent = new Vector3();
	        
	        if (numNormals > 0) {
	           
	           for(int i = 0; i < numFaces; i++) {
	              
	              int triNormalIndex1 = facesNormals[i*3];
	              int triNormalIndex2 = facesNormals[i*3+1];
	              int triNormalIndex3 = facesNormals[i*3+2];
	              
	              triNormal1 = new Vector3(normals[triNormalIndex1*3], normals[triNormalIndex1*3+1], normals[triNormalIndex1*3+2]);
	              triNormal2 = new Vector3(normals[triNormalIndex2*3], normals[triNormalIndex2*3+1], normals[triNormalIndex2*3+2]);
	              triNormal3 = new Vector3(normals[triNormalIndex3*3], normals[triNormalIndex3*3+1], normals[triNormalIndex3*3+2]);
	              
	              int triVertex1index = facesVerts[i*3];
	              int triVertex2index = facesVerts[i*3+1];
	              int triVertex3index = facesVerts[i*3+2];
	              
	              triVertex1 = new Vector3(vertices[triVertex1index*3], vertices[triVertex1index*3+1], vertices[triVertex1index*3+2]);
	              triVertex2 = new Vector3(vertices[triVertex2index*3], vertices[triVertex2index*3+1], vertices[triVertex2index*3+2]);
	              triVertex3 = new Vector3(vertices[triVertex3index*3], vertices[triVertex3index*3+1], vertices[triVertex3index*3+2]);
	              
	              int triUv1index = facesUV[i*3];
	              int triUv2index = facesUV[i*3+1];
	              int triUv3index = facesUV[i*3+2];
	              
	              triUv1 = new Vector2(uv[triUv1index*2], uv[triUv1index*2+1]);
	              triUv2 = new Vector2(uv[triUv2index*2], uv[triUv2index*2+1]);
	              triUv3 = new Vector2(uv[triUv3index*2], uv[triUv3index*2+1]);
	              
	              //math starts here.....
	              
	              edge1.x = triVertex2.x - triVertex1.x;
	               edge1.y = triVertex2.y - triVertex1.y;
	               edge1.z = triVertex2.z - triVertex1.z;

	               edge2.x = triVertex3.x - triVertex1.x;
	               edge2.y = triVertex3.y - triVertex1.y;
	               edge2.z = triVertex3.z - triVertex1.z;

	               texEdge1.x = triUv2.x - triUv1.x;
	               texEdge1.y = triUv2.y - triUv1.y;

	               texEdge2.x = triUv3.x - triUv1.x;
	               texEdge2.y = triUv3.y - triUv1.y;

	               float det = texEdge1.x * texEdge2.y - texEdge2.x * texEdge1.y;
	              
	               if (Math.abs(det) < 1e-6f)
	               {
	                  tempTangent.x = 1.0f;
	                  tempTangent.y = 0.0f;
	                  tempTangent.z = 0.0f;

	                  tempBitangent.x = 0.0f;
	                  tempBitangent.y = 1.0f;
	                  tempBitangent.z = 0.0f;
	               }
	               else
	               {
	                   det = 1.0f / det;

	                   tempTangent.x = (texEdge2.y * edge1.x - texEdge1.y * edge2.x) * det;
	                   tempTangent.y = (texEdge2.y * edge1.y - texEdge1.y * edge2.y) * det;
	                   tempTangent.z = (texEdge2.y * edge1.z - texEdge1.y * edge2.z) * det;

	                   tempBitangent.x = (-texEdge2.x * edge1.x + texEdge1.x * edge2.x) * det;
	                   tempBitangent.y = (-texEdge2.x * edge1.y + texEdge1.x * edge2.y) * det;
	                   tempBitangent.z = (-texEdge2.x * edge1.z + texEdge1.x * edge2.z) * det;
	               }
	              
	               tangents[triNormalIndex1*3] += tempTangent.x;
	               tangents[triNormalIndex1*3+1] += tempTangent.y;
	               tangents[triNormalIndex1*3+2] += tempTangent.z;
	               bitangents[triNormalIndex1*3] += tempBitangent.x;
	               bitangents[triNormalIndex1*3+1] += tempBitangent.y;
	               bitangents[triNormalIndex1*3+2] += tempBitangent.z;

	               tangents[triNormalIndex2*3] += tempTangent.x;
	               tangents[triNormalIndex2*3+1] += tempTangent.y;
	               tangents[triNormalIndex2*3+2] += tempTangent.z;
	               bitangents[triNormalIndex2*3] += tempBitangent.x;
	               bitangents[triNormalIndex2*3+1] += tempBitangent.y;
	               bitangents[triNormalIndex2*3+2] += tempBitangent.z;

	               tangents[triNormalIndex3*3] += tempTangent.x;
	               tangents[triNormalIndex3*3+1] += tempTangent.y;
	               tangents[triNormalIndex3*3+2] += tempTangent.z;
	               bitangents[triNormalIndex3*3] += tempBitangent.x;
	               bitangents[triNormalIndex3*3+1] += tempBitangent.y;
	               bitangents[triNormalIndex3*3+2] += tempBitangent.z;
	           }
	        }
	        
	        for (int i = 0; i < numNormals; i++)
	        {
	            int vertex = i*3;

	            float nDotT = normals[vertex] * tangents[vertex] +
	                    normals[vertex+1] * tangents[vertex+1] +
	                    normals[vertex+2] * tangents[vertex+2];

	            tangents[vertex] -= normals[vertex] * nDotT;
	            tangents[vertex+1] -= normals[vertex+1] * nDotT;
	            tangents[vertex+2] -= normals[vertex+2] * nDotT;

	            float length = (float) (1.0f / Math.sqrt(tangents[vertex] * tangents[vertex] +
	                                  tangents[vertex+1] * tangents[vertex+1] +
	                                  tangents[vertex+2] * tangents[vertex+2]));

	            tangents[vertex] *= length;
	            tangents[vertex+1] *= length;
	            tangents[vertex+2] *= length;

	            float bitangent0 = (normals[vertex+1] * tangents[vertex+2]) - 
	                           (normals[vertex+2] * tangents[vertex+1]);
	            float bitangent1 = (normals[vertex+2] * tangents[vertex]) -
	                           (normals[vertex] * tangents[vertex+2]);
	            float bitangent2 = (normals[vertex] * tangents[vertex+1]) - 
	                           (normals[vertex+1] * tangents[vertex]);

	            float bDotB = bitangent0 * bitangents[0] + 
	                          bitangent1 * bitangents[1] + 
	                          bitangent2 * bitangents[2];

	            if(bDotB < 0) {
	               bitangents[0] = -bitangent0;
	               bitangents[1] = -bitangent1;
	               bitangents[2] = -bitangent2;
	            }
	            else {
	               bitangents[0] = bitangent0;
	                bitangents[1] = bitangent1;
	                bitangents[2] = bitangent2;
	            }
	        }
	        
	        
	        float[] verts = new float[(numFaces * 3) * (3 + (numNormals > 0 ? 3 : 0) + (numUV > 0 ? 2 : 0)
	              + /*for tangent*/(numNormals > 0 ? 3 : 0) + /*for bitangent*/(numNormals > 0 ? 3 : 0))];

	        for (int i = 0, vi = 0; i < numFaces * 3; i++) {
	                int vertexIdx = facesVerts[i] * 3;
	                verts[vi++] = vertices[vertexIdx];
	                verts[vi++] = vertices[vertexIdx + 1];
	                verts[vi++] = vertices[vertexIdx + 2];

	                if (numNormals > 0) {
	                    int normalIdx = facesNormals[i] * 3;
	                    verts[vi++] = normals[normalIdx];
	                    verts[vi++] = normals[normalIdx + 1];
	                    verts[vi++] = normals[normalIdx + 2];
	                }
	                if (numUV > 0) {
	                    int uvIdx = facesUV[i] * 2;
	                    verts[vi++] = uv[uvIdx];
	                    verts[vi++] = uv[uvIdx + 1];
	                }
	                if (numNormals > 0) {
	                   int normalIdx = facesNormals[i] * 3;
	                    verts[vi++] = tangents[normalIdx];
	                    verts[vi++] = tangents[normalIdx + 1];
	                    verts[vi++] = tangents[normalIdx + 2];
	                    
	                    verts[vi++] = bitangents[normalIdx];
	                    verts[vi++] = bitangents[normalIdx + 1];
	                    verts[vi++] = bitangents[normalIdx + 2];
	                }
	        }

	        Mesh mesh = null;

	        ArrayList<VertexAttribute> attributes = new ArrayList<VertexAttribute>();
	        attributes.add(new VertexAttribute(Usage.Position, 3, "a_Position"));
	        if (numNormals > 0) attributes.add(new VertexAttribute(Usage.Normal, 3, "a_Normal"));
	        if (numUV > 0) attributes.add(new VertexAttribute(Usage.TextureCoordinates, 2, "a_TexCoord"));
	        
	        if (numNormals > 0) {
	           attributes.add(new VertexAttribute(10, 3, "a_Tangent"));
	           attributes.add(new VertexAttribute(11, 3, "a_Bitangent"));
	        }

	        mesh = new Mesh(true, numFaces * 3, 0, attributes.toArray(new VertexAttribute[attributes.size()]));
	        mesh.setVertices(verts);
	    	reader.close();
	    	 return mesh; 
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
	       
	   }
	   
	   private static int getIndex (String index, int size) {
	        if (index == null || index.length() == 0) return 0;
	        int idx = Integer.parseInt(index);
	        if (idx < 0)
	                return size + idx;
	        else
	                return idx - 1;
	   }
	}