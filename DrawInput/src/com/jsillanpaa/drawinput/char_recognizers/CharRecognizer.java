package com.jsillanpaa.drawinput.char_recognizers;

import java.util.ArrayList;

import android.content.Context;
import com.jsillanpaa.drawinput.hwr.HwrCharacter;
import com.jsillanpaa.drawinput.hwr.InputMode;

public abstract class CharRecognizer {
	
	public interface CharRecognizerListener{
		void onNoResult();
		void onRecognizedChar(CharRecognitionResult result);
		void onNewInputModeLoaded(InputMode mode);
		void onNewInputModeLoading(InputMode mode);
	}

	protected ArrayList<CharRecognizerListener> mListeners;
	protected InputMode mInputMode = null;
	protected Context mContext;

	protected int mCanvasWidth;
	protected int mCanvasHeight;

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
		if(result != null){
			for (CharRecognizerListener l : mListeners) {
				l.onRecognizedChar(result);
			}	
		}	
	}
	protected void notifyRecognizedChar(char ch) {
		CharRecognitionResult result = new CharRecognitionResult(ch);
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


	public void setCanvasWidth(int w) {
		mCanvasWidth = w;
	}

	public void setCanvasHeight(int h) {
		mCanvasHeight = h;
	}

	public InputMode getInputMode() {
		return mInputMode;
	}
	
	public abstract boolean isLoaded(InputMode m);
	
}
