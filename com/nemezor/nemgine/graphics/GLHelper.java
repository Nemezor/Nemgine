package com.nemezor.nemgine.graphics;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.nemezor.nemgine.graphics.util.Camera;
import com.nemezor.nemgine.main.Nemgine;
import com.nemezor.nemgine.misc.Registry;
import com.nemezor.nemgine.misc.Side;

public class GLHelper {

	protected static float zNear;
	protected static float zFar;
	protected static float aspect;
	protected static float FOV;
	
	private static Matrix4f projection = new Matrix4f();
	
	private GLHelper() {}
	
	protected static void updatePerspectiveProjection() {
		projection = initPerspectiveProjectionMatrix(FOV, aspect, zNear, zFar);
	}
	
	public static Matrix4f getCurrentPerspectiveProjectionMatrix() {
		return projection;
	}
	
	public static Matrix4f initTransformationMatrix(Camera camera, Vector3f translation, Vector3f rotation, Vector3f scale) {
		Matrix4f mat = initTransformationMatrix(translation, rotation, scale);
		Matrix4f mat2 = initViewMatrix(camera);
		return Matrix4f.mul(mat2, mat, new Matrix4f());
	}
	
	public static Matrix4f initViewMatrix(Camera camera) {
		Matrix4f mat = new Matrix4f();
		Vector3f rotation = camera.getRotation();
		
		Matrix4f.rotate(rotation.getX(), new Vector3f(1, 0, 0), mat, mat);
		Matrix4f.rotate(rotation.getY(), new Vector3f(0, 1, 0), mat, mat);
		Matrix4f.rotate(rotation.getZ(), new Vector3f(0, 0, 1), mat, mat);
		Matrix4f.translate(camera.getPosition().negate(new Vector3f()), mat, mat);
		
		return mat;
	}
	
	public static Matrix4f initTransformationMatrix(Vector3f translation, Vector3f rotation, Vector3f scale) {
		Matrix4f mat = new Matrix4f();

		Matrix4f.translate(translation, mat, mat);
		Matrix4f.rotate(rotation.getY(), new Vector3f(0, 1, 0), mat, mat);
		Matrix4f.rotate(rotation.getZ(), new Vector3f(0, 0, 1), mat, mat);
		Matrix4f.rotate(rotation.getX(), new Vector3f(1, 0, 0), mat, mat);
		Matrix4f.scale(scale, mat, mat);
		
		return mat;
	}
	
	public static Matrix4f initPerspectiveProjectionMatrix(float fieldOfView, float width, float height, float zNear, float zFar) {
		float a = width / height;
		return initPerspectiveProjectionMatrix(fieldOfView, a, zNear, zFar);
	}
	
	public static Matrix4f initPerspectiveProjectionMatrix(float fieldOfView, float aspectRatio, float zNear, float zFar) {
		Matrix4f mat = new Matrix4f();
		float fov = 1.0f / ((float)Math.tan(fieldOfView / 2.0f));
		float zp = zFar + zNear;
		float zm = zFar - zNear;
		
		mat.m00 = fov / aspectRatio;
		mat.m11 = fov;
		mat.m22 = -zp / zm;
		mat.m23 = -1.0f;
		mat.m32 = -(2.0f * zFar * zNear) / zm;
		mat.m33 = 0.0f;
		
		return mat;
	}
	
	public static Matrix4f initOrthographicProjectionMatrix(float left, float right, float top, float bottom, float zNear, float zFar) {
		Matrix4f mat = new Matrix4f();
		
		mat.m00 = 2.0f / (right - left);
		mat.m11 = 2.0f / (top - bottom);
		mat.m22 = -2.0f / (zFar - zNear);
		mat.m30 = -((right + left) / (right - left));
		mat.m31 = -((top + bottom) / (top - bottom));
		mat.m32 = -((zFar + zNear) / (zFar - zNear));
		
		return mat;
	}
	
	public static int createBufferAndStore(int attrNum, int size, float[] data) {
		if (Nemgine.getSide() == Side.SERVER) {
			return Registry.INVALID;
		}
		int vbo = GL15.glGenBuffers();
		FloatBuffer buf = BufferUtils.createFloatBuffer(data.length);
		buf.put(data);
		buf.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(attrNum, size, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		return vbo;
	}
}
