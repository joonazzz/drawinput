package com.jsillanpaa.drawinput.char_recognizers;

public class CharRecognitionResult{
	private char mChar;
	private double mConfidence;

	public CharRecognitionResult() {
		this('-', 0.0);
	}
	public CharRecognitionResult(char ch) {
		this(ch, 0.0);
	}
	
	public CharRecognitionResult(char ch, double confidence) {
		mChar = ch;
		mConfidence = confidence;
	}
	public char getChar() {
		return mChar;
	}
	public double getConfidence() {
		return mConfidence;
	}
	
}