package com.nemezor.nemgine.graphics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Iterator;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix2f;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.nemezor.nemgine.misc.EnumShaderType;
import com.nemezor.nemgine.misc.NemgineShaderException;
import com.nemezor.nemgine.misc.Registry;

public class ShaderManager {

	private static HashMap<Integer, Shader> shaders = new HashMap<Integer, Shader>();
	private static int shaderCounter = 0;
	private static int currentShader = 0;
	private static Shader currentShaderData = null;

	public static int generateShaders() {
		shaderCounter++;
		shaders.put(shaderCounter, new Shader());
		return shaderCounter;
	}

	public static void disposeShader(int id) {
		if (currentShader == id) {
			GL20.glUseProgram(0);
		}
		Shader shader = shaders.get(id);
		if (shader == null) {
			return;
		}
		GL20.glDetachShader(shader.progID, shader.vertID);
		GL20.glDetachShader(shader.progID, shader.fragID);
		GL20.glDeleteShader(shader.vertID);
		GL20.glDeleteShader(shader.fragID);
		GL20.glDeleteProgram(shader.progID);
		shaders.remove(id);
	}

	public static void disposeAll() {
		GL20.glUseProgram(0);
		Iterator<Integer> keys = shaders.keySet().iterator();
		while (keys.hasNext()) {
			Shader shader = shaders.get(keys.next());
			GL20.glDetachShader(shader.progID, shader.vertID);
			GL20.glDetachShader(shader.progID, shader.fragID);
			GL20.glDeleteShader(shader.vertID);
			GL20.glDeleteShader(shader.fragID);
			GL20.glDeleteProgram(shader.progID);
		}
		shaders.clear();
	}

	public static void bindShader(int id) {
		if (currentShader == id) {
			return;
		}
		Shader shader = shaders.get(id);
		if (shader == null) {
			return;
		}
		currentShader = shader.progID;
		currentShaderData = shader;
		GL20.glUseProgram(shader.progID);
	}

	public static void unbindShader() {
		if (currentShader == 0) {
			return;
		}
		currentShader = 0;
		currentShaderData = null;
		GL20.glUseProgram(0);
	}

	public static void loadMatrix4(int id, String name, Matrix4f data) {
		if (id != currentShader) {
			return;
		}
		FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
		data.store(buffer);
		buffer.flip();
		GL20.glUniformMatrix4(currentShaderData.data.get(name), false, buffer);
	}

	public static void loadMatrix3(int id, String name, Matrix3f data) {
		if (id != currentShader) {
			return;
		}
		FloatBuffer buffer = BufferUtils.createFloatBuffer(9);
		data.store(buffer);
		buffer.flip();
		GL20.glUniformMatrix3(currentShaderData.data.get(name), false, buffer);
	}

	public static void loadMatrix2(int id, String name, Matrix2f data) {
		if (id != currentShader) {
			return;
		}
		FloatBuffer buffer = BufferUtils.createFloatBuffer(4);
		data.store(buffer);
		buffer.flip();
		GL20.glUniformMatrix2(currentShaderData.data.get(name), false, buffer);
	}

	public static void loadVector4(int id, String name, Vector4f data) {
		if (id != currentShader) {
			return;
		}
		GL20.glUniform4f(currentShaderData.data.get(name), data.getX(), data.getY(), data.getZ(), data.getW());
	}

	public static void loadVector3(int id, String name, Vector3f data) {
		if (id != currentShader) {
			return;
		}
		GL20.glUniform3f(currentShaderData.data.get(name), data.getX(), data.getY(), data.getZ());
	}

	public static void loadVector2(int id, String name, Vector2f data) {
		if (id != currentShader) {
			return;
		}
		GL20.glUniform2f(currentShaderData.data.get(name), data.getX(), data.getY());
	}

	public static void loadFloat(int id, String name, float data) {
		if (id != currentShader) {
			return;
		}
		GL20.glUniform1f(currentShaderData.data.get(name), data);
	}

	public static void loadBoolean(int id, String name, boolean data) {
		if (id != currentShader) {
			return;
		}
		GL20.glUniform1f(currentShaderData.data.get(name), data ? 1.0f : 0.0f);
	}

	public static boolean initializeShader(int id, String vertexFile, String fragmentFile, String[] uniforms, int[] binds) {
		if (uniforms.length != binds.length) {
			return false;
		}
		if (currentShader == id) {
			return false;
		}
		Shader shader = shaders.get(id);
		if (shader == null) {
			return false;
		}
		shader.vertID = loadShader(vertexFile, EnumShaderType.VERTEX);
		if (shader.vertID == Registry.INVALID) {
			NemgineShaderException ex = new NemgineShaderException(Registry.SHADER_MANAGER_LOADER_GLOBAL_ERROR);
			ex.setShaderInfo(vertexFile, EnumShaderType.VERTEX);
			ex.setThrower(Registry.SHADER_MANAGER_NAME);
			ex.printStackTrace();
			return false;
		}
		shader.fragID = loadShader(fragmentFile, EnumShaderType.FRAGMENT);
		if (shader.fragID == Registry.INVALID) {
			NemgineShaderException ex = new NemgineShaderException(Registry.SHADER_MANAGER_LOADER_GLOBAL_ERROR);
			ex.setShaderInfo(fragmentFile, EnumShaderType.FRAGMENT);
			ex.setThrower(Registry.SHADER_MANAGER_NAME);
			ex.printStackTrace();
			return false;
		}
		shader.progID = GL20.glCreateProgram();
		GL20.glAttachShader(shader.progID, shader.vertID);
		GL20.glAttachShader(shader.progID, shader.fragID);
		for (int i = 0; i < uniforms.length; i++) {
			GL20.glBindAttribLocation(shader.progID, binds[i], uniforms[i]);
		}
		GL20.glLinkProgram(shader.progID);
		GL20.glValidateProgram(shader.progID);
		for (String uni : uniforms) {
			int uniId = GL20.glGetUniformLocation(shader.progID, uni);
			shader.data.put(uni, uniId);
		}
		return true;
	}

	private static int loadShader(String file, EnumShaderType type) {
		StringBuilder shaderSrc = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				shaderSrc.append(line).append("\n");
			}
			reader.close();
		} catch (IOException e) {
			NemgineShaderException ex = new NemgineShaderException(Registry.SHADER_LOADER_LOAD_ERROR);
			ex.setShaderInfo(file, type);
			ex.setThrower(Registry.SHADER_LOADER_NAME);
			ex.printStackTrace();
			e.printStackTrace();
			return Registry.INVALID;
		}
		int shaderID = GL20.glCreateShader(type.getGlShaderType());
		GL20.glShaderSource(shaderID, shaderSrc);
		GL20.glCompileShader(shaderID);
		if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			NemgineShaderException ex = new NemgineShaderException(Registry.SHADER_LOADER_COMPILE_ERROR);
			ex.setShaderInfo(file, type);
			ex.setThrower(Registry.SHADER_LOADER_NAME);
			ex.printStackTrace();
			System.err.println(GL20.glGetShaderInfoLog(shaderID, Registry.SHADER_ERROR_LOG_SIZE));
			return Registry.INVALID;
		}
		return shaderID;
	}
}
