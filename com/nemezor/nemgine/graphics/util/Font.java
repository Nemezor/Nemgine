package com.nemezor.nemgine.graphics.util;

import java.util.HashMap;

import com.nemezor.nemgine.misc.Registry;

public class Font {

	public HashMap<Character, GLCharacter> chars;
	public int textureId;
	public java.awt.Font font;
	public int state;
	
	public Font() {
		state = Registry.INVALID;
	}
	
	public void initializeFont(HashMap<Character, GLCharacter> chars, int texId, java.awt.Font font) {
		this.chars = chars;
		this.textureId = texId;
		this.font = font;
		this.state = 0;
	}
}