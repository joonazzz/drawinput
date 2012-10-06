package com.jsillanpaa.drawinput.hwr;

public enum InputMode {
	NUMBER, BIG_LETTERS, SMALL_LETTERS, SPECIAL_CHARS;
	
	@Override
	public String toString() {
		switch (this) {
		case NUMBER:
			return "123";
		case BIG_LETTERS:
			return "ABC";
		case SMALL_LETTERS:
			return "abc";
		case SPECIAL_CHARS:
			return "*";
		default:
			return "";
		}
		
	};
	
}
