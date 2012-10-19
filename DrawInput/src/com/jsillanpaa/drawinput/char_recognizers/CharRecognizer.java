package com.jsillanpaa.drawinput.char_recognizers;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

import com.jsillanpaa.drawinput.hwr.HwrCharacter;
import com.jsillanpaa.drawinput.hwr.InputMode;

public abstract class CharRecognizer {
	
	public interface CharRecognizerListener{
		void onNoResult();
		void onRecognizedChar(CharRecognitionResult result);
		void onNewInputModeLoaded(InputMode mode);
		void onNewInputModeLoading(InputMode mode);
	}

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
	
	protected ArrayList<CharRecognizerListener> mListeners;
	protected InputMode mInputMode;
	protected Context mContext;

	public CharRecognizer(Context context, CharRecognizerListener l) {
		mContext = context;
		mListeners = new ArrayList<CharRecognizerListener>();
		mListeners.add(l);
	}
	
	public void addListener(CharRecognizerListener l){
		mListeners.add(l);
	}
	
	public abstract void tryRecognition(HwrCharacter ch);
	public abstract void setInputMode(InputMode mode);
	
	protected void notifyRecognizedChar(CharRecognitionResult result) {
		for (CharRecognizerListener l : mListeners) {
			l.onRecognizedChar(result);
		}
	}

	protected void notifyNoResult() {
		for (CharRecognizerListener l : mListeners) {
			l.onNoResult();
		}
	}
	protected void notifyNewInputModeLoaded(InputMode mode) {
		for (CharRecognizerListener l : mListeners) {
			l.onNewInputModeLoaded(mode);
		}
	}
	protected void notifyNewInputModeLoading(InputMode mode) {
	
		for (CharRecognizerListener l : mListeners) {
			l.onNewInputModeLoading(mode);
		}
	}
	
	
}
